package com.pulsecheck;

import com.pulsecheck.user.entity.User;
import com.pulsecheck.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthControllerTests {

    private static final String REGISTER_URL = "/api/v1/auth/register";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String body(String email, String password) {
        return """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);
    }

    @Test
    void shouldRegisterUserAndStoreHashedPassword() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("new@example.com", "password123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User saved = userRepository.findByEmail("new@example.com").orElseThrow();
        assertNotEquals("password123", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("password123", saved.getPasswordHash()));
    }

    @Test
    void shouldRejectAlreadyRegisteredEmail() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("dup@example.com", "password123")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("dup@example.com", "password456")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidEmailAndShortPassword() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("not-an-email", "123")))
                .andExpect(status().isBadRequest());
    }
}