package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.HocPhi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HocPhiRepository extends JpaRepository<HocPhi, String> {
    List<HocPhi> findByMaSv(String maSv);
    List<HocPhi> findByMaSvAndNamHocAndHocKy(String maSv, String namHoc, Integer hocKy);
}
