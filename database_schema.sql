-- Database Schema for Project An Yên
-- Generated from Entity Classes

-- ============================================
-- TABLES CƠ BẢN (Basic Tables)
-- ============================================

-- Table: tintuc (Tin tức)
CREATE TABLE tintuc (
    MaTinTuc INT AUTO_INCREMENT PRIMARY KEY,
    TieuDe VARCHAR(150) NOT NULL,
    TomTat VARCHAR(500) NOT NULL,
    NoiDung LONGTEXT,
    AnhDaiDien VARCHAR(255),
    LoaiTin INT NOT NULL,
    TrangThai INT NOT NULL DEFAULT 1,
    NgayDang DATETIME,
    CreatedAt DATETIME,
    UpdatedAt DATETIME,
    INDEX idx_tintuc_trangthai (TrangThai),
    INDEX idx_tintuc_ngaydang (NgayDang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: sanphamchitiet (Chi tiết sản phẩm)
CREATE TABLE sanphamchitiet (
    MaChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaSanPham INT NOT NULL,
    LoaiKhoi VARCHAR(255),
    NoiDung TEXT,
    ThuTu INT,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham) ON DELETE CASCADE,
    INDEX idx_spct_masp (MaSanPham)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: sanphamhinhanh (Hình ảnh sản phẩm)
CREATE TABLE sanphamhinhanh (
    MaHinhAnh INT AUTO_INCREMENT PRIMARY KEY,
    MaSanPham INT NOT NULL,
    MaChiTiet INT,
    LoaiHinhAnh VARCHAR(255),
    UrlHinhAnh TEXT,
    ThuTu INT,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham) ON DELETE CASCADE,
    FOREIGN KEY (MaChiTiet) REFERENCES sanphamchitiet(MaChiTiet) ON DELETE SET NULL,
    INDEX idx_spha_masp (MaSanPham),
    INDEX idx_spha_mact (MaChiTiet)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: combochitiet_hinhanh (Hình ảnh chi tiết combo)
CREATE TABLE combochitiet_hinhanh (
    MaHinhAnh INT AUTO_INCREMENT PRIMARY KEY,
    ComboChiTietId INT NOT NULL,
    TenHinhAnh VARCHAR(255),
    HinhAnh TEXT,
    ThuTu INT,
    FOREIGN KEY (ComboChiTietId) REFERENCES combochitiet(ComboChiTietId) ON DELETE CASCADE,
    INDEX idx_cbctha_cbctid (ComboChiTietId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: combo_hinhanh (Hình ảnh combo)
CREATE TABLE combo_hinhanh (
    MaHinhAnh INT AUTO_INCREMENT PRIMARY KEY,
    ComboId INT NOT NULL,
    HinhAnh VARCHAR(500) NOT NULL,
    TenHinhAnh VARCHAR(255),
    ThuTu INT NOT NULL,
    FOREIGN KEY (ComboId) REFERENCES combo(ComboId) ON DELETE CASCADE,
    INDEX idx_cbha_cbid (ComboId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLES TRUNG GIAN (Intermediate Tables)
-- ============================================

-- Table: combochitiet (Chi tiết combo)
CREATE TABLE combochitiet (
    ComboChiTietId INT AUTO_INCREMENT PRIMARY KEY,
    MaSanPham INT,
    ComboID INT NOT NULL,
    Loai INT DEFAULT 0,
    SoLuong INT NOT NULL DEFAULT 1,
    NoiDung TEXT,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham) ON DELETE CASCADE,
    FOREIGN KEY (ComboID) REFERENCES combo(ComboId) ON DELETE CASCADE,
    INDEX idx_cbct_masp (MaSanPham),
    INDEX idx_cbct_cbid (ComboID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: chitietdonhang (Chi tiết đơn hàng)
CREATE TABLE chitietdonhang (
    MaDonHangChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT NOT NULL,
    giaTien DECIMAL(18,2) NOT NULL,
    TrangThaiDoiTac INT DEFAULT 0,
    NgayGiaoDuKien DATE,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang) ON DELETE CASCADE,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham) ON DELETE RESTRICT,
    INDEX idx_ctdh_madh (MaDonHang),
    INDEX idx_ctdh_masp (MaSanPham)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: hdongct (Chi tiết hợp đồng)
CREATE TABLE hdongct (
    MaHDongCT INT AUTO_INCREMENT PRIMARY KEY,
    MaHopDong INT NOT NULL,
    HoTenNguoiMat VARCHAR(255),
    NgayMat DATE,
    NgaySinh DATE,
    GioiTinh BOOLEAN,
    SoGiayBaoTu VARCHAR(255),
    NoiCapGiayBaoTu VARCHAR(255),
    CoSoMaiTang VARCHAR(255),
    KhuMo VARCHAR(255),
    SoMo VARCHAR(255),
    NgayGioAnTang DATETIME,
    FOREIGN KEY (MaHopDong) REFERENCES hopdong(MaHopDong) ON DELETE CASCADE,
    INDEX idx_hdct_mahd (MaHopDong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: lichsucongno (Lịch sử công nợ)
CREATE TABLE lichsucongno (
    MaLichSuCongNo INT AUTO_INCREMENT PRIMARY KEY,
    MaCongNo INT NOT NULL,
    SoTienThanhToan DECIMAL(18,2) NOT NULL,
    PhuongThucThanhToan INT NOT NULL,
    MaNhanVien INT,
    NgayThanhToan DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MaGiaoDich VARCHAR(100),
    GhiChu TEXT,
    FOREIGN KEY (MaCongNo) REFERENCES congno(MaCongNo) ON DELETE CASCADE,
    FOREIGN KEY (MaNhanVien) REFERENCES nhanvien(MaNhanVien) ON DELETE SET NULL,
    INDEX idx_lscn_macn (MaCongNo),
    INDEX idx_lscn_manv (MaNhanVien),
    INDEX idx_lscv_ngaytt (NgayThanhToan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLES QUAN TRỌNG (Important Tables)
-- ============================================

-- Table: thongbaodoitac (Thông báo đối tác)
CREATE TABLE thongbaodoitac (
    MaThongBao INT AUTO_INCREMENT PRIMARY KEY,
    MaDoiTac INT NOT NULL,
    MaDonHang INT,
    Loai VARCHAR(50) DEFAULT 'DON_HANG',
    TieuDe VARCHAR(255),
    NoiDung TEXT,
    TrangThaiThongBao INT DEFAULT 0,
    LyDoTuChoi TEXT,
    DaDoc BOOLEAN DEFAULT FALSE,
    ThoiGianTao DATETIME,
    ThoiGianXuLy DATETIME,
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac) ON DELETE CASCADE,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang) ON DELETE SET NULL,
    INDEX idx_tbdt_madt (MaDoiTac),
    INDEX idx_tbdt_madh (MaDonHang),
    INDEX idx_tbdt_trangthai (TrangThaiThongBao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: thongbao (Thông báo nhân viên)
CREATE TABLE thongbao (
    MaThongBao INT AUTO_INCREMENT PRIMARY KEY,
    TieuDe VARCHAR(255),
    NoiDung TEXT,
    LoaiThongBao VARCHAR(50),
    NguoiGuiId INT,
    NguoiNhanId INT,
    MaKhachHang INT,
    MaSanPham INT,
    TrangThai INT DEFAULT 0,
    DaDoc BOOLEAN DEFAULT FALSE,
    LyDoTuChoi TEXT,
    NgayTao DATETIME,
    NgayCapNhat DATETIME,
    FOREIGN KEY (NguoiGuiId) REFERENCES nhanvien(MaNhanVien) ON DELETE SET NULL,
    FOREIGN KEY (NguoiNhanId) REFERENCES nhanvien(MaNhanVien) ON DELETE CASCADE,
    FOREIGN KEY (MaKhachHang) REFERENCES khachhang(MaKhachHang) ON DELETE SET NULL,
    FOREIGN KEY (MaSanPham) REFERENCES sanpham(MaSanPham) ON DELETE SET NULL,
    INDEX idx_tb_nguoinhan (NguoiNhanId),
    INDEX idx_tb_makh (MaKhachHang),
    INDEX idx_tb_masp (MaSanPham),
    INDEX idx_tb_trangthai (TrangThai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: khachhang (Khách hàng)
CREATE TABLE khachhang (
    MaKhachHang INT AUTO_INCREMENT PRIMARY KEY,
    tenKhachHang VARCHAR(255),
    cccd VARCHAR(20),
    DiaChi VARCHAR(500),
    email VARCHAR(255),
    soDienThoai VARCHAR(20),
    MaNhanVienPhuTrach INT,
    NgayDangKy DATETIME,
    NguonDangKy VARCHAR(100),
    NhuCauHoTro TEXT,
    GhiChu TEXT,
    Latitude DECIMAL(10,8),
    Longitude DECIMAL(11,8),
    FOREIGN KEY (MaNhanVienPhuTrach) REFERENCES nhanvien(MaNhanVien) ON DELETE SET NULL,
    INDEX idx_kh_manv (MaNhanVienPhuTrach),
    INDEX idx_kh_sdt (soDienThoai),
    INDEX idx_kh_cccd (cccd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: doitac (Đối tác)
CREATE TABLE doitac (
    MaDoiTac INT AUTO_INCREMENT PRIMARY KEY,
    TenDoiTac VARCHAR(255),
    TenDoanhNghiep VARCHAR(255),
    MaSoThue VARCHAR(50),
    TenDangNhap VARCHAR(100) UNIQUE,
    MatKhau VARCHAR(255),
    Email VARCHAR(255),
    SoDienThoai VARCHAR(20),
    DiaChi VARCHAR(500),
    TrangThai INT DEFAULT 2,
    ConfirmationToken VARCHAR(255),
    CreatedAt DATETIME,
    UpdatedAt DATETIME,
    INDEX idx_dt_trangthai (TrangThai),
    INDEX idx_dt_tendn (TenDangNhap)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: nhanvien (Nhân viên)
CREATE TABLE nhanvien (
    MaNhanVien INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(255),
    TenDangNhap VARCHAR(100) UNIQUE,
    MatKhau VARCHAR(255),
    VaiTro INT,
    TrangThai INT DEFAULT 1,
    Email VARCHAR(255),
    DiaChi VARCHAR(500),
    SoDienThoai VARCHAR(20),
    Latitude DECIMAL(10,8),
    Longitude DECIMAL(11,8),
    INDEX idx_nv_vaitro (VaiTro),
    INDEX idx_nv_trangthai (TrangThai),
    INDEX idx_nv_tendn (TenDangNhap)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLES CỐT LÕI (Core Tables)
-- ============================================

-- Table: combo (Combo sản phẩm)
CREATE TABLE combo (
    ComboId INT AUTO_INCREMENT PRIMARY KEY,
    TenCombo VARCHAR(255),
    Gia DECIMAL(18,2),
    MoTa TEXT,
    HinhAnh VARCHAR(500),
    MaDoiTac INT NOT NULL,
    TrangThai INT DEFAULT 1,
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac) ON DELETE CASCADE,
    INDEX idx_cb_madt (MaDoiTac),
    INDEX idx_cb_trangthai (TrangThai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: sanpham (Sản phẩm)
CREATE TABLE sanpham (
    MaSanPham INT AUTO_INCREMENT PRIMARY KEY,
    tenSanPham VARCHAR(255) NOT NULL,
    loai VARCHAR(100),
    noiThat VARCHAR(255),
    quyCach VARCHAR(255),
    tonGiao VARCHAR(100),
    giaTien DECIMAL(18,2),
    MaDoiTac INT,
    SoLuong INT,
    thietKe VARCHAR(255),
    xuatXu VARCHAR(255),
    GhiChu TEXT,
    khuyenMai DECIMAL(18,2),
    mauSac VARCHAR(100),
    HinhAnh VARCHAR(500),
    vatLieu VARCHAR(100),
    trangThai INT DEFAULT 1,
    kichThuoc VARCHAR(100),
    trongLuong VARCHAR(100),
    CNSX VARCHAR(100),
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac) ON DELETE SET NULL,
    INDEX idx_sp_madt (MaDoiTac),
    INDEX idx_sp_trangthai (trangThai),
    INDEX idx_sp_loai (loai),
    INDEX idx_sp_tongiao (tonGiao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: phientuvan (Phiên tư vấn)
CREATE TABLE phientuvan (
    MaPhien BIGINT AUTO_INCREMENT PRIMARY KEY,
    TokenPhien VARCHAR(64) NOT NULL UNIQUE,
    TenKhachHang VARCHAR(100) NOT NULL,
    MaNhanVien INT,
    TrangThai INT DEFAULT 0,
    TinNhanCuoi VARCHAR(500),
    ThoiGianTinNhanCuoi DATETIME NOT NULL,
    SoTinNhanChuaDocNhanVien INT DEFAULT 0,
    SoTinNhanChuaDocKhach INT DEFAULT 0,
    CreatedAt DATETIME NOT NULL,
    UpdatedAt DATETIME NOT NULL,
    HetHanLuc DATETIME NOT NULL,
    FOREIGN KEY (MaNhanVien) REFERENCES nhanvien(MaNhanVien) ON DELETE SET NULL,
    INDEX idx_ptv_token (TokenPhien),
    INDEX idx_ptv_manv (MaNhanVien),
    INDEX idx_ptv_trangthai (TrangThai),
    INDEX idx_ptv_hethanluc (HetHanLuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: tinnhantuvan (Tin nhắn tư vấn)
CREATE TABLE tinnhantuvan (
    MaTinNhan BIGINT AUTO_INCREMENT PRIMARY KEY,
    MaPhien BIGINT NOT NULL,
    NguoiGui VARCHAR(20) NOT NULL,
    MaNhanVien INT,
    NoiDung VARCHAR(2000) NOT NULL,
    DaDoc BOOLEAN NOT NULL,
    CreatedAt DATETIME NOT NULL,
    FOREIGN KEY (MaPhien) REFERENCES phientuvan(MaPhien) ON DELETE CASCADE,
    FOREIGN KEY (MaNhanVien) REFERENCES nhanvien(MaNhanVien) ON DELETE SET NULL,
    INDEX idx_tntv_maphien (MaPhien),
    INDEX idx_tntv_nguoigui (NguoiGui),
    INDEX idx_tntv_createdat (CreatedAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: yeucautuvanai (Yêu cầu tư vấn AI)
CREATE TABLE yeucautuvanai (
    MaYeuCau BIGINT AUTO_INCREMENT PRIMARY KEY,
    MaPhien BIGINT NOT NULL UNIQUE,
    HoTen VARCHAR(150),
    SoDienThoai VARCHAR(20),
    DiaChi VARCHAR(500),
    NhuCau TEXT,
    ThoiGianMongMuon VARCHAR(255),
    NganSachDuKien DECIMAL(18,2),
    TongTienThamKhao DECIMAL(18,2),
    GhiChu TEXT,
    TrangThai INT NOT NULL DEFAULT 0,
    DaXacNhan BOOLEAN NOT NULL DEFAULT FALSE,
    DaGuiHotline BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt DATETIME NOT NULL,
    UpdatedAt DATETIME NOT NULL,
    FOREIGN KEY (MaPhien) REFERENCES phientuvan(MaPhien) ON DELETE CASCADE,
    INDEX idx_yctv_maphien (MaPhien),
    INDEX idx_yctv_daxacnhan (DaXacNhan),
    INDEX idx_yctv_daguihotline (DaGuiHotline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: donhang (Đơn hàng)
CREATE TABLE donhang (
    MaDonHang INT AUTO_INCREMENT PRIMARY KEY,
    MaKhachHang INT,
    MaNhanVien INT,
    NgayTaoDon DATE,
    NgayGiaoDuKien DATE,
    tongTien DECIMAL(18,2),
    TrangThai INT DEFAULT 1,
    GhiChu TEXT,
    PhuongThucThanhToan INT DEFAULT 0,
    TrangThaiThanhToan INT DEFAULT 0,
    LyDoHuy TEXT,
    TrangThaiTruocSuCo INT,
    LyDoSuCo TEXT,
    NguoiBaoCaoSuCo VARCHAR(255),
    FOREIGN KEY (MaKhachHang) REFERENCES khachhang(MaKhachHang) ON DELETE SET NULL,
    FOREIGN KEY (MaNhanVien) REFERENCES nhanvien(MaNhanVien) ON DELETE SET NULL,
    INDEX idx_dh_makh (MaKhachHang),
    INDEX idx_dh_manv (MaNhanVien),
    INDEX idx_dh_trangthai (TrangThai),
    INDEX idx_dh_ngaytaodon (NgayTaoDon)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: hopdong (Hợp đồng)
CREATE TABLE hopdong (
    MaHopDong INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    NgayKyHD DATE,
    NgayViet DATE,
    ThoiHanKetThuc DATE,
    TrangThai INT DEFAULT 0,
    An BOOLEAN,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang) ON DELETE CASCADE,
    UNIQUE KEY uk_hopdong_madh (MaDonHang),
    INDEX idx_hd_trangthai (TrangThai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: congno (Công nợ)
CREATE TABLE congno (
    MaCongNo INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    MaDoiTac INT NOT NULL,
    TongTien DECIMAL(18,2) NOT NULL,
    DaThanhToan DECIMAL(18,2) NOT NULL,
    ConLai DECIMAL(18,2) NOT NULL,
    HanThanhToan DATE,
    TrangThai INT NOT NULL,
    GhiChu TEXT,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang) ON DELETE CASCADE,
    FOREIGN KEY (MaDoiTac) REFERENCES doitac(MaDoiTac) ON DELETE CASCADE,
    INDEX idx_cn_madh (MaDonHang),
    INDEX idx_cn_madt (MaDoiTac),
    INDEX idx_cn_trangthai (TrangThai),
    INDEX idx_cn_hanthanhtoan (HanThanhToan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE QUAN TRỌNG NHẤT (Most Important Table)
-- ============================================

-- Table: hoadon (Hóa đơn)
CREATE TABLE hoadon (
    MaHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT,
    NgayIn DATE,
    TongTien DECIMAL(18,2),
    PhuongThucThanhToan INT DEFAULT 0,
    TrangThai INT DEFAULT 1,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (MaDonHang) REFERENCES donhang(MaDonHang) ON DELETE SET NULL,
    INDEX idx_hd_madh (MaDonHang),
    INDEX idx_hd_ngayin (NgayIn),
    INDEX idx_hd_trangthai (TrangThai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- INDEXES BỔ SUNG CHO TỐI ƯU HIỆU NĂNG
-- ============================================

-- Composite indexes for common queries
CREATE INDEX idx_dh_makh_trangthai ON donhang(MaKhachHang, TrangThai);
CREATE INDEX idx_dh_manv_trangthai ON donhang(MaNhanVien, TrangThai);
CREATE INDEX idx_sp_madt_trangthai ON sanpham(MaDoiTac, trangThai);
CREATE INDEX idx_cb_madt_trangthai ON combo(MaDoiTac, TrangThai);
CREATE INDEX idx_tb_nguoinhan_trangthai ON thongbao(NguoiNhanId, TrangThai);
CREATE INDEX idx_tbdt_madt_trangthai ON thongbaodoitac(MaDoiTac, TrangThaiThongBao);
CREATE INDEX idx_cn_madt_trangthai ON congno(MaDoiTac, TrangThai);
