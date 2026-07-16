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

    @GetMapping("/{id}")
    public SanPhamResponse getSanPhamById(@PathVariable Integer id) {
        return sanPhamService.getSanPhamById(id);
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

    @PutMapping("/{id}/duyet")

    public SanPhamResponse duyetSanPham(@PathVariable Integer id) {

        return sanPhamService.duyetSanPham(id);

    }



    @GetMapping("/cho-duyet")

    public SanPhamPageResponse getSanPhamChoDuyet(

            @RequestParam(value = "page", required = false) Integer page,

            @RequestParam(value = "pageSize", required = false) Integer pageSize

    ) {

        // Gọi đúng hàm, truyền đủ tham số và trả về đúng kiểu PageResponse

        return sanPhamService.getSanPhamChoDuyet(page, pageSize);

    }

    @PutMapping("/{id}/tu-choi")

    public SanPhamResponse tuChoiSanPham(

            @PathVariable Integer id,

            @RequestBody java.util.Map<String, String> payload // Sử dụng Map để hứng JSON Body gửi lên từ FE

    ) {

        String lyDoTuChoi = payload.get("lyDoTuChoi");

        if (lyDoTuChoi == null || lyDoTuChoi.trim().isBlank()) {

            throw new IllegalArgumentException("Lý do từ chối không được để trống");

        }

        return sanPhamService.tuChoiSanPham(id, lyDoTuChoi);

    }




}