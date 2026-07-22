package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.entity.ChucDanh;
import com.wiz.universityerpapi.repository.ChucDanhRepository;
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
public class ChucDanhService {

    private final ChucDanhRepository chucDanhRepository;

    @Cacheable(value = "chuc_danh", key = "'all'")
    public List<ChucDanh> findAll() {
        log.info("Querying ChucDanh list from database...");
        return chucDanhRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "chuc_danh", allEntries = true)
    public ChucDanh save(ChucDanh chucDanh) {
        log.info("Saving ChucDanh and evicting cache 'chuc_danh'...");
        return chucDanhRepository.save(chucDanh);
    }

    @Transactional
    @CacheEvict(value = "chuc_danh", allEntries = true)
    public ChucDanh update(String maCd, ChucDanh updatedChucDanh) {
        log.info("Updating ChucDanh {} and evicting cache 'chuc_danh'...", maCd);
        ChucDanh chucDanh = chucDanhRepository.findById(maCd)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chức danh với mã: " + maCd));
        chucDanh.setTenCd(updatedChucDanh.getTenCd());
        chucDanh.setHeSoCd(updatedChucDanh.getHeSoCd());
        return chucDanhRepository.save(chucDanh);
    }

    @Transactional
    @CacheEvict(value = "chuc_danh", allEntries = true)
    public void deleteById(String maCd) {
        log.info("Deleting ChucDanh {} and evicting cache 'chuc_danh'...", maCd);
        if (!chucDanhRepository.existsById(maCd)) {
            throw new RuntimeException("Không tìm thấy Chức danh với mã: " + maCd);
        }
        chucDanhRepository.deleteById(maCd);
    }
}
