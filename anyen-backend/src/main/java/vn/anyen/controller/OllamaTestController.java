package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.service.OllamaService;

import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaTestController {

    private final OllamaService ollamaService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        boolean available =
                ollamaService.isAvailable();

        if (!available) {
            return ResponseEntity
                    .status(503)
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Không thể kết nối Ollama."
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message",
                        "Ollama đang hoạt động.",
                        "model",
                        ollamaService.getModel()
                )
        );
    }

    @PostMapping("/test")
    public ResponseEntity<?> test(
            @RequestBody Map<String, String> request
    ) {
        String message = request.get("message");

        String answer = ollamaService.chat(
                """
                Bạn là trợ lý của Dịch vụ Mai táng An Yên.

                Quy tắc:
                - Luôn trả lời bằng tiếng Việt.
                - Trả lời lịch sự.
                - Không quá 50 từ.
                """,
                message
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "provider", "OLLAMA",
                        "model", ollamaService.getModel(),
                        "reply", answer
                )
        );
    }

    @PostMapping("/test-json")
    public ResponseEntity<?> testJson(
            @RequestBody Map<String, String> request
    ) {
        try {
            String message = request.get("message");

            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Thiếu trường message."
                        )
                );
            }

            String answer = ollamaService.chatJson(
                    """
                    Bạn là trợ lý tư vấn của Dịch vụ Mai táng An Yên.
            
                    Đây là dịch vụ hợp pháp về tang lễ, an táng,
                    hỏa táng và sản phẩm tang lễ.
            
                    QUY TẮC:
            
                    - Luôn viết tiếng Việt có dấu.
                    - Không tự tạo tên sản phẩm hoặc giá bán.
                    - Không tự suy đoán thông tin khách chưa cung cấp.
                    - Không hỏi số lượng người cần an táng.
                    - Không dùng các nhu cầu mơ hồ như:
                      "Thông tin chi tiết", "Tư vấn chung", "Hỗ trợ thêm".
                    - Chỉ trả về JSON đúng schema.
                    - Không dùng markdown.
                    """,
                    """
                    TIN NHẮN KHÁCH HÀNG:
                    %s
            
                    Hãy xác định nhu cầu trực tiếp từ câu khách nói.
            
                    CÁCH PHÂN LOẠI:
            
                    - Có từ "quan tài", "hòm", "áo quan":
                      nhuCau = "Tư vấn quan tài".
            
                    - Có từ "hỏa táng":
                      nhuCau = "Dịch vụ hỏa táng".
            
                    - Có từ "an táng", "chôn cất":
                      nhuCau = "Dịch vụ an táng".
            
                    - Có từ "tang lễ", "làm đám tang":
                      nhuCau = "Tổ chức tang lễ".
            
                    - Nếu câu có cả "quan tài" và "an táng":
                      nhuCau = "Tư vấn quan tài để an táng".
            
                    QUY TẮC REPLY:
            
                    - Xác nhận đúng nhu cầu của khách.
                    - Chỉ hỏi thêm tối đa một thông tin phù hợp.
                    - Với quan tài, nên hỏi ngân sách, vật liệu
                      hoặc kiểu dáng mong muốn.
                    - Không hỏi số lượng người.
                    - Reply dưới 50 từ.
                    - Không chào lại nếu không cần thiết.
                    
                    """.formatted(message)
            );
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "provider", "OLLAMA",
                            "model", ollamaService.getModel(),
                            "rawJson", answer
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(502).body(
                    Map.of(
                            "success", false,
                            "message",
                            e.getMessage() == null
                                    ? "Ollama xảy ra lỗi."
                                    : e.getMessage()
                    )
            );
        }
    }
}