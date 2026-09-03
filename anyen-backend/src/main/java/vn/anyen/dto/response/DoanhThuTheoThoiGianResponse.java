package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuTheoThoiGianResponse {
    private String thoiGian;
    private BigDecimal doanhThu;
    private Long soDonHang;
}