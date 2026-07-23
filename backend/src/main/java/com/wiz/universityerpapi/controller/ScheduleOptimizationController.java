package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.*;
import com.wiz.universityerpapi.service.ScheduleOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleOptimizationController {

    private final ScheduleOptimizationService scheduleOptimizationService;

    @PostMapping("/optimize")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU')")
    public ResponseEntity<ScheduleOptimizationResponseDTO> optimizeSchedule(@RequestBody ScheduleOptimizationRequestDTO request) {
        log.info("REST POST /api/v1/schedule/optimize - Yêu cầu chạy solver xếp lịch AI");
        if (request.getClassesToSchedule() == null || request.getClassesToSchedule().isEmpty()) {
            request.setClassesToSchedule(getSampleClassesList());
        }
        ScheduleOptimizationResponseDTO result = scheduleOptimizationService.optimizeSchedule(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sample-classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU', 'GIANG_VIEN', 'SINH_VIEN')")
    public ResponseEntity<List<ClassRequirementDTO>> getSampleClasses() {
        return ResponseEntity.ok(getSampleClassesList());
    }

    private List<ClassRequirementDTO> getSampleClassesList() {
        return Arrays.asList(
                ClassRequirementDTO.builder().maLopHp("HP-CNTT01").tenMon("Cấu trúc dữ liệu & Giải thuật").maGv("GV001").tenGiangVien("TS. Nguyễn Văn Hùng").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-CNTT02").tenMon("Cơ sở dữ liệu Nâng cao").maGv("GV001").tenGiangVien("TS. Nguyễn Văn Hùng").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-CNTT03").tenMon("Trí tuệ nhân tạo (AI)").maGv("GV002").tenGiangVien("PGS.TS Lê Thị Mai").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-CNTT04").tenMon("Kiến trúc Máy tính").maGv("GV002").tenGiangVien("PGS.TS Lê Thị Mai").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-CNTT05").tenMon("Mạng Máy tính & Bảo mật").maGv("GV003").tenGiangVien("ThS. Trần Đức Minh").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-KT01").tenMon("Kinh tế Vi mô").maGv("GV004").tenGiangVien("TS. Phạm Hồng Hà").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-KT02").tenMon("Quản trị Tài chính Doanh nghiệp").maGv("GV004").tenGiangVien("TS. Phạm Hồng Hà").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build(),
                ClassRequirementDTO.builder().maLopHp("HP-NN01").tenMon("Tiếng Anh Chuyên ngành CNTT").maGv("GV005").tenGiangVien("ThS. Hoàng Thu Thủy").soTiet(3).danhSachTuan(Arrays.asList(1,2,3,4,5,6,7,8,9,10)).build()
        );
    }
}
