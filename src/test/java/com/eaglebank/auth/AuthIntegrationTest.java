package com.eaglebank.auth;

import com.eaglebank.user.User;
import com.eaglebank.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {

    private static final String AUTH_ENDPOINT = "/v1/auth/login";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testEmail = "loginuser@example.com";
    private final String rawPassword = "validPassword123";

    @BeforeAll
    void setupUser() {
        User user = User.builder()
                .name("Login User")
                .email(testEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .phoneNumber("+441234567890")
                .addressLine1("1 Login Street")
                .addressLine2("")
                .addressLine3("")
                .town("Logintown")
                .county("Loginshire")
                .postcode("LG1 1AA")
                .build();

        userRepository.save(user);
    }

    @Nested
    @DisplayName("Login Endpoint")
    class LoginEndpointTests {

        @Test
        @DisplayName("When given valid credentials should return 200 and a bearer token")
        void loginSuccess() throws Exception {
            String loginRequest = String.format("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """, testEmail, rawPassword);

            mockMvc.perform(post(AUTH_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginRequest))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("When given invalid credentials should return 401")
        void loginFailure() throws Exception {
            String loginRequest = String.format("""
                    {
                      "email": "%s",
                      "password": "wrongPassword"
                    }
                    """, testEmail);

            mockMvc.perform(post(AUTH_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginRequest))
                    .andExpect(status().isUnauthorized());
        }
    }
}
