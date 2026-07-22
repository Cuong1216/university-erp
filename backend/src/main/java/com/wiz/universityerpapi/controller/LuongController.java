package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.ChotLuongRequestDTO;
import com.wiz.universityerpapi.dto.ChotLuongResponseDTO;
import com.wiz.universityerpapi.dto.MySalaryResponseDTO;
import com.wiz.universityerpapi.security.CustomUserDetails;
import com.wiz.universityerpapi.service.LuongService;
import com.wiz.universityerpapi.service.SalaryExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/luong")
@RequiredArgsConstructor
public class LuongController {

    private final LuongService luongService;
    private final SalaryExportService salaryExportService;

    @PostMapping("/chot-luong")
    @PreAuthorize("hasAnyRole('ROLE_GIANG_VIEN', 'ROLE_GIAO_VU', 'ROLE_ADMIN')")
    public ResponseEntity<ChotLuongResponseDTO> chotLuongThang(@Valid @RequestBody ChotLuongRequestDTO request,
                                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        ChotLuongResponseDTO response = luongService.chotLuongThang(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-salary")
    @PreAuthorize("hasAnyRole('ROLE_GIANG_VIEN', 'ROLE_GIAO_VU', 'ROLE_ADMIN')")
    public ResponseEntity<List<MySalaryResponseDTO>> getMySalary(@RequestParam(value = "thang", required = false) Integer thang,
                                                                 @RequestParam(value = "nam", required = false) Integer nam,
                                                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(luongService.getMySalaryHistory(currentUser, thang, nam));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ROLE_GIAO_VU', 'ROLE_ADMIN')")
    public ResponseEntity<Resource> exportExcel(@RequestParam("thang") Integer thang,
                                                @RequestParam("nam") Integer nam) {
        try {
            byte[] excelBytes = salaryExportService.exportBangLuongThangExcel(thang, nam);
            ByteArrayResource resource = new ByteArrayResource(excelBytes);

            String filename = String.format("bang_luong_thang_%02d_%d.xlsx", thang, nam);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            throw new com.wiz.universityerpapi.exception.BusinessRuleViolationException("Không thể tạo file Excel: " + e.getMessage());
        }
    }
}
