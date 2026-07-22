package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "phan_cong_day", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_gv", "ma_lop_hp"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanCongDay {
    @Id
    @Column(name = "ma_phan_cong", length = 20, nullable = false)
    private String maPhanCong;

    @Column(name = "ma_gv", length = 20)
    private String maGv;

    @Column(name = "ma_lop_hp", length = 20)
    private String maLopHp;

    @Column(name = "vai_tro", length = 50)
    @Builder.Default
    private String vaiTro = "Giảng dạy chính";
}
