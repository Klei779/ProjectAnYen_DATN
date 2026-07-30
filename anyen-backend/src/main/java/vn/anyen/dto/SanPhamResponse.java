package vn.anyen.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamResponse {
    private Integer id;
    private String name;
    private String subname;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String image;
    private String tenDoiTac;

    private String tenTrangThai;

    private String loai;
    private String vatLieu;
    private String tonGiao;
    private String mauSac;
    private Integer trangThai;

    // Additional fields for product detail page
    private String code;
    private String quyCach;
    private String kichThuoc;
    private String trongLuong;
    private String xuatXu;
    private String nhaCungCap;
    private String nhaSanXuat;
    private Integer soLuong;
    private String ngayCapNhat;
    private BigDecimal discount;
    private String moTa;
    private String huongDanBaoQuan;

    // Chi tiết sản phẩm dạng bài viết
    private List<SanPhamChiTietResponse> sanPhamChiTiets;
    private List<SanPhamHinhAnhResponse> sanPhamHinhAnhs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SanPhamChiTietResponse {
        private Integer maChiTiet;
        private String loaiKhoi;
        private String noiDung;
        private Integer thuTu;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SanPhamHinhAnhResponse {
        private Integer maHinhAnh;
        private String loaiHinhAnh;
        private String urlHinhAnh;
        private Integer thuTu;
    }
}