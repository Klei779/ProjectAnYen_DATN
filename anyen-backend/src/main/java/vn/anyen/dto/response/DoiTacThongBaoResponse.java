package vn.anyen.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTacThongBaoResponse {

    private Integer MaThongBao;
    private Integer MaDoiTac;
    private Integer MaDonHang;
    private String Loai;
    private String TieuDe;
    private String NoiDung;
    private String TrangThaiThongBao;
    private String LyDoTuChoi;
    private Boolean DaDoc;
    private LocalDateTime ThoiGianTao;
    private LocalDateTime ThoiGianXuLy;
}