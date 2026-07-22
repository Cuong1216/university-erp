package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.ChucDanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChucDanhRepository extends JpaRepository<ChucDanh, String> {
}
