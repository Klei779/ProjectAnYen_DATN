package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuyetSanPhamResponse {

    private Integer maThongBao;
    private Integer maSanPham;

    private String tenSanPham;
    private String loai;
    private String vatLieu;
    private String mauSac;
    private String tonGiao;
    private String hinhAnh;

    private BigDecimal giaTien;
    private Integer soLuong;

    private String tenDoiTac;
    private String tenDoanhNghiep;

    private String trangThai;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime ngayTao;
}