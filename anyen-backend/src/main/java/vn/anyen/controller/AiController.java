package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.AiChatRequest;
import vn.anyen.dto.response.AiChatResponse;
import vn.anyen.service.GeminiService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiService geminiService;

    /**
     * API chatbot Gemini.
     *
     * URL:
     * POST /api/ai/chat
     *
     * Body:
     * {
     *   "message": "An Yên cung cấp những dịch vụ gì?"
     * }
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {
        try {
            String question = request
                    .getMessage()
                    .trim();

            /*
             * Dữ liệu mẫu để kiểm tra Gemini trước.
             *
             * Ở bước sau sẽ thay đoạn này bằng dữ liệu thật
             * lấy từ bảng sanpham, dichvu và thongtin_ai.
             */
            String companyContext = """
                    THÔNG TIN DOANH NGHIỆP:

                    - Tên đơn vị: Dịch vụ Mai táng An Yên.
                    - An Yên cung cấp sản phẩm và dịch vụ
                      hỗ trợ tổ chức tang lễ.
                    - Website có các sản phẩm như quan tài,
                      nền tảng và hoa tang lễ.
                    - Giá và số lượng sản phẩm phải được
                      xác nhận theo dữ liệu hiện tại.
                    - Khách hàng có thể để lại thông tin
                      để nhân viên tư vấn liên hệ.
                    - An Yên không yêu cầu khách hàng cung cấp
                      mật khẩu, mã OTP hoặc thông tin ngân hàng
                      qua chatbot.

                    CHÍNH SÁCH MẪU:

                    - Giá hiển thị trên website chỉ mang tính
                      tham khảo cho đến khi đơn hàng được xác nhận.
                    - Thông tin khách hàng chỉ được sử dụng để
                      tư vấn và thực hiện dịch vụ.
                    - Khi cần tư vấn chi tiết, khách hàng nên
                      liên hệ trực tiếp nhân viên An Yên.
                    """;

            String answer = geminiService.chat(
                    question,
                    companyContext
            );

            return ResponseEntity.ok(
                    new AiChatResponse(
                            true,
                            answer
                    )
            );

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new AiChatResponse(
                                    false,
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(
                            new AiChatResponse(
                                    false,
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new AiChatResponse(
                                    false,
                                    "Đã xảy ra lỗi khi xử lý câu hỏi."
                            )
                    );
        }
    }

    /**
     * API kiểm tra controller đã hoạt động chưa.
     *
     * URL:
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<AiChatResponse> health() {
        return ResponseEntity.ok(
                new AiChatResponse(
                        true,
                        "AI Controller đang hoạt động."
                )
        );
    }
}