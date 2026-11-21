package com.pulsecheck.monitor;

import com.pulsecheck.monitor.dto.CreateMonitorRequest;
import com.pulsecheck.monitor.dto.MonitorResponse;
import com.pulsecheck.monitor.dto.UpdateMonitorRequest;
import com.pulsecheck.monitor.mapper.MonitorMapper;
import com.pulsecheck.monitor.repository.MonitorRepository;
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
        return monitorRepository.findAll()
                .stream()
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