package vn.anyen.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamFilterResponse {
    private List<SanPhamFilterOptionResponse> categories;
    private List<SanPhamFilterOptionResponse> materials;
    private List<SanPhamFilterOptionResponse> religions;
    private List<SanPhamFilterOptionResponse> colors;
}