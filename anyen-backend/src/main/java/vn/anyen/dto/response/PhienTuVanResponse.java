package vn.anyen.dto.response;

import java.time.LocalDateTime;

public class PhienTuVanResponse {
    private final Long maPhien;
    private final String tokenPhien;
    private final String tenKhachHang;
    private final Integer maNhanVienPhuTrach;
    private final String tenNhanVienPhuTrach;
    private final Integer trangThai;
    private final String tenTrangThai;
    private final String tinNhanCuoi;
    private final LocalDateTime thoiGianTinNhanCuoi;
    private final Integer soTinNhanChuaDocNhanVien;
    private final Integer soTinNhanChuaDocKhach;
    private final LocalDateTime createdAt;
    private final String guestToken;

    public PhienTuVanResponse(
            Long maPhien,
            String tokenPhien,
            String tenKhachHang,
            Integer maNhanVienPhuTrach,
            String tenNhanVienPhuTrach,
            Integer trangThai,
            String tenTrangThai,
            String tinNhanCuoi,
            LocalDateTime thoiGianTinNhanCuoi,
            Integer soTinNhanChuaDocNhanVien,
            Integer soTinNhanChuaDocKhach,
            LocalDateTime createdAt,
            String guestToken
    ) {
        this.maPhien = maPhien;
        this.tokenPhien = tokenPhien;
        this.tenKhachHang = tenKhachHang;
        this.maNhanVienPhuTrach = maNhanVienPhuTrach;
        this.tenNhanVienPhuTrach = tenNhanVienPhuTrach;
        this.trangThai = trangThai;
        this.tenTrangThai = tenTrangThai;
        this.tinNhanCuoi = tinNhanCuoi;
        this.thoiGianTinNhanCuoi = thoiGianTinNhanCuoi;
        this.soTinNhanChuaDocNhanVien = soTinNhanChuaDocNhanVien;
        this.soTinNhanChuaDocKhach = soTinNhanChuaDocKhach;
        this.createdAt = createdAt;
        this.guestToken = guestToken;
    }

    public Long getMaPhien() { return maPhien; }
    public String getTokenPhien() { return tokenPhien; }
    public String getTenKhachHang() { return tenKhachHang; }
    public Integer getMaNhanVienPhuTrach() { return maNhanVienPhuTrach; }
    public String getTenNhanVienPhuTrach() { return tenNhanVienPhuTrach; }
    public Integer getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
    public String getTinNhanCuoi() { return tinNhanCuoi; }
    public LocalDateTime getThoiGianTinNhanCuoi() { return thoiGianTinNhanCuoi; }
    public Integer getSoTinNhanChuaDocNhanVien() { return soTinNhanChuaDocNhanVien; }
    public Integer getSoTinNhanChuaDocKhach() { return soTinNhanChuaDocKhach; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getGuestToken() { return guestToken; }
}
