package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.service.QuanLyHopDongService;

import java.util.Map;

/**
 * API ADMIN: quản lý / lịch sử hợp đồng (chỉ xem, ẩn/hiện, xóa).
 * Được bảo vệ bằng ROLE_ADMIN trong SecurityConfig.
 */
@RestController
@RequestMapping("/api/nhan-vien/quanlyhopdong")
@RequiredArgsConstructor
public class QuanLyHopDongController {

    private final QuanLyHopDongService quanLyHopDongService;

    @GetMapping
    public HopDongPageResponse getHopDongs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue =  "3") Integer trangThai,
            @RequestParam(defaultValue = "true") boolean includeHidden,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return quanLyHopDongService.getHopDongs(
                keyword,
                trangThai,
                includeHidden,
                page,
                pageSize
        );
    }

    @GetMapping("/{id}")
    public HopDongResponse getChiTiet(@PathVariable Integer id) {
        return quanLyHopDongService.getChiTiet(id);
    }

    @PutMapping("/{id}/an")
    public HopDongResponse anHopDong(@PathVariable Integer id) {
        return quanLyHopDongService.anHopDong(id);
    }

    @PutMapping("/{id}/hien")
    public HopDongResponse hienHopDong(@PathVariable Integer id) {
        return quanLyHopDongService.hienHopDong(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> xoaHopDong(@PathVariable Integer id) {
        quanLyHopDongService.xoaHopDong(id);
        return Map.of(
                "success", true,
                "message", "Đã xóa hợp đồng và toàn bộ dữ liệu liên quan"
        );
    }
}
