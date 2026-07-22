package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tuan_hoc_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuanHocChiTiet {
    @Id
    @Column(name = "ma_tuan_hoc", length = 20, nullable = false)
    private String maTuanHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lich")
    private LichHocChiTiet lichHocChiTiet;

    @Column(name = "tuan_thu", nullable = false)
    private Integer tuanThu;
}
