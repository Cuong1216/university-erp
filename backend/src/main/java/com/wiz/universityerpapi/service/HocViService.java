package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.entity.HocVi;
import com.wiz.universityerpapi.repository.HocViRepository;
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
public class HocViService {

    private final HocViRepository hocViRepository;

    @Cacheable(value = "hoc_vi", key = "'all'")
    public List<HocVi> findAll() {
        log.info("Querying HocVi list from database...");
        return hocViRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "hoc_vi", allEntries = true)
    public HocVi save(HocVi hocVi) {
        log.info("Saving HocVi and evicting cache 'hoc_vi'...");
        return hocViRepository.save(hocVi);
    }

    @Transactional
    @CacheEvict(value = "hoc_vi", allEntries = true)
    public HocVi update(String maHv, HocVi updatedHocVi) {
        log.info("Updating HocVi {} and evicting cache 'hoc_vi'...", maHv);
        HocVi hocVi = hocViRepository.findById(maHv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Học vị với mã: " + maHv));
        hocVi.setTenHv(updatedHocVi.getTenHv());
        hocVi.setHeSoHv(updatedHocVi.getHeSoHv());
        return hocViRepository.save(hocVi);
    }

    @Transactional
    @CacheEvict(value = "hoc_vi", allEntries = true)
    public void deleteById(String maHv) {
        log.info("Deleting HocVi {} and evicting cache 'hoc_vi'...", maHv);
        if (!hocViRepository.existsById(maHv)) {
            throw new RuntimeException("Không tìm thấy Học vị với mã: " + maHv);
        }
        hocViRepository.deleteById(maHv);
    }
}
