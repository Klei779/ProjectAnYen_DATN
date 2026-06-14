package vn.anyen.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDoiTacRequest {
// test

    private String tenSanPham;
    private String loai;
    private String noiThat;
    private String quyCach;
    private String tonGiao;
    private BigDecimal giaTien;
    private Integer soLuong;
    private String thietKe;
    private String xuatXu;
    private String ghiChu;
    private BigDecimal khuyenMai;
    private String mauSac;
    private String hinhAnh;
    private String vatLieu;
    private String trangThai;
    private String kichThuoc;
    private String trongLuong;
    private String cnsx;
}