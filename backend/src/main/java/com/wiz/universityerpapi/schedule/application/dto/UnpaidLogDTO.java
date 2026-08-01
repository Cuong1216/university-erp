package com.wiz.universityerpapi.schedule.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnpaidLogDTO {
    private String maNhatKy;
    private String maLich;
    private LocalDate ngayDayThucTe;
    private Integer soTietThucTe;
}
