package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapNhatTrangThaiDonHangRequest {

    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    private String trangThai;
}