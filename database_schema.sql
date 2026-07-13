-- ============================================
-- Database Schema - Project AnYen
-- Generated from Entity Classes
-- ============================================

CREATE DATABASE IF NOT EXISTS anyen_db;
USE anyen_db;

-- ============================================
-- 1. Table: doitac (Đối tác)
-- ============================================
CREATE TABLE doitac (
    MaDoiTac INT AUTO_INCREMENT PRIMARY KEY,
    TenDoiTac VARCHAR(255),
    TenDoanhNghiep VARCHAR(255),
    MaSoThue VARCHAR(50),
    TenDangNhap VARCHAR(255),
    MatKhau VARCHAR(255),
    Email VARCHAR(255),
    SoDienThoai VARCHAR(20),
    DiaChi VARCHAR(500),
    TrangThai INT DEFAULT 1 COMMENT '0=Ngừng hoạt động, 1=Đang hoạt động, 2=Chờ xác nhận, 3=Đã xóa',
    ConfirmationToken VARCHAR(255),
    CreatedAt DATETIME,
    UpdatedAt DATETIME
);

-- ============================================
-- 2. Table: nhanvien (Nhân viên)
-- ============================================
CREATE TABLE nhanvien (
    MaNhanVien INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(255),
    TenDangNhap VARCHAR(255),
    MatKhau VARCHAR(255),
    VaiTro INT COMMENT '1=Admin, 2=Bán hàng, 3=Tư vấn, 4=Hotline, 5=Kế toán',
    TrangThai INT DEFAULT 1 COMMENT '0=Nghỉ việc, 1=Hoạt động',
    Email VARCHAR(255),
    DiaChi VARCHAR(500),
    SoDienThoai VARCHAR(20),
    Latitude DECIMAL(10, 8),
    Longitude DECIMAL(11, 8)
);

-- ============================================
-- 3. Table: khachhang (Khách hàng)
-- ============================================
CREATE TABLE khachhang (
    MaKhachHang INT AUTO_INCREMENT PRIMARY KEY,
    tenKhachHang VARCHAR(255),
    cccd VARCHAR(50),
    DiaChi VARCHAR(500),
    email VARCHAR(255),
    soDienThoai VARCHAR(20),
    MaNhanVienPhuTrach INT,
    NgayDangKy DATETIME,
    NguonDangKy VARCHAR(255),
    NhuCauHoTro TEXT,
    GhiChu TEXT,
    Latitude DECIMAL(10, 8),
    Longitude DECIMAL(11, 8)
);

-- ============================================
-- 4. Table: sanpham (Sản phẩm)
-- ============================================
CREATE TABLE sanpham (
    MaSanPham INT AUTO_INCREMENT PRIMARY KEY,
    tenSanPham VARCHAR(255),
    loai VARCHAR(100),
    noiThat VARCHAR(100),
    quyCach VARCHAR(255),
    tonGiao VARCHAR(100),
    giaTien DECIMAL(18, 2),
    MaDoiTac INT,
    SoLuong INT,
    thietKe VARCHAR(255),
    xuatXu VARCHAR(255),
    GhiChu TEXT,
    khuyenMai DECIMAL(18, 2),
    mauSac VARCHAR(100),
    HinhAnh VARCHAR(500),
    vatLieu VARCHAR(255),
    trangThai INT DEFAULT 1 COMMENT '0=Ẩn, 1=Đang bán, 2=Chờ xác nhận',
    kichThuoc VARCHAR(255),
    trongLuong VARCHAR(100),
    CNSX VARCHAR(255),
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac)
);

-- ============================================
-- 5. Table: sanphamchitiet (Chi tiết sản phẩm)
-- ============================================
CREATE TABLE sanphamchitiet (
    MaChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaSanPham INT,
    LoaiKhoi VARCHAR(100),
    NoiDung TEXT,
    ThuTu INT,
    CreatedAt DATETIME,
    UpdatedAt DATETIME,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham)
);

-- ============================================
-- 6. Table: sanphamhinhanh (Hình ảnh sản phẩm)
-- ============================================
CREATE TABLE sanphamhinhanh (
    MaHinhAnh INT AUTO_INCREMENT PRIMARY KEY,
    MaSanPham INT,
    MaChiTiet INT,
    LoaiHinhAnh VARCHAR(100),
    UrlHinhAnh VARCHAR(500),
    ThuTu INT,
    CreatedAt DATETIME,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham),
    FOREIGN KEY (MaChiTiet) REFERENCES sanphamchitiet(MaChiTiet)
);

