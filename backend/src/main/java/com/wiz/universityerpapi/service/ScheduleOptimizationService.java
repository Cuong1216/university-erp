package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.*;
import com.wiz.universityerpapi.exception.BusinessRuleViolationException;
import com.wiz.universityerpapi.service.schedule.CpSatSchedulerEngine;
import com.wiz.universityerpapi.service.schedule.GreedySchedulerEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleOptimizationService {

    private static final int BATCH_SIZE = 50; // Tối đa 50 lớp mỗi batch

    private final CpSatSchedulerEngine cpSatSchedulerEngine;
    private final GreedySchedulerEngine greedySchedulerEngine;

    /**
     * Tự động tối ưu hóa và xếp lịch cho danh sách học phần bằng Google OR-Tools Constraint Solver (CpModel).
     * Giải quyết 3 ràng buộc cứng (Hard constraints):
     * 1. Mỗi lớp học phần phải được xếp đúng 1 phòng, 1 thứ trong tuần, 1 ca học.
     * 2. Không trùng phòng: Tại mỗi phòng, mỗi ngày, mỗi ca chỉ tối đa 1 lớp.
     * 3. Không trùng giảng viên: Tại mỗi ngày, mỗi ca, một giảng viên chỉ dạy tối đa 1 lớp.
     * Và ràng buộc mềm (Soft constraint): Giảm thiểu số tiết dạy vào ca tối (Tiết 10-12).
     */
    public ScheduleOptimizationResponseDTO optimizeSchedule(ScheduleOptimizationRequestDTO request) {
        List<ClassRequirementDTO> classes = request.getClassesToSchedule();
        if (classes == null || classes.isEmpty()) {
            return ScheduleOptimizationResponseDTO.builder()
                    .status("NO_DATA")
                    .solverEngine("OR-Tools SAT Solver")
                    .solveTimeSeconds(0.0)
                    .batchCount(0)
                    .totalClassesScheduled(0)
                    .scheduledSlots(Collections.emptyList())
                    .message("Không có lớp học phần nào cần xếp lịch.")
                    .build();
        }

        if (classes.size() > 1000) {
            throw new BusinessRuleViolationException(
                "Số lớp tối đa mỗi lần xếp lịch là 1000. Vui lòng chia nhỏ yêu cầu.");
        }

        if (classes.size() <= BATCH_SIZE) {
            ScheduleOptimizationResponseDTO response = optimizeSingleBatch(request, new HashSet<>(), new HashSet<>());
            response.setBatchCount(1);
            return response;
        }

        return optimizeInBatches(classes, request);
    }

    private ScheduleOptimizationResponseDTO optimizeInBatches(List<ClassRequirementDTO> classes, ScheduleOptimizationRequestDTO request) {
        long startTime = System.currentTimeMillis();
        int numBatches = (int) Math.ceil((double) classes.size() / BATCH_SIZE);
        log.info("Batched scheduling: {} classes → {} batches of size {}", classes.size(), numBatches, BATCH_SIZE);

        Set<String> usedRoomSlots = new HashSet<>();
        Set<String> usedTeacherSlots = new HashSet<>();
        List<ScheduledSlotDTO> allScheduledSlots = new ArrayList<>();
        int totalScheduled = 0;
        String lastStatus = "OPTIMAL";

        for (int i = 0; i < numBatches; i++) {
            int start = i * BATCH_SIZE;
            int end = Math.min(start + BATCH_SIZE, classes.size());
            List<ClassRequirementDTO> batchClasses = classes.subList(start, end);

            ScheduleOptimizationRequestDTO batchRequest = ScheduleOptimizationRequestDTO.builder()
                    .classesToSchedule(batchClasses)
                    .availableRooms(request.getAvailableRooms())
                    .availableDays(request.getAvailableDays())
                    .startPeriods(request.getStartPeriods())
                    .build();

            ScheduleOptimizationResponseDTO batchResponse = optimizeSingleBatch(batchRequest, usedRoomSlots, usedTeacherSlots);
            
            if (batchResponse.getScheduledSlots() != null) {
                allScheduledSlots.addAll(batchResponse.getScheduledSlots());
                totalScheduled += batchResponse.getTotalClassesScheduled();
                
                for (ScheduledSlotDTO slot : batchResponse.getScheduledSlots()) {
                    usedRoomSlots.add(slot.getPhongHoc() + "_" + slot.getThuTrongTuan() + "_" + slot.getTietBatDau());
                    if (slot.getMaGv() != null && !slot.getMaGv().trim().isEmpty()) {
                        usedTeacherSlots.add(slot.getMaGv().trim() + "_" + slot.getThuTrongTuan() + "_" + slot.getTietBatDau());
                    }
                }
            }

            if ("GREEDY_FALLBACK".equals(batchResponse.getStatus())) {
                 lastStatus = "GREEDY_FALLBACK";
            } else if ("FEASIBLE".equals(batchResponse.getStatus()) && !"GREEDY_FALLBACK".equals(lastStatus)) {
                 lastStatus = "FEASIBLE";
            }
        }

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        return ScheduleOptimizationResponseDTO.builder()
                .status(lastStatus)
                .solverEngine("Batched OR-Tools / Greedy")
                .solveTimeSeconds(duration)
                .totalClassesScheduled(totalScheduled)
                .scheduledSlots(allScheduledSlots)
                .batchCount(numBatches)
                .message("Đã xếp lịch " + totalScheduled + " lớp trong " + numBatches + " batch")
                .build();
    }

    private ScheduleOptimizationResponseDTO optimizeSingleBatch(ScheduleOptimizationRequestDTO request, Set<String> usedRoomSlots, Set<String> usedTeacherSlots) {
        log.info("Bắt đầu xử lý tự động xếp lịch AI cho {} lớp học phần...", 
                request.getClassesToSchedule() != null ? request.getClassesToSchedule().size() : 0);

        List<ClassRequirementDTO> classes = request.getClassesToSchedule();

        List<String> rooms = request.getAvailableRooms() != null && !request.getAvailableRooms().isEmpty()
                ? request.getAvailableRooms()
                : Arrays.asList("A1-101", "A1-102", "A1-201", "B2-101", "B2-202", "C3-301", "C3-302", "LAB-CNTT1", "LAB-CNTT2");

        List<Integer> days = request.getAvailableDays() != null && !request.getAvailableDays().isEmpty()
                ? request.getAvailableDays()
                : Arrays.asList(2, 3, 4, 5, 6, 7); // Thứ 2 đến Thứ 7

        List<Integer> startPeriods = request.getStartPeriods() != null && !request.getStartPeriods().isEmpty()
                ? request.getStartPeriods()
                : Arrays.asList(1, 4, 7, 10); // Ca 1 (1-3), Ca 2 (4-6), Ca 3 (7-9), Ca 4 (10-12)

        try {
            ScheduleOptimizationResponseDTO response = cpSatSchedulerEngine.solve(classes, rooms, days, startPeriods, usedRoomSlots, usedTeacherSlots);
            if (response != null) {
                return response;
            } else {
                log.warn("OR-Tools không tìm thấy giải pháp khả thi với các ràng buộc cứng. Chuyển sang Greedy Heuristic.");
            }
        } catch (Exception e) {
            log.warn("Lỗi chạy mô hình CSP: {}. Chuyển sang Greedy Heuristic Solver.", e.getMessage());
        }

        // Fallback Heuristic Engine
        return greedySchedulerEngine.solve(classes, rooms, days, startPeriods, usedRoomSlots, usedTeacherSlots);
    }
}
