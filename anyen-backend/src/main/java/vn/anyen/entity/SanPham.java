package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sanpham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @Column(name = "tenSanPham")
    private String tenSanPham;

    @Column(name = "loai")
    private String loai;

    @Column(name = "noiThat")
    private String noiThat;

    @Column(name = "quyCach")
    private String quyCach;

    @Column(name = "tonGiao")
    private String tonGiao;

    @Column(name = "giaTien")
    private BigDecimal giaTien;

    @Column(name = "MaDoiTac")
    private Integer maDoiTac;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "thietKe")
    private String thietKe;

    @Column(name = "xuatXu")
    private String xuatXu;

    @Column(name = "GhiChu")
    private String ghiChu;

    @Column(name = "khuyenMai")
    private BigDecimal khuyenMai;

    @Column(name = "mauSac")
    private String mauSac;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    @Column(name = "vatLieu")
    private String vatLieu;

    public static final Integer TRANG_THAI_AN = 0;
    public static final Integer TRANG_THAI_DANG_BAN = 1;
    public static final Integer TRANG_THAI_CHO_XAC_NHAN = 2;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "kichThuoc")
    private String kichThuoc;

    @Column(name = "trongLuong")
    private String trongLuong;

    @Column(name = "CNSX")
    private String cnsx;
}