package com.pulsecheck.service;

import com.pulsecheck.dto.IncidentResponse;
import com.pulsecheck.entity.Incident;
import com.pulsecheck.entity.IncidentStatus;
import com.pulsecheck.exception.InvalidIncidentTransitionException;
import com.pulsecheck.event.IncidentResolvedEvent;
import com.pulsecheck.repository.IncidentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IncidentService(IncidentRepository incidentRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidents(IncidentStatus status, Pageable pageable) {
        Page<Incident> page = (status != null)
                ? incidentRepository.findByStatus(status, pageable)
                : incidentRepository.findAll(pageable);
        return page.map(IncidentResponse::from);
    }

    @Transactional
    public IncidentResponse acknowledge(Long id) {
        Incident incident = findIncidentOrThrow(id);

        if (incident.getStatus() != IncidentStatus.OPEN) {
            throw new InvalidIncidentTransitionException(
                    "Cannot acknowledge incident in status " + incident.getStatus()
            );
        }

        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        incident.setAcknowledgedAt(Instant.now());

        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentResponse resolve(Long id) {
        Incident incident = findIncidentOrThrow(id);

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            throw new InvalidIncidentTransitionException(
                    "Incident is already RESOLVED"
            );
        }

        Instant now = Instant.now();
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(now);

        Incident saved = incidentRepository.save(incident);

        eventPublisher.publishEvent(new IncidentResolvedEvent(
                saved.getId(),
                saved.getMonitor().getId(),
                saved.getResolvedAt()
        ));

        return IncidentResponse.from(saved);
    }

    private Incident findIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found: " + id));
    }
}