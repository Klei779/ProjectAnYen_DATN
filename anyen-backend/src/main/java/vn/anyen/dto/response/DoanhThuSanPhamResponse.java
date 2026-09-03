package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuSanPhamResponse {
    private Integer maSanPham;
    private String tenSanPham;
    private Long soLuongBan;
    private BigDecimal doanhThu;
}