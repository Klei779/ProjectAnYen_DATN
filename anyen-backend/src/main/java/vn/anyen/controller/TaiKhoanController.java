package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.DoiMatKhauRequest;
import vn.anyen.service.TaiKhoanService;

import java.util.Map;

@RestController
@RequestMapping("/api/tai-khoan")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    @PutMapping("/doi-mat-khau")
    public Map<String, String> doiMatKhau(
            @Valid @RequestBody DoiMatKhauRequest request,
            Authentication authentication
    ) {
        String message = taiKhoanService.doiMatKhau(request, authentication);

        return Map.of("message", message);
    }
}
