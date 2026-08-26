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
    private String tenDangNhap;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private String quanHuyen;
    private String tinhThanh;
    private Integer trangThai;
    private LocalDateTime createdAt;
    private Boolean coTheXoa;
}
