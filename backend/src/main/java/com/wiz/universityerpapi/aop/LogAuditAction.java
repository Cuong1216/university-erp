package com.wiz.universityerpapi.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Annotation dùng để đánh dấu các phương thức cần lưu log Audit (AOP Pointcut marker).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAuditAction {

    /**
     * Loại thao tác (Ví dụ: CHOT_LUONG_THANG, TAO_LICH_HOC, CAP_NHAT_DIEM).
     */
    String actionType();

    /**
     * Tên thực thể chính bị tác động (Ví dụ: BangLuongThang, LichHocChiTiet).
     */
    String entityName();

    /**
     * SpEL Expression để trích xuất ID thực thể từ kết quả trả về (#result) hoặc tham số (#request).
     * Ví dụ: "#result.maBangLuong" hoặc "#request.maLich".
     */
    String idExpression() default "";
}
