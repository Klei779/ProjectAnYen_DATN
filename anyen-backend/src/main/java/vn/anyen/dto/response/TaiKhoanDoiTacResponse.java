package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TaiKhoanDoiTacResponse {

    private Integer maDoiTac;
    private String tenDoiTac;
    private String tenDoanhNghiep;
    private String maSoThue;
    private String tenDangNhap;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private Integer trangThai;
    private BigDecimal longitude;
    private BigDecimal latitude;
}