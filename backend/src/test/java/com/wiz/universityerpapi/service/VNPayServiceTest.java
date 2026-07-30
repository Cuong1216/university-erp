package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.config.VNPayConfig;
import com.wiz.universityerpapi.dto.payment.PaymentDTOs.VNPayWebhookResponseDTO;
import com.wiz.universityerpapi.entity.HocPhi;
import com.wiz.universityerpapi.repository.HocPhiRepository;
import com.wiz.universityerpapi.repository.ThanhToanLogRepository;
import com.wiz.universityerpapi.util.VNPayUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @Mock
    private VNPayConfig vnpayConfig;

    @Mock
    private HocPhiRepository hocPhiRepository;

    @Mock
    private ThanhToanLogRepository thanhToanLogRepository;

    @InjectMocks
    private VNPayService vnPayService;

    @Test
    void processWebhookCallback_returnsInvalidChecksum_whenHashMismatch() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "HP001_123456789");
        params.put("vnp_SecureHash", "invalid_hash");

        when(vnpayConfig.getVnpHashSecret()).thenReturn("mySecret");

        try (MockedStatic<VNPayUtil> mockedVNPayUtil = Mockito.mockStatic(VNPayUtil.class)) {
            mockedVNPayUtil.when(() -> VNPayUtil.hashAllFields(any(), eq("mySecret"))).thenReturn("valid_hash");

            // Act
            VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);

            // Assert
            assertEquals("97", response.getRspCode());
            assertEquals("Invalid Checksum", response.getMessage());
        }
    }

    @Test
    void processWebhookCallback_returnsAlreadyConfirmed_whenDuplicate() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "HP001_123456789");
        params.put("vnp_SecureHash", "valid_hash");

        when(vnpayConfig.getVnpHashSecret()).thenReturn("mySecret");

        try (MockedStatic<VNPayUtil> mockedVNPayUtil = Mockito.mockStatic(VNPayUtil.class)) {
            mockedVNPayUtil.when(() -> VNPayUtil.hashAllFields(any(), eq("mySecret"))).thenReturn("valid_hash");
            when(thanhToanLogRepository.existsByVnpTxnRef("HP001_123456789")).thenReturn(true);

            // Act
            VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);

            // Assert
            assertEquals("02", response.getRspCode());
            assertEquals("Order already confirmed", response.getMessage());
        }
    }

    @Test
    void processWebhookCallback_updatesHocPhi_whenSuccess() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "HP001_123456789");
        params.put("vnp_SecureHash", "valid_hash");
        params.put("vnp_Amount", "100000000"); // 1,000,000 VND
        params.put("vnp_ResponseCode", "00");

        when(vnpayConfig.getVnpHashSecret()).thenReturn("mySecret");

        try (MockedStatic<VNPayUtil> mockedVNPayUtil = Mockito.mockStatic(VNPayUtil.class)) {
            mockedVNPayUtil.when(() -> VNPayUtil.hashAllFields(any(), eq("mySecret"))).thenReturn("valid_hash");
            when(thanhToanLogRepository.existsByVnpTxnRef("HP001_123456789")).thenReturn(false);

            HocPhi hocPhi = new HocPhi();
            hocPhi.setMaHocPhi("HP001");
            hocPhi.setSoTienPhaiNop(new BigDecimal("2000000"));
            hocPhi.setSoTienDaNop(new BigDecimal("0"));
            when(hocPhiRepository.findById("HP001")).thenReturn(Optional.of(hocPhi));

            // Act
            VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);

            // Assert
            assertEquals("00", response.getRspCode());
            assertEquals("Confirm Success", response.getMessage());
            
            // Verify hocPhi is saved with updated amount
            assertEquals(new BigDecimal("1000000"), hocPhi.getSoTienDaNop());
            assertEquals("NOP_MOT_PHAN", hocPhi.getTrangThai());
            verify(hocPhiRepository).save(hocPhi);
        }
    }

    @Test
    void processWebhookCallback_doesNotUpdateHocPhi_whenFailed() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "HP001_123456789");
        params.put("vnp_SecureHash", "valid_hash");
        params.put("vnp_Amount", "100000000"); // 1,000,000 VND
        params.put("vnp_ResponseCode", "24"); // Failed response code

        when(vnpayConfig.getVnpHashSecret()).thenReturn("mySecret");

        try (MockedStatic<VNPayUtil> mockedVNPayUtil = Mockito.mockStatic(VNPayUtil.class)) {
            mockedVNPayUtil.when(() -> VNPayUtil.hashAllFields(any(), eq("mySecret"))).thenReturn("valid_hash");
            when(thanhToanLogRepository.existsByVnpTxnRef("HP001_123456789")).thenReturn(false);

            HocPhi hocPhi = new HocPhi();
            hocPhi.setMaHocPhi("HP001");
            hocPhi.setSoTienPhaiNop(new BigDecimal("2000000"));
            hocPhi.setSoTienDaNop(new BigDecimal("0"));
            when(hocPhiRepository.findById("HP001")).thenReturn(Optional.of(hocPhi));

            // Act
            VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(params);

            // Assert
            assertEquals("00", response.getRspCode()); // Always return 00 for confirm success
            assertEquals("Confirm Success", response.getMessage());
            
            // Verify hocPhi is NOT saved with updated amount
            assertEquals(new BigDecimal("0"), hocPhi.getSoTienDaNop());
            verify(hocPhiRepository, never()).save(any(HocPhi.class));
        }
    }
}
