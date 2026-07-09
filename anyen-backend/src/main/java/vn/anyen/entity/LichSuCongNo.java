package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lichsucongno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuCongNo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLichSuCongNo")
    private Integer maLichSuCongNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaCongNo", nullable = false)
    private CongNo congNo;

    @Column(name = "SoTienThanhToan", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienThanhToan;

    @Column(name = "PhuongThucThanhToan", nullable = false)
    private Integer phuongThucThanhToan;

    public static final Integer PT_TIEN_MAT = 1;
    public static final Integer PT_CHUYEN_KHOAN = 2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @CreationTimestamp
    @Column(name = "NgayThanhToan", nullable = false, updatable = false)
    private LocalDateTime ngayThanhToan;

    @Column(name = "MaGiaoDich", length = 100)
    private String maGiaoDich;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;
}
