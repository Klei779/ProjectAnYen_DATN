package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "yeucautuvanai")
@Getter
@Setter
public class YeuCauTuVanAi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaYeuCau")
    private Long maYeuCau;

    @Column(
            name = "MaPhien",
            nullable = false,
            unique = true
    )
    private Long maPhien;

    @Column(name = "HoTen", length = 150)
    private String hoTen;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "DiaChi", length = 500)
    private String diaChi;

    @Column(name = "NhuCau", columnDefinition = "TEXT")
    private String nhuCau;

    @Column(name = "ThoiGianMongMuon", length = 255)
    private String thoiGianMongMuon;

    @Column(
            name = "NganSachDuKien",
            precision = 18,
            scale = 2
    )
    private BigDecimal nganSachDuKien;

    @Column(
            name = "TongTienThamKhao",
            precision = 18,
            scale = 2
    )
    private BigDecimal tongTienThamKhao;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "TrangThai", nullable = false)
    private Integer trangThai;

    @Column(name = "DaXacNhan", nullable = false)
    private Boolean daXacNhan;

    @Column(name = "DaGuiHotline", nullable = false)
    private Boolean daGuiHotline;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (trangThai == null) {
            trangThai = 0;
        }

        if (daXacNhan == null) {
            daXacNhan = false;
        }

        if (daGuiHotline == null) {
            daGuiHotline = false;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}