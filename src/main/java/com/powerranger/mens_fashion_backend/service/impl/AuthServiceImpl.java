package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.LoginRequest;
import com.powerranger.mens_fashion_backend.dto.request.RegisterRequest;
import com.powerranger.mens_fashion_backend.dto.response.AuthResponse;
import com.powerranger.mens_fashion_backend.entity.User;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.repository.UserRepository;
import com.powerranger.mens_fashion_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadRequestException("User account is inactive");
        }

        String token = generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.isAdmin());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setActive(true);
        // First user can be admin if no admin exists
        user.setAdmin(!userRepository.existsByAdminTrue());

        userRepository.save(user);

        String token = generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.isAdmin());
    }

    private String generateToken(User user) {
        Instant now = Instant.now();
        String role = user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(36000))
                .subject(user.getEmail())
                .claim("roles", List.of(role))
                .build();
                
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
