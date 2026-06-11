package vn.anyen.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamRequest {
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
    private String CNXS;
}
