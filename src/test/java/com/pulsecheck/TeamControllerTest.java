package com.pulsecheck;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class TeamControllerTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String TEAMS_URL = "/api/v1/teams";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    private record TestUser(Long id, String email, String token) {
    }

    // ---------- helpers ----------

    private String credentials(String email) {
        return """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, PASSWORD);
    }

    private TestUser registerAndLogin(String email) throws Exception {
        String registerResponse = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = ((Number) JsonPath.read(registerResponse, "$.id")).longValue();

        String loginResponse = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(loginResponse, "$.token");

        return new TestUser(id, email, token);
    }

    private String bearer(TestUser user) {
        return "Bearer " + user.token();
    }

    private Long createTeam(TestUser owner, String name) throws Exception {
        String response = mockMvc.perform(post(TEAMS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"%s\"}".formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private void addMember(Long teamId, TestUser actor, TestUser member, String role) throws Exception {
        mockMvc.perform(post(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(actor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"role\": \"%s\"}".formatted(member.email(), role)))
                .andExpect(status().isCreated());
    }

    private String roleBody(String role) {
        return "{\"role\": \"%s\"}".formatted(role);
    }

    private String roleUrl(Long teamId, TestUser member) {
        return TEAMS_URL + "/" + teamId + "/members/" + member.id() + "/role";
    }

    private String memberUrl(Long teamId, TestUser member) {
        return TEAMS_URL + "/" + teamId + "/members/" + member.id();
    }

    // ---------- creating teams ----------

    @Test
    void shouldCreateTeamAndMakeCreatorOwner() throws Exception {
        TestUser owner = registerAndLogin("owner1@example.com");

        mockMvc.perform(post(TEAMS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Backend\", \"plan\": \"PRO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Backend"))
                .andExpect(jsonPath("$.plan").value("PRO"));

        mockMvc.perform(get(TEAMS_URL).header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldDefaultPlanToFree() throws Exception {
        TestUser owner = registerAndLogin("owner2@example.com");

        mockMvc.perform(post(TEAMS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Free team\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plan").value("FREE"));
    }

    @Test
    void shouldRejectTeamWithBlankName() throws Exception {
        TestUser owner = registerAndLogin("owner3@example.com");

        mockMvc.perform(post(TEAMS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void shouldRequireAuthenticationToCreateTeam() throws Exception {
        mockMvc.perform(post(TEAMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"No token\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListOnlyMyTeams() throws Exception {
        TestUser alice = registerAndLogin("alice@example.com");
        TestUser bob = registerAndLogin("bob@example.com");
        createTeam(alice, "Alice 1");
        createTeam(alice, "Alice 2");
        createTeam(bob, "Bob 1");

        mockMvc.perform(get(TEAMS_URL).header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ---------- members ----------

    @Test
    void shouldListMembersWithCreatorAsOwner() throws Exception {
        TestUser owner = registerAndLogin("owner4@example.com");
        Long teamId = createTeam(owner, "Members team");

        mockMvc.perform(get(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("owner4@example.com"))
                .andExpect(jsonPath("$[0].teamRole").value("OWNER"));
    }

    @Test
    void shouldAddMemberByEmail() throws Exception {
        TestUser owner = registerAndLogin("owner5@example.com");
        TestUser member = registerAndLogin("member5@example.com");
        Long teamId = createTeam(owner, "Add member");

        mockMvc.perform(post(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"member5@example.com\", \"role\": \"MEMBER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("member5@example.com"))
                .andExpect(jsonPath("$.teamRole").value("MEMBER"));

        mockMvc.perform(get(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRejectDuplicateMember() throws Exception {
        TestUser owner = registerAndLogin("owner6@example.com");
        TestUser member = registerAndLogin("member6@example.com");
        Long teamId = createTeam(owner, "Duplicate");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(post(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"member6@example.com\", \"role\": \"VIEWER\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectUnknownUserEmail() throws Exception {
        TestUser owner = registerAndLogin("owner7@example.com");
        Long teamId = createTeam(owner, "Unknown user");

        mockMvc.perform(post(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"nobody@example.com\", \"role\": \"MEMBER\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldForbidNonOwnerFromAddingMembers() throws Exception {
        TestUser owner = registerAndLogin("owner8@example.com");
        TestUser member = registerAndLogin("member8@example.com");
        TestUser outsider = registerAndLogin("outsider8@example.com");
        Long teamId = createTeam(owner, "Forbidden add");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(post(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"outsider8@example.com\", \"role\": \"MEMBER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidNonMemberFromListingMembers() throws Exception {
        TestUser owner = registerAndLogin("owner9@example.com");
        TestUser outsider = registerAndLogin("outsider9@example.com");
        Long teamId = createTeam(owner, "Private team");

        mockMvc.perform(get(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(outsider)))
                .andExpect(status().isForbidden());
    }

    // ---------- roles and the "last OWNER" rule ----------

    @Test
    void shouldChangeMemberRole() throws Exception {
        TestUser owner = registerAndLogin("owner10@example.com");
        TestUser member = registerAndLogin("member10@example.com");
        Long teamId = createTeam(owner, "Change role");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(put(roleUrl(teamId, member))
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamRole").value("VIEWER"));
    }

    @Test
    void shouldNotDemoteTheLastOwner() throws Exception {
        TestUser owner = registerAndLogin("owner11@example.com");
        Long teamId = createTeam(owner, "Last owner");

        mockMvc.perform(put(roleUrl(teamId, owner))
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody("MEMBER")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A team must keep at least one OWNER"));
    }

    @Test
    void shouldAllowDemotingAnOwnerWhenAnotherOwnerExists() throws Exception {
        TestUser owner = registerAndLogin("owner12@example.com");
        TestUser second = registerAndLogin("second12@example.com");
        Long teamId = createTeam(owner, "Two owners");
        addMember(teamId, owner, second, "OWNER");

        mockMvc.perform(put(roleUrl(teamId, owner))
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamRole").value("MEMBER"));
    }

    @Test
    void shouldForbidNonOwnerFromChangingRoles() throws Exception {
        TestUser owner = registerAndLogin("owner13@example.com");
        TestUser member = registerAndLogin("member13@example.com");
        Long teamId = createTeam(owner, "Forbidden role change");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(put(roleUrl(teamId, owner))
                        .header(HttpHeaders.AUTHORIZATION, bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody("VIEWER")))
                .andExpect(status().isForbidden());
    }

    // ---------- removing members ----------

    @Test
    void shouldRemoveMember() throws Exception {
        TestUser owner = registerAndLogin("owner14@example.com");
        TestUser member = registerAndLogin("member14@example.com");
        Long teamId = createTeam(owner, "Remove member");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(delete(memberUrl(teamId, member))
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(TEAMS_URL + "/" + teamId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldAllowMemberToLeaveTeam() throws Exception {
        TestUser owner = registerAndLogin("owner15@example.com");
        TestUser member = registerAndLogin("member15@example.com");
        Long teamId = createTeam(owner, "Leave team");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(delete(memberUrl(teamId, member))
                        .header(HttpHeaders.AUTHORIZATION, bearer(member)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotRemoveTheLastOwner() throws Exception {
        TestUser owner = registerAndLogin("owner16@example.com");
        Long teamId = createTeam(owner, "Keep owner");

        mockMvc.perform(delete(memberUrl(teamId, owner))
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A team must keep at least one OWNER"));
    }

    @Test
    void shouldForbidMemberFromRemovingOthers() throws Exception {
        TestUser owner = registerAndLogin("owner17@example.com");
        TestUser member = registerAndLogin("member17@example.com");
        Long teamId = createTeam(owner, "Forbidden remove");
        addMember(teamId, owner, member, "MEMBER");

        mockMvc.perform(delete(memberUrl(teamId, owner))
                        .header(HttpHeaders.AUTHORIZATION, bearer(member)))
                .andExpect(status().isForbidden());
    }
}