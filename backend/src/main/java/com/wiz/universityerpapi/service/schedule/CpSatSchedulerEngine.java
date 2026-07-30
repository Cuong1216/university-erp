package com.wiz.universityerpapi.service.schedule;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ClassRequirementDTO;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.ScheduledSlotDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CpSatSchedulerEngine implements ISchedulerEngine {

    @Override
    public ScheduleOptimizationResponseDTO solve(
            List<ClassRequirementDTO> classes,
            List<String> rooms,
            List<Integer> days,
            List<Integer> startPeriods,
            Set<String> usedRoomSlots,
            Set<String> usedTeacherSlots) {
        
        long startTime = System.currentTimeMillis();
        log.info("Nạp thư viện gốc (Native libraries) Google OR-Tools...");
        Loader.loadNativeLibraries();

        CpModel model = new CpModel();

        int nClasses = classes.size();
        int nRooms = rooms.size();
        int nDays = days.size();
        int nPeriods = startPeriods.size();

        // 1. Khai báo biến quyết định: x[i][r][d][p] = 1 nếu lớp i xếp vào phòng r, ngày d, ca p
        BoolVar[][][][] x = new BoolVar[nClasses][nRooms][nDays][nPeriods];
        for (int i = 0; i < nClasses; i++) {
            for (int r = 0; r < nRooms; r++) {
                for (int d = 0; d < nDays; d++) {
                    for (int p = 0; p < nPeriods; p++) {
                        x[i][r][d][p] = model.newBoolVar(String.format("x_%d_%d_%d_%d", i, r, d, p));
                    }
                }
            }
        }

        // 2. Ràng buộc cứng 1 (Completeness): Mỗi lớp phải được xếp vào chính xác 1 slot (phòng, thứ, ca)
        for (int i = 0; i < nClasses; i++) {
            List<Literal> classSlots = new ArrayList<>();
            for (int r = 0; r < nRooms; r++) {
                for (int d = 0; d < nDays; d++) {
                    for (int p = 0; p < nPeriods; p++) {
                        classSlots.add(x[i][r][d][p]);
                    }
                }
            }
            model.addExactlyOne(classSlots.toArray(new Literal[0]));
        }

        // 3. Ràng buộc cứng 2 (Room capacity): Không trùng phòng
        for (int r = 0; r < nRooms; r++) {
            for (int d = 0; d < nDays; d++) {
                for (int p = 0; p < nPeriods; p++) {
                    List<Literal> roomSlots = new ArrayList<>();
                    for (int i = 0; i < nClasses; i++) {
                        roomSlots.add(x[i][r][d][p]);
                    }
                    model.addAtMostOne(roomSlots.toArray(new Literal[0]));
                }
            }
        }

        // 4. Ràng buộc cứng 3 (Teacher availability): Không trùng giảng viên
        Map<String, List<Integer>> teacherClassMap = new HashMap<>();
        for (int i = 0; i < nClasses; i++) {
            String maGv = classes.get(i).getMaGv();
            if (maGv != null && !maGv.trim().isEmpty()) {
                teacherClassMap.computeIfAbsent(maGv.trim(), k -> new ArrayList<>()).add(i);
            }
        }

        for (Map.Entry<String, List<Integer>> entry : teacherClassMap.entrySet()) {
            List<Integer> teacherClasses = entry.getValue();
            if (teacherClasses.size() > 1) {
                for (int d = 0; d < nDays; d++) {
                    for (int p = 0; p < nPeriods; p++) {
                        List<Literal> teacherSlots = new ArrayList<>();
                        for (int iClass : teacherClasses) {
                            for (int r = 0; r < nRooms; r++) {
                                teacherSlots.add(x[iClass][r][d][p]);
                            }
                        }
                        model.addAtMostOne(teacherSlots.toArray(new Literal[0]));
                    }
                }
            }
        }

        // 5. Hàm mục tiêu (Soft constraint): Giảm thiểu xếp vào ca tối (p ứng với tiết >= 10 có penalty = 3)
        LinearExprBuilder objective = LinearExpr.newBuilder();
        for (int i = 0; i < nClasses; i++) {
            for (int r = 0; r < nRooms; r++) {
                for (int d = 0; d < nDays; d++) {
                    for (int p = 0; p < nPeriods; p++) {
                        int startP = startPeriods.get(p);
                        int penalty = (startP >= 10) ? 3 : (startP == 7 ? 1 : 0);
                        if (penalty > 0) {
                            objective.addTerm(x[i][r][d][p], penalty);
                        }
                    }
                }
            }
        }
        model.minimize(objective);

        // 6. Prevent using already used slots from previous batches
        for (int i = 0; i < nClasses; i++) {
            for (int r = 0; r < nRooms; r++) {
                for (int d = 0; d < nDays; d++) {
                    for (int p = 0; p < nPeriods; p++) {
                        String roomKey = rooms.get(r) + "_" + days.get(d) + "_" + startPeriods.get(p);
                        String maGv = classes.get(i).getMaGv();
                        String teacherKey = (maGv != null && !maGv.trim().isEmpty()) ? maGv.trim() + "_" + days.get(d) + "_" + startPeriods.get(p) : "";
                        
                        if (usedRoomSlots.contains(roomKey) || (!teacherKey.isEmpty() && usedTeacherSlots.contains(teacherKey))) {
                            model.addEquality(x[i][r][d][p], 0);
                        }
                    }
                }
            }
        }

        // 7. Thực thi Solver
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(5.0); // Giới hạn tối đa 5s giải CSP
        CpSolverStatus status = solver.solve(model);

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Google OR-Tools hoàn tất giải CSP sau {} giây. Trạng thái: {}", duration, status);

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            List<ScheduledSlotDTO> result = new ArrayList<>();
            for (int i = 0; i < nClasses; i++) {
                for (int r = 0; r < nRooms; r++) {
                    for (int d = 0; d < nDays; d++) {
                        for (int p = 0; p < nPeriods; p++) {
                            if (solver.booleanValue(x[i][r][d][p])) {
                                ClassRequirementDTO c = classes.get(i);
                                int startTiet = startPeriods.get(p);
                                int soTiet = c.getSoTiet() != null && c.getSoTiet() > 0 ? c.getSoTiet() : 3;
                                result.add(ScheduledSlotDTO.builder()
                                        .maLopHp(c.getMaLopHp())
                                        .tenMon(c.getTenMon())
                                        .maGv(c.getMaGv())
                                        .tenGiangVien(c.getTenGiangVien())
                                        .phongHoc(rooms.get(r))
                                        .thuTrongTuan(days.get(d))
                                        .tietBatDau(startTiet)
                                        .tietKetThuc(startTiet + soTiet - 1)
                                        .danhSachTuan(c.getDanhSachTuan() != null ? c.getDanhSachTuan() : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                                        .build());
                            }
                        }
                    }
                }
            }

            return ScheduleOptimizationResponseDTO.builder()
                    .status(status.name())
                    .solverEngine("Google OR-Tools CP-SAT Solver (Exact Constraint Satisfaction)")
                    .solveTimeSeconds(duration)
                    .totalClassesScheduled(result.size())
                    .scheduledSlots(result)
                    .message(status == CpSolverStatus.OPTIMAL 
                            ? "Tối ưu hóa thành công tuyệt đối! Đã tìm thấy thời gian biểu tối ưu không xung đột."
                            : "Đã xếp lịch thành công (phương án hợp lệ không xung đột).")
                    .build();
        } else {
            return null; // Orchestrator will handle null as failure and use fallback
        }
    }
}
