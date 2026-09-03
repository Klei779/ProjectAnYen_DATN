package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.QuanLyDoiTacRequest;
import vn.anyen.dto.response.QuanLyDoiTacResponse;
import vn.anyen.service.QuanLyDoiTacService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/quanlydoitac")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class QuanLyDoiTacController {

    private final QuanLyDoiTacService quanLyDoiTacService;

    @GetMapping
    public List<QuanLyDoiTacResponse> getAllDoiTac() {
        return quanLyDoiTacService.getAllDoiTac();
    }

    @PostMapping("/create-doitac")
    public QuanLyDoiTacResponse createDoiTac(@Valid @RequestBody vn.anyen.dto.request.ThemDoiTacRequest request) {
        return quanLyDoiTacService.createDoiTac(request);
    }

    @PutMapping("/{maDoiTac}")
    public QuanLyDoiTacResponse updateDoiTac(
            @PathVariable Integer maDoiTac,
            @Valid @RequestBody QuanLyDoiTacRequest request
    ) {
        return quanLyDoiTacService.updateDoiTac(maDoiTac, request);
    }

    @PutMapping("/{maDoiTac}/trang-thai")
    public QuanLyDoiTacResponse updateTrangThai(
            @PathVariable Integer maDoiTac,
            @RequestParam Integer trangThai
    ) {
        return quanLyDoiTacService.updateTrangThai(maDoiTac, trangThai);
    }

    @DeleteMapping("/{maDoiTac}")
    public void deleteDoiTac(@PathVariable Integer maDoiTac) {
        quanLyDoiTacService.deleteDoiTac(maDoiTac);
    }
}