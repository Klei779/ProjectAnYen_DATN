package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuDoiTuongResponse {

    private Integer maDoiTuong;

    private String tenDoiTuong;

    private Long soDonHang;

    private BigDecimal doanhThu;
}