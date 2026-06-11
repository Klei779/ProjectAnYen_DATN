package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.LichSuKhachHang;
import vn.anyen.service.KhachHangService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/khach-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class KhachHangController {

    private final KhachHangService khachHangService;

    @GetMapping
    public List<KhachHang> getAll() {
        return khachHangService.getAll();
    }

    @GetMapping("/{maKhachHang}")
    public KhachHang getById(@PathVariable Integer maKhachHang) {
        return khachHangService.getById(maKhachHang);
    }

    @GetMapping("/{maKhachHang}/lich-su")
    public List<LichSuKhachHang> getLichSu(@PathVariable Integer maKhachHang) {
        return khachHangService.getLichSu(maKhachHang);
    }
}