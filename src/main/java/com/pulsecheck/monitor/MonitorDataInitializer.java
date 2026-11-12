package com.pulsecheck.monitor;

import com.pulsecheck.monitor.repository.MonitorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorDataInitializer {

    @Bean
    public CommandLineRunner initializeMonitors(
            MonitorRepository monitorRepository
    ) {
        return args -> {

            if (monitorRepository.count() == 0) {

                Monitor google = new Monitor();
                google.setName("Google");
                google.setUrl("https://www.google.com");
                google.setIntervalSeconds(60);
                google.setExpectedStatusCode(200);
                google.setActive(true);

                Monitor github = new Monitor();
                github.setName("GitHub");
                github.setUrl("https://github.com");
                github.setIntervalSeconds(300);
                github.setExpectedStatusCode(200);
                github.setActive(true);

                monitorRepository.save(google);
                monitorRepository.save(github);
            }
        };
    }
}