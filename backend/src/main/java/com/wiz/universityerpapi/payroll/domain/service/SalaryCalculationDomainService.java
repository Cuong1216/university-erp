package com.wiz.universityerpapi.payroll.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Service (POJO thuần) chịu trách nhiệm tính toán logic toán học lương.
 * Không phụ thuộc vào Spring Context hay DB.
 */
public class SalaryCalculationDomainService {

    public BigDecimal calculateTienGiangDay(int tongSoTietThucTe, BigDecimal donGiaTiet, BigDecimal heSoChucDanh) {
        return BigDecimal.valueOf(tongSoTietThucTe)
                .multiply(donGiaTiet)
                .multiply(heSoChucDanh)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTongTienLuong(BigDecimal luongCoBan, BigDecimal tienGiangDay) {
        return luongCoBan.add(tienGiangDay).setScale(2, RoundingMode.HALF_UP);
    }
}
