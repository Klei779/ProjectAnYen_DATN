package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoiDichVuResponse {

    private Integer comboId;
    private String tenCombo;
    private Long gia;
    private String moTa;
    private String hinhAnh;

    public static GoiDichVuResponse fromEntity(Combo combo) {
        return new GoiDichVuResponse(
                combo.getComboId(),
                combo.getTenCombo(),
                combo.getGia(),
                combo.getMoTa(),
                combo.getHinhAnh()
        );
    }
}