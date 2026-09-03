package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TuChoiSanPhamRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(min = 3, message = "Lý do từ chối phải từ 3 ký tự trở lên")
    private String lyDoTuChoi;
}