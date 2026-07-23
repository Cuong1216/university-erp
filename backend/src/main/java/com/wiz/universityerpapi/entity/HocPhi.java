package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoc_phi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HocPhi {

    @Id
    @Column(name = "ma_hoc_phi", length = 30, nullable = false)
    private String maHocPhi;

    @Column(name = "ma_sv", length = 20, nullable = false)
    private String maSv;

    @Column(name = "nam_hoc", length = 20, nullable = false)
    private String namHoc;

    @Column(name = "hoc_ky", nullable = false)
    private Integer hocKy;

    @Column(name = "so_tien_phai_nop", precision = 15, scale = 2, nullable = false)
    private BigDecimal soTienPhaiNop;

    @Column(name = "so_tien_da_nop", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal soTienDaNop = BigDecimal.ZERO;

    @Column(name = "trang_thai", length = 30, nullable = false)
    private String trangThai; // CHUA_NOP, NOP_MOT_PHAN, DA_NOP_DU

    @Column(name = "han_nop", nullable = false)
    private LocalDateTime hanNop;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
