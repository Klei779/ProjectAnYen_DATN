package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tinnhantuvan")
public class TinNhanTuVan {

    public static final String NGUOI_GUI_KHACH_HANG = "KHACH_HANG";
    public static final String NGUOI_GUI_NHAN_VIEN = "NHAN_VIEN";
    public static final String NGUOI_GUI_AI = "AI";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTinNhan")
    private Long maTinNhan;

    @Column(name = "MaPhien", nullable = false)
    private Long maPhien;

    @Column(name = "NguoiGui", nullable = false, length = 20)
    private String nguoiGui;

    @Column(name = "MaNhanVien")
    private Integer maNhanVien;

    @Column(name = "NoiDung", nullable = false, length = 2000)
    private String noiDung;

    @Column(name = "DaDoc", nullable = false)
    private Boolean daDoc;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    public TinNhanTuVan() {
    }

    public Long getMaTinNhan() { return maTinNhan; }
    public void setMaTinNhan(Long maTinNhan) { this.maTinNhan = maTinNhan; }
    public Long getMaPhien() { return maPhien; }
    public void setMaPhien(Long maPhien) { this.maPhien = maPhien; }
    public String getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(String nguoiGui) { this.nguoiGui = nguoiGui; }
    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public Boolean getDaDoc() { return daDoc; }
    public void setDaDoc(Boolean daDoc) { this.daDoc = daDoc; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
