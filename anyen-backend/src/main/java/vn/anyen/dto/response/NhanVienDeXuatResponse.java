package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienDeXuatResponse {
    private Integer maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private String diaChiDayDu;
    private String tinhThanh;
    private String trangThaiLamViec;
    private String trangThaiLamViecText;
    private Double khoangCach; // km
    private String khoangCachText;
    private Long donDangXuLy; // số đơn đang xử lý
    private Long donHoanThanh; // số đơn đã hoàn thành
    private Double diem; // điểm xếp hạng (càng thấp càng tốt)
    private BigDecimal latitude; // tọa độ nhân viên
    private BigDecimal longitude; // tọa độ nhân viên
}
