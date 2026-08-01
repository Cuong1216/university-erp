package com.wiz.universityerpapi.schedule.infrastructure.repository;

import com.wiz.universityerpapi.schedule.infrastructure.entity.GiangVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiangVienRepository extends JpaRepository<GiangVien, String> {
    Optional<GiangVien> findByUserId(UUID userId);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT cd.he_so_cd as heSoCd, COALESCE(hv.he_so_hv, 1.00) as heSoHv FROM giang_vien gv 
            LEFT JOIN chuc_danh cd ON gv.ma_cd = cd.ma_cd 
            LEFT JOIN hoc_vi hv ON gv.ma_hv = hv.ma_hv 
            WHERE gv.ma_gv = :maGv OR gv.user_id = (SELECT u.id FROM users u WHERE u.username = :maGv)
            LIMIT 1
            """, nativeQuery = true)
    Optional<com.wiz.universityerpapi.schedule.infrastructure.repository.projection.GiangVienHeSoView> findHeSoByMaGv(@org.springframework.data.repository.query.Param("maGv") String maGv);
}
