package vn.anyen.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ComboDoiTacRequest {

    @NotBlank(message = "Tên combo không được để trống")
    @Size(max = 255, message = "Tên combo tối đa 255 ký tự")
    private String tenCombo;

    @NotNull(message = "Giá combo không được để trống")
    @Positive(message = "Giá combo phải lớn hơn 0")
    private BigDecimal gia;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String moTa;
    @Size(max = 5000, message = "Ghi chú tối đa 5000 ký tự")
    private String ghiChu;
    @Size(max = 500, message = "Đường dẫn hình ảnh tối đa 500 ký tự")
    private String hinhAnh;

    private Integer trangThai;

    /**
     * Dữ liệu mới: mỗi sản phẩm đi kèm số lượng sử dụng trong combo.
     */
    @Valid
    private List<ComboSanPhamRequest> sanPhams;

    /**
     * Giữ tương thích với frontend cũ. Nếu sanPhams rỗng, mỗi mã ở đây được hiểu là số lượng 1.
     */
    private List<Integer> maSanPhams;


    @Getter
    @Setter
    public static class ComboSanPhamRequest {
        @NotNull(message = "Mã sản phẩm không được để trống")
        private Integer maSanPham;

        @NotNull(message = "Số lượng sản phẩm không được để trống")
        @Positive(message = "Số lượng sản phẩm trong combo phải lớn hơn 0")
        private Integer soLuong;
    }
}
