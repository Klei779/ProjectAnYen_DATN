package vn.anyen.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushSubscriptionRequest {

    @NotBlank(message = "Endpoint không được để trống")
    private String endpoint;

    @Valid
    @NotNull(message = "Keys không được để trống")
    private Keys keys;

    @Getter
    @Setter
    public static class Keys {

        @NotBlank(message = "p256dh không được để trống")
        private String p256dh;

        @NotBlank(message = "auth không được để trống")
        private String auth;
    }
}
