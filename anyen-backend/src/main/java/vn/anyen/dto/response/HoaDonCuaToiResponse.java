package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonCuaToiResponse {

    private Integer maHoaDon;
    private String soHoaDon;

    private Integer maDonHang;
    private String maDonHangCode;

    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;

    private String tenNhanVien;

    private LocalDate ngayIn;
    private BigDecimal tongTien;
    private String phuongThucThanhToan;
    private String trangThai;
}