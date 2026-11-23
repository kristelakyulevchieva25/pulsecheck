package com.pulsecheck.service;

import com.pulsecheck.repository.CheckResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CheckResultCleanupService {

    private static final Logger log =
            LoggerFactory.getLogger(CheckResultCleanupService.class);

    private static final int RETENTION_DAYS = 30;

    private final CheckResultRepository checkResultRepository;

    public CheckResultCleanupService(
            CheckResultRepository checkResultRepository
    ) {
        this.checkResultRepository = checkResultRepository;
    }

    @Transactional
    public int deleteOldCheckResults() {

        Instant cutoff = Instant.now().minus(
                RETENTION_DAYS,
                ChronoUnit.DAYS
        );

        int deletedCount =
                checkResultRepository.deleteOlderThan(cutoff);

        log.info(
                "Deleted {} check results older than {} days",
                deletedCount,
                RETENTION_DAYS
        );

        return deletedCount;
    }
}