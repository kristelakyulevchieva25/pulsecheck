package com.pulsecheck;

import com.pulsecheck.monitor.MonitorService;
import com.pulsecheck.monitor.dto.CreateMonitorRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@WithMockUser
class MonitorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorService monitorService;

    private Long createTestMonitor() {
        CreateMonitorRequest request = new CreateMonitorRequest();
        request.setName("Controller Test Monitor");
        request.setUrl("https://example.com");
        request.setIntervalSeconds(60);
        request.setExpectedStatusCode(200);
        request.setActive(true);

        return monitorService.createMonitor(request).getId();
    }

    @Test
    void shouldCreateMonitor() throws Exception {
        mockMvc.perform(post("/api/monitors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "API Test Monitor",
                                    "url": "https://example.com",
                                    "intervalSeconds": 60,
                                    "expectedStatusCode": 200,
                                    "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("API Test Monitor"));
    }

    @Test
    void shouldGetAllMonitors() throws Exception {
        mockMvc.perform(get("/api/monitors"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMonitorById() throws Exception {
        Long id = createTestMonitor();

        mockMvc.perform(get("/api/monitors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Controller Test Monitor"));
    }

    @Test
    void shouldUpdateMonitor() throws Exception {
        Long id = createTestMonitor();

        mockMvc.perform(put("/api/monitors/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated API Monitor",
                                    "url": "https://updated-example.com",
                                    "intervalSeconds": 300,
                                    "expectedStatusCode": 200,
                                    "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated API Monitor"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldDeleteMonitor() throws Exception {
        Long id = createTestMonitor();

        mockMvc.perform(delete("/api/monitors/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundForMissingMonitor() throws Exception {
        mockMvc.perform(get("/api/monitors/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidUrl() throws Exception {
        mockMvc.perform(post("/api/monitors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Invalid Monitor",
                                    "url": "not-a-valid-url",
                                    "intervalSeconds": 60,
                                    "expectedStatusCode": 200,
                                    "active": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidInterval() throws Exception {
        mockMvc.perform(post("/api/monitors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Invalid Monitor",
                                    "url": "https://example.com",
                                    "intervalSeconds": 30,
                                    "expectedStatusCode": 200,
                                    "active": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}