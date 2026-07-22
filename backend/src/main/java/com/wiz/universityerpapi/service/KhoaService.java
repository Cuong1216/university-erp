package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.entity.Khoa;
import com.wiz.universityerpapi.repository.KhoaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhoaService {

    private final KhoaRepository khoaRepository;

    @Cacheable(value = "khoa", key = "'all'")
    public List<Khoa> findAll() {
        log.info("Querying Khoa list from database...");
        return khoaRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "khoa", allEntries = true)
    public Khoa save(Khoa khoa) {
        log.info("Saving Khoa and evicting cache 'khoa'...");
        return khoaRepository.save(khoa);
    }

    @Transactional
    @CacheEvict(value = "khoa", allEntries = true)
    public Khoa update(String maKhoa, Khoa updatedKhoa) {
        log.info("Updating Khoa {} and evicting cache 'khoa'...", maKhoa);
        Khoa khoa = khoaRepository.findById(maKhoa)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khoa với mã: " + maKhoa));
        khoa.setTenKhoa(updatedKhoa.getTenKhoa());
        khoa.setCoSo(updatedKhoa.getCoSo());
        return khoaRepository.save(khoa);
    }

    @Transactional
    @CacheEvict(value = "khoa", allEntries = true)
    public void deleteById(String maKhoa) {
        log.info("Deleting Khoa {} and evicting cache 'khoa'...", maKhoa);
        if (!khoaRepository.existsById(maKhoa)) {
            throw new RuntimeException("Không tìm thấy Khoa với mã: " + maKhoa);
        }
        khoaRepository.deleteById(maKhoa);
    }
}
