package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.service.DonHangService;

@RestController
@RequestMapping("/api/nhan-vien/don-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NhanVienDonHangController {

    private final DonHangService donHangService;

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
}