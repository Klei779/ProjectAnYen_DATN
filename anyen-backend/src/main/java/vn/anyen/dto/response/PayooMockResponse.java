package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayooMockResponse {

    private String maGiaoDich;

    private String loaiGiaoDich;

    private Integer maDoiTac;

    private Integer maCongNo;

    private BigDecimal soTien;

    private Integer trangThai;

    private String trangThaiText;

    private String noiDung;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

}