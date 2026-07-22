package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.BoMon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoMonRepository extends JpaRepository<BoMon, String> {
}
