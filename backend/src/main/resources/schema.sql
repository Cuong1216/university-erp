-- ==============================================================================
-- HỆ THỐNG QUẢN LÝ ĐÀO TẠO VÀ NHÂN SỰ CHUẨN ENTERPRISE (TÍCH HỢP ĐẦY ĐỦ)
-- DBMS: PostgreSQL
-- ==============================================================================

-- ==============================================================================
-- PHẦN 1: DỌN DẸP MÔI TRƯỜNG (RESET DATABASE)
-- ==============================================================================
DROP VIEW IF EXISTS v_ho_so_giang_vien;
DROP FUNCTION IF EXISTS function_kiem_tra_tiet_hoc CASCADE;

-- Bảng dùng CASCADE để tự động rụng các khóa ngoại
DROP TABLE IF EXISTS KET_QUA_HOC_TAP CASCADE;
DROP TABLE IF EXISTS SINH_VIEN CASCADE;
DROP TABLE IF EXISTS LOP_SINH_HOAT CASCADE;
DROP TABLE IF EXISTS NHAT_KY_GIANG_DAY CASCADE;
DROP TABLE IF EXISTS PHAN_CONG_DAY CASCADE;
DROP TABLE IF EXISTS TUAN_HOC_CHI_TIET CASCADE;
DROP TABLE IF EXISTS LICH_HOC_CHI_TIET CASCADE;
DROP TABLE IF EXISTS LOP_HOC_PHAN CASCADE;
DROP TABLE IF EXISTS MON_HOC CASCADE;
DROP TABLE IF EXISTS CHUNG_CHI CASCADE;
DROP TABLE IF EXISTS BANG_LUONG_THANG CASCADE;
DROP TABLE IF EXISTS CAU_HINH_LUONG CASCADE;
DROP TABLE IF EXISTS GIANG_VIEN CASCADE;
DROP TABLE IF EXISTS LOAI_CC CASCADE;
DROP TABLE IF EXISTS HOC_VI CASCADE;
DROP TABLE IF EXISTS CHUC_DANH CASCADE;
DROP TABLE IF EXISTS BO_MON CASCADE;
DROP TABLE IF EXISTS KHOA CASCADE;
DROP TABLE IF EXISTS ROLE_PERMISSIONS CASCADE;
DROP TABLE IF EXISTS USER_ROLES CASCADE;
DROP TABLE IF EXISTS PERMISSIONS CASCADE;
DROP TABLE IF EXISTS ROLES CASCADE;
DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS AUDIT_LOG CASCADE;


-- ==============================================================================
-- PHẦN 2: KHỞI TẠO CẤU TRÚC BẢNG (SCHEMA DESIGN)
-- ==============================================================================

