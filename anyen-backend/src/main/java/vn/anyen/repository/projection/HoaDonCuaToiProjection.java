package vn.anyen.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface HoaDonCuaToiProjection {

    Integer getMaHoaDon();

    Integer getMaDonHang();

    String getTenKhachHang();

    String getSoDienThoai();

    String getEmail();

    String getDiaChi();

    String getTenNhanVien();

    LocalDate getNgayIn();

    BigDecimal getTongTien();

    String getPhuongThucThanhToan();

    String getTrangThai();
}