package com.pulsecheck.exception;

public class MonitorNotFoundException extends ResourceNotFoundException {

    public MonitorNotFoundException(Long id) {
        super("Monitor", id);
    }
}