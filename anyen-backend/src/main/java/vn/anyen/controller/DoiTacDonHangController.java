package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.BaoCaoSuCoRequest;
import vn.anyen.dto.request.HuyDonHangRequest;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.service.DonHangService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/doi-tac/quan-ly-don-hang")
@RequiredArgsConstructor
public class DoiTacDonHangController {

    private final DonHangService donHangService;
    private final DoiTacRepository doiTacRepository;

    @GetMapping
    public DoiTacDonHangPageResponse getDoiTacDonHangs(
            Authentication authentication,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "Tất cả") String trangThai,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        DoiTac doiTac = doiTacRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác đăng nhập"));

        return donHangService.getDoiTacDonHangs(
                doiTac.getMaDoiTac(),
                keyword,
                trangThai,
                page,
                pageSize
        );
    }

    @GetMapping("/{maDonHang}")
    public DoiTacDonHangResponse getDoiTacDonHangDetail(
            Authentication authentication,
            @PathVariable Integer maDonHang
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        DoiTac doiTac = doiTacRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác đăng nhập"));

        return donHangService.getDoiTacDonHangDetail(
                maDonHang,
                doiTac.getMaDoiTac()
        );
    }

    @PutMapping("/{maDonHang}/xu-ly")
    public void xuLyDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang,
            @RequestBody Map<String, String> body
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        DoiTac doiTac = doiTacRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác đăng nhập"));

        String ngayGiaoDuKienStr = body.get("ngayGiaoDuKien");
        if (ngayGiaoDuKienStr == null || ngayGiaoDuKienStr.trim().isEmpty()) {
            throw new RuntimeException("Ngày giao dự kiến không được để trống");
        }

        LocalDate ngayGiaoDuKien = LocalDate.parse(ngayGiaoDuKienStr);

        donHangService.xuLyDonHangDoiTac(
                maDonHang,
                doiTac.getMaDoiTac(),
                ngayGiaoDuKien
        );
    }

    @PostMapping("/{maDonHang}/bao-cao-su-co")
    public void baoCaoSuCo(
            Authentication authentication,
            @PathVariable Integer maDonHang,
            @Valid @RequestBody BaoCaoSuCoRequest request
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        donHangService.baoCaoSuCo(maDonHang, request.getLyDoSuCo(), authentication);
    }

    @PostMapping("/{maDonHang}/giai-quyet-su-co")
    public void giaiQuyetSuCo(
            Authentication authentication,
            @PathVariable Integer maDonHang
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        donHangService.giaiQuyetSuCo(maDonHang, authentication);
    }

    @PutMapping("/{maDonHang}/huy")
    public void huyDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang,
            @Valid @RequestBody HuyDonHangRequest request
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        donHangService.huyDonHang(maDonHang, request, authentication);
    }
}