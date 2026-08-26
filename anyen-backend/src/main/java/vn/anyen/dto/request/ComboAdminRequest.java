package vn.anyen.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class ComboAdminRequest {

    @NotBlank(message = "Tên combo không được để trống")
    @Size(max = 255, message = "Tên combo tối đa 255 ký tự")
    private String tenCombo;

    @NotNull(message = "Giá combo không được để trống")
    @Positive(message = "Giá combo phải lớn hơn 0")
    private BigDecimal gia;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String moTa;

    @Size(max = 10000, message = "Quyền lợi/ghi chú tối đa 10000 ký tự")
    private String ghiChu;

    private Integer trangThai;
    private Boolean thayAnhDaiDien = false;
    private Boolean thayAnhQuyTrinh = false;

    @Valid
    @NotEmpty(message = "Combo phải có ít nhất một sản phẩm được tick chọn")
    private List<ComboSanPhamRequest> sanPhams;

    public ComboAdminRequest() {
    }

    public String getTenCombo() {
        return tenCombo;
    }

    public void setTenCombo(String tenCombo) {
        this.tenCombo = tenCombo;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getThayAnhDaiDien() {
        return thayAnhDaiDien;
    }

    public void setThayAnhDaiDien(Boolean thayAnhDaiDien) {
        this.thayAnhDaiDien = thayAnhDaiDien;
    }

    public Boolean getThayAnhQuyTrinh() {
        return thayAnhQuyTrinh;
    }

    public void setThayAnhQuyTrinh(Boolean thayAnhQuyTrinh) {
        this.thayAnhQuyTrinh = thayAnhQuyTrinh;
    }

    public List<ComboSanPhamRequest> getSanPhams() {
        return sanPhams;
    }

    public void setSanPhams(List<ComboSanPhamRequest> sanPhams) {
        this.sanPhams = sanPhams;
    }

    public static class ComboSanPhamRequest {

        @NotNull(message = "Mã sản phẩm không được để trống")
        private Integer maSanPham;

        @NotNull(message = "Số lượng sản phẩm không được để trống")
        @Positive(message = "Số lượng sản phẩm trong combo phải lớn hơn 0")
        private Integer soLuong;

        @Size(max = 255, message = "Nội dung sản phẩm trong combo tối đa 255 ký tự")
        private String noiDung;

        public ComboSanPhamRequest() {
        }

        public Integer getMaSanPham() {
            return maSanPham;
        }

        public void setMaSanPham(Integer maSanPham) {
            this.maSanPham = maSanPham;
        }

        public Integer getSoLuong() {
            return soLuong;
        }

        public void setSoLuong(Integer soLuong) {
            this.soLuong = soLuong;
        }

        public String getNoiDung() {
            return noiDung;
        }

        public void setNoiDung(String noiDung) {
            this.noiDung = noiDung;
        }
    }
}