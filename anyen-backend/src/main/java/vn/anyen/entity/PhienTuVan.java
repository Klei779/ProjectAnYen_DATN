package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "phientuvan",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_phientuvan_token",
                columnNames = "TokenPhien"
        )
)
public class PhienTuVan {

    public static final Integer TRANG_THAI_CHO_TIEP_NHAN = 0;
    public static final Integer TRANG_THAI_DANG_TU_VAN = 1;
    public static final Integer TRANG_THAI_DA_DONG = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhien")
    private Long maPhien;

    @Column(
            name = "TokenPhien",
            nullable = false,
            length = 64
    )
    private String tokenPhien;

    @Column(
            name = "TenKhachHang",
            nullable = false,
            length = 100
    )
    private String tenKhachHang;

    @Column(name = "MaNhanVien")
    private Integer maNhanVien;

    @Column(
            name = "TrangThai",
            nullable = false
    )
    private Integer trangThai;

    @Column(
            name = "TinNhanCuoi",
            length = 500
    )
    private String tinNhanCuoi;

    @Column(
            name = "ThoiGianTinNhanCuoi",
            nullable = false
    )
    private LocalDateTime thoiGianTinNhanCuoi;

    @Column(
            name = "SoTinNhanChuaDocNhanVien",
            nullable = false
    )
    private Integer soTinNhanChuaDocNhanVien;

    @Column(
            name = "SoTinNhanChuaDocKhach",
            nullable = false
    )
    private Integer soTinNhanChuaDocKhach;

    @Column(
            name = "CreatedAt",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "UpdatedAt",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "HetHanLuc",
            nullable = false
    )
    private LocalDateTime hetHanLuc;

    public PhienTuVan() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (thoiGianTinNhanCuoi == null) {
            thoiGianTinNhanCuoi = now;
        }

        if (hetHanLuc == null) {
            hetHanLuc = now.plusDays(30);
        }

        if (trangThai == null) {
            trangThai = TRANG_THAI_CHO_TIEP_NHAN;
        }

        if (soTinNhanChuaDocNhanVien == null) {
            soTinNhanChuaDocNhanVien = 0;
        }

        if (soTinNhanChuaDocKhach == null) {
            soTinNhanChuaDocKhach = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(Long maPhien) {
        this.maPhien = maPhien;
    }

    public String getTokenPhien() {
        return tokenPhien;
    }

    public void setTokenPhien(String tokenPhien) {
        this.tokenPhien = tokenPhien;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public Integer getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(Integer maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public String getTinNhanCuoi() {
        return tinNhanCuoi;
    }

    public void setTinNhanCuoi(String tinNhanCuoi) {
        this.tinNhanCuoi = tinNhanCuoi;
    }

    public LocalDateTime getThoiGianTinNhanCuoi() {
        return thoiGianTinNhanCuoi;
    }

    public void setThoiGianTinNhanCuoi(
            LocalDateTime thoiGianTinNhanCuoi
    ) {
        this.thoiGianTinNhanCuoi =
                thoiGianTinNhanCuoi;
    }

    public Integer getSoTinNhanChuaDocNhanVien() {
        return soTinNhanChuaDocNhanVien;
    }

    public void setSoTinNhanChuaDocNhanVien(
            Integer soTinNhanChuaDocNhanVien
    ) {
        this.soTinNhanChuaDocNhanVien =
                soTinNhanChuaDocNhanVien;
    }

    public Integer getSoTinNhanChuaDocKhach() {
        return soTinNhanChuaDocKhach;
    }

    public void setSoTinNhanChuaDocKhach(
            Integer soTinNhanChuaDocKhach
    ) {
        this.soTinNhanChuaDocKhach =
                soTinNhanChuaDocKhach;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getHetHanLuc() {
        return hetHanLuc;
    }

    public void setHetHanLuc(
            LocalDateTime hetHanLuc
    ) {
        this.hetHanLuc = hetHanLuc;
    }
}