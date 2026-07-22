package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.SalaryStatsResponseDTO;
import com.wiz.universityerpapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/salary-stats")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GIAO_VU', 'ROLE_GIANG_VIEN')")
    public ResponseEntity<SalaryStatsResponseDTO> getSalaryStats(
            @RequestParam(value = "thang", required = false) Integer thang,
            @RequestParam(value = "nam", required = false) Integer nam) {
        return ResponseEntity.ok(dashboardService.getSalaryStats(thang, nam));
    }
}
