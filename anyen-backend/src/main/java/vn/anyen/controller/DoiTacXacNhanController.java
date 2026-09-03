package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.anyen.entity.DoiTac;
import vn.anyen.service.QuanLyDoiTacService;

@RestController
@RequestMapping("/api/auth/doi-tac")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DoiTacXacNhanController {

    private final QuanLyDoiTacService quanLyDoiTacService;
    private final TemplateEngine templateEngine;

    /**
     * API Public: Xác nhận hợp tác từ link email (Bước 1)
     */
    @GetMapping("/xac-nhan")
    public ResponseEntity<?> xacNhanDoiTac(@RequestParam("token") String token) {
        try {
            DoiTac doiTac = quanLyDoiTacService.xacNhanDoiTac(token);

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("tenDoiTac", doiTac.getTenDoiTac());
            response.put("tenDoanhNghiep", doiTac.getTenDoanhNghiep());
            response.put("maDoiTac", "AY" + String.format("%05d", doiTac.getMaDoiTac()));
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /**
     * API Public: Ký hợp đồng và thiết lập tài khoản (Bước 2)
     */
    @PostMapping("/ky-hop-dong")
    public ResponseEntity<?> kyHopDong(@jakarta.validation.Valid @RequestBody vn.anyen.dto.request.KyHopDongRequest request) {
        quanLyDoiTacService.kyHopDong(request);
        return ResponseEntity.ok(java.util.Map.of("message", "Ký hợp đồng và tạo tài khoản thành công!"));
    }
}
