package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.request.HuyDonHangRequest;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.dto.response.SanPhamTaoDonHangResponse;
import vn.anyen.service.DonHangService;
import vn.anyen.service.SanPhamService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nhan-vien/don-hang")
@RequiredArgsConstructor
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

    @GetMapping("/{maDonHang}/co-hop-dong")
    public ResponseEntity<Map<String, Boolean>> kiemTraDonHangCoHopDong(
            @PathVariable Integer maDonHang
    ) {
        boolean daCoHopDong = donHangService.kiemTraDonHangDaCoHopDong(maDonHang);

        return ResponseEntity.ok(Map.of("daCoHopDong", daCoHopDong));
    }

    @PutMapping("/{maDonHang}")
    public ResponseEntity<DonHangResponse> capNhatDonHang(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody TaoDonHangRequest request
    ) {
        return ResponseEntity.ok(
                donHangService.capNhatDonHang(maDonHang, request)
        );
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

    @PutMapping("/{maDonHang}/huy")
    public DonHangResponse huyDonHang(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody HuyDonHangRequest request
    ) {
        return donHangService.huyDonHang(maDonHang, request);
    }

    @PutMapping("/{maDonHang}/gui-doi-tac")
    public ResponseEntity<DonHangResponse> guiDoiTac(@PathVariable Integer maDonHang) {
        return ResponseEntity.ok(donHangService.guiDoiTac(maDonHang));
    }

    @GetMapping("/san-pham-options")
    public ResponseEntity<List<SanPhamTaoDonHangResponse>> getSanPhamTaoDonHangOptions() {
        return ResponseEntity.ok(
                sanPhamService.getSanPhamTaoDonHangOptions()
        );
    }
}