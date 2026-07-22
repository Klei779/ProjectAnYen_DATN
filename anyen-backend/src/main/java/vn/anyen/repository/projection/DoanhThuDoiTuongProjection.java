package vn.anyen.repository.projection;

import java.math.BigDecimal;

public interface DoanhThuDoiTuongProjection {

    Integer getMaDoiTuong();

    String getTenDoiTuong();

    Long getSoDonHang();

    BigDecimal getDoanhThu();
}