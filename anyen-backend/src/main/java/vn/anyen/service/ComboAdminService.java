package vn.anyen.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.ComboAdminRequest;
import vn.anyen.dto.response.ComboAdminResponse;
import vn.anyen.dto.response.SanPhamComboAdminResponse;
import vn.anyen.entity.ComBo;
import vn.anyen.entity.ComBoChiTiet;
import vn.anyen.entity.ComBoHinhAnh;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.ComBoChiTietRepository;
import vn.anyen.repository.ComBoHinhAnhRepository;
import vn.anyen.repository.ComBoRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.SanPhamRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ComboAdminService {

    private static final int SO_ANH_DAI_DIEN_BAT_BUOC = 3;
    private static final int SO_ANH_QUY_TRINH_TOI_DA = 20;
    private static final long DUNG_LUONG_ANH_TOI_DA = 5L * 1024 * 1024;
    private static final Set<String> DINH_DANG_ANH_HOP_LE = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final ComBoRepository comboRepository;
    private final ComBoChiTietRepository comboChiTietRepository;
    private final ComBoHinhAnhRepository comboHinhAnhRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DoiTacRepository doiTacRepository;
    private final NhanVienRepository nhanVienRepository;
    private final CloudinaryService cloudinaryService;

    public ComboAdminService(
            ComBoRepository comboRepository,
            ComBoChiTietRepository comboChiTietRepository,
            ComBoHinhAnhRepository comboHinhAnhRepository,
            SanPhamRepository sanPhamRepository,
            DoiTacRepository doiTacRepository,
            NhanVienRepository nhanVienRepository,
            CloudinaryService cloudinaryService
    ) {
        this.comboRepository = comboRepository;
        this.comboChiTietRepository = comboChiTietRepository;
        this.comboHinhAnhRepository = comboHinhAnhRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.doiTacRepository = doiTacRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public List<ComboAdminResponse> getCombos(Authentication authentication) {
        requireAdmin(authentication);
        return comboRepository.findAllByOrderByComboIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComboAdminResponse getCombo(
            Authentication authentication,
            Integer comboId
    ) {
        requireAdmin(authentication);
        return toResponse(findCombo(comboId));
    }

    @Transactional(readOnly = true)
    public List<SanPhamComboAdminResponse> getSanPhamCoTheChon(
            Authentication authentication
    ) {
        requireAdmin(authentication);

        List<SanPham> products = sanPhamRepository.findAllAvailableForAdminCombo();
        Map<Integer, String> partnerNames = loadPartnerNames(products);

        return products.stream()
                .map(product -> toProductResponse(product, null, partnerNames))
                .toList();
    }

    @Transactional
    public ComboAdminResponse createCombo(
            Authentication authentication,
            ComboAdminRequest request,
            List<MultipartFile> anhDaiDien,
            List<MultipartFile> anhQuyTrinh
    ) {
        NhanVien admin = requireAdmin(authentication);
        List<ValidatedComboItem> items = validateProducts(request);
        validateComboPrice(request.getGia(), items);

        List<MultipartFile> coverFiles = realFiles(anhDaiDien);
        List<MultipartFile> processFiles = realFiles(anhQuyTrinh);
        validateCoverImages(coverFiles);
        validateProcessImages(processFiles);

        ComBo combo = new ComBo();
        combo.setMaDoiTac(null);
        combo.setMaNhanVienTao(admin.getMaNhanVien());
        applyRequest(combo, request);

        ComBo saved = comboRepository.save(combo);
        saveDetails(saved, items, request);

        List<String> coverUrls = replaceImageGroup(
                saved.getComboId(),
                ComBoHinhAnh.LOAI_DAI_DIEN,
                coverFiles,
                "combo-dai-dien"
        );

        if (!processFiles.isEmpty()) {
            replaceImageGroup(
                    saved.getComboId(),
                    ComBoHinhAnh.LOAI_QUY_TRINH,
                    processFiles,
                    "combo-quy-trinh"
            );
        }

        saved.setHinhAnh(coverUrls.get(0));
        saved = comboRepository.save(saved);
        return toResponse(saved);
    }

    @Transactional
    public ComboAdminResponse updateCombo(
            Authentication authentication,
            Integer comboId,
            ComboAdminRequest request,
            List<MultipartFile> anhDaiDien,
            List<MultipartFile> anhQuyTrinh
    ) {
        NhanVien admin = requireAdmin(authentication);
        ComBo combo = findCombo(comboId);
        List<ValidatedComboItem> items = validateProducts(request);
        validateComboPrice(request.getGia(), items);

        List<MultipartFile> coverFiles = realFiles(anhDaiDien);
        List<MultipartFile> processFiles = realFiles(anhQuyTrinh);

        boolean replaceCovers = Boolean.TRUE.equals(request.getThayAnhDaiDien())
                || !coverFiles.isEmpty();
        boolean replaceProcesses = Boolean.TRUE.equals(request.getThayAnhQuyTrinh())
                || !processFiles.isEmpty();

        if (replaceCovers) {
            validateCoverImages(coverFiles);
        } else {
            long currentCoverCount = comboHinhAnhRepository
                    .countByComboIdAndLoaiHinhAnh(
                            comboId,
                            ComBoHinhAnh.LOAI_DAI_DIEN
                    );
            if (currentCoverCount != SO_ANH_DAI_DIEN_BAT_BUOC) {
                throw badRequest(
                        "Combo hiện chưa đủ 3 ảnh đại diện. Vui lòng chọn đúng 3 ảnh mới trước khi lưu"
                );
            }
        }

        if (replaceProcesses) {
            validateProcessImages(processFiles);
        }

        if (combo.getMaNhanVienTao() == null) {
            combo.setMaNhanVienTao(admin.getMaNhanVien());
        }
        combo.setMaDoiTac(null);
        applyRequest(combo, request);
        ComBo saved = comboRepository.save(combo);

        comboChiTietRepository.deleteByComboId(comboId);
        comboChiTietRepository.flush();
        saveDetails(saved, items, request);

        if (replaceCovers) {
            List<String> coverUrls = replaceImageGroup(
                    comboId,
                    ComBoHinhAnh.LOAI_DAI_DIEN,
                    coverFiles,
                    "combo-dai-dien"
            );
            saved.setHinhAnh(coverUrls.get(0));
            saved = comboRepository.save(saved);
        } else {
            syncLegacyCover(saved);
        }

        if (replaceProcesses) {
            replaceImageGroup(
                    comboId,
                    ComBoHinhAnh.LOAI_QUY_TRINH,
                    processFiles,
                    "combo-quy-trinh"
            );
        }

        return toResponse(saved);
    }

    @Transactional
    public ComboAdminResponse updateTrangThai(
            Authentication authentication,
            Integer comboId,
            Integer trangThai
    ) {
        requireAdmin(authentication);
        ComBo combo = findCombo(comboId);
        Integer normalizedStatus = normalizeStatus(trangThai);
        if (ComBo.TT_HOAT_DONG.equals(normalizedStatus)) {
            validateCanActivate(comboId);
        }
        combo.setTrangThai(normalizedStatus);
        return toResponse(comboRepository.save(combo));
    }

    private void validateCanActivate(Integer comboId) {
        long coverCount = comboHinhAnhRepository.countByComboIdAndLoaiHinhAnh(
                comboId,
                ComBoHinhAnh.LOAI_DAI_DIEN
        );
        if (coverCount != SO_ANH_DAI_DIEN_BAT_BUOC) {
            throw badRequest(
                    "Không thể bật combo khi chưa có đúng 3 ảnh đại diện"
            );
        }

        boolean hasSelectedProduct = comboChiTietRepository
                .findByComboId(comboId)
                .stream()
                .anyMatch(detail ->
                        ComBoChiTiet.LOAI_SAN_PHAM.equals(detail.getLoai())
                                && detail.getMaSanPham() != null
                );
        if (!hasSelectedProduct) {
            throw badRequest(
                    "Không thể bật combo khi chưa tick ít nhất một sản phẩm"
            );
        }
    }

    private void applyRequest(ComBo combo, ComboAdminRequest request) {
        combo.setTenCombo(request.getTenCombo().trim());
        combo.setGia(request.getGia());
        combo.setMoTa(trimToNull(request.getMoTa()));
        combo.setGhiChu(trimToNull(request.getGhiChu()));
        combo.setTrangThai(normalizeStatus(request.getTrangThai()));
    }

    private List<ValidatedComboItem> validateProducts(ComboAdminRequest request) {
        LinkedHashMap<Integer, Integer> requestedItems = new LinkedHashMap<>();

        if (request.getSanPhams() != null) {
            for (ComboAdminRequest.ComboSanPhamRequest item : request.getSanPhams()) {
                if (item == null || item.getMaSanPham() == null) {
                    throw badRequest("Danh sách sản phẩm được tick không hợp lệ");
                }
                if (item.getSoLuong() == null || item.getSoLuong() <= 0) {
                    throw badRequest("Số lượng sản phẩm trong combo phải lớn hơn 0");
                }
                if (requestedItems.putIfAbsent(
                        item.getMaSanPham(),
                        item.getSoLuong()
                ) != null) {
                    throw badRequest(
                            "Sản phẩm mã " + item.getMaSanPham() + " bị tick trùng trong combo"
                    );
                }
            }
        }

        if (requestedItems.isEmpty()) {
            throw badRequest("Combo phải có ít nhất một sản phẩm được tick chọn");
        }

        Map<Integer, SanPham> productMap = sanPhamRepository
                .findAllById(requestedItems.keySet())
                .stream()
                .collect(Collectors.toMap(
                        SanPham::getMaSanPham,
                        Function.identity()
                ));

        Set<Integer> partnerIds = productMap.values().stream()
                .map(SanPham::getMaDoiTac)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, DoiTac> partnerMap = doiTacRepository
                .findAllById(partnerIds)
                .stream()
                .collect(Collectors.toMap(
                        DoiTac::getMaDoiTac,
                        Function.identity()
                ));

        List<ValidatedComboItem> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : requestedItems.entrySet()) {
            SanPham product = productMap.get(entry.getKey());
            if (product == null) {
                throw badRequest("Không tìm thấy sản phẩm có mã " + entry.getKey());
            }
            if (!SanPham.TRANG_THAI_DANG_BAN.equals(product.getTrangThai())) {
                throw badRequest(
                        "Sản phẩm " + product.getTenSanPham() + " chưa ở trạng thái đang bán"
                );
            }

            DoiTac partner = partnerMap.get(product.getMaDoiTac());
            if (partner == null
                    || !DoiTac.TT_DANG_HOAT_DONG.equals(partner.getTrangThai())) {
                throw badRequest(
                        "Đối tác của sản phẩm " + product.getTenSanPham() + " không còn hoạt động"
                );
            }

            int stock = Math.max(0, product.getSoLuong() == null ? 0 : product.getSoLuong());
            int quantity = entry.getValue();
            if (quantity > stock) {
                throw badRequest(
                        "Số lượng " + product.getTenSanPham()
                                + " trong combo (" + quantity + ") vượt tồn kho hiện tại ("
                                + stock + ")"
                );
            }

            result.add(new ValidatedComboItem(product, quantity));
        }
        return result;
    }

    private void validateComboPrice(
            BigDecimal comboPrice,
            List<ValidatedComboItem> items
    ) {
        if (comboPrice == null || comboPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Giá combo phải lớn hơn 0");
        }

    }

    private void validateCoverImages(List<MultipartFile> files) {
        if (files.size() != SO_ANH_DAI_DIEN_BAT_BUOC) {
            throw badRequest("Ảnh đại diện của combo bắt buộc phải đúng 3 ảnh");
        }
        validateImageFiles(files, SO_ANH_DAI_DIEN_BAT_BUOC, "ảnh đại diện");
    }

    private void validateProcessImages(List<MultipartFile> files) {
        validateImageFiles(files, SO_ANH_QUY_TRINH_TOI_DA, "ảnh quy trình");
    }

    private void validateImageFiles(
            List<MultipartFile> files,
            int maxFiles,
            String groupName
    ) {
        if (files.size() > maxFiles) {
            throw badRequest(
                    "Combo chỉ được chọn tối đa " + maxFiles + " " + groupName
            );
        }

        for (MultipartFile file : files) {
            if (file.getSize() > DUNG_LUONG_ANH_TOI_DA) {
                throw badRequest(
                        "Ảnh " + safeFilename(file) + " vượt quá 5 MB"
                );
            }

            String contentType = file.getContentType();
            if (contentType == null
                    || !DINH_DANG_ANH_HOP_LE.contains(contentType.toLowerCase())) {
                throw badRequest(
                        "Ảnh " + safeFilename(file)
                                + " không đúng định dạng JPG, PNG hoặc WEBP"
                );
            }
        }
    }

    private List<String> replaceImageGroup(
            Integer comboId,
            String imageType,
            List<MultipartFile> files,
            String uploadFolder
    ) {
        comboHinhAnhRepository.deleteByComboIdAndLoaiHinhAnh(comboId, imageType);
        comboHinhAnhRepository.flush();

        if (files.isEmpty()) {
            return List.of();
        }

        List<String> urls = new ArrayList<>();
        List<ComBoHinhAnh> images = new ArrayList<>();

        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String imageUrl;
            try {
                imageUrl = cloudinaryService.upload(file, uploadFolder);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Không thể lưu ảnh " + safeFilename(file),
                        exception
                );
            }

            urls.add(imageUrl);
            images.add(new ComBoHinhAnh(
                    null,
                    comboId,
                    imageType,
                    imageUrl,
                    safeFilename(file),
                    index + 1
            ));
        }

        comboHinhAnhRepository.saveAll(images);
        comboHinhAnhRepository.flush();
        return urls;
    }

    private void saveDetails(
            ComBo combo,
            List<ValidatedComboItem> items,
            ComboAdminRequest request
    ) {
        Map<Integer, String> descriptions = new HashMap<>();
        for (ComboAdminRequest.ComboSanPhamRequest item : request.getSanPhams()) {
            String description = trimToNull(item.getNoiDung());
            if (description != null) {
                descriptions.put(item.getMaSanPham(), description);
            }
        }

        List<ComBoChiTiet> details = items.stream()
                .map(item -> {
                    ComBoChiTiet detail = new ComBoChiTiet();
                    detail.setComboId(combo.getComboId());
                    detail.setMaSanPham(item.product().getMaSanPham());
                    detail.setLoai(ComBoChiTiet.LOAI_SAN_PHAM);
                    detail.setSoLuong(item.quantity());
                    detail.setNoiDung(descriptions.getOrDefault(
                            item.product().getMaSanPham(),
                            buildDefaultProductDescription(item.product())
                    ));
                    return detail;
                })
                .toList();

        comboChiTietRepository.saveAll(details);
        comboChiTietRepository.flush();
    }

    private String buildDefaultProductDescription(SanPham product) {
        String material = trimToNull(product.getVatLieu());
        if (material == null) {
            return product.getTenSanPham();
        }
        String result = product.getTenSanPham() + " - " + material;
        return result.length() <= 255 ? result : result.substring(0, 255);
    }

    private ComboAdminResponse toResponse(ComBo combo) {
        List<ComBoChiTiet> details = comboChiTietRepository
                .findByComboId(combo.getComboId())
                .stream()
                .filter(detail -> ComBoChiTiet.LOAI_SAN_PHAM.equals(detail.getLoai()))
                .filter(detail -> detail.getMaSanPham() != null)
                .toList();

        List<Integer> productIds = details.stream()
                .map(ComBoChiTiet::getMaSanPham)
                .distinct()
                .toList();

        Map<Integer, SanPham> productMap = sanPhamRepository
                .findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(
                        SanPham::getMaSanPham,
                        Function.identity()
                ));

        Map<Integer, String> partnerNames = loadPartnerNames(
                new ArrayList<>(productMap.values())
        );

        List<SanPhamComboAdminResponse> products = details.stream()
                .map(detail -> {
                    SanPham product = productMap.get(detail.getMaSanPham());
                    if (product == null) {
                        return null;
                    }
                    return toProductResponse(
                            product,
                            normalizeQuantity(detail.getSoLuong()),
                            partnerNames
                    );
                })
                .filter(item -> item != null)
                .toList();

        BigDecimal totalProductPrice = products.stream()
                .map(item -> item.getThanhTien() == null
                        ? BigDecimal.ZERO
                        : item.getThanhTien())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> covers = imageUrls(
                combo.getComboId(),
                ComBoHinhAnh.LOAI_DAI_DIEN
        );
        if (covers.isEmpty() && trimToNull(combo.getHinhAnh()) != null) {
            covers = List.of(combo.getHinhAnh().trim());
        }

        List<String> processImages = imageUrls(
                combo.getComboId(),
                ComBoHinhAnh.LOAI_QUY_TRINH
        );

        String mainImage = covers.isEmpty()
                ? trimToNull(combo.getHinhAnh())
                : covers.get(0);

        String creatorName = combo.getMaNhanVienTao() == null
                ? "Dữ liệu combo cũ"
                : nhanVienRepository.findById(combo.getMaNhanVienTao())
                .map(NhanVien::getHoTen)
                .orElse("Không xác định");

        return new ComboAdminResponse(
                combo.getComboId(),
                combo.getMaNhanVienTao(),
                creatorName,
                combo.getTenCombo(),
                combo.getGia(),
                totalProductPrice,
                combo.getMoTa(),
                combo.getGhiChu(),
                mainImage,
                covers,
                processImages,
                combo.getTrangThai(),
                statusLabel(combo.getTrangThai()),
                products
        );
    }

    private SanPhamComboAdminResponse toProductResponse(
            SanPham product,
            Integer quantityInCombo,
            Map<Integer, String> partnerNames
    ) {
        BigDecimal lineTotal = quantityInCombo == null
                ? null
                : safePrice(product).multiply(BigDecimal.valueOf(quantityInCombo));

        return new SanPhamComboAdminResponse(
                product.getMaSanPham(),
                product.getTenSanPham(),
                defaultText(product.getLoai(), "Chưa phân loại"),
                defaultText(product.getVatLieu(), "Chưa cập nhật"),
                defaultText(product.getMauSac(), "Chưa cập nhật"),
                product.getGiaTien(),
                product.getHinhAnh(),
                product.getSoLuong(),
                quantityInCombo,
                lineTotal,
                product.getTrangThai(),
                product.getMaDoiTac(),
                partnerNames.getOrDefault(
                        product.getMaDoiTac(),
                        "Không xác định"
                )
        );
    }

    private Map<Integer, String> loadPartnerNames(List<SanPham> products) {
        Set<Integer> partnerIds = products.stream()
                .map(SanPham::getMaDoiTac)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        return doiTacRepository.findAllById(partnerIds)
                .stream()
                .collect(Collectors.toMap(
                        DoiTac::getMaDoiTac,
                        partner -> defaultText(
                                partner.getTenDoiTac(),
                                partner.getTenDoanhNghiep()
                        )
                ));
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

    private void syncLegacyCover(ComBo combo) {
        List<String> covers = imageUrls(
                combo.getComboId(),
                ComBoHinhAnh.LOAI_DAI_DIEN
        );
        if (!covers.isEmpty() && !covers.get(0).equals(combo.getHinhAnh())) {
            combo.setHinhAnh(covers.get(0));
            comboRepository.save(combo);
        }
    }

    private BigDecimal calculateTotalProductPrice(List<ValidatedComboItem> items) {
        return items.stream()
                .map(item -> safePrice(item.product())
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safePrice(SanPham product) {
        return product.getGiaTien() == null
                ? BigDecimal.ZERO
                : product.getGiaTien();
    }

    private int normalizeQuantity(Integer quantity) {
        return quantity == null || quantity <= 0 ? 1 : quantity;
    }

    private List<MultipartFile> realFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private NhanVien requireAdmin(Authentication authentication) {
        if (authentication == null || trimToNull(authentication.getName()) == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Vui lòng đăng nhập bằng tài khoản Admin"
            );
        }

        NhanVien admin = nhanVienRepository
                .findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Không tìm thấy tài khoản nhân viên"
                ));

        if (!NhanVien.VAI_TRO_ADMIN.equals(admin.getVaiTro())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ Admin mới được tạo và quản lý combo"
            );
        }
        if (!NhanVien.TRANG_THAI_HOAT_DONG.equals(admin.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản Admin đang không hoạt động"
            );
        }
        return admin;
    }

    private ComBo findCombo(Integer comboId) {
        return comboRepository.findById(comboId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy combo có mã " + comboId
                ));
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return ComBo.TT_HOAT_DONG;
        }
        if (!List.of(
                ComBo.TT_AN,
                ComBo.TT_HOAT_DONG,
                ComBo.TT_NGUNG_KINH_DOANH
        ).contains(status)) {
            throw badRequest("Trạng thái combo không hợp lệ");
        }
        return status;
    }

    private String statusLabel(Integer status) {
        if (ComBo.TT_HOAT_DONG.equals(status)) {
            return "Đang hoạt động";
        }
        if (ComBo.TT_NGUNG_KINH_DOANH.equals(status)) {
            return "Ngừng kinh doanh";
        }
        return "Đang ẩn";
    }

    private String safeFilename(MultipartFile file) {
        String filename = file == null ? null : file.getOriginalFilename();
        return trimToNull(filename) == null ? "không rõ tên" : filename.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String defaultText(String primary, String fallback) {
        String normalized = trimToNull(primary);
        if (normalized != null) {
            return normalized;
        }
        String fallbackNormalized = trimToNull(fallback);
        return fallbackNormalized == null ? "Không xác định" : fallbackNormalized;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record ValidatedComboItem(SanPham product, int quantity) {
    }
}