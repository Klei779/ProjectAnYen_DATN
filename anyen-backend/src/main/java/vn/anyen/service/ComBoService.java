package vn.anyen.service;

import vn.anyen.dto.GoiDichVuResponse;
import vn.anyen.dto.response.ComBoChiTietResponse;

import java.util.List;
public interface ComBoService {
    List<GoiDichVuResponse> getAllCombos();

    GoiDichVuResponse getComboById(Integer id);

    List<ComBoChiTietResponse> getComBoChiTiet(Integer comboId);
}

