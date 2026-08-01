package com.wiz.universityerpapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Infrastructure Service chuyên biệt cho việc gửi thông báo (WebSocket).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendPayrollSuccessNotification(String username, String maGv, int thang, int nam, Object data) {
        Map<String, Object> payload = Map.of(
                "type", "CHOT_LUONG_SUCCESS",
                "status", "SUCCESS",
                "message", String.format("🎉 Chốt lương thành công cho giảng viên %s (Tháng %d/%d)!", maGv, thang, nam),
                "data", data,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
        log.info("Đã gửi thông báo WebSocket thành công tới user: {}", username);
    }

    public void sendPayrollErrorNotification(String username, String maGv, int thang, int nam, String errorMessage) {
        Map<String, Object> errorPayload = Map.of(
                "type", "CHOT_LUONG_ERROR",
                "status", "ERROR",
                "message", String.format("❌ Chốt lương thất bại (Tháng %d/%d cho %s): %s", thang, nam, maGv, errorMessage),
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", errorPayload);
    }
}
