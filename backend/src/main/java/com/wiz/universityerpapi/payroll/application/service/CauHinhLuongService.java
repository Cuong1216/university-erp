package com.wiz.universityerpapi.payroll.application.service;

import com.wiz.universityerpapi.payroll.infrastructure.entity.CauHinhLuong;
import com.wiz.universityerpapi.payroll.infrastructure.repository.CauHinhLuongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CauHinhLuongService {

    private final CauHinhLuongRepository cauHinhLuongRepository;

    /**
     * Lấy cấu hình lương ACTIVE.
     * Sử dụng @Cacheable để lưu kết quả vào Redis.
     * Nếu có trong cache, sẽ không gọi xuống DB.
     */
    @Cacheable(value = "cau_hinh_luong", key = "'active_config'")
    public CauHinhLuong getActiveCauHinh() {
        log.info("Lấy cấu hình lương ACTIVE từ cơ sở dữ liệu (Cache miss)");
        return cauHinhLuongRepository.findFirstByTrangThaiOrderByIdDesc("ACTIVE")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình lương đang ACTIVE trong hệ thống"));
    }

    /**
     * Thêm mới hoặc cập nhật cấu hình lương.
     * Sử dụng @CacheEvict(..., allEntries = true) để xóa sạch cache cũ,
     * đảm bảo lần lấy tiếp theo sẽ query lại DB để có đơn giá mới nhất.
     */
    @Transactional
    @CacheEvict(value = "cau_hinh_luong", allEntries = true)
    public CauHinhLuong saveCauHinhLuong(CauHinhLuong cauHinhLuong) {
        log.info("Lưu cấu hình lương và xóa cache 'cau_hinh_luong'");
        return cauHinhLuongRepository.save(cauHinhLuong);
    }

    /**
     * Xóa cấu hình lương.
     * Tương tự cũng cần xóa cache.
     */
    @Transactional
    @CacheEvict(value = "cau_hinh_luong", allEntries = true)
    public void deleteCauHinhLuong(Integer id) {
        log.info("Xóa cấu hình lương ID: {} và xóa cache 'cau_hinh_luong'", id);
        cauHinhLuongRepository.deleteById(id);
    }
}
