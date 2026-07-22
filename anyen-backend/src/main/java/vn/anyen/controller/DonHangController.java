package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.service.DonHangService;

import java.util.List;

@RestController
@RequestMapping("/api/don-hang")
@RequiredArgsConstructor
public class DonHangController {

    private final DonHangService donHangService;

    @GetMapping
    public List<DonHangResponse> getAllDonHang() {
        return donHangService.getAllDonHang();
    }

    @GetMapping("/{maDonHang}")
    public ResponseEntity<DonHangResponse> getDonHangById(@PathVariable Integer maDonHang) {
        return ResponseEntity.ok(donHangService.getDonHangById(maDonHang));
    }

    @PutMapping("/{maDonHang}/trang-thai")
    public ResponseEntity<DonHangResponse> capNhatTrangThai(
            @PathVariable Integer maDonHang,
            @RequestBody CapNhatTrangThaiDonHangRequest request) {
        DonHangResponse response = donHangService.capNhatTrangThai(maDonHang, request.getTrangThai());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{maDonHang}/huy")
    public ResponseEntity<DonHangResponse> huyDonHang(@PathVariable Integer maDonHang) {
        DonHangResponse response = donHangService.huyDonHang(maDonHang);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{maDonHang}/gui-doi-tac")
    public ResponseEntity<DonHangResponse> guiDonChoDoiTac(@PathVariable Integer maDonHang) {
        DonHangResponse response = donHangService.guiDonChoDoiTac(maDonHang);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{maDonHang}/tao-hop-dong")
    public ResponseEntity<DonHangResponse> taoHopDong(@PathVariable Integer maDonHang) {
        DonHangResponse response = donHangService.taoHopDong(maDonHang);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{maDonHang}/doi-tac-bao-da-giao")
    public ResponseEntity<DonHangResponse> doiTacBaoDaGiao(@PathVariable Integer maDonHang) {
        DonHangResponse response = donHangService.doiTacBaoDaGiao(maDonHang);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{maDonHang}/thanh-toan")
    public ResponseEntity<DonHangResponse> thanhToanDonHang(@PathVariable Integer maDonHang) {
        DonHangResponse response = donHangService.thanhToanDonHang(maDonHang);
        return ResponseEntity.ok(response);
    }
}
