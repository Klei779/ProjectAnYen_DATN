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

    @Column(name = "TrangThai")
    private String trangThai;
}