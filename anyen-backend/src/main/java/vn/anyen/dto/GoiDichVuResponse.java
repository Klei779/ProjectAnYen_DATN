package vn.anyen.dto;

import vn.anyen.entity.ComBo;

import java.math.BigDecimal;
import java.util.List;

public class GoiDichVuResponse {

    private Integer comboId;
    private String tenCombo;
    private BigDecimal gia;
    private String moTa;

    /** Ảnh đại diện đầu tiên, giữ tương thích với website cũ. */
    private String hinhAnh;

    private String ghiChu;

    /** Đúng ba ảnh đại diện của combo mới. */
    private List<String> hinhAnhs;

    /** Ảnh minh họa quy trình do Admin tải lên. */
    private List<String> hinhAnhQuyTrinhs;

    /** Ảnh chi tiết bao quát combo do Admin tải lên. */
    private String hinhAnhChiTiet;

    private Integer trangThai;

    public GoiDichVuResponse() {
    }

    public GoiDichVuResponse(
            Integer comboId,
            String tenCombo,
            BigDecimal gia,
            String moTa,
            String hinhAnh,
            String ghiChu,
            List<String> hinhAnhs,
            List<String> hinhAnhQuyTrinhs,
            String hinhAnhChiTiet,
            Integer trangThai
    ) {
        this.comboId = comboId;
        this.tenCombo = tenCombo;
        this.gia = gia;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.ghiChu = ghiChu;
        this.hinhAnhs = hinhAnhs;
        this.hinhAnhQuyTrinhs = hinhAnhQuyTrinhs;
        this.hinhAnhChiTiet = hinhAnhChiTiet;
        this.trangThai = trangThai;
    }

    public static GoiDichVuResponse fromEntity(
            ComBo combo,
            List<String> hinhAnhDaiDiens,
            List<String> hinhAnhQuyTrinhs,
            String hinhAnhChiTiet
    ) {
        List<String> covers = normalizeImages(hinhAnhDaiDiens);
        List<String> processImages = normalizeImages(hinhAnhQuyTrinhs);

        // Dữ liệu cũ có thể chỉ còn cột combo.HinhAnh.
        if (covers.isEmpty()
                && combo.getHinhAnh() != null
                && !combo.getHinhAnh().isBlank()) {
            covers = List.of(combo.getHinhAnh().trim());
        }

        String mainImage = combo.getHinhAnh();
        if ((mainImage == null || mainImage.isBlank()) && !covers.isEmpty()) {
            mainImage = covers.get(0);
        }

        return new GoiDichVuResponse(
                combo.getComboId(),
                combo.getTenCombo(),
                combo.getGia(),
                combo.getMoTa(),
                mainImage,
                combo.getGhiChu(),
                covers,
                processImages,
                hinhAnhChiTiet != null && !hinhAnhChiTiet.isBlank() ? hinhAnhChiTiet.trim() : null,
                combo.getTrangThai()
        );
    }

    private static List<String> normalizeImages(List<String> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    public Integer getComboId() { return comboId; }
    public void setComboId(Integer comboId) { this.comboId = comboId; }
    public String getTenCombo() { return tenCombo; }
    public void setTenCombo(String tenCombo) { this.tenCombo = tenCombo; }
    public BigDecimal getGia() { return gia; }
    public void setGia(BigDecimal gia) { this.gia = gia; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public List<String> getHinhAnhs() { return hinhAnhs; }
    public void setHinhAnhs(List<String> hinhAnhs) { this.hinhAnhs = hinhAnhs; }
    public List<String> getHinhAnhQuyTrinhs() { return hinhAnhQuyTrinhs; }
    public void setHinhAnhQuyTrinhs(List<String> hinhAnhQuyTrinhs) { this.hinhAnhQuyTrinhs = hinhAnhQuyTrinhs; }
    public String getHinhAnhChiTiet() { return hinhAnhChiTiet; }
    public void setHinhAnhChiTiet(String hinhAnhChiTiet) { this.hinhAnhChiTiet = hinhAnhChiTiet; }
    public Integer getTrangThai() { return trangThai; }
    public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
}