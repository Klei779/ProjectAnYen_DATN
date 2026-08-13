package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushUnsubscribeRequest {

    @NotBlank
    private String endpoint;
}