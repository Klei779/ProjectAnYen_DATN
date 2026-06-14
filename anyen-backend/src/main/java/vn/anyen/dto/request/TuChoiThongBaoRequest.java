package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TuChoiThongBaoRequest {

    @NotBlank(message = "Vui lòng nhập lý do từ chối")
    private String lyDo;
}