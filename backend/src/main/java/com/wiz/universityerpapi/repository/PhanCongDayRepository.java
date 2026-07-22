package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.PhanCongDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanCongDayRepository extends JpaRepository<PhanCongDay, String> {

    @Query("SELECT pc.maGv FROM PhanCongDay pc WHERE pc.maLopHp = :maLopHp")
    List<String> findMaGvByMaLopHp(@Param("maLopHp") String maLopHp);
}
