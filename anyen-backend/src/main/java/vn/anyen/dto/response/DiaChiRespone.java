package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DiaChiRespone {

    private BigDecimal lat;
    private BigDecimal lon;
    private String diaChiChiTiet;
}
