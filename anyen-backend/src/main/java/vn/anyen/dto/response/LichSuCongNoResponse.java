package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuCongNoResponse {

    private Integer maLichSuCongNo;

    private Integer maCongNo;

    private Integer maDonHang;

    private Integer maDoiTac;

    private String tenDoiTac;

    private BigDecimal soTienThanhToan;

    private Integer phuongThucThanhToan;

    private String phuongThucThanhToanText;

    private LocalDateTime ngayThanhToan;

    private String maGiaoDich;

    private String ghiChu;
}