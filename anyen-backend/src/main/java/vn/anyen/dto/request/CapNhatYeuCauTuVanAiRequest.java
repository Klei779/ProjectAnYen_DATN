package vn.anyen.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CapNhatYeuCauTuVanAiRequest {

    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String nhuCau;
    private String thoiGianMongMuon;
    private BigDecimal nganSachDuKien;
    private String ghiChu;
}