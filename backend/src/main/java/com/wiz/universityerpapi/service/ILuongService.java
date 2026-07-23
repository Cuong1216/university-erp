package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.ChotLuongRequestDTO;
import com.wiz.universityerpapi.dto.ChotLuongResponseDTO;
import com.wiz.universityerpapi.dto.MySalaryResponseDTO;
import com.wiz.universityerpapi.security.CustomUserDetails;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface cho Payroll Service — tuân thủ nguyên tắc Dependency Inversion (SOLID).
 * Cho phép dễ dàng mock trong unit test và swap implementation nếu cần.
 */
public interface ILuongService {

    /**
     * Chốt lương đồng bộ — tính toán và lưu bảng lương tháng cho một giảng viên.
     */
    ChotLuongResponseDTO chotLuongThang(ChotLuongRequestDTO request, CustomUserDetails currentUser);

    /**
     * Chốt lương bất đồng bộ — trả về ngay lập tức và push kết quả qua WebSocket.
     */
    CompletableFuture<Void> chotLuongThangAsync(ChotLuongRequestDTO request, CustomUserDetails currentUser);

    /**
     * Lấy lịch sử lương của giảng viên hiện tại.
     */
    List<MySalaryResponseDTO> getMySalaryHistory(CustomUserDetails currentUser, Integer thang, Integer nam);
}
