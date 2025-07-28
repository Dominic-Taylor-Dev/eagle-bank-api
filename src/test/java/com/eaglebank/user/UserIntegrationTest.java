package com.eaglebank.user;

import com.eaglebank.auth.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Integration Tests")
class UserIntegrationTest {

    private static final String USER_ENDPOINT = "/v1/users";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("Create User Endpoint")
    class CreateUserTests {

        private String validUserJson() {
            return """
                {
                  "name": "Bob Smith",
                  "address": {
                    "line1": "12 Green Lane",
                    "line2": "Flat 4",
                    "line3": "",
                    "town": "Brighton",
                    "county": "Sussex",
                    "postcode": "BN1 3AA"
                  },
                  "phoneNumber": "+447700900123",
                  "email": "bob@example.com",
                  "password": "password123"
                }
                """;
        }

        @Test
        @DisplayName("When given valid user data, should create user and persist in DB")
        void shouldCreateUserAndPersist() throws Exception {
            String testUserEmail = "bob@example.com";
            String testUserName = "Bob Smith";

            mockMvc.perform(post(USER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validUserJson()))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.email").value(testUserEmail))
                    .andExpect(jsonPath("$.name").value(testUserName));

            boolean userExists = userRepository.existsByEmail(testUserEmail);
            assertTrue(userExists, "User should be saved in the database");
        }
    }

    @Nested
    @DisplayName("Get User Endpoint (secured)")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class GetUserTests {

        @Autowired
        private JwtService jwtService;

        private String testUserId;
        private static final String testUserEmail = "alice@example.com";

        @BeforeAll
        void setupUser() {
            User user = User.builder()
                    .name("Secure Test User")
                    .email(testUserEmail)
                    .passwordHash("irrelevantForThisTest")
                    .phoneNumber("+447283485960")
                    .addressLine1("123 Test Street")
                    .addressLine2("Apt 1")
                    .addressLine3("")
                    .town("Testville")
                    .county("Testshire")
                    .postcode("TST123")
                    .build();

            user = userRepository.save(user);
            this.testUserId = user.getId();

        }

        @Test
        @DisplayName("When accessing own user data with valid token returns 200")
        void getUserAuthorized() throws Exception {
            String token = jwtService.generateToken(testUserId, testUserEmail);

            mockMvc.perform(get(USER_ENDPOINT + "/" + testUserId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUserId))
                    .andExpect(jsonPath("$.email").value(testUserEmail))
                    .andExpect(jsonPath("$.name").isNotEmpty());
        }

        @Test
        @DisplayName("When accessing another user's data with valid token returns 403 Forbidden")
        void getUserForbidden() throws Exception {
            String token = jwtService.generateToken(testUserId, testUserEmail);
            String otherUserId = "some-other-user-id";

            mockMvc.perform(get(USER_ENDPOINT + "/" + otherUserId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("When accessing any user data without a valid token returns 401 Unauthorized")
        void getUserUnauthorized() throws Exception {
            mockMvc.perform(get(USER_ENDPOINT + "/" + testUserId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
