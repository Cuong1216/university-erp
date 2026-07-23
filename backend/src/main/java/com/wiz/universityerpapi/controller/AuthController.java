package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.LoginRequestDTO;
import com.wiz.universityerpapi.dto.LoginResponseDTO;
import com.wiz.universityerpapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO response = authService.login(loginRequestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout: blacklist JWT token hiện tại của user đang đăng nhập.
     * Yêu cầu: phải gửi kèm "Authorization: Bearer {token}" header.
     * Sau khi gọi API này, token cũ sẽ bị từ chối ở mọi endpoint dù chưa hết hạn.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @AuthenticationPrincipal UserDetails currentUser) {
        authService.logout(authorizationHeader);
        String username = currentUser != null ? currentUser.getUsername() : "unknown";
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đăng xuất thành công. Token đã bị vô hiệu hóa.",
                "username", username
        ));
    }
}
