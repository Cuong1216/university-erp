package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lop_sinh_hoat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LopSinhHoat {
    @Id
    @Column(name = "ma_lop_sh", length = 20, nullable = false)
    private String maLopSh;

    @Column(name = "ten_lop_sh", nullable = false, length = 100)
    private String tenLopSh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khoa")
    private Khoa khoa;

    @Column(name = "khoa_hoc", length = 20)
    private String khoaHoc;
}
