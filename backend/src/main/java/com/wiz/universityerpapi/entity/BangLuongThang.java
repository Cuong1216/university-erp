package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bang_luong_thang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BangLuongThang {
    @Id
    @Column(name = "ma_bang_luong", length = 50, nullable = false)
    private String maBangLuong;

    @Column(name = "ma_gv", length = 20)
    private String maGv;

    @Column(name = "thang")
    private Integer thang;

    @Column(name = "nam")
    private Integer nam;

    @Column(name = "tong_so_tiet_dieu_dong")
    @Builder.Default
    private Integer tongSoTietDieuDong = 0;

    @Column(name = "tong_so_tiet_thuc_te")
    @Builder.Default
    private Integer tongSoTietThucTe = 0;

    @Column(name = "so_tiet_day_thay")
    @Builder.Default
    private Integer soTietDayThay = 0;

    @Column(name = "he_so_cd_snapshot", precision = 4, scale = 2, nullable = false)
    private BigDecimal heSoCdSnapshot;

    @Column(name = "he_so_hv_snapshot", precision = 4, scale = 2, nullable = false)
    private BigDecimal heSoHvSnapshot;

    @Column(name = "luong_co_ban_snapshot", precision = 15, scale = 2, nullable = false)
    private BigDecimal luongCoBanSnapshot;

    @Column(name = "don_gia_tiet_snapshot", precision = 15, scale = 2, nullable = false)
    private BigDecimal donGiaTietSnapshot;

    @Column(name = "tong_tien_luong", precision = 15, scale = 2, nullable = false)
    private BigDecimal tongTienLuong;

    @Column(name = "trang_thai", length = 30)
    @Builder.Default
    private String trangThai = "CHO_DUYET";

    @CreationTimestamp
    @Column(name = "ngay_chot_luong", updatable = false)
    private LocalDateTime ngayChotLuong;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chi_tiet_tinh_luong_json", columnDefinition = "jsonb")
    private String chiTietTinhLuongJson;
}
