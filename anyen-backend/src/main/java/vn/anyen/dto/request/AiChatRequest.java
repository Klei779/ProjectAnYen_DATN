package vn.anyen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatRequest {

    @NotBlank(message = "Vui lòng nhập nội dung cần hỏi")
    @Size(
            max = 1000,
            message = "Câu hỏi không được vượt quá 1000 ký tự"
    )
    private String message;
}