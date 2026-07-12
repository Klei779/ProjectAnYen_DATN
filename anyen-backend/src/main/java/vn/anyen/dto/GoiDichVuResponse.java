package vn.anyen.dto;

import vn.anyen.entity.ComBo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoiDichVuResponse {

    private Integer comboId;
    private String tenCombo;
    private BigDecimal gia;
    private String moTa;
    private String hinhAnh;
    private Integer trangThai;

    public static GoiDichVuResponse fromEntity(ComBo combo) {
        return new GoiDichVuResponse(
                combo.getComboId(),
                combo.getTenCombo(),
                combo.getGia(),
                combo.getMoTa(),
                combo.getHinhAnh(),
                combo.getTrangThai()
        );
    }
}