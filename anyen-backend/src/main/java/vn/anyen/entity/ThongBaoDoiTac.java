package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "thongbaodoitac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBaoDoiTac {

    /** Loai: 0 = Đơn hàng, 1 = Duyệt sản phẩm */
    public static final Integer LOAI_DON_HANG = 0;
    public static final Integer LOAI_DUYET_SAN_PHAM = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThongBao")
    private Integer maThongBao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDoiTac")
    private DoiTac doiTac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    /**
     * Loại thông báo: 0 = Đơn hàng, 1 = Duyệt sản phẩm.
     * Khi loai = 1 (DUYET_SAN_PHAM), mã sản phẩm được nhúng trong NoiDung
     * với format [MASP:id]
     */
    @Column(name = "Loai")
    private Integer loai;

    @Column(name = "TieuDe")
    private String tieuDe;

    @Column(name = "NoiDung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "TrangThaiThongBao")
    private Integer trangThaiThongBao;

    @Column(name = "LyDoTuChoi", columnDefinition = "TEXT")
    private String lyDoTuChoi;

    @Column(name = "DaDoc")
    private Boolean daDoc;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianXuLy")
    private LocalDateTime thoiGianXuLy;

    @PrePersist
    public void prePersist() {
        if (loai == null) loai = LOAI_DON_HANG;
        if (trangThaiThongBao == null) trangThaiThongBao = 0;
        if (daDoc == null) daDoc = false;
        if (thoiGianTao == null) thoiGianTao = LocalDateTime.now();
    }
}