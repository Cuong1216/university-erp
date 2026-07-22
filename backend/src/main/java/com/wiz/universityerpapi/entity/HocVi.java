package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "hoc_vi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HocVi {
    @Id
    @Column(name = "ma_hv", length = 20, nullable = false)
    private String maHv;

    @Column(name = "ten_hv", nullable = false, length = 100)
    private String tenHv;

    @Column(name = "he_so_hv", precision = 4, scale = 2)
    private BigDecimal heSoHv;
}
