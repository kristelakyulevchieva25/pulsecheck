package com.pulsecheck.incident.repository;

import com.pulsecheck.incident.entity.Incident;
import com.pulsecheck.incident.entity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByMonitorId(Long monitorId);
    Optional<Incident> findByMonitorIdAndStatus(Long monitorId, IncidentStatus status);
}