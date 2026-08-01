package com.wiz.universityerpapi.payroll.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface MonthlySalaryTrendView {
    Integer getThang();
    Integer getNam();
    BigDecimal getTongTienLuong();
}
