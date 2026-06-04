package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "DonHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDonHang;

    @ManyToOne
    @JoinColumn(name = "MaKhachHang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    private LocalDate ngayTaoDon;

    private BigDecimal tongTien;

    private String trangThai;

    @Column(columnDefinition = "TEXT")
    private String ghiChu;
}