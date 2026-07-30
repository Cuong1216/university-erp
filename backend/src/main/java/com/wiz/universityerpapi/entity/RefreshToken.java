package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Date expiresAt;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRevoked = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
}
