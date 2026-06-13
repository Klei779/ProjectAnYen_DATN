package vn.anyen.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamDoiTacPageResponse {

    private List<SanPhamDoiTacResponse> items;
    private long total;
}