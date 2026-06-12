package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "email")
    private String email;

    @Column(name = "soDienThoai")
    private String soDienThoai;
}