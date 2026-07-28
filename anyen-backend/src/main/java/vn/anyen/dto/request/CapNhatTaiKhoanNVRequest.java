package vn.anyen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CapNhatTaiKhoanNVRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Pattern(
            regexp = "^$|^[0-9]{9,20}$",
            message = "Số điện thoại chỉ gồm 9 - 20 chữ số"
    )
    private String soDienThoai;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;
    @NotBlank(message = "Quận Huyện không được để trống")
    @Size(max = 100, message = "Quận Huyện tối đa 100 ký tự")
    private String quanHuyen;

    @NotBlank(message = "Tỉnh thành không được để trống")
    @Size(max = 100, message = "Tỉnh thành tối đa 100 ký tự")
    private String tinhThanh;

    @NotBlank(message = "Phường xá không được để trống")
    @Size(max = 100, message = "Phường xá tối đa 100 ký tự")
    private String phuongXa;
    @NotBlank(message = "Số nhà đường không được để trống")
    @Size(max = 100, message = "Số nhà đường tối đa 100 ký tự")
    private String soNhaDuong;

}