package com.pulsecheck.mapper;

import com.pulsecheck.entity.Monitor;
import com.pulsecheck.dto.CreateMonitorRequest;
import com.pulsecheck.dto.MonitorResponse;
import com.pulsecheck.dto.UpdateMonitorRequest;
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