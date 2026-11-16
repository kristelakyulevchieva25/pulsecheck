package com.pulsecheck.monitor;

import com.pulsecheck.common.exception.ResourceNotFoundException;

public class MonitorNotFoundException extends ResourceNotFoundException {

    public MonitorNotFoundException(Long id) {
        super("Monitor", id);
    }
}