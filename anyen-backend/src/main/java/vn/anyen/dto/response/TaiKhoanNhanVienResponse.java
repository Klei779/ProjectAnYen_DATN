package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TaiKhoanNhanVienResponse {

    private Integer maNhanVien;
    private String hoTen;
    private String tenDangNhap;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private Integer vaiTro;
    private Integer trangThai;
}