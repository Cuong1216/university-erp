package com.wiz.universityerpapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.config.ActiveMqConfig;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.*;
import com.wiz.universityerpapi.service.ScheduleOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleOptimizationController {

    private final ScheduleOptimizationService scheduleOptimizationService;
    private final JmsTemplate jmsTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/optimize")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU')")
    public ResponseEntity<ScheduleOptimizationJobResponseDTO> optimizeSchedule(@RequestBody ScheduleOptimizationRequestDTO request) {
        log.info("REST POST /api/v1/schedule/optimize - Yêu cầu chạy solver xếp lịch AI (ASYNC)");
        if (request.getClassesToSchedule() == null || request.getClassesToSchedule().isEmpty()) {
            request.setClassesToSchedule(getSampleClassesList());
        }
        
        String requestId = UUID.randomUUID().toString();
        String username = SecurityContextHolder.getContext().getAuthentication() != null ? 
                SecurityContextHolder.getContext().getAuthentication().getName() : "system";

        ScheduleJobDTO jobDto = ScheduleJobDTO.builder()
                .requestId(requestId)
                .requestPayload(request)
                .requestedByUsername(username)
                .build();
                
        jmsTemplate.convertAndSend(ActiveMqConfig.SCHEDULE_OPTIMIZATION_REQUESTS_QUEUE, jobDto);
        
        ScheduleOptimizationJobResponseDTO response = ScheduleOptimizationJobResponseDTO.builder()
                .requestId(requestId)
                .status("PROCESSING")
                .message("Yêu cầu xếp lịch AI đang được xử lý trong nền.")
                .build();
                
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/optimize/status/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU')")
    public ResponseEntity<?> getOptimizationStatus(@PathVariable String requestId) {
        String redisKey = "schedule:result:" + requestId;
        String jsonResult = (String) redisTemplate.opsForValue().get(redisKey);
        
        if (jsonResult != null) {
            try {
                ScheduleOptimizationResponseDTO result = objectMapper.readValue(jsonResult, ScheduleOptimizationResponseDTO.class);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("Lỗi khi parse kết quả xếp lịch từ Redis", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi dữ liệu kết quả.");
            }
        }
        
        ScheduleOptimizationJobResponseDTO response = ScheduleOptimizationJobResponseDTO.builder()
                .requestId(requestId)
                .status("PROCESSING")
                .message("Yêu cầu đang được xử lý...")
                .build();
                
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
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
