package com.eaglebank.auth;

import com.eaglebank.auth.dto.AuthRequest;
import com.eaglebank.auth.dto.AuthResponse;
import com.eaglebank.common.exception.InvalidCredentialsException;
import com.eaglebank.user.User;
import com.eaglebank.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Auth Service Unit Tests")
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AutoCloseable mocks;

    private AuthRequest request;

    private final String email = "user@example.com";
    private final String rawPassword = "password123";
    private final String passwordHash = "hashedPassword";
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        request = new AuthRequest(email, rawPassword);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("When given valid credentials, returns AuthResponse with token")
    void authenticateSuccess() {
        User user = User.builder()
                .id(userId)
                .email(email)
                .passwordHash(passwordHash)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, passwordHash)).thenReturn(true);
        String token = "jwt-token";
        when(jwtService.generateToken(userId, email)).thenReturn(token);

        AuthResponse response = authService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, passwordHash);
        verify(jwtService).generateToken(userId, email);
    }

    @Test
    @DisplayName("When user not found, throws InvalidCredentialsException")
    void authenticateUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("When password does not match, throws InvalidCredentialsException")
    void authenticatePasswordMismatch() {
        User user = User.builder()
                .id(userId)
                .email(email)
                .passwordHash(passwordHash)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, passwordHash)).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, passwordHash);
        verify(jwtService, never()).generateToken(any(), any());
    }
}
