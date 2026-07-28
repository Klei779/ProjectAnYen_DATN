package vn.anyen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.anyen.entity.ComBo;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoiDichVuResponse {

    private Integer comboId;
    private String tenCombo;
    private BigDecimal gia;
    private String moTa;

    // Ảnh đại diện cũ, giữ lại để không ảnh hưởng code hiện tại
    private String hinhAnh;

    private String ghiChu;

    // Danh sách toàn bộ ảnh của combo
    private List<String> hinhAnhs;

    private Integer trangThai;
    public static GoiDichVuResponse fromEntity(
            ComBo combo,
            List<String> hinhAnhs
    ) {
        List<String> danhSachAnh = hinhAnhs == null
                ? List.of()
                : hinhAnhs.stream()
                .filter(url -> url != null && !url.isBlank())
                .distinct()
                .toList();

        /*
         * Combo cũ chưa có dữ liệu trong combo_hinhanh
         * thì lấy lại trường HinhAnh của bảng combo.
         */
        if (
                danhSachAnh.isEmpty()
                        && combo.getHinhAnh() != null
                        && !combo.getHinhAnh().isBlank()
        ) {
            danhSachAnh = List.of(combo.getHinhAnh());
        }

        String anhChinh = combo.getHinhAnh();

        /*
         * Nếu cột HinhAnh bị trống nhưng bảng combo_hinhanh có dữ liệu,
         * lấy ảnh đầu tiên làm ảnh chính.
         */
        if (
                (anhChinh == null || anhChinh.isBlank())
                        && !danhSachAnh.isEmpty()
        ) {
            anhChinh = danhSachAnh.get(0);
        }

        return new GoiDichVuResponse(
                combo.getComboId(),
                combo.getTenCombo(),
                combo.getGia(),
                combo.getMoTa(),
                anhChinh,
                combo.getGhiChu(),
                danhSachAnh,
                combo.getTrangThai()
        );
    }
}