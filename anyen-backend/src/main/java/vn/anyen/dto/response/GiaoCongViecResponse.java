package vn.anyen.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GiaoCongViecResponse {
    private Integer maKhachHang;
    private Integer maThongBao;
    private Integer maNhanVien;
    private String tenNhanVien;
    private String message;
}
