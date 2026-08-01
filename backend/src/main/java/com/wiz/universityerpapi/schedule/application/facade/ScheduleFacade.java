package com.wiz.universityerpapi.schedule.application.facade;

import com.wiz.universityerpapi.schedule.application.dto.GiangVienHeSoDTO;
import com.wiz.universityerpapi.schedule.application.dto.UnpaidLogDTO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleFacade {
    
    /**
     * Lấy danh sách nhật ký giảng dạy chưa được thanh toán (chưa chốt lương)
     */
    List<UnpaidLogDTO> getUnpaidLogs(String maGv, LocalDate tuNgay, LocalDate denNgay);

    /**
     * Cập nhật trạng thái đã thanh toán cho các nhật ký giảng dạy
     */
    void markAsPaid(String maBangLuong, List<String> danhSachMaNhatKy);

    /**
     * Lấy hệ số chức danh và học vị của giảng viên
     */
    GiangVienHeSoDTO getHeSoByMaGv(String maGv);
}
