package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lich_hoc_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichHocChiTiet {
    @Id
    @Column(name = "ma_lich", length = 20, nullable = false)
    private String maLich;

    @Column(name = "ma_lop_hp", length = 20)
    private String maLopHp;

    @Column(name = "phong_hoc", length = 50)
    private String phongHoc;

    @Column(name = "thu_trong_tuan")
    private Integer thuTrongTuan;

    @Column(name = "tiet_bat_dau")
    private Integer tietBatDau;

    @Column(name = "tiet_ket_thuc")
    private Integer tietKetThuc;

    @OneToMany(mappedBy = "lichHocChiTiet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TuanHocChiTiet> danhSachTuanHoc = new ArrayList<>();
}
