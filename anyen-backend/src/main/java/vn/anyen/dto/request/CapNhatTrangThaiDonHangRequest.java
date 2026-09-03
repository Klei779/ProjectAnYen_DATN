package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapNhatTrangThaiDonHangRequest {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private Integer trangThai;
}