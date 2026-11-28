package com.pulsecheck;

import com.pulsecheck.dto.IncidentResponse;
import com.pulsecheck.entity.Incident;
import com.pulsecheck.entity.IncidentStatus;
import com.pulsecheck.exception.InvalidIncidentTransitionException;
import com.pulsecheck.event.IncidentResolvedEvent;
import com.pulsecheck.monitor.Monitor;
import com.pulsecheck.repository.IncidentRepository;
import com.pulsecheck.service.IncidentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTests {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private IncidentService incidentService;

    private Incident incident;

    @BeforeEach
    void setUp() {
        Monitor monitor = new Monitor();
        ReflectionTestUtils.setField(monitor, "id", 10L);

        incident = new Incident();
        incident.setId(1L);
        incident.setMonitor(monitor);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCause("HTTP 500");
        incident.setStartedAt(Instant.parse("2026-11-26T10:00:00Z"));
    }

    @Test
    void acknowledgeTransitionsOpenToAcknowledged() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResponse response = incidentService.acknowledge(1L);

        assertEquals(IncidentStatus.ACKNOWLEDGED, response.status());
        assertNotNull(response.acknowledgedAt());
        verify(incidentRepository).save(incident);
    }

    @Test
    void acknowledgeThrowsConflictWhenAlreadyAcknowledged() {
        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(InvalidIncidentTransitionException.class, () -> incidentService.acknowledge(1L));
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void acknowledgeThrowsConflictWhenAlreadyResolved() {
        incident.setStatus(IncidentStatus.RESOLVED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(InvalidIncidentTransitionException.class, () -> incidentService.acknowledge(1L));
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void resolveTransitionsOpenDirectlyToResolvedAndPublishesEvent() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResponse response = incidentService.resolve(1L);

        assertEquals(IncidentStatus.RESOLVED, response.status());
        assertNotNull(response.resolvedAt());

        ArgumentCaptor<IncidentResolvedEvent> captor = ArgumentCaptor.forClass(IncidentResolvedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().incidentId());
        assertEquals(10L, captor.getValue().monitorId());
    }

    @Test
    void resolveTransitionsAcknowledgedToResolved() {
        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResponse response = incidentService.resolve(1L);

        assertEquals(IncidentStatus.RESOLVED, response.status());
        assertNotNull(response.resolvedAt());
        verify(eventPublisher).publishEvent(any(IncidentResolvedEvent.class));
    }

    @Test
    void resolveThrowsConflictWhenAlreadyResolved() {
        incident.setStatus(IncidentStatus.RESOLVED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(InvalidIncidentTransitionException.class, () -> incidentService.resolve(1L));
        verify(incidentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void throwsNotFoundWhenIncidentDoesNotExist() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> incidentService.acknowledge(99L));
    }

    @Test
    void getIncidentsFiltersByStatusWhenProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        when(incidentRepository.findByStatus(IncidentStatus.OPEN, pageable))
                .thenReturn(new PageImpl<>(List.of(incident), pageable, 1));

        Page<IncidentResponse> result = incidentService.getIncidents(IncidentStatus.OPEN, pageable);

        assertEquals(1, result.getTotalElements());
        verify(incidentRepository).findByStatus(IncidentStatus.OPEN, pageable);
        verify(incidentRepository, never()).findAll(pageable);
    }

    @Test
    void getIncidentsReturnsAllWhenStatusIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(incidentRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(incident), pageable, 1));

        Page<IncidentResponse> result = incidentService.getIncidents(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(incidentRepository).findAll(pageable);
        verify(incidentRepository, never()).findByStatus(any(), any());
    }
}