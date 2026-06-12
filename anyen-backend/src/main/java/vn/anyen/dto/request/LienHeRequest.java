package vn.anyen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LienHeRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String sdt;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String tieude;

    @NotBlank(message = "Nội dung không được để trống")
    private String noidung;
}