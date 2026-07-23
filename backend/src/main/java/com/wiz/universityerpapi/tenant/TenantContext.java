package com.wiz.universityerpapi.tenant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>();
    public static final String DEFAULT_TENANT = "public";

    public static String getTenantId() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : DEFAULT_TENANT;
    }

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            CURRENT_TENANT.set(tenantId.toLowerCase().trim());
        } else {
            CURRENT_TENANT.set(DEFAULT_TENANT);
        }
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
