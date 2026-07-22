package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mon_hoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonHoc {
    @Id
    @Column(name = "ma_mon", length = 20, nullable = false)
    private String maMon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bo_mon")
    private BoMon boMon;

    @Column(name = "ten_mon", nullable = false, length = 255)
    private String tenMon;

    @Column(name = "so_tin_chi")
    private Integer soTinChi;

    @Column(name = "so_tiet_ly_thuyet")
    @Builder.Default
    private Integer soTietLyThuyet = 0;

    @Column(name = "so_tiet_thuc_hanh")
    @Builder.Default
    private Integer soTietThucHanh = 0;
}
