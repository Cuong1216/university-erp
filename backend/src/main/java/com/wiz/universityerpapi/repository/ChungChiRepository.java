package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.ChungChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChungChiRepository extends JpaRepository<ChungChi, String> {
}
