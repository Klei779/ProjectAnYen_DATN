package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CapNhatViTriRequest {
@NotBlank
    private BigDecimal latitude;
@NotBlank
    private BigDecimal longitude;
}