package com.wiz.universityerpapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChotLuongResponseDTO {
    private String maBangLuong;
    private String maGv;
    private Integer thang;
    private Integer nam;
    private Integer tongSoTietThucTe;
    private BigDecimal heSoCdSnapshot;
    private BigDecimal donGiaTietSnapshot;
    private BigDecimal tongTienLuong;
    private Map<String, Object> chiTietTinhLuongJson;
}
