package vn.anyen.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class QuanLyNhanVienResponse {

    private Integer maNhanVien;
    private String hoTen;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String trangThai;
    private String email;
    private String diaChi;
    private String soDienThoai;
}
