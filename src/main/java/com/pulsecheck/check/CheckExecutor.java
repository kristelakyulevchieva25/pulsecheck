
package com.pulsecheck.check;

import com.pulsecheck.check.event.CheckCompletedEvent;
import com.pulsecheck.check.repository.CheckResultRepository;
import com.pulsecheck.repository.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

@Service
public class CheckExecutor {

    private static final Logger log =
            LoggerFactory.getLogger(CheckExecutor.class);

    private final RestClient restClient;
    private final CheckResultRepository checkResultRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CheckExecutor(
            RestClient checkRestClient,
            CheckResultRepository checkResultRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.restClient = checkRestClient;
        this.checkResultRepository = checkResultRepository;
        this.eventPublisher = eventPublisher;
    }

    public CheckResult execute(Monitor monitor) {

        long startTime = System.nanoTime();

        Integer statusCode = null;
        boolean success = false;

        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri(monitor.getUrl())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        // HTTP error statuses are valid check results.
                    })
                    .toBodilessEntity();

            statusCode = response.getStatusCode().value();

            success = statusCode.equals(monitor.getExpectedStatusCode());

        } catch (RestClientException ex) {
            log.warn(
                    "HTTP check failed for monitor {}",
                    monitor.getId(),
                    ex
            );
        }

        long responseTimeMs =
                (System.nanoTime() - startTime) / 1_000_000;

        Instant checkedAt = Instant.now();

        CheckResult result = new CheckResult(
                monitor,
                success,
                statusCode,
                responseTimeMs,
                checkedAt
        );

        CheckResult savedResult = checkResultRepository.save(result);

        eventPublisher.publishEvent(
                new CheckCompletedEvent(
                        monitor.getId(),
                        success,
                        statusCode,
                        responseTimeMs,
                        checkedAt
                )
        );

        return savedResult;
    }
}
