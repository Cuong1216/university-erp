package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleOptimizationServiceTest {

    private ScheduleOptimizationService scheduleOptimizationService;
    private Method greedyMethod;

    @BeforeEach
    void setUp() throws Exception {
        scheduleOptimizationService = new ScheduleOptimizationService();
        greedyMethod = ScheduleOptimizationService.class.getDeclaredMethod(
                "greedyHeuristicSolver", List.class, List.class, List.class, List.class, long.class);
        greedyMethod.setAccessible(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void greedyFallback_shouldScheduleAllClasses_withNoConflict() throws Exception {
        List<ClassRequirementDTO> classes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ClassRequirementDTO c = new ClassRequirementDTO();
            c.setMaLopHp("LH" + i);
            c.setMaGv("GV" + i); // Different teachers
            c.setSoTiet(3);
            classes.add(c);
        }
        List<String> rooms = Arrays.asList("R1", "R2", "R3");
        List<Integer> days = Arrays.asList(2, 3, 4, 5, 6);
        List<Integer> periods = Arrays.asList(1, 4, 7);

        ScheduleOptimizationResponseDTO response = (ScheduleOptimizationResponseDTO) greedyMethod.invoke(
                scheduleOptimizationService, classes, rooms, days, periods, System.currentTimeMillis());

        assertEquals(10, response.getTotalClassesScheduled());
        assertEquals(10, response.getScheduledSlots().size());
        assertTrue(response.getMessage().contains("100%"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void greedyFallback_shouldNotScheduleMoreThanRoomCapacity() throws Exception {
        List<ClassRequirementDTO> classes = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // 5 classes
            ClassRequirementDTO c = new ClassRequirementDTO();
            c.setMaLopHp("LH" + i);
            c.setMaGv("GV" + i);
            c.setSoTiet(3);
            classes.add(c);
        }
        List<String> rooms = Arrays.asList("R1"); // 1 room
        List<Integer> days = Arrays.asList(2);    // 1 day
        List<Integer> periods = Arrays.asList(1, 4, 7); // 3 periods -> max 3 classes can be scheduled

        ScheduleOptimizationResponseDTO response = (ScheduleOptimizationResponseDTO) greedyMethod.invoke(
                scheduleOptimizationService, classes, rooms, days, periods, System.currentTimeMillis());

        assertEquals(3, response.getTotalClassesScheduled());
        assertEquals(3, response.getScheduledSlots().size());
        assertTrue(response.getMessage().contains("vượt quá dung lượng"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void greedyFallback_shouldPreventTeacherConflict() throws Exception {
        List<ClassRequirementDTO> classes = new ArrayList<>();
        for (int i = 0; i < 2; i++) { // 2 classes, same teacher
            ClassRequirementDTO c = new ClassRequirementDTO();
            c.setMaLopHp("LH" + i);
            c.setMaGv("GV1"); // SAME teacher
            c.setSoTiet(3);
            classes.add(c);
        }
        List<String> rooms = Arrays.asList("R1", "R2"); 
        List<Integer> days = Arrays.asList(2);    
        List<Integer> periods = Arrays.asList(1); // 1 period

        // Since it's 1 period and same teacher, only 1 class can be scheduled, despite having 2 rooms
        ScheduleOptimizationResponseDTO response = (ScheduleOptimizationResponseDTO) greedyMethod.invoke(
                scheduleOptimizationService, classes, rooms, days, periods, System.currentTimeMillis());

        assertEquals(1, response.getTotalClassesScheduled());
        assertEquals(1, response.getScheduledSlots().size());
    }

    @Test
    void optimizeSchedule_shouldReturnNoData_whenEmptyInput() {
        ScheduleOptimizationRequestDTO request = new ScheduleOptimizationRequestDTO();
        request.setClassesToSchedule(new ArrayList<>());
        
        ScheduleOptimizationResponseDTO response = scheduleOptimizationService.optimizeSchedule(request);
        
        assertEquals("NO_DATA", response.getStatus());
        assertEquals(0, response.getTotalClassesScheduled());
    }
}
