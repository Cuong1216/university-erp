package com.wiz.universityerpapi.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các yêu cầu hỏi đáp với ERP Copilot (AI Assistant).
 * Chỉ cho phép quyền ADMIN hoặc GIAO_VU truy cập.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final SalaryAnalystAiService salaryAnalystAiService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU')")
    public ResponseEntity<AiChatResponseDTO> chatWithCopilot(@RequestBody AiChatRequestDTO request) {
        log.info("Nhận câu hỏi từ người dùng tới ERP Copilot: {}", request.getMessage());
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AiChatResponseDTO("Vui lòng nhập câu hỏi.", System.currentTimeMillis()));
        }

        try {
            String aiReply = salaryAnalystAiService.chat(request.getMessage());
            return ResponseEntity.ok(new AiChatResponseDTO(aiReply, System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Lỗi xử lý AI Copilot: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    new AiChatResponseDTO("Xin lỗi, hệ thống AI hiện đang bận hoặc gặp sự cố kết nối. Vui lòng thử lại sau.", System.currentTimeMillis())
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiChatRequestDTO {
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiChatResponseDTO {
        private String reply;
        private long timestamp;
    }
}
