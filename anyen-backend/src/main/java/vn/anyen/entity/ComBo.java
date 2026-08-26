package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "combo")
public class ComBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComboId")
    private Integer comboId;

    @Column(name = "TenCombo", nullable = false, length = 255)
    private String tenCombo;

    @Column(name = "Gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal gia;

    @Lob
    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    /**
     * Trường tương thích dữ liệu cũ. Luôn đồng bộ với ảnh đại diện thứ nhất
     * trong bảng combo_hinhanh.
     */
    @Column(name = "HinhAnh", length = 500)
    private String hinhAnh;

    @Lob
    @Column(name = "GhiChu", columnDefinition = "LONGTEXT")
    private String ghiChu;

    /** Dữ liệu cũ; combo mới do Admin tạo sẽ để null. */
    @Column(name = "MaDoiTac")
    private Integer maDoiTac;

    /** Admin/nhân viên đã tạo combo. */
    @Column(name = "MaNhanVienTao")
    private Integer maNhanVienTao;

    /** 0 = Ẩn, 1 = Hoạt động, 2 = Ngừng kinh doanh. */
    @Column(name = "TrangThai", nullable = false)
    private Integer trangThai;

    public static final Integer TT_AN = 0;
    public static final Integer TT_HOAT_DONG = 1;
    public static final Integer TT_NGUNG_KINH_DOANH = 2;

    public ComBo() {
    }

    public ComBo(
            Integer comboId,
            String tenCombo,
            BigDecimal gia,
            String moTa,
            String hinhAnh,
            String ghiChu,
            Integer maDoiTac,
            Integer maNhanVienTao,
            Integer trangThai
    ) {
        this.comboId = comboId;
        this.tenCombo = tenCombo;
        this.gia = gia;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.ghiChu = ghiChu;
        this.maDoiTac = maDoiTac;
        this.maNhanVienTao = maNhanVienTao;
        this.trangThai = trangThai;
    }

    @PrePersist
    public void prePersist() {
        if (trangThai == null) {
            trangThai = TT_HOAT_DONG;
        }
    }

    public Integer getComboId() {
        return comboId;
    }

    public void setComboId(Integer comboId) {
        this.comboId = comboId;
    }

    public String getTenCombo() {
        return tenCombo;
    }

    public void setTenCombo(String tenCombo) {
        this.tenCombo = tenCombo;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Integer getMaDoiTac() {
        return maDoiTac;
    }

    public void setMaDoiTac(Integer maDoiTac) {
        this.maDoiTac = maDoiTac;
    }

    public Integer getMaNhanVienTao() {
        return maNhanVienTao;
    }

    public void setMaNhanVienTao(Integer maNhanVienTao) {
        this.maNhanVienTao = maNhanVienTao;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }
}