package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.SanPhamDoiTacPageResponse;
import vn.anyen.dto.SanPhamDoiTacResponse;
import vn.anyen.dto.request.SanPhamRequest;
import vn.anyen.service.SanPhamDoiTacService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/doi-tac/san-pham")
@RequiredArgsConstructor
public class SanPhamDoiTacController {

    private final SanPhamDoiTacService sanPhamDoiTacService;

    @GetMapping
    public ResponseEntity<SanPhamDoiTacPageResponse> getSanPhamDoiTac(
            Authentication authentication,

            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loai,
            @RequestParam(required = false) String vatLieu,
            @RequestParam(required = false) String tonGiao,
            @RequestParam(required = false) String mauSac,

            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,

            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int pageSize
    ) {
        return ResponseEntity.ok(
                sanPhamDoiTacService.getSanPhamDoiTac(
                        authentication,
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
                )
        );
    }

    @PostMapping
    public ResponseEntity<SanPhamDoiTacResponse> taoSanPham(
            Authentication authentication,
            @RequestBody SanPhamRequest request
    ) {
        return ResponseEntity.ok(
                sanPhamDoiTacService.taoSanPham(authentication, request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamDoiTacResponse> capNhatSanPham(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody SanPhamRequest request
    ) {
        return ResponseEntity.ok(
                sanPhamDoiTacService.capNhatSanPham(authentication, id, request)
        );
    }

    @PutMapping("/{id}/ton-kho")
    public ResponseEntity<SanPhamDoiTacResponse> capNhatTonKho(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body
    ) {
        Integer soLuong = body.get("soLuong");

        return ResponseEntity.ok(
                sanPhamDoiTacService.capNhatTonKho(authentication, id, soLuong)
        );
    }

    @PutMapping("/{id}/an")
    public ResponseEntity<SanPhamDoiTacResponse> anSanPham(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                sanPhamDoiTacService.anSanPham(authentication, id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> xoaSanPham(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        sanPhamDoiTacService.xoaSanPhamMem(authentication, id);
        return ResponseEntity.noContent().build();
    }
}