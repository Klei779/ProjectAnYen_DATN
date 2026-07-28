package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.TinTucRequest;
import vn.anyen.dto.response.TinTucPageResponse;
import vn.anyen.dto.response.TinTucResponse;
import vn.anyen.service.CloudinaryService;
import vn.anyen.service.TinTucService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tin-tuc")
@RequiredArgsConstructor
public class AdminTinTucController {

    private static final long MAX_IMAGE_SIZE =
            10L * 1024 * 1024;

    private final TinTucService tinTucService;

    private final CloudinaryService cloudinaryService;

    /**
     * Danh sách quản lý tin tức.
     *
     * Ví dụ:
     * /api/admin/tin-tuc?page=1&pageSize=10
     * /api/admin/tin-tuc?keyword=tang lễ
     * /api/admin/tin-tuc?loaiTin=1
     * /api/admin/tin-tuc?trangThai=1
     */
    @GetMapping
    public ResponseEntity<TinTucPageResponse> search(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Integer loaiTin,

            @RequestParam(required = false)
            Integer trangThai,

            @RequestParam(defaultValue = "1")
            int page,

            @RequestParam(defaultValue = "10")
            int pageSize
    ) {
        return ResponseEntity.ok(
                tinTucService.searchAdmin(
                        keyword,
                        loaiTin,
                        trangThai,
                        page,
                        pageSize
                )
        );
    }

    /**
     * Admin xem chi tiết bài viết.
     * API này xem được cả bài đang ẩn.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TinTucResponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                tinTucService.findAdminById(id)
        );
    }

    /**
     * Tạo tin tức.
     */
    @PostMapping
    public ResponseEntity<TinTucResponse> create(
            @Valid
            @RequestBody
            TinTucRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        tinTucService.create(request)
                );
    }

    /**
     * Cập nhật tin tức.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TinTucResponse> update(
            @PathVariable Integer id,

            @Valid
            @RequestBody
            TinTucRequest request
    ) {
        return ResponseEntity.ok(
                tinTucService.update(
                        id,
                        request
                )
        );
    }

    /**
     * Ẩn hoặc hiển thị bài viết.
     *
     * trangThai = 0: Ẩn
     * trangThai = 1: Hiển thị
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<TinTucResponse> changeStatus(
            @PathVariable Integer id,

            @RequestParam Integer trangThai
    ) {
        return ResponseEntity.ok(
                tinTucService.changeStatus(
                        id,
                        trangThai
                )
        );
    }

    /**
     * Xóa bài viết.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Integer id
    ) {
        tinTucService.delete(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Xóa tin tức thành công"
                )
        );
    }

    /**
     * Upload ảnh đại diện lên Cloudinary.
     */
    @PostMapping(
            value = "/upload-anh",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>>
    uploadImage(
            @RequestParam("file")
            MultipartFile file
    ) throws IOException {

        validateImage(file);

        String imageUrl =
                cloudinaryService.upload(
                        file,
                        "tin-tuc"
                );

        return ResponseEntity.ok(
                Map.of(
                        "url",
                        imageUrl
                )
        );
    }

    private void validateImage(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn ảnh đại diện"
            );
        }

        String contentType =
                file.getContentType();

        if (
                contentType == null
                        || !contentType.startsWith("image/")
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tệp tải lên phải là hình ảnh"
            );
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Ảnh đại diện không được vượt quá 10 MB"
            );
        }
    }
}