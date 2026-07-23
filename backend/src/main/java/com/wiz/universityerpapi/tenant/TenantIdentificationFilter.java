package com.wiz.universityerpapi.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantIdentificationFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final Pattern TENANT_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = request.getParameter("tenantId");
        }

        if (tenantId != null && !tenantId.isBlank()) {
            // Chống SQL Injection trong tên schema: chỉ cho phép chữ, số và dấu gạch dưới
            if (TENANT_PATTERN.matcher(tenantId).matches()) {
                TenantContext.setTenantId(tenantId);
            } else {
                log.warn("CẢNH BÁO BẢO MẬT: Phát hiện định dạng X-Tenant-ID bất hợp lệ hoặc nghi vấn SQL Injection: {}", tenantId);
                TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
            }
        } else {
            TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
