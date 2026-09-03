package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.anyen.entity.HoaDon;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class HoaDonResponse {

    private Integer maHoaDon;
    private Integer maDonHang;
    private String maCode;
    private LocalDate ngayIn;
    private BigDecimal tongTien;
    private Integer phuongThucThanhToan;
    private Integer trangThai;

    public static HoaDonResponse fromEntity(HoaDon hoaDon) {
        Integer maDonHang = hoaDon.getDonHang() != null
                ? hoaDon.getDonHang().getMaDonHang()
                : null;

        return HoaDonResponse.builder()
                .maHoaDon(hoaDon.getMaHoaDon())
                .maDonHang(maDonHang)
                .maCode(maDonHang != null ? "HD" + String.format("%04d", hoaDon.getMaHoaDon()) : null)
                .ngayIn(hoaDon.getNgayIn())
                .tongTien(hoaDon.getTongTien())
                .phuongThucThanhToan(hoaDon.getPhuongThucThanhToan())
                .trangThai(hoaDon.getTrangThai())
                .build();
    }
}
