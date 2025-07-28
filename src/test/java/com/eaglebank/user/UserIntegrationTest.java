package com.eaglebank.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
                  "name": "Alice Smith",
                  "address": {
                    "line1": "12 Green Lane",
                    "line2": "Flat 4",
                    "line3": "",
                    "town": "Brighton",
                    "county": "Sussex",
                    "postcode": "BN1 3AA"
                  },
                  "phoneNumber": "+447700900123",
                  "email": "alice@example.com",
                  "password": "password123"
                }
                """;
        }

        @Test
        @DisplayName("When given valid user data, should create user and persist in DB")
        void shouldCreateUserAndPersist() throws Exception {
            String testUserEmail = "alice@example.com";
            String testUserName = "Alice Smith";

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
}
