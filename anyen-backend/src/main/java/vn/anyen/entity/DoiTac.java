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

    public static final Integer TRANG_THAI_NGUNG_HOAT_DONG = 0;
    public static final Integer TRANG_THAI_HOAT_DONG = 1;

    @Column(name = "TrangThai")
    private Integer trangThai;
}