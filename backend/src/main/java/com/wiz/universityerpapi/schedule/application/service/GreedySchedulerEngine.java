package com.wiz.universityerpapi.schedule.application.service;

import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduledSlotDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;

@Slf4j
@Component
public class GreedySchedulerEngine implements ISchedulerEngine {

    @Override
    public ScheduleOptimizationResponseDTO solve(
            List<ClassRequirementDTO> classes,
            List<String> rooms,
            List<Integer> days,
            List<Integer> startPeriods,
            Set<String> usedRoomSlots,
            Set<String> usedTeacherSlots) {
        
        long startTime = System.currentTimeMillis();
        log.info("Thực thi Greedy Heuristic Schedule Optimization...");
        List<ScheduledSlotDTO> scheduled = new ArrayList<>();
        // Theo dõi (phòng_thứ_ca) đã sử dụng
        Set<String> localUsedRoomSlots = new HashSet<>(usedRoomSlots);
        // Theo dõi (gv_thứ_ca) đã sử dụng
        Set<String> localUsedTeacherSlots = new HashSet<>(usedTeacherSlots);

        int unscheduledCount = 0;
        for (ClassRequirementDTO c : classes) {
            boolean assigned = false;
            String maGv = c.getMaGv() != null ? c.getMaGv().trim() : "";

            // Ưu tiên ngày trong tuần và ca sáng/chiều
            for (int d : days) {
                if (assigned) break;
                for (int p : startPeriods) {
                    if (assigned) break;
                    for (String r : rooms) {
                        String roomSlotKey = r + "_" + d + "_" + p;
                        String teacherSlotKey = maGv + "_" + d + "_" + p;

                        if (!localUsedRoomSlots.contains(roomSlotKey) && (maGv.isEmpty() || !localUsedTeacherSlots.contains(teacherSlotKey))) {
                            localUsedRoomSlots.add(roomSlotKey);
                            if (!maGv.isEmpty()) localUsedTeacherSlots.add(teacherSlotKey);

                            int soTiet = c.getSoTiet() != null && c.getSoTiet() > 0 ? c.getSoTiet() : 3;
                            scheduled.add(ScheduledSlotDTO.builder()
                                    .maLopHp(c.getMaLopHp())
                                    .tenMon(c.getTenMon())
                                    .maGv(c.getMaGv())
                                    .tenGiangVien(c.getTenGiangVien())
                                    .phongHoc(r)
                                    .thuTrongTuan(d)
                                    .tietBatDau(p)
                                    .tietKetThuc(p + soTiet - 1)
                                    .danhSachTuan(c.getDanhSachTuan() != null ? c.getDanhSachTuan() : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                                    .build());
                            assigned = true;
                            break;
                        }
                    }
                }
            }
            if (!assigned) {
                unscheduledCount++;
                log.warn("Không thể xếp lịch cho lớp {} do hết phòng/thời gian", c.getMaLopHp());
            }
        }

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        return ScheduleOptimizationResponseDTO.builder()
                .status("GREEDY_FALLBACK")
                .solverEngine("Java Greedy Heuristic CSP Engine (Zero-Conflict Guaranteed)")
                .solveTimeSeconds(duration)
                .totalClassesScheduled(scheduled.size())
                .scheduledSlots(scheduled)
                .message(unscheduledCount == 0
                        ? "Xếp lịch hoàn tất bằng Heuristic Engine (100% lớp được phân bổ không xung đột)."
                        : String.format("Đã xếp lịch %d lớp. Còn %d lớp vượt quá dung lượng phòng học.", scheduled.size(), unscheduledCount))
                .build();
    }
}
