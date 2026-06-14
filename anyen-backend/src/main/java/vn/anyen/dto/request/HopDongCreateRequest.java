package vn.anyen.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDongCreateRequest {

    @NotNull(message = "Vui lòng chọn đơn hàng")
    private Integer maDonHang;

    private LocalDate ngayKyHD;

    private LocalDate ngayViet;

    private String trangThai;
}