package com.pulsecheck;

import com.pulsecheck.monitor.Monitor;
import com.pulsecheck.monitor.Tag;
import com.pulsecheck.monitor.repository.MonitorRepository;
import com.pulsecheck.monitor.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class MonitorRepositoryTests {

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveMonitorWithTags() {
        // Create and save a tag
        Tag tag = new Tag();
        tag.setName("Production");

        tagRepository.saveAndFlush(tag);

        // Create a monitor
        Monitor monitor = new Monitor();
        monitor.setName("Google");
        monitor.setUrl("https://www.google.com");
        monitor.setIntervalSeconds(60);
        monitor.setExpectedStatusCode(200);

        // Connect monitor with tag
        monitor.addTag(tag);

        // Save monitor
        monitorRepository.saveAndFlush(monitor);

        // Clear persistence context to force database reload
        entityManager.clear();

        // Load monitor again
        Monitor savedMonitor = monitorRepository.findById(monitor.getId())
                .orElseThrow();

        // Verify monitor and its tag
        assertEquals("Google", savedMonitor.getName());
        assertEquals(1, savedMonitor.getTags().size());
        assertEquals("Production",
                savedMonitor.getTags().iterator().next().getName());
    }
}