package com.wiz.universityerpapi.aop;

import com.wiz.universityerpapi.entity.AuditLog;
import com.wiz.universityerpapi.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Lưu log trong một Transaction hoàn toàn độc lập (REQUIRES_NEW).
     * Đảm bảo cho dù giao dịch chính thành công hay thất bại, hoặc nếu ghi log gặp sự cố,
     * các giao dịch nghiệp vụ không bị ảnh hưởng chéo.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("Đã lưu audit log thành công cho action: {} - entity: {}", 
                      auditLog.getActionType(), auditLog.getEntityName());
        } catch (Exception e) {
            log.error("CRITICAL: Lỗi khi lưu audit log vào database. Action: {}, Error: {}", 
                      auditLog.getActionType(), e.getMessage(), e);
        }
    }
}
