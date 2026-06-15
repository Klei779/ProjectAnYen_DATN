package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.TaoHoaDonRequest;
import vn.anyen.dto.response.HoaDonResponse;
import vn.anyen.service.HoaDonService;

@RestController
@RequestMapping("/api/nhan-vien/hoa-don")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
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
}
