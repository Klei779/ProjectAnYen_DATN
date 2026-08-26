package vn.anyen.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.anyen.dto.request.ComboAdminRequest;
import vn.anyen.dto.response.ComboAdminResponse;
import vn.anyen.dto.response.SanPhamComboAdminResponse;
import vn.anyen.service.ComboAdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/combo")
public class ComboAdminController {

    private final ComboAdminService comboAdminService;

    public ComboAdminController(ComboAdminService comboAdminService) {
        this.comboAdminService = comboAdminService;
    }

    @GetMapping
    public List<ComboAdminResponse> getAll(Authentication authentication) {
        return comboAdminService.getCombos(authentication);
    }

    @GetMapping("/san-pham")
    public List<SanPhamComboAdminResponse> getSanPham(Authentication authentication) {
        return comboAdminService.getSanPhamCoTheChon(authentication);
    }

    @GetMapping("/{comboId}")
    public ComboAdminResponse getById(
            Authentication authentication,
            @PathVariable Integer comboId
    ) {
        return comboAdminService.getCombo(authentication, comboId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComboAdminResponse> create(
            Authentication authentication,
            @Valid @RequestPart("data") ComboAdminRequest request,
            @RequestPart(value = "anhDaiDien", required = false)
            List<MultipartFile> anhDaiDien,
            @RequestPart(value = "anhQuyTrinh", required = false)
            List<MultipartFile> anhQuyTrinh
    ) {
        ComboAdminResponse response = comboAdminService.createCombo(
                authentication,
                request,
                anhDaiDien,
                anhQuyTrinh
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(
            value = "/{comboId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ComboAdminResponse update(
            Authentication authentication,
            @PathVariable Integer comboId,
            @Valid @RequestPart("data") ComboAdminRequest request,
            @RequestPart(value = "anhDaiDien", required = false)
            List<MultipartFile> anhDaiDien,
            @RequestPart(value = "anhQuyTrinh", required = false)
            List<MultipartFile> anhQuyTrinh
    ) {
        return comboAdminService.updateCombo(
                authentication,
                comboId,
                request,
                anhDaiDien,
                anhQuyTrinh
        );
    }

    @PatchMapping("/{comboId}/trang-thai")
    public ComboAdminResponse updateStatus(
            Authentication authentication,
            @PathVariable Integer comboId,
            @RequestBody TrangThaiComboRequest request
    ) {
        return comboAdminService.updateTrangThai(
                authentication,
                comboId,
                request.getTrangThai()
        );
    }

    public static class TrangThaiComboRequest {
        private Integer trangThai;

        public TrangThaiComboRequest() {
        }

        public Integer getTrangThai() {
            return trangThai;
        }

        public void setTrangThai(Integer trangThai) {
            this.trangThai = trangThai;
        }
    }
}