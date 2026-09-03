package vn.anyen.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComBoChiTietResponse {

    private Integer comboChiTietId;

    private Integer loai;

    private Integer soLuong;

    private String noiDung;

    private List<ComBoChiTietHinhAnhResponse> hinhAnhs;

    /** Tên loại thực của sản phẩm lấy từ SanPham.loai (ví dụ: "Quan tài", "Vòng hoa"). */
    private String tenLoaiSanPham;

    /** Ảnh đại diện lấy từ SanPham.hinhAnh – dùng làm fallback khi hinhAnhs rỗng. */
    private String hinhAnhSanPham;
}
