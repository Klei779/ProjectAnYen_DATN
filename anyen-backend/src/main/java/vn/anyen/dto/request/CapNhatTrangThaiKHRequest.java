package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapNhatTrangThaiKHRequest {

    @NotBlank(message = "Trạng thái làm việc không được để trống")
    private String trangThaiLamViec;
}