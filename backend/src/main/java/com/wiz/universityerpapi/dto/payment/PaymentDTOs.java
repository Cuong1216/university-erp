package com.wiz.universityerpapi.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TuitionResponseDTO {
        private String maHocPhi;
        private String maSv;
        private String namHoc;
        private Integer hocKy;
        private BigDecimal soTienPhaiNop;
        private BigDecimal soTienDaNop;
        private BigDecimal soTienConLai;
        private String trangThai; // CHUA_NOP, NOP_MOT_PHAN, DA_NOP_DU
        private LocalDateTime hanNop;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePaymentUrlRequestDTO {
        private String maHocPhi;
        private BigDecimal amount;
        private String returnUrl;
        private String ipAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePaymentUrlResponseDTO {
        private String paymentUrl;
        private String vnpTxnRef;
        private BigDecimal amount;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VNPayWebhookResponseDTO {
        private String RspCode; // 00: Thành công, 02: Giao dịch đã được cập nhật trước đó (Idempotency), 97: Chữ ký không hợp lệ, 99: Lỗi khác
        private String Message;
    }
}
