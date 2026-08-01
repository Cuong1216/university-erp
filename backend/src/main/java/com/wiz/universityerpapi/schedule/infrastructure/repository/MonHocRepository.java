package com.wiz.universityerpapi.schedule.infrastructure.repository;

import com.wiz.universityerpapi.schedule.infrastructure.entity.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonHocRepository extends JpaRepository<MonHoc, String> {
}
