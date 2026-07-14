-- Thêm dữ liệu demo cho nhân viên với tọa độ
-- Chạy lệnh này trong MySQL Workbench hoặc phpMyAdmin

INSERT INTO nhanvien (HoTen, TenDangNhap, MatKhau, VaiTro, TrangThai, Email, DiaChi, SoDienThoai, Latitude, Longitude, TinhThanh, QuanHuyen, PhuongXa, SoNhaDuong, TrangThaiLamViec, CapNhatToaDoTai)
VALUES 
('Nguyễn Văn A', 'nhanvien_a', '$2a$10$X7Op/b0r1Vz1zKzQzQzQzOzQzQzQzQzQzQzQzQzQzQzQzQzQzQz', 3, 1, 'nhanvien_a@example.com', '123 Nguyễn Huệ, Q1, TP HCM', '0901234567', 10.776889, 106.700806, 'TP Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '123 Nguyễn Huệ', 'RANH', NOW()),
('Trần Thị B', 'nhanvien_b', '$2a$10$X7Op/b0r1Vz1zKzQzQzQzOzQzQzQzQzQzQzQzQzQzQzQzQzQzQz', 3, 1, 'nhanvien_b@example.com', '456 Đặng Thùy Trâm, Q3, TP HCM', '0912345678', 10.785000, 106.680000, 'TP Hồ Chí Minh', 'Quận 3', 'Phường 6', '456 Đặng Thùy Trâm', 'RANH', NOW()),
('Lê Văn C', 'nhanvien_c', '$2a$10$X7Op/b0r1Vz1zKzQzQzQzOzQzQzQzQzQzQzQzQzQzQzQzQzQzQz', 3, 1, 'nhanvien_c@example.com', '789 Lê Hồng Phong, Vũng Tàu', '0923456789', 10.346000, 107.084300, 'Bà Rịa - Vũng Tàu', 'TP Vũng Tàu', 'Phường 1', '789 Lê Hồng Phong', 'BAN', NOW()),
('Phạm Thị D', 'nhanvien_d', '$2a$10$X7Op/b0r1Vz1zKzQzQzQzOzQzQzQzQzQzQzQzQzQzQzQzQzQzQz', 3, 1, 'nhanvien_d@example.com', '101 Trần Phú, Q5, TP HCM', '0934567890', 10.750000, 106.660000, 'TP Hồ Chí Minh', 'Quận 5', 'Phường 2', '101 Trần Phú', 'RANH', NOW()),
('Hoàng Văn E', 'nhanvien_e', '$2a$10$X7Op/b0r1Vz1zKzQzQzQzOzQzQzQzQzQzQzQzQzQzQzQzQzQzQz', 3, 1, 'nhanvien_e@example.com', '202 Hai Bà Trưng, Q1, TP HCM', '0945678901', 10.780000, 106.690000, 'TP Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '202 Hai Bà Trưng', 'RANH', NOW())
ON DUPLICATE KEY UPDATE 
    Latitude = VALUES(Latitude),
    Longitude = VALUES(Longitude),
    TinhThanh = VALUES(TinhThanh),
    QuanHuyen = VALUES(QuanHuyen),
    PhuongXa = VALUES(PhuongXa),
    SoNhaDuong = VALUES(SoNhaDuong),
    TrangThaiLamViec = VALUES(TrangThaiLamViec),
    CapNhatToaDoTai = VALUES(CapNhatToaDoTai);
