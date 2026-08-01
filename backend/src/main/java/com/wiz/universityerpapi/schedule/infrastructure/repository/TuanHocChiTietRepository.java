package com.wiz.universityerpapi.schedule.infrastructure.repository;

import com.wiz.universityerpapi.schedule.infrastructure.entity.TuanHocChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TuanHocChiTietRepository extends JpaRepository<TuanHocChiTiet, String> {
}
