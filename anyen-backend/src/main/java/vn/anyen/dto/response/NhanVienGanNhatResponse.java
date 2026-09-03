package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class NhanVienGanNhatResponse {
    private Integer maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double khoangCachKm;
    private String trangThai;
}
