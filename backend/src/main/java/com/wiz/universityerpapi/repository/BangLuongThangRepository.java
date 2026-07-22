package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.BangLuongThang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BangLuongThangRepository extends JpaRepository<BangLuongThang, String> {
    Optional<BangLuongThang> findByMaGvAndThangAndNam(String maGv, Integer thang, Integer nam);
    List<BangLuongThang> findByMaGvOrderByNamDescThangDesc(String maGv);
    boolean existsByMaGvAndThangAndNam(String maGv, Integer thang, Integer nam);
    List<BangLuongThang> findByThangAndNam(Integer thang, Integer nam);
}
