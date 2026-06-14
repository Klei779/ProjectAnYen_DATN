package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHangHopDongOptionResponse {

    private Integer maDonHang;
    private String maDonHangText;

    private String tenKhachHang;
    private String soDienThoai;

    private LocalDate ngayTaoDon;
    private BigDecimal tongTien;
    private String trangThai;

    private Boolean daCoHopDong;
}