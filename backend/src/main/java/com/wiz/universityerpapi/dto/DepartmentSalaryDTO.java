package com.wiz.universityerpapi.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSalaryDTO {
    private String maKhoaHoacBoMon;
    private String tenKhoaHoacBoMon;
    private BigDecimal totalSalary;
    private Double percentage; // Tỷ lệ % phân bổ
}
