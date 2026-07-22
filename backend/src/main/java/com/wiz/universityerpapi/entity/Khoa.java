package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "khoa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Khoa {
    @Id
    @Column(name = "ma_khoa", length = 20, nullable = false)
    private String maKhoa;

    @Column(name = "ten_khoa", nullable = false, length = 255)
    private String tenKhoa;

    @Column(name = "co_so", length = 100)
    private String coSo;
}
