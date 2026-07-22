package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.LichHocChiTiet;
import com.wiz.universityerpapi.entity.TuanHocChiTiet;
import com.wiz.universityerpapi.repository.projection.LecturerConflictView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichHocChiTietRepository extends JpaRepository<LichHocChiTiet, String> {

    @Query("SELECT th FROM TuanHocChiTiet th " +
           "JOIN th.lichHocChiTiet lh " +
           "WHERE lh.phongHoc = :phongHoc " +
           "AND lh.thuTrongTuan = :thuTrongTuan " +
           "AND th.tuanThu IN :danhSachTuan " +
           "AND (:tietBatDau <= lh.tietKetThuc AND :tietKetThuc >= lh.tietBatDau)")
    List<TuanHocChiTiet> findConflictingRoomSchedule(@Param("phongHoc") String phongHoc,
                                                     @Param("thuTrongTuan") Integer thuTrongTuan,
                                                     @Param("tietBatDau") Integer tietBatDau,
                                                     @Param("tietKetThuc") Integer tietKetThuc,
                                                     @Param("danhSachTuan") List<Integer> danhSachTuan);

    @Query("SELECT th AS tuanHocChiTiet, pc.maGv AS maGv FROM TuanHocChiTiet th " +
           "JOIN th.lichHocChiTiet lh " +
           "JOIN PhanCongDay pc ON pc.maLopHp = lh.maLopHp " +
           "WHERE pc.maGv IN :danhSachMaGv " +
           "AND lh.thuTrongTuan = :thuTrongTuan " +
           "AND th.tuanThu IN :danhSachTuan " +
           "AND (:tietBatDau <= lh.tietKetThuc AND :tietKetThuc >= lh.tietBatDau)")
    List<LecturerConflictView> findConflictingLecturerSchedule(@Param("danhSachMaGv") List<String> danhSachMaGv,
                                                               @Param("thuTrongTuan") Integer thuTrongTuan,
                                                               @Param("tietBatDau") Integer tietBatDau,
                                                               @Param("tietKetThuc") Integer tietKetThuc,
                                                               @Param("danhSachTuan") List<Integer> danhSachTuan);
}
