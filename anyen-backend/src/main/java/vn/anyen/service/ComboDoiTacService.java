package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.ComboDoiTacRequest;
import vn.anyen.dto.response.ComboDoiTacResponse;
import vn.anyen.dto.response.SanPhamComboDoiTacResponse;
import vn.anyen.entity.ComBo;
import vn.anyen.entity.ComBoChiTiet;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.ComBoChiTietRepository;
import vn.anyen.repository.ComBoRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.SanPhamDoiTacRepository;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboDoiTacService {

    private final ComBoRepository comboRepository;
    private final ComBoChiTietRepository comboChiTietRepository;
    private final SanPhamDoiTacRepository sanPhamRepository;
    private final DoiTacRepository doiTacRepository;

    @Transactional(readOnly = true)
    public List<ComboDoiTacResponse> getCombos(Authentication authentication) {
        DoiTac doiTac = requireDoiTac(authentication);
        return comboRepository.findByMaDoiTacOrderByComboIdDesc(doiTac.getMaDoiTac())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SanPhamComboDoiTacResponse> getSanPhamCoTheChon(Authentication authentication) {
        DoiTac doiTac = requireDoiTac(authentication);
        return sanPhamRepository
                .findByMaDoiTacAndTrangThaiOrderByMaSanPhamDesc(
                        doiTac.getMaDoiTac(),
                        SanPham.TRANG_THAI_DANG_BAN
                )
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional
    public ComboDoiTacResponse createCombo(
            Authentication authentication,
            ComboDoiTacRequest request
    ) {
        DoiTac doiTac = requireDoiTac(authentication);
        List<SanPham> sanPhams = validateProducts(doiTac, request.getMaSanPhams());

        ComBo combo = new ComBo();
        combo.setMaDoiTac(doiTac.getMaDoiTac());
        applyRequest(combo, request);

        ComBo saved = comboRepository.save(combo);
        saveDetails(saved, sanPhams);
        return toResponse(saved);
    }

    @Transactional
    public ComboDoiTacResponse updateCombo(
            Authentication authentication,
            Integer comboId,
            ComboDoiTacRequest request
    ) {
        DoiTac doiTac = requireDoiTac(authentication);
        ComBo combo = findOwnedCombo(comboId, doiTac.getMaDoiTac());
        List<SanPham> sanPhams = validateProducts(doiTac, request.getMaSanPhams());

        applyRequest(combo, request);
        ComBo saved = comboRepository.save(combo);

        comboChiTietRepository.deleteByComboId(comboId);
        comboChiTietRepository.flush();
        saveDetails(saved, sanPhams);

        return toResponse(saved);
    }

    @Transactional
    public ComboDoiTacResponse updateTrangThai(
            Authentication authentication,
            Integer comboId,
            Integer trangThai
    ) {
        DoiTac doiTac = requireDoiTac(authentication);
        ComBo combo = findOwnedCombo(comboId, doiTac.getMaDoiTac());
        combo.setTrangThai(normalizeStatus(trangThai));
        return toResponse(comboRepository.save(combo));
    }

    private void applyRequest(ComBo combo, ComboDoiTacRequest request) {
        combo.setTenCombo(request.getTenCombo().trim());
        combo.setGia(request.getGia() == null ? BigDecimal.ZERO : request.getGia());
        combo.setMoTa(trimToNull(request.getMoTa()));
        combo.setHinhAnh(trimToNull(request.getHinhAnh()));
        combo.setTrangThai(normalizeStatus(request.getTrangThai()));
    }

    private void saveDetails(ComBo combo, List<SanPham> sanPhams) {
        List<ComBoChiTiet> details = sanPhams.stream()
                .map(product -> {
                    ComBoChiTiet detail = new ComBoChiTiet();
                    detail.setComboId(combo.getComboId());
                    detail.setMaSanPham(product.getMaSanPham());
                    detail.setLoai(ComBoChiTiet.LOAI_SAN_PHAM);
                    detail.setNoiDung(product.getTenSanPham());
                    return detail;
                })
                .toList();

        comboChiTietRepository.saveAll(details);
        comboChiTietRepository.flush();
    }

    private List<SanPham> validateProducts(DoiTac doiTac, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Combo phải có ít nhất một sản phẩm"
            );
        }

        Set<Integer> uniqueIds = ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Danh sách sản phẩm không hợp lệ"
            );
        }

        List<SanPham> products = sanPhamRepository.findAllById(uniqueIds);
        Map<Integer, SanPham> productMap = products.stream()
                .collect(Collectors.toMap(SanPham::getMaSanPham, Function.identity()));

        return uniqueIds.stream()
                .map(id -> {
                    SanPham product = productMap.get(id);
                    if (product == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Không tìm thấy sản phẩm có mã " + id
                        );
                    }

                    if (!doiTac.getMaDoiTac().equals(product.getMaDoiTac())) {
                        throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Combo chỉ được chứa sản phẩm của chính đối tác đang đăng nhập"
                        );
                    }

                    if (!SanPham.TRANG_THAI_DANG_BAN.equals(product.getTrangThai())) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Sản phẩm " + product.getTenSanPham() + " chưa ở trạng thái đang bán"
                        );
                    }

                    return product;
                })
                .toList();
    }

    private ComboDoiTacResponse toResponse(ComBo combo) {
        List<ComBoChiTiet> details = comboChiTietRepository.findByComboId(combo.getComboId());
        List<Integer> productIds = details.stream()
                .filter(detail -> ComBoChiTiet.LOAI_SAN_PHAM.equals(detail.getLoai()))
                .map(ComBoChiTiet::getMaSanPham)
                .filter(id -> id != null)
                .toList();

        Map<Integer, SanPham> productMap = sanPhamRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(SanPham::getMaSanPham, Function.identity()));

        List<SanPhamComboDoiTacResponse> products = productIds.stream()
                .map(productMap::get)
                .filter(product -> product != null)
                .map(this::toProductResponse)
                .toList();

        return ComboDoiTacResponse.builder()
                .comboId(combo.getComboId())
                .maDoiTac(combo.getMaDoiTac())
                .tenCombo(combo.getTenCombo())
                .gia(combo.getGia())
                .moTa(combo.getMoTa())
                .hinhAnh(combo.getHinhAnh())
                .trangThai(combo.getTrangThai())
                .tenTrangThai(statusLabel(combo.getTrangThai()))
                .sanPhams(products)
                .build();
    }

    private SanPhamComboDoiTacResponse toProductResponse(SanPham product) {
        return SanPhamComboDoiTacResponse.builder()
                .maSanPham(product.getMaSanPham())
                .tenSanPham(product.getTenSanPham())
                .giaTien(product.getGiaTien())
                .hinhAnh(product.getHinhAnh())
                .soLuong(product.getSoLuong())
                .trangThai(product.getTrangThai())
                .build();
    }

    private ComBo findOwnedCombo(Integer comboId, Integer partnerId) {
        return comboRepository.findByComboIdAndMaDoiTac(comboId, partnerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy combo thuộc đối tác đang đăng nhập"
                ));
    }

    private DoiTac requireDoiTac(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }

        return doiTacRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Không tìm thấy tài khoản đối tác"
                ));
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return ComBo.TT_HOAT_DONG;
        }
        if (!List.of(ComBo.TT_AN, ComBo.TT_HOAT_DONG, ComBo.TT_NGUNG_KINH_DOANH).contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái combo không hợp lệ");
        }
        return status;
    }

    private String statusLabel(Integer status) {
        if (ComBo.TT_HOAT_DONG.equals(status)) return "Đang hoạt động";
        if (ComBo.TT_NGUNG_KINH_DOANH.equals(status)) return "Ngừng kinh doanh";
        return "Đang ẩn";
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
}
