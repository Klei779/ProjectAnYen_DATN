package vn.anyen.dto.response;

import java.math.BigDecimal;

public class SanPhamComboAdminResponse {
    private final Integer maSanPham;
    private final String tenSanPham;
    private final String loai;
    private final String vatLieu;
    private final String mauSac;
    private final BigDecimal giaTien;
    private final String hinhAnh;
    private final Integer soLuong;
    private final Integer soLuongTrongCombo;
    private final BigDecimal thanhTien;
    private final Integer trangThai;
    private final Integer maDoiTac;
    private final String tenDoiTac;

    public SanPhamComboAdminResponse(
            Integer maSanPham,
            String tenSanPham,
            String loai,
            String vatLieu,
            String mauSac,
            BigDecimal giaTien,
            String hinhAnh,
            Integer soLuong,
            Integer soLuongTrongCombo,
            BigDecimal thanhTien,
            Integer trangThai,
            Integer maDoiTac,
            String tenDoiTac
    ) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.loai = loai;
        this.vatLieu = vatLieu;
        this.mauSac = mauSac;
        this.giaTien = giaTien;
        this.hinhAnh = hinhAnh;
        this.soLuong = soLuong;
        this.soLuongTrongCombo = soLuongTrongCombo;
        this.thanhTien = thanhTien;
        this.trangThai = trangThai;
        this.maDoiTac = maDoiTac;
        this.tenDoiTac = tenDoiTac;
    }

    public Integer getMaSanPham() { return maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public String getLoai() { return loai; }
    public String getVatLieu() { return vatLieu; }
    public String getMauSac() { return mauSac; }
    public BigDecimal getGiaTien() { return giaTien; }
    public String getHinhAnh() { return hinhAnh; }
    public Integer getSoLuong() { return soLuong; }
    public Integer getSoLuongTrongCombo() { return soLuongTrongCombo; }
    public BigDecimal getThanhTien() { return thanhTien; }
    public Integer getTrangThai() { return trangThai; }
    public Integer getMaDoiTac() { return maDoiTac; }
    public String getTenDoiTac() { return tenDoiTac; }
}