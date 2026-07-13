package vn.anyen.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TinTucResponse {

    private Integer maTinTuc;

    private String tieuDe;

    private String tomTat;

    private String noiDung;

    private String anhDaiDien;

    private Integer loaiTin;

    private Integer trangThai;

    private LocalDateTime ngayDang;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}