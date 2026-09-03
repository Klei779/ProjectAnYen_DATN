package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "combochitiet")
public class ComBoChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComboChiTietId")
    private Integer comboChiTietId;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @Column(name = "ComboId")
    private Integer comboId;

    @Column(name = "Loai")
    private Integer loai;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong = 1;

    @Column(name = "NoiDung", length = 255)
    private String noiDung;

    public static final Integer LOAI_TIEN_ICH_DICH_VU = 0;
    public static final Integer LOAI_SAN_PHAM = 1;

    public ComBoChiTiet() {
    }

    public ComBoChiTiet(
            Integer comboChiTietId,
            Integer maSanPham,
            Integer comboId,
            Integer loai,
            Integer soLuong,
            String noiDung
    ) {
        this.comboChiTietId = comboChiTietId;
        this.maSanPham = maSanPham;
        this.comboId = comboId;
        this.loai = loai;
        this.soLuong = soLuong;
        this.noiDung = noiDung;
    }

    public Integer getComboChiTietId() {
        return comboChiTietId;
    }

    public void setComboChiTietId(Integer comboChiTietId) {
        this.comboChiTietId = comboChiTietId;
    }

    public Integer getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(Integer maSanPham) {
        this.maSanPham = maSanPham;
    }

    public Integer getComboId() {
        return comboId;
    }

    public void setComboId(Integer comboId) {
        this.comboId = comboId;
    }

    public Integer getLoai() {
        return loai;
    }

    public void setLoai(Integer loai) {
        this.loai = loai;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }
}