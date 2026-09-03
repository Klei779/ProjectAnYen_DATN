package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HuyDonHangRequest {

    @NotBlank(message = "Vui lòng nhập lý do hủy đơn hàng")
    @Size(min = 4, message = "Lý do hủy phải trên 3 ký tự")
    private String lyDoHuy;
}