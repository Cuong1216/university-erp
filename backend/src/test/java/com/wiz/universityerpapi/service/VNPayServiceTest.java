package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.config.VNPayConfig;
import com.wiz.universityerpapi.dto.payment.PaymentDTOs.VNPayWebhookResponseDTO;
import com.wiz.universityerpapi.entity.HocPhi;
import com.wiz.universityerpapi.repository.HocPhiRepository;
import com.wiz.universityerpapi.repository.ThanhToanLogRepository;
import com.wiz.universityerpapi.util.VNPayUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VNPayServiceTest {

    @Mock
    private VNPayConfig vnpayConfig;
    @Mock
    private HocPhiRepository hocPhiRepository;
    @Mock
    private ThanhToanLogRepository thanhToanLogRepository;

    @InjectMocks
    private VNPayService vnPayService;
    
    private MockedStatic<VNPayUtil> vnPayUtilMock;

    @BeforeEach
    void setUp() {
        vnPayUtilMock = mockStatic(VNPayUtil.class);
    }

    @AfterEach
    void tearDown() {
        vnPayUtilMock.close();
    }

    @Test
    void processWebhook_shouldReturnInvalidChecksum_whenHashMismatch() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "invalid_hash");
        
        when(vnpayConfig.getVnpHashSecret()).thenReturn("secret");
        vnPayUtilMock.when(() -> VNPayUtil.hashAllFields(any(), any())).thenReturn("valid_hash");

        VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);
        assertEquals("97", response.getRspCode());
    }

    @Test
    void processWebhook_shouldReturnAlreadyConfirmed_whenTxnRefExists() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "hash");
        params.put("vnp_TxnRef", "HP01_12345");
        
        when(vnpayConfig.getVnpHashSecret()).thenReturn("secret");
        vnPayUtilMock.when(() -> VNPayUtil.hashAllFields(any(), any())).thenReturn("hash");
        when(thanhToanLogRepository.existsByVnpTxnRef("HP01_12345")).thenReturn(true);

        VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);
        assertEquals("02", response.getRspCode());
    }

    @Test
    void processWebhook_shouldReturnOrderNotFound_whenMaHocPhiNotExist() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "hash");
        params.put("vnp_TxnRef", "HP01_12345");
        
        when(vnpayConfig.getVnpHashSecret()).thenReturn("secret");
        vnPayUtilMock.when(() -> VNPayUtil.hashAllFields(any(), any())).thenReturn("hash");
        when(thanhToanLogRepository.existsByVnpTxnRef("HP01_12345")).thenReturn(false);
        when(hocPhiRepository.findById("HP01")).thenReturn(Optional.empty());

        VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);
        assertEquals("01", response.getRspCode());
    }

    @Test
    void processWebhook_shouldUpdateHocPhiToFullyPaid_whenPaymentSuccessful() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "hash");
        params.put("vnp_TxnRef", "HP01_12345");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_Amount", "100000000"); // 1,000,000 VND
        
        when(vnpayConfig.getVnpHashSecret()).thenReturn("secret");
        vnPayUtilMock.when(() -> VNPayUtil.hashAllFields(any(), any())).thenReturn("hash");
        when(thanhToanLogRepository.existsByVnpTxnRef("HP01_12345")).thenReturn(false);
        
        HocPhi hocPhi = new HocPhi();
        hocPhi.setSoTienPhaiNop(new BigDecimal("1000000"));
        hocPhi.setSoTienDaNop(BigDecimal.ZERO);
        when(hocPhiRepository.findById("HP01")).thenReturn(Optional.of(hocPhi));

        VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);
        assertEquals("00", response.getRspCode());
        assertEquals("DA_NOP_DU", hocPhi.getTrangThai());
    }

    @Test
    void processWebhook_shouldSetPartialPaid_whenPartialPayment() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "hash");
        params.put("vnp_TxnRef", "HP01_12345");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_Amount", "50000000"); // 500,000 VND
        
        when(vnpayConfig.getVnpHashSecret()).thenReturn("secret");
        vnPayUtilMock.when(() -> VNPayUtil.hashAllFields(any(), any())).thenReturn("hash");
        when(thanhToanLogRepository.existsByVnpTxnRef("HP01_12345")).thenReturn(false);
        
        HocPhi hocPhi = new HocPhi();
        hocPhi.setSoTienPhaiNop(new BigDecimal("1000000"));
        hocPhi.setSoTienDaNop(BigDecimal.ZERO);
        when(hocPhiRepository.findById("HP01")).thenReturn(Optional.of(hocPhi));

        VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);
        assertEquals("00", response.getRspCode());
        assertEquals("NOP_MOT_PHAN", hocPhi.getTrangThai());
    }
}
