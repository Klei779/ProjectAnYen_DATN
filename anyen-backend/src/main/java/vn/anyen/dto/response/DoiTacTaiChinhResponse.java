package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTacTaiChinhResponse {

    private Boolean daMoQuy;

    private BigDecimal soDuQuy;

    private BigDecimal soDuQuyDangKhoa;

    private BigDecimal soDuQuyKhaDung;

    private BigDecimal soDuVi;
}