package com.pulsecheck.config;

import com.pulsecheck.entity.Role;
import com.pulsecheck.entity.Team;
import com.pulsecheck.entity.TeamPlan;
import com.pulsecheck.entity.TeamRole;
import com.pulsecheck.entity.User;
import com.pulsecheck.repository.TeamRepository;
import com.pulsecheck.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
public class DemoDataSeeder implements ApplicationRunner {

    static final String DEMO_EMAIL = "demo@pulsecheck.com";
    static final String COLLEAGUE_EMAIL = "colleague@pulsecheck.com";
    static final String DEMO_PASSWORD = "demo12345";

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository,
                          TeamRepository teamRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }

        User demo = createUser(DEMO_EMAIL);
        User colleague = createUser(COLLEAGUE_EMAIL);

        Team team = new Team();
        team.setName("Demo Team");
        team.setPlan(TeamPlan.PRO);
        team.addMember(demo, TeamRole.OWNER);
        team.addMember(colleague, TeamRole.MEMBER);
        teamRepository.save(team);

        log.info("Demo data created: log in as {} / {}", DEMO_EMAIL, DEMO_PASSWORD);
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
}