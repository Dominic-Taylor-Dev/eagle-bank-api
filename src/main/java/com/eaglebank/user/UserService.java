package com.eaglebank.user;

import com.eaglebank.common.exception.EmailAlreadyInUseException;
import com.eaglebank.common.exception.InvalidUserIdException;
import com.eaglebank.user.dto.CreateUserRequest;
import com.eaglebank.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        var address = request.address();

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(hashedPassword)
                .phoneNumber(request.phoneNumber())
                .addressLine1(address.line1())
                .addressLine2(address.line2())
                .addressLine3(address.line3())
                .town(address.town())
                .county(address.county())
                .postcode(address.postcode())
                .build();

        User saved = userRepository.save(user);
        log.info("User created with id: {}", saved.getId());

        return UserResponse.from(saved);
    }

    public UserResponse getUserById(String userId) {
        var user = userRepository.findById(userId).orElseThrow(InvalidUserIdException::new);

        return UserResponse.from(user);
    }
}
