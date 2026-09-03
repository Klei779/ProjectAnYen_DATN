package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongNoTongQuanResponse {

    // Tổng tiền công ty còn phải trả
    private BigDecimal tongCongNoPhaiTra;

    // Số đối tác còn nợ
    private Long soDoiTacConNo;

    // Công nợ đến hạn trong 7 ngày
    private BigDecimal denHanThanhToan;

    private Long soDoiTacDenHan;

    // Công nợ đã quá hạn
    private BigDecimal quaHanThanhToan;

    private Long soDoiTacQuaHan;

    // Tổng tiền đã thanh toán tháng hiện tại
    private BigDecimal daThanhToanThangNay;

    // Số lần thanh toán tháng hiện tại
    private Long soGiaoDichThangNay;
}