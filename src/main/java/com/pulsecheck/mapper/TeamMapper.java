package com.pulsecheck.mapper;

import com.pulsecheck.dto.MemberResponse;
import com.pulsecheck.dto.TeamResponse;
import com.pulsecheck.entity.Team;
import com.pulsecheck.entity.TeamMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    TeamResponse toResponse(Team team);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    MemberResponse toMemberResponse(TeamMembership membership);
}