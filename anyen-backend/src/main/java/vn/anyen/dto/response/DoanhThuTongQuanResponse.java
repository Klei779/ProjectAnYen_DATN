package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuTongQuanResponse {

    // Tổng giá trị đơn hàng, luôn là 100%
    private BigDecimal tongDoanhThu;

    // Số tiền thực nhận sau khi áp dụng tỷ lệ
    private BigDecimal doanhThuThucNhan;

    // 80 đối với đối tác, 20 đối với admin
    private Integer tyLeDoanhThu;

    private Long tongHoaDon;
    private Long tongDonHang;
    private BigDecimal doanhThuTrungBinh;
}