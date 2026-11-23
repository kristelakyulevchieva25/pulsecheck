package com.pulsecheck.scheduler;

import com.pulsecheck.service.CheckResultCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckResultCleanupScheduler {

    private final CheckResultCleanupService cleanupService;

    public CheckResultCleanupScheduler(
            CheckResultCleanupService cleanupService
    ) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(
            cron = "0 0 2 * * *",
            zone = "Europe/Sofia"
    )
    public void cleanupOldCheckResults() {
        cleanupService.deleteOldCheckResults();
    }
}