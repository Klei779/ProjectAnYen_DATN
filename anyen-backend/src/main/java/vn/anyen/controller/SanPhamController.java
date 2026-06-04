package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.SanPhamPageResponse;
import vn.anyen.service.SanPhamService;

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
}