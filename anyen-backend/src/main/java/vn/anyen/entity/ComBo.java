package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "combo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComboId")
    private Integer comboId;

    @Column(
            name = "TenCombo",
            nullable = false,
            length = 255
    )
    private String tenCombo;

    @Column(
            name = "Gia",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal gia;

    @Lob
    @Column(
            name = "MoTa",
            columnDefinition = "TEXT"
    )
    private String moTa;

    /**
     * Ảnh đại diện duy nhất của combo.
     *
     * Ảnh này được hiển thị tại trang danh sách dịch vụ.
     * Khi upload nhiều ảnh, ảnh đầu tiên sẽ được chọn làm ảnh đại diện.
     */
    @Column(
            name = "HinhAnh",
            length = 500
    )
    private String hinhAnh;

    /**
     * Danh sách quyền lợi hoặc ghi chú ngắn của combo.
     *
     * Có thể lưu dưới dạng HTML:
     *
     * <ul>
     *     <li>Tư vấn 24/7</li>
     *     <li>Xe đưa đón</li>
     * </ul>
     */
    @Lob
    @Column(
            name = "GhiChu",
            columnDefinition = "LONGTEXT"
    )
    private String ghiChu;

    /**
     * Đối tác tạo và sở hữu combo.
     */
    @Column(
            name = "MaDoiTac",
            nullable = false
    )
    private Integer maDoiTac;

    /**
     * 0 = Ẩn
     * 1 = Hoạt động
     * 2 = Ngừng kinh doanh
     */
    @Column(
            name = "TrangThai",
            nullable = false
    )
    private Integer trangThai;



    public static final Integer TT_AN = 0;
    public static final Integer TT_HOAT_DONG = 1;
    public static final Integer TT_NGUNG_KINH_DOANH = 2;

    @PrePersist
    public void prePersist() {
        if (trangThai == null) {
            trangThai = TT_HOAT_DONG;
        }
    }
}