package vn.anyen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuKhachHangResponse {
    private Integer id;
    private String giaiDoan;
    private String trangThai;
    private String noiDung;
    private String ghiChu;
    private String thoiGian;
}