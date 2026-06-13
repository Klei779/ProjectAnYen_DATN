package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.service.HopDongService;

@RestController
@RequestMapping("/api/nhan-vien/hop-dong")
@RequiredArgsConstructor
public class HopDongController {

    private final HopDongService hopDongService;

    @GetMapping
    public HopDongPageResponse getHopDongs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "Tất cả") String trangThai,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return hopDongService.getHopDongs(keyword, trangThai, page, pageSize);
    }

    @GetMapping("/{id}")
    public HopDongResponse getChiTiet(@PathVariable Integer id) {
        return hopDongService.getChiTiet(id);
    }
}