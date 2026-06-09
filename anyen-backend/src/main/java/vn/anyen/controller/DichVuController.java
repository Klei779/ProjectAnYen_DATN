package vn.anyen.controller;

import vn.anyen.dto.GoiDichVuResponse;
import vn.anyen.entity.ComBoChiTiet;
import vn.anyen.service.ComBoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dich-vu")
@RequiredArgsConstructor
public class DichVuController {

    private final ComBoService comboService;

    @GetMapping
    public List<GoiDichVuResponse> getAll() {

        return comboService.getAllCombos();
    }

    @GetMapping("/{id}")
    public GoiDichVuResponse getById(
            @PathVariable Integer id
    ) {

        return comboService.getComboById(id);
    }

    @GetMapping("/{id}/chitiet")
    public List<ComBoChiTiet> getChiTiet(
            @PathVariable Integer id
    ) {

        return comboService.getComboChiTiet(id);
    }
}