package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.CapNhatYeuCauTuVanAiRequest;
import vn.anyen.entity.YeuCauTuVanAi;
import vn.anyen.service.YeuCauTuVanAiService;
import jakarta.validation.Valid;
import vn.anyen.dto.request.AiPhanTichTinNhanRequest;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/yeu-cau-tu-van")
@RequiredArgsConstructor
public class YeuCauTuVanAiController {

    private final YeuCauTuVanAiService yeuCauTuVanAiService;

    @GetMapping("/theo-token/{tokenPhien}")
    public ResponseEntity<?> layTheoToken(
            @PathVariable String tokenPhien
    ) {
        try {
            YeuCauTuVanAi yeuCau =
                    yeuCauTuVanAiService
                            .layHoacTaoTheoToken(
                                    tokenPhien
                            );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("data", yeuCau);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);
            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
    @PutMapping("/theo-token/{tokenPhien}")
    public ResponseEntity<?> capNhatTheoToken(
            @PathVariable String tokenPhien,
            @RequestBody CapNhatYeuCauTuVanAiRequest request
    ) {
        try {
            YeuCauTuVanAi yeuCau =
                    yeuCauTuVanAiService
                            .capNhatThongTin(
                                    tokenPhien,
                                    request
                            );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("data", yeuCau);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);
            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    @PostMapping("/phan-tich-tin-nhan")
    public ResponseEntity<?> phanTichTinNhan(
            @Valid
            @RequestBody AiPhanTichTinNhanRequest request
    ) {
        try {
            AiTrichXuatKhachHangResult result =
                    yeuCauTuVanAiService
                            .phanTichTinNhan(
                                    request.getTokenPhien(),
                                    request.getMessage()
                            );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);
            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    @PutMapping("/xac-nhan/{tokenPhien}")
    public ResponseEntity<?> xacNhanThongTin(
            @PathVariable String tokenPhien
    ) {
        try {
            YeuCauTuVanAi yeuCau =
                    yeuCauTuVanAiService
                            .xacNhanThongTin(
                                    tokenPhien
                            );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put(
                    "message",
                    "Đã xác nhận thông tin khách hàng"
            );
            response.put("data", yeuCau);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);
            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    @PutMapping("/gui-hotline/{tokenPhien}")
    public ResponseEntity<?> guiChoHotline(
            @PathVariable String tokenPhien
    ) {
        try {
            YeuCauTuVanAi yeuCau =
                    yeuCauTuVanAiService
                            .guiChoHotline(
                                    tokenPhien
                            );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put(
                    "message",
                    "Đã gửi yêu cầu cho Hotline"
            );
            response.put("data", yeuCau);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", false);
            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
}