package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoadon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHoaDon")
    private Integer maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    @Column(name = "NgayIn")
    private LocalDate ngayIn;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    public static final Integer TT_DA_HUY = 0;
    public static final Integer TT_DA_TAO = 1;

    public static final Integer PT_CHUA_CHON = 0;
    public static final Integer PT_TIEN_MAT = 1;
    public static final Integer PT_CHUYEN_KHOAN = 2;
    public static final Integer PT_PAYOO = 3;

    @Column(name = "PhuongThucThanhToan")
    private Integer phuongThucThanhToan;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}