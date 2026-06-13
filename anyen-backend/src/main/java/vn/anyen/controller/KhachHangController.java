package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.entity.KhachHang;
import vn.anyen.service.JwtService;
import vn.anyen.service.KhachHangService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/khach-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class KhachHangController {

    private final KhachHangService khachHangService;
    private final JwtService jwtService;

    /**
     * Lấy danh sách khách hàng do nhân viên đang đăng nhập phụ trách
     */
    @GetMapping
    public List<KhachHang> getAll(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Integer userId = jwtService.getUserIdFromToken(token);

        return khachHangService.getByNhanVien(userId);
    }

    @GetMapping("/{maKhachHang}")
    public KhachHang getById(@PathVariable Integer maKhachHang) {
        return khachHangService.getById(maKhachHang);
    }

}