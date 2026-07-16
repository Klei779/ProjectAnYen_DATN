package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.TinTucRequest;
import vn.anyen.service.TinTucService;

@RestController
@RequestMapping("/api/tin-tuc")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TinTucController {

    private final TinTucService tinTucService;

    // Người dùng chưa đăng nhập vẫn xem được tin tức
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(tinTucService.getAll());
    }

    // Xem chi tiết tin tức
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(tinTucService.findById(id));
    }


    // Các chức năng bên dưới nên giới hạn quyền nhân viên/admin
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody TinTucRequest request) {

        return ResponseEntity.ok(tinTucService.create(request));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody TinTucRequest request) {

        return ResponseEntity.ok(
                tinTucService.update(id, request)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Integer id) {

        tinTucService.delete(id);

        return ResponseEntity.ok(
                "Xóa tin tức thành công"
        );
    }
}