-- ============================================
-- 7. Table: combo (Combo sản phẩm)
-- ============================================
CREATE TABLE combo (
    ComboId INT AUTO_INCREMENT PRIMARY KEY,
    TenCombo VARCHAR(255),
    Gia DECIMAL(18, 2),
    MoTa TEXT,
    HinhAnh VARCHAR(500),
    TrangThai INT COMMENT '0=Ẩn, 1=Hoạt động, 2=Ngừng kinh doanh'
);

-- ============================================
-- 8. Table: combochitiet (Chi tiết combo)
-- ============================================
CREATE TABLE combochitiet (
    ComboChiTietId INT PRIMARY KEY,
    MaSanPham INT,
    ComboID INT,
    Loai INT COMMENT '0=Sản phẩm, 1=Dịch vụ',
    NoiDung TEXT,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham),
    FOREIGN KEY (ComboID) REFERENCES combo(ComboId)
);

-- ============================================
-- 9. Table: combochitiet_hinhanh (Hình ảnh chi tiết combo)
-- ============================================
CREATE TABLE combochitiet_hinhanh (
    MaHinhAnh INT AUTO_INCREMENT PRIMARY KEY,
    ComboChiTietId INT,
    TenHinhAnh VARCHAR(255),
    HinhAnh VARCHAR(500),
    ThuTu INT,
    FOREIGN KEY (ComboChiTietId) REFERENCES combochitiet(ComboChiTietId)
);

-- ============================================
-- 10. Table: donhang (Đơn hàng)
-- ============================================
CREATE TABLE donhang (
    MaDonHang INT AUTO_INCREMENT PRIMARY KEY,
    MaKhachHang INT,
    MaNhanVien INT,
    NgayTaoDon DATE,
    tongTien DECIMAL(18, 2),
    TrangThai INT COMMENT '1=Mới tạo, 2=Chờ đối tác xác nhận, 3=Đã xác nhận, 4=Đang xử lý, 5=Chờ thanh toán, 6=Hoàn thành, 7=Đã hủy, 8=Đối tác từ chối',
    GhiChu TEXT,
    PhuongThucThanhToan INT COMMENT '0=Chưa chọn, 1=Tiền mặt, 2=Chuyển khoản',
    TrangThaiThanhToan INT COMMENT '0=Chưa thanh toán, 1=Đã thanh toán, 2=Chờ xác nhận',
    LyDoHuy TEXT,
    FOREIGN KEY (MaKhachHang) REFERENCES khachhang(MaKhachHang),
    FOREIGN KEY (MaNhanVien) REFERENCES nhanvien(MaNhanVien)
);

-- ============================================
-- 11. Table: chitietdonhang (Chi tiết đơn hàng)
-- ============================================
CREATE TABLE chitietdonhang (
    MaDonHangChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT,
    MaSanPham INT,
    SoLuong INT,
    giaTien DECIMAL(18, 2),
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang),
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham)
);

-- ============================================
-- 12. Table: hoadon (Hóa đơn)
-- ============================================
CREATE TABLE hoadon (
    MaHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT,
    NgayIn DATE,
    TongTien DECIMAL(18, 2),
    PhuongThucThanhToan INT COMMENT '0=Chưa chọn, 1=Tiền mặt, 2=Chuyển khoản',
    TrangThai INT COMMENT '0=Đã hủy, 1=Đã tạo',
    CreatedAt DATETIME,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang)
);

-- ============================================
-- 13. Table: hopdong (Hợp đồng)
-- ============================================
CREATE TABLE hopdong (
    MaHopDong INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    NgayKyHD DATE,
    NgayViet DATE,
    ThoiHanKetThuc DATE,
    TrangThai VARCHAR(50),
    An BOOLEAN,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang)
);

