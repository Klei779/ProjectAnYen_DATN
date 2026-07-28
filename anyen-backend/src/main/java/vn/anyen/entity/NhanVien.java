package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "nhanvien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhanVien")
    private Integer maNhanVien;

    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "TenDangNhap")
    private String tenDangNhap;

    @Column(name = "MatKhau")
    private String matKhau;

    public static final Integer TRANG_THAI_NGHI_VIEC = 0;
    public static final Integer TRANG_THAI_HOAT_DONG = 1;

    public static final Integer VAI_TRO_ADMIN = 1;
    public static final Integer VAI_TRO_BAN_HANG = 2;
    public static final Integer VAI_TRO_TU_VAN = 3;
    public static final Integer VAI_TRO_HOTLINE = 4;
    public static final Integer VAI_TRO_KE_TOAN = 5;

    @Column(name = "VaiTro")
    private Integer vaiTro;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "Email")
    private String email;

    @Column(name = "TinhThanh")
    private String tinhThanh;
    @Column(name = "QuanHuyen")
    private String quanHuyen;
    @Column(name = "PhuongXa")
    private String phuongXa;
    @Column(name = "SoNhaDuong")
    private String soNhaDuong;
    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "Latitude")
    private BigDecimal latitude;

    @Column(name = "Longitude")
    private BigDecimal longitude;
}