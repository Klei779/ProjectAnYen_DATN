package vn.anyen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComBoChiTietHinhAnhResponse {

    private Integer maHinhAnh;

    private String tenHinhAnh;

    private String hinhAnh;

    private Integer thuTu;
}
