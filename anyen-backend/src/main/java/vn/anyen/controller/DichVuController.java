package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.entity.GoiDichVu;
import vn.anyen.repository.GoiDichVuRepository;

import java.util.List;

@RestController
@RequestMapping("/api/dich-vu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DichVuController {

    private final GoiDichVuRepository goiDichVuRepository;

    @GetMapping
    public ResponseEntity<List<GoiDichVu>> layTatCa() {

        return ResponseEntity.ok(
                goiDichVuRepository.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoiDichVu> layChiTiet(
            @PathVariable Integer id
    ) {

        GoiDichVu goi = goiDichVuRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy gói dịch vụ"));

        return ResponseEntity.ok(goi);
    }
}
