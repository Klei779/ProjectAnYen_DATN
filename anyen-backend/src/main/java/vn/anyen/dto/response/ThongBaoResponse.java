package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThongBaoResponse {

    private Integer maThongBao;
    private String tieuDe;
    private String noiDung;
    private String loaiThongBao;
    private Integer trangThai;
    private String tenTrangThai;
    private String lyDoTuChoi;
    private String ngayTao;

    // Thông tin người gửi
    private Integer nguoiGuiId;
    private String tenNguoiGui;
    private Integer maSanPham;
    private Boolean daDoc;
    // Thông tin người nhận
    private Integer nguoiNhanId;
    private String tenNguoiNhan;
    // Thông tin khách hàng (nếu có)
    private Integer maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String cccd;
    private String ngayDangKy;
    private String nguonDangKy;
    private String nhuCauHoTro;
    private String ghiChu;

}
