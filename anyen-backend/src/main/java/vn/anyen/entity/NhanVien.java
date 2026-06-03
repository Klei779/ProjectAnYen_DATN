package vn.anyen.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NhanVien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNhanVien;

    private String hoTen;

    private String tenDangNhap;

    private String matKhau;

    private String email;

    private String soDienThoai;

    private String diaChi;

    private String vaiTro;

    private String trangThai;
}