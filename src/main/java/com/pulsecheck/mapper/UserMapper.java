package com.pulsecheck.mapper;

import com.pulsecheck.dto.UserResponse;
import com.pulsecheck.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
