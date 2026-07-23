package com.wiz.universityerpapi.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ScheduleOptimizationDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleOptimizationRequestDTO {
        private List<ClassRequirementDTO> classesToSchedule;
        private List<String> availableRooms;
        private List<Integer> availableDays; // 2 -> 7 (Thứ 2 đến Thứ 7)
        private List<Integer> startPeriods;  // Ví dụ: [1, 4, 7, 10] (ca 3 tiết)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassRequirementDTO {
        private String maLopHp;
        private String tenMon;
        private String maGv;
        private String tenGiangVien;
        private Integer soTiet; // Mặc định 3 tiết/buổi
        private List<Integer> danhSachTuan;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleOptimizationResponseDTO {
        private String status; // OPTIMAL, FEASIBLE, GREEDY_FALLBACK, INFEASIBLE
        private String solverEngine;
        private double solveTimeSeconds;
        private int totalClassesScheduled;
        private List<ScheduledSlotDTO> scheduledSlots;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduledSlotDTO {
        private String maLopHp;
        private String tenMon;
        private String maGv;
        private String tenGiangVien;
        private String phongHoc;
        private Integer thuTrongTuan; // 2 -> 7
        private Integer tietBatDau;
        private Integer tietKetThuc;
        private List<Integer> danhSachTuan;
    }
}
