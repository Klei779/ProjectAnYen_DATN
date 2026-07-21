package vn.anyen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final RestClient restClient;

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.timeout-seconds:180}")
    private int timeoutSeconds;

    @Value("${ollama.keep-alive:10m}")
    private String keepAlive;

    @Value("${ollama.num-context:4096}")
    private int numContext;

    @Value("${ollama.num-predict:1000}")
    private int numPredict;

    @Value("${ollama.temperature:0.1}")
    private double temperature;

    @Value("${ollama.think:false}")
    private boolean think;

    public OllamaService() {
        /*
         * Timeout được đặt lại trong từng request factory.
         * Giá trị mặc định ban đầu là 180 giây.
         */
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(180_000);

        this.restClient = RestClient
                .builder()
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Gọi Ollama để chat thông thường.
     */
    public String chat(
            String systemInstruction,
            String userPrompt
    ) {
        validateConfiguration();
        validatePrompt(userPrompt);

        String safeSystemInstruction =
                systemInstruction == null
                        || systemInstruction.isBlank()
                        ? """
                          Bạn là trợ lý tư vấn của Dịch vụ Mai táng An Yên.
                          Luôn trả lời bằng tiếng Việt, lịch sự và ngắn gọn.
                          """
                        : systemInstruction.trim();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", safeSystemInstruction
                        ),
                        Map.of(
                                "role", "user",
                                "content", userPrompt.trim()
                        )
                ),
                "stream", false,
                "think", think,
                "keep_alive", keepAlive,
                "options", Map.of(
                        "temperature", temperature,
                        "num_ctx", numContext,
                        "num_predict", numPredict,
                        "top_k", 10,
                        "top_p", 0.8,
                        "repeat_penalty", 1.05
                )
        );

        return callChatApi(requestBody);
    }

    /**
     * Gọi Ollama và yêu cầu kết quả ở dạng JSON.
     */
    public String chatJson(
            String systemInstruction,
            String userPrompt
    ) {
        validateConfiguration();
        validatePrompt(userPrompt);

        String safeSystemInstruction =
                systemInstruction == null
                        || systemInstruction.isBlank()
                        ? """
                      Bạn là trợ lý tư vấn của Dịch vụ Mai táng An Yên.

                      Đây là dịch vụ hỗ trợ hợp pháp về tổ chức tang lễ,
                      an táng, hỏa táng và sản phẩm tang lễ.

                      Hãy trả lời lịch sự bằng tiếng Việt.
                      Không từ chối câu hỏi chỉ vì có nội dung liên quan
                      đến tang lễ, an táng, quan tài hoặc hỏa táng.

                      Chỉ trả về JSON đúng cấu trúc được yêu cầu.
                      Không dùng markdown.
                      Không viết nội dung ngoài JSON.
                      """
                        : systemInstruction.trim();

        Map<String, Object> jsonSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "reply", Map.of(
                                "type", "string"
                        ),
                        "nhuCau", Map.of(
                                "type", List.of("string", "null")
                        )
                ),
                "required", List.of(
                        "reply",
                        "nhuCau"
                ),
                "additionalProperties", false
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,

                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", safeSystemInstruction
                        ),
                        Map.of(
                                "role", "user",
                                "content", userPrompt.trim()
                        )
                ),

                "format", jsonSchema,
                "stream", false,
                "think", false,
                "keep_alive", keepAlive,

                "options", Map.of(
                        "temperature", 0.0,
                        "num_ctx", numContext,
                        "num_predict", 300,
                        "top_k", 10,
                        "top_p", 0.8,
                        "repeat_penalty", 1.05
                )
        );

        return callChatApi(requestBody);
    }

    /**
     * Kiểm tra Ollama API có đang hoạt động hay không.
     */
    public boolean isAvailable() {
        try {
            validateConfiguration();

            Map<?, ?> response = restClient
                    .get()
                    .uri(normalizeBaseUrl() + "/api/tags")
                    .retrieve()
                    .body(Map.class);

            return response != null
                    && response.get("models") instanceof List<?>;

        } catch (Exception e) {
            System.err.println(
                    "Ollama không khả dụng: " + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Trả tên model đang cấu hình.
     */
    public String getModel() {
        return model;
    }

    /**
     * Gọi endpoint /api/chat.
     */
    private String callChatApi(
            Map<String, Object> requestBody
    ) {
        String requestUrl =
                normalizeBaseUrl() + "/api/chat";

        try {
            Map<?, ?> response = restClient
                    .post()
                    .uri(requestUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractChatContent(response);

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException(
                    "Không tìm thấy model Ollama: "
                            + model
                            + ". Hãy chạy: ollama pull "
                            + model,
                    e
            );

        } catch (HttpClientErrorException e) {
            throw new RuntimeException(
                    "Ollama API trả về lỗi HTTP "
                            + e.getStatusCode().value()
                            + ": "
                            + getErrorMessage(e),
                    e
            );

        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Không thể kết nối đến Ollama tại "
                            + normalizeBaseUrl()
                            + ". Hãy kiểm tra Ollama đang chạy.",
                    e
            );
        }
    }

    /**
     * Đọc message.content từ kết quả /api/chat.
     */
    private String extractChatContent(
            Map<?, ?> response
    ) {
        if (response == null) {
            throw new RuntimeException(
                    "Ollama không trả về dữ liệu."
            );
        }

        Object errorObject = response.get("error");

        if (errorObject != null) {
            throw new RuntimeException(
                    "Ollama trả về lỗi: " + errorObject
            );
        }

        Object messageObject = response.get("message");

        if (!(messageObject instanceof Map<?, ?> message)) {
            throw new RuntimeException(
                    "Phản hồi Ollama không có trường message."
            );
        }

        Object contentObject = message.get("content");

        if (contentObject == null) {
            throw new RuntimeException(
                    "Phản hồi Ollama không có nội dung."
            );
        }

        String content =
                contentObject.toString().trim();

        if (content.isBlank()) {
            throw new RuntimeException(
                    "Ollama trả về nội dung trống."
            );
        }

        return content;
    }

    private String normalizeBaseUrl() {
        String normalized = baseUrl.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }

    private void validateConfiguration() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình ollama.base-url."
            );
        }

        if (model == null || model.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình ollama.model."
            );
        }

        if (timeoutSeconds <= 0) {
            throw new IllegalStateException(
                    "ollama.timeout-seconds phải lớn hơn 0."
            );
        }

        if (numContext <= 0) {
            throw new IllegalStateException(
                    "ollama.num-context phải lớn hơn 0."
            );
        }

        if (numPredict <= 0) {
            throw new IllegalStateException(
                    "ollama.num-predict phải lớn hơn 0."
            );
        }
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Nội dung gửi Ollama không được để trống."
            );
        }

        if (prompt.length() > 30_000) {
            throw new IllegalArgumentException(
                    "Nội dung gửi Ollama quá dài."
            );
        }
    }

    private String normalizeNhuCau(
            String tinNhan,
            String aiNhuCau
    ) {
        if (tinNhan == null) {
            return aiNhuCau;
        }

        String text = tinNhan.toLowerCase();

        boolean coQuanTai =
                text.contains("quan tài")
                        || text.contains("hòm")
                        || text.contains("áo quan");

        boolean coAnTang =
                text.contains("an táng")
                        || text.contains("chôn cất");

        if (coQuanTai && coAnTang) {
            return "Tư vấn quan tài để an táng";
        }

        if (coQuanTai) {
            return "Tư vấn quan tài";
        }

        if (text.contains("hỏa táng")) {
            return "Dịch vụ hỏa táng";
        }

        if (coAnTang) {
            return "Dịch vụ an táng";
        }

        if (text.contains("tang lễ")
                || text.contains("đám tang")) {
            return "Tổ chức tang lễ";
        }

        return aiNhuCau;
    }

    private String getErrorMessage(
            HttpClientErrorException exception
    ) {
        String body =
                exception.getResponseBodyAsString();

        if (body == null || body.isBlank()) {
            return exception.getMessage();
        }

        return body;
    }

    public String chatCustomerExtractionJson(
            String systemInstruction,
            String userPrompt
    ) {
        validateConfiguration();
        validatePrompt(userPrompt);

        Map<String, Object> nullableString = Map.of(
                "type", List.of("string", "null")
        );

        Map<String, Object> nullableNumber = Map.of(
                "type", List.of("number", "null")
        );

        Map<String, Object> customerInfoSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "hoTen", nullableString,
                        "soDienThoai", nullableString,
                        "diaChi", nullableString,
                        "nhuCau", nullableString,
                        "thoiGianMongMuon", nullableString,
                        "nganSachDuKien", nullableNumber,
                        "ghiChu", nullableString
                ),
                "required", List.of(
                        "hoTen",
                        "soDienThoai",
                        "diaChi",
                        "nhuCau",
                        "thoiGianMongMuon",
                        "nganSachDuKien",
                        "ghiChu"
                ),
                "additionalProperties", false
        );

        Map<String, Object> jsonSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "reply", Map.of(
                                "type", "string"
                        ),
                        "customerInfo", customerInfoSchema,
                        "missingFields", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "string",
                                        "enum", List.of(
                                                "hoTen",
                                                "soDienThoai",
                                                "diaChi",
                                                "nhuCau"
                                        )
                                )
                        ),
                        "readyForHotline", Map.of(
                                "type", "boolean"
                        ),
                        "customerConfirmed", Map.of(
                                "type", "boolean"
                        )
                ),
                "required", List.of(
                        "reply",
                        "customerInfo",
                        "missingFields",
                        "readyForHotline",
                        "customerConfirmed"
                ),
                "additionalProperties", false
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", systemInstruction.trim()
                        ),
                        Map.of(
                                "role", "user",
                                "content", userPrompt.trim()
                        )
                ),
                "format", jsonSchema,
                "stream", false,
                "think", false,
                "keep_alive", keepAlive,
                "options", Map.of(
                        "temperature", 0.0,
                        "num_ctx", numContext,
                        "num_predict", numPredict,
                        "top_k", 10,
                        "top_p", 0.8,
                        "repeat_penalty", 1.05
                )
        );

        return callChatApi(requestBody);
    }
}