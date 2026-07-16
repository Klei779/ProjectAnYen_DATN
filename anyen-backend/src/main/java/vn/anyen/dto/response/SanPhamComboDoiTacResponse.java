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
    /** Số lượng tồn kho hiện tại. */
    private Integer soLuong;
    /** Số lượng sản phẩm được sử dụng trong combo. */
    private Integer soLuongTrongCombo;
    /** Thành tiền = giá sản phẩm x số lượng trong combo. */
    private BigDecimal thanhTien;
    private Integer trangThai;
}
