package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "sinh_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SinhVien {
    @Id
    @Column(name = "ma_sv", length = 20, nullable = false)
    private String maSv;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop_sh")
    private LopSinhHoat lopSinhHoat;

    @Column(name = "ho_dem", nullable = false, length = 100)
    private String hoDem;

    @Column(name = "ten", nullable = false, length = 50)
    private String ten;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "dien_thoai", length = 20)
    private String dienThoai;

    @Column(name = "trang_thai_hoc_tap", length = 50)
    @Builder.Default
    private String trangThaiHocTap = "Đang học";
}
