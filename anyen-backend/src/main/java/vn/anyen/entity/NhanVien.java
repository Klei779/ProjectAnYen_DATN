package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "nhanvien")
@Getter
@Setter
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

    @Column(name = "VaiTro")
    private String vaiTro;

    @Column(name = "TrangThai")
    private String trangThai;
}