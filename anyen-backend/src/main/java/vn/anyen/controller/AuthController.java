package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.anyen.dto.request.LoginRequest;
import vn.anyen.dto.request.QuenMatKhauRequest;
import vn.anyen.dto.response.LoginResponse;
import vn.anyen.dto.response.QuenMatKhauResponse;
import vn.anyen.service.AuthService;
import vn.anyen.service.QuenMatKhauService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final QuenMatKhauService
            quenMatKhauService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<QuenMatKhauResponse>
    forgotPassword(
            @Valid
            @RequestBody
            QuenMatKhauRequest request
    ) {
        return ResponseEntity.ok(
                quenMatKhauService
                        .quenMatKhau(request)
        );
    }
}