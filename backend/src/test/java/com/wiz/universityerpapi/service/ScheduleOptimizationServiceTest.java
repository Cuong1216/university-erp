package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ScheduleOptimizationRequestDTO;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;
import com.wiz.universityerpapi.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleOptimizationServiceTest {

    private ScheduleOptimizationService scheduleOptimizationService;

    @BeforeEach
    void setUp() {
        scheduleOptimizationService = new ScheduleOptimizationService(
                new com.wiz.universityerpapi.service.schedule.CpSatSchedulerEngine(),
                new com.wiz.universityerpapi.service.schedule.GreedySchedulerEngine()
        );
    }

    @Test
    void optimizeSchedule_returnsNoData_whenEmptyInput() {
        // Arrange
        ScheduleOptimizationRequestDTO request = new ScheduleOptimizationRequestDTO();
        request.setClassesToSchedule(new ArrayList<>());

        // Act
        ScheduleOptimizationResponseDTO response = scheduleOptimizationService.optimizeSchedule(request);

        // Assert
        assertEquals("NO_DATA", response.getStatus());
        assertEquals(0, response.getBatchCount());
        assertEquals(0, response.getTotalClassesScheduled());
    }

    @Test
    void optimizeSchedule_throwsException_whenOver1000Classes() {
        // Arrange
        ScheduleOptimizationRequestDTO request = new ScheduleOptimizationRequestDTO();
        List<ClassRequirementDTO> classes = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            classes.add(new ClassRequirementDTO());
        }
        request.setClassesToSchedule(classes);

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> scheduleOptimizationService.optimizeSchedule(request));
    }

    @Test
    void optimizeSchedule_returnsSingleBatch_whenUnder50Classes() {
        // Arrange
        ScheduleOptimizationRequestDTO request = new ScheduleOptimizationRequestDTO();
        List<ClassRequirementDTO> classes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ClassRequirementDTO classReq = new ClassRequirementDTO();
            classReq.setMaLopHp("MH" + i);
            classReq.setTenMon("Mon Hoc " + i);
            classReq.setMaGv("GV001");
            classReq.setTenGiangVien("Giang Vien 1");
            classReq.setSoTiet(3);
            classes.add(classReq);
        }
        request.setClassesToSchedule(classes);

        // Act
        ScheduleOptimizationResponseDTO response = scheduleOptimizationService.optimizeSchedule(request);

        // Assert
        assertEquals(1, response.getBatchCount());
        assertEquals(10, response.getTotalClassesScheduled());
        assertNotNull(response.getScheduledSlots());
        assertEquals(10, response.getScheduledSlots().size());
        
        // Assert it fallback to Greedy or solved by OR-Tools
        assertTrue(response.getStatus().equals("GREEDY_FALLBACK") || 
                   response.getStatus().equals("OPTIMAL") || 
                   response.getStatus().equals("FEASIBLE"));
    }
}
