
package com.pulsecheck;

import com.pulsecheck.check.CheckExecutor;
import com.pulsecheck.check.event.CheckCompletedEvent;
import com.pulsecheck.entity.CheckResult;
import com.pulsecheck.entity.Monitor;
import com.pulsecheck.repository.CheckResultRepository;
import com.pulsecheck.repository.MonitorRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({
        TestcontainersConfiguration.class,
        CheckExecutorTests.ShortTimeoutConfiguration.class
})
@RecordApplicationEvents
@Transactional
class CheckExecutorTests {

    @Autowired
    private CheckExecutor checkExecutor;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private HttpServer server;

    @BeforeEach
    void setUpServer() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );

        server.createContext("/success", exchange ->
                respond(exchange, 200)
        );

        server.createContext("/server-error", exchange ->
                respond(exchange, 500)
        );

        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(1000);
                respond(exchange, 200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                exchange.close();
            } catch (IOException ex) {
                exchange.close();
            }
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void respond(HttpExchange exchange, int status)
            throws IOException {

        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private Monitor createMonitor(String path, int expectedStatus) {

        Monitor monitor = new Monitor();

        monitor.setName("Check Executor Test");
        monitor.setUrl(
                "http://127.0.0.1:"
                        + server.getAddress().getPort()
                        + path
        );
        monitor.setIntervalSeconds(60);
        monitor.setExpectedStatusCode(expectedStatus);
        monitor.setActive(true);

        return monitorRepository.saveAndFlush(monitor);
    }

    private CheckCompletedEvent getPublishedEvent() {

        List<CheckCompletedEvent> events =
                applicationEvents.stream(CheckCompletedEvent.class)
                        .toList();

        assertEquals(1, events.size());

        return events.get(0);
    }

    @Test
    void shouldExecuteSuccessfulCheckAndPublishEvent() {

        Monitor monitor = createMonitor("/success", 200);

        CheckResult result = checkExecutor.execute(monitor);

        assertNotNull(result.getId());
        assertTrue(result.isSuccess());
        assertEquals(Integer.valueOf(200), result.getStatusCode());
        assertTrue(result.getResponseTimeMs() >= 0);
        assertNotNull(result.getCheckedAt());

        CheckResult savedResult =
                checkResultRepository.findById(result.getId())
                        .orElseThrow();

        assertEquals(
                monitor.getId(),
                savedResult.getMonitor().getId()
        );

        CheckCompletedEvent event = getPublishedEvent();

        assertEquals(monitor.getId(), event.monitorId());
        assertTrue(event.success());
        assertEquals(Integer.valueOf(200), event.statusCode());
        assertEquals(result.getResponseTimeMs(), event.responseTimeMs());
        assertEquals(result.getCheckedAt(), event.checkedAt());
    }

    @Test
    void shouldSaveFailedCheckWhenStatusDoesNotMatch() {

        Monitor monitor = createMonitor("/server-error", 200);

        CheckResult result = checkExecutor.execute(monitor);

        assertFalse(result.isSuccess());
        assertEquals(Integer.valueOf(500), result.getStatusCode());

        CheckCompletedEvent event = getPublishedEvent();

        assertEquals(monitor.getId(), event.monitorId());
        assertFalse(event.success());
        assertEquals(Integer.valueOf(500), event.statusCode());
    }

    @Test
    void shouldTreatExpectedErrorStatusAsSuccessful() {

        Monitor monitor = createMonitor("/server-error", 500);

        CheckResult result = checkExecutor.execute(monitor);

        assertTrue(result.isSuccess());
        assertEquals(Integer.valueOf(500), result.getStatusCode());

        CheckCompletedEvent event = getPublishedEvent();

        assertTrue(event.success());
        assertEquals(Integer.valueOf(500), event.statusCode());
    }

    @Test
    void shouldSaveFailedCheckWhenRequestTimesOut() {

        Monitor monitor = createMonitor("/slow", 200);

        CheckResult result = checkExecutor.execute(monitor);

        assertFalse(result.isSuccess());
        assertNull(result.getStatusCode());
        assertTrue(result.getResponseTimeMs() >= 0);
        assertNotNull(result.getCheckedAt());

        CheckCompletedEvent event = getPublishedEvent();

        assertEquals(monitor.getId(), event.monitorId());
        assertFalse(event.success());
        assertNull(event.statusCode());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ShortTimeoutConfiguration {

        @Bean
        @Primary
        RestClient testCheckRestClient() {

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(250))
                    .build();

            JdkClientHttpRequestFactory requestFactory =
                    new JdkClientHttpRequestFactory(httpClient);

            requestFactory.setReadTimeout(Duration.ofMillis(250));

            return RestClient.builder()
                    .requestFactory(requestFactory)
                    .build();
        }
    }
}
