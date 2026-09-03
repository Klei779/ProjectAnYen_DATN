package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaoCaoSuCoRequest {

    @NotBlank(message = "Vui lòng nhập lý do sự cố")
    @Size(min = 4, message = "Lý do sự cố phải trên 3 ký tự")
    private String lyDoSuCo;
}
