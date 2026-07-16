package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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

    @NotNull(message = "Vui lòng chọn loại tin")
    private Integer loaiTin;

    private Integer trangThai;
}