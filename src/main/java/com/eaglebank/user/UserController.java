package com.eaglebank.user;

import com.eaglebank.security.SecurityUtils;
import com.eaglebank.user.dto.CreateUserRequest;
import com.eaglebank.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse createdUserResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        String authenticatedUserId = SecurityUtils.getAuthenticatedUserId();

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You are not authorized to access this user's details");
        }

        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
