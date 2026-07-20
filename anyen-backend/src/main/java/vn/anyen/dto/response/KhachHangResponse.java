package vn.anyen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangResponse {

    private Integer maKhachHang;
    private String tenKhachHang;
    private String cccd;
    private String diaChi;
    private String email;
    private String soDienThoai;
    private Integer maNhanVienPhuTrach;
    private String ngayDangKy;
    private String nguonDangKy;
    private String nhuCauHoTro;
    private String ghiChu;

    private String trangThaiHienTai;
    private String giaiDoanHienTai;
    private String avatar;
    private String tenNhanVienPhuTrach;
    private String emailNhanVienPhuTrach;
    private String soDienThoaiNhanVienPhuTrach;
}