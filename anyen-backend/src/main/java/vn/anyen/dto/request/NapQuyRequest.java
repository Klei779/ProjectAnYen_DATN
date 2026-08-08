package vn.anyen.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class NapQuyRequest {

    @NotNull(
            message =
                    "Số tiền nạp không được để trống"
    )
    @DecimalMin(
            value = "1000",
            message =
                    "Số tiền nạp tối thiểu là 1.000đ"
    )
    private BigDecimal soTien;
}