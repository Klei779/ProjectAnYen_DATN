package vn.anyen.repository.projection;

import java.math.BigDecimal;

public interface DoanhThuTheoThoiGianProjection {
    String getThoiGian();
    BigDecimal getDoanhThu();
    Long getSoDonHang();
}