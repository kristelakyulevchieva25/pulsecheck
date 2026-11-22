package com.pulsecheck.entity;

import com.pulsecheck.monitor.Monitor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "check_results")
@Getter
@Setter
@NoArgsConstructor
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    private Boolean success;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    public CheckResult(Monitor monitor, boolean success, Integer statusCode, long responseTimeMs, Instant checkedAt) {
        this.monitor = monitor;
        this.success = success;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.checkedAt = checkedAt;
    }

    public CheckResult(Monitor monitor, boolean success, Integer statusCode, long responseTimeMs, Instant checkedAt, String errorMessage) {
        this.monitor = monitor;
        this.success = success;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.checkedAt = checkedAt;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }
}