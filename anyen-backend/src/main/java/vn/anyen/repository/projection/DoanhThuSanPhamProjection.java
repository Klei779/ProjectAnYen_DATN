package vn.anyen.repository.projection;

import java.math.BigDecimal;

public interface DoanhThuSanPhamProjection {
    Integer getMaSanPham();
    String getTenSanPham();
    Long getSoLuongBan();
    BigDecimal getDoanhThu();
}