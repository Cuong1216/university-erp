package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.LoginRequestDTO;
import com.wiz.universityerpapi.dto.LoginResponseDTO;
import com.wiz.universityerpapi.entity.Role;
import com.wiz.universityerpapi.core.security.CustomUserDetails;
import com.wiz.universityerpapi.core.security.CustomUserDetailsService;
import com.wiz.universityerpapi.core.security.JwtTokenProvider;
import com.wiz.universityerpapi.core.security.TokenBlacklistService;
import com.wiz.universityerpapi.entity.RefreshToken;
import com.wiz.universityerpapi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        String refreshTokenString = jwtTokenProvider.generateRefreshToken();
        
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshTokenString)
                .username(userDetails.getUsername())
                .expiresAt(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)) // 7 days
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        List<String> roles = userDetails.getUser().getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .userId(userDetails.getUser().getId())
                .username(userDetails.getUser().getUsername())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void logout(String bearerToken) {
        if (!StringUtils.hasText(bearerToken)) {
            log.warn("Logout được gọi với token rỗng");
            return;
        }

        // Bỏ prefix "Bearer " nếu có
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;

        try {
            String jti = jwtTokenProvider.getJtiFromToken(token);
            Date expiration = jwtTokenProvider.getExpirationFromToken(token);
            long remainingSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            tokenBlacklistService.blacklist(jti, remainingSeconds);
            log.info("Logout thành công, token JTI {} đã bị blacklist", jti);

            String username = jwtTokenProvider.getUsernameFromToken(token);
            refreshTokenRepository.revokeAllByUsername(username);
        } catch (Exception e) {
            log.warn("Không thể blacklist token khi logout: {}", e.getMessage());
            // Không ném exception — logout vẫn thành công về mặt client
        }
    }

    @Override
    @Transactional
    public LoginResponseDTO refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshTokenString)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không hợp lệ hoặc đã bị thu hồi"));

        if (refreshTokenEntity.getExpiresAt().before(new Date())) {
            refreshTokenEntity.setRevoked(true);
            refreshTokenRepository.save(refreshTokenEntity);
            throw new IllegalArgumentException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(refreshTokenEntity.getUsername());
        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
        String newRefreshTokenString = jwtTokenProvider.generateRefreshToken();

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .token(newRefreshTokenString)
                .username(userDetails.getUsername())
                .expiresAt(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)) // 7 days
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        List<String> roles = userDetails.getUser().getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        return LoginResponseDTO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .tokenType("Bearer")
                .userId(userDetails.getUser().getId())
                .username(userDetails.getUser().getUsername())
                .roles(roles)
                .build();
    }
}
