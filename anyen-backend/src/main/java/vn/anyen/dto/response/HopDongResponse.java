package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDongResponse {

    private Integer maHopDong;
    private String maHopDongText;

    private Integer maDonHang;
    private String maDonHangText;

    private String tenKhachHang;
    private String soDienThoai;

    private LocalDate ngayTaoDon;
    private LocalDate ngayKyHD;
    private LocalDate ngayViet;

    private BigDecimal giaTriHopDong;

    private String trangThai;
}