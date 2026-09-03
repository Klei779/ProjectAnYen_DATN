package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.response.KhachHangResponse;
import vn.anyen.service.KhachHangService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/quan-ly-khach-hang")
@RequiredArgsConstructor
public class QuanLyKhachHang {
    private final KhachHangService khachHangService;

    @GetMapping
    public List<KhachHangResponse> getAll(
    ) {
        return khachHangService.getAll();
    }
}
