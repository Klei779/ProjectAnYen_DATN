package vn.anyen.service;

import vn.anyen.dto.GoiDichVuResponse;
import vn.anyen.entity.ComBoChiTiet;

import java.util.List;
public interface ComBoService {
    List<GoiDichVuResponse> getAllCombos();

    GoiDichVuResponse getComboById(Integer id);

    List<ComBoChiTiet> getComboChiTiet(Integer comboId);
}

