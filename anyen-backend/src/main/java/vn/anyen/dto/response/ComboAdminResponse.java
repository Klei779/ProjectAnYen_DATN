package vn.anyen.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ComboAdminResponse {
    private final Integer comboId;
    private final Integer maNhanVienTao;
    private final String tenNguoiTao;
    private final String tenCombo;
    private final BigDecimal gia;
    private final BigDecimal tongGiaSanPham;
    private final String moTa;
    private final String ghiChu;
    private final String hinhAnh;
    private final List<String> hinhAnhDaiDiens;
    private final List<String> hinhAnhQuyTrinhs;
    private final Integer trangThai;
    private final String tenTrangThai;
    private final List<SanPhamComboAdminResponse> sanPhams;

    public ComboAdminResponse(
            Integer comboId,
            Integer maNhanVienTao,
            String tenNguoiTao,
            String tenCombo,
            BigDecimal gia,
            BigDecimal tongGiaSanPham,
            String moTa,
            String ghiChu,
            String hinhAnh,
            List<String> hinhAnhDaiDiens,
            List<String> hinhAnhQuyTrinhs,
            Integer trangThai,
            String tenTrangThai,
            List<SanPhamComboAdminResponse> sanPhams
    ) {
        this.comboId = comboId;
        this.maNhanVienTao = maNhanVienTao;
        this.tenNguoiTao = tenNguoiTao;
        this.tenCombo = tenCombo;
        this.gia = gia;
        this.tongGiaSanPham = tongGiaSanPham;
        this.moTa = moTa;
        this.ghiChu = ghiChu;
        this.hinhAnh = hinhAnh;
        this.hinhAnhDaiDiens = hinhAnhDaiDiens;
        this.hinhAnhQuyTrinhs = hinhAnhQuyTrinhs;
        this.trangThai = trangThai;
        this.tenTrangThai = tenTrangThai;
        this.sanPhams = sanPhams;
    }

    public Integer getComboId() { return comboId; }
    public Integer getMaNhanVienTao() { return maNhanVienTao; }
    public String getTenNguoiTao() { return tenNguoiTao; }
    public String getTenCombo() { return tenCombo; }
    public BigDecimal getGia() { return gia; }
    public BigDecimal getTongGiaSanPham() { return tongGiaSanPham; }
    public String getMoTa() { return moTa; }
    public String getGhiChu() { return ghiChu; }
    public String getHinhAnh() { return hinhAnh; }
    public List<String> getHinhAnhDaiDiens() { return hinhAnhDaiDiens; }
    public List<String> getHinhAnhQuyTrinhs() { return hinhAnhQuyTrinhs; }
    public Integer getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
    public List<SanPhamComboAdminResponse> getSanPhams() { return sanPhams; }
}