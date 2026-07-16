-- Schema only: CREATE TABLE statements extracted from Dump20260715.sql
-- Database: dich_vu_mai_tang

CREATE DATABASE IF NOT EXISTS `dich_vu_mai_tang`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `dich_vu_mai_tang`;

SET FOREIGN_KEY_CHECKS = 0;

-- Table: `chitietdonhang`
DROP TABLE IF EXISTS `chitietdonhang`;
CREATE TABLE `chitietdonhang` (
                                  `MaDonHangChiTiet` int NOT NULL AUTO_INCREMENT,
                                  `MaDonHang` int DEFAULT NULL,
                                  `MaSanPham` int DEFAULT NULL,
                                  `SoLuong` int DEFAULT NULL,
                                  `giaTien` decimal(18,2) DEFAULT NULL,
                                  PRIMARY KEY (`MaDonHangChiTiet`),
                                  KEY `MaDonHang` (`MaDonHang`),
                                  KEY `MaSanPham` (`MaSanPham`),
                                  CONSTRAINT `chitietdonhang_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`),
                                  CONSTRAINT `chitietdonhang_ibfk_2` FOREIGN KEY (`MaSanPham`) REFERENCES `sanpham` (`MaSanPham`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `combo`
DROP TABLE IF EXISTS `combo`;
CREATE TABLE `combo` (
                         `ComboId` int NOT NULL AUTO_INCREMENT,
                         `TenCombo` varchar(255) DEFAULT NULL,
                         `Gia` decimal(18,2) DEFAULT NULL,
                         `MoTa` text,
                         `HinhAnh` varchar(500) DEFAULT NULL,
                         `TrangThai` int DEFAULT NULL,
                         PRIMARY KEY (`ComboId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `combochitiet`
DROP TABLE IF EXISTS `combochitiet`;
CREATE TABLE `combochitiet` (
                                `ComboChiTietId` int NOT NULL,
                                `MaSanPham` int DEFAULT NULL,
                                `ComboID` int DEFAULT NULL,
                                `Loai` int DEFAULT NULL,
                                `NoiDung` text,
                                PRIMARY KEY (`ComboChiTietId`),
                                KEY `MaSanPham` (`MaSanPham`),
                                KEY `ComboID` (`ComboID`),
                                CONSTRAINT `combochitiet_ibfk_1` FOREIGN KEY (`MaSanPham`) REFERENCES `sanpham` (`MaSanPham`),
                                CONSTRAINT `combochitiet_ibfk_2` FOREIGN KEY (`ComboID`) REFERENCES `combo` (`ComboId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `combochitiet_hinhanh`
DROP TABLE IF EXISTS `combochitiet_hinhanh`;
CREATE TABLE `combochitiet_hinhanh` (
                                        `MaHinhAnh` int NOT NULL AUTO_INCREMENT,
                                        `ComboChiTietId` int DEFAULT NULL,
                                        `TenHinhAnh` varchar(255) DEFAULT NULL,
                                        `HinhAnh` varchar(500) DEFAULT NULL,
                                        `ThuTu` int DEFAULT NULL,
                                        PRIMARY KEY (`MaHinhAnh`),
                                        KEY `ComboChiTietId` (`ComboChiTietId`),
                                        CONSTRAINT `combochitiet_hinhanh_ibfk_1` FOREIGN KEY (`ComboChiTietId`) REFERENCES `combochitiet` (`ComboChiTietId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `congno`
DROP TABLE IF EXISTS `congno`;
CREATE TABLE `congno` (
                          `MaCongNo` int NOT NULL AUTO_INCREMENT,
                          `MaDonHang` int NOT NULL,
                          `MaDoiTac` int NOT NULL,
                          `TongTien` decimal(18,2) NOT NULL,
                          `DaThanhToan` decimal(18,2) NOT NULL,
                          `ConLai` decimal(18,2) NOT NULL,
                          `HanThanhToan` date DEFAULT NULL,
                          `TrangThai` int NOT NULL COMMENT '0=Chưa thanh toán, 1=Thanh toán một phần, 2=Đã thanh toán, 3=Quá hạn',
                          `GhiChu` text,
                          `CreatedAt` datetime DEFAULT NULL,
                          `UpdatedAt` datetime DEFAULT NULL,
                          PRIMARY KEY (`MaCongNo`),
                          KEY `idx_congno_madoitac` (`MaDoiTac`),
                          KEY `idx_congno_madonhang` (`MaDonHang`),
                          CONSTRAINT `congno_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`),
                          CONSTRAINT `congno_ibfk_2` FOREIGN KEY (`MaDoiTac`) REFERENCES `doitac` (`MaDoiTac`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `doitac`
DROP TABLE IF EXISTS `doitac`;
CREATE TABLE `doitac` (
                          `MaDoiTac` int NOT NULL AUTO_INCREMENT,
                          `TenDoiTac` varchar(255) DEFAULT NULL,
                          `TenDoanhNghiep` varchar(255) DEFAULT NULL,
                          `MaSoThue` varchar(50) DEFAULT NULL,
                          `TenDangNhap` varchar(255) DEFAULT NULL,
                          `MatKhau` varchar(255) DEFAULT NULL,
                          `Email` varchar(255) DEFAULT NULL,
                          `SoDienThoai` varchar(20) DEFAULT NULL,
                          `DiaChi` varchar(500) DEFAULT NULL,
                          `TrangThai` int DEFAULT '1' COMMENT '0=Ngừng hoạt động, 1=Đang hoạt động, 2=Chờ xác nhận, 3=Đã xóa',
                          `ConfirmationToken` varchar(255) DEFAULT NULL,
                          `CreatedAt` datetime DEFAULT NULL,
                          `UpdatedAt` datetime DEFAULT NULL,
                          PRIMARY KEY (`MaDoiTac`),
                          KEY `idx_doitac_trangthai` (`TrangThai`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `donhang`
DROP TABLE IF EXISTS `donhang`;
CREATE TABLE `donhang` (
                           `MaDonHang` int NOT NULL AUTO_INCREMENT,
                           `MaKhachHang` int DEFAULT NULL,
                           `MaNhanVien` int DEFAULT NULL,
                           `NgayTaoDon` date DEFAULT NULL,
                           `tongTien` decimal(18,2) DEFAULT NULL,
                           `TrangThai` int DEFAULT NULL COMMENT '1=Mới tạo, 2=Chờ đối tác xác nhận, 3=Đã xác nhận, 4=Đang xử lý, 5=Chờ thanh toán, 6=Hoàn thành, 7=Đã hủy, 8=Đối tác từ chối',
                           `GhiChu` text,
                           `PhuongThucThanhToan` int DEFAULT NULL COMMENT '0=Chưa chọn, 1=Tiền mặt, 2=Chuyển khoản',
                           `TrangThaiThanhToan` int DEFAULT NULL COMMENT '0=Chưa thanh toán, 1=Đã thanh toán, 2=Chờ xác nhận',
                           `LyDoHuy` text,
                           `AudioUrl` text,
                           PRIMARY KEY (`MaDonHang`),
                           KEY `idx_donhang_makhachhang` (`MaKhachHang`),
                           KEY `idx_donhang_manhanvien` (`MaNhanVien`),
                           KEY `idx_donhang_trangthai` (`TrangThai`),
                           CONSTRAINT `donhang_ibfk_1` FOREIGN KEY (`MaKhachHang`) REFERENCES `khachhang` (`MaKhachHang`),
                           CONSTRAINT `donhang_ibfk_2` FOREIGN KEY (`MaNhanVien`) REFERENCES `nhanvien` (`MaNhanVien`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `hdongct`
DROP TABLE IF EXISTS `hdongct`;
CREATE TABLE `hdongct` (
                           `MaHDongCT` int NOT NULL AUTO_INCREMENT,
                           `MaHopDong` int NOT NULL,
                           `HoTenNguoiMat` varchar(255) DEFAULT NULL,
                           `NgayMat` date DEFAULT NULL,
                           `NgaySinh` date DEFAULT NULL,
                           `GioiTinh` varchar(10) DEFAULT NULL,
                           `SoGiayBaoTu` varchar(100) DEFAULT NULL,
                           `NoiCapGiayBaoTu` varchar(255) DEFAULT NULL,
                           `CoSoMaiTang` varchar(255) DEFAULT NULL,
                           `KhuMo` varchar(100) DEFAULT NULL,
                           `SoMo` varchar(100) DEFAULT NULL,
                           `NgayGioAnTang` datetime DEFAULT NULL,
                           PRIMARY KEY (`MaHDongCT`),
                           KEY `MaHopDong` (`MaHopDong`),
                           CONSTRAINT `hdongct_ibfk_1` FOREIGN KEY (`MaHopDong`) REFERENCES `hopdong` (`MaHopDong`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `hoadon`
DROP TABLE IF EXISTS `hoadon`;
CREATE TABLE `hoadon` (
                          `MaHoaDon` int NOT NULL AUTO_INCREMENT,
                          `MaDonHang` int DEFAULT NULL,
                          `NgayIn` date DEFAULT NULL,
                          `TongTien` decimal(18,2) DEFAULT NULL,
                          `PhuongThucThanhToan` int DEFAULT NULL COMMENT '0=Chưa chọn, 1=Tiền mặt, 2=Chuyển khoản',
                          `TrangThai` int DEFAULT NULL COMMENT '0=Đã hủy, 1=Đã tạo',
                          `CreatedAt` datetime DEFAULT NULL,
                          PRIMARY KEY (`MaHoaDon`),
                          KEY `MaDonHang` (`MaDonHang`),
                          CONSTRAINT `hoadon_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `hopdong`
DROP TABLE IF EXISTS `hopdong`;
CREATE TABLE `hopdong` (
                           `MaHopDong` int NOT NULL AUTO_INCREMENT,
                           `MaDonHang` int NOT NULL,
                           `NgayKyHD` date DEFAULT NULL,
                           `NgayViet` date DEFAULT NULL,
                           `ThoiHanKetThuc` date DEFAULT NULL,
                           `TrangThai` varchar(50) DEFAULT NULL,
                           `An` tinyint(1) DEFAULT NULL,
                           PRIMARY KEY (`MaHopDong`),
                           KEY `MaDonHang` (`MaDonHang`),
                           CONSTRAINT `hopdong_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `khachhang`
DROP TABLE IF EXISTS `khachhang`;
CREATE TABLE `khachhang` (
                             `MaKhachHang` int NOT NULL AUTO_INCREMENT,
                             `tenKhachHang` varchar(255) DEFAULT NULL,
                             `cccd` varchar(50) DEFAULT NULL,
                             `DiaChi` varchar(500) DEFAULT NULL,
                             `email` varchar(255) DEFAULT NULL,
                             `soDienThoai` varchar(20) DEFAULT NULL,
                             `MaNhanVienPhuTrach` int DEFAULT NULL,
                             `NgayDangKy` datetime DEFAULT NULL,
                             `NguonDangKy` varchar(255) DEFAULT NULL,
                             `NhuCauHoTro` text,
                             `GhiChu` text,
                             `Latitude` decimal(10,8) DEFAULT NULL,
                             `Longitude` decimal(11,8) DEFAULT NULL,
                             `TinhThanh` varchar(100) DEFAULT NULL,
                             `QuanHuyen` varchar(100) DEFAULT NULL,
                             `PhuongXa` varchar(100) DEFAULT NULL,
                             `SoNhaDuong` varchar(255) DEFAULT NULL,
                             `DiaChiDayDu` varchar(500) DEFAULT NULL COMMENT 'Địa chỉ đầy đủ ghép từ các trường',
                             `CapNhatToaDoTai` datetime DEFAULT NULL COMMENT 'Thời điểm cập nhật tọa độ lần cuối',
                             PRIMARY KEY (`MaKhachHang`),
                             KEY `idx_lat_lng` (`Latitude`,`Longitude`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `lichsucongno`
DROP TABLE IF EXISTS `lichsucongno`;
CREATE TABLE `lichsucongno` (
                                `MaLichSuCongNo` int NOT NULL AUTO_INCREMENT,
                                `MaCongNo` int NOT NULL,
                                `SoTienThanhToan` decimal(18,2) NOT NULL,
                                `PhuongThucThanhToan` int NOT NULL COMMENT '1=Tiền mặt, 2=Chuyển khoản',
                                `MaNhanVien` int DEFAULT NULL,
                                `NgayThanhToan` datetime NOT NULL,
                                `MaGiaoDich` varchar(100) DEFAULT NULL,
                                `GhiChu` text,
                                PRIMARY KEY (`MaLichSuCongNo`),
                                KEY `MaCongNo` (`MaCongNo`),
                                KEY `MaNhanVien` (`MaNhanVien`),
                                CONSTRAINT `lichsucongno_ibfk_1` FOREIGN KEY (`MaCongNo`) REFERENCES `congno` (`MaCongNo`),
                                CONSTRAINT `lichsucongno_ibfk_2` FOREIGN KEY (`MaNhanVien`) REFERENCES `nhanvien` (`MaNhanVien`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `nhanvien`
DROP TABLE IF EXISTS `nhanvien`;
CREATE TABLE `nhanvien` (
                            `MaNhanVien` int NOT NULL AUTO_INCREMENT,
                            `HoTen` varchar(255) DEFAULT NULL,
                            `TenDangNhap` varchar(255) DEFAULT NULL,
                            `MatKhau` varchar(255) DEFAULT NULL,
                            `VaiTro` int DEFAULT NULL COMMENT '1=Admin, 2=Bán hàng, 3=Tư vấn, 4=Hotline, 5=Kế toán',
                            `TrangThai` int DEFAULT '1' COMMENT '0=Nghỉ việc, 1=Hoạt động',
                            `Email` varchar(255) DEFAULT NULL,
                            `DiaChi` varchar(500) DEFAULT NULL,
                            `SoDienThoai` varchar(20) DEFAULT NULL,
                            `Latitude` decimal(10,8) DEFAULT NULL,
                            `Longitude` decimal(11,8) DEFAULT NULL,
                            `TinhThanh` varchar(100) DEFAULT NULL,
                            `QuanHuyen` varchar(100) DEFAULT NULL,
                            `PhuongXa` varchar(100) DEFAULT NULL,
                            `SoNhaDuong` varchar(255) DEFAULT NULL,
                            `TrangThaiLamViec` varchar(20) DEFAULT 'RANH' COMMENT 'RANH=Rảnh, BAN=Bận, NGHI=Nghỉ',
                            `CapNhatToaDoTai` datetime DEFAULT NULL COMMENT 'Thời điểm cập nhật tọa độ lần cuối',
                            `DiaChiDayDu` varchar(500) DEFAULT NULL COMMENT 'Địa chỉ đầy đủ ghép từ các trường',
                            PRIMARY KEY (`MaNhanVien`),
                            KEY `idx_nhanvien_trangthai` (`TrangThai`),
                            KEY `idx_lat_lng` (`Latitude`,`Longitude`),
                            KEY `idx_trang_thai_lam_viec` (`TrangThaiLamViec`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `sanpham`
DROP TABLE IF EXISTS `sanpham`;
CREATE TABLE `sanpham` (
                           `MaSanPham` int NOT NULL AUTO_INCREMENT,
                           `tenSanPham` varchar(255) DEFAULT NULL,
                           `loai` varchar(100) DEFAULT NULL,
                           `noiThat` varchar(100) DEFAULT NULL,
                           `quyCach` varchar(255) DEFAULT NULL,
                           `tonGiao` varchar(100) DEFAULT NULL,
                           `giaTien` decimal(18,2) DEFAULT NULL,
                           `MaDoiTac` int DEFAULT NULL,
                           `SoLuong` int DEFAULT NULL,
                           `thietKe` varchar(255) DEFAULT NULL,
                           `xuatXu` varchar(255) DEFAULT NULL,
                           `GhiChu` text,
                           `khuyenMai` decimal(18,2) DEFAULT NULL,
                           `mauSac` varchar(100) DEFAULT NULL,
                           `HinhAnh` varchar(500) DEFAULT NULL,
                           `vatLieu` varchar(255) DEFAULT NULL,
                           `trangThai` int DEFAULT '1' COMMENT '0=Ẩn, 1=Đang bán, 2=Chờ xác nhận',
                           `kichThuoc` varchar(255) DEFAULT NULL,
                           `trongLuong` varchar(100) DEFAULT NULL,
                           `CNSX` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`MaSanPham`),
                           KEY `idx_sanpham_trangthai` (`trangThai`),
                           KEY `idx_sanpham_madoitac` (`MaDoiTac`),
                           CONSTRAINT `sanpham_ibfk_1` FOREIGN KEY (`MaDoiTac`) REFERENCES `doitac` (`MaDoiTac`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `sanphamchitiet`
DROP TABLE IF EXISTS `sanphamchitiet`;
CREATE TABLE `sanphamchitiet` (
                                  `MaChiTiet` int NOT NULL AUTO_INCREMENT,
                                  `MaSanPham` int DEFAULT NULL,
                                  `LoaiKhoi` varchar(100) DEFAULT NULL,
                                  `NoiDung` text,
                                  `ThuTu` int DEFAULT NULL,
                                  `CreatedAt` datetime DEFAULT NULL,
                                  `UpdatedAt` datetime DEFAULT NULL,
                                  PRIMARY KEY (`MaChiTiet`),
                                  KEY `MaSanPham` (`MaSanPham`),
                                  CONSTRAINT `sanphamchitiet_ibfk_1` FOREIGN KEY (`MaSanPham`) REFERENCES `sanpham` (`MaSanPham`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `sanphamhinhanh`
DROP TABLE IF EXISTS `sanphamhinhanh`;
CREATE TABLE `sanphamhinhanh` (
                                  `MaHinhAnh` int NOT NULL AUTO_INCREMENT,
                                  `MaSanPham` int DEFAULT NULL,
                                  `MaChiTiet` int DEFAULT NULL,
                                  `LoaiHinhAnh` varchar(100) DEFAULT NULL,
                                  `UrlHinhAnh` varchar(500) DEFAULT NULL,
                                  `ThuTu` int DEFAULT NULL,
                                  `CreatedAt` datetime DEFAULT NULL,
                                  PRIMARY KEY (`MaHinhAnh`),
                                  KEY `MaSanPham` (`MaSanPham`),
                                  KEY `MaChiTiet` (`MaChiTiet`),
                                  CONSTRAINT `sanphamhinhanh_ibfk_1` FOREIGN KEY (`MaSanPham`) REFERENCES `sanpham` (`MaSanPham`),
                                  CONSTRAINT `sanphamhinhanh_ibfk_2` FOREIGN KEY (`MaChiTiet`) REFERENCES `sanphamchitiet` (`MaChiTiet`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `thongbao`
DROP TABLE IF EXISTS `thongbao`;
CREATE TABLE `thongbao` (
                            `MaThongBao` int NOT NULL AUTO_INCREMENT,
                            `TieuDe` varchar(255) DEFAULT NULL,
                            `NoiDung` text,
                            `LoaiThongBao` varchar(50) DEFAULT NULL COMMENT 'CONG_VIEC, HE_THONG, TU_CHOI, DUYET_SAN_PHAM',
                            `NguoiGuiId` int DEFAULT NULL,
                            `NguoiNhanId` int DEFAULT NULL,
                            `MaKhachHang` int DEFAULT NULL,
                            `TrangThai` int DEFAULT '0' COMMENT '0=Chưa đọc, 1=Đã đọc, 2=Đã chấp nhận, 3=Đã từ chối, 4=Chờ xác nhận',
                            `LyDoTuChoi` text,
                            `NgayTao` datetime DEFAULT NULL,
                            `NgayCapNhat` datetime DEFAULT NULL,
                            PRIMARY KEY (`MaThongBao`),
                            KEY `idx_thongbao_nguoinhanid` (`NguoiNhanId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table: `thongbaodoitac`
DROP TABLE IF EXISTS `thongbaodoitac`;
CREATE TABLE `thongbaodoitac` (
                                  `MaThongBao` int NOT NULL AUTO_INCREMENT,
                                  `MaDoiTac` int DEFAULT NULL,
                                  `MaDonHang` int DEFAULT NULL,
                                  `Loai` varchar(50) DEFAULT 'DON_HANG' COMMENT 'DON_HANG, DUYET_SAN_PHAM',
                                  `TieuDe` varchar(255) DEFAULT NULL,
                                  `NoiDung` text,
                                  `TrangThaiThongBao` varchar(50) DEFAULT 'CHO_XAC_NHAN' COMMENT 'CHO_XAC_NHAN, DA_CHAP_NHAN, DA_TU_CHOI',
                                  `LyDoTuChoi` text,
                                  `DaDoc` tinyint(1) DEFAULT '0',
                                  `ThoiGianTao` datetime DEFAULT NULL,
                                  `ThoiGianXuLy` datetime DEFAULT NULL,
                                  PRIMARY KEY (`MaThongBao`),
                                  KEY `idx_thongbaodoitac_madoitac` (`MaDoiTac`),
                                  KEY `idx_thongbaodoitac_madonhang` (`MaDonHang`),
                                  CONSTRAINT `thongbaodoitac_ibfk_1` FOREIGN KEY (`MaDoiTac`) REFERENCES `doitac` (`MaDoiTac`),
                                  CONSTRAINT `thongbaodoitac_ibfk_2` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- DỮ LIỆU MẪU
-- Mật khẩu mẫu đang được lưu dưới dạng BCrypt theo file dump gốc.
-- =====================================================

INSERT INTO `chitietdonhang` VALUES (1,1,1,1,15000000.00),(2,1,3,2,1000000.00),(3,1,4,10,1000000.00),(4,1,5,1,800000.00),(5,2,2,1,25000000.00),(6,2,3,4,2000000.00),(7,2,4,20,2000000.00),(8,2,5,1,800000.00),(9,2,6,2,600000.00),(10,3,1,1,15000000.00),(11,4,1,1,15000000.00);

INSERT INTO `combo` VALUES (1,'Gói tang lễ cơ bản',20000000.00,'Bao gồm: quan tài gỗ sồi, lọ hoa, nến hương, vải liệm','/images/combo-co-ban.jpg',1),(2,'Gói tang lễ cao cấp',45000000.00,'Bao gồm: quan tài gỗ mun, lọ hoa gốm, nến hương, vải liệm lụa, bộ quần áo tang','/images/combo-cao-cap.jpg',1),(3,'Gói hỏa táng',15000000.00,'Bao gồm: dịch vụ hỏa táng, lọ hoa, nến hương','/images/combo-hoa-tang.jpg',1);

INSERT INTO `combochitiet` VALUES (1,1,1,0,'Quan tài gỗ sồi'),(2,3,1,0,'Lọ hoa bằng gốm'),(3,4,1,0,'Nến hương'),(4,5,1,0,'Vải liệm trắng'),(5,2,2,0,'Quan tài gỗ mun'),(6,3,2,0,'Lọ hoa bằng gốm'),(7,4,2,0,'Nến hương'),(8,5,2,0,'Vải liệm trắng'),(9,6,2,0,'Bộ quần áo tang');

INSERT INTO `congno` VALUES (1,1,1,20000000.00,10000000.00,10000000.00,'2026-07-29',1,'Thanh toán 50%','2026-07-14 14:45:18','2026-07-14 14:45:18'),(2,2,1,45000000.00,0.00,45000000.00,'2026-08-13',0,'Chưa thanh toán','2026-07-14 14:45:18','2026-07-14 14:45:18'),(3,3,2,15000000.00,15000000.00,0.00,'2026-07-12',2,'Đã thanh toán đủ','2026-07-12 14:45:18','2026-07-12 14:45:18');

INSERT INTO `doitac` VALUES (1,'Nguyễn Văn A','Công ty TNHH Mai Táng An Yên','0101234567','doitac1','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW','nguyenvana@gmail.com','0901234567','Hà Nội',1,NULL,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(2,'Trần Thị B','Công ty CP Dịch Vụ Táng Lễ','0102345678','doitac2','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW','tranthib@gmail.com','0912345678','TP. Hồ Chí Minh',1,NULL,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(3,'Lê Văn C','Công ty TNHH Vật Liệu Táng Lễ','0103456789','doitac3','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW','levanc@gmail.com','0923456789','Đà Nẵng',2,NULL,'2026-07-14 14:45:18','2026-07-14 14:45:18');

INSERT INTO `donhang` VALUES (1,1,2,'2026-07-14',20000000.00,3,'Khách hàng cần gấp',2,1,NULL,NULL),(2,2,3,'2026-07-14',45000000.00,4,'Đang xử lý',1,0,NULL,NULL),(3,3,4,'2026-07-12',15000000.00,6,'Đã hoàn thành',2,1,NULL,NULL),(4,4,2,'2026-07-09',20000000.00,7,'Khách hủy đơn',0,0,'Khách thay đổi ý định',NULL);

INSERT INTO `hdongct` VALUES (1,1,'Phạm Văn K','2026-07-13','1950-05-15','Nam','BG123456','UBND Quận Hoàn Kiếm','Nghĩa trang Văn Điển','Khu A','A123','2026-07-15 00:00:00'),(2,2,'Hoàng Thị L','2026-07-11','1955-08-20','Nữ','BG234567','UBND Quận 1','Nghĩa trang Bình Hưng Hòa','Khu B','B456','2026-07-12 00:00:00');

INSERT INTO `hoadon` VALUES (1,1,'2026-07-14',20000000.00,2,1,'2026-07-14 14:45:18'),(2,3,'2026-07-12',15000000.00,2,1,'2026-07-12 14:45:18');

INSERT INTO `hopdong` VALUES (1,1,'2026-07-14','2026-07-14','2026-08-13','Đang hiệu lực',0),(2,3,'2026-07-12','2026-07-12','2026-08-11','Đã hoàn thành',1);

INSERT INTO `khachhang` VALUES (1,'Phạm Văn M','001234567890','123 Nguyễn Trãi, Hà Nội','phamvanm@gmail.com','0911223344',2,'2026-07-14 14:45:18','Website','Tư vấn gói tang lễ trọn gói',NULL,21.02850000,105.85420000,NULL,NULL,NULL,NULL,NULL,NULL),(2,'Hoàng Thị N','001234567891','456 Lê Lợi, TP. Hồ Chí Minh','hoangthin@gmail.com','0912233445',3,'2026-07-14 14:45:18','Facebook','Dịch vụ hỏa táng',NULL,10.82310000,106.62970000,NULL,NULL,NULL,NULL,NULL,NULL),(3,'Đỗ Văn P','001234567892','789 Trần Phú, Đà Nẵng','dovanp@gmail.com','0912344556',4,'2026-07-14 14:45:18','Zalo','Mua quan tài',NULL,16.05440000,108.20220000,NULL,NULL,NULL,NULL,NULL,NULL),(4,'Vũ Thị Q','001234567893','321 Hai Bà Trưng, Hà Nội','vuthiq@gmail.com','0913455667',2,'2026-07-14 14:45:18','Hotline','Dịch vụ mai táng',NULL,21.02850000,105.85420000,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO `lichsucongno` VALUES (1,1,10000000.00,2,5,'2026-07-14 14:45:18','GD202401120001','Thanh toán đợt 1'),(2,3,15000000.00,2,5,'2026-07-12 14:45:18','GD202401100001','Thanh toán đầy đủ');

INSERT INTO `nhanvien` VALUES (1,'Admin System','admin','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',1,1,'admin@anyen.vn','Hà Nội','0987654321',10.77688900,106.70080600,'TP Hồ Chí Minh','Quận 1','Phường Bến Nghé','123 Nguyễn Huệ','RANH','2026-07-14 14:47:57','123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP Hồ Chí Minh'),(2,'Nguyễn Văn X','nhanvien1','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',2,1,'nhanvien1@anyen.vn','Hà Nội','0987654322',21.02850000,105.85420000,NULL,NULL,NULL,NULL,'RANH',NULL,NULL),(3,'Trần Thị Y','nhanvien2','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',3,1,'nhanvien2@anyen.vn','TP. Hồ Chí Minh','0987654323',10.82310000,106.62970000,NULL,NULL,NULL,NULL,'RANH',NULL,NULL),(4,'Lê Văn Z','nhanvien3','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',4,1,'nhanvien3@anyen.vn','Đà Nẵng','0987654324',16.05440000,108.20220000,NULL,NULL,NULL,NULL,'RANH',NULL,NULL),(5,'Phạm Thị T','nhanvien4','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',5,1,'nhanvien4@anyen.vn','Hà Nội','0987654325',21.02850000,105.85420000,NULL,NULL,NULL,NULL,'RANH',NULL,NULL),(6,'Nguyễn Văn A','nhanvien_a','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',3,1,'nhanvien_a@example.com','123 Nguyễn Huệ, Q1, TP HCM','0901234567',10.77688900,106.70080600,'TP Hồ Chí Minh','Quận 1','Phường Bến Nghé','123 Nguyễn Huệ','RANH',NULL,'123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP Hồ Chí Minh'),(7,'Trần Thị B','nhanvien_b','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',3,1,'nhanvien_b@example.com','456 Đặng Thùy Trâm, Q3, TP HCM','0912345678',10.78500000,106.68000000,'TP Hồ Chí Minh','Quận 3','Phường 6','456 Đặng Thùy Trâm','RANH',NULL,'456 Đặng Thùy Trâm, Phường 6, Quận 3, TP Hồ Chí Minh'),(8,'Lê Văn C','nhanvien_c','$2a$10$pE8YJ.bsepBVEkyXLegnrOzcsDxebdzwJ/K1xoCzExF0C.24QzvvW',3,1,'nhanvien_c@example.com','789 Lê Hồng Phong, Vũng Tàu','0923456789',10.34600000,107.08430000,'Bà Rịa - Vũng Tàu','TP Vũng Tàu','Phường 1','789 Lê Hồng Phong','BAN',NULL,'789 Lê Hồng Phong, Phường 1, TP Vũng Tàu, Bà Rịa - Vũng Tàu');

INSERT INTO `sanpham` VALUES (1,'Quan tài gỗ sồi','Quan tài','Gỗ sồi','2m x 0.6m x 0.5m','Phật giáo',15000000.00,1,10,'Truyền thống','Việt Nam','Quan tài gỗ sồi cao cấp',0.00,'Nâu','/images/quan-tai-go-soi.jpg','Gỗ sồi',1,'200x60x50 cm','50kg','QT001'),(2,'Quan tài gỗ mun','Quan tài','Gỗ mun','2m x 0.6m x 0.5m','Phật giáo',25000000.00,1,5,'Cao cấp','Việt Nam','Quan tài gỗ mun quý hiếm',0.00,'Đen','/images/quan-tai-go-mun.jpg','Gỗ mun',1,'200x60x50 cm','60kg','QT002'),(3,'Lọ hoa bằng gốm','Lọ hoa','Gốm sứ','30cm cao','Phật giáo',500000.00,2,50,'Truyền thống','Việt Nam','Lọ hoa gốm Bát Tràng',50000.00,'Trắng','/images/lo-hoa-gom.jpg','Gốm',1,'30cm','2kg','LH001'),(4,'Nến hương','Nến','Paraffin','20cm','Phật giáo',100000.00,2,100,'Truyền thống','Việt Nam','Nến hương cao cấp',0.00,'Vàng','/images/nen-huong.jpg','Paraffin',1,'20cm','0.5kg','NH001'),(5,'Vải liệm trắng','Vải liệm','Vải lụa','3m x 2m','Phật giáo',800000.00,3,30,'Truyền thống','Trung Quốc','Vải liệm lụa trắng',0.00,'Trắng','/images/vai-liem.jpg','Lụa',1,'300x200 cm','1kg','VL001'),(6,'Bộ quần áo tang','Quần áo','Vải cotton','L, XL','Phật giáo',300000.00,3,50,'Truyền thống','Việt Nam','Bộ quần áo tang lễ',0.00,'Trắng','/images/quan-ao-tang.jpg','Cotton',1,'L, XL','0.5kg','QA001');

INSERT INTO `sanphamchitiet` VALUES (1,1,'Khung','Khung quan tài làm từ gỗ sồi 100%',1,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(2,1,'Nắp','Nắp quan tài có thể mở đóng',2,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(3,1,'Lót','Lót bên trong bằng vải nhung',3,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(4,2,'Khung','Khung quan tài làm từ gỗ mun quý hiếm',1,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(5,2,'Nắp','Nắp quan tài chạm khắc hoa văn',2,'2026-07-14 14:45:18','2026-07-14 14:45:18'),(6,2,'Lót','Lót bên trong bằng lụa tơ tằm',3,'2026-07-14 14:45:18','2026-07-14 14:45:18');

INSERT INTO `sanphamhinhanh` VALUES (1,1,1,'Chính','/images/quan-tai-soi-1.jpg',1,'2026-07-14 14:45:18'),(2,1,1,'Chi tiết','/images/quan-tai-soi-detail-1.jpg',2,'2026-07-14 14:45:18'),(3,1,2,'Chính','/images/quan-tai-soi-2.jpg',3,'2026-07-14 14:45:18'),(4,2,1,'Chính','/images/quan-tai-mun-1.jpg',1,'2026-07-14 14:45:18'),(5,2,1,'Chi tiết','/images/quan-tai-mun-detail-1.jpg',2,'2026-07-14 14:45:18'),(6,3,NULL,'Chính','/images/lo-hoa-gom-1.jpg',1,'2026-07-14 14:45:18');

INSERT INTO `thongbao` VALUES (1,'Khách hàng mới cần tư vấn','Khách hàng Phạm Văn M cần tư vấn gói tang lễ trọn gói','CONG_VIEC',4,2,1,2,NULL,'2026-07-14 14:45:18','2026-07-15 08:37:49'),(2,'Hệ thống bảo trì','Hệ thống sẽ bảo trì vào ngày 15/01/2024','HE_THONG',NULL,NULL,NULL,1,NULL,'2026-07-13 14:45:18','2026-07-13 14:45:18'),(3,'Yêu cầu từ chối','Nhân viên từ chối nhận công việc','TU_CHOI',2,4,1,3,'Đang bận công việc khác','2026-07-12 14:45:18','2026-07-12 14:45:18'),(4,'Duyệt sản phẩm mới','Đối tác Nguyễn Văn A đăng ký sản phẩm mới cần duyệt','DUYET_SAN_PHAM',NULL,1,NULL,4,NULL,'2026-07-14 14:45:18','2026-07-14 14:45:18');

INSERT INTO `thongbaodoitac` VALUES (1,1,1,'DON_HANG','Đơn hàng mới #DH001','Bạn có đơn hàng mới cần xác nhận','DA_CHAP_NHAN',NULL,1,'2026-07-13 14:45:18','2026-07-13 14:45:18'),(2,1,2,'DON_HANG','Đơn hàng mới #DH002','Bạn có đơn hàng mới cần xác nhận','CHO_XAC_NHAN',NULL,0,'2026-07-14 14:45:18',NULL),(3,2,3,'DON_HANG','Đơn hàng mới #DH003','Bạn có đơn hàng mới cần xác nhận','DA_CHAP_NHAN',NULL,1,'2026-07-12 14:45:18','2026-07-12 14:45:18'),(4,3,NULL,'DUYET_SAN_PHAM','Duyệt sản phẩm mới','Sản phẩm [MASP:7] cần được duyệt','CHO_XAC_NHAN',NULL,0,'2026-07-14 14:45:18',NULL);

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- CẬP NHẬT DATABASE CHO PROJECT AN YÊN
-- Database: dich_vu_mai_tang
--
-- Chức năng được hỗ trợ:
-- 1. Combo thuộc riêng từng đối tác.
-- 2. Combo chỉ chứa sản phẩm của chính đối tác đó.
-- 3. Tự động tăng khóa chính chi tiết combo.
-- 4. Thông báo duyệt sản phẩm lưu trực tiếp MaSanPham.
-- 5. Sửa trạng thái để hiện nút Duyệt / Từ chối.
--
-- Script này giữ nguyên dữ liệu hiện có.
-- Chỉ chạy một lần.
-- ============================================================

USE `dich_vu_mai_tang`;

SELECT DATABASE() AS `DatabaseDangCapNhat`;

SET FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- 1. GẮN COMBO VỚI ĐỐI TÁC
-- ============================================================

ALTER TABLE `combo`
    ADD COLUMN `MaDoiTac` INT NULL AFTER `HinhAnh`;

CREATE INDEX `idx_combo_madoitac`
    ON `combo` (`MaDoiTac`);

ALTER TABLE `combo`
    ADD CONSTRAINT `fk_combo_doitac`
        FOREIGN KEY (`MaDoiTac`)
            REFERENCES `doitac` (`MaDoiTac`);


-- ============================================================
-- 2. TỰ ĐỘNG TĂNG KHÓA CHÍNH CHI TIẾT COMBO
-- ============================================================

ALTER TABLE `combochitiet`
    MODIFY COLUMN `ComboChiTietId`
        INT NOT NULL AUTO_INCREMENT;


-- ============================================================
-- 3. THÔNG BÁO DUYỆT SẢN PHẨM LƯU MaSanPham
-- ============================================================

ALTER TABLE `thongbao`
    ADD COLUMN `MaSanPham` INT NULL AFTER `MaKhachHang`;

CREATE INDEX `idx_thongbao_masanpham`
    ON `thongbao` (`MaSanPham`);

ALTER TABLE `thongbao`
    ADD CONSTRAINT `fk_thongbao_sanpham`
        FOREIGN KEY (`MaSanPham`)
            REFERENCES `sanpham` (`MaSanPham`)
            ON DELETE SET NULL;


-- ============================================================
-- 4. KHÔI PHỤC MaSanPham CHO THÔNG BÁO CŨ
--
-- Áp dụng với nội dung có dạng:
-- Sản phẩm [MASP:7] cần được duyệt
-- ============================================================

UPDATE `thongbao` AS tb
    INNER JOIN `sanpham` AS sp
    ON sp.`MaSanPham` = CAST(
            SUBSTRING_INDEX(
                    SUBSTRING_INDEX(tb.`NoiDung`, '[MASP:', -1),
                    ']',
                    1
            ) AS UNSIGNED
                        )
SET
    tb.`MaSanPham` = sp.`MaSanPham`,
    tb.`NgayCapNhat` = NOW()
WHERE tb.`LoaiThongBao` = 'DUYET_SAN_PHAM'
  AND tb.`MaSanPham` IS NULL
  AND tb.`NoiDung` LIKE '%[MASP:%]%';


-- ============================================================
-- 5. SỬA TRẠNG THÁI THÔNG BÁO SẢN PHẨM ĐANG CHỜ DUYỆT
--
-- sanpham.trangThai:
-- 0 = Ẩn / bị từ chối
-- 1 = Đã duyệt / đang bán
-- 2 = Chờ duyệt
--
-- thongbao.TrangThai:
-- 0 = Chưa đọc
-- 1 = Đã đọc
-- 2 = Đã chấp nhận
-- 3 = Đã từ chối
-- 4 = Chờ xác nhận
-- ============================================================

UPDATE `thongbao` AS tb
    INNER JOIN `sanpham` AS sp
    ON sp.`MaSanPham` = tb.`MaSanPham`
SET
    tb.`TrangThai` = 4,
    tb.`NgayCapNhat` = NOW()
WHERE tb.`LoaiThongBao` = 'DUYET_SAN_PHAM'
  AND sp.`trangThai` = 2
  AND tb.`TrangThai` IN (0, 1);


-- ============================================================
-- 6. ĐỒNG BỘ THÔNG BÁO CỦA SẢN PHẨM ĐÃ DUYỆT
-- ============================================================

UPDATE `thongbao` AS tb
    INNER JOIN `sanpham` AS sp
    ON sp.`MaSanPham` = tb.`MaSanPham`
SET
    tb.`TrangThai` = 2,
    tb.`NgayCapNhat` = NOW()
WHERE tb.`LoaiThongBao` = 'DUYET_SAN_PHAM'
  AND sp.`trangThai` = 1
  AND tb.`TrangThai` = 4;


-- ============================================================
-- 7. ĐÓNG THÔNG BÁO MẪU CŨ KHÔNG CÓ SẢN PHẨM
--
-- Database mẫu hiện có thông báo duyệt nhưng không lưu mã
-- sản phẩm và nội dung cũng không chứa [MASP:id].
-- Nếu giữ trạng thái 4, nút duyệt sẽ hiện nhưng không xử lý được.
-- Không xóa thông báo, chỉ chuyển sang trạng thái đã đọc.
-- ============================================================

UPDATE `thongbao` AS tb
    LEFT JOIN `sanpham` AS sp
    ON sp.`MaSanPham` = tb.`MaSanPham`
SET
    tb.`TrangThai` = 1,
    tb.`NgayCapNhat` = NOW()
WHERE tb.`LoaiThongBao` = 'DUYET_SAN_PHAM'
  AND tb.`TrangThai` = 4
  AND (
    tb.`MaSanPham` IS NULL
        OR sp.`MaSanPham` IS NULL
    );


-- ============================================================
-- 8. GÁN ĐỐI TÁC CHO COMBO CŨ KHI COMBO CHỈ CÓ SẢN PHẨM
--    CỦA MỘT ĐỐI TÁC
--
-- Combo chứa sản phẩm của nhiều đối tác sẽ giữ MaDoiTac = NULL.
-- Không xóa combo hoặc sản phẩm cũ.
-- ============================================================

UPDATE `combo` AS c
    INNER JOIN (
        SELECT
            cct.`ComboID`,
            MIN(sp.`MaDoiTac`) AS `MaDoiTac`
        FROM `combochitiet` AS cct
                 INNER JOIN `sanpham` AS sp
                            ON sp.`MaSanPham` = cct.`MaSanPham`
        WHERE sp.`MaDoiTac` IS NOT NULL
        GROUP BY cct.`ComboID`
        HAVING COUNT(DISTINCT sp.`MaDoiTac`) = 1
    ) AS du_lieu_combo
    ON du_lieu_combo.`ComboID` = c.`ComboId`
SET c.`MaDoiTac` = du_lieu_combo.`MaDoiTac`
WHERE c.`MaDoiTac` IS NULL;


-- ============================================================
-- 9. CHUẨN HÓA TRẠNG THÁI COMBO CŨ
--
-- 0 = Ẩn
-- 1 = Hoạt động
-- 2 = Ngừng kinh doanh
-- ============================================================

UPDATE `combo`
SET `TrangThai` = 1
WHERE `TrangThai` IS NULL;


SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- 10. KIỂM TRA SAU KHI CẬP NHẬT
-- ============================================================

SELECT
    c.`ComboId`,
    c.`TenCombo`,
    c.`MaDoiTac`,
    dt.`TenDoiTac`,
    c.`TrangThai`
FROM `combo` AS c
         LEFT JOIN `doitac` AS dt
                   ON dt.`MaDoiTac` = c.`MaDoiTac`
ORDER BY c.`ComboId`;


SELECT
    tb.`MaThongBao`,
    tb.`TieuDe`,
    tb.`LoaiThongBao`,
    tb.`MaSanPham`,
    sp.`tenSanPham`,
    sp.`trangThai` AS `TrangThaiSanPham`,
    tb.`TrangThai` AS `TrangThaiThongBao`
FROM `thongbao` AS tb
         LEFT JOIN `sanpham` AS sp
                   ON sp.`MaSanPham` = tb.`MaSanPham`
WHERE tb.`LoaiThongBao` = 'DUYET_SAN_PHAM'
ORDER BY tb.`MaThongBao` DESC;
ALTER TABLE `combochitiet`
    MODIFY COLUMN `ComboChiTietId` INT NOT NULL AUTO_INCREMENT,
    ADD COLUMN `SoLuong` INT NOT NULL DEFAULT 1 AFTER `Loai`;

UPDATE `combochitiet`
SET `SoLuong` = 1
WHERE `SoLuong` IS NULL OR `SoLuong` <= 0;

SELECT `ComboChiTietId`, `ComboID`, `MaSanPham`, `SoLuong`
FROM `combochitiet`
ORDER BY `ComboID`, `ComboChiTietId`;

SHOW COLUMNS FROM `combo`;
SHOW COLUMNS FROM `combochitiet`;
SHOW COLUMNS FROM `thongbao`;
SELECT
    ComboId,
    TenCombo,
    MaDoiTac,
    TrangThai
FROM combo
ORDER BY ComboId;
-- ============================================================
-- 1. QUAN TÀI GỖ HƯƠNG PHÚC AN – HOA TRẮNG TƯỞNG NIỆM
-- ============================================================
INSERT INTO sanpham (
    tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien,
    MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai,
    mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX
) VALUES (
             'Quan tài gỗ hương Phúc An – Hoa trắng tưởng niệm',
             'Quan tài',
             'Lót nhung trắng cao cấp, gối đầu và đệm êm trang nghiêm',
             'Dáng hộp truyền thống, nắp mở toàn phần, 6 tay cầm kim loại',
             'Phật giáo, Công giáo và nghi lễ truyền thống',
             28500000.00,
             1,
             6,
             'Cổ điển trang nghiêm, đường nét cân đối',
             'Việt Nam',
             'Mẫu quan tài cao cấp dành cho gia đình đề cao sự chỉn chu, trang nghiêm và vẻ đẹp truyền thống. Sản phẩm có thể phối hoa trắng theo yêu cầu.',
             1500000.00,
             'Nâu gụ',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/quantai1_mzhy8f.jpg',
             'Gỗ hương ghép thanh tuyển chọn, phụ kiện hợp kim chống oxy hóa',
             1,
             '210 x 72 x 58 cm',
             'Khoảng 85 kg',
             'Gỗ được sấy kiểm soát độ ẩm, xử lý chống cong vênh và hoàn thiện sơn PU nhiều lớp'
         );
SET @sp1 = LAST_INSERT_ID();

INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
                                                                                           (@sp1, 'TIEU_DE', 'Một lựa chọn trang nghiêm cho lời tiễn biệt trọn vẹn', 1, NOW(), NOW()),
                                                                                           (@sp1, 'NOI_DUNG', 'Quan tài gỗ hương Phúc An được phát triển dành cho những gia đình mong muốn một nghi lễ chỉn chu, ấm áp và giàu tính tưởng niệm. Tông nâu gụ trầm tạo cảm giác vững chãi, kết hợp cùng cụm hoa trắng thanh khiết giúp tổng thể giữ được sự trang trọng mà không nặng nề. Mỗi đường nét đều được hoàn thiện theo tinh thần “an lành tiễn biệt”, để giây phút cuối cùng trở nên trang nghiêm, bình yên và đầy yêu thương.', 2, NOW(), NOW()),
                                                                                           (@sp1, 'TIEU_DE', 'Chất liệu gỗ chọn lọc, kết cấu chắc chắn', 3, NOW(), NOW()),
                                                                                           (@sp1, 'NOI_DUNG', 'Thân quan tài sử dụng gỗ hương ghép thanh tuyển chọn, được sấy kiểm soát độ ẩm trước khi gia công nhằm hạn chế co ngót và cong vênh. Hệ khung gia cường bên trong giúp sản phẩm ổn định trong quá trình di chuyển. Bề mặt được xử lý mịn, phủ sơn PU nhiều lớp để làm nổi bật vân gỗ tự nhiên, đồng thời tăng khả năng chống ẩm và giữ màu bền đẹp trong suốt thời gian tổ chức tang lễ.', 4, NOW(), NOW()),
                                                                                           (@sp1, 'TIEU_DE', 'Không gian nội thất êm ái và tôn nghiêm', 5, NOW(), NOW()),
                                                                                           (@sp1, 'NOI_DUNG', 'Phần nội thất được lót nhung trắng cao cấp, đi kèm đệm và gối đầu đồng bộ. Từng nếp vải được may gọn gàng, tạo cảm giác mềm mại, sạch sẽ và trang trọng. Thiết kế nắp mở toàn phần giúp gia đình thuận tiện thực hiện các nghi thức theo phong tục, tôn giáo hoặc nguyện vọng riêng của người đã khuất.', 6, NOW(), NOW()),
                                                                                           (@sp1, 'TIEU_DE', 'Hoa trắng tưởng niệm – vẻ đẹp của sự thanh khiết', 7, NOW(), NOW()),
                                                                                           (@sp1, 'NOI_DUNG', 'Cụm hoa trắng đặt trên nắp được phối theo phong cách thanh lịch với sắc trắng chủ đạo và điểm xanh tự nhiên. Gia đình có thể yêu cầu điều chỉnh loại hoa, mật độ hoa hoặc thêm dải băng tưởng niệm. Cách phối này phù hợp với nhiều không gian tang lễ, từ tư gia đến nhà tang lễ, đồng thời tạo nên một điểm nhấn trang nhã cho toàn bộ nghi thức.', 8, NOW(), NOW()),
                                                                                           (@sp1, 'TIEU_DE', 'Dịch vụ đi kèm chu đáo', 9, NOW(), NOW()),
                                                                                           (@sp1, 'NOI_DUNG', 'Sản phẩm được kiểm tra kỹ trước khi bàn giao, hỗ trợ vận chuyển đến địa điểm tổ chức và phối hợp cùng đội nghi lễ khi cần. An Yên tư vấn kích thước, cách trang trí và phương án bố trí phù hợp với không gian thực tế, giúp gia đình giảm bớt áp lực trong thời điểm cần sự bình tĩnh và sẻ chia.', 10, NOW(), NOW());

INSERT INTO sanphamhinhanh (
    MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt
) VALUES (
             @sp1, NULL, 'Chính',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/quantai1_mzhy8f.jpg',
             1, NOW()
         );

-- ============================================================
-- 2. BỘ NẾN LY MINI AN TÂM 4 SẮC
-- ============================================================
INSERT INTO sanpham (
    tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien,
    MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai,
    mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX
) VALUES (
             'Bộ nến ly mini An Tâm 4 sắc',
             'Nến tang lễ',
             'Bộ 4 nến thơm ly thủy tinh, tim cotton ít khói',
             'Bộ 4 ly, mỗi ly cháy khoảng 16–20 giờ',
             'Phù hợp mọi tôn giáo và không gian tưởng niệm',
             450000.00,
             2,
             80,
             'Tối giản hiện đại, dễ bố trí trên bàn tưởng niệm',
             'Việt Nam',
             'Bộ nến nhỏ gọn tạo ánh sáng ấm áp, phù hợp bàn thờ, bàn tưởng niệm, lễ cầu nguyện và không gian gia đình.',
             50000.00,
             'Đỏ, hồng, tím, xanh dịu',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/nentang2_d4ye5w.webp',
             'Sáp thực vật phối sáp khoáng tinh luyện, ly thủy tinh chịu nhiệt, tim cotton',
             1,
             'Mỗi ly khoảng 7 x 7 cm',
             'Khoảng 0.8 kg/bộ',
             'Đổ sáp kiểm soát nhiệt độ, cân tim thủ công và kiểm tra cháy thử trước khi đóng gói'
         );
SET @sp2 = LAST_INSERT_ID();

INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
                                                                                           (@sp2, 'TIEU_DE', 'Ánh nến dịu dàng cho không gian tưởng niệm', 1, NOW(), NOW()),
                                                                                           (@sp2, 'NOI_DUNG', 'Bộ nến ly mini An Tâm được thiết kế để mang lại nguồn sáng ấm áp, nhẹ nhàng và gần gũi trong những khoảnh khắc tưởng nhớ. Bốn sắc nến thanh nhã giúp gia đình linh hoạt phối hợp với hoa, ảnh kỷ niệm và các vật phẩm trên bàn thờ. Kích thước nhỏ gọn nhưng thời gian cháy ổn định, phù hợp sử dụng trong lễ viếng, lễ cầu nguyện hoặc những buổi tưởng niệm riêng tư tại gia đình.', 2, NOW(), NOW()),
                                                                                           (@sp2, 'TIEU_DE', 'Tim cotton ít khói, cháy đều và an toàn', 3, NOW(), NOW()),
                                                                                           (@sp2, 'NOI_DUNG', 'Mỗi ly nến sử dụng tim cotton được căn chỉnh thủ công để ngọn lửa cháy vừa phải, hạn chế khói đen và giảm hiện tượng cháy lệch. Ly thủy tinh chịu nhiệt giúp bảo vệ bề mặt bàn, đồng thời giữ sáp gọn gàng trong quá trình sử dụng. Khi đặt nến, nên sử dụng trên mặt phẳng chắc chắn và tránh khu vực có gió mạnh để duy trì ngọn lửa ổn định.', 4, NOW(), NOW()),
                                                                                           (@sp2, 'TIEU_DE', 'Bốn gam màu mang ý nghĩa bình an', 5, NOW(), NOW()),
                                                                                           (@sp2, 'NOI_DUNG', 'Sắc đỏ đại diện cho tình thân, sắc hồng gợi sự dịu dàng, sắc tím mang nét trầm lắng và sắc xanh tạo cảm giác thanh tịnh. Bộ màu được lựa chọn theo hướng nhã nhặn, không quá rực, phù hợp với không khí trang nghiêm của lễ tưởng niệm. Gia đình có thể thắp đồng thời cả bốn ly hoặc sử dụng luân phiên theo từng thời điểm.', 6, NOW(), NOW()),
                                                                                           (@sp2, 'TIEU_DE', 'Hương thơm nhẹ, không lấn át không gian nghi lễ', 7, NOW(), NOW()),
                                                                                           (@sp2, 'NOI_DUNG', 'Nến có mùi hương dịu với cường độ vừa phải, giúp không gian bớt lạnh lẽo nhưng vẫn giữ được sự trang nghiêm. Công thức hương được định hướng thanh sạch, không quá ngọt và không gây cảm giác nồng khi sử dụng trong phòng kín. Đây là lựa chọn phù hợp cho gia đình yêu thích phong cách tưởng niệm hiện đại, tinh tế và tối giản.', 8, NOW(), NOW()),
                                                                                           (@sp2, 'TIEU_DE', 'Đóng gói chỉn chu, thuận tiện sử dụng', 9, NOW(), NOW()),
                                                                                           (@sp2, 'NOI_DUNG', 'Mỗi bộ gồm bốn ly nến được đóng gói chống va đập và kiểm tra ngoại quan trước khi giao. Sản phẩm có thể kết hợp cùng kệ hoa, khung ảnh, sổ tang hoặc bộ vật phẩm tưởng niệm để tạo thành một không gian đồng bộ. An Yên hỗ trợ tư vấn số lượng nến theo diện tích bàn thờ và thời gian tổ chức lễ.', 10, NOW(), NOW());

INSERT INTO sanphamhinhanh (
    MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt
) VALUES (
             @sp2, NULL, 'Chính',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/nentang2_d4ye5w.webp',
             1, NOW()
         );

-- ============================================================
-- 3. QUAN TÀI NGHỆ THUẬT SMART MEMORIAL
-- ============================================================
INSERT INTO sanpham (
    tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien,
    MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai,
    mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX
) VALUES (
             'Quan tài nghệ thuật Smart Memorial',
             'Quan tài',
             'Lót nỉ đen cao cấp, đệm định hình và gối đồng bộ',
             'Dáng hộp hiện đại, nắp phẳng in đồ họa theo yêu cầu',
             'Không phân biệt tôn giáo',
             32000000.00,
             1,
             2,
             'Cá nhân hóa hiện đại, phong cách công nghệ',
             'Việt Nam',
             'Mẫu quan tài nghệ thuật dành cho gia đình mong muốn lưu giữ dấu ấn riêng của người đã khuất thông qua hình ảnh và chủ đề cá nhân hóa.',
             2000000.00,
             'Đen, viền bạc',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/quantai2_vakurw.jpg',
             'Gỗ công nghiệp chống ẩm phủ composite, viền hợp kim và lớp in bảo vệ',
             1,
             '205 x 70 x 55 cm',
             'Khoảng 78 kg',
             'Gia công CNC, phủ composite chống ẩm và in UV độ phân giải cao'
         );
SET @sp3 = LAST_INSERT_ID();

INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
                                                                                           (@sp3, 'TIEU_DE', 'Lưu giữ cá tính riêng trong một thiết kế tưởng niệm hiện đại', 1, NOW(), NOW()),
                                                                                           (@sp3, 'NOI_DUNG', 'Smart Memorial là mẫu quan tài nghệ thuật hướng đến sự cá nhân hóa, phù hợp với gia đình mong muốn kể lại một phần câu chuyện, sở thích hoặc dấu ấn đặc biệt của người đã khuất. Thiết kế lấy cảm hứng từ ngôn ngữ công nghệ hiện đại, sử dụng nền đen sang trọng cùng bề mặt đồ họa nổi bật. Sản phẩm mang đến một cách thể hiện mới mẻ nhưng vẫn giữ trọn sự tôn kính cần có trong nghi lễ tiễn biệt.', 2, NOW(), NOW()),
                                                                                           (@sp3, 'TIEU_DE', 'Bề mặt in UV sắc nét và có lớp bảo vệ', 3, NOW(), NOW()),
                                                                                           (@sp3, 'NOI_DUNG', 'Phần đồ họa trên nắp được thực hiện bằng công nghệ in UV độ phân giải cao, giúp hình ảnh rõ nét và màu sắc ổn định. Sau khi in, bề mặt được phủ lớp bảo vệ nhằm hạn chế trầy xước trong quá trình vận chuyển và bố trí nghi lễ. Gia đình có thể lựa chọn chủ đề hình ảnh, màu sắc, câu chữ ngắn hoặc biểu tượng mang ý nghĩa riêng.', 4, NOW(), NOW()),
                                                                                           (@sp3, 'TIEU_DE', 'Kết cấu hiện đại, chắc chắn và gọn gàng', 5, NOW(), NOW()),
                                                                                           (@sp3, 'NOI_DUNG', 'Thân sản phẩm sử dụng vật liệu gỗ công nghiệp chống ẩm kết hợp lớp phủ composite, tạo bề mặt đồng đều và giảm ảnh hưởng của thay đổi nhiệt độ, độ ẩm. Khung bên trong được gia cường tại các vị trí chịu lực, tay cầm bố trí cân đối giúp việc di chuyển thuận tiện. Các góc cạnh được bo và hoàn thiện kỹ để bảo đảm tính thẩm mỹ.', 6, NOW(), NOW()),
                                                                                           (@sp3, 'TIEU_DE', 'Nội thất tối màu trang trọng', 7, NOW(), NOW()),
                                                                                           (@sp3, 'NOI_DUNG', 'Khoang bên trong được lót nỉ đen cao cấp, kết hợp đệm định hình và gối đồng bộ. Tông màu trầm tạo sự thống nhất với ngoại thất, đồng thời mang lại cảm giác trang nghiêm, kín đáo. Gia đình có thể yêu cầu thay đổi màu lót hoặc bổ sung biểu tượng tôn giáo để phù hợp với nghi thức riêng.', 8, NOW(), NOW()),
                                                                                           (@sp3, 'TIEU_DE', 'Dịch vụ cá nhân hóa theo yêu cầu gia đình', 9, NOW(), NOW()),
                                                                                           (@sp3, 'NOI_DUNG', 'Đội ngũ thiết kế của An Yên tiếp nhận nội dung, hình ảnh và ý tưởng từ gia đình, sau đó dựng bản xem trước để xác nhận trước khi sản xuất. Quy trình này giúp kiểm soát bố cục, màu sắc và thông điệp tưởng niệm. Sản phẩm phù hợp với các nghi lễ hiện đại, lễ tiễn biệt cá nhân hóa hoặc những gia đình muốn lưu giữ một hình ảnh thật riêng biệt và đáng nhớ.', 10, NOW(), NOW());

INSERT INTO sanphamhinhanh (
    MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt
) VALUES (
             @sp3, NULL, 'Chính',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/quantai2_vakurw.jpg',
             1, NOW()
         );

-- ============================================================
-- 4. KỆ HOA TANG LỄ VĨNH BIỆT THANH KHIẾT
-- ============================================================
INSERT INTO sanpham (
    tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien,
    MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai,
    mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX
) VALUES (
             'Kệ hoa tang lễ Vĩnh Biệt Thanh Khiết',
             'Hoa tang lễ',
             'Ba vòng hoa kết tầng, điểm hoa ly và hoa trắng cao cấp',
             'Kệ đứng 3 tầng, dáng dọc',
             'Phù hợp mọi tôn giáo',
             3800000.00,
             2,
             20,
             'Thanh lịch, trang nghiêm, bố cục ba tầng',
             'Việt Nam',
             'Kệ hoa trắng cao cấp gửi gắm lời chia buồn chân thành, phù hợp lễ viếng tại tư gia, nhà tang lễ hoặc cơ quan.',
             300000.00,
             'Trắng, xanh lá nhạt',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/hoatang1_wqkucc.jpg',
             'Hoa ly, hoa cúc, hoa hồng trắng, hoa phụ và lá trang trí',
             1,
             'Cao khoảng 180 cm, rộng khoảng 80 cm',
             'Khoảng 18 kg',
             'Cắm hoa thủ công trên khung gia cố, giữ ẩm bằng mút chuyên dụng'
         );
SET @sp4 = LAST_INSERT_ID();

INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
                                                                                           (@sp4, 'TIEU_DE', 'Sắc trắng thanh khiết thay lời tiễn biệt', 1, NOW(), NOW()),
                                                                                           (@sp4, 'NOI_DUNG', 'Kệ hoa Vĩnh Biệt Thanh Khiết sử dụng sắc trắng làm chủ đạo, tượng trưng cho sự tinh khôi, bình an và lòng thành kính. Bố cục ba tầng tạo chiều cao trang trọng, giúp kệ hoa nổi bật trong không gian lễ viếng nhưng vẫn giữ vẻ nhã nhặn. Đây là lựa chọn phù hợp để gia đình, bạn bè, đồng nghiệp hoặc tập thể gửi lời phân ưu chân thành đến người đã khuất.', 2, NOW(), NOW()),
                                                                                           (@sp4, 'TIEU_DE', 'Bố cục ba tầng cân đối và giàu chiều sâu', 3, NOW(), NOW()),
                                                                                           (@sp4, 'NOI_DUNG', 'Ba vòng hoa được kết nối theo trục dọc, tạo cảm giác liền mạch và hướng thượng. Hoa ly và hoa cúc được bố trí xen kẽ cùng hoa hồng trắng, hoa phụ và lá xanh nhằm tạo độ mềm mại tự nhiên. Phần chân kệ được hoàn thiện bằng nơ trắng, giúp tổng thể gọn gàng, trang nghiêm và phù hợp với nhiều phong cách tổ chức tang lễ.', 4, NOW(), NOW()),
                                                                                           (@sp4, 'TIEU_DE', 'Hoa tươi được tuyển chọn trong ngày', 5, NOW(), NOW()),
                                                                                           (@sp4, 'NOI_DUNG', 'Hoa được lựa chọn theo tiêu chí cánh khỏe, màu đồng đều và độ nở phù hợp với thời gian diễn ra lễ viếng. Mút giữ ẩm chuyên dụng giúp duy trì độ tươi trong suốt thời gian trưng bày. Tùy mùa, một số loại hoa có thể được thay thế bằng giống tương đương nhưng vẫn bảo đảm đúng tông màu, giá trị thẩm mỹ và tinh thần của mẫu thiết kế.', 6, NOW(), NOW()),
                                                                                           (@sp4, 'TIEU_DE', 'Thông điệp chia buồn được thiết kế riêng', 7, NOW(), NOW()),
                                                                                           (@sp4, 'NOI_DUNG', 'Gia đình hoặc đơn vị đặt hoa có thể cung cấp nội dung trên băng rôn, tên người gửi và lời tiễn biệt. Nội dung được trình bày rõ ràng, trang trọng, hạn chế lỗi chính tả và cân đối với tổng thể kệ hoa. An Yên hỗ trợ gợi ý câu chữ phù hợp trong trường hợp khách hàng chưa lựa chọn được thông điệp.', 8, NOW(), NOW()),
                                                                                           (@sp4, 'TIEU_DE', 'Giao và bố trí tận nơi', 9, NOW(), NOW()),
                                                                                           (@sp4, 'NOI_DUNG', 'Kệ hoa được vận chuyển bằng phương tiện phù hợp, hạn chế dập cánh và xô lệch bố cục. Nhân viên hỗ trợ đặt kệ đúng vị trí tại tư gia, nhà tang lễ hoặc địa điểm tổ chức, đồng thời kiểm tra lại băng rôn và dáng hoa trước khi bàn giao. Dịch vụ hướng đến sự đúng giờ, chỉn chu và tôn trọng tuyệt đối không gian nghi lễ.', 10, NOW(), NOW());

INSERT INTO sanphamhinhanh (
    MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt
) VALUES (
             @sp4, NULL, 'Chính',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/hoatang1_wqkucc.jpg',
             1, NOW()
         );

-- ============================================================
-- 5. NẾN TƯỞNG NIỆM BẠCH LIÊN
-- ============================================================
INSERT INTO sanpham (
    tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien,
    MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai,
    mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX
) VALUES (
             'Nến tưởng niệm Bạch Liên',
             'Nến tang lễ',
             'Nến trụ phối hoa hồng trắng và dải ruy băng tưởng niệm',
             '01 nến trụ, 01 hoa trang trí và ruy băng đồng bộ',
             'Phù hợp mọi tôn giáo',
             350000.00,
             2,
             60,
             'Trang nghiêm tối giản',
             'Việt Nam',
             'Mẫu nến tưởng niệm thanh lịch, phù hợp bàn thờ, bàn ký sổ tang, góc lưu niệm hoặc lễ cầu nguyện.',
             30000.00,
             'Trắng ngà, đen',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131155/nentang1_nnkq8y.jpg',
             'Sáp paraffin tinh luyện phối sáp ong, tim cotton, hoa lụa cao cấp và ruy băng',
             1,
             'Nến cao khoảng 20 cm, đường kính 7 cm',
             'Khoảng 0.6 kg',
             'Đúc nến thủ công, căn tim chống lệch và hoàn thiện phụ kiện bằng tay'
         );
SET @sp5 = LAST_INSERT_ID();

INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
                                                                                           (@sp5, 'TIEU_DE', 'Một ngọn nến nhỏ, một khoảng lặng đầy yêu thương', 1, NOW(), NOW()),
                                                                                           (@sp5, 'NOI_DUNG', 'Nến tưởng niệm Bạch Liên được tạo nên dành cho những khoảnh khắc cần sự lắng đọng. Ánh nến vàng ấm nổi bật trên nền trắng và đen, tạo nên một góc tưởng niệm trang nghiêm, thanh sạch và gần gũi. Sản phẩm phù hợp đặt cạnh di ảnh, sổ tang, bình hoa hoặc những kỷ vật gắn bó với người đã khuất.', 2, NOW(), NOW()),
                                                                                           (@sp5, 'TIEU_DE', 'Thiết kế trắng – đen tối giản và tinh tế', 3, NOW(), NOW()),
                                                                                           (@sp5, 'NOI_DUNG', 'Nến trụ màu trắng ngà được kết hợp cùng hoa hồng trắng và dải ruy băng đen, tạo nên ngôn ngữ thị giác cân bằng giữa sự thanh khiết và nỗi tiếc thương. Kiểu dáng tối giản giúp sản phẩm dễ hòa hợp với nhiều không gian tưởng niệm, từ truyền thống đến hiện đại, mà không làm mất đi vẻ trang nghiêm cần thiết.', 4, NOW(), NOW()),
                                                                                           (@sp5, 'TIEU_DE', 'Ngọn lửa ổn định, thời gian cháy lâu', 5, NOW(), NOW()),
                                                                                           (@sp5, 'NOI_DUNG', 'Tim cotton được căn giữa giúp ngọn lửa cháy tương đối ổn định và hạn chế khói đen khi sử dụng đúng hướng dẫn. Thân nến được đúc đặc, phù hợp thắp trong nhiều giờ. Để bảo đảm an toàn, nên đặt nến trên đế chịu nhiệt, tránh rèm vải, giấy và khu vực có trẻ nhỏ hoặc vật nuôi.', 6, NOW(), NOW()),
                                                                                           (@sp5, 'TIEU_DE', 'Phù hợp nhiều mục đích tưởng niệm', 7, NOW(), NOW()),
                                                                                           (@sp5, 'NOI_DUNG', 'Ngoài sử dụng trong lễ tang, nến Bạch Liên còn phù hợp cho ngày giỗ, lễ cầu siêu, lễ tưởng niệm tập thể hoặc góc lưu niệm tại gia đình. Thiết kế gọn gàng giúp dễ bố trí trong không gian nhỏ. Khách hàng có thể yêu cầu thêm thẻ tên, ngày tháng hoặc một câu tưởng nhớ ngắn để tăng tính cá nhân hóa.', 8, NOW(), NOW()),
                                                                                           (@sp5, 'TIEU_DE', 'Đóng gói bảo vệ và giao hàng cẩn trọng', 9, NOW(), NOW()),
                                                                                           (@sp5, 'NOI_DUNG', 'Sản phẩm được bọc chống trầy, cố định hoa và ruy băng trước khi đặt vào hộp. Nhân viên kiểm tra bề mặt nến, tim nến và phụ kiện trước khi bàn giao. An Yên có thể phối hợp sản phẩm cùng hoa trắng, khung ảnh và vật phẩm tưởng niệm khác để tạo nên một bộ trang trí đồng nhất, trang nghiêm và giàu cảm xúc.', 10, NOW(), NOW());

INSERT INTO sanphamhinhanh (
    MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt
) VALUES (
             @sp5, NULL, 'Chính',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131155/nentang1_nnkq8y.jpg',
             1, NOW()
         );

-- ============================================================
-- 6. KỆ HOA CHIA BUỒN HOÀNG ÂN
-- ============================================================
INSERT INTO sanpham (
    tenSanPham, loai, noiThat, quyCach, tonGiao, giaTien,
    MaDoiTac, SoLuong, thietKe, xuatXu, GhiChu, khuyenMai,
    mauSac, HinhAnh, vatLieu, trangThai, kichThuoc, trongLuong, CNSX
) VALUES (
             'Kệ hoa chia buồn Hoàng Ân',
             'Hoa tang lễ',
             'Vòng hoa lớn kết tầng, chân hoa vàng và băng rôn chia buồn',
             'Kệ đứng 2 tầng, vòng tròn trung tâm',
             'Phù hợp mọi tôn giáo',
             2900000.00,
             2,
             25,
             'Trang trọng, nổi bật với sắc vàng chủ đạo',
             'Việt Nam',
             'Kệ hoa vàng thể hiện sự trân trọng, biết ơn và lời tiễn biệt chân thành. Phù hợp đặt tại nhà tang lễ, tư gia hoặc lễ viếng của cơ quan.',
             200000.00,
             'Vàng, xanh lá',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/hoatang2_dy55rt.jpg',
             'Hoa cúc vàng, hoa hồng vàng, hoa lan, lá cọ và lá phụ',
             1,
             'Cao khoảng 170 cm, rộng khoảng 85 cm',
             'Khoảng 16 kg',
             'Cắm hoa thủ công trên khung đứng gia cố, sử dụng mút giữ ẩm chuyên dụng'
         );
SET @sp6 = LAST_INSERT_ID();

INSERT INTO sanphamchitiet (MaSanPham, LoaiKhoi, NoiDung, ThuTu, CreatedAt, UpdatedAt) VALUES
                                                                                           (@sp6, 'TIEU_DE', 'Sắc vàng của lòng biết ơn và sự trân trọng', 1, NOW(), NOW()),
                                                                                           (@sp6, 'NOI_DUNG', 'Kệ hoa chia buồn Hoàng Ân mang sắc vàng chủ đạo, tượng trưng cho sự trân quý, lòng biết ơn và những ký ức tốt đẹp dành cho người đã khuất. Thiết kế vòng tròn trung tâm thể hiện sự trọn vẹn, kết hợp tầng hoa phía dưới tạo cảm giác vững chãi và trang trọng. Mẫu hoa phù hợp với lễ viếng của gia đình, bạn bè, đồng nghiệp hoặc đại diện cơ quan, tổ chức.', 2, NOW(), NOW()),
                                                                                           (@sp6, 'TIEU_DE', 'Bố cục nổi bật nhưng vẫn giữ sự trang nghiêm', 3, NOW(), NOW()),
                                                                                           (@sp6, 'NOI_DUNG', 'Vòng hoa lớn được kết từ hoa cúc và hoa hồng vàng, bao quanh bởi các lớp lá xanh tạo độ tương phản rõ ràng. Phần chân kệ sử dụng hoa vàng theo dáng hướng lên, giúp tổng thể cân đối và dễ nhìn từ xa. Nơ và băng rôn được bố trí gọn gàng, không che khuất hoa và bảo đảm nội dung chia buồn được thể hiện trang trọng.', 4, NOW(), NOW()),
                                                                                           (@sp6, 'TIEU_DE', 'Hoa tươi, màu sắc đồng đều', 5, NOW(), NOW()),
                                                                                           (@sp6, 'NOI_DUNG', 'Các cành hoa được tuyển chọn theo độ nở phù hợp, ưu tiên màu vàng tươi nhưng không quá chói. Mút giữ ẩm được bổ sung đầy đủ để duy trì độ tươi trong thời gian trưng bày. Trong trường hợp một loại hoa không có đúng mùa, An Yên sẽ chủ động trao đổi và thay thế bằng loại hoa tương đương, giữ nguyên tông màu và bố cục chính.', 6, NOW(), NOW()),
                                                                                           (@sp6, 'TIEU_DE', 'Băng rôn được cá nhân hóa theo người gửi', 7, NOW(), NOW()),
                                                                                           (@sp6, 'NOI_DUNG', 'Khách hàng có thể cung cấp tên người viếng, tên cơ quan và nội dung chia buồn. Đội ngũ thực hiện sẽ kiểm tra chính tả, cách xưng hô và độ dài câu chữ trước khi in. Với khách hàng cần giao gấp, An Yên hỗ trợ gợi ý mẫu nội dung ngắn gọn, lịch sự và phù hợp với mối quan hệ giữa người gửi và gia quyến.', 8, NOW(), NOW()),
                                                                                           (@sp6, 'TIEU_DE', 'Giao đúng giờ, lắp đặt hoàn chỉnh', 9, NOW(), NOW()),
                                                                                           (@sp6, 'NOI_DUNG', 'Kệ hoa được cố định chắc chắn trước khi vận chuyển, hạn chế nghiêng đổ và dập cánh. Nhân viên giao hoa hỗ trợ đặt đúng khu vực, chỉnh lại dáng hoa và xác nhận nội dung băng rôn tại địa điểm. Mỗi đơn hàng đều được ưu tiên về thời gian, bởi sự đúng giờ và chỉn chu là cách An Yên thể hiện sự tôn trọng đối với gia đình trong thời khắc quan trọng.', 10, NOW(), NOW());

INSERT INTO sanphamhinhanh (
    MaSanPham, MaChiTiet, LoaiHinhAnh, UrlHinhAnh, ThuTu, CreatedAt
) VALUES (
             @sp6, NULL, 'Chính',
             'https://res.cloudinary.com/dnj7xhvs1/image/upload/v1784131156/hoatang2_dy55rt.jpg',
             1, NOW()
         );

