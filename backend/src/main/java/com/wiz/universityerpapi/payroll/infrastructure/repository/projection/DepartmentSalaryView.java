package com.wiz.universityerpapi.payroll.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface DepartmentSalaryView {
    String getMaKhoaHoacBoMon();
    String getTenKhoaHoacBoMon();
    BigDecimal getTongTienLuong();
}
