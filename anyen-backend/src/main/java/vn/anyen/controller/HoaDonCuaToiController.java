package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.service.HoaDonCuaToiService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nhan-vien/hoa-don-cua-toi")
public class HoaDonCuaToiController {

    private final HoaDonCuaToiService hoaDonCuaToiService;

    @GetMapping
    public Map<String, Object> getHoaDonCuaToi(
            Authentication authentication,

            @RequestParam(defaultValue = "")
            String keyword,

            @RequestParam(defaultValue = "Tất cả")
            String trangThai,

            @RequestParam(defaultValue = "Tất cả")
            String phuongThucThanhToan,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate tuNgay,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate denNgay,

            @RequestParam(defaultValue = "1")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer pageSize
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        return hoaDonCuaToiService.getHoaDonCuaToi(
                authentication.getName(),
                keyword,
                trangThai,
                phuongThucThanhToan,
                tuNgay,
                denNgay,
                page,
                pageSize
        );
    }
    @GetMapping("/{maHoaDon}")
    public Map<String,Object> getChiTietHoaDon(
            @PathVariable Integer maHoaDon
    ){

        return hoaDonCuaToiService
                .getChiTietHoaDon(maHoaDon);
    }
}