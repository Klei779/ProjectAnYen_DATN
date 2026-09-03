package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "combo_hinhanh")
public class ComBoHinhAnh {

    public static final String LOAI_DAI_DIEN = "DAI_DIEN";
    public static final String LOAI_QUY_TRINH = "QUY_TRINH";
    public static final String LOAI_CHI_TIET = "CHI_TIET";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnh")
    private Integer maHinhAnh;

    @Column(name = "ComboId", nullable = false)
    private Integer comboId;

    @Column(name = "LoaiHinhAnh", nullable = false, length = 20)
    private String loaiHinhAnh;

    @Column(name = "HinhAnh", nullable = false, length = 500)
    private String hinhAnh;

    @Column(name = "TenHinhAnh", length = 255)
    private String tenHinhAnh;

    @Column(name = "ThuTu", nullable = false)
    private Integer thuTu;

    public ComBoHinhAnh() {
    }

    public ComBoHinhAnh(
            Integer maHinhAnh,
            Integer comboId,
            String loaiHinhAnh,
            String hinhAnh,
            String tenHinhAnh,
            Integer thuTu
    ) {
        this.maHinhAnh = maHinhAnh;
        this.comboId = comboId;
        this.loaiHinhAnh = loaiHinhAnh;
        this.hinhAnh = hinhAnh;
        this.tenHinhAnh = tenHinhAnh;
        this.thuTu = thuTu;
    }

    @PrePersist
    public void prePersist() {
        if (loaiHinhAnh == null || loaiHinhAnh.isBlank()) {
            loaiHinhAnh = LOAI_DAI_DIEN;
        }
        if (thuTu == null || thuTu <= 0) {
            thuTu = 1;
        }
    }

    public Integer getMaHinhAnh() {
        return maHinhAnh;
    }

    public void setMaHinhAnh(Integer maHinhAnh) {
        this.maHinhAnh = maHinhAnh;
    }

    public Integer getComboId() {
        return comboId;
    }

    public void setComboId(Integer comboId) {
        this.comboId = comboId;
    }

    public String getLoaiHinhAnh() {
        return loaiHinhAnh;
    }

    public void setLoaiHinhAnh(String loaiHinhAnh) {
        this.loaiHinhAnh = loaiHinhAnh;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTenHinhAnh() {
        return tenHinhAnh;
    }

    public void setTenHinhAnh(String tenHinhAnh) {
        this.tenHinhAnh = tenHinhAnh;
    }

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }
}