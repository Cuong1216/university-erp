package com.wiz.universityerpapi.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UUID userId;
    private String username;
    private List<String> roles;
}
