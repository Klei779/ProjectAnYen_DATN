package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.request.HopDongCreateRequest;
import vn.anyen.dto.response.DonHangHopDongDetailResponse;
import vn.anyen.dto.response.DonHangHopDongOptionResponse;
import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.service.HopDongService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/hop-dong")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class HopDongController {

    private final HopDongService hopDongService;

    @GetMapping
    public HopDongPageResponse getHopDongs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "Tất cả") String trangThai,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return hopDongService.getHopDongs(
                keyword,
                trangThai,
                page,
                pageSize
        );
    }

    @GetMapping("/don-hang-options")
    public List<DonHangHopDongOptionResponse> getDonHangOptions() {
        return hopDongService.getDonHangOptions();
    }

    @GetMapping("/don-hang/{maDonHang}")
    public DonHangHopDongDetailResponse getDonHangDetail(
            @PathVariable Integer maDonHang
    ) {
        return hopDongService.getDonHangDetail(maDonHang);
    }

    @PostMapping
    public HopDongResponse taoHopDong(
            @Valid @RequestBody HopDongCreateRequest request
    ) {
        return hopDongService.taoHopDong(request);
    }

   @GetMapping("/{id}")
    public HopDongResponse getChiTiet(@PathVariable Integer id) {
        return hopDongService.getChiTiet(id);
    }

    @PutMapping("/{id}/huy")
    public HopDongResponse huyHopDong(@PathVariable Integer id) {
        return hopDongService.huyHopDong(id);
    }
}
