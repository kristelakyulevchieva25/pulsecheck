package com.pulsecheck.monitor;

public class MonitorNotFoundException extends RuntimeException {

    public MonitorNotFoundException(Long id) {
        super("Monitor not found with id: " + id);
    }
}