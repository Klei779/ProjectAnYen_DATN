package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.CapNhatTaiKhoanNVRequest;
import vn.anyen.dto.response.TaiKhoanNhanVienResponse;
import vn.anyen.service.TaiKhoanNhanVienService;

@RestController
@RequestMapping("/api/nhan-vien/tai-khoan")
@RequiredArgsConstructor
public class NhanVienTaiKhoanController {

    private final TaiKhoanNhanVienService taiKhoanNhanVienService;

    @GetMapping("/me")
    public TaiKhoanNhanVienResponse getTaiKhoan(Authentication authentication) {
        return taiKhoanNhanVienService.getTaiKhoan(getTenDangNhap(authentication));
    }

    @PutMapping("/me")
    public TaiKhoanNhanVienResponse capNhatTaiKhoan(
            Authentication authentication,
            @Valid @RequestBody CapNhatTaiKhoanNVRequest request
    ) {
        return taiKhoanNhanVienService.capNhatTaiKhoan(
                getTenDangNhap(authentication),
                request
        );
    }

    private String getTenDangNhap(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Vui lòng đăng nhập"
            );
        }

        return authentication.getName();
    }
}