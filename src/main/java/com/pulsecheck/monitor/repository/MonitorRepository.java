package com.pulsecheck.monitor.repository;

import com.pulsecheck.monitor.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
}