package com.pulsecheck.incident.repository;

import com.pulsecheck.incident.entity.CheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findByMonitorIdOrderByCheckedAtDesc(Long monitorId);
}