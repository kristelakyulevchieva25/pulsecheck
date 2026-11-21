package com.pulsecheck.repository;

import com.pulsecheck.entity.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // Locks the team row so two concurrent role changes cannot both remove the last OWNER
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Team t WHERE t.id = :id")
    Optional<Team> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT t FROM Team t
            JOIN t.memberships m
            WHERE m.user.id = :userId
            ORDER BY t.name
            """)
    List<Team> findAllByMemberUserId(@Param("userId") Long userId);
}