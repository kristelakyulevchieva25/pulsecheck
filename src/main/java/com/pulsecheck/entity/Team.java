package com.pulsecheck.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TeamPlan plan = TeamPlan.FREE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMembership> memberships = new ArrayList<>();

    public Team() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TeamPlan getPlan() {
        return plan;
    }

    public void setPlan(TeamPlan plan) {
        this.plan = plan;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<TeamMembership> getMemberships() {
        return memberships;
    }

    public TeamMembership addMember(User user, TeamRole role) {
        TeamMembership membership = new TeamMembership(this, user, role);
        memberships.add(membership);
        return membership;
    }

    public void removeMember(TeamMembership membership) {
        memberships.remove(membership);
    }
}