-- ============================================
-- 14. Table: hdongct (Chi tiết hợp đồng)
-- ============================================
CREATE TABLE hdongct (
    MaHDongCT INT AUTO_INCREMENT PRIMARY KEY,
    MaHopDong INT NOT NULL,
    HoTenNguoiMat VARCHAR(255),
    NgayMat DATE,
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    SoGiayBaoTu VARCHAR(100),
    NoiCapGiayBaoTu VARCHAR(255),
    CoSoMaiTang VARCHAR(255),
    KhuMo VARCHAR(100),
    SoMo VARCHAR(100),
    NgayGioAnTang DATETIME,
    FOREIGN KEY (MaHopDong) REFERENCES hopdong(MaHopDong)
);

-- ============================================
-- 15. Table: congno (Công nợ)
-- ============================================
CREATE TABLE congno (
    MaCongNo INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    MaDoiTac INT NOT NULL,
    TongTien DECIMAL(18, 2) NOT NULL,
    DaThanhToan DECIMAL(18, 2) NOT NULL,
    ConLai DECIMAL(18, 2) NOT NULL,
    HanThanhToan DATE,
    TrangThai INT NOT NULL COMMENT '0=Chưa thanh toán, 1=Thanh toán một phần, 2=Đã thanh toán, 3=Quá hạn',
    GhiChu TEXT,
    CreatedAt DATETIME,
    UpdatedAt DATETIME,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang),
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac)
);

-- ============================================
-- 16. Table: lichsucongno (Lịch sử công nợ)
-- ============================================
CREATE TABLE lichsucongno (
    MaLichSuCongNo INT AUTO_INCREMENT PRIMARY KEY,
    MaCongNo INT NOT NULL,
    SoTienThanhToan DECIMAL(18, 2) NOT NULL,
    PhuongThucThanhToan INT NOT NULL COMMENT '1=Tiền mặt, 2=Chuyển khoản',
    MaNhanVien INT,
    NgayThanhToan DATETIME NOT NULL,
    MaGiaoDich VARCHAR(100),
    GhiChu TEXT,
    FOREIGN KEY (MaCongNo) REFERENCES congno(MaCongNo),
    FOREIGN KEY (MaNhanVien) REFERENCES nhanvien(MaNhanVien)
);

-- ============================================
-- 17. Table: thongbao (Thông báo)
-- ============================================
CREATE TABLE thongbao (
    MaThongBao INT AUTO_INCREMENT PRIMARY KEY,
    TieuDe VARCHAR(255),
    NoiDung TEXT,
    LoaiThongBao VARCHAR(50) COMMENT 'CONG_VIEC, HE_THONG, TU_CHOI, DUYET_SAN_PHAM',
    NguoiGuiId INT,
    NguoiNhanId INT,
    MaKhachHang INT,
    TrangThai INT DEFAULT 0 COMMENT '0=Chưa đọc, 1=Đã đọc, 2=Đã chấp nhận, 3=Đã từ chối, 4=Chờ xác nhận',
    LyDoTuChoi TEXT,
    NgayTao DATETIME,
    NgayCapNhat DATETIME
);

-- ============================================
-- 18. Table: thongbaodoitac (Thông báo đối tác)
-- ============================================
CREATE TABLE thongbaodoitac (
    MaThongBao INT AUTO_INCREMENT PRIMARY KEY,
    MaDoiTac INT,
    MaDonHang INT,
    Loai VARCHAR(50) DEFAULT 'DON_HANG' COMMENT 'DON_HANG, DUYET_SAN_PHAM',
    TieuDe VARCHAR(255),
    NoiDung TEXT,
    TrangThaiThongBao VARCHAR(50) DEFAULT 'CHO_XAC_NHAN' COMMENT 'CHO_XAC_NHAN, DA_CHAP_NHAN, DA_TU_CHOI',
    LyDoTuChoi TEXT,
    DaDoc BOOLEAN DEFAULT FALSE,
    ThoiGianTao DATETIME,
    ThoiGianXuLy DATETIME,
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac),
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang)
);

