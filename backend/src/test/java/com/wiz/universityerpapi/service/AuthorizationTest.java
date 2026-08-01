package com.wiz.universityerpapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.payroll.application.dto.ChotLuongRequestDTO;
import com.wiz.universityerpapi.payroll.application.dto.ChotLuongResponseDTO;
import com.wiz.universityerpapi.payroll.infrastructure.entity.CauHinhLuong;
import com.wiz.universityerpapi.schedule.application.dto.GiangVienHeSoDTO;
import com.wiz.universityerpapi.schedule.application.dto.UnpaidLogDTO;
import com.wiz.universityerpapi.schedule.application.facade.ScheduleFacade;
import com.wiz.universityerpapi.payroll.application.service.LuongService;
import com.wiz.universityerpapi.payroll.application.service.CauHinhLuongService;
import com.wiz.universityerpapi.payroll.infrastructure.repository.BangLuongThangRepository;
import com.wiz.universityerpapi.repository.UserRepository;
import com.wiz.universityerpapi.core.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizationTest {

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
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private IDashboardService dashboardService;

    @InjectMocks
    private LuongService luongService;

    private ChotLuongRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new ChotLuongRequestDTO();
        request.setMaGv("GV_OTHER");
        request.setThang(5);
        request.setNam(2023);
    }

    private void setupMockForSuccess() throws Exception {
        when(bangLuongThangRepository.existsByMaGvAndThangAndNam(anyString(), anyInt(), anyInt())).thenReturn(false);
        
        CauHinhLuong cauHinh = new CauHinhLuong();
        cauHinh.setLuongCoBan(new BigDecimal("5000000"));
        cauHinh.setDonGiaTietChuan(new BigDecimal("150000"));
        when(cauHinhLuongService.getActiveCauHinh()).thenReturn(cauHinh);

        UnpaidLogDTO nk = new UnpaidLogDTO();
        nk.setSoTietThucTe(10);
        nk.setNgayDayThucTe(LocalDate.now());
        when(scheduleFacade.getUnpaidLogs(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(nk));

        GiangVienHeSoDTO heSoView = mock(GiangVienHeSoDTO.class);
        when(heSoView.getHeSoCd()).thenReturn(new BigDecimal("1.5"));
        when(heSoView.getHeSoHv()).thenReturn(new BigDecimal("1.0"));
        when(scheduleFacade.getHeSoByMaGv(anyString())).thenReturn(heSoView);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        when(bangLuongThangRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void adminCanSettleSalaryForOtherGiangVien() throws Exception {
        setupMockForSuccess();
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        // Do return raw collection to avoid type inference issues with generics
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(currentUser).getAuthorities();

        ChotLuongResponseDTO response = luongService.chotLuongThang(request, currentUser);
        assertNotNull(response);
    }

    @Test
    void giaoVuCanSettleSalaryForOtherGiangVien() throws Exception {
        setupMockForSuccess();
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GIAO_VU"))).when(currentUser).getAuthorities();

        ChotLuongResponseDTO response = luongService.chotLuongThang(request, currentUser);
        assertNotNull(response);
    }

    @Test
    void giangVienCanSettleSalaryForSelf() throws Exception {
        setupMockForSuccess();
        // Sửa request thành chốt cho chính mình
        request.setMaGv("GV_SELF");

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GIANG_VIEN"))).when(currentUser).getAuthorities();
        when(currentUser.getMaGv()).thenReturn("GV_SELF");

        ChotLuongResponseDTO response = luongService.chotLuongThang(request, currentUser);
        assertNotNull(response);
    }

    @Test
    void giangVienCannotSettleSalaryForOtherGiangVien() {
        // Không cần setup các mock bên dưới vì sẽ ném ngoại lệ ngay tại bước kiểm tra phân quyền
        request.setMaGv("GV_OTHER");

        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GIANG_VIEN"))).when(currentUser).getAuthorities();
        when(currentUser.getMaGv()).thenReturn("GV_SELF");

        assertThrows(AccessDeniedException.class, () -> luongService.chotLuongThang(request, currentUser));
    }
}
