package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TuChoiHoaDonRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(min = 4, max = 500, message = "Lý do từ chối phải từ 4 đến 500 ký tự")
    private String lyDoTuChoi;
}