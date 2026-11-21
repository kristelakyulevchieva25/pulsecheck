
package com.pulsecheck.check;

import com.pulsecheck.repository.Monitor;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "check_results")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms", nullable = false)
    private long responseTimeMs;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    public CheckResult() {
    }

    public CheckResult(
            Monitor monitor,
            boolean success,
            Integer statusCode,
            long responseTimeMs,
            Instant checkedAt
    ) {
        this.monitor = monitor;
        this.success = success;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.checkedAt = checkedAt;
    }

    public Long getId() {
        return id;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }
}
