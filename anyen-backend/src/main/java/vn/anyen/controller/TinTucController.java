package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.anyen.dto.response.TinTucResponse;
import vn.anyen.service.TinTucService;

import java.util.List;

@RestController
@RequestMapping("/api/tin-tuc")
@RequiredArgsConstructor
public class TinTucController {

    private final TinTucService tinTucService;

    /**
     * API công khai.
     * Chỉ trả bài viết có TrangThai = 1.
     */
    @GetMapping
    public ResponseEntity<List<TinTucResponse>>
    getAllPublic() {

        return ResponseEntity.ok(
                tinTucService.getAllPublic()
        );
    }

    /**
     * API chi tiết công khai.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TinTucResponse>
    getPublicById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                tinTucService.findPublicById(id)
        );
    }
}