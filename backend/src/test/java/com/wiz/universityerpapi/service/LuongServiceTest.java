package com.wiz.universityerpapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.dto.ChotLuongRequestDTO;
import com.wiz.universityerpapi.dto.ChotLuongResponseDTO;
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
import com.wiz.universityerpapi.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LuongServiceTest {

    @Mock
    private CauHinhLuongRepository cauHinhLuongRepository;
    @Mock
    private NhatKyGiangDayRepository nhatKyGiangDayRepository;
    @Mock
    private BangLuongThangRepository bangLuongThangRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private IDashboardService dashboardService;

    @InjectMocks
    private LuongService luongService;

    @Test
    void chotLuong_shouldThrowConflictException_whenAlreadySettledForSameMonth() {
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(true);

        assertThrows(ConflictException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void chotLuong_shouldThrowResourceNotFoundException_whenNoCauHinhLuongActive() {
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(false);
        when(cauHinhLuongRepository.findFirstByTrangThaiOrderByIdDesc("ACTIVE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void chotLuong_shouldThrowBusinessRuleViolation_whenNoUnpaidLogs() {
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(false);
        CauHinhLuong cauHinh = new CauHinhLuong();
        when(cauHinhLuongRepository.findFirstByTrangThaiOrderByIdDesc("ACTIVE")).thenReturn(Optional.of(cauHinh));
        when(nhatKyGiangDayRepository.findUnpaidLogsByGvAndDateRange(eq("GV001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessRuleViolationException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void chotLuong_shouldThrowAccessDenied_whenGiangVienAttemptsToSettleOtherTeacher() {
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV002");
        
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GIANG_VIEN")));
        when(currentUser.getMaGv()).thenReturn("GV001");

        assertThrows(AccessDeniedException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void chotLuong_shouldCalculateSalaryCorrectly_withValidData() throws Exception {
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(false);
        
        CauHinhLuong cauHinh = new CauHinhLuong();
        cauHinh.setLuongCoBan(new BigDecimal("5000000"));
        cauHinh.setDonGiaTietChuan(new BigDecimal("150000"));
        when(cauHinhLuongRepository.findFirstByTrangThaiOrderByIdDesc("ACTIVE")).thenReturn(Optional.of(cauHinh));

        NhatKyGiangDay nk = new NhatKyGiangDay();
        nk.setSoTietThucTe(10);
        nk.setNgayDayThucTe(LocalDate.now());
        when(nhatKyGiangDayRepository.findUnpaidLogsByGvAndDateRange(eq("GV001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(nk));

        GiangVienHeSoView heSoView = mock(GiangVienHeSoView.class);
        when(heSoView.getHeSoCd()).thenReturn(new BigDecimal("1.5"));
        when(heSoView.getHeSoHv()).thenReturn(new BigDecimal("1.0"));
        when(userRepository.findHeSoByMaGv("GV001")).thenReturn(Optional.of(heSoView));

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        when(bangLuongThangRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChotLuongResponseDTO response = luongService.chotLuongThang(request, currentUser);

        // 5000000 + (10 * 150000 * 1.5) = 7250000
        assertEquals(new BigDecimal("7250000.00"), response.getTongTienLuong());
        verify(bangLuongThangRepository, times(1)).save(any());
    }

    @Test
    void getMySalary_shouldThrowBusinessRuleViolation_whenUserHasNoMaGv() {
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getMaGv()).thenReturn(null);

        assertThrows(BusinessRuleViolationException.class, () -> luongService.getMySalaryHistory(currentUser, null, null));
    }
}
