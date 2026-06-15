package vn.anyen.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaoDonHangRequest {

    private Integer maKhachHang;

    private String tenKhachHang;
    private String soDienThoai;
    private String cccd;
    private String email;
    private String diaChi;

    private LocalDate ngayTaoDon;
    private String ghiChu;
    private String phuongThucThanhToan;
    private String trangThaiThanhToan;

    @Valid
    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<SanPhamTrongDonRequest> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SanPhamTrongDonRequest {

        @NotNull(message = "Mã sản phẩm không được để trống")
        private Integer maSanPham;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;
    }
}