
package com.pulsecheck.controller;

import com.pulsecheck.check.dto.CheckHistoryResponse;
import com.pulsecheck.service.CheckHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/monitors")
@Tag(
        name = "Check History",
        description = "Browse historical monitor checks with pagination and date filtering"
)
public class CheckHistoryController {

    private final CheckHistoryService checkHistoryService;

    public CheckHistoryController(CheckHistoryService checkHistoryService) {
        this.checkHistoryService = checkHistoryService;
    }

    @Operation(
            summary = "Get monitor check history",
            description = """
                    Returns the check history for a specific monitor.
                    Supports pagination and optional start and end date filters.
                    Results are sorted from newest to oldest.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Check history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters or date range"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Monitor not found"
            )
    })
    @GetMapping("/{id}/checks")
    public ResponseEntity<CheckHistoryResponse> getCheckHistory(

            @PathVariable Long id,

            @Parameter(description = "Start of the date range (ISO-8601)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,

            @Parameter(description = "End of the date range (ISO-8601)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate,

            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Number of results per page (1-100)")
            @RequestParam(defaultValue = "20")
            int size

    ) {
        CheckHistoryResponse response =
                checkHistoryService.getCheckHistory(
                        id,
                        startDate,
                        endDate,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}
