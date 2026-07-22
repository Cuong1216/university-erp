package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.KetQuaHocTap;
import com.wiz.universityerpapi.entity.KetQuaHocTapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KetQuaHocTapRepository extends JpaRepository<KetQuaHocTap, KetQuaHocTapId> {
}
