package com.wiz.universityerpapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.dto.ChotLuongRequestDTO;
import com.wiz.universityerpapi.dto.ChotLuongResponseDTO;
import com.wiz.universityerpapi.dto.MySalaryResponseDTO;
import com.wiz.universityerpapi.entity.BangLuongThang;
import com.wiz.universityerpapi.entity.CauHinhLuong;
import com.wiz.universityerpapi.entity.NhatKyGiangDay;
import com.wiz.universityerpapi.exception.BusinessRuleViolationException;
import com.wiz.universityerpapi.exception.ConflictException;
import com.wiz.universityerpapi.exception.ResourceNotFoundException;
import com.wiz.universityerpapi.repository.BangLuongThangRepository;
import com.wiz.universityerpapi.repository.CauHinhLuongRepository;
import com.wiz.universityerpapi.repository.NhatKyGiangDayRepository;
import com.wiz.universityerpapi.repository.UserRepository;
import com.wiz.universityerpapi.repository.projection.GiangVienHeSoView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.wiz.universityerpapi.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LuongService {

    private final CauHinhLuongRepository cauHinhLuongRepository;
    private final NhatKyGiangDayRepository nhatKyGiangDayRepository;
    private final BangLuongThangRepository bangLuongThangRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChotLuongResponseDTO chotLuongThang(ChotLuongRequestDTO request, CustomUserDetails currentUser) {
        String maGvToProcess = request.getMaGv();
        boolean isAdminOrGiaoVu = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_GIAO_VU"));
        if (!isAdminOrGiaoVu) {
            if (currentUser.getMaGv() == null || !currentUser.getMaGv().equals(maGvToProcess)) {
                throw new AccessDeniedException("Giảng viên không được chốt lương thay người khác");
            }
        }

        String maGv = request.getMaGv();
        int thang = request.getThang();
        int nam = request.getNam();
        // Kiểm tra chống tính trùng toàn bộ bảng lương trong tháng/năm cho giảng viên
        if (bangLuongThangRepository.existsByMaGvAndThangAndNam(maGv, thang, nam)) {
            throw new ConflictException(String.format("Bảng lương tháng %d/%d của giảng viên %s đã được chốt trước đó", thang, nam, maGv));
        }

        // 1. Lấy cấu hình lương (lương cơ bản, đơn giá) đang Active từ bảng CAU_HINH_LUONG
        CauHinhLuong cauHinh = cauHinhLuongRepository.findFirstByTrangThaiOrderByIdDesc("ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình lương đang ACTIVE trong hệ thống"));

        // 2. Lấy tổng số tiết thực tế của Giảng viên trong tháng/năm đó từ bảng NHAT_KY_GIANG_DAY mà trang_thai_thanh_toan = false
        LocalDate tuNgay = LocalDate.of(nam, thang, 1);
        LocalDate denNgay = tuNgay.withDayOfMonth(tuNgay.lengthOfMonth());
        List<NhatKyGiangDay> unpaidLogs = nhatKyGiangDayRepository.findUnpaidLogsByGvAndDateRange(maGv, tuNgay, denNgay);
        if (unpaidLogs.isEmpty()) {
            throw new BusinessRuleViolationException(String.format("Giảng viên %s không có tiết dạy nào chưa thanh toán trong tháng %d/%d", maGv, thang, nam));
        }

        int tongSoTietThucTe = unpaidLogs.stream()
                .mapToInt(NhatKyGiangDay::getSoTietThucTe)
                .sum();

        // 3. Tạo bản ghi mới trong bảng BANG_LUONG_THANG. Lưu cứng (Snapshot) các giá trị
        GiangVienHeSoView heSoView = userRepository.findHeSoByMaGv(maGv)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin hệ số chức danh/học vị cho giảng viên: " + maGv));
        BigDecimal heSoCdSnapshot = heSoView.getHeSoCd() != null ? heSoView.getHeSoCd() : new BigDecimal("1.00");
        BigDecimal heSoHvSnapshot = heSoView.getHeSoHv() != null ? heSoView.getHeSoHv() : new BigDecimal("1.00");
        BigDecimal donGiaTietSnapshot = cauHinh.getDonGiaTietChuan();
        BigDecimal luongCoBanSnapshot = cauHinh.getLuongCoBan();

        BigDecimal tienGiangDay = BigDecimal.valueOf(tongSoTietThucTe)
                .multiply(donGiaTietSnapshot)
                .multiply(heSoCdSnapshot)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tongTienLuong = luongCoBanSnapshot.add(tienGiangDay).setScale(2, RoundingMode.HALF_UP);

        String maBangLuong = String.format("BL-%s-%02d%d-%s", maGv, thang, nam, UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        // 4. Lưu công thức tính lương vào cột chi_tiet_tinh_luong_json bằng Jackson/ObjectMapper
        Map<String, Object> chiTietMap = new LinkedHashMap<>();
        chiTietMap.put("maGv", maGv);
        chiTietMap.put("thang", thang);
        chiTietMap.put("nam", nam);
        chiTietMap.put("luongCoBan", luongCoBanSnapshot);
        chiTietMap.put("donGiaTietChuan", donGiaTietSnapshot);
        chiTietMap.put("heSoChucDanh", heSoCdSnapshot);
        chiTietMap.put("heSoHocVi", heSoHvSnapshot);
        chiTietMap.put("tongSoTietThucTe", tongSoTietThucTe);
        chiTietMap.put("tienGiangDay", tienGiangDay);
        chiTietMap.put("tongTienLuong", tongTienLuong);
        chiTietMap.put("congThuc", "TongTienLuong = LuongCoBan + (TongSoTietThucTe * DonGiaTietChuan * HeSoChucDanh)");

        List<Map<String, Object>> chiTietNhatKy = unpaidLogs.stream().map(nk -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("maNhatKy", nk.getMaNhatKy());
            item.put("maLich", nk.getMaLich());
            item.put("ngayDayThucTe", nk.getNgayDayThucTe().toString());
            item.put("soTietThucTe", nk.getSoTietThucTe());
            return item;
        }).collect(Collectors.toList());
        chiTietMap.put("danhSachNhatKyGiangDay", chiTietNhatKy);

        String chiTietJsonString;
        try {
            chiTietJsonString = objectMapper.writeValueAsString(chiTietMap);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi chuyển đổi chi tiết tính lương sang JSONB", e);
            throw new RuntimeException("Lỗi xử lý JSONB chi tiết lương: " + e.getMessage(), e);
        }

        BangLuongThang bangLuong = BangLuongThang.builder()
                .maBangLuong(maBangLuong)
                .maGv(maGv)
                .thang(thang)
                .nam(nam)
                .tongSoTietThucTe(tongSoTietThucTe)
                .heSoCdSnapshot(heSoCdSnapshot)
                .heSoHvSnapshot(heSoHvSnapshot)
                .luongCoBanSnapshot(luongCoBanSnapshot)
                .donGiaTietSnapshot(donGiaTietSnapshot)
                .tongTienLuong(tongTienLuong)
                .chiTietTinhLuongJson(chiTietJsonString)
                .build();

        BangLuongThang savedBangLuong = bangLuongThangRepository.save(bangLuong);

        // 5. Bulk Update các bản ghi trong NHAT_KY_GIANG_DAY sang trang_thai_thanh_toan = true và gắn ma_bang_luong
        List<String> maNhatKyList = unpaidLogs.stream()
                .map(NhatKyGiangDay::getMaNhatKy)
                .collect(Collectors.toList());
        nhatKyGiangDayRepository.markAsPaid(maBangLuong, maNhatKyList);

        return ChotLuongResponseDTO.builder()
                .maBangLuong(savedBangLuong.getMaBangLuong())
                .maGv(savedBangLuong.getMaGv())
                .thang(savedBangLuong.getThang())
                .nam(savedBangLuong.getNam())
                .tongSoTietThucTe(savedBangLuong.getTongSoTietThucTe())
                .heSoCdSnapshot(savedBangLuong.getHeSoCdSnapshot())
                .donGiaTietSnapshot(savedBangLuong.getDonGiaTietSnapshot())
                .tongTienLuong(savedBangLuong.getTongTienLuong())
                .chiTietTinhLuongJson(chiTietMap)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MySalaryResponseDTO> getMySalaryHistory(CustomUserDetails currentUser, Integer thang, Integer nam) {
        String maGv = currentUser.getMaGv();
        if (maGv == null || maGv.isBlank()) {
            throw new BusinessRuleViolationException("Tài khoản hiện tại không được liên kết với mã giảng viên nào");
        }

        List<BangLuongThang> bangLuongList;
        if (thang != null && nam != null) {
            bangLuongList = bangLuongThangRepository.findByMaGvAndThangAndNam(maGv, thang, nam)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            bangLuongList = bangLuongThangRepository.findByMaGvOrderByNamDescThangDesc(maGv);
        }

        return bangLuongList.stream().map(bl -> MySalaryResponseDTO.builder()
                .maBangLuong(bl.getMaBangLuong())
                .maGv(bl.getMaGv())
                .thang(bl.getThang())
                .nam(bl.getNam())
                .tongSoTietThucTe(bl.getTongSoTietThucTe())
                .heSoCdSnapshot(bl.getHeSoCdSnapshot())
                .heSoHvSnapshot(bl.getHeSoHvSnapshot())
                .luongCoBanSnapshot(bl.getLuongCoBanSnapshot())
                .donGiaTietSnapshot(bl.getDonGiaTietSnapshot())
                .tongTienLuong(bl.getTongTienLuong())
                .trangThai(bl.getTrangThai())
                .ngayChotLuong(bl.getNgayChotLuong())
                .build()).collect(Collectors.toList());
    }
}
