package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "doitac")
@Getter
@Setter
public class DoiTac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDoiTac")
    private Integer maDoiTac;

    @Column(name = "TenDoiTac")
    private String tenDoiTac;

    @Column(name = "TenDangNhap")
    private String tenDangNhap;

    @Column(name = "MatKhau")
    private String matKhau;

    @Column(name = "Email")
    private String email;

    @Column(name = "TenDoanhNghiep")
    private String tenDoanhNghiep;

    @Column(name = "MaSoThue")
    private String maSoThue;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "DiaChi")
    private String diaChi;

    public static final Integer TRANG_THAI_NGUNG_HOAT_DONG = 0;
    public static final Integer TRANG_THAI_HOAT_DONG = 1;
    public static final Integer TRANG_THAI_CHO_XAC_NHAN = 2; // Đang ký

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private java.time.LocalDateTime updatedAt;
}