package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.CapNhatTaiKhoanDTRequest;
import vn.anyen.dto.request.CapNhatViTriRequest;
import vn.anyen.dto.response.TaiKhoanDoiTacResponse;
import vn.anyen.dto.response.TaiKhoanNhanVienResponse;
import vn.anyen.service.TaiKhoanDoiTacService;

@RestController
@RequestMapping("/api/doi-tac/tai-khoan")
@RequiredArgsConstructor
public class DoiTacTaiKhoanController {

    private final TaiKhoanDoiTacService taiKhoanDoiTacService;

    @GetMapping("/me")
    public TaiKhoanDoiTacResponse getTaiKhoan(Authentication authentication) {
        return taiKhoanDoiTacService.getTaiKhoan(getTenDangNhap(authentication));
    }

    @PutMapping("/me")
    public TaiKhoanDoiTacResponse capNhatTaiKhoan(
            Authentication authentication,
            @Valid @RequestBody CapNhatTaiKhoanDTRequest request
    ) {
        return taiKhoanDoiTacService.capNhatTaiKhoan(
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
    @PostMapping("/location")
    public TaiKhoanDoiTacResponse capNhatViTri(Authentication authentication, @RequestBody CapNhatViTriRequest request){
        return taiKhoanDoiTacService.capNhatViTriDoiTac(getTenDangNhap(authentication),request);
    }
}