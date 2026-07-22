package com.wiz.universityerpapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class KetQuaHocTapId implements Serializable {
    @Column(name = "ma_sv", length = 20)
    private String maSv;

    @Column(name = "ma_lop_hp", length = 20)
    private String maLopHp;
}
