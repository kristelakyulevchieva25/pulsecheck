package com.pulsecheck.repository;

import com.pulsecheck.entity.CheckResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    // Used by Incident functionality
    List<CheckResult> findByMonitorIdOrderByCheckedAtDesc(Long monitorId);

    // Paginated check history
    Page<CheckResult> findByMonitor_Id(
            Long monitorId,
            Pageable pageable
    );

    // Filter by start date
    Page<CheckResult> findByMonitor_IdAndCheckedAtGreaterThanEqual(
            Long monitorId,
            Instant startDate,
            Pageable pageable
    );

    // Filter by end date
    Page<CheckResult> findByMonitor_IdAndCheckedAtLessThanEqual(
            Long monitorId,
            Instant endDate,
            Pageable pageable
    );

    // Filter by date range
    Page<CheckResult> findByMonitor_IdAndCheckedAtGreaterThanEqualAndCheckedAtLessThanEqual(
            Long monitorId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );
}