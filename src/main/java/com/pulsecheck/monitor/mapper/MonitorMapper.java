package com.pulsecheck.monitor.mapper;

import com.pulsecheck.monitor.Monitor;
import com.pulsecheck.monitor.dto.CreateMonitorRequest;
import com.pulsecheck.monitor.dto.MonitorResponse;
import com.pulsecheck.monitor.dto.UpdateMonitorRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MonitorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nextCheckAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Monitor toEntity(CreateMonitorRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nextCheckAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntity(UpdateMonitorRequest request, @MappingTarget Monitor monitor);

    MonitorResponse toResponse(Monitor monitor);
}