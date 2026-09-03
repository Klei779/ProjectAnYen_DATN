package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HuyHoaDonRequest {

    @NotBlank(message = "Lý do hủy không được để trống")
    @Size(min = 4, max = 500, message = "Lý do hủy phải từ 4 đến 500 ký tự")
    private String lyDoHuy;
}
