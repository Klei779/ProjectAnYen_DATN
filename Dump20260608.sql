-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: dich_vu_mai_tang
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chitietdonhang`
--

DROP TABLE IF EXISTS `chitietdonhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chitietdonhang` (
  `MaDonHangChiTiet` int NOT NULL AUTO_INCREMENT,
  `MaDonHang` int DEFAULT NULL,
  `MaSanPham` int DEFAULT NULL,
  `SoLuong` int DEFAULT NULL,
  `giaTien` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`MaDonHangChiTiet`),
  KEY `MaDonHang` (`MaDonHang`),
  KEY `MaSanPham` (`MaSanPham`),
  CONSTRAINT `chitietdonhang_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`),
  CONSTRAINT `chitietdonhang_ibfk_2` FOREIGN KEY (`MaSanPham`) REFERENCES `sanpham` (`MaSanPham`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chitietdonhang`
--

LOCK TABLES `chitietdonhang` WRITE;
/*!40000 ALTER TABLE `chitietdonhang` DISABLE KEYS */;
/*!40000 ALTER TABLE `chitietdonhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doi_tac`
--

DROP TABLE IF EXISTS `doi_tac`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doi_tac` (
  `ma_doi_tac` int NOT NULL AUTO_INCREMENT,
  `mat_khau` varchar(255) DEFAULT NULL,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `ten_doi_tac` varchar(255) DEFAULT NULL,
  `trang_thai` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ma_doi_tac`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doi_tac`
--

LOCK TABLES `doi_tac` WRITE;
/*!40000 ALTER TABLE `doi_tac` DISABLE KEYS */;
/*!40000 ALTER TABLE `doi_tac` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doitac`
--

DROP TABLE IF EXISTS `doitac`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doitac` (
  `MaDoiTac` int NOT NULL AUTO_INCREMENT,
  `TenDoiTac` varchar(255) DEFAULT NULL,
  `TenDoanhNghiep` varchar(150) DEFAULT NULL,
  `MaSoThue` varchar(50) DEFAULT NULL,
  `SoTaiKhoan` varchar(50) DEFAULT NULL,
  `NganHang` varchar(100) DEFAULT NULL,
  `TenDangNhap` varchar(255) DEFAULT NULL,
  `MatKhau` varchar(255) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `SoDienThoai` varchar(20) DEFAULT NULL,
  `DiaChi` varchar(255) DEFAULT NULL,
  `TrangThai` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `ten_doi_tac` varchar(255) DEFAULT NULL,
  `trang_thai` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MaDoiTac`),
  UNIQUE KEY `TenDangNhap` (`TenDangNhap`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doitac`
--

LOCK TABLES `doitac` WRITE;
/*!40000 ALTER TABLE `doitac` DISABLE KEYS */;
INSERT INTO `doitac` VALUES (1,'Cong ty An Phuc','An Phuc Funeral','0311111111','123456789','Vietcombank','anphuc','$2a$10$O6MWtEIMBoXXZemhXCixXuetSJtnPomWv4lA8HO6KMLjAnVe/zT76','anphuc@gmail.com','0981111111','TP.HCM','Dang hop tac',NULL,NULL,NULL,NULL),(2,'Cong ty Tam Duc','Tam Duc Service','0322222222','987654321','ACB','tamduc','123456','tamduc@gmail.com','0982222222','Binh Duong','Dang hop tac',NULL,NULL,NULL,NULL),(3,'Cong ty Phuc Loc','Phuc Loc Co., Ltd','0333333333','456789123','Techcombank','phucloc','123456','phucloc@gmail.com','0983333333','Dong Nai','Tam ngung',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `doitac` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donhang`
--

DROP TABLE IF EXISTS `donhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donhang` (
  `MaDonHang` int NOT NULL AUTO_INCREMENT,
  `MaKhachHang` int DEFAULT NULL,
  `MaNhanVien` int DEFAULT NULL,
  `NgayTaoDon` date DEFAULT NULL,
  `tongTien` decimal(38,2) DEFAULT NULL,
  `trangThai` varchar(255) DEFAULT NULL,
  `GhiChu` text,
  PRIMARY KEY (`MaDonHang`),
  KEY `MaKhachHang` (`MaKhachHang`),
  KEY `MaNhanVien` (`MaNhanVien`),
  CONSTRAINT `donhang_ibfk_1` FOREIGN KEY (`MaKhachHang`) REFERENCES `khachhang` (`MaKhachHang`),
  CONSTRAINT `donhang_ibfk_2` FOREIGN KEY (`MaNhanVien`) REFERENCES `nhanvien` (`MaNhanVien`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donhang`
--

LOCK TABLES `donhang` WRITE;
/*!40000 ALTER TABLE `donhang` DISABLE KEYS */;
INSERT INTO `donhang` VALUES (1,1,2,'2026-06-01',13500000.00,'Đã xác nhận','Khách cần giao trong ngày'),(2,2,3,'2026-06-02',23700000.00,'Đang xử lý','Khách chọn gói cao cấp'),(3,3,2,'2026-06-03',5500000.00,'Chờ thanh toán','Khách cần tư vấn thêm'),(4,1,2,'2026-06-01',13500000.00,'Đã xác nhận','Khách cần giao trong ngày'),(5,2,3,'2026-06-02',23700000.00,'Đang xử lý','Khách chọn gói cao cấp'),(6,3,2,'2026-06-03',5500000.00,'Chờ thanh toán','Khách cần tư vấn thêm');
/*!40000 ALTER TABLE `donhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hoadon`
--

DROP TABLE IF EXISTS `hoadon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hoadon` (
  `MaHoaDon` int NOT NULL AUTO_INCREMENT,
  `MaDonHang` int DEFAULT NULL,
  `NgayIn` date DEFAULT NULL,
  PRIMARY KEY (`MaHoaDon`),
  KEY `MaDonHang` (`MaDonHang`),
  CONSTRAINT `hoadon_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoadon`
--

LOCK TABLES `hoadon` WRITE;
/*!40000 ALTER TABLE `hoadon` DISABLE KEYS */;
/*!40000 ALTER TABLE `hoadon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hopdong`
--

DROP TABLE IF EXISTS `hopdong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hopdong` (
  `MaHopDong` int NOT NULL AUTO_INCREMENT,
  `MaDonHang` int DEFAULT NULL,
  `NgayKyHD` date DEFAULT NULL,
  `NgayViet` date DEFAULT NULL,
  `trangThai` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MaHopDong`),
  KEY `MaDonHang` (`MaDonHang`),
  CONSTRAINT `hopdong_ibfk_1` FOREIGN KEY (`MaDonHang`) REFERENCES `donhang` (`MaDonHang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hopdong`
--

LOCK TABLES `hopdong` WRITE;
/*!40000 ALTER TABLE `hopdong` DISABLE KEYS */;
/*!40000 ALTER TABLE `hopdong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khachhang`
--

DROP TABLE IF EXISTS `khachhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khachhang` (
  `MaKhachHang` int NOT NULL AUTO_INCREMENT,
  `tenKhachHang` varchar(255) DEFAULT NULL,
  `cccd` varchar(255) DEFAULT NULL,
  `DiaChi` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `soDienThoai` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MaKhachHang`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES (1,'Nguyen Van An','079201000001','TP.HCM','an@gmail.com','0901111111'),(2,'Tran Thi Binh','079202000002','Dong Nai','binh@gmail.com','0902222222'),(3,'Le Van Cuong','079203000003','Binh Duong','cuong@gmail.com','0903333333');
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhan_vien`
--

DROP TABLE IF EXISTS `nhan_vien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhan_vien` (
  `ma_nhan_vien` int NOT NULL AUTO_INCREMENT,
  `ho_ten` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `trang_thai` varchar(255) DEFAULT NULL,
  `vai_tro` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ma_nhan_vien`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhan_vien`
--

LOCK TABLES `nhan_vien` WRITE;
/*!40000 ALTER TABLE `nhan_vien` DISABLE KEYS */;
/*!40000 ALTER TABLE `nhan_vien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhanvien`
--

DROP TABLE IF EXISTS `nhanvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhanvien` (
  `MaNhanVien` int NOT NULL AUTO_INCREMENT,
  `HoTen` varchar(255) DEFAULT NULL,
  `TenDangNhap` varchar(255) DEFAULT NULL,
  `MatKhau` varchar(255) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `SoDienThoai` varchar(20) DEFAULT NULL,
  `DiaChi` varchar(255) DEFAULT NULL,
  `VaiTro` varchar(255) DEFAULT NULL,
  `TrangThai` varchar(255) DEFAULT NULL,
  `ho_ten` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `trang_thai` varchar(255) DEFAULT NULL,
  `vai_tro` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MaNhanVien`),
  UNIQUE KEY `TenDangNhap` (`TenDangNhap`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (1,'Pham Minh Duc','ducpm','$2a$10$O6MWtEIMBoXXZemhXCixXuetSJtnPomWv4lA8HO6KMLjAnVe/zT76','duc@company.com','0911111111','TP.HCM','Admin','Dang lam viec',NULL,NULL,NULL,NULL,NULL),(2,'Vo Thanh Hai','haivt','123456','hai@company.com','0922222222','TP.HCM','Nhan vien ban hang','Dang lam viec',NULL,NULL,NULL,NULL,NULL),(3,'Dang Ngoc Lan','landn','123456','lan@company.com','0933333333','Dong Nai','Nhan vien tu van','Dang lam viec',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sanpham`
--

DROP TABLE IF EXISTS `sanpham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sanpham` (
  `MaSanPham` int NOT NULL AUTO_INCREMENT,
  `tenSanPham` varchar(255) DEFAULT NULL,
  `loai` varchar(255) DEFAULT NULL,
  `noiThat` varchar(255) DEFAULT NULL,
  `quyCach` varchar(255) DEFAULT NULL,
  `tonGiao` varchar(255) DEFAULT NULL,
  `giaTien` decimal(38,2) DEFAULT NULL,
  `MaDoiTac` int DEFAULT NULL,
  `SoLuong` int DEFAULT NULL,
  `thietKe` varchar(255) DEFAULT NULL,
  `xuatXu` varchar(255) DEFAULT NULL,
  `GhiChu` text,
  `khuyenMai` decimal(38,2) DEFAULT NULL,
  `mauSac` varchar(255) DEFAULT NULL,
  `HinhAnh` varchar(255) DEFAULT NULL,
  `vatLieu` varchar(255) DEFAULT NULL,
  `trangThai` varchar(255) DEFAULT NULL,
  `kichThuoc` varchar(255) DEFAULT NULL,
  `trongLuong` varchar(255) DEFAULT NULL,
  `CNSX` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MaSanPham`),
  KEY `MaDoiTac` (`MaDoiTac`),
  CONSTRAINT `sanpham_ibfk_1` FOREIGN KEY (`MaDoiTac`) REFERENCES `doitac` (`MaDoiTac`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (1,'Quan tai go thong','Quan tai','Co dem lot','Tieu chuan','Phat giao',12000000.00,1,10,'Truyen thong','Viet Nam','Hang pho thong',500000.00,'Nau','quan_tai_go_thong.jpg','Go thong','Con hang','2m x 0.7m','80kg','2025'),(2,'Binh tro cot gom su','Binh tro cot','Khong','Nho','Khong phan biet',2500000.00,2,25,'Hoa van sen','Viet Nam','San pham cao cap',200000.00,'Trang','binh_tro_cot.jpg','Gom su','Con hang','30cm x 20cm','3kg','2025'),(3,'Bo le tang tron goi','Dich vu','Day du','Tron goi','Cong giao',35000000.00,1,5,'Trang trong','Viet Nam','Goi dich vu co ban',1000000.00,'Trang den','bo_le_tang.jpg','Tong hop','Con hang','Theo yeu cau','Khong co','2025'),(4,'Xe tang lễ 16 chỗ','Dịch vụ vận chuyển','Máy lạnh','Theo chuyến','Không phân biệt',5000000.00,3,3,'Trang nghiêm','Việt Nam','Có tài xế',300000.00,'Đen','xe-tang-le.jpg','Xe chuyên dụng','Còn dịch vụ','16 chỗ','2500kg','Đăng kiểm định kỳ'),(5,'Bàn thờ tang lễ','Vật phẩm tang lễ','Có khăn phủ','Trọn bộ','Phật giáo',3200000.00,1,12,'Truyền thống','Việt Nam','Bao gồm lư hương và đèn',100000.00,'Nâu vàng','ban-tho.jpg','Gỗ MDF','Còn hàng','120x60x100cm','35kg','Lắp ráp nhanh');
/*!40000 ALTER TABLE `sanpham` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-08 16:05:31
