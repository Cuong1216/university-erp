package com.wiz.universityerpapi.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * Around Advice chặn tất cả các phương thức có gắn annotation @LogAuditAction.
     */
    @Around("@annotation(logAuditAction)")
    public Object auditAround(ProceedingJoinPoint joinPoint, LogAuditAction logAuditAction) throws Throwable {
        String username = extractUsernameFromSecurityContext();
        String actionType = logAuditAction.actionType();
        String entityName = logAuditAction.entityName();

        // 1. Ghi nhận thông tin tham số đầu vào trước khi method thực thi (Làm oldValue / Request payload)
        String oldValueJson = serializeArguments(joinPoint);

        Object result = null;
        try {
            // 2. Cho phép method nghiệp vụ thực thi và lấy kết quả trả về
            result = joinPoint.proceed();

            // 3. Sau khi nghiệp vụ chạy THÀNH CÔNG -> Trích xuất entityId và newValue
            String entityId = extractEntityId(joinPoint, logAuditAction.idExpression(), result);
            String newValueJson = serializeObject(result);

            // 4. Tạo đối tượng AuditLog và lưu qua dịch vụ độc lập
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .actionType(actionType)
                    .entityName(entityName)
                    .entityId(entityId)
                    .oldValue(oldValueJson)
                    .newValue(newValueJson)
                    .build();

            auditLogService.saveAuditLog(auditLog);

            return result;

        } catch (Throwable ex) {
            log.warn("Method bị exception, thao tác audit action={} của user={} không được hoàn tất: {}", 
                     actionType, username, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Trích xuất Username từ Spring Security Context.
     */
    private String extractUsernameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            return (String) principal;
        }
        return "ANONYMOUS";
    }

    /**
     * Trích xuất Entity ID bằng SpEL Expression (Ví dụ: "#result.maBangLuong").
     */
    private String extractEntityId(ProceedingJoinPoint joinPoint, String idExpression, Object result) {
        if (idExpression == null || idExpression.isBlank()) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            EvaluationContext context = new MethodBasedEvaluationContext(
                    joinPoint.getTarget(), method, joinPoint.getArgs(), parameterNameDiscoverer
            );
            context.setVariable("result", result);

            Object idObj = spelParser.parseExpression(idExpression).getValue(context);
            return idObj != null ? idObj.toString() : null;
        } catch (Exception ex) {
            log.debug("Không thể trích xuất ID từ expression '{}': {}", idExpression, ex.getMessage());
            return null;
        }
    }

    /**
     * Serialize toàn bộ tham số đầu vào sang chuỗi JSON.
     */
    private String serializeArguments(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (args == null || args.length == 0) return null;

            Map<String, Object> argsMap = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                String name = (paramNames != null && paramNames.length > i) ? paramNames[i] : ("arg" + i);
                Object arg = args[i];
                if (arg != null && !isNonSerializableArg(arg)) {
                    argsMap.put(name, arg);
                }
            }
            return objectMapper.writeValueAsString(argsMap);
        } catch (Exception ex) {
            log.debug("Lỗi serialize arguments cho audit: {}", ex.getMessage());
            return "{\"error\": \"Could not serialize input arguments\"}";
        }
    }

    private String serializeObject(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            log.debug("Lỗi serialize object cho audit: {}", ex.getMessage());
            return null;
        }
    }

    private boolean isNonSerializableArg(Object arg) {
        return arg instanceof UserDetails 
                || arg.getClass().getName().startsWith("org.springframework.security")
                || arg.getClass().getName().startsWith("jakarta.servlet");
    }
}
