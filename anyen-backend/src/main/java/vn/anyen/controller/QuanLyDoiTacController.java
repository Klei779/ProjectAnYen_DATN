package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.QuanLyDoiTacRequest;
import vn.anyen.dto.response.QuanLyDoiTacResponse;
import vn.anyen.service.QuanLyDoiTacService;

@RestController
@RequestMapping("/api/nhan-vien/quanlydoitac")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class QuanLyDoiTacController {

    private final QuanLyDoiTacService quanLyDoiTacService;

    /**
     * API: Tạo đối tác mới và gửi email xác nhận
     * POST http://localhost:8080/api/nhan-vien/quanlydoitac/create-doitac
     */
    @PostMapping("/create-doitac")
    public QuanLyDoiTacResponse createDoiTac(@Valid @RequestBody QuanLyDoiTacRequest request) {
        return quanLyDoiTacService.createDoiTac(request);
    }
}
