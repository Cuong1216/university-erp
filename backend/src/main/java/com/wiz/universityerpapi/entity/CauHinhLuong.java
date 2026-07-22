package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cau_hinh_luong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauHinhLuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nam_hoc", length = 20, nullable = false)
    private String namHoc;

    @Column(name = "luong_co_ban", precision = 15, scale = 2, nullable = false)
    private BigDecimal luongCoBan;

    @Column(name = "don_gia_tiet_chuan", precision = 15, scale = 2, nullable = false)
    private BigDecimal donGiaTietChuan;

    @Column(name = "don_gia_vuot_gio", precision = 15, scale = 2, nullable = false)
    private BigDecimal donGiaVuotGio;

    @Column(name = "ngay_ap_dung", nullable = false)
    private LocalDate ngayApDung;

    @Column(name = "trang_thai", length = 20)
    @Builder.Default
    private String trangThai = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
