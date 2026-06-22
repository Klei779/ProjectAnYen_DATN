package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuTongQuanResponse {
    private BigDecimal tongDoanhThu;
    private Long tongHoaDon;
    private Long tongDonHang;
    private BigDecimal doanhThuTrungBinh;
}