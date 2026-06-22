package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sanphamhinhanh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamHinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnh")
    private Integer maHinhAnh;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @Column(name = "LoaiHinhAnh")
    private String loaiHinhAnh;

    @Column(name = "UrlHinhAnh")
    private String urlHinhAnh;

    @Column(name = "ThuTu")
    private Integer thuTu;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
