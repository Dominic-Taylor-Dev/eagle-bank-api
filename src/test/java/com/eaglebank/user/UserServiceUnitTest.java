package com.eaglebank.user;

import com.eaglebank.common.exception.EmailAlreadyInUseException;
import com.eaglebank.user.dto.AddressDto;
import com.eaglebank.user.dto.CreateUserRequest;
import com.eaglebank.user.dto.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("User Service Unit Tests")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        AddressDto address = new AddressDto(
                "123 Main St",
                "Apt 1",
                "Top Floor",
                "Redhill",
                "Surrey",
                "ZIP123"
        );

        request = new CreateUserRequest(
                "Jane Doe",
                "jane@example.com",
                "password123",
                "555-1234",
                address
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("When given valid input, successfully creates a new user")
    void createUserSuccess() {
        String mockUserId = "user-123";
        String mockHashedPassword = "hashedPassword";

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn(mockHashedPassword);

        User savedUser = User.builder()
                .id(mockUserId)
                .name(request.name())
                .email(request.email())
                .passwordHash(mockHashedPassword)
                .addressLine1(request.address().line1())
                .addressLine2(request.address().line2())
                .addressLine3(request.address().line3())
                .town(request.address().town())
                .county(request.address().county())
                .postcode(request.address().postcode())
                .phoneNumber(request.phoneNumber())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(mockUserId);
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.email()).isEqualTo(request.email());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("When email already exists, throws EmailAlreadyInUseException")
    void createUserEmailAlreadyExists() {
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessageContaining(request.email());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
