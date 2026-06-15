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
public class DonHangResponse {
    private Integer MaDonHang;
    private String maCode;
    private Integer MaKhachHang;
    private String tenKhachHang;
    private String avatarKH;
    private String emailKH;
    private String soDienThoaiKH;
    private String diaChiKH;
    private String loaiKH;
    private Integer tongDonKH;
    private BigDecimal tongChiTieuKH;
    private String ghiChuKH;
    private String ghiChuNoiBo;
    private Integer MaNhanVien;
    private String tenNhanVien;
    private LocalDate NgayTaoDon;
    private BigDecimal tongTien;
    private String trangThai;
    private String GhiChu;
    private String phuongThucThanhToan;
    private String trangThaiThanhToan;
    private String phuongThucGiaoHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal giamGia;
    private Integer maHoaDon;
    private Boolean daCoHoaDon;
    private String trangThaiHoaDon;
    
    private List<ChiTietDonHangResponse> sanPhams;
    private List<LichSuDonHangResponse> lichSu;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChiTietDonHangResponse {
        private Integer MaSanPham;
        private String tenSanPham;
        private String maSKU;
        private String phanLoai;
        private String HinhAnh;
        private BigDecimal giaTien;
        private Integer SoLuong;
        private BigDecimal thanhTien;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LichSuDonHangResponse {
        private String trangThai;
        private String thoiGian;
        private String moTa;
        private String color;
        private boolean done;
    }
}
