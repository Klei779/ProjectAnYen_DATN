package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.dto.response.DonHangResponse;
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

    private DoiTac getDoiTacFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        return doiTacRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác đăng nhập"));
    }

    @PostMapping("/{maDonHang}/chap-nhan")
    public DonHangResponse chapNhanDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang
    ) {
        DoiTac doiTac = getDoiTacFromAuth(authentication);
        return donHangService.doiTacChapNhanDonHang(maDonHang, doiTac.getMaDoiTac());
    }

    @PostMapping("/{maDonHang}/tu-choi")
    public DonHangResponse tuChoiDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang
    ) {
        DoiTac doiTac = getDoiTacFromAuth(authentication);
        return donHangService.doiTacTuChoiDonHang(maDonHang, doiTac.getMaDoiTac());
    }

    @PostMapping("/{maDonHang}/xu-ly")
    public DonHangResponse xuLyDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang,
            @RequestParam String thoiGianUocTinh
    ) {
        DoiTac doiTac = getDoiTacFromAuth(authentication);
        return donHangService.doiTacXuLyDonHang(maDonHang, doiTac.getMaDoiTac(), thoiGianUocTinh);
    }

    @PostMapping("/{maDonHang}/da-giao")
    public DonHangResponse daGiaoDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang
    ) {
        DoiTac doiTac = getDoiTacFromAuth(authentication);
        return donHangService.doiTacDaGiaoDonHang(maDonHang, doiTac.getMaDoiTac());
    }

    @PostMapping("/{maDonHang}/huy")
    public DonHangResponse huyDonHang(
            Authentication authentication,
            @PathVariable Integer maDonHang,
            @RequestParam String lyDo
    ) {
        DoiTac doiTac = getDoiTacFromAuth(authentication);
        return donHangService.doiTacHuyDonHang(maDonHang, doiTac.getMaDoiTac(), lyDo);
    }

    @PostMapping("/{maDonHang}/bao-cao-su-co")
    public org.springframework.http.ResponseEntity<?> baoCaoSuCo(
            Authentication authentication,
            @PathVariable Integer maDonHang,
            @RequestParam String lyDo
    ) {
        DoiTac doiTac = getDoiTacFromAuth(authentication);
        donHangService.doiTacBaoCaoSuCo(maDonHang, doiTac.getMaDoiTac(), lyDo);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Đã gửi thông báo sự cố cho nhân viên."));
    }
}