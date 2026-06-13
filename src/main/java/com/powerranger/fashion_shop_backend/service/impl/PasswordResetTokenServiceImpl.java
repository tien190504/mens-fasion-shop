package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.entity.PasswordResetToken;
import com.powerranger.fashion_shop_backend.entity.User;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.repository.PasswordResetTokenRepository;
import com.powerranger.fashion_shop_backend.repository.UserRepository;
import com.powerranger.fashion_shop_backend.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String generateToken(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Delete any existing tokens for this user first
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(tokenStr);
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
        passwordResetTokenRepository.save(token);

        return tokenStr;
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}
