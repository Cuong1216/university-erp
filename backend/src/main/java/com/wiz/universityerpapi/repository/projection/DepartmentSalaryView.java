package com.wiz.universityerpapi.repository.projection;

import java.math.BigDecimal;

public interface DepartmentSalaryView {
    String getMaKhoaHoacBoMon();
    String getTenKhoaHoacBoMon();
    BigDecimal getTongTienLuong();
}
