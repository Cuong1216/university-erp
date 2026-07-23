package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.payment.PaymentDTOs.*;
import com.wiz.universityerpapi.service.VNPayService;
import com.wiz.universityerpapi.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @GetMapping("/tuitions/me")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'ADMIN')")
    public ResponseEntity<List<TuitionResponseDTO>> getMyTuitions(Authentication authentication) {
        String username = authentication.getName(); // username chính là mã sinh viên (ví dụ: SV001)
        log.info("REST GET /api/v1/payment/tuitions/me cho user={}", username);
        List<TuitionResponseDTO> result = vnPayService.getStudentTuitions(username);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tuitions/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU')")
    public ResponseEntity<List<TuitionResponseDTO>> getAllTuitions() {
        log.info("REST GET /api/v1/payment/tuitions/all");
        return ResponseEntity.ok(vnPayService.getAllTuitions());
    }

    @PostMapping("/create-url")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'ADMIN')")
    public ResponseEntity<CreatePaymentUrlResponseDTO> createPaymentUrl(
            @RequestBody CreatePaymentUrlRequestDTO request,
            HttpServletRequest servletRequest
    ) {
        if (request.getIpAddress() == null || request.getIpAddress().isEmpty()) {
            request.setIpAddress(VNPayUtil.getIpAddress(servletRequest));
        }
        log.info("REST POST /api/v1/payment/create-url cho học phí={}", request.getMaHocPhi());
        CreatePaymentUrlResponseDTO response = vnPayService.createPaymentUrl(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Webhook IPN (Instant Payment Notification) từ máy chủ VNPay gọi ngầm tới.
     * Trả về JSON theo chuẩn VNPay: {"RspCode": "00", "Message": "Confirm Success"}
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<VNPayWebhookResponseDTO> vnpayIpnCallback(@RequestParam Map<String, String> allParams) {
        log.info("REST GET /api/v1/payment/vnpay-ipn (Webhook Callback từ VNPay Gateway)");
        VNPayWebhookResponseDTO response = vnPayService.processWebhookCallback(allParams);
        return ResponseEntity.ok(response);
    }

    /**
     * Return URL để trình duyệt người dùng được redirect về sau khi thanh toán trên VNPay.
     * Cũng thực hiện processWebhookCallback để chốt dữ liệu ngay nếu IPN chưa tới kịp, rồi chuyển hướng sang trang Frontend.
     */
    @GetMapping("/vnpay-return")
    public void vnpayReturnCallback(@RequestParam Map<String, String> allParams, HttpServletResponse response) throws IOException {
        log.info("REST GET /api/v1/payment/vnpay-return (Redirect trình duyệt sau khi thanh toán)");
        VNPayWebhookResponseDTO result = vnPayService.processWebhookCallback(allParams);

        String vnpTxnRef = allParams.getOrDefault("vnp_TxnRef", "");
        String vnpAmount = allParams.getOrDefault("vnp_Amount", "0");
        String vnpResponseCode = allParams.getOrDefault("vnp_ResponseCode", "");

        String status = "SUCCESS";
        if (!"00".equals(vnpResponseCode) || !"00".equals(result.getRspCode()) && !"02".equals(result.getRspCode())) {
            status = "FAILED";
        }

        String redirectUrl = String.format("http://localhost:5173/tuition/result?status=%s&txnRef=%s&amount=%s&code=%s",
                status, vnpTxnRef, vnpAmount, vnpResponseCode);

        response.sendRedirect(redirectUrl);
    }
}
