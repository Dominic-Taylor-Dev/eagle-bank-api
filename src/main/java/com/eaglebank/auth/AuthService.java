package com.eaglebank.auth;

import com.eaglebank.auth.dto.AuthRequest;
import com.eaglebank.auth.dto.AuthResponse;
import com.eaglebank.common.exception.InvalidCredentialsException;
import com.eaglebank.user.User;
import com.eaglebank.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse authenticate(AuthRequest request) {
        // A more specific exception is deliberately not thrown as we do not wish clients to know whether a given email
        // address is registered or not
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, "Bearer");
    }
}
