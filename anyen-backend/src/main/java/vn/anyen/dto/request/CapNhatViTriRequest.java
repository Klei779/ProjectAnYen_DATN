package vn.anyen.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CapNhatViTriRequest {

    private BigDecimal latitude;

    private BigDecimal longitude;
}