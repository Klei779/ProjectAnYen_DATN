package vn.anyen.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamFilterOptionResponse {
    private String id;
    private String name;
    private Long total;
}