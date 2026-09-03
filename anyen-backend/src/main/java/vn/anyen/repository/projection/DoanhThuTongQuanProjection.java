package vn.anyen.repository.projection;

import java.math.BigDecimal;

public interface DoanhThuTongQuanProjection {
    BigDecimal getTongDoanhThu();
    Long getTongHoaDon();
    Long getTongDonHang();
    BigDecimal getDoanhThuTrungBinh();
}