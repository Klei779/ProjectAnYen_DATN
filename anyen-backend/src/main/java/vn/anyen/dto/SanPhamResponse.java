package vn.anyen.dto;

import lombok.*;

import java.math.BigDecimal;

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
}