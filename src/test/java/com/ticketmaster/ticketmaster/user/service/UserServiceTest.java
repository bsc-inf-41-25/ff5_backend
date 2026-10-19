package com.ticketmaster.ticketmaster.user.service;

import com.ticketmaster.ticketmaster.security.CustomUserDetailsService;
import com.ticketmaster.ticketmaster.security.JwtService;
import com.ticketmaster.ticketmaster.user.dto.AuthResponse;
import com.ticketmaster.ticketmaster.user.dto.LoginRequest;
import com.ticketmaster.ticketmaster.user.dto.RegisterRequest;
import com.ticketmaster.ticketmaster.user.entity.User;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    // ============== REGISTER TESTS ==============

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("newuser@test.com", "password123", User.Role.CUSTOMER);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userDetailsService.loadUserByUsername("newuser@test.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("newuser@test.com")
                        .password("hashedPassword")
                        .authorities("ROLE_CUSTOMER")
                        .build());
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock.jwt.token");

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("newuser@test.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("mock.jwt.token", response.getToken());

        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@test.com", "password123", User.Role.CUSTOMER);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );
        assertEquals("Email already registered", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldHashPasswordBeforeSaving() {
        RegisterRequest request = new RegisterRequest("newuser@test.com", "plaintext", User.Role.CUSTOMER);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertEquals("$2a$10$hashed", u.getPassword(), "Password must be hashed");
            return u;
        });
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("newuser@test.com").password("x").authorities("ROLE_CUSTOMER").build());
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token");

        userService.register(request);

        verify(userRepository).save(any(User.class));
    }

    // ============== LOGIN TESTS ==============

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("$2a$10$hashed")
                .role(User.Role.CUSTOMER)
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("test@test.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("test@test.com").password("$2a$10$hashed").authorities("ROLE_CUSTOMER").build());
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock.jwt.token");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("mock.jwt.token", response.getToken());
    }

    @Test
    void login_shouldThrowWhenBadCredentials() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.login(request));

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest("ghost@test.com", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }
}