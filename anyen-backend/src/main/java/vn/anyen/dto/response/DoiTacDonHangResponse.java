package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTacDonHangResponse {

    private Integer maDonHang;
    private String maCode;
    private String tenKhachHang;
    private String cccd;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String ngayDat;
    private String nhanVien;
    private String ghiChu;
    private String trangThai;
    private BigDecimal tongCong;
    private Boolean coHopDong;

    private List<LichSuTrangThaiResponse> trangThaiLichSu;
    private TepDinhKemResponse tepDinhKem;
    private List<SanPhamTrongDonResponse> sanPhams;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SanPhamTrongDonResponse {
        private Integer stt;
        private String ten;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
        private String ngayGiaoDuKien;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LichSuTrangThaiResponse {
        private String buoc;
        private String thoiGian;
        private String trangThai;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TepDinhKemResponse {
        private String ten;
        private String dungLuong;
        private String url;
    }
}