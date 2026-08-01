package com.wiz.universityerpapi.core.security;

/**
 * Tập trung tất cả role names để tránh hardcode string rải rác.
 * Single source of truth cho authorization configuration.
 */
public final class AuthorizationConstants {
    public static final String ROLE_ADMIN    = "ROLE_ADMIN";
    public static final String ROLE_GIAO_VU  = "ROLE_GIAO_VU";
    public static final String ROLE_GIANG_VIEN = "ROLE_GIANG_VIEN";
    public static final String ROLE_SINH_VIEN  = "ROLE_SINH_VIEN";
    // Predefined expressions cho @PreAuthorize
    public static final String CAN_MANAGE_PAYROLL =
        "hasAnyRole('ROLE_ADMIN', 'ROLE_GIAO_VU', 'ROLE_GIANG_VIEN')";
    public static final String CAN_VIEW_ALL_SALARY =
        "hasAnyRole('ROLE_ADMIN', 'ROLE_GIAO_VU')";
    public static final String ADMIN_ONLY = "hasRole('ROLE_ADMIN')";
    private AuthorizationConstants() {}
}
