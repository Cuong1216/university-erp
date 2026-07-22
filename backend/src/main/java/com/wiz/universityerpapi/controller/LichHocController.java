package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.dto.LichHocResponseDTO;
import com.wiz.universityerpapi.dto.TaoLichRequestDTO;
import com.wiz.universityerpapi.service.LichHocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lich-hoc")
@RequiredArgsConstructor
public class LichHocController {

    private final LichHocService lichHocService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_GIAO_VU', 'ROLE_ADMIN')")
    public ResponseEntity<LichHocResponseDTO> taoLichHoc(@Valid @RequestBody TaoLichRequestDTO request) {
        LichHocResponseDTO response = lichHocService.taoLichHoc(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
