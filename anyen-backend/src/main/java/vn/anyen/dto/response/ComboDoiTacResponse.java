package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ComboDoiTacResponse {
    private Integer comboId;
    private Integer maDoiTac;
    private String tenCombo;
    private BigDecimal gia;
    private BigDecimal tongGiaSanPham;
    private String moTa;
    private String hinhAnh;
    private Integer trangThai;
    private String tenTrangThai;
    private List<SanPhamComboDoiTacResponse> sanPhams;
}
