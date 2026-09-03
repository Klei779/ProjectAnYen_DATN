package vn.anyen.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDongPageResponse {

    private List<HopDongResponse> items;

    private long total;
}
