package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.NhatKyGiangDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NhatKyGiangDayRepository extends JpaRepository<NhatKyGiangDay, String> {

    @Query("SELECT nk FROM NhatKyGiangDay nk " +
           "JOIN LichHocChiTiet lh ON lh.maLich = nk.maLich " +
           "JOIN PhanCongDay pc ON pc.maLopHp = lh.maLopHp " +
           "WHERE pc.maGv = :maGv " +
           "AND nk.ngayDayThucTe BETWEEN :tuNgay AND :denNgay " +
           "AND (nk.trangThaiThanhToan IS NULL OR nk.trangThaiThanhToan = false)")
    List<NhatKyGiangDay> findUnpaidLogsByGvAndDateRange(@Param("maGv") String maGv,
                                                        @Param("tuNgay") LocalDate tuNgay,
                                                        @Param("denNgay") LocalDate denNgay);

    @Modifying
    @Query("UPDATE NhatKyGiangDay n " +
           "SET n.trangThaiThanhToan = true, n.maBangLuong = :maBangLuong " +
           "WHERE n.maNhatKy IN :danhSachMaNhatKy")
    int markAsPaid(@Param("maBangLuong") String maBangLuong,
                   @Param("danhSachMaNhatKy") List<String> danhSachMaNhatKy);
}
