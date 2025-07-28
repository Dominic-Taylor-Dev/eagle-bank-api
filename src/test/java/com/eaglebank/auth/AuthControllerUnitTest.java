package com.eaglebank.auth;

import com.eaglebank.auth.dto.AuthRequest;
import com.eaglebank.auth.dto.AuthResponse;
import com.eaglebank.common.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("Auth Controller Unit Tests")
class AuthControllerUnitTest {

    private static final String BASE_URL = "/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    private AuthResponse sampleAuthResponse() {
        return new AuthResponse("fake-jwt-token", "Bearer");
    }

    @Nested
    @DisplayName("Login Endpoint")
    class LoginTests {

        @Test
        @DisplayName("When given valid credentials should authenticate successfully")
        void shouldAuthenticateSuccessfully() throws Exception {
            when(authService.authenticate(any(AuthRequest.class))).thenReturn(sampleAuthResponse());

            AuthRequest validRequest = new AuthRequest("user@example.com", "password123");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("When request body is missing should return 400 with invalid request body error")
        void shouldReturnBadRequestForMissingRequestBody() throws Exception {
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("")) // empty body
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/invalid-request-body"))
                    .andExpect(jsonPath("$.title").value("Invalid Request Body"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Request body is missing or malformed JSON."))
                    .andExpect(jsonPath("$.instance").value(BASE_URL + "/login"));
        }

        @Test
        @DisplayName("When credentials are invalid should return 401 Unauthorized")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            when(authService.authenticate(any(AuthRequest.class)))
                    .thenThrow(new InvalidCredentialsException());

            AuthRequest invalidRequest = new AuthRequest("wrong@example.com", "badpassword");

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/invalid-credentials"))
                    .andExpect(jsonPath("$.title").value("Invalid Credentials"));
        }

        @Test
        @DisplayName("When email format is invalid should return 400 with validation errors")
        void shouldReturnBadRequestForInvalidEmailFormat() throws Exception {
            String invalidEmailRequestJson = """
                {
                    "email": "not-an-email",
                    "password": "validPassword123"
                }
                """;

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidEmailRequestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/validation-error"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.email").value("Must be a valid email address"));
        }

        @Test
        @DisplayName("When password is missing should return 400 with validation errors")
        void shouldReturnBadRequestForMissingPassword() throws Exception {
            String missingPasswordJson = """
                {
                    "email": "user@example.com"
                }
                """;

            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(missingPasswordJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("https://api.eaglebank.com/problems/validation-error"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.password").value("Password is required"));
        }
    }
}
