package vn.anyen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuenMatKhauRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Loại tài khoản không được để trống")
    @Pattern(
            regexp = "NHAN_VIEN|DOI_TAC",
            message = "Loại tài khoản không hợp lệ"
    )
    private String loaiTaiKhoan;
}