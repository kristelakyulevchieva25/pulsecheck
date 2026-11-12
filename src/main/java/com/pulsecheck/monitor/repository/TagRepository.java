package com.pulsecheck.monitor.repository;

import com.pulsecheck.monitor.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}