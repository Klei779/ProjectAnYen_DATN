package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamTaoDonHangResponse {

    private Integer maSanPham;
    private String tenSanPham;
    private String loai;
    private BigDecimal giaTien;

    private Integer tonKho;

    private Integer maDoiTac;
    private String tenDoiTac;

    private String hinhAnh;
    private Integer trangThai;
}