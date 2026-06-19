package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.QuanLyNhanVienRequest;
import vn.anyen.dto.response.QuanLyNhanVienResponse;
import vn.anyen.service.QuanLyNhanVienService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/quanlynhanvien")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Cho phép gọi API từ cổng mặc định của Vite Vue 3
public class QuanLyNhanVienController {

    private final QuanLyNhanVienService quanLyNhanVienService;

    /**
     * 1. API: Thêm mới nhân viên
     */
    @PostMapping("/create-nhanvien")
    public QuanLyNhanVienResponse createNhanVien(@Valid @RequestBody QuanLyNhanVienRequest request) {
        return quanLyNhanVienService.createQuanLyNhanVien(request);
    }

    /**
     * 2. BỔ SUNG: API Lấy danh sách tất cả nhân viên
     * Đường dẫn gọi từ Frontend: GET http://localhost:8080/api/nhan-vien/quanlynhanvien
     */
    @GetMapping
    public List<QuanLyNhanVienResponse> getAllNhanVien() {
        return quanLyNhanVienService.getAllNhanVien();
    }

    /**
     * 3. BỔ SUNG: API Cập nhật trạng thái nghỉ việc của nhân viên
     * Đường dẫn gọi từ Frontend: PUT http://localhost:8080/api/nhan-vien/quanlynhanvien/nghi-viec/{maNhanVien}
     */
    @PutMapping("/nghi-viec/{maNhanVien}")
    public QuanLyNhanVienResponse nghiViecNhanVien(@PathVariable Integer maNhanVien) {
        return quanLyNhanVienService.nghiViecNhanVien(maNhanVien);
    }
}