package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongNoResponse {
    private Integer maCongNo;
    private Integer maDonHang;
    private Integer maDoiTac;
    private String tenDoiTac;
    private BigDecimal tongTien;
    private BigDecimal daThanhToan;
    private BigDecimal conLai;
    private LocalDate hanThanhToan;
    private Integer trangThai;
    private String trangThaiText;
    private String ghiChu;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin đơn hàng
    private String ngayTaoDon;
    private String trangThaiDonHang;
}
