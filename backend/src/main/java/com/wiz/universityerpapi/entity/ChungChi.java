package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "chung_chi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChungChi {
    @Id
    @Column(name = "ma_chung_chi", length = 20, nullable = false)
    private String maChungChi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gv")
    private GiangVien giangVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_cc")
    private LoaiChungChi loaiChungChi;

    @Column(name = "ten_bang_cap", nullable = false, length = 255)
    private String tenBangCap;

    @Column(name = "noi_cap", length = 255)
    private String noiCap;

    @Column(name = "ngay_cap")
    private LocalDate ngayCap;
}
