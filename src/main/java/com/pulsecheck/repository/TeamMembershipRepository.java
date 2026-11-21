package com.pulsecheck.repository;

import com.pulsecheck.entity.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {

    @Query("""
            SELECT m FROM TeamMembership m
            JOIN FETCH m.user
            WHERE m.team.id = :teamId
            ORDER BY m.joinedAt
            """)
    List<TeamMembership> findAllByTeamId(@Param("teamId") Long teamId);
}