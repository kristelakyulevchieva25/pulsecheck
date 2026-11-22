
package com.pulsecheck;

import com.pulsecheck.entity.CheckResult;
import com.pulsecheck.repository.CheckResultRepository;
import com.pulsecheck.entity.Monitor;
import com.pulsecheck.repository.MonitorRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@WithMockUser
class CheckHistoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private CheckResultRepository checkResultRepository;

    private Monitor createMonitor() {

        Monitor monitor = new Monitor();

        monitor.setName("History Test Monitor");
        monitor.setUrl("https://example.com");
        monitor.setIntervalSeconds(60);
        monitor.setExpectedStatusCode(200);
        monitor.setActive(true);

        return monitorRepository.saveAndFlush(monitor);
    }

    private CheckResult createCheckResult(
            Monitor monitor,
            Instant checkedAt
    ) {

        CheckResult result = new CheckResult(
                monitor,
                true,
                200,
                120,
                checkedAt
        );

        return checkResultRepository.saveAndFlush(result);
    }

    @Test
    void shouldReturnPaginatedCheckHistorySortedNewestFirst()
            throws Exception {

        Monitor monitor = createMonitor();

        Instant now = Instant.now();

        createCheckResult(monitor, now.minusSeconds(180));

        CheckResult secondResult =
                createCheckResult(monitor, now.minusSeconds(120));

        CheckResult newestResult =
                createCheckResult(monitor, now.minusSeconds(60));

        mockMvc.perform(get(
                        "/api/v1/monitors/{id}/checks",
                        monitor.getId()
                )
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id")
                        .value(newestResult.getId()))
                .andExpect(jsonPath("$.content[1].id")
                        .value(secondResult.getId()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldFilterCheckHistoryByDateRange()
            throws Exception {

        Monitor monitor = createMonitor();

        Instant startDate =
                Instant.parse("2026-01-10T10:00:00Z");

        Instant endDate =
                Instant.parse("2026-01-10T12:00:00Z");

        createCheckResult(
                monitor,
                Instant.parse("2026-01-10T09:00:00Z")
        );

        createCheckResult(monitor, startDate);

        createCheckResult(
                monitor,
                Instant.parse("2026-01-10T11:00:00Z")
        );

        createCheckResult(monitor, endDate);

        createCheckResult(
                monitor,
                Instant.parse("2026-01-10T13:00:00Z")
        );

        mockMvc.perform(get(
                        "/api/v1/monitors/{id}/checks",
                        monitor.getId()
                )
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldReturnNotFoundForMissingMonitor()
            throws Exception {

        mockMvc.perform(get(
                        "/api/v1/monitors/{id}/checks",
                        -1L
                ))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidDateRange()
            throws Exception {

        Instant startDate =
                Instant.parse("2026-01-10T12:00:00Z");

        Instant endDate =
                Instant.parse("2026-01-10T10:00:00Z");

        Monitor monitor = createMonitor();

        mockMvc.perform(get(
                        "/api/v1/monitors/{id}/checks",
                        monitor.getId()
                )
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNegativePage()
            throws Exception {

        Monitor monitor = createMonitor();

        mockMvc.perform(get(
                        "/api/v1/monitors/{id}/checks",
                        monitor.getId()
                )
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPageSizeAboveMaximum()
            throws Exception {

        Monitor monitor = createMonitor();

        mockMvc.perform(get(
                        "/api/v1/monitors/{id}/checks",
                        monitor.getId()
                )
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}
