package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.HuyHoaDonRequest;
import vn.anyen.dto.request.TaoHoaDonRequest;
import vn.anyen.dto.response.HoaDonResponse;
import vn.anyen.service.HoaDonService;

import java.util.Map;

@RestController
@RequestMapping("/api/nhan-vien/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    @PostMapping
    public HoaDonResponse taoHoaDon(@Valid @RequestBody TaoHoaDonRequest request) {
        return hoaDonService.taoHoaDon(request);
    }

    @GetMapping("/don-hang/{maDonHang}")
    public HoaDonResponse getHoaDonByDonHang(@PathVariable Integer maDonHang) {
        return hoaDonService.getHoaDonByDonHang(maDonHang);
    }

    @PostMapping("/{maHoaDon}/yeu-cau-huy")
    public Map<String, Object> guiYeuCauHuy(
            @PathVariable Integer maHoaDon,
            @Valid @RequestBody HuyHoaDonRequest request,
            Authentication authentication
    ) {
        return hoaDonService.guiYeuCauHuy(
                maHoaDon,
                authentication.getName(),
                request
        );
    }
}
