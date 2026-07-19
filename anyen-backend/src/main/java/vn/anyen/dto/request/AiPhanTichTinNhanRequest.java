package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPhanTichTinNhanRequest {

    @NotBlank(message = "Token phiên không được để trống")
    private String tokenPhien;

    @NotBlank(message = "Tin nhắn không được để trống")
    @Size(
            max = 3000,
            message = "Tin nhắn không được vượt quá 3000 ký tự"
    )
    private String message;
}