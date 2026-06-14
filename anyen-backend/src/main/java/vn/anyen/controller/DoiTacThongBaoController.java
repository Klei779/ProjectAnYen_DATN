package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.request.TuChoiThongBaoRequest;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.dto.response.DoiTacThongBaoResponse;
import vn.anyen.dto.response.XuLyThongBaoResponse;
import vn.anyen.service.DoiTacThongBaoService;

import java.util.List;

@RestController
@RequestMapping("/api/doi-tac")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DoiTacThongBaoController {

    private final DoiTacThongBaoService doiTacThongBaoService;

    @GetMapping("/thong-bao")
    public List<DoiTacThongBaoResponse> getThongBao(
            Authentication authentication
    ) {
        return doiTacThongBaoService.getThongBao(authentication);
    }

    @PostMapping("/thong-bao/{maThongBao}/chap-nhan")
    public XuLyThongBaoResponse chapNhanThongBao(
            @PathVariable Integer maThongBao,
            Authentication authentication
    ) {
        return doiTacThongBaoService.chapNhanThongBao(
                maThongBao,
                authentication
        );
    }

    @PostMapping("/thong-bao/{maThongBao}/tu-choi")
    public XuLyThongBaoResponse tuChoiThongBao(
            @PathVariable Integer maThongBao,
            @Valid @RequestBody TuChoiThongBaoRequest request,
            Authentication authentication
    ) {
        return doiTacThongBaoService.tuChoiThongBao(
                maThongBao,
                request,
                authentication
        );
    }

    @GetMapping("/don-hang")
    public DoiTacDonHangPageResponse getDonHangDaChapNhan(
            Authentication authentication
    ) {
        return doiTacThongBaoService.getDonHangDaChapNhan(authentication);
    }

    @GetMapping("/don-hang/{maDonHang}")
    public DoiTacDonHangResponse getChiTietDonHang(
            @PathVariable Integer maDonHang,
            Authentication authentication
    ) {
        return doiTacThongBaoService.getChiTietDonHang(
                maDonHang,
                authentication
        );
    }

    @PutMapping("/don-hang/{maDonHang}/trang-thai")
    public XuLyThongBaoResponse updateTrangThaiDonHang(
            @PathVariable Integer maDonHang,
            @RequestBody CapNhatTrangThaiDonHangRequest request,
            Authentication authentication
    ) {
        return doiTacThongBaoService.updateTrangThaiDonHang(
                maDonHang,
                request,
                authentication
        );
    }
}