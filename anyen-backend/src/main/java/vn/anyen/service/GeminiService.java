package vn.anyen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.model}")
    private String model;

    public GeminiService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Gửi câu hỏi đến Gemini.
     *
     * Hiện tại companyContext có thể là chuỗi thông tin mẫu.
     * Ở bước sau, dữ liệu này sẽ được lấy từ database:
     * - sản phẩm
     * - số lượng
     * - giá bán
     * - điều khoản
     * - chính sách
     */
    public String chat(
            String userMessage,
            String companyContext
    ) {
        validateConfiguration();

        String requestUrl = baseUrl
                + "/v1beta/models/"
                + model
                + ":generateContent";

        String system_instruction = """
                Bạn là trợ lý tư vấn trực tuyến của
                Dịch vụ Mai táng An Yên.

                Quy tắc bắt buộc:

                1. Luôn trả lời bằng tiếng Việt.
                2. Giọng điệu lịch sự, nhẹ nhàng và tôn trọng.
                3. Chỉ trả lời dựa trên dữ liệu An Yên được cung cấp.
                4. Không tự bịa tên sản phẩm, giá bán, số lượng,
                   dịch vụ, điều khoản hoặc chính sách.
                5. Nếu dữ liệu không có câu trả lời, hãy nói:
                   "Hiện tại tôi chưa có đủ thông tin để trả lời
                   chính xác. Vui lòng liên hệ nhân viên An Yên
                   để được hỗ trợ."
                6. Không yêu cầu khách hàng cung cấp mật khẩu,
                   mã OTP hoặc thông tin ngân hàng.
                7. Không khẳng định sản phẩm còn hàng nếu số lượng
                   bằng 0 hoặc dữ liệu không có số lượng.
                8. Trả lời ngắn gọn, rõ ràng, dễ hiểu.
                9. Không trả lời quá 250 từ, trừ khi khách hàng
                   yêu cầu giải thích chi tiết.
                """;

        String safeContext = companyContext == null
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

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(
                                Map.of(
                                        "text",
                                        system_instruction
                                )
                        )
                ),

                "contents", List.of(
                        Map.of(
                                "role",
                                "user",

                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                completePrompt
                                        )
                                )
                        )
                ),

                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 600
                )
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
            throw new RuntimeException(
                    "Gemini đã hết hạn mức tạm thời. "
                            + "Vui lòng thử lại sau."
            );

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
                            + ". Hãy kiểm tra cấu hình "
                            + "gemini.model trong application.yaml."
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
                            + "Hãy kiểm tra Internet và cấu hình API."
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

    private String extractAnswer(Map<?, ?> response) {
        if (response == null) {
            throw new RuntimeException(
                    "Gemini không trả về dữ liệu."
            );
        }

        Object candidatesObject = response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)
                || candidates.isEmpty()) {
            throw new RuntimeException(
                    getBlockedReason(response)
            );
        }

        Object firstCandidateObject = candidates.get(0);

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

        Object partsObject = content.get("parts");

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

            Object textObject = part.get("text");

            if (textObject != null) {
                answerBuilder
                        .append(textObject)
                        .append("\n");
            }
        }

        String answer =
                answerBuilder.toString().trim();

        if (answer.isBlank()) {
            throw new RuntimeException(
                    "Gemini không tạo được câu trả lời."
            );
        }

        return answer;
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