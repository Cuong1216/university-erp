package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.LoginRequestDTO;
import com.wiz.universityerpapi.dto.LoginResponseDTO;
import com.wiz.universityerpapi.entity.Role;
import com.wiz.universityerpapi.security.CustomUserDetails;
import com.wiz.universityerpapi.security.JwtTokenProvider;
import com.wiz.universityerpapi.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
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

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        List<String> roles = userDetails.getUser().getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userDetails.getUser().getId())
                .username(userDetails.getUser().getUsername())
                .roles(roles)
                .build();
    }

    @Override
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
        } catch (Exception e) {
            log.warn("Không thể blacklist token khi logout: {}", e.getMessage());
            // Không ném exception — logout vẫn thành công về mặt client
        }
    }
}
