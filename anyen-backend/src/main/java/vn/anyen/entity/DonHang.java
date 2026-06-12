package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "donhang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonHang")
    private Integer maDonHang;

    @ManyToOne
    @JoinColumn(name = "MaKhachHang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @Column(name = "NgayTaoDon")
    private LocalDate ngayTaoDon;

    @Column(name = "tongTien")
    private BigDecimal tongTien;

    @Column(name = "trangThai")
    private String trangThai;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;
}