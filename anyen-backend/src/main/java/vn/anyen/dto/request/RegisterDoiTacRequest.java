package vn.anyen.dto.request;

import lombok.Data;

@Data
public class RegisterDoiTacRequest {
    private String token;
    private String tenDoiTac;
    private String tenDoanhNghiep;
    private String maSoThue;
    private String tenDangNhap;
    private String matKhau;
    private String soDienThoai;
    private String diaChi;
}
