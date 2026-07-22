package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chuc_danh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChucDanh {
    @Id
    @Column(name = "ma_cd", length = 20, nullable = false)
    private String maCd;

    @Column(name = "ten_cd", nullable = false, length = 100)
    private String tenCd;

    @Column(name = "he_so_cd", precision = 4, scale = 2)
    private BigDecimal heSoCd;
}
