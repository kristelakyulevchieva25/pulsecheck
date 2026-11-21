package com.pulsecheck;

import com.pulsecheck.monitor.MonitorNotFoundException;
import com.pulsecheck.monitor.MonitorService;
import com.pulsecheck.monitor.dto.CreateMonitorRequest;
import com.pulsecheck.monitor.dto.MonitorResponse;
import com.pulsecheck.monitor.dto.UpdateMonitorRequest;
import com.pulsecheck.monitor.repository.MonitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class MonitorServiceTests {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private MonitorRepository monitorRepository;

    private CreateMonitorRequest createRequest() {
        CreateMonitorRequest request = new CreateMonitorRequest();
        request.setName("Test Monitor");
        request.setUrl("https://example.com");
        request.setIntervalSeconds(60);
        request.setExpectedStatusCode(200);
        request.setActive(true);

        return request;
    }

    private UpdateMonitorRequest updateRequest() {
        UpdateMonitorRequest request = new UpdateMonitorRequest();
        request.setName("Updated Monitor");
        request.setUrl("https://updated-example.com");
        request.setIntervalSeconds(300);
        request.setExpectedStatusCode(200);
        request.setActive(false);

        return request;
    }

    @Test
    void shouldCreateMonitor() {
        MonitorResponse response =
                monitorService.createMonitor(createRequest());

        assertNotNull(response.getId());
        assertEquals("Test Monitor", response.getName());
        assertEquals("https://example.com", response.getUrl());
        assertEquals(60, response.getIntervalSeconds());
        assertTrue(response.isActive());
    }

    @Test
    void shouldGetAllMonitors() {
        monitorService.createMonitor(createRequest());

        List<MonitorResponse> monitors =
                monitorService.getAllMonitors();

        assertTrue(
                monitors.stream()
                        .anyMatch(m -> "Test Monitor".equals(m.getName()))
        );
    }

    @Test
    void shouldGetMonitorById() {
        MonitorResponse created =
                monitorService.createMonitor(createRequest());

        MonitorResponse found =
                monitorService.getMonitorById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Test Monitor", found.getName());
    }

    @Test
    void shouldUpdateMonitor() {
        MonitorResponse created =
                monitorService.createMonitor(createRequest());

        MonitorResponse updated =
                monitorService.updateMonitor(
                        created.getId(),
                        updateRequest()
                );

        assertEquals(created.getId(), updated.getId());
        assertEquals("Updated Monitor", updated.getName());
        assertEquals("https://updated-example.com", updated.getUrl());
        assertEquals(300, updated.getIntervalSeconds());
        assertFalse(updated.isActive());
    }

    @Test
    void shouldDeleteMonitor() {
        MonitorResponse created =
                monitorService.createMonitor(createRequest());

        monitorService.deleteMonitor(created.getId());

        assertFalse(
                monitorRepository.existsById(created.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenMonitorDoesNotExist() {
        assertThrows(
                MonitorNotFoundException.class,
                () -> monitorService.getMonitorEntity(-1L)
        );
    }
}