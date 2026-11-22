package com.pulsecheck.controller;

import com.pulsecheck.dto.CreateMonitorRequest;
import com.pulsecheck.dto.MonitorResponse;
import com.pulsecheck.dto.UpdateMonitorRequest;
import com.pulsecheck.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    public ResponseEntity<MonitorResponse> createMonitor(
            @Valid @RequestBody CreateMonitorRequest request
    ) {
        MonitorResponse response = monitorService.createMonitor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping
    public ResponseEntity<List<MonitorResponse>> getAllMonitors(
            @Parameter(description = "Optional tag name to filter monitors")
            @RequestParam(required = false) String tag
    ) {
        return ResponseEntity.ok(
                monitorService.getAllMonitors(tag)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<MonitorResponse> getMonitorById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(monitorService.getMonitorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonitorResponse> updateMonitor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMonitorRequest request
    ) {
        return ResponseEntity.ok(
                monitorService.updateMonitor(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonitor(
            @PathVariable Long id
    ) {
        monitorService.deleteMonitor(id);

        return ResponseEntity.noContent().build();
    }
}