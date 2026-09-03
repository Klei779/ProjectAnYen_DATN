package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "dambaodonhangdoitac",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dambao_donhang_doitac",
                columnNames = {
                        "MaDonHang",
                        "MaDoiTac"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DamBaoDonHangDoiTac {

    // 0 = tiền đang bị khóa
    public static final Integer TT_DANG_KHOA = 0;

    // 1 = đã lấy 20%, chuyển 80% vào ví
    public static final Integer TT_DA_QUYET_TOAN = 1;

    // 2 = dành cho xử lý hoàn quỹ sau này
    public static final Integer TT_DA_HOAN_QUY = 2;


    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "MaDamBao")
    private Integer maDamBao;


    @Column(
            name = "MaDonHang",
            nullable = false
    )
    private Integer maDonHang;


    @Column(
            name = "MaDoiTac",
            nullable = false
    )
    private Integer maDoiTac;


    /*
     * 100% giá trị phần đơn của đối tác.
     */
    @Column(
            name = "SoTienKhoa",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal soTienKhoa;


    /*
     * 20% phí An Yên sau quyết toán.
     */
    @Builder.Default
    @Column(
            name = "PhiSan",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal phiSan =
            BigDecimal.ZERO;


    /*
     * 80% chuyển vào Ví.
     */
    @Builder.Default
    @Column(
            name = "TienVaoVi",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal tienVaoVi =
            BigDecimal.ZERO;


    @Builder.Default
    @Column(
            name = "TrangThai",
            nullable = false
    )
    private Integer trangThai =
            TT_DANG_KHOA;


    @Column(
            name = "CreatedAt",
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;


    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}