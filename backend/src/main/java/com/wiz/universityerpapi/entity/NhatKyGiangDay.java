package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "nhat_ky_giang_day")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhatKyGiangDay {
    @Id
    @Column(name = "ma_nhat_ky", length = 20, nullable = false)
    private String maNhatKy;

    @Column(name = "ma_lich", length = 20)
    private String maLich;

    @Column(name = "ngay_day_thuc_te", nullable = false)
    private LocalDate ngayDayThucTe;

    @Column(name = "so_tiet_thuc_te", nullable = false)
    private Integer soTietThucTe;

    @Column(name = "trang_thai_buoi_hoc", length = 50)
    @Builder.Default
    private String trangThaiBuoiHoc = "Bình thường";

    @Column(name = "ma_gv_day_thay", length = 20)
    private String maGvDayThay;

    @Column(name = "trang_thai_thanh_toan")
    @Builder.Default
    private Boolean trangThaiThanhToan = false;

    @Column(name = "ma_bang_luong", length = 50)
    private String maBangLuong;
}
