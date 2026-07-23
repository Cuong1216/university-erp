package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.tenant.TenantOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/saas")
@RequiredArgsConstructor
public class SaaSController {

    private final TenantOnboardingService tenantOnboardingService;

    @PostMapping("/tenants/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> onboardTenant(@PathVariable String tenantId) {
        log.info("REST POST /api/v1/saas/tenants/{} - Khởi tạo môi trường SaaS mới", tenantId);
        try {
            tenantOnboardingService.onboardNewTenant(tenantId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã khởi tạo thành công môi trường cho Tenant: " + tenantId,
                    "tenantId", tenantId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
