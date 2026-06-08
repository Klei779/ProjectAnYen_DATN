package vn.anyen.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoiDichVuResponse {

    private Integer comboId;

    private String tenCombo;

    private BigDecimal gia;

    private String moTa;

    private String hinhAnh;
}