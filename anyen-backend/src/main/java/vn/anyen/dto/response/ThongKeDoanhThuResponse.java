package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private List<DoanhThuTheoThoiGianResponse>
            bieuDoDoanhThu;

    private List<DoanhThuSanPhamResponse>
            topSanPham;

    private List<DoanhThuDoiTuongResponse>
            topNhanVien;

    private List<DoanhThuDoiTuongResponse>
            topDoiTac;

    private List<DoanhThuPhuongThucResponse>
            phuongThucThanhToan;
}