package com.eaglebank.user;

import com.eaglebank.auth.JwtService;
import com.eaglebank.common.exception.EmailAlreadyInUseException;
import com.eaglebank.common.exception.InvalidUserIdException;
import com.eaglebank.security.SecurityUtils;
import com.eaglebank.user.dto.CreateUserRequest;
import com.eaglebank.user.dto.UserResponse;
import com.eaglebank.user.dto.AddressDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("User Controller Unit Tests")
class UserControllerUnitTest {

    private static final String BASE_URL = "/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Nested
    @DisplayName("Create User")
    class CreateUserTests {

        private CreateUserRequest validRequest() {
            return new CreateUserRequest(
                    "Jane Doe",
                    "jane@example.com",
                    "securePass123",
                    "+12345678901",
                    new AddressDto(
                            "123 Main St",
                            "Apt 4B",
                            "Building 5",
                            "Springfield",
                            "SomeCounty",
                            "SP1 2AB"
                    )
            );
        }

        private UserResponse sampleUserResponse() {
            return new UserResponse(
                    "user-123",
                    "Jane Doe",
                    new AddressDto(
                            "123 Main St",
                            "Apt 4B",
                            "Building 5",
                            "Springfield",
                            "SomeCounty",
                            "SP1 2AB"
                    ),
                    "+12345678901",
                    "jane@example.com",
                    Instant.parse("2025-07-28T01:22:52.435Z"),
                    Instant.parse("2025-07-28T01:22:52.435Z")
            );
        }

        @Test
        @DisplayName("When given valid input should create user successfully")
        void shouldCreateUserSuccessfully() throws Exception {
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(sampleUserResponse());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("user-123"))
                    .andExpect(jsonPath("$.name").value("Jane Doe"))
                    .andExpect(jsonPath("$.email").value("jane@example.com"))
                    .andExpect(jsonPath("$.address.line1").value("123 Main St"));
        }

        @Test
        @DisplayName("When request body is missing should return 400 with invalid request body error")
        void shouldReturnBadRequestForMissingRequestBody() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("")) // empty body
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/invalid-request-body"))
                    .andExpect(jsonPath("$.title").value("Invalid Request Body"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Request body is missing or malformed JSON."))
                    .andExpect(jsonPath("$.instance").value(BASE_URL));
        }

        @Test
        @DisplayName("When address object is missing entirely should return 400 with appropriate error")
        void shouldReturnBadRequestWhenAddressIsMissing() throws Exception {
            String jsonMissingAddress = """
                {
                  "name": "Jane Doe",
                  "email": "jane@example.com",
                  "password": "strongPassword123",
                  "phoneNumber": "+12345678901"
                }
                """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMissingAddress))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/validation-error"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors.address").value("Address is required"));
        }

        @Test
        @DisplayName("When address subfields are missing or invalid should return 400 with detailed errors")
        void shouldReturnBadRequestForInvalidAddressFields() throws Exception {
            String invalidRequestBody = """
                {
                  "name": "a",
                  "email": "invalid-email",
                  "password": "short",
                  "phoneNumber": "12345",
                  "address": {
                    "line1": "",
                    "town": "",
                    "county": "",
                    "postcode": ""
                  }
                }
                """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/validation-error"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                    .andExpect(jsonPath("$.instance").value(BASE_URL))
                    .andExpect(jsonPath("$.errors.password").value("Password must be between 8 and 100 characters"))
                    .andExpect(jsonPath("$.errors.phoneNumber").value("Phone number must be in E.164 format starting with '+' followed by country code and number"))
                    .andExpect(jsonPath("$.errors.name").value("Name must be between 2 and 100 characters"))
                    .andExpect(jsonPath("$.errors.email").value("Must be a valid email address"))
                    .andExpect(jsonPath("$.errors['address.line1']").value("Address line 1 is required"))
                    .andExpect(jsonPath("$.errors['address.town']").value("Town is required"))
                    .andExpect(jsonPath("$.errors['address.county']").value("County is required"))
                    .andExpect(jsonPath("$.errors['address.postcode']").value("Postcode is required"));
        }

        @Test
        @DisplayName("When given valid input for an email address already in use should return 409")
        void shouldReturnConflictForDuplicateEmail() throws Exception {
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new EmailAlreadyInUseException("jane@example.com"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/email-already-in-use"))
                    .andExpect(jsonPath("$.title").value("Email Already In Use"));
        }
    }

    @Nested
    @DisplayName("Get User")
    class GetUserTests {

        private final String userId = "user-123";

        private UserResponse sampleUserResponse() {
            return new UserResponse(
                    userId,
                    "Jane Doe",
                    new AddressDto("123 Main St", "Apt 4B", "Building 5", "Springfield", "SomeCounty", "SP1 2AB"),
                    "+12345678901",
                    "jane@example.com",
                    Instant.now(),
                    Instant.now()
            );
        }

        @Test
        @DisplayName("Should return user if authenticated user matches path userId")
        void shouldReturnUserIfAuthenticated() throws Exception {
            // Simulate correct user authenticated
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUserId).thenReturn(userId);
                when(userService.getUserById(userId)).thenReturn(sampleUserResponse());

                mockMvc.perform(get(BASE_URL + "/" + userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(userId))
                        .andExpect(jsonPath("$.name").value("Jane Doe"))
                        .andExpect(jsonPath("$.email").value("jane@example.com"));
            }
        }

        @Test
        @DisplayName("Should return 403 if authenticated user does not match path userId")
        void shouldReturnForbiddenForMismatchedUserId() throws Exception {
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUserId).thenReturn("another-user");

                mockMvc.perform(get(BASE_URL + "/" + userId))
                        .andExpect(status().isForbidden());
            }
        }

        @Test
        @DisplayName("Should return 404 if user not found")
        void shouldReturnNotFoundWhenUserNotFound() throws Exception {
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getAuthenticatedUserId).thenReturn(userId);

                when(userService.getUserById(userId)).thenThrow(new InvalidUserIdException());

                mockMvc.perform(get(BASE_URL + "/" + userId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/user-not-found"))
                        .andExpect(jsonPath("$.title").value("User Not Found"));
            }
        }
    }
}
