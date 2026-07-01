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
    private Integer vaiTro;
    private Integer trangThai;
    private String tenTrangThai;
    private String email;
    private String diaChi;
    private String soDienThoai;
}
