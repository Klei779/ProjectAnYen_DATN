package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.service.DonHangService;
import vn.anyen.dto.response.SanPhamTaoDonHangResponse;
import vn.anyen.service.SanPhamService;


import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/don-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NhanVienDonHangController {

    private final DonHangService donHangService;
    private final SanPhamService sanPhamService;

    @PostMapping
    public ResponseEntity<DonHangResponse> taoDonHang(
            @Valid @RequestBody TaoDonHangRequest request,
            Authentication authentication
    ) {
        DonHangResponse response = donHangService.taoDonHang(
                request,
                authentication
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{maDonHang}/trang-thai")
    public ResponseEntity<DonHangResponse> capNhatTrangThaiDonHang(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody CapNhatTrangThaiDonHangRequest request
    ) {
        DonHangResponse response = donHangService.capNhatTrangThaiNhanVien(
                maDonHang,
                request.getTrangThai()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/san-pham-options")
    public ResponseEntity<List<SanPhamTaoDonHangResponse>> getSanPhamTaoDonHangOptions() {
        return ResponseEntity.ok(
                sanPhamService.getSanPhamTaoDonHangOptions()
        );
    }
}