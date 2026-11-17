package com.pulsecheck.monitor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public class UpdateMonitorRequest {

    @NotBlank(message = "Monitor name is required")
    private String name;

    @NotBlank(message = "Monitor URL is required")
    @URL(
            regexp = "(?i)^https?://.+$",
            message = "URL must be a valid HTTP or HTTPS address"
    )
    private String url;

    @NotNull(message = "Check interval is required")
    @Min(value = 60, message = "Check interval must be at least 60 seconds")
    @Max(value = 3600, message = "Check interval must not exceed 3600 seconds")
    private Integer intervalSeconds;

    @NotNull(message = "Expected status code is required")
    @Min(value = 100, message = "Status code must be at least 100")
    @Max(value = 599, message = "Status code must not exceed 599")
    private Integer expectedStatusCode;

    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Integer getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(Integer expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}