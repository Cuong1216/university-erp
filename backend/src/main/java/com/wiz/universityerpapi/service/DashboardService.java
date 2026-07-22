package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.DepartmentSalaryDTO;
import com.wiz.universityerpapi.dto.MonthlySalaryTrendDTO;
import com.wiz.universityerpapi.dto.SalaryStatsResponseDTO;
import com.wiz.universityerpapi.repository.BangLuongThangRepository;
import com.wiz.universityerpapi.repository.projection.DepartmentSalaryView;
import com.wiz.universityerpapi.repository.projection.MonthlySalaryTrendView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BangLuongThangRepository bangLuongThangRepository;

    @Transactional(readOnly = true)
    public SalaryStatsResponseDTO getSalaryStats(Integer thang, Integer nam) {
        // 1. Xác định tháng/năm hiện tại để báo cáo (nếu null thì lấy tháng mới nhất có dữ liệu hoặc tháng hiện tại)
        if (thang == null || nam == null) {
            List<MonthlySalaryTrendView> latest = bangLuongThangRepository.findSalaryTrends(PageRequest.of(0, 1));
            if (!latest.isEmpty()) {
                thang = latest.get(0).getThang();
                nam = latest.get(0).getNam();
            } else {
                LocalDate now = LocalDate.now();
                thang = now.getMonthValue();
                nam = now.getYear();
            }
        }

        // 2. Lấy biến động chi phí lương 6 tháng gần nhất (đến tháng/năm đang chọn)
        List<MonthlySalaryTrendView> rawTrends = bangLuongThangRepository.findSalaryTrends(PageRequest.of(0, 12));
        Map<String, BigDecimal> trendMap = new HashMap<>();
        for (MonthlySalaryTrendView tv : rawTrends) {
            String key = tv.getNam() + "-" + tv.getThang();
            trendMap.put(key, tv.getTongTienLuong() != null ? tv.getTongTienLuong() : BigDecimal.ZERO);
        }

        // Tạo chuỗi 6 tháng liên tiếp kết thúc tại (thang, nam)
        List<MonthlySalaryTrendDTO> monthlyTrends = new ArrayList<>();
        YearMonth currentYm = YearMonth.of(nam, thang);
        for (int i = 5; i >= 0; i--) {
            YearMonth targetYm = currentYm.minusMonths(i);
            int m = targetYm.getMonthValue();
            int y = targetYm.getYear();
            String key = y + "-" + m;
            BigDecimal amount = trendMap.getOrDefault(key, BigDecimal.ZERO);
            monthlyTrends.add(MonthlySalaryTrendDTO.builder()
                    .thang(m)
                    .nam(y)
                    .period(String.format("T%02d/%d", m, y))
                    .totalSalary(amount)
                    .build());
        }

        // 3. Phân bổ chi phí lương theo Khoa trong tháng hiện tại
        List<DepartmentSalaryView> khoaViews = bangLuongThangRepository.findSalaryDistributionByKhoa(thang, nam);
        BigDecimal totalSalaryCurrentMonth = khoaViews.stream()
                .map(v -> v.getTongTienLuong() != null ? v.getTongTienLuong() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DepartmentSalaryDTO> departmentDistributions = khoaViews.stream().map(v -> {
            BigDecimal amount = v.getTongTienLuong() != null ? v.getTongTienLuong() : BigDecimal.ZERO;
            double pct = 0.0;
            if (totalSalaryCurrentMonth.compareTo(BigDecimal.ZERO) > 0) {
                pct = amount.divide(totalSalaryCurrentMonth, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                pct = Math.round(pct * 100.0) / 100.0;
            }
            return DepartmentSalaryDTO.builder()
                    .maKhoaHoacBoMon(v.getMaKhoaHoacBoMon())
                    .tenKhoaHoacBoMon(v.getTenKhoaHoacBoMon())
                    .totalSalary(amount)
                    .percentage(pct)
                    .build();
        }).collect(Collectors.toList());

        // 4. Phân bổ chi phí lương theo Bộ môn trong tháng hiện tại
        List<DepartmentSalaryView> boMonViews = bangLuongThangRepository.findSalaryDistributionByBoMon(thang, nam);
        List<DepartmentSalaryDTO> boMonDistributions = boMonViews.stream().map(v -> {
            BigDecimal amount = v.getTongTienLuong() != null ? v.getTongTienLuong() : BigDecimal.ZERO;
            double pct = 0.0;
            if (totalSalaryCurrentMonth.compareTo(BigDecimal.ZERO) > 0) {
                pct = amount.divide(totalSalaryCurrentMonth, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                pct = Math.round(pct * 100.0) / 100.0;
            }
            return DepartmentSalaryDTO.builder()
                    .maKhoaHoacBoMon(v.getMaKhoaHoacBoMon())
                    .tenKhoaHoacBoMon(v.getTenKhoaHoacBoMon())
                    .totalSalary(amount)
                    .percentage(pct)
                    .build();
        }).collect(Collectors.toList());

        // 5. Thống kê KPI: Số GV nhận lương, chi phí tháng trước, tốc độ tăng trưởng
        long totalLecturersPaid = bangLuongThangRepository.countByThangAndNam(thang, nam);

        YearMonth prevYm = currentYm.minusMonths(1);
        String prevKey = prevYm.getYear() + "-" + prevYm.getMonthValue();
        BigDecimal totalSalaryPreviousMonth = trendMap.getOrDefault(prevKey, BigDecimal.ZERO);

        double monthlyGrowthRate = 0.0;
        if (totalSalaryPreviousMonth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalSalaryCurrentMonth.subtract(totalSalaryPreviousMonth);
            monthlyGrowthRate = diff.divide(totalSalaryPreviousMonth, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            monthlyGrowthRate = Math.round(monthlyGrowthRate * 100.0) / 100.0;
        }

        return SalaryStatsResponseDTO.builder()
                .monthlyTrends(monthlyTrends)
                .departmentDistributions(departmentDistributions)
                .boMonDistributions(boMonDistributions)
                .currentMonth(thang)
                .currentYear(nam)
                .totalSalaryCurrentMonth(totalSalaryCurrentMonth)
                .totalSalaryPreviousMonth(totalSalaryPreviousMonth)
                .monthlyGrowthRate(monthlyGrowthRate)
                .totalLecturersPaid(totalLecturersPaid)
                .build();
    }
}
