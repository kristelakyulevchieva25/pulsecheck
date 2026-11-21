package com.pulsecheck.repository;

import com.pulsecheck.dto.CreateMonitorRequest;
import com.pulsecheck.dto.MonitorResponse;
import com.pulsecheck.dto.UpdateMonitorRequest;
import com.pulsecheck.mapper.MonitorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final MonitorMapper monitorMapper;

    public MonitorService(
            MonitorRepository monitorRepository,
            MonitorMapper monitorMapper
    ) {
        this.monitorRepository = monitorRepository;
        this.monitorMapper = monitorMapper;
    }

    public MonitorResponse createMonitor(CreateMonitorRequest request) {
        Monitor monitor = monitorMapper.toEntity(request);

        Monitor savedMonitor = monitorRepository.save(monitor);

        return monitorMapper.toResponse(savedMonitor);
    }


    @Transactional(readOnly = true)
    public List<MonitorResponse> getAllMonitors() {
        return getAllMonitors(null);
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> getAllMonitors(String tagName) {

        List<Monitor> monitors;

        if (tagName == null || tagName.isBlank()) {
            monitors = monitorRepository.findAll();
        } else {
            monitors = monitorRepository
                    .findDistinctByTags_NameIgnoreCase(tagName.trim());
        }

        return monitors.stream()
                .map(monitorMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public MonitorResponse getMonitorById(Long id) {
        Monitor monitor = getMonitorEntity(id);

        return monitorMapper.toResponse(monitor);
    }

    public MonitorResponse updateMonitor(
            Long id,
            UpdateMonitorRequest request
    ) {
        Monitor monitor = getMonitorEntity(id);

        monitorMapper.updateEntity(request, monitor);

        Monitor updatedMonitor = monitorRepository.save(monitor);

        return monitorMapper.toResponse(updatedMonitor);
    }

    public void deleteMonitor(Long id) {
        Monitor monitor = getMonitorEntity(id);

        monitorRepository.delete(monitor);
    }

    @Transactional(readOnly = true)
    public Monitor getMonitorEntity(Long id) {
        return monitorRepository.findById(id)
                .orElseThrow(() ->
                        new MonitorNotFoundException(id)
                );
    }
}