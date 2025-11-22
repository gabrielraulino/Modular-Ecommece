package com.modulith.ecommerce.auth;

import com.modulith.ecommerce.exception.InvalidCredentialsException;
import com.modulith.ecommerce.exception.ValidationException;
import com.modulith.ecommerce.common.Role;
import com.modulith.ecommerce.user.UserLoginDTO;
import com.modulith.ecommerce.user.UserDTO;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserModuleAPI userModuleAPI;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Value("${app.admin.key}")
    private String adminKey;

    public LoginResponse login(LoginRequest request) {
        UserLoginDTO user = userModuleAPI.findUserByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.email(), user.role().name());

        return new LoginResponse(token, user.email(), user.role().name());
    }

    public UserDTO register(RegisterRequest request) {
        // Determine role based on adminKey
        Role role = Role.USER;
        
        if (request.adminKey() != null && !request.adminKey().isBlank()) {
            if (!adminKey.equals(request.adminKey())) {
                throw new ValidationException("adminKey", request.adminKey(), "Invalid admin key");
            }
            role = Role.ADMIN;
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.password());

        // Register user
        return userModuleAPI.registerUser(
                request.name(),
                request.email(),
                encodedPassword,
                role
        );
    }
}

