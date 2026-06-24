package vn.anyen.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class QuanLyDoiTacResponse {

    private Integer maDoiTac;
    private String tenDoiTac;
    private String tenDoanhNghiep;
    private String maSoThue;
    private String soTaiKhoan;
    private String nganHang;
    private String tenDangNhap;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private String trangThai;
    private LocalDateTime createdAt;
}