-- ---------------------------------------------------------
-- MODULE 1: AUTH & RBAC (Bảo mật & Phân quyền)
-- ---------------------------------------------------------
CREATE TABLE USERS (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, 
    status VARCHAR(20) DEFAULT 'ACTIVE', 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ROLES (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL, 
    description VARCHAR(255)
);

CREATE TABLE PERMISSIONS (
    id SERIAL PRIMARY KEY,
    permission_code VARCHAR(100) UNIQUE NOT NULL, 
    description VARCHAR(255)
);

CREATE TABLE USER_ROLES (
    user_id UUID REFERENCES USERS(id) ON DELETE CASCADE,
    role_id INT REFERENCES ROLES(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE ROLE_PERMISSIONS (
    role_id INT REFERENCES ROLES(id) ON DELETE CASCADE,
    permission_id INT REFERENCES PERMISSIONS(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);


-- ---------------------------------------------------------
-- MODULE 2: CƠ CẤU TỔ CHỨC & DANH MỤC
-- ---------------------------------------------------------
CREATE TABLE KHOA (
    ma_khoa VARCHAR(20) PRIMARY KEY,
    ten_khoa VARCHAR(255) NOT NULL,
    co_so VARCHAR(100)
);

CREATE TABLE BO_MON (
    ma_bo_mon VARCHAR(20) PRIMARY KEY,
    ma_khoa VARCHAR(20) REFERENCES KHOA(ma_khoa) ON DELETE SET NULL,
    ten_bo_mon VARCHAR(255) NOT NULL
);

CREATE TABLE LOP_SINH_HOAT (
    ma_lop_sh VARCHAR(20) PRIMARY KEY,
    ten_lop_sh VARCHAR(100) NOT NULL,
    ma_khoa VARCHAR(20) REFERENCES KHOA(ma_khoa),
    khoa_hoc VARCHAR(20) 
);

CREATE TABLE CHUC_DANH (
    ma_cd VARCHAR(20) PRIMARY KEY,
    ten_cd VARCHAR(100) NOT NULL,
    he_so_cd DECIMAL(4,2)
);

CREATE TABLE HOC_VI (
    ma_hv VARCHAR(20) PRIMARY KEY,
    ten_hv VARCHAR(100) NOT NULL,
    he_so_hv DECIMAL(4,2)
);

CREATE TABLE LOAI_CC (
    ma_loai_cc VARCHAR(20) PRIMARY KEY,
    ten_loai_cc VARCHAR(100) NOT NULL
);


-- ---------------------------------------------------------
-- MODULE 3: QUẢN LÝ NHÂN SỰ & SINH VIÊN
-- ---------------------------------------------------------
CREATE TABLE GIANG_VIEN (
    ma_gv VARCHAR(20) PRIMARY KEY,
    user_id UUID REFERENCES USERS(id) ON DELETE SET NULL, -- Mapping tài khoản
    ma_bo_mon VARCHAR(20) REFERENCES BO_MON(ma_bo_mon),
    ma_cd VARCHAR(20) REFERENCES CHUC_DANH(ma_cd),
    ma_hv VARCHAR(20) REFERENCES HOC_VI(ma_hv),
    loai_hop_dong VARCHAR(50) DEFAULT 'Cơ hữu',
    ho_dem VARCHAR(100) NOT NULL,
    ten VARCHAR(50) NOT NULL,
    ngay_sinh DATE,
    gioi_tinh VARCHAR(10),
    cccd VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    dien_thoai VARCHAR(20),
    ngay_vao_lam DATE,
    trang_thai_lam_viec VARCHAR(50) DEFAULT 'Đang công tác'
);

CREATE TABLE CHUNG_CHI (
    ma_chung_chi VARCHAR(20) PRIMARY KEY,
    ma_gv VARCHAR(20) REFERENCES GIANG_VIEN(ma_gv) ON DELETE CASCADE,
    ma_loai_cc VARCHAR(20) REFERENCES LOAI_CC(ma_loai_cc),
    ten_bang_cap VARCHAR(255) NOT NULL,
    noi_cap VARCHAR(255),
    ngay_cap DATE
);

CREATE TABLE SINH_VIEN (
    ma_sv VARCHAR(20) PRIMARY KEY,
    user_id UUID REFERENCES USERS(id) ON DELETE SET NULL, -- Mapping tài khoản
    ma_lop_sh VARCHAR(20) REFERENCES LOP_SINH_HOAT(ma_lop_sh),
    ho_dem VARCHAR(100) NOT NULL,
    ten VARCHAR(50) NOT NULL,
    ngay_sinh DATE,
    gioi_tinh VARCHAR(10),
    email VARCHAR(100) UNIQUE,
    dien_thoai VARCHAR(20),
    trang_thai_hoc_tap VARCHAR(50) DEFAULT 'Đang học'
);


-- ---------------------------------------------------------
-- MODULE 4: ĐÀO TẠO, LỊCH HỌC & ĐIỂM SỐ
-- ---------------------------------------------------------
CREATE TABLE MON_HOC (
    ma_mon VARCHAR(20) PRIMARY KEY,
    ma_bo_mon VARCHAR(20) REFERENCES BO_MON(ma_bo_mon),
    ten_mon VARCHAR(255) NOT NULL,
    so_tin_chi INT CHECK (so_tin_chi > 0),
    so_tiet_ly_thuyet INT DEFAULT 0,
    so_tiet_thuc_hanh INT DEFAULT 0
);

CREATE TABLE LOP_HOC_PHAN (
    ma_lop_hp VARCHAR(20) PRIMARY KEY,
    ma_mon VARCHAR(20) REFERENCES MON_HOC(ma_mon),
    nam_hoc VARCHAR(20),
    hoc_ky INT,
    si_so_toi_da INT
);

CREATE TABLE KET_QUA_HOC_TAP (
    ma_sv VARCHAR(20) REFERENCES SINH_VIEN(ma_sv) ON DELETE CASCADE,
    ma_lop_hp VARCHAR(20) REFERENCES LOP_HOC_PHAN(ma_lop_hp) ON DELETE CASCADE,
    diem_chuyen_can DECIMAL(4,2) CHECK (diem_chuyen_can >= 0 AND diem_chuyen_can <= 10),
    diem_giua_ky DECIMAL(4,2) CHECK (diem_giua_ky >= 0 AND diem_giua_ky <= 10),
    diem_cuoi_ky DECIMAL(4,2) CHECK (diem_cuoi_ky >= 0 AND diem_cuoi_ky <= 10),
    diem_tong_ket DECIMAL(4,2) GENERATED ALWAYS AS (
        diem_chuyen_can * 0.1 + diem_giua_ky * 0.3 + diem_cuoi_ky * 0.6
    ) STORED, 
    PRIMARY KEY (ma_sv, ma_lop_hp)
);

CREATE TABLE LICH_HOC_CHI_TIET (
    ma_lich VARCHAR(20) PRIMARY KEY,
    ma_lop_hp VARCHAR(20) REFERENCES LOP_HOC_PHAN(ma_lop_hp) ON DELETE CASCADE,
    phong_hoc VARCHAR(50),
    thu_trong_tuan INT CHECK (thu_trong_tuan BETWEEN 2 AND 8),
    tiet_bat_dau INT,
    tiet_ket_thuc INT
);

CREATE TABLE TUAN_HOC_CHI_TIET (
    ma_tuan_hoc VARCHAR(20) PRIMARY KEY,
    ma_lich VARCHAR(20) REFERENCES LICH_HOC_CHI_TIET(ma_lich) ON DELETE CASCADE,
    tuan_thu INT NOT NULL
);


-- ---------------------------------------------------------
-- MODULE 5: CHẤM CÔNG & TÍNH LƯƠNG (SNAPSHOT PATTERN)
-- ---------------------------------------------------------
CREATE TABLE PHAN_CONG_DAY (
    ma_phan_cong VARCHAR(20) PRIMARY KEY,
    ma_gv VARCHAR(20) REFERENCES GIANG_VIEN(ma_gv),
    ma_lop_hp VARCHAR(20) REFERENCES LOP_HOC_PHAN(ma_lop_hp),
    vai_tro VARCHAR(50) DEFAULT 'Giảng dạy chính',
    UNIQUE(ma_gv, ma_lop_hp)
);

CREATE TABLE CAU_HINH_LUONG (
    id SERIAL PRIMARY KEY,
    nam_hoc VARCHAR(20) NOT NULL,
    luong_co_ban DECIMAL(15,2) NOT NULL,      
    don_gia_tiet_chuan DECIMAL(15,2) NOT NULL, 
    don_gia_vuot_gio DECIMAL(15,2) NOT NULL,  
    ngay_ap_dung DATE NOT NULL,
    trang_thai VARCHAR(20) DEFAULT 'ACTIVE',    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE BANG_LUONG_THANG (
    ma_bang_luong VARCHAR(50) PRIMARY KEY, 
    ma_gv VARCHAR(20) REFERENCES GIANG_VIEN(ma_gv),
    thang INT CHECK (thang BETWEEN 1 AND 12),
    nam INT CHECK (nam >= 2000),
    tong_so_tiet_dieu_dong INT DEFAULT 0,  
    tong_so_tiet_thuc_te INT DEFAULT 0,   
    so_tiet_day_thay INT DEFAULT 0,        
    he_so_cd_snapshot DECIMAL(4,2) NOT NULL, 
    he_so_hv_snapshot DECIMAL(4,2) NOT NULL, 
    luong_co_ban_snapshot DECIMAL(15,2) NOT NULL,
    don_gia_tiet_snapshot DECIMAL(15,2) NOT NULL,
    tong_tien_luong DECIMAL(15,2) NOT NULL,
    trang_thai VARCHAR(30) DEFAULT 'CHO_DUYET',
    ngay_chot_luong TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    chi_tiet_tinh_luong_json JSONB,
    UNIQUE(ma_gv, thang, nam) 
);

CREATE TABLE NHAT_KY_GIANG_DAY (
    ma_nhat_ky VARCHAR(20) PRIMARY KEY,
    ma_lich VARCHAR(20) REFERENCES LICH_HOC_CHI_TIET(ma_lich),
    ngay_day_thuc_te DATE NOT NULL,
    so_tiet_thuc_te INT NOT NULL CHECK (so_tiet_thuc_te > 0),
    trang_thai_buoi_hoc VARCHAR(50) DEFAULT 'Bình thường',
    ma_gv_day_thay VARCHAR(20) REFERENCES GIANG_VIEN(ma_gv),
    -- Tích hợp biến kiểm soát lương
    trang_thai_thanh_toan BOOLEAN DEFAULT FALSE,
    ma_bang_luong VARCHAR(50) REFERENCES BANG_LUONG_THANG(ma_bang_luong) ON DELETE SET NULL
);

CREATE TABLE AUDIT_LOG (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    old_value JSONB,
    new_value JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- ==============================================================================
-- PHẦN 3: TẠO TRIGGER, INDEX, VIEW (TỐI ƯU HÓA DB)
-- ==============================================================================

-- Trigger chặn nhập sai tiết học
CREATE OR REPLACE FUNCTION function_kiem_tra_tiet_hoc()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tiet_bat_dau > NEW.tiet_ket_thuc THEN
        RAISE EXCEPTION 'Lỗi: Tiết bắt đầu (%) không thể lớn hơn tiết kết thúc (%)!', NEW.tiet_bat_dau, NEW.tiet_ket_thuc;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_kiem_tra_tiet_hoc
BEFORE INSERT OR UPDATE ON LICH_HOC_CHI_TIET
FOR EACH ROW EXECUTE FUNCTION function_kiem_tra_tiet_hoc();

-- Đánh Index tối ưu truy vấn
CREATE INDEX idx_giang_vien_ten ON GIANG_VIEN(ten, ho_dem);
CREATE INDEX idx_lich_hoc_lop_hp ON LICH_HOC_CHI_TIET(ma_lop_hp);
CREATE INDEX idx_nhat_ky_chua_thanh_toan ON NHAT_KY_GIANG_DAY(ngay_day_thuc_te, trang_thai_thanh_toan) WHERE trang_thai_thanh_toan = FALSE;
CREATE INDEX idx_audit_log_username ON AUDIT_LOG(username);
CREATE INDEX idx_audit_log_entity ON AUDIT_LOG(entity_name, entity_id);
CREATE INDEX idx_audit_log_action_type ON AUDIT_LOG(action_type);

-- View xuất hồ sơ giảng viên
CREATE OR REPLACE VIEW v_ho_so_giang_vien AS
SELECT 
    gv.ma_gv, concat(gv.ho_dem, ' ', gv.ten) AS ho_va_ten,
    gv.email, bm.ten_bo_mon, k.ten_khoa, cd.ten_cd AS chuc_danh, gv.trang_thai_lam_viec
FROM GIANG_VIEN gv
LEFT JOIN BO_MON bm ON gv.ma_bo_mon = bm.ma_bo_mon
LEFT JOIN KHOA k ON bm.ma_khoa = k.ma_khoa
LEFT JOIN CHUC_DANH cd ON gv.ma_cd = cd.ma_cd;


-- ==============================================================================
-- PHẦN 4: SINH DỮ LIỆU MẪU (MOCK DATA GENERATION)
-- ==============================================================================

-- 4.1 Danh mục Tổ chức & Phụ trợ
INSERT INTO KHOA (ma_khoa, ten_khoa, co_so) VALUES 
('KHOA_1', 'Khoa Công nghệ Thông tin', 'Cơ sở chính'),
('KHOA_2', 'Khoa Kinh tế - Quản trị', 'Cơ sở chính'),
('KHOA_3', 'Khoa Ngoại ngữ', 'Cơ sở 2'),
('KHOA_4', 'Khoa Cơ khí - Động lực', 'Cơ sở 2'),
('KHOA_5', 'Khoa Điện - Điện tử', 'Cơ sở chính');

INSERT INTO BO_MON (ma_bo_mon, ma_khoa, ten_bo_mon)
SELECT 
    'BM_' || i, 'KHOA_' || ((i % 5) + 1), 
    (ARRAY['Mạng máy tính', 'Công nghệ phần mềm', 'Hệ thống thông tin', 'Khoa học máy tính', 
           'Kế toán', 'Tài chính ngân hàng', 'Quản trị nhân lực', 'Marketing',
           'Ngôn ngữ Anh', 'Ngôn ngữ Nhật', 'Ngôn ngữ Hàn', 'Biên phiên dịch',
           'Chế tạo máy', 'Công nghệ ô tô', 'Cơ điện tử', 'Nhiệt lạnh',
           'Điện công nghiệp', 'Điện tử viễn thông', 'Tự động hóa', 'Thiết kế vi mạch'])[i]
FROM generate_series(1, 20) AS i;

INSERT INTO CHUC_DANH (ma_cd, ten_cd, he_so_cd) VALUES 
('CD_1', 'Giảng viên', 1.0), ('CD_2', 'Giảng viên chính', 1.5), ('CD_3', 'Giảng viên cao cấp', 2.0);

INSERT INTO HOC_VI (ma_hv, ten_hv, he_so_hv) VALUES 
('HV_1', 'Cử nhân', 1.0), ('HV_2', 'Thạc sĩ', 1.2), ('HV_3', 'Tiến sĩ', 1.5), ('HV_4', 'PGS.TS', 2.0);

INSERT INTO LOAI_CC (ma_loai_cc, ten_loai_cc) VALUES 
('CC_1', 'Ngoại ngữ'), ('CC_2', 'Tin học'), ('CC_3', 'Nghiệp vụ Sư phạm');

-- Khởi tạo Lớp sinh hoạt
INSERT INTO LOP_SINH_HOAT (ma_lop_sh, ten_lop_sh, ma_khoa, khoa_hoc)
SELECT 'LSH_' || i, 'Lớp Học ' || i, 'KHOA_' || ((i % 5) + 1), 'K' || (floor(random() * 4 + 15)::int)
FROM generate_series(1, 100) AS i;

-- 4.2 Module Auth
INSERT INTO ROLES (role_name, description) VALUES 
('ROLE_ADMIN', 'Quản trị viên'), ('ROLE_GIANG_VIEN', 'Giảng viên'), ('ROLE_SINH_VIEN', 'Sinh viên');

-- 4.3 Sinh 1000 Giảng Viên & Mapping User (với hash chuẩn BCrypt của mật khẩu 123456)
WITH new_gv_users AS (
    INSERT INTO USERS (username, password_hash)
    SELECT 'GV_' || LPAD(i::text, 4, '0'), '$2a$10$wT/0v4r9.2/V/L/2/0/2.e/1/2/3/4/5/6/7/8/9/0/1/2/3/4/5/6/7'
    FROM generate_series(1, 1000) AS i RETURNING id, username
)
INSERT INTO GIANG_VIEN (ma_gv, user_id, ma_bo_mon, ma_cd, ma_hv, ho_dem, ten, email, cccd)
SELECT 
    nu.username, nu.id, 'BM_' || ((floor(random() * 20) + 1)::int), 
    'CD_' || ((floor(random() * 3) + 1)::int), 'HV_' || ((floor(random() * 4) + 1)::int),
    'Nguyen', 'Giang Vien ' || nu.username, nu.username || '@truong.edu.vn', '0012' || floor(random() * 1000000)
FROM new_gv_users nu;

INSERT INTO USER_ROLES (user_id, role_id)
SELECT id, (SELECT id FROM ROLES WHERE role_name = 'ROLE_GIANG_VIEN') FROM USERS WHERE username LIKE 'GV_%';

-- 4.4 Sinh 2000 Sinh viên & Mapping User (với hash chuẩn BCrypt của mật khẩu 123456)
WITH new_sv_users AS (
    INSERT INTO USERS (username, password_hash)
    SELECT 'SV_' || LPAD(i::text, 4, '0'), '$2a$10$wT/0v4r9.2/V/L/2/0/2.e/1/2/3/4/5/6/7/8/9/0/1/2/3/4/5/6/7'
    FROM generate_series(1, 2000) AS i RETURNING id, username
)
INSERT INTO SINH_VIEN (ma_sv, user_id, ma_lop_sh, ho_dem, ten, email)
SELECT 
    nu.username, nu.id, 'LSH_' || ((floor(random() * 100) + 1)::int),
    'Tran', 'Sinh Vien ' || nu.username, nu.username || '@student.edu.vn'
FROM new_sv_users nu;

INSERT INTO USER_ROLES (user_id, role_id)
SELECT id, (SELECT id FROM ROLES WHERE role_name = 'ROLE_SINH_VIEN') FROM USERS WHERE username LIKE 'SV_%';

-- 4.5 Môn học, Lớp học phần, Lịch học & Phân công
INSERT INTO MON_HOC (ma_mon, ma_bo_mon, ten_mon, so_tin_chi)
SELECT 'MON_' || i, 'BM_' || ((i % 20) + 1), 'Mon hoc ' || i, 3 FROM generate_series(1, 100) AS i;

INSERT INTO LOP_HOC_PHAN (ma_lop_hp, ma_mon, nam_hoc, hoc_ky)
SELECT 'LHP_' || i, 'MON_' || ((i % 100) + 1), '2023-2024', 1 FROM generate_series(1, 500) AS i;

INSERT INTO PHAN_CONG_DAY (ma_phan_cong, ma_gv, ma_lop_hp)
SELECT 'PC_' || i, 'GV_' || LPAD(((i % 1000) + 1)::text, 4, '0'), 'LHP_' || i FROM generate_series(1, 500) AS i;

INSERT INTO LICH_HOC_CHI_TIET (ma_lich, ma_lop_hp, phong_hoc, thu_trong_tuan, tiet_bat_dau, tiet_ket_thuc)
SELECT 'LICH_' || i, 'LHP_' || i, 'A1-101', ((i % 7) + 2), 1, 3 FROM generate_series(1, 500) AS i;

-- 4.6 Nhật ký & Cấu hình lương
INSERT INTO CAU_HINH_LUONG (nam_hoc, luong_co_ban, don_gia_tiet_chuan, don_gia_vuot_gio, ngay_ap_dung)
VALUES ('2023-2024', 2340000, 120000, 180000, '2023-09-01');

INSERT INTO NHAT_KY_GIANG_DAY (ma_nhat_ky, ma_lich, ngay_day_thuc_te, so_tiet_thuc_te, trang_thai_thanh_toan)
SELECT 'NK_' || i, 'LICH_' || i, CURRENT_DATE - (floor(random() * 30 + 1)::int * interval '1 day'), 3, FALSE
FROM generate_series(1, 500) AS i;

-- Script hoàn tất!

-- 4.7 Sinh dữ liệu mẫu Bảng Lương Tháng cho 6 tháng gần nhất (để hiển thị trực quan trên Admin Dashboard)
INSERT INTO BANG_LUONG_THANG (
    ma_bang_luong, ma_gv, thang, nam, tong_so_tiet_thuc_te, 
    he_so_cd_snapshot, he_so_hv_snapshot, luong_co_ban_snapshot, don_gia_tiet_snapshot, 
    tong_tien_luong, trang_thai, ngay_chot_luong, chi_tiet_tinh_luong_json
)
SELECT 
    'BL_SAMPLE_' || m || '_' || LPAD(i::text, 4, '0'),
    'GV_' || LPAD(i::text, 4, '0'),
    m,
    2026,
    (20 + (i % 15)),
    1.5,
    1.2,
    2340000,
    120000,
    (2340000 + (20 + (i % 15)) * 120000 * 1.5),
    'DA_DUYET',
    CURRENT_TIMESTAMP - ((7 - m) * interval '30 day'),
    '{"congThuc": "TongTienLuong = LuongCoBan + (SoTiet * DonGia * HeSo)"}'::jsonb
FROM generate_series(2, 7) AS m
CROSS JOIN generate_series(1, 50) AS i
ON CONFLICT (ma_gv, thang, nam) DO NOTHING;

