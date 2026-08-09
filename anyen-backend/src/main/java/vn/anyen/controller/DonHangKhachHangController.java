package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.service.DonHangKhachHangService;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang/don-hang")
@RequiredArgsConstructor
public class DonHangKhachHangController {

    private final DonHangKhachHangService
            donHangKhachHangService;

    @PostMapping
    public ResponseEntity<List<DonHangResponse>> taoDonHang(
            @RequestBody TaoDonHangRequest request
    ) {

        return ResponseEntity.ok(
                donHangKhachHangService
                        .taoDonHangKhachHang(request)
        );
    }
}