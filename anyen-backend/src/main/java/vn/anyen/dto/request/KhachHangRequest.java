package vn.anyen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String tenKhachHang;

    private String cccd;
    private String diaChi;
    private String soNhaDuong;
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String soDienThoai;
    private Integer maNhanVienPhuTrach;
    private LocalDateTime ngayDangKy;
    private String nguonDangKy;
    private String nhuCauHoTro;
    private String ghiChu;
}