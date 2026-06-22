package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.response.ThongKeDoanhThuResponse;
import vn.anyen.service.ThongKeDoanhThuService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ThongKeDoanhThuController {

    private final ThongKeDoanhThuService thongKeDoanhThuService;

    @GetMapping("/api/nhan-vien/thong-ke-doanh-thu")
    public ThongKeDoanhThuResponse thongKeNhanVien(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate tuNgay,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate denNgay,

            @RequestParam(defaultValue = "NGAY")
            String kieuThongKe
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        return thongKeDoanhThuService.thongKeNhanVien(
                authentication.getName(),
                tuNgay,
                denNgay,
                kieuThongKe
        );
    }
    @GetMapping("/api/doi-tac/thong-ke-doanh-thu")
    public ThongKeDoanhThuResponse thongKeDoiTac(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate tuNgay,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate denNgay,

            @RequestParam(defaultValue = "NGAY")
            String kieuThongKe
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        return thongKeDoanhThuService.thongKeDoiTac(
                authentication.getName(),
                tuNgay,
                denNgay,
                kieuThongKe
        );
    }
}