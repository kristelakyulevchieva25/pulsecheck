package com.pulsecheck.controller;

import com.pulsecheck.common.dto.PageResponse;
import com.pulsecheck.dto.IncidentResponse;
import com.pulsecheck.entity.IncidentStatus;
import com.pulsecheck.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Incident lifecycle management")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @Operation(summary = "List incidents with optional status filter and pagination")
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    public ResponseEntity<PageResponse<IncidentResponse>> listIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<IncidentResponse> page = incidentService.getIncidents(status, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge an open incident")
    @ApiResponse(responseCode = "200", description = "Incident acknowledged")
    @ApiResponse(responseCode = "404", description = "Incident not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<IncidentResponse> acknowledge(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.acknowledge(id));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve an open or acknowledged incident")
    @ApiResponse(responseCode = "200", description = "Incident resolved")
    @ApiResponse(responseCode = "404", description = "Incident not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<IncidentResponse> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.resolve(id));
    }
}