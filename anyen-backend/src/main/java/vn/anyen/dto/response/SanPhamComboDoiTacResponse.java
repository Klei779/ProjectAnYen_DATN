package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SanPhamComboDoiTacResponse {
    private Integer maSanPham;
    private String tenSanPham;
    private BigDecimal giaTien;
    private String hinhAnh;
    private Integer soLuong;
    private Integer trangThai;
}
