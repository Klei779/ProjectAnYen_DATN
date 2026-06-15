package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.CapNhatTrangThaiKHRequest;
import vn.anyen.dto.request.KhachHangRequest;
import vn.anyen.dto.response.KhachHangLichSuResponse;
import vn.anyen.dto.response.KhachHangResponse;
import vn.anyen.service.JwtService;
import vn.anyen.service.KhachHangService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/khach-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class KhachHangController {

    private final KhachHangService khachHangService;
    private final JwtService jwtService;

    private Integer getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Không có token xác thực");
        }

        String token = authHeader.substring(7);
        return jwtService.getUserIdFromToken(token);
    }

    @GetMapping
    public List<KhachHangResponse> getAll(
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = getUserIdFromHeader(authHeader);
        return khachHangService.getByNhanVien(userId);
    }

    @GetMapping("/{maKhachHang}")
    public KhachHangResponse getById(
            @PathVariable Integer maKhachHang,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = getUserIdFromHeader(authHeader);
        return khachHangService.getByIdResponse(maKhachHang, userId);
    }

    @PostMapping
    public ResponseEntity<KhachHangResponse> create(
            @Valid @RequestBody KhachHangRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = getUserIdFromHeader(authHeader);
        return ResponseEntity.ok(khachHangService.create(request, userId));
    }

    @PutMapping("/{maKhachHang}/trang-thai-lam-viec")
    public ResponseEntity<KhachHangResponse> updateTrangThaiLamViec(
            @PathVariable Integer maKhachHang,
            @Valid @RequestBody CapNhatTrangThaiKHRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = getUserIdFromHeader(authHeader);

        return ResponseEntity.ok(
                khachHangService.updateTrangThaiLamViec(
                        maKhachHang,
                        userId,
                        request.getTrangThaiLamViec()
                )
        );
    }

    @GetMapping("/{maKhachHang}/lich-su")
    public List<KhachHangLichSuResponse> getLichSu(
            @PathVariable Integer maKhachHang,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = getUserIdFromHeader(authHeader);
        return khachHangService.getLichSu(maKhachHang, userId);
    }
}