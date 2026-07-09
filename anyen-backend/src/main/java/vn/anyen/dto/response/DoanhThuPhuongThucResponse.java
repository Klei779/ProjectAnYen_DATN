package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuPhuongThucResponse {
    private String phuongThucThanhToan;
    private Long soHoaDon;
    private BigDecimal doanhThu;
}