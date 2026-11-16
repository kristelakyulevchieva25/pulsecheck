package com.pulsecheck;

import com.pulsecheck.common.exception.ConflictException;
import com.pulsecheck.user.dto.RegisterRequest;
import com.pulsecheck.user.dto.UserResponse;
import com.pulsecheck.user.entity.Role;
import com.pulsecheck.user.entity.User;
import com.pulsecheck.user.mapper.UserMapper;
import com.pulsecheck.user.repository.UserRepository;
import com.pulsecheck.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("  Test@Example.com ", "password123");
        UserResponse expected = new UserResponse(1L, "test@example.com", Role.USER, Instant.now());

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(expected);

        UserResponse response = userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertEquals("test@example.com", captor.getValue().getEmail());
        assertEquals("hashed-password", captor.getValue().getPasswordHash());
        assertEquals(Role.USER, captor.getValue().getRole());
        assertEquals(expected, response);
    }

    @Test
    void shouldRejectAlreadyRegisteredEmail() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));

        verify(userRepository, never()).saveAndFlush(any());
    }
}