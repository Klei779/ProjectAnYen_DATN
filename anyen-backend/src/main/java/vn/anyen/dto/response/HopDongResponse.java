package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDongResponse {

    private Integer maHopDong;

    private String soHopDong;
    private String maHopDongText;

    private Integer maDonHang;
    private String maDonHangText;

    private String tenKhachHang;
    private String soDienThoai;

    private LocalDate ngayTaoDon;
    private LocalDate ngayKyHD;
    private LocalDate ngayViet;

    private LocalDate thoiHanKetThuc;
    private LocalDate ngayKetThuc;
    private LocalDate ngayHetHan;

    private BigDecimal giaTriHopDong;

    private String trangThai;

    // Dữ liệu bảng hdongct
    private Integer maHDongCT;

    private String hoTenNguoiMat;
    private LocalDate ngayMat;
    private LocalDate ngaySinh;
    private String gioiTinh;

    private String soGiayBaoTu;
    private String noiCapGiayBaoTu;

    private String coSoMaiTang;
    private String khuMo;
    private String soMo;

    private LocalDateTime ngayGioAnTang;
}