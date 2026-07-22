package com.wiz.universityerpapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySalaryResponseDTO {
    private String maBangLuong;
    private String maGv;
    private Integer thang;
    private Integer nam;
    private Integer tongSoTietThucTe;
    private BigDecimal heSoCdSnapshot;
    private BigDecimal heSoHvSnapshot;
    private BigDecimal luongCoBanSnapshot;
    private BigDecimal donGiaTietSnapshot;
    private BigDecimal tongTienLuong;
    private String trangThai;
    private LocalDateTime ngayChotLuong;
}
