package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "chitietdonhang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonHangChiTiet")
    private Integer maDonHangChiTiet;

    @ManyToOne
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    @ManyToOne
    @JoinColumn(name = "MaSanPham")
    private SanPham sanPham;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "giaTien")
    private BigDecimal giaTien;

    @Column(name = "TrangThaiDoiTac", columnDefinition = "int default 0")
    private Integer trangThaiDoiTac = 0;

    @Column(name = "NgayGiaoDuKien")
    private LocalDate ngayGiaoDuKien;
}