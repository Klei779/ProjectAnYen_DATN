package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "congno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongNo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCongNo")
    private Integer maCongNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonHang", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDoiTac", nullable = false)
    private DoiTac doiTac;

    @Column(name = "TongTien", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "DaThanhToan", nullable = false, precision = 18, scale = 2)
    private BigDecimal daThanhToan;

    @Column(name = "ConLai", nullable = false, precision = 18, scale = 2)
    private BigDecimal conLai;

    @Column(name = "HanThanhToan")
    private LocalDate hanThanhToan;

    @Column(name = "TrangThai", nullable = false)
    private Integer trangThai;

    public static final Integer TT_CHUA_THANH_TOAN = 0;
    public static final Integer TT_THANH_TOAN_MOT_PHAN = 1;
    public static final Integer TT_DA_THANH_TOAN = 2;
    public static final Integer TT_QUA_HAN = 3;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
