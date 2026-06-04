package vn.anyen.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDonHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDonHangChiTiet;

    @ManyToOne
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    @ManyToOne
    @JoinColumn(name = "MaSanPham")
    private SanPham sanPham;

    private Integer soLuong;

    private BigDecimal giaTien;
}