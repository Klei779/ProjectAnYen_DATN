package vn.anyen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.YeuCauTuVanAi;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.model}")
    private String model;

    public GeminiService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Chat thông thường với khách hàng.
     */
    public String chat(
            String userMessage,
            String companyContext
    ) {
        validateConfiguration();
        validateMessage(userMessage);

        String systemInstruction = """
                Bạn là trợ lý tư vấn trực tuyến của
                Dịch vụ Mai táng An Yên.

                Quy tắc bắt buộc:

                1. Luôn trả lời bằng tiếng Việt.
                2. Giọng điệu lịch sự, nhẹ nhàng và tôn trọng.
                3. Chỉ trả lời dựa trên dữ liệu An Yên được cung cấp.
                4. Không tự bịa tên sản phẩm, giá bán, số lượng,
                   dịch vụ, điều khoản hoặc chính sách.
                5. Nếu dữ liệu không có câu trả lời, hãy nói:
                   "Hiện tại An Yên chưa có đủ thông tin để trả lời
                   chính xác. Anh/chị vui lòng liên hệ nhân viên
                   An Yên để được hỗ trợ."
                6. Không yêu cầu khách hàng cung cấp mật khẩu,
                   mã OTP hoặc thông tin ngân hàng.
                7. Không khẳng định sản phẩm còn hàng nếu số lượng
                   bằng 0 hoặc dữ liệu không có số lượng.
                8. Trả lời ngắn gọn, rõ ràng và dễ hiểu.
                9. Không trả lời quá 250 từ, trừ khi khách yêu cầu
                   giải thích chi tiết.
                """;

        String safeContext =
                companyContext == null
                        || companyContext.isBlank()
                        ? "Chưa có dữ liệu doanh nghiệp."
                        : companyContext.trim();

        String completePrompt = """
                DỮ LIỆU HIỆN TẠI CỦA AN YÊN:
                --------------------------------
                %s
                --------------------------------

                CÂU HỎI CỦA KHÁCH HÀNG:
                %s

                Hãy trả lời dựa trên dữ liệu An Yên ở trên.
                Không được tự bổ sung thông tin không có trong dữ liệu.
                """.formatted(
                safeContext,
                userMessage.trim()
        );

        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.2,
                "maxOutputTokens", 600
        );

        return callGemini(
                systemInstruction,
                completePrompt,
                generationConfig
        );
    }

    /**
     * Gemini đọc tin nhắn mới và trích xuất thông tin khách hàng.
     */
    public AiTrichXuatKhachHangResult
    trichXuatThongTinKhachHang(
            String tenKhachHang,
            YeuCauTuVanAi thongTinHienTai,
            String tinNhanMoi
    ) {
        validateConfiguration();
        validateMessage(tinNhanMoi);

        if (thongTinHienTai == null) {
            throw new IllegalArgumentException(
                    "Thông tin yêu cầu tư vấn không được để trống."
            );
        }

        String systemInstruction = """
                Bạn là trợ lý tiếp nhận thông tin khách hàng
                của Dịch vụ Mai táng An Yên.

                Nhiệm vụ của bạn là:
                - Đọc tin nhắn mới của khách hàng.
                - Trích xuất thông tin khách đã cung cấp.
                - Giữ lại thông tin cũ nếu khách không sửa.
                - Không tự suy đoán dữ liệu khách chưa nói.
                - Hỏi tiếp những thông tin bắt buộc còn thiếu.
                - Trả về đúng một đối tượng JSON hợp lệ.
                """;

        String prompt = """
                TÊN KHÁCH TRONG PHIÊN:
                %s

                THÔNG TIN HIỆN ĐANG LƯU:
                - Họ tên: %s
                - Số điện thoại: %s
                - Địa chỉ: %s
                - Nhu cầu: %s
                - Thời gian mong muốn: %s
                - Ngân sách dự kiến: %s
                - Ghi chú: %s

                TIN NHẮN MỚI:
                %s

                CÁC TRƯỜNG BẮT BUỘC:
                - hoTen
                - soDienThoai
                - diaChi
                - nhuCau

                QUY TẮC TRÍCH XUẤT:
                                
                1. Chỉ sử dụng thông tin khách thực sự cung cấp.
                   Không tự tạo họ tên, số điện thoại, địa chỉ, nhu cầu,
                   thời gian hoặc ngân sách.
                                
                2. Giữ nguyên dữ liệu cũ nếu tin nhắn mới không sửa.
                   Nếu khách sửa, ưu tiên dữ liệu mới.
                                
                3. Trường chưa xác định phải trả về null.
                   Không suy đoán từ câu nói mơ hồ.
                                
                4. Nhận biết nhu cầu theo cách nói tự nhiên:
                   - hòm, quan tài, áo quan => nhu cầu mua hoặc tư vấn quan tài;
                   - hỏa táng => dịch vụ hỏa táng;
                   - làm tang lễ => tổ chức tang lễ.
                   Khi đã hiểu nhu cầu, không hỏi lại khách cần gì.
                                
                5. missingFields chỉ gồm các trường bắt buộc còn thiếu:
                   hoTen, soDienThoai, diaChi, nhuCau.
                                
                6. Backend cần đủ họ tên, số điện thoại, địa chỉ và nhu cầu
                   trước khi chuyển Hotline.
                                
                7. Nếu chưa đủ dữ liệu:
                   - readyForHotline = false;
                   - customerConfirmed = false;
                   - hỏi tối đa hai thông tin phù hợp nhất trong mỗi lượt.
                                
                8. Không hỏi dồn thông tin. Ưu tiên hỏi theo ngữ cảnh:
                   nhu cầu → ngân sách/thời gian → địa chỉ → họ tên/số điện thoại.
                                
                9. Nếu khách mới tham khảo, hãy tư vấn sơ bộ trước.
                   Chỉ ưu tiên xin thông tin liên hệ khi khách muốn lên đơn,
                   nhận báo giá, được gọi lại hoặc chuyển nhân viên.
                                
                10. Reply phải tự nhiên, lịch sự, dưới 80 từ.
                    Chỉ chào ở lượt đầu; không lặp lời chào ở các lượt sau.
                                
                11. Không nhắc lại nguyên văn lời khách.
                    Có thể xác nhận ngắn như “Dạ được ạ” hoặc
                    “Em hiểu nhu cầu của anh/chị rồi ạ”.
                                
                12. Thay đổi cách diễn đạt giữa các lượt.
                    Không dùng cùng một câu mở đầu liên tiếp và không luôn nói
                    “đã ghi nhận”.
                                
                13. Khi hỏi thêm, ưu tiên cấu trúc:
                    xác nhận ngắn → giải thích lý do → hỏi một hoặc hai thông tin.
                                
                14. Nếu đủ dữ liệu nhưng khách chưa xác nhận:
                    - readyForHotline = true;
                    - customerConfirmed = false;
                    - tổng hợp ngắn họ tên, số điện thoại, địa chỉ, nhu cầu,
                      ngân sách và hỏi thông tin đã chính xác chưa.
                                
                15. customerConfirmed chỉ được true khi:
                    - thông tin bắt buộc đã đủ;
                    - khách xác nhận rõ như “đúng rồi”, “xác nhận”,
                      “thông tin chính xác”.
                    Chỉ nói “ok” chưa chắc là xác nhận.
                                
                16. Khi customerConfirmed = true:
                    - không hỏi thêm;
                    - nói ngắn rằng đã xác nhận và đang chuyển Hotline.
                                
                17. nganSachDuKien phải là số hoặc null:
                    - 25 triệu => 25000000;
                    - 1,5 triệu => 1500000.
                    Chỉ gán khi khách nói rõ đây là ngân sách của nhu cầu hiện tại.
                                
                18. Chỉ trả về một JSON hoàn chỉnh, không markdown,
                    không giải thích, không bỏ bất kỳ trường nào.
                    
                MỤC TIÊU HỘI THOẠI:
                                
                - Trước tiên hiểu khách đang cần gì.
                - Tư vấn sơ bộ theo đúng nhu cầu.
                - Hỏi thêm từng phần một cách tự nhiên.
                - Chỉ thu thập đủ thông tin lên đơn khi khách có ý định tiếp tục.
                - Không ép khách cung cấp thông tin quá sớm.
                - Không lặp lại câu chào hoặc nội dung khách vừa nói.

                JSON BẮT BUỘC:
                                
                {
                  "reply": "Câu trả lời gửi cho khách",
                  "customerInfo": {
                    "hoTen": null,
                    "soDienThoai": null,
                    "diaChi": null,
                    "nhuCau": null,
                    "thoiGianMongMuon": null,
                    "nganSachDuKien": 25000000,
                    "ghiChu": null
                  },
                  "missingFields": [],
                  "readyForHotline": false,
                  "customerConfirmed": false
                }
                """.formatted(
                safeValue(tenKhachHang),
                safeValue(
                        firstNonBlank(
                                thongTinHienTai.getHoTen(),
                                tenKhachHang
                        )
                ),
                safeValue(thongTinHienTai.getSoDienThoai()),
                safeValue(thongTinHienTai.getDiaChi()),
                safeValue(thongTinHienTai.getNhuCau()),
                safeValue(thongTinHienTai.getThoiGianMongMuon()),
                safeValue(thongTinHienTai.getNganSachDuKien()),
                safeValue(thongTinHienTai.getGhiChu()),
                tinNhanMoi.trim()
        );

        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.15,
                "maxOutputTokens", 1200,
                "responseMimeType", "application/json"
        );

        String rawAnswer = callGemini(
                systemInstruction,
                prompt,
                generationConfig
        );

        String cleanJson =
                cleanJsonResponse(rawAnswer);

        try {
            AiTrichXuatKhachHangResult result =
                    objectMapper.readValue(
                            cleanJson,
                            AiTrichXuatKhachHangResult.class
                    );

            normalizeExtractionResult(result);

            return result;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Gemini trả về JSON không hợp lệ. "
                            + "Nguyên nhân: "
                            + e.getOriginalMessage()
                            + ". JSON nhận được: "
                            + cleanJson,
                    e
            );
        }
    }

    /**
     * Gọi Gemini API dùng chung cho chat thường và trích xuất JSON.
     */
    private String callGemini(
            String systemInstruction,
            String prompt,
            Map<String, Object> generationConfig
    ) {
        String requestUrl =
                baseUrl
                        + "/v1beta/models/"
                        + model
                        + ":generateContent";

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(
                                Map.of(
                                        "text",
                                        systemInstruction
                                )
                        )
                ),
                "contents", List.of(
                        Map.of(
                                "role",
                                "user",
                                "parts", List.of(
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                )
                        )
                ),
                "generationConfig", generationConfig
        );

        try {
            Map<?, ?> response = restClient
                    .post()
                    .uri(requestUrl)
                    .header(
                            "x-goog-api-key",
                            apiKey
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractAnswer(response);

        } catch (HttpClientErrorException.TooManyRequests e) {
            String retryAfter =
                    e.getResponseHeaders() != null
                            ? e.getResponseHeaders().getFirst("Retry-After")
                            : null;

            String message =
                    "Gemini đang vượt hạn mức sử dụng.";

            if (retryAfter != null && !retryAfter.isBlank()) {
                message += " Vui lòng thử lại sau "
                        + retryAfter
                        + " giây.";
            } else {
                message += " Vui lòng chờ một lúc rồi thử lại.";
            }

            System.err.println(
                    "Gemini 429 response: "
                            + e.getResponseBodyAsString()
            );

            throw new RuntimeException(message, e);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new RuntimeException(
                    "Yêu cầu gửi đến Gemini không hợp lệ: "
                            + getErrorMessage(e)
            );

        } catch (
                HttpClientErrorException.Unauthorized
                | HttpClientErrorException.Forbidden e
        ) {
            throw new RuntimeException(
                    "Gemini API key không hợp lệ "
                            + "hoặc chưa được cấp quyền."
            );

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException(
                    "Không tìm thấy model Gemini: "
                            + model
                            + ". Hãy kiểm tra gemini.model "
                            + "trong application.yaml."
            );

        } catch (HttpClientErrorException e) {
            throw new RuntimeException(
                    "Gemini API trả về lỗi HTTP "
                            + e.getStatusCode().value()
                            + ": "
                            + getErrorMessage(e)
            );

        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Không thể kết nối đến Gemini API. "
                            + "Hãy kiểm tra Internet và cấu hình API.",
                    e
            );
        }
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình biến môi trường "
                            + "GEMINI_API_KEY."
            );
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình gemini.base-url "
                            + "trong application.yaml."
            );
        }

        if (model == null || model.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình gemini.model "
                            + "trong application.yaml."
            );
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Tin nhắn không được để trống."
            );
        }

        if (message.length() > 3000) {
            throw new IllegalArgumentException(
                    "Tin nhắn không được vượt quá 3000 ký tự."
            );
        }
    }

    private String extractAnswer(Map<?, ?> response) {
        if (response == null) {
            throw new RuntimeException(
                    "Gemini không trả về dữ liệu."
            );
        }

        Object candidatesObject =
                response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)
                || candidates.isEmpty()) {
            throw new RuntimeException(
                    getBlockedReason(response)
            );
        }

        Object firstCandidateObject =
                candidates.get(0);

        if (!(firstCandidateObject
                instanceof Map<?, ?> firstCandidate)) {
            throw new RuntimeException(
                    "Phản hồi Gemini không đúng định dạng."
            );
        }

        Object contentObject =
                firstCandidate.get("content");

        if (!(contentObject instanceof Map<?, ?> content)) {
            throw new RuntimeException(
                    "Gemini không trả về nội dung."
            );
        }

        Object partsObject =
                content.get("parts");

        if (!(partsObject instanceof List<?> parts)
                || parts.isEmpty()) {
            throw new RuntimeException(
                    "Gemini không trả về nội dung văn bản."
            );
        }

        StringBuilder answerBuilder =
                new StringBuilder();

        for (Object partObject : parts) {
            if (!(partObject instanceof Map<?, ?> part)) {
                continue;
            }

            Object textObject =
                    part.get("text");

            if (textObject != null) {
                answerBuilder
                        .append(textObject)
                        .append("\n");
            }
        }

        String answer =
                answerBuilder
                        .toString()
                        .trim();

        if (answer.isBlank()) {
            throw new RuntimeException(
                    "Gemini không tạo được câu trả lời."
            );
        }

        return answer;
    }

    private String cleanJsonResponse(String value) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(
                    "Gemini không trả về dữ liệu JSON."
            );
        }

        String cleaned = value.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(
                    0,
                    cleaned.length() - 3
            );
        }

        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');

        if (startIndex < 0 || endIndex < startIndex) {
            throw new RuntimeException(
                    "Không tìm thấy JSON trong phản hồi Gemini: "
                            + cleaned
            );
        }

        return cleaned
                .substring(startIndex, endIndex + 1)
                .trim();
    }

    private void normalizeExtractionResult(
            AiTrichXuatKhachHangResult result
    ) {
        if (result == null) {
            throw new RuntimeException(
                    "Gemini không trả về kết quả phân tích."
            );
        }

        if (result.getReply() == null
                || result.getReply().isBlank()) {
            result.setReply(
                    "Anh/chị vui lòng cung cấp thêm thông tin "
                            + "để An Yên hỗ trợ chính xác hơn."
            );
        }

        if (result.getCustomerInfo() == null) {
            result.setCustomerInfo(
                    new AiTrichXuatKhachHangResult.CustomerInfo()
            );
        }

        if (result.getMissingFields() == null) {
            result.setMissingFields(
                    new java.util.ArrayList<>()
            );
        }

        if (result.getReadyForHotline() == null) {
            result.setReadyForHotline(false);
        }

        if (result.getCustomerConfirmed() == null) {
            result.setCustomerConfirmed(false);
        }
    }

    private String safeValue(Object value) {
        if (value == null) {
            return "Chưa có";
        }

        String text =
                value.toString().trim();

        return text.isEmpty()
                ? "Chưa có"
                : text;
    }

    private String firstNonBlank(
            String first,
            String second
    ) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }

        if (second != null && !second.isBlank()) {
            return second.trim();
        }

        return null;
    }

    private String getBlockedReason(
            Map<?, ?> response
    ) {
        Object promptFeedbackObject =
                response.get("promptFeedback");

        if (promptFeedbackObject
                instanceof Map<?, ?> promptFeedback) {

            Object blockReason =
                    promptFeedback.get("blockReason");

            if (blockReason != null) {
                return "Gemini đã từ chối câu hỏi. "
                        + "Lý do: "
                        + blockReason;
            }
        }

        return "Gemini không tạo được câu trả lời.";
    }

    private String getErrorMessage(
            HttpClientErrorException exception
    ) {
        String responseBody =
                exception.getResponseBodyAsString();

        if (responseBody == null
                || responseBody.isBlank()) {
            return exception.getMessage();
        }

        return responseBody;
    }
}