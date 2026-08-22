package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.anyen.dto.request.CapNhatNhanVienRequest;
import vn.anyen.dto.request.QuanLyNhanVienRequest;
import vn.anyen.dto.response.QuanLyNhanVienResponse;
import vn.anyen.service.QuanLyNhanVienService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/quanlynhanvien")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class QuanLyNhanVienController {

    private final QuanLyNhanVienService quanLyNhanVienService;

    @PostMapping("/create-nhanvien")
    public QuanLyNhanVienResponse createNhanVien(
            @Valid @RequestBody QuanLyNhanVienRequest request
    ) {
        return quanLyNhanVienService.createQuanLyNhanVien(request);
    }

    @GetMapping
    public List<QuanLyNhanVienResponse> getAllNhanVien() {
        return quanLyNhanVienService.getAllNhanVien();
    }

    /**
     * Chỉ ROLE_ADMIN (nhân viên có VaiTro = 1) gọi được API này.
     * Quyền được chặn tại SecurityConfig cho toàn bộ /quanlynhanvien/**.
     */
    @PutMapping("/{maNhanVien}")
    public QuanLyNhanVienResponse capNhatNhanVien(
            @PathVariable Integer maNhanVien,
            @Valid @RequestBody QuanLyNhanVienRequest request
    ) {
        return quanLyNhanVienService.capNhatNhanVien(maNhanVien, request);
    }

    @PutMapping("/nghi-viec/{maNhanVien}")
    public QuanLyNhanVienResponse nghiViecNhanVien(@PathVariable Integer maNhanVien) {
        return quanLyNhanVienService.nghiViecNhanVien(maNhanVien);
    }
    @PutMapping("/khoa/{maNhanVien}")
    public QuanLyNhanVienResponse khoaTaiKhoan(@PathVariable Integer maNhanVien) {
        return quanLyNhanVienService.khoaTaiKhoan(maNhanVien);
    }
    @PutMapping("/mo-khoa/{maNhanVien}")
    public QuanLyNhanVienResponse moKhoaTaiKhoan(@PathVariable Integer maNhanVien) {
        return quanLyNhanVienService.moKhoaTaiKhoan(maNhanVien);
    }
}