-- ============================================
-- Indexes for performance
-- ============================================
CREATE INDEX idx_doitac_trangthai ON doitac(TrangThai);
CREATE INDEX idx_nhanvien_trangthai ON nhanvien(TrangThai);
CREATE INDEX idx_sanpham_trangthai ON sanpham(trangThai);
CREATE INDEX idx_sanpham_madoitac ON sanpham(MaDoiTac);
CREATE INDEX idx_donhang_makhachhang ON donhang(MaKhachHang);
CREATE INDEX idx_donhang_manhanvien ON donhang(MaNhanVien);
CREATE INDEX idx_donhang_trangthai ON donhang(TrangThai);
CREATE INDEX idx_congno_madoitac ON congno(MaDoiTac);
CREATE INDEX idx_congno_madonhang ON congno(MaDonHang);
CREATE INDEX idx_thongbao_nguoinhanid ON thongbao(NguoiNhanId);
CREATE INDEX idx_thongbaodoitac_madoitac ON thongbaodoitac(MaDoiTac);
CREATE INDEX idx_thongbaodoitac_madonhang ON thongbaodoitac(MaDonHang);

-- ============================================
-- Sample Data
-- ============================================

-- Insert sample data into doitac
INSERT INTO doitac (TenDoiTac, TenDoanhNghiep, MaSoThue, TenDangNhap, MatKhau, Email, SoDienThoai, DiaChi, TrangThai, CreatedAt, UpdatedAt) VALUES
('Nguyễn Văn A', 'Công ty TNHH Mai Táng An Yên', '0101234567', 'doitac1', '123456', 'nguyenvana@gmail.com', '0901234567', 'Hà Nội', 1, NOW(), NOW()),
('Trần Thị B', 'Công ty CP Dịch Vụ Táng Lễ', '0102345678', 'doitac2', '123456', 'tranthib@gmail.com', '0912345678', 'TP. Hồ Chí Minh', 1, NOW(), NOW()),
('Lê Văn C', 'Công ty TNHH Vật Liệu Táng Lễ', '0103456789', 'doitac3', '123456', 'levanc@gmail.com', '0923456789', 'Đà Nẵng', 2, NOW(), NOW());

-- Insert sample data into nhanvien
INSERT INTO nhanvien (HoTen, TenDangNhap, MatKhau, VaiTro, TrangThai, Email, DiaChi, SoDienThoai, Latitude, Longitude) VALUES
('Admin System', 'admin', '123456', 1, 1, 'admin@anyen.vn', 'Hà Nội', '0987654321', 21.0285, 105.8542),
('Nguyễn Văn X', 'nhanvien1', '123456', 2, 1, 'nhanvien1@anyen.vn', 'Hà Nội', '0987654322', 21.0285, 105.8542),
('Trần Thị Y', 'nhanvien2', '123456', 3, 1, 'nhanvien2@anyen.vn', 'TP. Hồ Chí Minh', '0987654323', 10.8231, 106.6297),
('Lê Văn Z', 'nhanvien3', '123456', 4, 1, 'nhanvien3@anyen.vn', 'Đà Nẵng', '0987654324', 16.0544, 108.2022),
('Phạm Thị T', 'nhanvien4', '123456', 5, 1, 'nhanvien4@anyen.vn', 'Hà Nội', '0987654325', 21.0285, 105.8542);

-- Insert sample data into khachhang
INSERT INTO khachhang (tenKhachHang, cccd, DiaChi, email, soDienThoai, MaNhanVienPhuTrach, NgayDangKy, NguonDangKy, NhuCauHoTro, GhiChu, Latitude, Longitude) VALUES
('Phạm Văn M', '001234567890', '123 Nguyễn Trãi, Hà Nội', 'phamvanm@gmail.com', '0911223344', 2, NOW(), 'Website', 'Tư vấn gói tang lễ trọn gói', NULL, 21.0285, 105.8542),
('Hoàng Thị N', '001234567891', '456 Lê Lợi, TP. Hồ Chí Minh', 'hoangthin@gmail.com', '0912233445', 3, NOW(), 'Facebook', 'Dịch vụ hỏa táng', NULL, 10.8231, 106.6297),
('Đỗ Văn P', '001234567892', '789 Trần Phú, Đà Nẵng', 'dovanp@gmail.com', '0912344556', 4, NOW(), 'Zalo', 'Mua quan tài', NULL, 16.0544, 108.2022),
('Vũ Thị Q', '001234567893', '321 Hai Bà Trưng, Hà Nội', 'vuthiq@gmail.com', '0913455667', 2, NOW(), 'Hotline', 'Dịch vụ mai táng', NULL, 21.0285, 105.8542);

