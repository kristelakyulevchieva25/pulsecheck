package com.pulsecheck.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "team_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_team_memberships_team_user",
                columnNames = {"team_id", "user_id"}))
public class TeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_role", nullable = false, length = 50)
    private TeamRole teamRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    protected TeamMembership() {
    }

    TeamMembership(Team team, User user, TeamRole teamRole) {
        this.team = team;
        this.user = user;
        this.teamRole = teamRole;
    }

    public Long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }

    public TeamRole getTeamRole() {
        return teamRole;
    }

    public void setTeamRole(TeamRole teamRole) {
        this.teamRole = teamRole;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}