package vn.anyen.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.GoiDichVuResponse;
import vn.anyen.dto.response.ComBoChiTietHinhAnhResponse;
import vn.anyen.dto.response.ComBoChiTietResponse;
import vn.anyen.entity.ComBo;
import vn.anyen.entity.ComBoChiTiet;
import vn.anyen.entity.ComBoChiTietHinhAnh;
import vn.anyen.entity.ComBoHinhAnh;
import vn.anyen.repository.ComBoChiTietHinhAnhRepository;
import vn.anyen.repository.ComBoChiTietRepository;
import vn.anyen.repository.ComBoHinhAnhRepository;
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

    // Repository lấy nhiều ảnh của combo
    private final ComBoHinhAnhRepository comboHinhAnhRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GoiDichVuResponse> getAllCombos() {

        return comboRepository.findAll()
                .stream()
                .map(this::toGoiDichVuResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoiDichVuResponse getComboById(Integer id) {

        ComBo combo = comboRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy combo có mã: " + id
                        )
                );

        return toGoiDichVuResponse(combo);
    }

    /**
     * Chuyển Combo thành DTO và lấy toàn bộ ảnh trong combo_hinhanh.
     */
    private GoiDichVuResponse toGoiDichVuResponse(ComBo combo) {

        List<String> hinhAnhs = comboHinhAnhRepository
                .findByComboIdOrderByThuTuAscMaHinhAnhAsc(
                        combo.getComboId()
                )
                .stream()
                .map(ComBoHinhAnh::getHinhAnh)
                .filter(url ->
                        url != null && !url.isBlank()
                )
                .distinct()
                .toList();

        return GoiDichVuResponse.fromEntity(
                combo,
                hinhAnhs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComBoChiTietResponse> getComBoChiTiet(
            Integer comboId
    ) {

        List<ComBoChiTiet> chiTiets =
                comboChiTietRepository.findByComboId(comboId);

        List<Integer> chiTietIds = chiTiets.stream()
                .map(ComBoChiTiet::getComboChiTietId)
                .toList();

        List<ComBoChiTietHinhAnh> hinhAnhs =
                chiTietIds.isEmpty()
                        ? List.of()
                        : comboChiTietHinhAnhRepository
                        .findByComboChiTietIds(chiTietIds);

        Map<Integer, List<ComBoChiTietHinhAnhResponse>> hinhAnhMap =
                hinhAnhs.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ComBoChiTietHinhAnh::getComboChiTietId,

                                        Collectors.mapping(
                                                h ->
                                                        ComBoChiTietHinhAnhResponse
                                                                .builder()
                                                                .maHinhAnh(
                                                                        h.getMaHinhAnh()
                                                                )
                                                                .tenHinhAnh(
                                                                        h.getTenHinhAnh()
                                                                )
                                                                .hinhAnh(
                                                                        h.getHinhAnh()
                                                                )
                                                                .thuTu(
                                                                        h.getThuTu()
                                                                )
                                                                .build(),

                                                Collectors.toList()
                                        )
                                )
                        );

        return chiTiets.stream()
                .map(item ->
                        ComBoChiTietResponse.builder()
                                .comboChiTietId(
                                        item.getComboChiTietId()
                                )
                                .loai(
                                        item.getLoai()
                                )
                                .soLuong(
                                        item.getSoLuong() == null
                                                || item.getSoLuong() <= 0
                                                ? 1
                                                : item.getSoLuong()
                                )
                                .noiDung(
                                        item.getNoiDung()
                                )
                                .hinhAnhs(
                                        hinhAnhMap.getOrDefault(
                                                item.getComboChiTietId(),
                                                List.of()
                                        )
                                )
                                .build()
                )
                .toList();
    }
}