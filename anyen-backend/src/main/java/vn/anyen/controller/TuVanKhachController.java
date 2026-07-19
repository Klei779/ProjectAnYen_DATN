package vn.anyen.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.GuiTinNhanTuVanRequest;
import vn.anyen.dto.request.TaoPhienTuVanRequest;
import vn.anyen.dto.response.PhienTuVanResponse;
import vn.anyen.dto.response.TinNhanTuVanResponse;
import vn.anyen.service.TuVanService;

import java.util.List;

@RestController
@RequestMapping("/api/tu-van")
public class TuVanKhachController {

    private final TuVanService tuVanService;

    public TuVanKhachController(TuVanService tuVanService) {
        this.tuVanService = tuVanService;
    }

    @PostMapping("/phien")
    @ResponseStatus(HttpStatus.CREATED)
    public PhienTuVanResponse taoPhien(@Valid @RequestBody TaoPhienTuVanRequest request) {
        return tuVanService.taoPhien(request);
    }

    @GetMapping("/phien/{tokenPhien}")
    public PhienTuVanResponse getPhien(@PathVariable String tokenPhien) {
        return tuVanService.getPhienKhach(tokenPhien);
    }

    @GetMapping("/phien/{tokenPhien}/tin-nhan")
    public List<TinNhanTuVanResponse> getTinNhan(@PathVariable String tokenPhien) {
        return tuVanService.getTinNhanKhach(tokenPhien);
    }

    @PostMapping("/phien/{tokenPhien}/tin-nhan")
    @ResponseStatus(HttpStatus.CREATED)
    public TinNhanTuVanResponse guiTinNhan(
            @PathVariable String tokenPhien,
            @Valid @RequestBody GuiTinNhanTuVanRequest request
    ) {
        return tuVanService.guiTinNhanKhach(tokenPhien, request);
    }

    @PostMapping("/phien/{tokenPhien}/da-doc")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void danhDauDaDoc(@PathVariable String tokenPhien) {
        tuVanService.danhDauKhachDaDoc(tokenPhien);
    }
}
