package vn.anyen.dto.request;

        import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

        @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapNhatNhanVienRequest {

            @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 50, message = "Họ tên tối đa 50 ký tự")
    private String hoTen;

            @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    private String tenDangNhap;

            // Không gửi hoặc gửi null khi không muốn đổi mật khẩu.
            @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String matKhau;

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





            @NotNull(message = "Vai trò không được để trống")
    @Min(value = 1, message = "Vai trò không hợp lệ")
    @Max(value = 3, message = "Vai trò không hợp lệ")
    private Integer vaiTro;
}