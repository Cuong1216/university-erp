package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.LichHocResponseDTO;
import com.wiz.universityerpapi.dto.TaoLichRequestDTO;
import com.wiz.universityerpapi.entity.LichHocChiTiet;
import com.wiz.universityerpapi.entity.TuanHocChiTiet;
import com.wiz.universityerpapi.exception.BusinessRuleViolationException;
import com.wiz.universityerpapi.exception.ConflictException;
import com.wiz.universityerpapi.repository.LichHocChiTietRepository;
import com.wiz.universityerpapi.repository.PhanCongDayRepository;
import com.wiz.universityerpapi.repository.projection.LecturerConflictView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LichHocService {

    private final LichHocChiTietRepository lichHocChiTietRepository;
    private final PhanCongDayRepository phanCongDayRepository;

    @Transactional
    public LichHocResponseDTO taoLichHoc(TaoLichRequestDTO request) {
        if (request.getTietBatDau() > request.getTietKetThuc()) {
            throw new BusinessRuleViolationException("Tiết bắt đầu không được lớn hơn tiết kết thúc");
        }

        List<Integer> targetWeeks = new ArrayList<>();
        if (request.getDanhSachTuan() != null && !request.getDanhSachTuan().isEmpty()) {
            targetWeeks.addAll(request.getDanhSachTuan());
        } else if (request.getTuanThu() != null) {
            targetWeeks.add(request.getTuanThu());
        } else {
            throw new BusinessRuleViolationException("Danh sách tuần học hoặc tuần thứ không được để trống");
        }

        // 1. Kiểm tra trùng lịch Phòng học
        // Công thức: (tiet_bat_dau_moi <= lh.tiet_ket_thuc) AND (tiet_ket_thuc_moi >= lh.tiet_bat_dau)
        // trong cùng một thu_trong_tuan và cùng tuan_thu
        List<TuanHocChiTiet> roomConflicts = lichHocChiTietRepository.findConflictingRoomSchedule(
                request.getPhongHoc(),
                request.getThuTrongTuan(),
                request.getTietBatDau(),
                request.getTietKetThuc(),
                targetWeeks
        );

        if (!roomConflicts.isEmpty()) {
            TuanHocChiTiet conflict = roomConflicts.get(0);
            throw new ConflictException(String.format("Phòng %s đã bị vướng lịch tại tuần %d",
                    conflict.getLichHocChiTiet().getPhongHoc(), conflict.getTuanThu()));
        }

        // 2. Kiểm tra trùng lịch Giảng viên
        Set<String> danhSachMaGvSet = Stream.concat(
                Optional.ofNullable(request.getMaGv()).filter(s -> !s.isBlank()).stream(),
                Optional.ofNullable(phanCongDayRepository.findMaGvByMaLopHp(request.getMaLopHp()))
                        .orElse(Collections.emptyList()).stream().filter(Objects::nonNull)
        ).collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> danhSachMaGv = new ArrayList<>(danhSachMaGvSet);

        if (!danhSachMaGv.isEmpty()) {
            List<LecturerConflictView> lecturerConflicts = lichHocChiTietRepository.findConflictingLecturerSchedule(
                    danhSachMaGv,
                    request.getThuTrongTuan(),
                    request.getTietBatDau(),
                    request.getTietKetThuc(),
                    targetWeeks
            );

            if (!lecturerConflicts.isEmpty()) {
                LecturerConflictView conflict = lecturerConflicts.get(0);
                TuanHocChiTiet conflictWeek = conflict.getTuanHocChiTiet();
                String conflictingGv = conflict.getMaGv();
                throw new ConflictException(String.format("Giảng viên %s đã bị vướng lịch tại tuần %d",
                        conflictingGv, conflictWeek.getTuanThu()));
            }
        }

        // 3. Nếu không trùng, tiến hành lưu lịch mới
        String maLich = request.getMaLich();
        if (maLich == null || maLich.trim().isEmpty()) {
            maLich = "LH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        LichHocChiTiet lichHoc = LichHocChiTiet.builder()
                .maLich(maLich)
                .maLopHp(request.getMaLopHp())
                .phongHoc(request.getPhongHoc())
                .thuTrongTuan(request.getThuTrongTuan())
                .tietBatDau(request.getTietBatDau())
                .tietKetThuc(request.getTietKetThuc())
                .build();

        List<TuanHocChiTiet> danhSachTuanHoc = new ArrayList<>();
        for (Integer tuan : targetWeeks) {
            String maTuanHoc = maLich + "-T" + tuan;
            if (maTuanHoc.length() > 20) {
                maTuanHoc = "TH-" + UUID.randomUUID().toString().substring(0, 15).toUpperCase();
            }
            TuanHocChiTiet tuanHoc = TuanHocChiTiet.builder()
                    .maTuanHoc(maTuanHoc)
                    .lichHocChiTiet(lichHoc)
                    .tuanThu(tuan)
                    .build();
            danhSachTuanHoc.add(tuanHoc);
        }
        lichHoc.setDanhSachTuanHoc(danhSachTuanHoc);

        LichHocChiTiet saved = lichHocChiTietRepository.save(lichHoc);

        List<Integer> savedWeeks = saved.getDanhSachTuanHoc().stream()
                .map(TuanHocChiTiet::getTuanThu)
                .collect(Collectors.toList());

        return LichHocResponseDTO.builder()
                .maLich(saved.getMaLich())
                .maLopHp(saved.getMaLopHp())
                .phongHoc(saved.getPhongHoc())
                .thuTrongTuan(saved.getThuTrongTuan())
                .tietBatDau(saved.getTietBatDau())
                .tietKetThuc(saved.getTietKetThuc())
                .danhSachTuan(savedWeeks)
                .build();
    }
}
