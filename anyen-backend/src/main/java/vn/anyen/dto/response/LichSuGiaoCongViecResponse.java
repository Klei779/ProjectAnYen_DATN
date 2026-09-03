package vn.anyen.dto.response;

public class LichSuGiaoCongViecResponse {

    private final Integer maThongBao;
    private final Integer maNhanVien;
    private final String tenNhanVien;
    private final String soDienThoai;
    private final Integer trangThai;
    private final String trangThaiText;
    private final String thoiGianGiao;

    public LichSuGiaoCongViecResponse(
            Integer maThongBao,
            Integer maNhanVien,
            String tenNhanVien,
            String soDienThoai,
            Integer trangThai,
            String trangThaiText,
            String thoiGianGiao
    ) {
        this.maThongBao = maThongBao;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.soDienThoai = soDienThoai;
        this.trangThai = trangThai;
        this.trangThaiText = trangThaiText;
        this.thoiGianGiao = thoiGianGiao;
    }

    public Integer getMaThongBao() { return maThongBao; }
    public Integer getMaNhanVien() { return maNhanVien; }
    public String getTenNhanVien() { return tenNhanVien; }
    public String getSoDienThoai() { return soDienThoai; }
    public Integer getTrangThai() { return trangThai; }
    public String getTrangThaiText() { return trangThaiText; }
    public String getThoiGianGiao() { return thoiGianGiao; }
}
