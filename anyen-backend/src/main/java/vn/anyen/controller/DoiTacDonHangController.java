package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.service.DonHangService;

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
}