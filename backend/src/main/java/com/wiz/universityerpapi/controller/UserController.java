package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.UserDTO;
import com.wiz.universityerpapi.entity.Role;
import com.wiz.universityerpapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream().map(u -> {
            String fullName = "-";
            String email = "-";
            if (u.getGiangVien() != null) {
                fullName = u.getGiangVien().getHoDem() + " " + u.getGiangVien().getTen();
                email = u.getGiangVien().getEmail();
            } else if (u.getSinhVien() != null) {
                fullName = u.getSinhVien().getHoDem() + " " + u.getSinhVien().getTen();
                email = u.getSinhVien().getEmail();
            }

            return UserDTO.builder()
                .id(u.getId().toString())
                .username(u.getUsername())
                .email(email)
                .fullName(fullName)
                .isActive("ACTIVE".equals(u.getStatus()))
                .roles(u.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()))
                .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(users);
    }
}
