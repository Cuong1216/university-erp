package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.Khoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhoaRepository extends JpaRepository<Khoa, String> {
}
