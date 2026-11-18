
package com.pulsecheck.check.repository;

import com.pulsecheck.check.CheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
}
