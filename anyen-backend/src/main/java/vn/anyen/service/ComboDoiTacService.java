package vn.anyen.service;

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
import vn.anyen.entity.ComBoHinhAnh;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.ComBoChiTietRepository;
import vn.anyen.repository.ComBoHinhAnhRepository;
import vn.anyen.repository.ComBoRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.SanPhamDoiTacRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ComboDoiTacService {

    private final ComBoRepository comboRepository;
    private final ComBoChiTietRepository comboChiTietRepository;
    private final ComBoHinhAnhRepository comboHinhAnhRepository;
    private final SanPhamDoiTacRepository sanPhamRepository;
    private final DoiTacRepository doiTacRepository;
    private final CloudinaryService cloudinaryService;

    public ComboDoiTacService(
            ComBoRepository comboRepository,
            ComBoChiTietRepository comboChiTietRepository,
            ComBoHinhAnhRepository comboHinhAnhRepository,
            SanPhamDoiTacRepository sanPhamRepository,
            DoiTacRepository doiTacRepository,
            CloudinaryService cloudinaryService
    ) {
        this.comboRepository = comboRepository;
        this.comboChiTietRepository = comboChiTietRepository;
        this.comboHinhAnhRepository = comboHinhAnhRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.doiTacRepository = doiTacRepository;
        this.cloudinaryService = cloudinaryService;
    }

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
                .map(product -> toProductResponse(product, null))
                .toList();
    }

    @Transactional
    public ComboDoiTacResponse createCombo(
            Authentication authentication,
            ComboDoiTacRequest request,
            List<MultipartFile> files
    ) {
        DoiTac doiTac = requireDoiTac(authentication);

        List<ValidatedComboItem> items =
                validateProducts(doiTac, request);

        validateComboPrice(request.getGia(), items);
        validateComboImages(files);

        ComBo combo = new ComBo();
        combo.setMaDoiTac(doiTac.getMaDoiTac());

        applyRequest(combo, request);

        ComBo saved = comboRepository.save(combo);

        List<String> imageUrls = uploadComboImages(files);

        if (!imageUrls.isEmpty()) {
            saved.setHinhAnh(imageUrls.get(0));
            saved = comboRepository.save(saved);
            saveComboImages(saved, files, imageUrls);
        }

        saveDetails(saved, items);

        return toResponse(saved);
    }

    private void validateComboImages(
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<MultipartFile> realFiles = files.stream()
                .filter(file ->
                        file != null && !file.isEmpty()
                )
                .toList();

        if (realFiles.size() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Combo chỉ được chọn tối đa 20 ảnh"
            );
        }

        long maxFileSize = 5L * 1024 * 1024;

        Set<String> allowedTypes = Set.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        );

        for (MultipartFile file : realFiles) {
            if (file.getSize() > maxFileSize) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ảnh "
                                + file.getOriginalFilename()
                                + " vượt quá 5 MB"
                );
            }

            String contentType = file.getContentType();

            if (
                    contentType == null
                            || !allowedTypes.contains(contentType)
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ảnh "
                                + file.getOriginalFilename()
                                + " không đúng định dạng JPG, PNG hoặc WEBP"
                );
            }
        }
    }

    @Transactional
    public ComboDoiTacResponse updateCombo(
            Authentication authentication,
            Integer comboId,
            ComboDoiTacRequest request
    ) {
        DoiTac doiTac = requireDoiTac(authentication);
        ComBo combo = findOwnedCombo(comboId, doiTac.getMaDoiTac());
        List<ValidatedComboItem> items = validateProducts(doiTac, request);
        validateComboPrice(request.getGia(), items);

        String previousImage = trimToNull(combo.getHinhAnh());
        String requestedImage = trimToNull(request.getHinhAnh());

        applyRequest(combo, request);
        ComBo saved = comboRepository.save(combo);
        if (!Objects.equals(previousImage, requestedImage)) {
            syncLegacyImage(saved, requestedImage);
        }

        comboChiTietRepository.deleteByComboId(comboId);
        comboChiTietRepository.flush();
        saveDetails(saved, items);

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
        combo.setGia(request.getGia());
        combo.setMoTa(trimToNull(request.getMoTa()));
        combo.setHinhAnh(trimToNull(request.getHinhAnh()));
        combo.setTrangThai(normalizeStatus(request.getTrangThai()));
    }

    private void saveDetails(ComBo combo, List<ValidatedComboItem> items) {
        List<ComBoChiTiet> details = items.stream()
                .map(item -> {
                    ComBoChiTiet detail = new ComBoChiTiet();
                    detail.setComboId(combo.getComboId());
                    detail.setMaSanPham(item.product().getMaSanPham());
                    detail.setLoai(ComBoChiTiet.LOAI_SAN_PHAM);
                    detail.setSoLuong(item.quantity());
                    detail.setNoiDung(item.product().getTenSanPham());
                    return detail;
                })
                .toList();

        comboChiTietRepository.saveAll(details);
        comboChiTietRepository.flush();
    }

    private List<ValidatedComboItem> validateProducts(
            DoiTac doiTac,
            ComboDoiTacRequest request
    ) {
        LinkedHashMap<Integer, Integer> requestedItems = resolveRequestedItems(request);
        List<SanPham> products = sanPhamRepository.findAllById(requestedItems.keySet());
        Map<Integer, SanPham> productMap = products.stream()
                .collect(Collectors.toMap(SanPham::getMaSanPham, Function.identity()));

        List<ValidatedComboItem> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : requestedItems.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            SanPham product = productMap.get(productId);

            if (product == null) {
                throw badRequest("Không tìm thấy sản phẩm có mã " + productId);
            }
            if (!doiTac.getMaDoiTac().equals(product.getMaDoiTac())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Combo chỉ được chứa sản phẩm của chính đối tác đang đăng nhập"
                );
            }
            if (!SanPham.TRANG_THAI_DANG_BAN.equals(product.getTrangThai())) {
                throw badRequest("Sản phẩm " + product.getTenSanPham() + " chưa ở trạng thái đang bán");
            }

            int stock = Math.max(0, product.getSoLuong() == null ? 0 : product.getSoLuong());
            if (quantity > stock) {
                throw badRequest(
                        "Số lượng " + product.getTenSanPham()
                                + " trong combo (" + quantity + ") vượt tồn kho hiện tại (" + stock + ")"
                );
            }

            result.add(new ValidatedComboItem(product, quantity));
        }
        return result;
    }

    private LinkedHashMap<Integer, Integer> resolveRequestedItems(ComboDoiTacRequest request) {
        LinkedHashMap<Integer, Integer> result = new LinkedHashMap<>();

        if (request.getSanPhams() != null && !request.getSanPhams().isEmpty()) {
            for (ComboDoiTacRequest.ComboSanPhamRequest item : request.getSanPhams()) {
                if (item == null || item.getMaSanPham() == null) {
                    throw badRequest("Danh sách sản phẩm không hợp lệ");
                }
                Integer quantity = item.getSoLuong();
                if (quantity == null || quantity <= 0) {
                    throw badRequest("Số lượng sản phẩm trong combo phải lớn hơn 0");
                }
                addUniqueItem(result, item.getMaSanPham(), quantity);
            }
        } else if (request.getMaSanPhams() != null && !request.getMaSanPhams().isEmpty()) {
            for (Integer productId : request.getMaSanPhams()) {
                if (productId == null) {
                    throw badRequest("Danh sách sản phẩm không hợp lệ");
                }
                addUniqueItem(result, productId, 1);
            }
        }

        if (result.isEmpty()) {
            throw badRequest("Combo phải có ít nhất một sản phẩm");
        }
        return result;
    }

    private void addUniqueItem(Map<Integer, Integer> items, Integer productId, Integer quantity) {
        if (items.putIfAbsent(productId, quantity) != null) {
            throw badRequest("Sản phẩm mã " + productId + " bị chọn trùng trong combo");
        }
    }

    private void validateComboPrice(BigDecimal comboPrice, List<ValidatedComboItem> items) {
        if (comboPrice == null || comboPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Giá combo phải lớn hơn 0");
        }

        BigDecimal totalProductPrice = calculateTotalProductPrice(items);
        if (totalProductPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Không thể tạo combo vì tổng giá sản phẩm chưa hợp lệ");
        }
        if (comboPrice.compareTo(totalProductPrice) > 0) {
            throw badRequest(
                    "Giá combo không được lớn hơn tổng giá sản phẩm theo số lượng ("
                            + totalProductPrice.stripTrailingZeros().toPlainString() + " đ)"
            );
        }
    }

    private BigDecimal calculateTotalProductPrice(List<ValidatedComboItem> items) {
        return items.stream()
                .map(item -> safePrice(item.product()).multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ComboDoiTacResponse toResponse(ComBo combo) {
        List<ComBoChiTiet> details = comboChiTietRepository.findByComboId(combo.getComboId())
                .stream()
                .filter(detail -> ComBoChiTiet.LOAI_SAN_PHAM.equals(detail.getLoai()))
                .filter(detail -> detail.getMaSanPham() != null)
                .toList();

        List<Integer> productIds = details.stream()
                .map(ComBoChiTiet::getMaSanPham)
                .distinct()
                .toList();

        Map<Integer, SanPham> productMap = sanPhamRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(SanPham::getMaSanPham, Function.identity()));

        List<SanPhamComboDoiTacResponse> products = details.stream()
                .map(detail -> {
                    SanPham product = productMap.get(detail.getMaSanPham());
                    if (product == null) return null;
                    return toProductResponse(product, normalizeQuantity(detail.getSoLuong()));
                })
                .filter(item -> item != null)
                .toList();

        BigDecimal totalProductPrice = products.stream()
                .map(item -> item.getThanhTien() == null ? BigDecimal.ZERO : item.getThanhTien())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> imageUrls = comboHinhAnhRepository
                .findByComboIdOrderByThuTuAscMaHinhAnhAsc(combo.getComboId())
                .stream()
                .map(ComBoHinhAnh::getHinhAnh)
                .filter(url -> url != null && !url.isBlank())
                .toList();

        if (imageUrls.isEmpty() && combo.getHinhAnh() != null && !combo.getHinhAnh().isBlank()) {
            imageUrls = List.of(combo.getHinhAnh());
        }

        return new ComboDoiTacResponse(
                combo.getComboId(),
                combo.getMaDoiTac(),
                combo.getTenCombo(),
                combo.getGia(),
                totalProductPrice,
                combo.getMoTa(),
                combo.getHinhAnh(),
                imageUrls,
                combo.getTrangThai(),
                statusLabel(combo.getTrangThai()),
                products,
                combo.getGhiChu()
        );
    }

    private SanPhamComboDoiTacResponse toProductResponse(SanPham product, Integer quantityInCombo) {
        BigDecimal lineTotal = quantityInCombo == null
                ? null
                : safePrice(product).multiply(BigDecimal.valueOf(quantityInCombo));

        return SanPhamComboDoiTacResponse.builder()
                .maSanPham(product.getMaSanPham())
                .tenSanPham(product.getTenSanPham())
                .giaTien(product.getGiaTien())
                .hinhAnh(product.getHinhAnh())
                .soLuong(product.getSoLuong())
                .soLuongTrongCombo(quantityInCombo)
                .thanhTien(lineTotal)
                .trangThai(product.getTrangThai())
                .build();
    }

    private BigDecimal safePrice(SanPham product) {
        return product.getGiaTien() == null ? BigDecimal.ZERO : product.getGiaTien();
    }

    private int normalizeQuantity(Integer quantity) {
        return quantity == null || quantity <= 0 ? 1 : quantity;
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
            throw badRequest("Trạng thái combo không hợp lệ");
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

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record ValidatedComboItem(SanPham product, int quantity) {
    }

    private List<String> uploadComboImages(
            List<MultipartFile> files
    ) {
        List<String> imageUrls = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return imageUrls;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            try {
                String imageUrl = cloudinaryService.upload(file, "combo");
                imageUrls.add(imageUrl);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Không thể lưu ảnh "
                                + file.getOriginalFilename(),
                        exception
                );
            }
        }

        return imageUrls;
    }
    private void saveComboImages(
            ComBo combo,
            List<MultipartFile> files,
            List<String> imageUrls
    ) {
        comboHinhAnhRepository.deleteByComboId(combo.getComboId());

        List<MultipartFile> realFiles = files == null
                ? List.of()
                : files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .toList();

        List<ComBoHinhAnh> images = new ArrayList<>();
        for (int index = 0; index < imageUrls.size(); index++) {
            String originalName = index < realFiles.size()
                    ? realFiles.get(index).getOriginalFilename()
                    : null;

            ComBoHinhAnh image = new ComBoHinhAnh();
            image.setComboId(combo.getComboId());
            image.setHinhAnh(imageUrls.get(index));
            image.setTenHinhAnh(originalName);
            image.setThuTu(index + 1);
            images.add(image);
        }

        if (!images.isEmpty()) {
            comboHinhAnhRepository.saveAll(images);
            comboHinhAnhRepository.flush();
        }
    }

    private void syncLegacyImage(ComBo combo, String imageUrl) {
        String normalized = trimToNull(imageUrl);
        comboHinhAnhRepository.deleteByComboId(combo.getComboId());

        if (normalized != null) {
            ComBoHinhAnh image = new ComBoHinhAnh();
            image.setComboId(combo.getComboId());
            image.setHinhAnh(normalized);
            image.setTenHinhAnh("Ảnh combo");
            image.setThuTu(1);
            comboHinhAnhRepository.save(image);
        }
    }

}
