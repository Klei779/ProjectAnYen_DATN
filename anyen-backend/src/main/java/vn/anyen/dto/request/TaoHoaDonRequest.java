package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TaoHoaDonRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Integer maDonHang;

    @NotNull(message = "Ngày in hóa đơn không được để trống")
    private LocalDate ngayIn;

    private BigDecimal tongTien;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private Integer phuongThucThanhToan;

    @NotNull(message = "Trạng thái hóa đơn không được để trống")
    private Integer trangThai;
}
