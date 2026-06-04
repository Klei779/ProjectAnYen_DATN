package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maSanPham;

    private String tenSanPham;

    private String loai;

    private String noiThat;

    private String quyCach;

    private String tonGiao;

    private BigDecimal giaTien;

    @ManyToOne
    @JoinColumn(name = "MaDoiTac")
    private DoiTac doiTac;

    private Integer soLuong;

    private String thietKe;

    private String xuatXu;

    @Column(columnDefinition = "TEXT")
    private String ghiChu;

    private BigDecimal khuyenMai;

    private String mauSac;

    private String hinhAnh;

    private String vatLieu;

    private String trangThai;

    private String kichThuoc;

    private String trongLuong;

    private String CNSX;
}