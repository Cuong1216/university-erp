package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ket_qua_hoc_tap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KetQuaHocTap {
    @EmbeddedId
    private KetQuaHocTapId id;

    @MapsId("maSv")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_sv")
    private SinhVien sinhVien;

    @MapsId("maLopHp")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop_hp")
    private LopHocPhan lopHocPhan;

    @Column(name = "diem_chuyen_can", precision = 4, scale = 2)
    private BigDecimal diemChuyenCan;

    @Column(name = "diem_giua_ky", precision = 4, scale = 2)
    private BigDecimal diemGiuaKy;

    @Column(name = "diem_cuoi_ky", precision = 4, scale = 2)
    private BigDecimal diemCuoiKy;

    @Column(name = "diem_tong_ket", precision = 4, scale = 2, insertable = false, updatable = false)
    private BigDecimal diemTongKet;
}
