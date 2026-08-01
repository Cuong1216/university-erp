package com.wiz.universityerpapi.schedule.infrastructure.repository.projection;

import com.wiz.universityerpapi.schedule.infrastructure.entity.TuanHocChiTiet;

public interface LecturerConflictView {
    TuanHocChiTiet getTuanHocChiTiet();
    String getMaGv();
}
