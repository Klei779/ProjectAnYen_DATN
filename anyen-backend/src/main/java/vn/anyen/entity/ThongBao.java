package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "thongbao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThongBao")
    private Integer maThongBao;

    @Column(name = "TieuDe")
    private String tieuDe;

    @Column(name = "NoiDung", columnDefinition = "TEXT")
    private String noiDung;

    /**
     * Loại thông báo:
     * CONG_VIEC  - Thông báo công việc (có nút Chấp nhận / Từ chối)
     * HE_THONG   - Thông báo hệ thống (chỉ đọc)
     * TU_CHOI    - Phản hồi từ chối gửi về hotline
     */
    @Column(name = "LoaiThongBao")
    private String loaiThongBao;

    /** ID nhân viên gửi (hotline). Null nếu là thông báo hệ thống */
    @Column(name = "NguoiGuiId")
    private Integer nguoiGuiId;

    /** ID nhân viên nhận. Null = broadcast cho tất cả */
    @Column(name = "NguoiNhanId")
    private Integer nguoiNhanId;

    /** Liên kết khách hàng (cho loại CONG_VIEC) */
    @Column(name = "MaKhachHang")
    private Integer maKhachHang;

    public static final Integer TT_CHUA_DOC = 0;
    public static final Integer TT_DA_DOC = 1;
    public static final Integer TT_DA_CHAP_NHAN = 2;
    public static final Integer TT_DA_TU_CHOI = 3;
    public static final Integer TT_CHO_XAC_NHAN = 4;

    /**
     * Trạng thái:
     * 0 = Chưa đọc, 1 = Đã đọc, 2 = Đã chấp nhận, 3 = Đã từ chối, 4 = Chờ xác nhận
     */
    @Column(name = "TrangThai")
    private Integer trangThai;

    /** Lý do từ chối (khi từ chối) */
    @Column(name = "LyDoTuChoi", columnDefinition = "TEXT")
    private String lyDoTuChoi;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = TT_CHUA_DOC;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
