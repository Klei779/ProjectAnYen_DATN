package vn.anyen.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.AiPhanTichTinNhanRequest;
import vn.anyen.dto.request.CapNhatYeuCauTuVanAiRequest;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.YeuCauTuVanAi;
import vn.anyen.service.GuestChatAuthService;
import vn.anyen.service.YeuCauTuVanAiService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/yeu-cau-tu-van")
public class YeuCauTuVanAiController {

    private final YeuCauTuVanAiService yeuCauTuVanAiService;
    private final GuestChatAuthService guestChatAuthService;

    public YeuCauTuVanAiController(
            YeuCauTuVanAiService yeuCauTuVanAiService,
            GuestChatAuthService guestChatAuthService
    ) {
        this.yeuCauTuVanAiService = yeuCauTuVanAiService;
        this.guestChatAuthService = guestChatAuthService;
    }

    @GetMapping("/theo-token/{tokenPhien}")
    public ResponseEntity<?> layTheoToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String tokenPhien
    ) {
        guestChatAuthService.requireSession(authorization, tokenPhien);

        try {
            YeuCauTuVanAi yeuCau = yeuCauTuVanAiService
                    .layHoacTaoTheoToken(tokenPhien);
            return ok(yeuCau);
        } catch (RuntimeException e) {
            return badRequest(e);
        }
    }

    @PutMapping("/theo-token/{tokenPhien}")
    public ResponseEntity<?> capNhatTheoToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String tokenPhien,
            @RequestBody CapNhatYeuCauTuVanAiRequest request
    ) {
        guestChatAuthService.requireSession(authorization, tokenPhien);

        try {
            YeuCauTuVanAi yeuCau = yeuCauTuVanAiService
                    .capNhatThongTin(tokenPhien, request);
            return ok(yeuCau);
        } catch (RuntimeException e) {
            return badRequest(e);
        }
    }

    @PostMapping("/phan-tich-tin-nhan")
    public ResponseEntity<?> phanTichTinNhan(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AiPhanTichTinNhanRequest request
    ) {
        guestChatAuthService.requireSession(
                authorization,
                request.getTokenPhien()
        );

        try {
            AiTrichXuatKhachHangResult result = yeuCauTuVanAiService
                    .phanTichTinNhan(
                            request.getTokenPhien(),
                            request.getMessage()
                    );
            return ok(result);
        } catch (RuntimeException e) {
            return badRequest(e);
        }
    }

    @PutMapping("/xac-nhan/{tokenPhien}")
    public ResponseEntity<?> xacNhanThongTin(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String tokenPhien
    ) {
        guestChatAuthService.requireSession(authorization, tokenPhien);

        try {
            YeuCauTuVanAi yeuCau = yeuCauTuVanAiService
                    .xacNhanThongTin(tokenPhien);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xác nhận thông tin khách hàng");
            response.put("data", yeuCau);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return badRequest(e);
        }
    }

    @PutMapping("/gui-hotline/{tokenPhien}")
    public ResponseEntity<?> guiChoHotline(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String tokenPhien
    ) {
        guestChatAuthService.requireSession(authorization, tokenPhien);

        try {
            YeuCauTuVanAi yeuCau = yeuCauTuVanAiService
                    .guiChoHotline(tokenPhien);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã gửi yêu cầu cho Hotline");
            response.put("data", yeuCau);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return badRequest(e);
        }
    }

    private ResponseEntity<?> ok(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> badRequest(RuntimeException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
