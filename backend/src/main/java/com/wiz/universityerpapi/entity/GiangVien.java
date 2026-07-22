package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "giang_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiangVien {
    @Id
    @Column(name = "ma_gv", length = 20, nullable = false)
    private String maGv;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bo_mon")
    private BoMon boMon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cd")
    private ChucDanh chucDanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hv")
    private HocVi hocVi;

    @Column(name = "loai_hop_dong", length = 50)
    @Builder.Default
    private String loaiHopDong = "Cơ hữu";

    @Column(name = "ho_dem", nullable = false, length = 100)
    private String hoDem;

    @Column(name = "ten", nullable = false, length = 50)
    private String ten;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "cccd", unique = true, length = 20)
    private String cccd;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "dien_thoai", length = 20)
    private String dienThoai;

    @Column(name = "ngay_vao_lam")
    private LocalDate ngayVaoLam;

    @Column(name = "trang_thai_lam_viec", length = 50)
    @Builder.Default
    private String trangThaiLamViec = "Đang công tác";
}
