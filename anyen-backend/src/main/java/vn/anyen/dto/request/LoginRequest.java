package vn.anyen.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String tenDangNhap;
    private String matKhau;
    private String loaiTaiKhoan;
}