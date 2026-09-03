package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payoomocktransaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayooMockTransaction {

    // =========================
    // LOẠI GIAO DỊCH
    // =========================

    public static final String LOAI_NAP_QUY =
            "NAP_QUY";

    public static final String LOAI_RUT_QUY =
            "RUT_QUY";

    public static final String LOAI_RUT_VI =
            "RUT_VI";

    public static final String LOAI_THANH_TOAN_CONG_NO =
            "THANH_TOAN_CONG_NO";

    public static final String LOAI_THANH_TOAN_DON_HANG =
            "THANH_TOAN_DON_HANG";


    // =========================
    // TRẠNG THÁI
    // =========================

    public static final Integer TT_CHO_XU_LY = 0;

    public static final Integer TT_DANG_XU_LY = 1;

    public static final Integer TT_THANH_CONG = 2;

    public static final Integer TT_THAT_BAI = 3;


    @Id
    @Column(
            name = "MaGiaoDich",
            length = 100
    )
    private String maGiaoDich;


    @Column(
            name = "LoaiGiaoDich",
            nullable = false,
            length = 50
    )
    private String loaiGiaoDich;


    @Column(name = "MaDoiTac")
    private Integer maDoiTac;


    @Column(name = "MaCongNo")
    private Integer maCongNo;


    @Column(name = "MaDonHang")
    private Integer maDonHang;


    @Column(
            name = "SoTien",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal soTien;


    @Builder.Default
    @Column(
            name = "TrangThai",
            nullable = false
    )
    private Integer trangThai =
            TT_CHO_XU_LY;


    @Column(
            name = "NoiDung",
            length = 500
    )
    private String noiDung;


    @Column(
            name = "CreatedAt",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;


    @PrePersist
    private void onCreate() {

        if (createdAt == null) {
            createdAt =
                    LocalDateTime.now();
        }

        if (trangThai == null) {
            trangThai =
                    TT_CHO_XU_LY;
        }
    }
}