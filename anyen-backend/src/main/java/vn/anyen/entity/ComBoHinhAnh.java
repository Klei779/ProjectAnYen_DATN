package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "combo_hinhanh")
public class ComBoHinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnh")
    private Integer maHinhAnh;

    @Column(name = "ComboId", nullable = false)
    private Integer comboId;

    @Column(name = "HinhAnh", nullable = false, length = 500)
    private String hinhAnh;

    @Column(name = "TenHinhAnh", length = 255)
    private String tenHinhAnh;

    @Column(name = "ThuTu", nullable = false)
    private Integer thuTu;

    public ComBoHinhAnh() {
    }

    public Integer getMaHinhAnh() { return maHinhAnh; }
    public void setMaHinhAnh(Integer maHinhAnh) { this.maHinhAnh = maHinhAnh; }
    public Integer getComboId() { return comboId; }
    public void setComboId(Integer comboId) { this.comboId = comboId; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public String getTenHinhAnh() { return tenHinhAnh; }
    public void setTenHinhAnh(String tenHinhAnh) { this.tenHinhAnh = tenHinhAnh; }
    public Integer getThuTu() { return thuTu; }
    public void setThuTu(Integer thuTu) { this.thuTu = thuTu; }
}
