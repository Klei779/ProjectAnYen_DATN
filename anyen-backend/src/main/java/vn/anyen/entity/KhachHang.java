package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "khachhang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKhachHang")
    private Integer maKhachHang;

    @Column(name = "tenKhachHang")
    private String tenKhachHang;

    @Column(name = "cccd")
    private String cccd;

    @Column(name="DiaChi")
    private String diaChi;

    @Column(name = "SoNhaDuong")
    private String soNhaDuong;

    @Column(name = "PhuongXa")
    private String phuongXa;

    @Column(name = "QuanHuyen")
    private String quanHuyen;

    @Column(name = "TinhThanh")
    private String tinhThanh;

    @Column(name = "email")
    private String email;

    @Column(name = "soDienThoai")
    private String soDienThoai;

    /** Nhân viên phụ trách khách hàng này (gán khi chấp nhận thông báo) */
    @Column(name = "MaNhanVienPhuTrach")
    private Integer maNhanVienPhuTrach;

    @Column(name = "NgayDangKy")
    private java.time.LocalDateTime ngayDangKy;

    @Column(name = "NguonDangKy")
    private String nguonDangKy;

    @Column(name = "NhuCauHoTro", columnDefinition = "TEXT")
    private String nhuCauHoTro;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "Latitude")
    private BigDecimal latitude;

    @Column(name = "Longitude")
    private BigDecimal longitude;
}