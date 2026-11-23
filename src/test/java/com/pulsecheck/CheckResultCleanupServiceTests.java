
package com.pulsecheck;

import com.pulsecheck.entity.CheckResult;
import com.pulsecheck.monitor.Monitor;
import com.pulsecheck.monitor.repository.MonitorRepository;
import com.pulsecheck.repository.CheckResultRepository;
import com.pulsecheck.service.CheckResultCleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CheckResultCleanupServiceTests {

    @Autowired
    private CheckResultCleanupService cleanupService;

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private MonitorRepository monitorRepository;

    @Test
    void shouldDeleteCheckResultsOlderThan30Days() {

        Monitor monitor = createMonitor();

        CheckResult oldResult = createCheckResult(
                monitor,
                Instant.now().minus(31, ChronoUnit.DAYS)
        );

        CheckResult recentResult = createCheckResult(
                monitor,
                Instant.now().minus(29, ChronoUnit.DAYS)
        );

        oldResult = checkResultRepository.saveAndFlush(oldResult);
        recentResult = checkResultRepository.saveAndFlush(recentResult);

        int deletedCount = cleanupService.deleteOldCheckResults();

        assertEquals(1, deletedCount);

        assertFalse(
                checkResultRepository.findById(oldResult.getId()).isPresent()
        );

        assertTrue(
                checkResultRepository.findById(recentResult.getId()).isPresent()
        );
    }

    @Test
    void shouldNotDeleteCheckResultExactlyAtCutoff() {

        Monitor monitor = createMonitor();

        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);

        CheckResult olderResult = createCheckResult(
                monitor,
                cutoff.minus(1, ChronoUnit.MILLIS)
        );

        CheckResult boundaryResult = createCheckResult(
                monitor,
                cutoff
        );

        olderResult = checkResultRepository.saveAndFlush(olderResult);
        boundaryResult = checkResultRepository.saveAndFlush(boundaryResult);

        int deletedCount = checkResultRepository.deleteOlderThan(cutoff);

        assertEquals(1, deletedCount);

        assertFalse(
                checkResultRepository.findById(olderResult.getId()).isPresent()
        );

        assertTrue(
                checkResultRepository.findById(boundaryResult.getId()).isPresent()
        );
    }

    @Test
    void shouldReturnZeroWhenNoOldCheckResultsExist() {

        Monitor monitor = createMonitor();

        CheckResult recentResult = createCheckResult(
                monitor,
                Instant.now().minus(10, ChronoUnit.DAYS)
        );

        checkResultRepository.saveAndFlush(recentResult);

        int deletedCount = cleanupService.deleteOldCheckResults();

        assertEquals(0, deletedCount);

        assertTrue(
                checkResultRepository.findById(recentResult.getId()).isPresent()
        );
    }

    private Monitor createMonitor() {

        Monitor monitor = new Monitor();

        monitor.setName("Cleanup Test Monitor");
        monitor.setUrl("https://example.com");
        monitor.setIntervalSeconds(60);
        monitor.setExpectedStatusCode(200);
        monitor.setActive(true);

        return monitorRepository.saveAndFlush(monitor);
    }

    private CheckResult createCheckResult(
            Monitor monitor,
            Instant checkedAt
    ) {

        return new CheckResult(
                monitor,
                true,
                200,
                150L,
                checkedAt
        );
    }
}
