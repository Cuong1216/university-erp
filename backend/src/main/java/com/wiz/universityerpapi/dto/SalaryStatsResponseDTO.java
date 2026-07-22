package com.wiz.universityerpapi.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStatsResponseDTO {
    private List<MonthlySalaryTrendDTO> monthlyTrends;
    private List<DepartmentSalaryDTO> departmentDistributions; // Phân bổ theo Khoa
    private List<DepartmentSalaryDTO> boMonDistributions;      // Phân bổ theo Bộ môn
    private Integer currentMonth;
    private Integer currentYear;
    private BigDecimal totalSalaryCurrentMonth;
    private BigDecimal totalSalaryPreviousMonth;
    private Double monthlyGrowthRate;
    private Long totalLecturersPaid;
}
