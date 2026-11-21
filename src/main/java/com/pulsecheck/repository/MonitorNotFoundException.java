package com.pulsecheck.repository;

import com.pulsecheck.exception.ResourceNotFoundException;

public class MonitorNotFoundException extends ResourceNotFoundException {

    public MonitorNotFoundException(Long id) {
        super("Monitor", id);
    }
}