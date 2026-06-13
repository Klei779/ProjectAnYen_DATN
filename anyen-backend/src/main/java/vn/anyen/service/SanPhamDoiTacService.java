package vn.anyen.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.SanPhamDoiTacPageResponse;
import vn.anyen.dto.SanPhamDoiTacResponse;
import vn.anyen.dto.request.SanPhamRequest;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.SanPhamDoiTacRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamDoiTacService {

    private final SanPhamDoiTacRepository sanPhamRepository;
    private final DoiTacRepository doiTacRepository;

    public SanPhamDoiTacPageResponse getSanPhamDoiTac(
            Authentication authentication,
            String keyword,
            String loai,
            String vatLieu,
            String tonGiao,
            String mauSac,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            int page,
            int pageSize
    ) {
        Integer maDoiTac = getMaDoiTacDangNhap(authentication);

        Sort sort = buildSort(sortBy);

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(pageSize, 1),
                sort
        );

        Specification<SanPham> spec = buildSpecification(
                maDoiTac,
                keyword,
                loai,
                vatLieu,
                tonGiao,
                mauSac,
                minPrice,
                maxPrice
        );

        Page<SanPham> result = sanPhamRepository.findAll(spec, pageable);

        List<SanPhamDoiTacResponse> items = result
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return SanPhamDoiTacPageResponse.builder()
                .items(items)
                .total(result.getTotalElements())
                .build();
    }

    @Transactional
    public SanPhamDoiTacResponse taoSanPham(
            Authentication authentication,
            SanPhamRequest request
    ) {
        Integer maDoiTac = getMaDoiTacDangNhap(authentication);

        SanPham sanPham = SanPham.builder()
                .tenSanPham(request.getTenSanPham())
                .loai(request.getLoai())
                .noiThat(request.getNoiThat())
                .quyCach(request.getQuyCach())
                .tonGiao(request.getTonGiao())
                .giaTien(defaultMoney(request.getGiaTien()))
                .maDoiTac(maDoiTac)
                .soLuong(defaultInt(request.getSoLuong()))
                .thietKe(request.getThietKe())
                .xuatXu(request.getXuatXu())
                .ghiChu(request.getGhiChu())
                .khuyenMai(defaultMoney(request.getKhuyenMai()))
                .mauSac(request.getMauSac())
                .hinhAnh(request.getHinhAnh())
                .vatLieu(request.getVatLieu())
                .trangThai(defaultTrangThai(request.getTrangThai(), request.getSoLuong()))
                .kichThuoc(request.getKichThuoc())
                .trongLuong(request.getTrongLuong())
                .cnsx(request.getCnsx())
                .build();

        return toResponse(sanPhamRepository.save(sanPham));
    }

    @Transactional
    public SanPhamDoiTacResponse capNhatSanPham(
            Authentication authentication,
            Integer maSanPham,
            SanPhamRequest request
    ) {
        Integer maDoiTac = getMaDoiTacDangNhap(authentication);

        SanPham sanPham = getSanPhamCuaDoiTac(maSanPham, maDoiTac);

        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setLoai(request.getLoai());
        sanPham.setNoiThat(request.getNoiThat());
        sanPham.setQuyCach(request.getQuyCach());
        sanPham.setTonGiao(request.getTonGiao());
        sanPham.setGiaTien(defaultMoney(request.getGiaTien()));
        sanPham.setSoLuong(defaultInt(request.getSoLuong()));
        sanPham.setThietKe(request.getThietKe());
        sanPham.setXuatXu(request.getXuatXu());
        sanPham.setGhiChu(request.getGhiChu());
        sanPham.setKhuyenMai(defaultMoney(request.getKhuyenMai()));
        sanPham.setMauSac(request.getMauSac());
        sanPham.setHinhAnh(request.getHinhAnh());
        sanPham.setVatLieu(request.getVatLieu());
        sanPham.setTrangThai(defaultTrangThai(request.getTrangThai(), request.getSoLuong()));
        sanPham.setKichThuoc(request.getKichThuoc());
        sanPham.setTrongLuong(request.getTrongLuong());
        sanPham.setCnsx(request.getCnsx());

        return toResponse(sanPhamRepository.save(sanPham));
    }

    @Transactional
    public SanPhamDoiTacResponse capNhatTonKho(
            Authentication authentication,
            Integer maSanPham,
            Integer soLuong
    ) {
        Integer maDoiTac = getMaDoiTacDangNhap(authentication);

        SanPham sanPham = getSanPhamCuaDoiTac(maSanPham, maDoiTac);

        int soLuongMoi = Math.max(soLuong == null ? 0 : soLuong, 0);
        sanPham.setSoLuong(soLuongMoi);

        if (soLuongMoi <= 0) {
            sanPham.setTrangThai("Hết hàng");
        } else if (
                sanPham.getTrangThai() == null ||
                        sanPham.getTrangThai().isBlank() ||
                        sanPham.getTrangThai().equalsIgnoreCase("Hết hàng")
        ) {
            sanPham.setTrangThai("Còn hàng");
        }

        return toResponse(sanPhamRepository.save(sanPham));
    }

    @Transactional
    public SanPhamDoiTacResponse anSanPham(
            Authentication authentication,
            Integer maSanPham
    ) {
        Integer maDoiTac = getMaDoiTacDangNhap(authentication);

        SanPham sanPham = getSanPhamCuaDoiTac(maSanPham, maDoiTac);
        sanPham.setTrangThai("Ẩn");

        return toResponse(sanPhamRepository.save(sanPham));
    }

    @Transactional
    public void xoaSanPhamMem(
            Authentication authentication,
            Integer maSanPham
    ) {
        Integer maDoiTac = getMaDoiTacDangNhap(authentication);

        SanPham sanPham = getSanPhamCuaDoiTac(maSanPham, maDoiTac);

        // Không xóa cứng để tránh lỗi khóa ngoại nếu sản phẩm đã nằm trong chi tiết đơn hàng.
        sanPham.setTrangThai("Ẩn");

        sanPhamRepository.save(sanPham);
    }

    private Integer getMaDoiTacDangNhap(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bạn chưa đăng nhập"
            );
        }

        String tenDangNhap = authentication.getName();

        DoiTac doiTac = doiTacRepository
                .findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Tài khoản hiện tại không phải đối tác"
                ));

        return doiTac.getMaDoiTac();
    }

    private SanPham getSanPhamCuaDoiTac(
            Integer maSanPham,
            Integer maDoiTac
    ) {
        return sanPhamRepository
                .findByMaSanPhamAndMaDoiTac(maSanPham, maDoiTac)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sản phẩm của đối tác này"
                ));
    }

    private Specification<SanPham> buildSpecification(
            Integer maDoiTac,
            String keyword,
            String loai,
            String vatLieu,
            String tonGiao,
            String mauSac,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("maDoiTac"), maDoiTac));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("tenSanPham")), kw),
                        cb.like(cb.lower(root.get("loai")), kw),
                        cb.like(cb.lower(root.get("vatLieu")), kw)
                ));
            }

            if (loai != null && !loai.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("loai"), loai.trim()));
            }

            if (vatLieu != null && !vatLieu.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("vatLieu")),
                        "%" + vatLieu.trim().toLowerCase() + "%"
                ));
            }

            if (tonGiao != null && !tonGiao.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("tonGiao")),
                        "%" + tonGiao.trim().toLowerCase() + "%"
                ));
            }

            if (mauSac != null && !mauSac.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("mauSac")),
                        "%" + mauSac.trim().toLowerCase() + "%"
                ));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("giaTien"),
                        minPrice
                ));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("giaTien"),
                        maxPrice
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "maSanPham");
        }

        return switch (sortBy) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "giaTien");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "giaTien");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "maSanPham");
            case "newest" -> Sort.by(Sort.Direction.DESC, "maSanPham");
            default -> Sort.by(Sort.Direction.DESC, "maSanPham");
        };
    }

    private SanPhamDoiTacResponse toResponse(SanPham sanPham) {
        String image = sanPham.getHinhAnh();

        return SanPhamDoiTacResponse.builder()
                .maSanPham(sanPham.getMaSanPham())
                .tenSanPham(sanPham.getTenSanPham())
                .loai(sanPham.getLoai())
                .noiThat(sanPham.getNoiThat())
                .quyCach(sanPham.getQuyCach())
                .tonGiao(sanPham.getTonGiao())
                .giaTien(sanPham.getGiaTien())
                .maDoiTac(sanPham.getMaDoiTac())
                .soLuong(sanPham.getSoLuong())
                .thietKe(sanPham.getThietKe())
                .xuatXu(sanPham.getXuatXu())
                .ghiChu(sanPham.getGhiChu())
                .khuyenMai(sanPham.getKhuyenMai())
                .mauSac(sanPham.getMauSac())
                .hinhAnh(image)
                .vatLieu(sanPham.getVatLieu())
                .trangThai(sanPham.getTrangThai())
                .kichThuoc(sanPham.getKichThuoc())
                .trongLuong(sanPham.getTrongLuong())
                .cnsx(sanPham.getCnsx())

                // Alias cho frontend
                .id(sanPham.getMaSanPham())
                .name(sanPham.getTenSanPham())
                .sku("SP-" + sanPham.getMaSanPham())
                .category(sanPham.getLoai())
                .price(sanPham.getGiaTien())
                .stock(defaultInt(sanPham.getSoLuong()))
                .status(sanPham.getTrangThai())
                .image(image)
                .build();
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultTrangThai(String trangThai, Integer soLuong) {
        if (trangThai != null && !trangThai.isBlank()) {
            return trangThai;
        }

        return defaultInt(soLuong) > 0 ? "Còn hàng" : "Hết hàng";
    }
}