-- Tạo Composite Index cho Bảng Lịch Học (lich_hoc_chi_tiet)
-- Hỗ trợ truy vấn kết hợp phong_hoc VÀ thu_trong_tuan 
CREATE INDEX idx_lichhoc_phong_thu ON lich_hoc_chi_tiet (phong_hoc, thu_trong_tuan);

-- Tạo Composite Index cho Bảng Lương (bang_luong_thang)
-- Hỗ trợ tra cứu nhanh bảng lương của 1 Giảng viên vào một tháng/năm cụ thể
CREATE INDEX idx_bangluong_magv_thang_nam ON bang_luong_thang (ma_gv, thang, nam);
