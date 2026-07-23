package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.analytics.SalaryForecastResponseDTO;
import com.wiz.universityerpapi.service.ForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ForecastService forecastService;

    @GetMapping("/salary-forecast")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIAO_VU')")
    public ResponseEntity<SalaryForecastResponseDTO> getSalaryForecast(
            @RequestParam(name = "periods", defaultValue = "6") int periods
    ) {
        log.info("REST GET /api/v1/analytics/salary-forecast (periods={})", periods);
        SalaryForecastResponseDTO response = forecastService.getSalaryForecast(periods);
        return ResponseEntity.ok(response);
    }
}
