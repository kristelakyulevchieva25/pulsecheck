package com.pulsecheck.repository;

import com.pulsecheck.entity.Incident;
import com.pulsecheck.entity.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByMonitorId(Long monitorId);
    Optional<Incident> findFirstByMonitorIdAndStatusNot(Long monitorId, IncidentStatus status);
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
}