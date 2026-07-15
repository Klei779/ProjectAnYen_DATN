package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GiaoCongViecRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 255, message = "Tên khách hàng tối đa 255 ký tự")
    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{9,20}$", message = "Số điện thoại chỉ gồm 9 - 20 chữ số")
    private String soDienThoai;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String diaChi;

    @NotNull(message = "Vui lòng chọn nhân viên trực tiếp")
    private Integer maNhanVien;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 2000, message = "Nhu cầu hỗ trợ tối đa 2000 ký tự")
    private String nhuCauHoTro;

    @Size(max = 2000, message = "Ghi chú tối đa 2000 ký tự")
    private String ghiChu;
}
