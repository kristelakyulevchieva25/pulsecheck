
package com.pulsecheck.service;

import com.pulsecheck.check.dto.CheckHistoryResponse;
import com.pulsecheck.check.dto.CheckResultResponse;
import com.pulsecheck.entity.CheckResult;
import com.pulsecheck.entity.Monitor;
import com.pulsecheck.exception.MonitorNotFoundException;
import com.pulsecheck.repository.CheckResultRepository;
import com.pulsecheck.repository.MonitorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class CheckHistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CheckResultRepository checkResultRepository;
    private final MonitorRepository monitorRepository;

    public CheckHistoryService(
            CheckResultRepository checkResultRepository,
            MonitorRepository monitorRepository
    ) {
        this.checkResultRepository = checkResultRepository;
        this.monitorRepository = monitorRepository;
    }

    @Transactional(readOnly = true)
    public CheckHistoryResponse getCheckHistory(
            Long monitorId,
            Instant startDate,
            Instant endDate,
            int page,
            int size
    ) {

        if (!monitorRepository.existsById(monitorId)) {
            throw new MonitorNotFoundException(monitorId);
        }

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page index must not be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date must not be after end date"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "checkedAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<CheckResult> results;

        if (startDate != null && endDate != null) {
            results = checkResultRepository
                    .findByMonitor_IdAndCheckedAtGreaterThanEqualAndCheckedAtLessThanEqual(
                            monitorId,
                            startDate,
                            endDate,
                            pageable
                    );
        } else if (startDate != null) {
            results = checkResultRepository
                    .findByMonitor_IdAndCheckedAtGreaterThanEqual(
                            monitorId,
                            startDate,
                            pageable
                    );
        } else if (endDate != null) {
            results = checkResultRepository
                    .findByMonitor_IdAndCheckedAtLessThanEqual(
                            monitorId,
                            endDate,
                            pageable
                    );
        } else {
            results = checkResultRepository.findByMonitor_Id(
                    monitorId,
                    pageable
            );
        }

        return new CheckHistoryResponse(
                results.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages(),
                results.isFirst(),
                results.isLast()
        );
    }

    private CheckResultResponse toResponse(CheckResult result) {

        return new CheckResultResponse(
                result.getId(),
                result.getMonitor().getId(),
                result.isSuccess(),
                result.getStatusCode(),
                result.getResponseTimeMs(),
                result.getCheckedAt()
        );
    }
}
