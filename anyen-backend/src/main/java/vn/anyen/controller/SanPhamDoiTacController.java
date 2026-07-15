package vn.anyen.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.SanPhamDoiTacPageResponse;
import vn.anyen.dto.SanPhamDoiTacResponse;
import vn.anyen.dto.request.SanPhamDoiTacRequest;
import vn.anyen.service.SanPhamDoiTacService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/doi-tac/san-pham")
@RequiredArgsConstructor
public class SanPhamDoiTacController {

    private final SanPhamDoiTacService sanPhamDoiTacService;

    @GetMapping
    public SanPhamDoiTacPageResponse getSanPhamDoiTac(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loai,
            @RequestParam(required = false) String vatLieu,
            @RequestParam(required = false) String tonGiao,
            @RequestParam(required = false) String mauSac,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int pageSize
    ) {
        return sanPhamDoiTacService.getSanPhamDoiTac(
                authentication,
                keyword,
                loai,
                vatLieu,
                tonGiao,
                mauSac,
                trangThai,
                minPrice,
                maxPrice,
                sortBy,
                page,
                pageSize
        );
    }

    @GetMapping("/{id}")
    public SanPhamDoiTacResponse getChiTietSanPham(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        return sanPhamDoiTacService.getChiTietSanPham(authentication, id);
    }

    @PostMapping
    public SanPhamDoiTacResponse createSanPham(
            Authentication authentication,
            @RequestBody SanPhamDoiTacRequest request
    ) {
        return sanPhamDoiTacService.createSanPham(authentication, request);
    }

    @PutMapping("/{id}")
    public SanPhamDoiTacResponse updateSanPham(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody SanPhamDoiTacRequest request
    ) {
        return sanPhamDoiTacService.updateSanPham(authentication, id, request);
    }

    @PatchMapping("/{id}/ton-kho")
    public SanPhamDoiTacResponse updateTonKho(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody CapNhatTonKhoRequest request
    ) {
        return sanPhamDoiTacService.updateTonKho(authentication, id, request.getSoLuong());
    }

    @PatchMapping("/{id}/an")
    public SanPhamDoiTacResponse anSanPham(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        return sanPhamDoiTacService.anSanPham(authentication, id);
    }

    @PatchMapping("/{id}/hien")
    public SanPhamDoiTacResponse hienSanPham(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        return sanPhamDoiTacService.hienSanPham(authentication, id);
    }

    @Data
    public static class CapNhatTonKhoRequest {
        private Integer soLuong;
    }
}