-- Insert sample data into sanpham
INSERT INTO sanpham (tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien, MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai, mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX) VALUES
('Quan tài gỗ sồi', 'Quan tài', 'Gỗ sồi', '2m x 0.6m x 0.5m', 'Phật giáo', 15000000.00, 1, 10, 'Truyền thống', 'Việt Nam', 'Quan tài gỗ sồi cao cấp', 0, 'Nâu', '/images/quan-tai-go-soi.jpg', 'Gỗ sồi', 1, '200x60x50 cm', '50kg', 'QT001'),
('Quan tài gỗ mun', 'Quan tài', 'Gỗ mun', '2m x 0.6m x 0.5m', 'Phật giáo', 25000000.00, 1, 5, 'Cao cấp', 'Việt Nam', 'Quan tài gỗ mun quý hiếm', 0, 'Đen', '/images/quan-tai-go-mun.jpg', 'Gỗ mun', 1, '200x60x50 cm', '60kg', 'QT002'),
('Lọ hoa bằng gốm', 'Lọ hoa', 'Gốm sứ', '30cm cao', 'Phật giáo', 500000.00, 2, 50, 'Truyền thống', 'Việt Nam', 'Lọ hoa gốm Bát Tràng', 50000.00, 'Trắng', '/images/lo-hoa-gom.jpg', 'Gốm', 1, '30cm', '2kg', 'LH001'),
('Nến hương', 'Nến', 'Paraffin', '20cm', 'Phật giáo', 100000.00, 2, 100, 'Truyền thống', 'Việt Nam', 'Nến hương cao cấp', 0, 'Vàng', '/images/nen-huong.jpg', 'Paraffin', 1, '20cm', '0.5kg', 'NH001'),
('Vải liệm trắng', 'Vải liệm', 'Vải lụa', '3m x 2m', 'Phật giáo', 800000.00, 3, 30, 'Truyền thống', 'Trung Quốc', 'Vải liệm lụa trắng', 0, 'Trắng', '/images/vai-liem.jpg', 'Lụa', 1, '300x200 cm', '1kg', 'VL001'),
('Bộ quần áo tang', 'Quần áo', 'Vải cotton', 'L, XL', 'Phật giáo', 300000.00, 3, 50, 'Truyền thống', 'Việt Nam', 'Bộ quần áo tang lễ', 0, 'Trắng', '/images/quan-ao-tang.jpg', 'Cotton', 1, 'L, XL', '0.5kg', 'QA001');

-- Insert sample data into sanphamchitiet
INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
(1, 'Khung', 'Khung quan tài làm từ gỗ sồi 100%', 1, NOW(), NOW()),
(1, 'Nắp', 'Nắp quan tài có thể mở đóng', 2, NOW(), NOW()),
(1, 'Lót', 'Lót bên trong bằng vải nhung', 3, NOW(), NOW()),
(2, 'Khung', 'Khung quan tài làm từ gỗ mun quý hiếm', 1, NOW(), NOW()),
(2, 'Nắp', 'Nắp quan tài chạm khắc hoa văn', 2, NOW(), NOW()),
(2, 'Lót', 'Lót bên trong bằng lụa tơ tằm', 3, NOW(), NOW());

-- Insert sample data into sanphamhinhanh
INSERT INTO sanphamhinhanh (MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt) VALUES
(1, 1, 'Chính', '/images/quan-tai-soi-1.jpg', 1, NOW()),
(1, 1, 'Chi tiết', '/images/quan-tai-soi-detail-1.jpg', 2, NOW()),
(1, 2, 'Chính', '/images/quan-tai-soi-2.jpg', 3, NOW()),
(2, 1, 'Chính', '/images/quan-tai-mun-1.jpg', 1, NOW()),
(2, 1, 'Chi tiết', '/images/quan-tai-mun-detail-1.jpg', 2, NOW()),
(3, NULL, 'Chính', '/images/lo-hoa-gom-1.jpg', 1, NOW());

