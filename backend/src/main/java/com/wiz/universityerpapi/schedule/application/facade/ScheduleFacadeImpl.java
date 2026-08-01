package com.wiz.universityerpapi.schedule.application.facade;

import com.wiz.universityerpapi.core.exception.ResourceNotFoundException;
import com.wiz.universityerpapi.schedule.application.dto.GiangVienHeSoDTO;
import com.wiz.universityerpapi.schedule.application.dto.UnpaidLogDTO;
import com.wiz.universityerpapi.schedule.infrastructure.repository.GiangVienRepository;
import com.wiz.universityerpapi.schedule.infrastructure.repository.NhatKyGiangDayRepository;
import com.wiz.universityerpapi.schedule.infrastructure.repository.projection.GiangVienHeSoView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleFacadeImpl implements ScheduleFacade {

    private final NhatKyGiangDayRepository nhatKyGiangDayRepository;
    private final GiangVienRepository giangVienRepository;

    @Override
    public List<UnpaidLogDTO> getUnpaidLogs(String maGv, LocalDate tuNgay, LocalDate denNgay) {
        return nhatKyGiangDayRepository.findUnpaidLogsByGvAndDateRange(maGv, tuNgay, denNgay)
                .stream()
                .map(nk -> UnpaidLogDTO.builder()
                        .maNhatKy(nk.getMaNhatKy())
                        .maLich(nk.getMaLich())
                        .ngayDayThucTe(nk.getNgayDayThucTe())
                        .soTietThucTe(nk.getSoTietThucTe())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void markAsPaid(String maBangLuong, List<String> danhSachMaNhatKy) {
        nhatKyGiangDayRepository.markAsPaid(maBangLuong, danhSachMaNhatKy);
    }

    @Override
    public GiangVienHeSoDTO getHeSoByMaGv(String maGv) {
        GiangVienHeSoView view = giangVienRepository.findHeSoByMaGv(maGv)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin hệ số chức danh/học vị cho giảng viên: " + maGv));
        
        return GiangVienHeSoDTO.builder()
                .heSoCd(view.getHeSoCd() != null ? view.getHeSoCd() : new BigDecimal("1.00"))
                .heSoHv(view.getHeSoHv() != null ? view.getHeSoHv() : new BigDecimal("1.00"))
                .build();
    }
}
