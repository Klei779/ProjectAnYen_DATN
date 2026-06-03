package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DoiTac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDoiTac;

    private String tenDoiTac;

    private String tenDoanhNghiep;

    private String maSoThue;

    private String soTaiKhoan;

    private String nganHang;

    private String tenDangNhap;

    private String matKhau;

    private String email;

    private String soDienThoai;

    private String diaChi;

    private String trangThai;
}