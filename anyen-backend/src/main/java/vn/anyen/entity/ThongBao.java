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

    /**
     * Trạng thái:
     * CHUA_DOC, DA_DOC, DA_CHAP_NHAN, DA_TU_CHOI
     */
    @Column(name = "TrangThai")
    private String trangThai;

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
            trangThai = "CHUA_DOC";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
