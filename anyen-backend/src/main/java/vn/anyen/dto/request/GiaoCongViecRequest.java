package vn.anyen.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiaoCongViecRequest {
    private Integer maNhanVien;
    private String hoTenKhachHang;
    private String soDienThoaiKhachHang;
    private String diaChiKhachHang;
    private String audioUrl;
    private Double latitude;
    private Double longitude;
}
