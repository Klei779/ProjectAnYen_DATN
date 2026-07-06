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

    // TrangThai constants (theo database mới)
    // 0 = Ngừng hoạt động
    // 1 = Đang hoạt động
    // 2 = Chờ xác nhận (lời mời)
    // 3 = Đã xóa
    public static final Integer TT_NGUNG_HOAT_DONG = 0;
    public static final Integer TT_DANG_HOAT_DONG = 1;
    public static final Integer TT_CHO_XAC_NHAN = 2;
    public static final Integer TT_DA_XOA = 3;

    @Column(name = "TrangThai")
    private Integer trangThai;

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