package com.wiz.universityerpapi.service.schedule;

import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;

import java.util.List;
import java.util.Set;

public interface ISchedulerEngine {
    ScheduleOptimizationResponseDTO solve(
        List<ClassRequirementDTO> classes,
        List<String> rooms,
        List<Integer> days,
        List<Integer> startPeriods,
        Set<String> usedRoomSlots,
        Set<String> usedTeacherSlots
    );
}
