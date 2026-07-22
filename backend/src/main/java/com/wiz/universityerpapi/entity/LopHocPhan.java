package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lop_hoc_phan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LopHocPhan {
    @Id
    @Column(name = "ma_lop_hp", length = 20, nullable = false)
    private String maLopHp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mon")
    private MonHoc monHoc;

    @Column(name = "nam_hoc", length = 20)
    private String namHoc;

    @Column(name = "hoc_ky")
    private Integer hocKy;

    @Column(name = "si_so_toi_da")
    private Integer siSoToiDa;
}
