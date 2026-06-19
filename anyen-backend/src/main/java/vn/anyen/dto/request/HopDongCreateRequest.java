package vn.anyen.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDongCreateRequest {

    @NotNull(message = "Vui lòng chọn đơn hàng")
    private Integer maDonHang;

    private String ngayKyHD;
    private String ngayViet;

    private String thoiHanKetThuc;
    private String ngayKetThuc;

    private String trangThai;

    // Dữ liệu lưu vào bảng hdongct
    private String hoTenNguoiMat;
    private String ngayMat;
    private String ngaySinh;
    private String gioiTinh;

    private String soGiayBaoTu;
    private String noiCapGiayBaoTu;

    private String coSoMaiTang;
    private String khuMo;
    private String soMo;

    private String ngayGioAnTang;
}