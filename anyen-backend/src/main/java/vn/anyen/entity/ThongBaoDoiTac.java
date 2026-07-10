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

    /** Loai: DON_HANG = Đơn hàng, DUYET_SAN_PHAM = Duyệt sản phẩm */
    public static final String LOAI_DON_HANG = "DON_HANG";
    public static final String LOAI_DUYET_SAN_PHAM = "DUYET_SAN_PHAM";

    /** TrangThaiThongBao: CHO_XAC_NHAN = Chờ xác nhận, DA_CHAP_NHAN = Đã chấp nhận, DA_TU_CHOI = Đã từ chối */
    public static final String TRANG_THAI_CHO_XAC_NHAN = "CHO_XAC_NHAN";
    public static final String TRANG_THAI_DA_CHAP_NHAN = "DA_CHAP_NHAN";
    public static final String TRANG_THAI_DA_TU_CHOI = "DA_TU_CHOI";

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
     * Loại thông báo: DON_HANG = Đơn hàng, DUYET_SAN_PHAM = Duyệt sản phẩm.
     * Khi loai = DUYET_SAN_PHAM, mã sản phẩm được nhúng trong NoiDung
     * với format [MASP:id]
     */
    @Column(name = "Loai")
    private String loai;

    @Column(name = "TieuDe")
    private String tieuDe;

    @Column(name = "NoiDung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "TrangThaiThongBao")
    private String trangThaiThongBao;

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
        if (trangThaiThongBao == null) trangThaiThongBao = TRANG_THAI_CHO_XAC_NHAN;
        if (daDoc == null) daDoc = false;
        if (thoiGianTao == null) thoiGianTao = LocalDateTime.now();
    }
}