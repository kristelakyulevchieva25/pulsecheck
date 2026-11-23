package com.pulsecheck.controller;

import com.pulsecheck.dto.AddMemberRequest;
import com.pulsecheck.dto.ChangeRoleRequest;
import com.pulsecheck.dto.CreateTeamRequest;
import com.pulsecheck.dto.MemberResponse;
import com.pulsecheck.dto.TeamResponse;
import com.pulsecheck.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Teams", description = "Teams and role-based memberships")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @Operation(summary = "Create a team; the creator becomes its OWNER")
    @ApiResponse(responseCode = "201", description = "Team created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.createTeam(authentication.getName(), request));
    }

    @GetMapping
    @Operation(summary = "List the teams the current user belongs to")
    @ApiResponse(responseCode = "200", description = "Teams returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    public ResponseEntity<List<TeamResponse>> getMyTeams(Authentication authentication) {
        return ResponseEntity.ok(teamService.getMyTeams(authentication.getName()));
    }

    @GetMapping("/{teamId}/members")
    @Operation(summary = "List the members of a team")
    @ApiResponse(responseCode = "200", description = "Members returned")
    @ApiResponse(responseCode = "403", description = "Not a member of this team")
    @ApiResponse(responseCode = "404", description = "Team not found")
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable Long teamId,
                                                           Authentication authentication) {
        return ResponseEntity.ok(teamService.getMembers(teamId, authentication.getName()));
    }

    @PostMapping("/{teamId}/members")
    @Operation(summary = "Add a member by email (OWNER only)")
    @ApiResponse(responseCode = "201", description = "Member added")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "403", description = "Only a team OWNER can add members")
    @ApiResponse(responseCode = "404", description = "Team or user not found")
    @ApiResponse(responseCode = "409", description = "User is already a member")
    public ResponseEntity<MemberResponse> addMember(@PathVariable Long teamId,
                                                    @Valid @RequestBody AddMemberRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.addMember(teamId, authentication.getName(), request));
    }

    @PutMapping("/{teamId}/members/{userId}/role")
    @Operation(summary = "Change a member's role (OWNER only)")
    @ApiResponse(responseCode = "200", description = "Role changed")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "403", description = "Only a team OWNER can change roles")
    @ApiResponse(responseCode = "404", description = "Team or member not found")
    @ApiResponse(responseCode = "422", description = "A team must keep at least one OWNER")
    public ResponseEntity<MemberResponse> changeRole(@PathVariable Long teamId,
                                                     @PathVariable Long userId,
                                                     @Valid @RequestBody ChangeRoleRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.ok(
                teamService.changeRole(teamId, userId, authentication.getName(), request));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Remove a member, or leave the team yourself")
    @ApiResponse(responseCode = "204", description = "Member removed")
    @ApiResponse(responseCode = "403", description = "Only a team OWNER can remove other members")
    @ApiResponse(responseCode = "404", description = "Team or member not found")
    @ApiResponse(responseCode = "422", description = "A team must keep at least one OWNER")
    public ResponseEntity<Void> removeMember(@PathVariable Long teamId,
                                             @PathVariable Long userId,
                                             Authentication authentication) {
        teamService.removeMember(teamId, userId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}