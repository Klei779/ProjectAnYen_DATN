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
public class DoiTacXacNhanController {

    private final QuanLyDoiTacService quanLyDoiTacService;
    private final TemplateEngine templateEngine;

    /**
     * API Public: Xác nhận hợp tác từ link email (Bước 1)
     */
    @GetMapping(value = "/xac-nhan", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> xacNhanDoiTac(@RequestParam("token") String token) {
        try {
            DoiTac doiTac = quanLyDoiTacService.xacNhanDoiTac(token);

            Context context = new Context();
            context.setVariable("tenDoiTac", doiTac.getTenDoiTac());
            context.setVariable("tenDoanhNghiep", doiTac.getTenDoanhNghiep());
            context.setVariable("maDoiTac", "AY" + String.format("%05d", doiTac.getMaDoiTac()));
            context.setVariable("token", token);
            String html = templateEngine.process("xac-nhan-thanh-cong", context);

            return ResponseEntity.ok(html);

        } catch (Exception e) {
            Context context = new Context();
            context.setVariable("errorMessage", e.getMessage());
            String html = templateEngine.process("xac-nhan-that-bai", context);

            return ResponseEntity.ok(html);
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
