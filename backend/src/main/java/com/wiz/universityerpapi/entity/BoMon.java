package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bo_mon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoMon {
    @Id
    @Column(name = "ma_bo_mon", length = 20, nullable = false)
    private String maBoMon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa")
    private Khoa khoa;

    @Column(name = "ten_bo_mon", nullable = false, length = 255)
    private String tenBoMon;
}
