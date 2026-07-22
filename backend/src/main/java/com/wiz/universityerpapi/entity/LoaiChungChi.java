package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loai_cc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiChungChi {
    @Id
    @Column(name = "ma_loai_cc", length = 20, nullable = false)
    private String maLoaiCc;

    @Column(name = "ten_loai_cc", nullable = false, length = 100)
    private String tenLoaiCc;
}
