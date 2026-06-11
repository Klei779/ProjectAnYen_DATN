package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.service.DonHangService;

import java.util.List;

@RestController
@RequestMapping("/api/don-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DonHangController {

    private final DonHangService donHangService;

    @GetMapping
    public List<DonHangResponse> getAllDonHang() {
        return donHangService.getAllDonHang();
    }
}
