package com.wiz.universityerpapi.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantOnboardingService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void onboardNewTenant(String tenantId) {
        log.info("Bắt đầu onboarding Tenant mới: {}", tenantId);
        
        // 1. Validate định dạng an toàn
        if (!tenantId.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException("Tenant ID không hợp lệ. Chỉ chấp nhận chữ thường, số và gạch dưới.");
        }

        // 2. Tạo Schema vật lý
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);
        log.info("Đã tạo schema: {}", tenantId);

        // 3. Đọc file schema gốc (schema.sql) để clone vào tenant mới
        try {
            ClassPathResource resource = new ClassPathResource("schema.sql");
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                String schemaSql = FileCopyUtils.copyToString(reader);
                
                // Set search_path tạm thời để execute script vào đúng Schema
                jdbcTemplate.execute("SET search_path TO " + tenantId);
                
                // Execute toàn bộ cấu trúc bảng
                // (Trong thực tế cần chia tách các lệnh DDL cẩn thận hơn, hoặc dùng Flyway/Liquibase, 
                // nhưng đây là PoC tự động khởi tạo)
                jdbcTemplate.execute(schemaSql);
                
                log.info("Đã clone thành công cấu trúc cơ sở dữ liệu cho Tenant: {}", tenantId);
                
            } finally {
                // Trả về mặc định
                jdbcTemplate.execute("SET search_path TO public");
            }
        } catch (Exception e) {
            log.error("Lỗi khi clone schema cho Tenant {}", tenantId, e);
            throw new RuntimeException("Lỗi onboarding Tenant: " + e.getMessage(), e);
        }
    }
}
