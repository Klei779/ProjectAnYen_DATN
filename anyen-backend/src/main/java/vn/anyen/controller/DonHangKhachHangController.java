package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.service.DonHangKhachHangService;

@RestController
@RequestMapping("/api/khach-hang/don-hang")
@RequiredArgsConstructor
public class DonHangKhachHangController {

    private final DonHangKhachHangService
            donHangKhachHangService;

    @PostMapping
    public ResponseEntity<DonHangResponse>
    taoDonHangKhachHang(
            @RequestBody TaoDonHangRequest request
    ) {
        DonHangResponse response =
                donHangKhachHangService
                        .taoDonHangKhachHang(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}