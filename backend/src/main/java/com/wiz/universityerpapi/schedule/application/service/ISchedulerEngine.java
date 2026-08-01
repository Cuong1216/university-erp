package com.wiz.universityerpapi.schedule.application.service;

import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;

import java.util.List;
import java.util.Set;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;

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
