package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHangHopDongDetailResponse {

    private Integer maDonHang;
    private String maDonHangText;
    private LocalDate ngayTaoDon;
    private BigDecimal tongTien;
    private String trangThai;
    private String ghiChu;

    private Integer maKhachHang;
    private String tenKhachHang;
    private String cccd;
    private String email;
    private String soDienThoai;
    private String diaChi;

    private Integer maNhanVien;
    private String tenNhanVien;

    private List<SanPhamTrongDonHangResponse> sanPhams;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SanPhamTrongDonHangResponse {

        private Integer maSanPham;
        private String tenSanPham;
        private String loai;
        private Integer soLuong;
        private BigDecimal giaTien;
        private BigDecimal thanhTien;
        private String hinhAnh;
    }
}