-- Insert sample data into combo
INSERT INTO combo (TenCombo, Gia, MoTa, HinhAnh, TrangThai) VALUES
('Gói tang lễ cơ bản', 20000000.00, 'Bao gồm: quan tài gỗ sồi, lọ hoa, nến hương, vải liệm', '/images/combo-co-ban.jpg', 1),
('Gói tang lễ cao cấp', 45000000.00, 'Bao gồm: quan tài gỗ mun, lọ hoa gốm, nến hương, vải liệm lụa, bộ quần áo tang', '/images/combo-cao-cap.jpg', 1),
('Gói hỏa táng', 15000000.00, 'Bao gồm: dịch vụ hỏa táng, lọ hoa, nến hương', '/images/combo-hoa-tang.jpg', 1);

-- Insert sample data into combochitiet
INSERT INTO combochitiet (ComboChiTietId, MaSanPham, ComboID, Loai, NoiDung) VALUES
(1, 1, 1, 0, 'Quan tài gỗ sồi'),
(2, 3, 1, 0, 'Lọ hoa bằng gốm'),
(3, 4, 1, 0, 'Nến hương'),
(4, 5, 1, 0, 'Vải liệm trắng'),
(5, 2, 2, 0, 'Quan tài gỗ mun'),
(6, 3, 2, 0, 'Lọ hoa bằng gốm'),
(7, 4, 2, 0, 'Nến hương'),
(8, 5, 2, 0, 'Vải liệm trắng'),
(9, 6, 2, 0, 'Bộ quần áo tang');

-- Insert sample data into donhang
INSERT INTO donhang (MaKhachHang, MaNhanVien, NgayTaoDon, tongTien, TrangThai, GhiChu, PhuongThucThanhToan, TrangThaiThanhToan, LyDoHuy) VALUES
(1, 2, CURDATE(), 20000000.00, 3, 'Khách hàng cần gấp', 2, 1, NULL),
(2, 3, CURDATE(), 45000000.00, 4, 'Đang xử lý', 1, 0, NULL),
(3, 4, CURDATE() - INTERVAL 2 DAY, 15000000.00, 6, 'Đã hoàn thành', 2, 1, NULL),
(4, 2, CURDATE() - INTERVAL 5 DAY, 20000000.00, 7, 'Khách hủy đơn', 0, 0, 'Khách thay đổi ý định');

-- Insert sample data into chitietdonhang
INSERT INTO chitietdonhang (MaDonHang, MaSanPham, SoLuong, giaTien) VALUES
(1, 1, 1, 15000000.00),
(1, 3, 2, 1000000.00),
(1, 4, 10, 1000000.00),
(1, 5, 1, 800000.00),
(2, 2, 1, 25000000.00),
(2, 3, 4, 2000000.00),
(2, 4, 20, 2000000.00),
(2, 5, 1, 800000.00),
(2, 6, 2, 600000.00),
(3, 1, 1, 15000000.00),
(4, 1, 1, 15000000.00);

-- Insert sample data into hoadon
INSERT INTO hoadon (MaDonHang, NgayIn, TongTien, PhuongThucThanhToan, TrangThai, CreatedAt) VALUES
(1, CURDATE(), 20000000.00, 2, 1, NOW()),
(3, CURDATE() - INTERVAL 2 DAY, 15000000.00, 2, 1, NOW() - INTERVAL 2 DAY);

-- Insert sample data into hopdong
INSERT INTO hopdong (MaDonHang, NgayKyHD, NgayViet, ThoiHanKetThuc, TrangThai, An) VALUES
(1, CURDATE(), CURDATE(), CURDATE() + INTERVAL 30 DAY, 'Đang hiệu lực', FALSE),
(3, CURDATE() - INTERVAL 2 DAY, CURDATE() - INTERVAL 2 DAY, CURDATE() + INTERVAL 28 DAY, 'Đã hoàn thành', TRUE);

-- Insert sample data into hdongct
INSERT INTO hdongct (MaHopDong, HoTenNguoiMat, NgayMat, NgaySinh, GioiTinh, SoGiayBaoTu, NoiCapGiayBaoTu, CoSoMaiTang, KhuMo, SoMo, NgayGioAnTang) VALUES
(1, 'Phạm Văn K', CURDATE() - INTERVAL 1 DAY, '1950-05-15', 'Nam', 'BG123456', 'UBND Quận Hoàn Kiếm', 'Nghĩa trang Văn Điển', 'Khu A', 'A123', CURDATE() + INTERVAL 1 DAY),
(2, 'Hoàng Thị L', CURDATE() - INTERVAL 3 DAY, '1955-08-20', 'Nữ', 'BG234567', 'UBND Quận 1', 'Nghĩa trang Bình Hưng Hòa', 'Khu B', 'B456', CURDATE() - INTERVAL 2 DAY);

