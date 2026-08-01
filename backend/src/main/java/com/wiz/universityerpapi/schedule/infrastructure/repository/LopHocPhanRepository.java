package com.wiz.universityerpapi.schedule.infrastructure.repository;

import com.wiz.universityerpapi.schedule.infrastructure.entity.LopHocPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LopHocPhanRepository extends JpaRepository<LopHocPhan, String> {
}
