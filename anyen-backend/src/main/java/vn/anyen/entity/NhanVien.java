package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "VaiTro")
    private Integer vaiTro;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "Email")
    private String email;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "SoDienThoai")
    private String soDienThoai;
}