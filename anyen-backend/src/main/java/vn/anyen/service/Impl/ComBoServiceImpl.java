package vn.anyen.service.Impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
public class ComBoServiceImpl implements ComBoService {

    private final ComBoRepository comboRepository;
    private final ComBoChiTietRepository comboChiTietRepository;
    private final ComBoChiTietHinhAnhRepository comboChiTietHinhAnhRepository;
    private final ComBoHinhAnhRepository comboHinhAnhRepository;

    public ComBoServiceImpl(
            ComBoRepository comboRepository,
            ComBoChiTietRepository comboChiTietRepository,
            ComBoChiTietHinhAnhRepository comboChiTietHinhAnhRepository,
            ComBoHinhAnhRepository comboHinhAnhRepository
    ) {
        this.comboRepository = comboRepository;
        this.comboChiTietRepository = comboChiTietRepository;
        this.comboChiTietHinhAnhRepository = comboChiTietHinhAnhRepository;
        this.comboHinhAnhRepository = comboHinhAnhRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoiDichVuResponse> getAllCombos() {
        return comboRepository
                .findByTrangThaiOrderByComboIdDesc(ComBo.TT_HOAT_DONG)
                .stream()
                .map(this::toGoiDichVuResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoiDichVuResponse getComboById(Integer id) {
        ComBo combo = comboRepository
                .findByComboIdAndTrangThai(id, ComBo.TT_HOAT_DONG)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy combo đang hoạt động có mã " + id
                ));
        return toGoiDichVuResponse(combo);
    }

    private GoiDichVuResponse toGoiDichVuResponse(ComBo combo) {
        List<String> covers = imageUrls(
                combo.getComboId(),
                ComBoHinhAnh.LOAI_DAI_DIEN
        );
        List<String> processImages = imageUrls(
                combo.getComboId(),
                ComBoHinhAnh.LOAI_QUY_TRINH
        );
        List<String> detailImages = imageUrls(
                combo.getComboId(),
                ComBoHinhAnh.LOAI_CHI_TIET
        );
        String detailImage = detailImages.isEmpty() ? null : detailImages.get(0);

        return GoiDichVuResponse.fromEntity(
                combo,
                covers,
                processImages,
                detailImage
        );
    }

    private List<String> imageUrls(Integer comboId, String imageType) {
        return comboHinhAnhRepository
                .findByComboIdAndLoaiHinhAnhOrderByThuTuAscMaHinhAnhAsc(
                        comboId,
                        imageType
                )
                .stream()
                .map(ComBoHinhAnh::getHinhAnh)
                .filter(url -> url != null && !url.isBlank())
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComBoChiTietResponse> getComBoChiTiet(Integer comboId) {
        comboRepository
                .findByComboIdAndTrangThai(comboId, ComBo.TT_HOAT_DONG)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy combo đang hoạt động có mã " + comboId
                ));

        List<ComBoChiTiet> chiTiets = comboChiTietRepository
                .findByComboId(comboId);

        List<Integer> chiTietIds = chiTiets.stream()
                .map(ComBoChiTiet::getComboChiTietId)
                .toList();

        List<ComBoChiTietHinhAnh> hinhAnhs = chiTietIds.isEmpty()
                ? List.of()
                : comboChiTietHinhAnhRepository.findByComboChiTietIds(chiTietIds);

        Map<Integer, List<ComBoChiTietHinhAnhResponse>> hinhAnhMap = hinhAnhs
                .stream()
                .collect(Collectors.groupingBy(
                        ComBoChiTietHinhAnh::getComboChiTietId,
                        Collectors.mapping(
                                image -> ComBoChiTietHinhAnhResponse.builder()
                                        .maHinhAnh(image.getMaHinhAnh())
                                        .tenHinhAnh(image.getTenHinhAnh())
                                        .hinhAnh(image.getHinhAnh())
                                        .thuTu(image.getThuTu())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        return chiTiets.stream()
                .map(item -> ComBoChiTietResponse.builder()
                        .comboChiTietId(item.getComboChiTietId())
                        .loai(item.getLoai())
                        .soLuong(item.getSoLuong() == null || item.getSoLuong() <= 0
                                ? 1
                                : item.getSoLuong())
                        .noiDung(item.getNoiDung())
                        .hinhAnhs(hinhAnhMap.getOrDefault(
                                item.getComboChiTietId(),
                                List.of()
                        ))
                        .build())
                .toList();
    }
}