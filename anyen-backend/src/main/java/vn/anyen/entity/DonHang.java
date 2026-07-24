package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "donhang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonHang")
    private Integer maDonHang;

    @ManyToOne
    @JoinColumn(name = "MaKhachHang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @Column(name = "NgayTaoDon")
    private LocalDate ngayTaoDon;

    @Column(name = "NgayGiaoDuKien")
    private LocalDate ngayGiaoDuKien;

    @Column(name = "tongTien")
    private BigDecimal tongTien;

    public static final Integer TT_MOI_TAO = 1;
    public static final Integer TT_CHO_DOI_TAC_XAC_NHAN = 2;
    public static final Integer TT_DA_XAC_NHAN = 3;
    public static final Integer TT_DANG_XU_LY = 4;
    public static final Integer TT_CHO_THANH_TOAN = 5;
    public static final Integer TT_HOAN_THANH = 6;
    public static final Integer TT_DA_HUY = 7;
    public static final Integer TT_DOI_TAC_TU_CHOI = 8;
    public static final Integer TT_DA_GIAO = 9;
    public static final Integer TT_DA_THANH_TOAN = 10;
    public static final Integer TT_GAP_SU_CO = 11;

    public static final Integer PT_CHUA_CHON = 0;
    public static final Integer PT_TIEN_MAT = 1;
    public static final Integer PT_CHUYEN_KHOAN = 2;

    public static final Integer TTTT_CHUA_THANH_TOAN = 0;
    public static final Integer TTTT_DA_THANH_TOAN = 1;
    public static final Integer TTTT_CHO_XAC_NHAN = 2;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "PhuongThucThanhToan")
    private Integer phuongThucThanhToan;

    @Column(name = "TrangThaiThanhToan")
    private Integer trangThaiThanhToan;

    @Column(name = "LyDoHuy")
    private String lyDoHuy;

    @Column(name = "TrangThaiTruocSuCo")
    private Integer trangThaiTruocSuCo;

    @Column(name = "LyDoSuCo", columnDefinition = "TEXT")
    private String lyDoSuCo;

    @Column(name = "NguoiBaoCaoSuCo")
    private String nguoiBaoCaoSuCo;

    @PrePersist
    private void prePersist() {
        if (ngayTaoDon == null) {
            ngayTaoDon = LocalDate.now();
        }

        // Trạng thái đơn hàng hợp lệ bắt đầu từ 1.
        // Dữ liệu 0 là dữ liệu cũ/không hợp lệ và không được tạo mới nữa.
        if (trangThai == null || trangThai == 0) {
            trangThai = TT_MOI_TAO;
        }

        if (phuongThucThanhToan == null) {
            phuongThucThanhToan = PT_CHUA_CHON;
        }

        if (trangThaiThanhToan == null) {
            trangThaiThanhToan = TTTT_CHUA_THANH_TOAN;
        }
    }
}
