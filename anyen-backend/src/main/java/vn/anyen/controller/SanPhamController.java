package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.SanPhamPageResponse;
import vn.anyen.service.SanPhamService;
import vn.anyen.dto.request.SanPhamRequest;
import vn.anyen.dto.SanPhamResponse;
import vn.anyen.dto.SanPhamFilterResponse;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SanPhamController {

    private final SanPhamService sanPhamService;

    @GetMapping
    public SanPhamPageResponse getSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loai,
            @RequestParam(required = false) String vatLieu,
            @RequestParam(required = false) String tonGiao,
            @RequestParam(required = false) String mauSac,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "16") Integer pageSize
    ) {
        return sanPhamService.getSanPham(
                keyword,
                loai,
                vatLieu,
                tonGiao,
                mauSac,
                minPrice,
                maxPrice,
                sortBy,
                page,
                pageSize
        );
    }
    @GetMapping("/bo-loc")
    public SanPhamFilterResponse getBoLocSanPham() {
        return sanPhamService.getBoLocSanPham();
    }
    @PutMapping("/{id}")
    public SanPhamResponse updateSanPham(
            @PathVariable Integer id,
            @RequestBody SanPhamRequest request
    ) {
        return sanPhamService.updateSanPham(id, request);
    }

    @PatchMapping("/{id}/an")
    public SanPhamResponse anSanPham(@PathVariable Integer id) {
        return sanPhamService.anSanPham(id);
    }

    @GetMapping("/cho-duyet")
    public SanPhamPageResponse getSanPhamChoDuyet(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "16") Integer pageSize
    ) {
        return sanPhamService.getSanPhamChoDuyet(page, pageSize);
    }

    @PatchMapping("/{id}/duyet")
    public SanPhamResponse duyetSanPham(@PathVariable Integer id) {
        return sanPhamService.duyetSanPham(id);
    }
}