-- Reset lịch sử giao dịch quỹ/ví để test lại từ đầu

SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Xóa lịch sử giao dịch đối tác (nếu bảng tồn tại)
DROP TABLE IF EXISTS `lichsugiaodichdoitac`;

-- 2. Tạo lại bảng lichsugiaodichdoitac
CREATE TABLE `lichsugiaodichdoitac` (
  `MaGiaoDich` int NOT NULL AUTO_INCREMENT,
  `MaDoiTac` int NOT NULL,
  `LoaiVi` varchar(10) NOT NULL COMMENT 'QUY hoặc VI',
  `LoaiGiaoDich` varchar(10) NOT NULL COMMENT '+' hoặc '-'',
  `SoTien` decimal(18,2) NOT NULL,
  `NoiDung` varchar(500) DEFAULT NULL,
  `ThoiGian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MaGiaoDich`),
  KEY `idx_lichsugiaodich_madoitac` (`MaDoiTac`),
  CONSTRAINT `fk_lichsugiaodich_doitac` FOREIGN KEY (`MaDoiTac`) REFERENCES `doitac` (`MaDoiTac`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Xóa giao dịch Payoo Mock
TRUNCATE TABLE `payoomocktransaction`;

-- 4. Reset số dư quỹ/ví của đối tác về 0 (tùy chọn)
UPDATE `doitac` SET 
  `SoDuQuy` = 0.00,
  `SoDuQuyDangKhoa` = 0.00,
  `SoDuVi` = 0.00
WHERE `MaDoiTac` IN (1, 2, 3, 4);

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
