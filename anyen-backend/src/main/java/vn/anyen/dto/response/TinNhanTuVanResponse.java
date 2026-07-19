package vn.anyen.dto.response;

import java.time.LocalDateTime;

public class TinNhanTuVanResponse {
    private final Long maTinNhan;
    private final Long maPhien;
    private final String nguoiGui;
    private final Integer maNhanVien;
    private final String tenNguoiGui;
    private final String noiDung;
    private final Boolean daDoc;
    private final LocalDateTime createdAt;

    public TinNhanTuVanResponse(
            Long maTinNhan,
            Long maPhien,
            String nguoiGui,
            Integer maNhanVien,
            String tenNguoiGui,
            String noiDung,
            Boolean daDoc,
            LocalDateTime createdAt
    ) {
        this.maTinNhan = maTinNhan;
        this.maPhien = maPhien;
        this.nguoiGui = nguoiGui;
        this.maNhanVien = maNhanVien;
        this.tenNguoiGui = tenNguoiGui;
        this.noiDung = noiDung;
        this.daDoc = daDoc;
        this.createdAt = createdAt;
    }

    public Long getMaTinNhan() { return maTinNhan; }
    public Long getMaPhien() { return maPhien; }
    public String getNguoiGui() { return nguoiGui; }
    public Integer getMaNhanVien() { return maNhanVien; }
    public String getTenNguoiGui() { return tenNguoiGui; }
    public String getNoiDung() { return noiDung; }
    public Boolean getDaDoc() { return daDoc; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
