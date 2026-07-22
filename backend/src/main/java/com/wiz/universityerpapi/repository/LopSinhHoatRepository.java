package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.LopSinhHoat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LopSinhHoatRepository extends JpaRepository<LopSinhHoat, String> {
}
