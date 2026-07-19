package vn.anyen.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ComboDoiTacResponse {
    private final Integer comboId;
    private final Integer maDoiTac;
    private final String tenCombo;
    private final BigDecimal gia;
    private final BigDecimal tongGiaSanPham;
    private final String moTa;
    private final String hinhAnh;
    private final List<String> hinhAnhs;
    private final Integer trangThai;
    private final String tenTrangThai;
    private final List<SanPhamComboDoiTacResponse> sanPhams;

    public ComboDoiTacResponse(
            Integer comboId,
            Integer maDoiTac,
            String tenCombo,
            BigDecimal gia,
            BigDecimal tongGiaSanPham,
            String moTa,
            String hinhAnh,
            List<String> hinhAnhs,
            Integer trangThai,
            String tenTrangThai,
            List<SanPhamComboDoiTacResponse> sanPhams
    ) {
        this.comboId = comboId;
        this.maDoiTac = maDoiTac;
        this.tenCombo = tenCombo;
        this.gia = gia;
        this.tongGiaSanPham = tongGiaSanPham;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.hinhAnhs = hinhAnhs;
        this.trangThai = trangThai;
        this.tenTrangThai = tenTrangThai;
        this.sanPhams = sanPhams;
    }

    public Integer getComboId() { return comboId; }
    public Integer getMaDoiTac() { return maDoiTac; }
    public String getTenCombo() { return tenCombo; }
    public BigDecimal getGia() { return gia; }
    public BigDecimal getTongGiaSanPham() { return tongGiaSanPham; }
    public String getMoTa() { return moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public List<String> getHinhAnhs() { return hinhAnhs; }
    public Integer getTrangThai() { return trangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
    public List<SanPhamComboDoiTacResponse> getSanPhams() { return sanPhams; }
}
