package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.anyen.dto.request.ComboDoiTacRequest;
import vn.anyen.dto.response.ComboDoiTacResponse;
import vn.anyen.dto.response.SanPhamComboDoiTacResponse;
import vn.anyen.service.ComboDoiTacService;

import java.util.List;

@RestController
@RequestMapping("/api/doi-tac/combo")
@RequiredArgsConstructor
public class ComboDoiTacController {

    private final ComboDoiTacService comboDoiTacService;

    /**
     * GET /api/doi-tac/combo
     */
    @GetMapping
    public List<ComboDoiTacResponse> getCombos(
            Authentication authentication
    ) {
        return comboDoiTacService.getCombos(authentication);
    }

    /**
     * GET /api/doi-tac/combo/san-pham
     */
    @GetMapping("/san-pham")
    public List<SanPhamComboDoiTacResponse> getSanPham(
            Authentication authentication
    ) {
        return comboDoiTacService
                .getSanPhamCoTheChon(authentication);
    }

    /**
     * POST /api/doi-tac/combo
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ComboDoiTacResponse> createCombo(
            Authentication authentication,

            @Valid
            @RequestPart("data")
            ComboDoiTacRequest request,

            @RequestPart(
                    value = "files",
                    required = false
            )
            List<MultipartFile> comboFiles
    ) {
        ComboDoiTacResponse response =
                comboDoiTacService.createCombo(
                        authentication,
                        request,
                        comboFiles
                );

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/doi-tac/combo/{comboId}
     */
    @PutMapping("/{comboId}")
    public ComboDoiTacResponse update(
            Authentication authentication,
            @PathVariable Integer comboId,
            @Valid @RequestBody ComboDoiTacRequest request
    ) {
        return comboDoiTacService.updateCombo(
                authentication,
                comboId,
                request
        );
    }

    /**
     * PATCH /api/doi-tac/combo/{comboId}/trang-thai
     */
    @PatchMapping("/{comboId}/trang-thai")
    public ComboDoiTacResponse updateStatus(
            Authentication authentication,
            @PathVariable Integer comboId,
            @RequestBody TrangThaiComboRequest request
    ) {
        return comboDoiTacService.updateTrangThai(
                authentication,
                comboId,
                request.getTrangThai()
        );
    }

    @Getter
    @Setter
    public static class TrangThaiComboRequest {
        private Integer trangThai;
    }
}