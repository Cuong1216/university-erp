package com.wiz.universityerpapi.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySalaryTrendDTO {
    private Integer thang;
    private Integer nam;
    private String period; // Ví dụ: "T09/2023"
    private BigDecimal totalSalary;
}
