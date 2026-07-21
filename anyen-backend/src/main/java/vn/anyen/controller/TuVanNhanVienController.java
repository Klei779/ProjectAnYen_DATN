package vn.anyen.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.GuiTinNhanTuVanRequest;
import vn.anyen.dto.response.PhienTuVanResponse;
import vn.anyen.dto.response.TinNhanTuVanResponse;
import vn.anyen.service.TuVanService;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien/tu-van")
public class TuVanNhanVienController {

    private final TuVanService tuVanService;

    public TuVanNhanVienController(TuVanService tuVanService) {
        this.tuVanService = tuVanService;
    }

    @PostMapping("/presence/heartbeat")
    public java.util.Map<String, Object> heartbeat(Authentication authentication) {
        tuVanService.heartbeat(authentication);
        return java.util.Map.of("online", true);
    }

    @GetMapping("/presence/me")
    public java.util.Map<String, Object> getPresence(Authentication authentication) {
        return java.util.Map.of("online", tuVanService.isOnline(authentication));
    }

    @DeleteMapping("/presence/offline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void offline(Authentication authentication) {
        tuVanService.offline(authentication);
    }

    @GetMapping("/phien")
    public List<PhienTuVanResponse> getDanhSachPhien(Authentication authentication) {
        return tuVanService.getDanhSachPhienNhanVien(authentication);
    }

    @PatchMapping("/phien/{maPhien}/nhan")
    public PhienTuVanResponse nhanPhien(
            Authentication authentication,
            @PathVariable Long maPhien
    ) {
        return tuVanService.nhanPhien(authentication, maPhien);
    }

    @GetMapping("/phien/{maPhien}/tin-nhan")
    public List<TinNhanTuVanResponse> getTinNhan(
            Authentication authentication,
            @PathVariable Long maPhien
    ) {
        return tuVanService.getTinNhanNhanVien(authentication, maPhien);
    }

    @PostMapping("/phien/{maPhien}/tin-nhan")
    @ResponseStatus(HttpStatus.CREATED)
    public TinNhanTuVanResponse guiTinNhan(
            Authentication authentication,
            @PathVariable Long maPhien,
            @Valid @RequestBody GuiTinNhanTuVanRequest request
    ) {
        return tuVanService.guiTinNhanNhanVien(authentication, maPhien, request);
    }

    @PatchMapping("/phien/{maPhien}/dong")
    public PhienTuVanResponse dongPhien(
            Authentication authentication,
            @PathVariable Long maPhien
    ) {
        return tuVanService.dongPhien(authentication, maPhien);
    }
}
