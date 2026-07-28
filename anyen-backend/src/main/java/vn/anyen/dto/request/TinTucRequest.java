package vn.anyen.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TinTucRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 150, message = "Tiêu đề tối đa 150 ký tự")
    private String tieuDe;

    @NotBlank(message = "Tóm tắt không được để trống")
    @Size(max = 500, message = "Tóm tắt tối đa 500 ký tự")
    private String tomTat;

    @NotBlank(message = "Nội dung không được để trống")
    private String noiDung;

    @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự")
    private String anhDaiDien;

    /**
     * 1: Kiến thức
     * 2: Phong tục
     * 3: Thông báo
     * 4: Hoạt động
     */
    @NotNull(message = "Vui lòng chọn loại tin")
    @Min(value = 1, message = "Loại tin không hợp lệ")
    @Max(value = 4, message = "Loại tin không hợp lệ")
    private Integer loaiTin;

    /**
     * 0: Ẩn
     * 1: Hiển thị
     */
    @Min(value = 0, message = "Trạng thái không hợp lệ")
    @Max(value = 1, message = "Trạng thái không hợp lệ")
    private Integer trangThai;
}