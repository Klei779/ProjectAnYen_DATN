package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
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

    @PositiveOrZero(message = "Giá combo không được âm")
    private BigDecimal gia;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String moTa;

    @Size(max = 500, message = "Đường dẫn hình ảnh tối đa 500 ký tự")
    private String hinhAnh;

    private Integer trangThai;

    @NotEmpty(message = "Combo phải có ít nhất một sản phẩm")
    private List<Integer> maSanPhams;
}
