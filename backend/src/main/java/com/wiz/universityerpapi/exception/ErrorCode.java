package com.wiz.universityerpapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    INVALID_INPUT(400, "Dữ liệu đầu vào không hợp lệ"),
    
    // 404 Not Found
    ENTITY_NOT_FOUND(404, "Không tìm thấy dữ liệu yêu cầu"),
    
    // 409 Conflict
    DATA_CONFLICT(409, "Dữ liệu đã tồn tại hoặc có xung đột"),
    
    // 422 Unprocessable Entity
    BUSINESS_RULE_VIOLATION(422, "Vi phạm quy tắc nghiệp vụ"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, "Hệ thống đang gặp sự cố");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
