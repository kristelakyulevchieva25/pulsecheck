package com.pulsecheck.monitor.dto;

import java.time.Instant;

public class MonitorResponse {

    private Long id;
    private String name;
    private String url;
    private Integer intervalSeconds;
    private Integer expectedStatusCode;
    private boolean active;
    private Instant nextCheckAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Instant getNextCheckAt() {
        return nextCheckAt;
    }

    public void setNextCheckAt(Instant nextCheckAt) {
        this.nextCheckAt = nextCheckAt;
    }
}