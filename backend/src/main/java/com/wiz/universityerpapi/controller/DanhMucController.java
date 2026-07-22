package com.wiz.universityerpapi.controller;

import com.wiz.universityerpapi.entity.ChucDanh;
import com.wiz.universityerpapi.entity.HocVi;
import com.wiz.universityerpapi.entity.Khoa;
import com.wiz.universityerpapi.service.ChucDanhService;
import com.wiz.universityerpapi.service.HocViService;
import com.wiz.universityerpapi.service.KhoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {

    private final KhoaService khoaService;
    private final ChucDanhService chucDanhService;
    private final HocViService hocViService;

    // --- KHOA ENDPOINTS ---
    @GetMapping("/khoa")
    public ResponseEntity<List<Khoa>> getAllKhoa() {
        return ResponseEntity.ok(khoaService.findAll());
    }

    @PostMapping("/khoa")
    public ResponseEntity<Khoa> createKhoa(@RequestBody Khoa khoa) {
        return ResponseEntity.ok(khoaService.save(khoa));
    }

    @PutMapping("/khoa/{maKhoa}")
    public ResponseEntity<Khoa> updateKhoa(@PathVariable String maKhoa, @RequestBody Khoa khoa) {
        return ResponseEntity.ok(khoaService.update(maKhoa, khoa));
    }

    @DeleteMapping("/khoa/{maKhoa}")
    public ResponseEntity<Void> deleteKhoa(@PathVariable String maKhoa) {
        khoaService.deleteById(maKhoa);
        return ResponseEntity.noContent().build();
    }

    // --- CHUC DANH ENDPOINTS ---
    @GetMapping("/chuc-danh")
    public ResponseEntity<List<ChucDanh>> getAllChucDanh() {
        return ResponseEntity.ok(chucDanhService.findAll());
    }

    @PostMapping("/chuc-danh")
    public ResponseEntity<ChucDanh> createChucDanh(@RequestBody ChucDanh chucDanh) {
        return ResponseEntity.ok(chucDanhService.save(chucDanh));
    }

    @PutMapping("/chuc-danh/{maCd}")
    public ResponseEntity<ChucDanh> updateChucDanh(@PathVariable String maCd, @RequestBody ChucDanh chucDanh) {
        return ResponseEntity.ok(chucDanhService.update(maCd, chucDanh));
    }

    @DeleteMapping("/chuc-danh/{maCd}")
    public ResponseEntity<Void> deleteChucDanh(@PathVariable String maCd) {
        chucDanhService.deleteById(maCd);
        return ResponseEntity.noContent().build();
    }

    // --- HOC VI ENDPOINTS ---
    @GetMapping("/hoc-vi")
    public ResponseEntity<List<HocVi>> getAllHocVi() {
        return ResponseEntity.ok(hocViService.findAll());
    }

    @PostMapping("/hoc-vi")
    public ResponseEntity<HocVi> createHocVi(@RequestBody HocVi hocVi) {
        return ResponseEntity.ok(hocViService.save(hocVi));
    }

    @PutMapping("/hoc-vi/{maHv}")
    public ResponseEntity<HocVi> updateHocVi(@PathVariable String maHv, @RequestBody HocVi hocVi) {
        return ResponseEntity.ok(hocViService.update(maHv, hocVi));
    }

    @DeleteMapping("/hoc-vi/{maHv}")
    public ResponseEntity<Void> deleteHocVi(@PathVariable String maHv) {
        hocViService.deleteById(maHv);
        return ResponseEntity.noContent().build();
    }
}
