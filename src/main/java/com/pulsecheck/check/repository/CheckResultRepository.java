
package com.pulsecheck.check.repository;

import com.pulsecheck.check.CheckResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    Page<CheckResult> findByMonitor_Id(
            Long monitorId,
            Pageable pageable
    );

    Page<CheckResult> findByMonitor_IdAndCheckedAtGreaterThanEqual(
            Long monitorId,
            Instant startDate,
            Pageable pageable
    );

    Page<CheckResult> findByMonitor_IdAndCheckedAtLessThanEqual(
            Long monitorId,
            Instant endDate,
            Pageable pageable
    );

    Page<CheckResult> findByMonitor_IdAndCheckedAtGreaterThanEqualAndCheckedAtLessThanEqual(
            Long monitorId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );
}
