package vn.anyen.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.QuanLyNhanVienRequest;
import vn.anyen.dto.response.QuanLyNhanVienResponse;
import vn.anyen.service.QuanLyNhanVienService;

@RestController
@RequestMapping("/api/nhan-vien/quanlynhanvien")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class QuanLyNhanVienController {
    private final QuanLyNhanVienService quanLyNhanVienService;

    @PostMapping("/create-nhanvien")
    public QuanLyNhanVienResponse createNhanVien(@Valid @RequestBody QuanLyNhanVienRequest request)
    {
        return quanLyNhanVienService.createQuanLyNhanVien(request);
    }

}
