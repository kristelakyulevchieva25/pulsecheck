package com.pulsecheck.service;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentDetectionService {

    private static final int FAILURE_THRESHOLD = 3;
    private static final int RECOVERY_THRESHOLD = 2;

    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final MonitorRepository monitorRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IncidentDetectionService(CheckResultRepository checkResultRepository,
                                    IncidentRepository incidentRepository,
                                    MonitorRepository monitorRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
        this.monitorRepository = monitorRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Transactional
    public void onCheckCompleted(CheckCompletedEvent event) {
        Optional<Incident> activeIncidentOpt =
                incidentRepository.findFirstByMonitorIdAndStatusNot(event.monitorId(), IncidentStatus.RESOLVED);

        if (!event.success()) {
            handleFailure(event, activeIncidentOpt);
        } else {
            handleSuccess(event, activeIncidentOpt);
        }
    }

    private void handleFailure(CheckCompletedEvent event, Optional<Incident> activeIncidentOpt) {
        if (activeIncidentOpt.isPresent()) {
            return;
        }

        List<CheckResult> recentChecks =
                checkResultRepository.findTop3ByMonitorIdOrderByCheckedAtDesc(event.monitorId());

        if (recentChecks.size() < FAILURE_THRESHOLD) {
            return;
        }

        boolean allFailed = recentChecks.stream().noneMatch(CheckResult::isSuccess);
        if (!allFailed) {
            return;
        }

        Monitor monitor = monitorRepository.findById(event.monitorId())
                .orElseThrow(() -> new IllegalStateException("Monitor not found: " + event.monitorId()));

        String cause = resolveCause(recentChecks.get(0), event);

        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCause(cause);
        incident.setStartedAt(event.checkedAt());

        Incident saved = incidentRepository.save(incident);

        eventPublisher.publishEvent(new IncidentOpenedEvent(
                saved.getId(),
                event.monitorId(),
                saved.getCause(),
                saved.getStartedAt()
        ));
    }

    private void handleSuccess(CheckCompletedEvent event, Optional<Incident> activeIncidentOpt) {
        if (activeIncidentOpt.isEmpty()) {
            return;
        }

        List<CheckResult> recentChecks =
                checkResultRepository.findTop3ByMonitorIdOrderByCheckedAtDesc(event.monitorId());

        if (recentChecks.size() < RECOVERY_THRESHOLD) {
            return;
        }

        boolean lastTwoSucceeded = recentChecks.subList(0, RECOVERY_THRESHOLD).stream()
                .allMatch(CheckResult::isSuccess);

        if (!lastTwoSucceeded) {
            return;
        }

        Incident incident = activeIncidentOpt.get();
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(event.checkedAt());

        Incident saved = incidentRepository.save(incident);

        eventPublisher.publishEvent(new IncidentResolvedEvent(
                saved.getId(),
                event.monitorId(),
                saved.getResolvedAt()
        ));
    }

    private String resolveCause(CheckResult latestResult, CheckCompletedEvent event) {
        if (latestResult.getErrorMessage() != null && !latestResult.getErrorMessage().isBlank()) {
            return latestResult.getErrorMessage();
        }
        if (event.statusCode() != null) {
            return "Unexpected HTTP status " + event.statusCode();
        }
        return "Connection error or timeout";
    }
}