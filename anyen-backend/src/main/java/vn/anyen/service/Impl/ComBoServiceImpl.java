package vn.anyen.service.Impl;

import vn.anyen.dto.GoiDichVuResponse;
import vn.anyen.entity.ComBo;
import vn.anyen.entity.ComBoChiTiet;
import vn.anyen.repository.ComBoRepository;
import vn.anyen.repository.ComBoChiTietRepository;
import vn.anyen.service.ComBoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComBoServiceImpl implements ComBoService {

    private final ComBoRepository comboRepository;

    private final ComBoChiTietRepository comboChiTietRepository;

    @Override
    public List<GoiDichVuResponse> getAllCombos() {

        return comboRepository.findAll()
                .stream()
                .map(GoiDichVuResponse::fromEntity)
                .toList();
    }

    @Override
    public GoiDichVuResponse getComboById(Integer id) {

        ComBo combo =
                comboRepository.findById(id)
                        .orElseThrow();

        return GoiDichVuResponse.fromEntity(combo);
    }

    @Override
    public List<ComBoChiTiet> getComboChiTiet(Integer comboId) {

        return comboChiTietRepository
                .findByComboId(comboId);
    }
}
