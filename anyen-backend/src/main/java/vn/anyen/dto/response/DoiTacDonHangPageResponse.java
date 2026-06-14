package vn.anyen.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTacDonHangPageResponse {

    private List<DoiTacDonHangResponse> items;
    private Integer total;
}