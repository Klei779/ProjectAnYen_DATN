package vn.anyen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuanLyDoiTacRequest {

    @NotBlank(message = "Tên đối tác không được để trống")
    @Size(max = 100, message = "Tên đối tác tối đa 100 ký tự")
    private String tenDoiTac;

    @Size(max = 150, message = "Tên doanh nghiệp tối đa 150 ký tự")
    private String tenDoanhNghiep;

    @Size(max = 50, message = "Mã số thuế tối đa 50 ký tự")
    private String maSoThue;

    @Size(max = 50, message = "Số tài khoản tối đa 50 ký tự")
    private String soTaiKhoan;

    @Size(max = 100, message = "Tên ngân hàng tối đa 100 ký tự")
    private String nganHang;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0[35789][0-9]{8}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09"
    )
    private String soDienThoai;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;
}
