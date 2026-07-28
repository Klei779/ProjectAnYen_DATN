package vn.anyen.dto.response;

import jakarta.persistence.Column;
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
    private Integer vaiTro;
    private String tenVaiTro;
    private String tenTrangThai;
    private Integer trangThai;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String soNhaDuong;
}