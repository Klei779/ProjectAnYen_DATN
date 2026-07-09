package vn.anyen.dto.response;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeDoanhThuResponse {
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private String kieuThongKe;

    private DoanhThuTongQuanResponse tongQuan;
    private List<DoanhThuTheoThoiGianResponse> bieuDoDoanhThu;
    private List<DoanhThuSanPhamResponse> topSanPham;
    private List<DoanhThuPhuongThucResponse> phuongThucThanhToan;
}