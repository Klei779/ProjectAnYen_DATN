package vn.anyen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XuLyThongBaoResponse {

    private boolean success;
    private String message;
    private String redirectUrl;
    private Integer maThongBao;
    private Integer maDonHang;
}