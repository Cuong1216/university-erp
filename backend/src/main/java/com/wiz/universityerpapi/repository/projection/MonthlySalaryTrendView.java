package com.wiz.universityerpapi.repository.projection;

import java.math.BigDecimal;

public interface MonthlySalaryTrendView {
    Integer getThang();
    Integer getNam();
    BigDecimal getTongTienLuong();
}
