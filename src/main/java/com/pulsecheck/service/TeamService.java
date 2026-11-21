package com.pulsecheck.service;

import com.pulsecheck.dto.AddMemberRequest;
import com.pulsecheck.dto.ChangeRoleRequest;
import com.pulsecheck.dto.CreateTeamRequest;
import com.pulsecheck.dto.MemberResponse;
import com.pulsecheck.dto.TeamResponse;
import com.pulsecheck.entity.Team;
import com.pulsecheck.entity.TeamMembership;
import com.pulsecheck.entity.TeamPlan;
import com.pulsecheck.entity.TeamRole;
import com.pulsecheck.entity.User;
import com.pulsecheck.exception.BusinessRuleException;
import com.pulsecheck.exception.ConflictException;
import com.pulsecheck.exception.ForbiddenException;
import com.pulsecheck.exception.ResourceNotFoundException;
import com.pulsecheck.mapper.TeamMapper;
import com.pulsecheck.repository.TeamMembershipRepository;
import com.pulsecheck.repository.TeamRepository;
import com.pulsecheck.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private static final String LAST_OWNER_MESSAGE = "A team must keep at least one OWNER";

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository,
                       TeamMembershipRepository membershipRepository,
                       UserRepository userRepository,
                       TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.teamMapper = teamMapper;
    }

    public TeamResponse createTeam(String creatorEmail, CreateTeamRequest request) {
        User creator = findUserByEmail(creatorEmail);

        Team team = new Team();
        team.setName(request.name().trim());
        team.setPlan(request.plan() != null ? request.plan() : TeamPlan.FREE);
        team.addMember(creator, TeamRole.OWNER);

        return teamMapper.toResponse(teamRepository.saveAndFlush(team));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(String email) {
        User user = findUserByEmail(email);
        return teamRepository.findAllByMemberUserId(user.getId()).stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long teamId, String actorEmail) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }

        List<TeamMembership> members = membershipRepository.findAllByTeamId(teamId);
        boolean isMember = members.stream()
                .anyMatch(m -> m.getUser().getEmail().equals(actorEmail));
        if (!isMember) {
            throw new ForbiddenException("You are not a member of this team");
        }

        return members.stream().map(teamMapper::toMemberResponse).toList();
    }

    public MemberResponse addMember(Long teamId, String actorEmail, AddMemberRequest request) {
        Team team = findTeamForUpdate(teamId);
        requireOwner(team, actorEmail);

        User user = findUserByEmail(request.email().trim().toLowerCase());

        boolean alreadyMember = team.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (alreadyMember) {
            throw new ConflictException("User is already a member of this team");
        }

        TeamMembership membership = team.addMember(user, request.role());
        teamRepository.flush();
        return teamMapper.toMemberResponse(membership);
    }

    public MemberResponse changeRole(Long teamId, Long userId, String actorEmail, ChangeRoleRequest request) {
        Team team = findTeamForUpdate(teamId);
        requireOwner(team, actorEmail);

        TeamMembership membership = findMembership(team, userId);

        boolean demotingOwner = membership.getTeamRole() == TeamRole.OWNER
                && request.role() != TeamRole.OWNER;
        if (demotingOwner && countOwners(team) == 1) {
            throw new BusinessRuleException(LAST_OWNER_MESSAGE);
        }

        membership.setTeamRole(request.role());
        return teamMapper.toMemberResponse(membership);
    }

    public void removeMember(Long teamId, Long userId, String actorEmail) {
        Team team = findTeamForUpdate(teamId);
        TeamMembership target = findMembership(team, userId);

        boolean leavingSelf = target.getUser().getEmail().equals(actorEmail);
        if (!leavingSelf) {
            requireOwner(team, actorEmail);
        }

        if (target.getTeamRole() == TeamRole.OWNER && countOwners(team) == 1) {
            throw new BusinessRuleException(LAST_OWNER_MESSAGE);
        }

        team.removeMember(target);
    }

    private Team findTeamForUpdate(Long teamId) {
        return teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private TeamMembership findMembership(Team team, Long userId) {
        return team.getMemberships().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Team member", userId));
    }

    private void requireOwner(Team team, String actorEmail) {
        boolean isOwner = team.getMemberships().stream()
                .anyMatch(m -> m.getTeamRole() == TeamRole.OWNER
                        && m.getUser().getEmail().equals(actorEmail));
        if (!isOwner) {
            throw new ForbiddenException("Only a team OWNER can do this");
        }
    }

    private long countOwners(Team team) {
        return team.getMemberships().stream()
                .filter(m -> m.getTeamRole() == TeamRole.OWNER)
                .count();
    }
}