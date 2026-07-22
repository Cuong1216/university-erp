package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.CauHinhLuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CauHinhLuongRepository extends JpaRepository<CauHinhLuong, Integer> {
    Optional<CauHinhLuong> findFirstByTrangThaiOrderByIdDesc(String trangThai);
}
