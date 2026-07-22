package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.GiangVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiangVienRepository extends JpaRepository<GiangVien, String> {
    Optional<GiangVien> findByUserId(UUID userId);
}
