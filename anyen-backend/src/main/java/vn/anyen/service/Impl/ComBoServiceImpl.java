package vn.anyen.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.dto.GoiDichVuResponse;
import vn.anyen.dto.response.ComBoChiTietHinhAnhResponse;
import vn.anyen.dto.response.ComBoChiTietResponse;
import vn.anyen.entity.ComBo;
import vn.anyen.entity.ComBoChiTiet;
import vn.anyen.entity.ComBoChiTietHinhAnh;
import vn.anyen.repository.ComBoChiTietHinhAnhRepository;
import vn.anyen.repository.ComBoChiTietRepository;
import vn.anyen.repository.ComBoRepository;
import vn.anyen.service.ComBoService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComBoServiceImpl implements ComBoService {

    private final ComBoRepository comboRepository;

    private final ComBoChiTietRepository comboChiTietRepository;

    private final ComBoChiTietHinhAnhRepository comboChiTietHinhAnhRepository;

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
    public List<ComBoChiTietResponse> getComBoChiTiet(Integer comboId) {

        List<ComBoChiTiet> chiTiets =
                comboChiTietRepository.findByComboId(comboId);

        List<Integer> chiTietIds = chiTiets.stream()
                .map(ComBoChiTiet::getComboChiTietId)
                .toList();

        List<ComBoChiTietHinhAnh> hinhAnhs =
                chiTietIds.isEmpty()
                        ? List.of()
                        : comboChiTietHinhAnhRepository.findByComboChiTietIds(chiTietIds);

        Map<Integer, List<ComBoChiTietHinhAnhResponse>> hinhAnhMap =
                hinhAnhs.stream()
                        .collect(Collectors.groupingBy(
                                ComBoChiTietHinhAnh::getComboChiTietId,
                                Collectors.mapping(
                                        h -> ComBoChiTietHinhAnhResponse.builder()
                                                .maHinhAnh(h.getMaHinhAnh())
                                                .tenHinhAnh(h.getTenHinhAnh())
                                                .hinhAnh(h.getHinhAnh())
                                                .thuTu(h.getThuTu())
                                                .build(),
                                        Collectors.toList()
                                )
                        ));

        return chiTiets.stream()
                .map(item -> ComBoChiTietResponse.builder()
                        .comboChiTietId(item.getComboChiTietId())
                        .loai(item.getLoai())
                        .noiDung(item.getNoiDung())
                        .hinhAnhs(
                                hinhAnhMap.getOrDefault(
                                        item.getComboChiTietId(),
                                        List.of()
                                )
                        )
                        .build())
                .toList();
    }
}