package vn.anyen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangResponse {
    private Integer id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String status;
    private String stage;
    private String avatar;
}