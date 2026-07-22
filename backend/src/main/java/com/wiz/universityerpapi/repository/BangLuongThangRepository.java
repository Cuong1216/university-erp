package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.BangLuongThang;
import com.wiz.universityerpapi.repository.projection.DepartmentSalaryView;
import com.wiz.universityerpapi.repository.projection.MonthlySalaryTrendView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BangLuongThangRepository extends JpaRepository<BangLuongThang, String> {
    Optional<BangLuongThang> findByMaGvAndThangAndNam(String maGv, Integer thang, Integer nam);
    List<BangLuongThang> findByMaGvOrderByNamDescThangDesc(String maGv);
    boolean existsByMaGvAndThangAndNam(String maGv, Integer thang, Integer nam);
    List<BangLuongThang> findByThangAndNam(Integer thang, Integer nam);

    @Query("SELECT b.thang AS thang, b.nam AS nam, SUM(b.tongTienLuong) AS tongTienLuong " +
           "FROM BangLuongThang b " +
           "GROUP BY b.nam, b.thang " +
           "ORDER BY b.nam DESC, b.thang DESC")
    List<MonthlySalaryTrendView> findSalaryTrends(Pageable pageable);

    @Query("SELECT k.maKhoa AS maKhoaHoacBoMon, k.tenKhoa AS tenKhoaHoacBoMon, COALESCE(SUM(b.tongTienLuong), 0) AS tongTienLuong " +
           "FROM BangLuongThang b " +
           "JOIN GiangVien gv ON b.maGv = gv.maGv " +
           "JOIN BoMon bm ON gv.boMon.maBoMon = bm.maBoMon " +
           "JOIN Khoa k ON bm.khoa.maKhoa = k.maKhoa " +
           "WHERE b.thang = :thang AND b.nam = :nam " +
           "GROUP BY k.maKhoa, k.tenKhoa " +
           "ORDER BY COALESCE(SUM(b.tongTienLuong), 0) DESC")
    List<DepartmentSalaryView> findSalaryDistributionByKhoa(@Param("thang") Integer thang, @Param("nam") Integer nam);

    @Query("SELECT bm.maBoMon AS maKhoaHoacBoMon, bm.tenBoMon AS tenKhoaHoacBoMon, COALESCE(SUM(b.tongTienLuong), 0) AS tongTienLuong " +
           "FROM BangLuongThang b " +
           "JOIN GiangVien gv ON b.maGv = gv.maGv " +
           "JOIN BoMon bm ON gv.boMon.maBoMon = bm.maBoMon " +
           "WHERE b.thang = :thang AND b.nam = :nam " +
           "GROUP BY bm.maBoMon, bm.tenBoMon " +
           "ORDER BY COALESCE(SUM(b.tongTienLuong), 0) DESC")
    List<DepartmentSalaryView> findSalaryDistributionByBoMon(@Param("thang") Integer thang, @Param("nam") Integer nam);

    long countByThangAndNam(Integer thang, Integer nam);
}

