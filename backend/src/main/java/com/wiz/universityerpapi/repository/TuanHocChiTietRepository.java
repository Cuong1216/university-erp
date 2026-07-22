package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.TuanHocChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TuanHocChiTietRepository extends JpaRepository<TuanHocChiTiet, String> {
}
