package vn.anyen.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private Integer id;
    private String tenDangNhap;
    private String hoTen;
    private String loaiTaiKhoan;
}