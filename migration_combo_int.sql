-- Migration: Đổi TrangThai và Loai sang INT cho bảng combo và combochitiet
-- Chạy lệnh này trên database hiện có

-- Đổi TrangThai từ VARCHAR sang INT trong bảng combo
ALTER TABLE combo MODIFY COLUMN TrangThai INT COMMENT '0=Ẩn, 1=Hoạt động, 2=Ngừng kinh doanh';

-- Cập nhật dữ liệu hiện tại: chuyển 'Hoạt động' sang 1
UPDATE combo SET TrangThai = 1 WHERE TrangThai = 'Hoạt động';

-- Đổi Loai từ VARCHAR sang INT trong bảng combochitiet  
ALTER TABLE combochitiet MODIFY COLUMN Loai INT COMMENT '0=Sản phẩm, 1=Dịch vụ';

-- Cập nhật dữ liệu hiện tại: chuyển 'Sản phẩm' sang 0
UPDATE combochitiet SET Loai = 0 WHERE Loai = 'Sản phẩm';
