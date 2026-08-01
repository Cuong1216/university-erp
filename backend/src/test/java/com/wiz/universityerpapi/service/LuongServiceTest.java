package com.wiz.universityerpapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.payroll.application.dto.ChotLuongRequestDTO;
import com.wiz.universityerpapi.payroll.application.dto.ChotLuongResponseDTO;
import com.wiz.universityerpapi.payroll.application.dto.MySalaryResponseDTO;
import com.wiz.universityerpapi.payroll.infrastructure.entity.BangLuongThang;
import com.wiz.universityerpapi.payroll.infrastructure.entity.CauHinhLuong;
import com.wiz.universityerpapi.schedule.application.dto.GiangVienHeSoDTO;
import com.wiz.universityerpapi.schedule.application.dto.UnpaidLogDTO;
import com.wiz.universityerpapi.schedule.application.facade.ScheduleFacade;
import com.wiz.universityerpapi.payroll.application.service.LuongService;
import com.wiz.universityerpapi.payroll.application.service.CauHinhLuongService;
import com.wiz.universityerpapi.payroll.infrastructure.repository.BangLuongThangRepository;
import com.wiz.universityerpapi.repository.UserRepository;
import com.wiz.universityerpapi.core.security.CustomUserDetails;
import com.wiz.universityerpapi.core.exception.BusinessRuleViolationException;
import com.wiz.universityerpapi.core.exception.ConflictException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LuongServiceTest {

    @Mock
    private CauHinhLuongService cauHinhLuongService;
    @Mock
    private ScheduleFacade scheduleFacade;
    @Mock
    private BangLuongThangRepository bangLuongThangRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private IDashboardService dashboardService;

    @InjectMocks
    private LuongService luongService;

    @Test
    void chotLuongThang_success() throws Exception {
        // Arrange
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        lenient().when(currentUser.getUsername()).thenReturn("admin");
        lenient().when(currentUser.getMaGv()).thenReturn("GV001");
        lenient().when(currentUser.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(false);

        CauHinhLuong cauHinh = new CauHinhLuong();
        cauHinh.setLuongCoBan(new BigDecimal("5000000"));
        cauHinh.setDonGiaTietChuan(new BigDecimal("100000"));
        when(cauHinhLuongService.getActiveCauHinh()).thenReturn(cauHinh);

        UnpaidLogDTO nk = new UnpaidLogDTO();
        nk.setSoTietThucTe(10);
        nk.setNgayDayThucTe(LocalDate.of(2023, 5, 10));
        when(scheduleFacade.getUnpaidLogs(eq("GV001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(nk));

        GiangVienHeSoDTO heSoView = mock(GiangVienHeSoDTO.class);
        when(heSoView.getHeSoCd()).thenReturn(new BigDecimal("1.2"));
        when(heSoView.getHeSoHv()).thenReturn(new BigDecimal("1.1"));
        when(scheduleFacade.getHeSoByMaGv("GV001")).thenReturn(heSoView);

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{}");

        BangLuongThang savedBangLuong = new BangLuongThang();
        savedBangLuong.setMaBangLuong("BL-GV001-052023-XYZ");
        savedBangLuong.setMaGv("GV001");
        savedBangLuong.setThang(5);
        savedBangLuong.setNam(2023);
        savedBangLuong.setTongSoTietThucTe(10);
        savedBangLuong.setTongTienLuong(new BigDecimal("6200000.00"));
        when(bangLuongThangRepository.save(any(BangLuongThang.class))).thenReturn(savedBangLuong);

        // Act
        ChotLuongResponseDTO response = luongService.chotLuongThang(request, currentUser);

        // Assert
        assertNotNull(response);
        assertEquals("BL-GV001-052023-XYZ", response.getMaBangLuong());
        assertEquals("GV001", response.getMaGv());
        assertEquals(new BigDecimal("6200000.00"), response.getTongTienLuong());
        verify(bangLuongThangRepository).save(any(BangLuongThang.class));
        verify(scheduleFacade).markAsPaid(anyString(), anyList());
        verify(dashboardService).invalidateDashboardCache();
    }

    @Test
    void chotLuongThang_throwsConflict_whenAlreadyChot() {
        // Arrange
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        lenient().when(currentUser.getUsername()).thenReturn("admin");
        lenient().when(currentUser.getMaGv()).thenReturn("GV001");
        lenient().when(currentUser.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void chotLuongThang_throwsAccessDenied_whenGiangVienChotForOther() {
        // Arrange
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV002"); // Request for GV002
        
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        lenient().when(currentUser.getUsername()).thenReturn("user1");
        lenient().when(currentUser.getMaGv()).thenReturn("GV001");
        lenient().when(currentUser.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_GIANG_VIEN")));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void chotLuongThang_throwsBusinessRule_whenNoUnpaidLogs() {
        // Arrange
        ChotLuongRequestDTO request = new ChotLuongRequestDTO();
        request.setMaGv("GV001");
        request.setThang(5);
        request.setNam(2023);

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        lenient().when(currentUser.getUsername()).thenReturn("admin");
        lenient().when(currentUser.getMaGv()).thenReturn("GV001");
        lenient().when(currentUser.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(bangLuongThangRepository.existsByMaGvAndThangAndNam("GV001", 5, 2023)).thenReturn(false);
        when(cauHinhLuongService.getActiveCauHinh()).thenReturn(new CauHinhLuong());
        when(scheduleFacade.getUnpaidLogs(eq("GV001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList()); // No unpaid logs

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> luongService.chotLuongThang(request, currentUser));
    }

    @Test
    void getMySalaryHistory_returnsEmpty_whenNoData() {
        // Arrange
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        lenient().when(currentUser.getUsername()).thenReturn("user1");
        lenient().when(currentUser.getMaGv()).thenReturn("GV001");
        lenient().when(currentUser.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_GIANG_VIEN")));

        when(bangLuongThangRepository.findByMaGvOrderByNamDescThangDesc("GV001")).thenReturn(Collections.emptyList());

        // Act
        List<MySalaryResponseDTO> response = luongService.getMySalaryHistory(currentUser, null, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getMySalaryHistory_throwsBusinessRule_whenMaGvNull() {
        // Arrange
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        lenient().when(currentUser.getUsername()).thenReturn("user1");
        lenient().when(currentUser.getMaGv()).thenReturn(null);
        lenient().when(currentUser.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> luongService.getMySalaryHistory(currentUser, null, null));
    }
}
