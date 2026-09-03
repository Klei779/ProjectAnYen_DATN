package vn.anyen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangLichSuResponse {

    private String thoiGian;
    private String tieuDe;
    private String noiDung;
    private String loai;
    private String trangThai;
}