package com.pulsecheck.repository;

import com.pulsecheck.entity.CheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    List<CheckResult> findByMonitorIdOrderByCheckedAtDesc(Long monitorId);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            DELETE FROM CheckResult cr
            WHERE cr.checkedAt < :cutoff
            """)
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}