package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.SalaryStatsResponseDTO;

/**
 * Interface cho Dashboard Service — tuân thủ nguyên tắc Dependency Inversion (SOLID).
 * Tách biệt contract khỏi implementation, cho phép mock trong unit test.
 */
public interface IDashboardService {

    /**
     * Lấy thống kê tổng hợp lương cho dashboard.
     * Kết quả được cache trong Redis với TTL 5 phút.
     *
     * @param thang Tháng cần thống kê (null = tháng mới nhất có dữ liệu)
     * @param nam   Năm cần thống kê (null = năm mới nhất có dữ liệu)
     */
    SalaryStatsResponseDTO getSalaryStats(Integer thang, Integer nam);

    /**
     * Xóa toàn bộ dashboard cache khi có bảng lương mới được chốt.
     */
    void invalidateDashboardCache();
}
