package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.LoginRequestDTO;
import com.wiz.universityerpapi.dto.LoginResponseDTO;
import com.wiz.universityerpapi.entity.Role;
import com.wiz.universityerpapi.security.CustomUserDetails;
import com.wiz.universityerpapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

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
}
