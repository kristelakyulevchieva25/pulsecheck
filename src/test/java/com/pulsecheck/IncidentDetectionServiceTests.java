package com.pulsecheck;

import com.pulsecheck.check.event.CheckCompletedEvent;
import com.pulsecheck.entity.CheckResult;
import com.pulsecheck.entity.Incident;
import com.pulsecheck.entity.IncidentStatus;
import com.pulsecheck.event.IncidentOpenedEvent;
import com.pulsecheck.event.IncidentResolvedEvent;
import com.pulsecheck.monitor.Monitor;
import com.pulsecheck.monitor.repository.MonitorRepository;
import com.pulsecheck.repository.CheckResultRepository;
import com.pulsecheck.repository.IncidentRepository;
import com.pulsecheck.service.IncidentDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentDetectionServiceTests {

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private MonitorRepository monitorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private IncidentDetectionService incidentDetectionService;

    private Monitor monitor;
    private Instant now;

    @BeforeEach
    void setUp() {
        monitor = new Monitor();
        ReflectionTestUtils.setField(monitor, "id", 1L);
        now = Instant.parse("2026-11-26T10:00:00Z");
    }

    private CheckResult checkResult(boolean success, Integer statusCode) {
        return new CheckResult(monitor, success, statusCode, 120L, now);
    }

    @Test
    void doesNotOpenIncidentAfterTwoConsecutiveFailures() {
        when(incidentRepository.findFirstByMonitorIdAndStatusNot(1L, IncidentStatus.RESOLVED))
                .thenReturn(Optional.empty());
        when(checkResultRepository.findTop3ByMonitorIdOrderByCheckedAtDesc(1L))
                .thenReturn(List.of(checkResult(false, 500), checkResult(false, 500)));

        incidentDetectionService.onCheckCompleted(new CheckCompletedEvent(1L, false, 500, 120L, now));

        verify(incidentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void opensIncidentAndPublishesEventAfterThreeConsecutiveFailures() {
        when(incidentRepository.findFirstByMonitorIdAndStatusNot(1L, IncidentStatus.RESOLVED))
                .thenReturn(Optional.empty());
        when(checkResultRepository.findTop3ByMonitorIdOrderByCheckedAtDesc(1L))
                .thenReturn(List.of(
                        checkResult(false, 503),
                        checkResult(false, 500),
                        checkResult(false, 500)
                ));
        when(monitorRepository.findById(1L)).thenReturn(Optional.of(monitor));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident inc = inv.getArgument(0);
            inc.setId(42L);
            return inc;
        });

        incidentDetectionService.onCheckCompleted(new CheckCompletedEvent(1L, false, 503, 120L, now));

        ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(incidentCaptor.capture());

        Incident saved = incidentCaptor.getValue();
        assertEquals(IncidentStatus.OPEN, saved.getStatus());
        assertEquals("Unexpected HTTP status 503", saved.getCause());
        assertEquals(now, saved.getStartedAt());

        ArgumentCaptor<IncidentOpenedEvent> eventCaptor = ArgumentCaptor.forClass(IncidentOpenedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(42L, eventCaptor.getValue().incidentId());
        assertEquals(1L, eventCaptor.getValue().monitorId());
        assertEquals(now, eventCaptor.getValue().startedAt());
    }

    @Test
    void doesNotOpenDuplicateIncidentWhenActiveIncidentAlreadyExists() {
        Incident existing = new Incident();
        existing.setId(42L);
        existing.setStatus(IncidentStatus.OPEN);

        when(incidentRepository.findFirstByMonitorIdAndStatusNot(1L, IncidentStatus.RESOLVED))
                .thenReturn(Optional.of(existing));

        incidentDetectionService.onCheckCompleted(new CheckCompletedEvent(1L, false, 500, 120L, now));

        verify(checkResultRepository, never()).findTop3ByMonitorIdOrderByCheckedAtDesc(any());
        verify(incidentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void doesNotResolveIncidentAfterOnlyOneSuccessfulCheck() {
        Incident active = new Incident();
        active.setId(42L);
        active.setStatus(IncidentStatus.OPEN);

        when(incidentRepository.findFirstByMonitorIdAndStatusNot(1L, IncidentStatus.RESOLVED))
                .thenReturn(Optional.of(active));
        when(checkResultRepository.findTop3ByMonitorIdOrderByCheckedAtDesc(1L))
                .thenReturn(List.of(checkResult(true, 200), checkResult(false, 500)));

        incidentDetectionService.onCheckCompleted(new CheckCompletedEvent(1L, true, 200, 100L, now));

        verify(incidentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void resolvesIncidentAndPublishesEventAfterTwoConsecutiveSuccessfulChecks() {
        Incident active = new Incident();
        active.setId(42L);
        active.setStatus(IncidentStatus.ACKNOWLEDGED);

        when(incidentRepository.findFirstByMonitorIdAndStatusNot(1L, IncidentStatus.RESOLVED))
                .thenReturn(Optional.of(active));
        when(checkResultRepository.findTop3ByMonitorIdOrderByCheckedAtDesc(1L))
                .thenReturn(List.of(
                        checkResult(true, 200),
                        checkResult(true, 200),
                        checkResult(false, 500)
                ));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        incidentDetectionService.onCheckCompleted(new CheckCompletedEvent(1L, true, 200, 95L, now));

        assertEquals(IncidentStatus.RESOLVED, active.getStatus());
        assertEquals(now, active.getResolvedAt());
        verify(incidentRepository).save(active);

        ArgumentCaptor<IncidentResolvedEvent> eventCaptor = ArgumentCaptor.forClass(IncidentResolvedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(42L, eventCaptor.getValue().incidentId());
        assertEquals(1L, eventCaptor.getValue().monitorId());
        assertEquals(now, eventCaptor.getValue().resolvedAt());
    }
}