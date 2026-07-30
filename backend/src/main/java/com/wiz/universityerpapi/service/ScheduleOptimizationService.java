package com.wiz.universityerpapi.service;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import com.wiz.universityerpapi.exception.BusinessRuleViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleOptimizationService {

    private static final int BATCH_SIZE = 50; // Tối đa 50 lớp mỗi batch

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
        long startTime = System.currentTimeMillis();
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
            // Gom các lớp cùng một giảng viên
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
                log.warn("OR-Tools không tìm thấy giải pháp khả thi với các ràng buộc cứng. Chuyển sang Greedy Heuristic.");
            }
        } catch (Exception e) {
            log.warn("Thư viện native Google OR-Tools không khả dụng hoặc lỗi chạy mô hình CSP: {}. Chuyển sang Greedy Heuristic Solver.", e.getMessage());
        }


        // Fallback Heuristic Engine: Xếp lịch tham lam đảm bảo không trùng phòng và không trùng GV
        return greedyHeuristicSolver(classes, rooms, days, startPeriods, startTime, usedRoomSlots, usedTeacherSlots);
    }

    private ScheduleOptimizationResponseDTO greedyHeuristicSolver(List<ClassRequirementDTO> classes, List<String> rooms,
                                                                  List<Integer> days, List<Integer> startPeriods, long startTime,
                                                                  Set<String> usedRoomSlots, Set<String> usedTeacherSlots) {
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
