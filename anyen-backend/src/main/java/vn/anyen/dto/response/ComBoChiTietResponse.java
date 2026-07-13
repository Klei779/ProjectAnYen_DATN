package vn.anyen.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComBoChiTietResponse {

    private Integer comboChiTietId;

    private Integer loai;

    private String noiDung;

    private List<ComBoChiTietHinhAnhResponse> hinhAnhs;
}
