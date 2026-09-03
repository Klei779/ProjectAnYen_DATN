package vn.anyen.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamPageResponse {
    private List<SanPhamResponse> items;
    private Long total;
}