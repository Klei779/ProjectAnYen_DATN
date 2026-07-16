package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.GiaoCongViecRequest;
import vn.anyen.dto.response.GiaoCongViecResponse;
import vn.anyen.dto.response.NhanVienGanNhatResponse;
import vn.anyen.service.HotlineCongViecService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping({"/api/nhan-vien/hotline/cong-viec", "/api/nhan-vien/truc-tuyen/cong-viec"})
@RequiredArgsConstructor
public class HotlineCongViecController {

    private final HotlineCongViecService hotlineCongViecService;

    @GetMapping("/nhan-vien-truc-tiep")
    public List<NhanVienGanNhatResponse> getNhanVienTrucTiep(
            Authentication authentication,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude
    ) {
        return hotlineCongViecService.getNhanVienTrucTiep(
                authentication,
                latitude,
                longitude
        );
    }

    @PostMapping
    public GiaoCongViecResponse giaoCongViec(
            Authentication authentication,
            @Valid @RequestBody GiaoCongViecRequest request
    ) {
        return hotlineCongViecService.giaoCongViec(authentication, request);
    }
}
