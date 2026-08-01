package com.wiz.universityerpapi.payroll.infrastructure.repository;

import com.wiz.universityerpapi.payroll.infrastructure.entity.CauHinhLuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CauHinhLuongRepository extends JpaRepository<CauHinhLuong, Integer> {
    Optional<CauHinhLuong> findFirstByTrangThaiOrderByIdDesc(String trangThai);
}
