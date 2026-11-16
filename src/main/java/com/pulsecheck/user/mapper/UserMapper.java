package com.pulsecheck.user.mapper;

import com.pulsecheck.user.dto.UserResponse;
import com.pulsecheck.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