-- Insert sample data into congno
INSERT INTO congno (MaDonHang, MaDoiTac, TongTien, DaThanhToan, ConLai, HanThanhToan, TrangThai, GhiChu, CreatedAt, UpdatedAt) VALUES
(1, 1, 20000000.00, 10000000.00, 10000000.00, CURDATE() + INTERVAL 15 DAY, 1, 'Thanh toán 50%', NOW(), NOW()),
(2, 1, 45000000.00, 0.00, 45000000.00, CURDATE() + INTERVAL 30 DAY, 0, 'Chưa thanh toán', NOW(), NOW()),
(3, 2, 15000000.00, 15000000.00, 0.00, CURDATE() - INTERVAL 2 DAY, 2, 'Đã thanh toán đủ', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);

-- Insert sample data into lichsucongno
INSERT INTO lichsucongno (MaCongNo, SoTienThanhToan, PhuongThucThanhToan, MaNhanVien, NgayThanhToan, MaGiaoDich, GhiChu) VALUES
(1, 10000000.00, 2, 5, NOW(), 'GD202401120001', 'Thanh toán đợt 1'),
(3, 15000000.00, 2, 5, NOW() - INTERVAL 2 DAY, 'GD202401100001', 'Thanh toán đầy đủ');

-- Insert sample data into thongbao
INSERT INTO thongbao (TieuDe, NoiDung, LoaiThongBao, NguoiGuiId, NguoiNhanId, MaKhachHang, TrangThai, LyDoTuChoi, NgayTao, NgayCapNhat) VALUES
('Khách hàng mới cần tư vấn', 'Khách hàng Phạm Văn M cần tư vấn gói tang lễ trọn gói', 'CONG_VIEC', 4, 2, 1, 0, NULL, NOW(), NOW()),
('Hệ thống bảo trì', 'Hệ thống sẽ bảo trì vào ngày 15/01/2024', 'HE_THONG', NULL, NULL, NULL, 1, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
('Yêu cầu từ chối', 'Nhân viên từ chối nhận công việc', 'TU_CHOI', 2, 4, 1, 3, 'Đang bận công việc khác', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
('Duyệt sản phẩm mới', 'Đối tác Nguyễn Văn A đăng ký sản phẩm mới cần duyệt', 'DUYET_SAN_PHAM', NULL, 1, NULL, 4, NULL, NOW(), NOW());

-- Insert sample data into thongbaodoitac
INSERT INTO thongbaodoitac (MaDoiTac, MaDonHang, Loai, TieuDe, NoiDung, TrangThaiThongBao, LyDoTuChoi, DaDoc, ThoiGianTao, ThoiGianXuLy) VALUES
(1, 1, 'DON_HANG', 'Đơn hàng mới #DH001', 'Bạn có đơn hàng mới cần xác nhận', 'DA_CHAP_NHAN', NULL, TRUE, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(1, 2, 'DON_HANG', 'Đơn hàng mới #DH002', 'Bạn có đơn hàng mới cần xác nhận', 'CHO_XAC_NHAN', NULL, FALSE, NOW(), NULL),
(2, 3, 'DON_HANG', 'Đơn hàng mới #DH003', 'Bạn có đơn hàng mới cần xác nhận', 'DA_CHAP_NHAN', NULL, TRUE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(3, NULL, 'DUYET_SAN_PHAM', 'Duyệt sản phẩm mới', 'Sản phẩm [MASP:7] cần được duyệt', 'CHO_XAC_NHAN', NULL, FALSE, NOW(), NULL);

-- ============================================
-- Summary
-- ============================================
-- Total Tables: 18
-- User Management: doitac, nhanvien, khachhang
-- Product Management: sanpham, sanphamchitiet, sanphamhinhanh, combo, combochitiet, combochitiet_hinhanh
-- Order Management: donhang, chitietdonhang, hoadon, hopdong, hdongct
-- Debt Management: congno, lichsucongno
-- Notification Management: thongbao, thongbaodoitac
