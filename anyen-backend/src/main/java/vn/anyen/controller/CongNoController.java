package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.request.SoTienRequest;
import vn.anyen.dto.response.CongNoResponse;
import vn.anyen.dto.response.CongNoTongQuanResponse;
import vn.anyen.dto.response.LichSuCongNoResponse;
import vn.anyen.dto.response.PayooMockResponse;
import vn.anyen.service.CongNoService;
import vn.anyen.service.PayooMockService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/congno")
@RequiredArgsConstructor
public class CongNoController {

    private final CongNoService congNoService;
    private final PayooMockService payooMockService;

    // ================================
    // DANH SÁCH CÔNG NỢ
    // ================================

    @GetMapping
    public ResponseEntity<Page<CongNoResponse>> getAllCongNo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer trangThai
    ) {

        return ResponseEntity.ok(
                congNoService.getDanhSach(
                        page,
                        size,
                        trangThai
                )
        );
    }

    // ================================
    // TỔNG QUAN
    // ================================

    @GetMapping("/tong-quan")
    public ResponseEntity<CongNoTongQuanResponse> getTongQuan() {

        return ResponseEntity.ok(
                congNoService.getTongQuan()
        );
    }

    // ================================
    // LỊCH SỬ THANH TOÁN
    // ================================

    @GetMapping("/lich-su")
    public ResponseEntity<Page<LichSuCongNoResponse>> getLichSu(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                congNoService.getLichSu(page, size)
        );
    }

    // ================================
    // CÔNG NỢ THEO ĐỐI TÁC
    // ================================

    @GetMapping("/doi-tac/{maDoiTac}")
    public ResponseEntity<List<CongNoResponse>> getByDoiTac(
            @PathVariable Integer maDoiTac
    ) {

        return ResponseEntity.ok(
                congNoService.getByDoiTac(maDoiTac)
        );
    }

    // ================================
    // CÔNG NỢ THEO ĐƠN HÀNG
    // ================================

    @GetMapping("/don-hang/{maDonHang}")
    public ResponseEntity<List<CongNoResponse>> getByDonHang(
            @PathVariable Integer maDonHang
    ) {

        return ResponseEntity.ok(
                congNoService.getByDonHang(maDonHang)
        );
    }

    // ================================
    // TẠO CÔNG NỢ TỪ ĐƠN
    // ================================

    @PostMapping("/tao-tu-don-hang/{maDonHang}")
    public ResponseEntity<List<CongNoResponse>> taoTuDonHang(
            @PathVariable Integer maDonHang
    ) {

        congNoService.taoCongNoTuDonHang(maDonHang);

        return ResponseEntity.ok(
                congNoService.getByDonHang(maDonHang)
        );
    }

    // ================================
    // THANH TOÁN PAYOO
    // ================================

    @PostMapping("/{maCongNo}/payoo")
    public PayooMockResponse thanhToanPayoo(
            @PathVariable Integer maCongNo,
            @Valid @RequestBody SoTienRequest request
    ) {

        return payooMockService.taoThanhToanCongNo(
                maCongNo,
                request.getSoTien()
        );
    }
}