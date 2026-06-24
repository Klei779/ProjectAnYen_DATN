package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "doitac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDoiTac")
    private Integer maDoiTac;

    @Column(name = "TenDoiTac")
    private String tenDoiTac;

    @Column(name = "TenDoanhNghiep")
    private String tenDoanhNghiep;

    @Column(name = "MaSoThue")
    private String maSoThue;

    @Column(name = "SoTaiKhoan")
    private String soTaiKhoan;

    @Column(name = "NganHang")
    private String nganHang;

    @Column(name = "TenDangNhap")
    private String tenDangNhap;

    @Column(name = "MatKhau")
    private String matKhau;

    @Column(name = "Email")
    private String email;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "ConfirmationToken")
    private String confirmationToken;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}