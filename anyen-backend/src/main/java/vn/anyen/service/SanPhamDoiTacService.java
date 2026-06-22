package vn.anyen.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.SanPhamDoiTacPageResponse;
import vn.anyen.dto.SanPhamDoiTacResponse;
import vn.anyen.dto.request.SanPhamDoiTacRequest;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.SanPhamDoiTacRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongBaoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class SanPhamDoiTacService {

    private static final String TRANG_THAI_DANG_BAN = "Đang bán";
    private static final String TRANG_THAI_AN = "Ẩn";

    private final SanPhamDoiTacRepository sanPhamDoiTacRepository;
    private final DoiTacRepository doiTacRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ThongBaoRepository thongBaoRepository;
    private final vn.anyen.repository.SanPhamChiTietRepository sanPhamChiTietRepository;
    private final vn.anyen.repository.SanPhamHinhAnhRepository sanPhamHinhAnhRepository;

    @Transactional(readOnly = true)
    public SanPhamDoiTacPageResponse getSanPhamDoiTac(
            Authentication authentication,
            String keyword,
            String loai,
            String vatLieu,
            String tonGiao,
            String mauSac,
            String trangThai,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            int page,
            int pageSize
    ) {
        Integer maDoiTac = getMaDoiTac(authentication);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(pageSize, 1), buildSort(sortBy));

        Specification<SanPham> spec = buildSpec(
                maDoiTac,
                keyword,
                loai,
                vatLieu,
                tonGiao,
                mauSac,
                trangThai,
                minPrice,
                maxPrice
        );

        Page<SanPham> result = sanPhamDoiTacRepository.findAll(spec, pageable);

        return SanPhamDoiTacPageResponse.builder()
                .items(result.getContent().stream().map(this::toResponse).toList())
                .total(result.getTotalElements())
                .page(result.getNumber())
                .pageSize(result.getSize())
                .build();
    }

    public SanPhamDoiTacResponse createSanPham(Authentication authentication, SanPhamDoiTacRequest request) {
        Integer maDoiTac = getMaDoiTac(authentication);
        validateRequest(request, true);

        SanPham sanPham = new SanPham();
        applyRequest(sanPham, request);
        sanPham.setMaDoiTac(maDoiTac);
        sanPham.setHienThi(false);

        if (isBlank(sanPham.getTrangThai())) {
            sanPham.setTrangThai(TRANG_THAI_DANG_BAN);
        }
        if (sanPham.getSoLuong() == null) {
            sanPham.setSoLuong(0);
        }

        SanPham savedSanPham = sanPhamDoiTacRepository.save(sanPham);

        saveChiTietVaHinhAnh(savedSanPham.getMaSanPham(), request);

        // Tạo thông báo cho Admin
        List<vn.anyen.entity.NhanVien> admins = nhanVienRepository.findByVaiTro("Admin");
        for (vn.anyen.entity.NhanVien admin : admins) {
            vn.anyen.entity.ThongBao thongBao = vn.anyen.entity.ThongBao.builder()
                    .tieuDe("Sản phẩm đối tác mới cần duyệt")
                    .noiDung("Đối tác vừa tạo sản phẩm: " + savedSanPham.getTenSanPham() + ". Vui lòng kiểm tra và duyệt.")
                    .loaiThongBao("HE_THONG")
                    .nguoiNhanId(admin.getMaNhanVien())
                    .trangThai("CHUA_DOC")
                    .build();
            thongBaoRepository.save(thongBao);
        }

        return toResponse(savedSanPham);
    }

    public SanPhamDoiTacResponse updateSanPham(Authentication authentication, Integer id, SanPhamDoiTacRequest request) {
        validateRequest(request, false);
        SanPham sanPham = getSanPhamCuaDoiTac(authentication, id);

        applyRequest(sanPham, request);
        SanPham savedSanPham = sanPhamDoiTacRepository.save(sanPham);
        
        sanPhamChiTietRepository.deleteByMaSanPham(id);
        sanPhamHinhAnhRepository.deleteByMaSanPham(id);
        saveChiTietVaHinhAnh(id, request);

        return toResponse(savedSanPham);
    }

    public SanPhamDoiTacResponse updateTonKho(Authentication authentication, Integer id, Integer soLuong) {
        if (soLuong == null || soLuong < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không hợp lệ");
        }

        SanPham sanPham = getSanPhamCuaDoiTac(authentication, id);
        sanPham.setSoLuong(soLuong);
        return toResponse(sanPhamDoiTacRepository.save(sanPham));
    }

    public SanPhamDoiTacResponse anSanPham(Authentication authentication, Integer id) {
        SanPham sanPham = getSanPhamCuaDoiTac(authentication, id);
        sanPham.setTrangThai(TRANG_THAI_AN);
        return toResponse(sanPhamDoiTacRepository.save(sanPham));
    }

    public SanPhamDoiTacResponse hienSanPham(Authentication authentication, Integer id) {
        SanPham sanPham = getSanPhamCuaDoiTac(authentication, id);
        sanPham.setTrangThai(TRANG_THAI_DANG_BAN);
        return toResponse(sanPhamDoiTacRepository.save(sanPham));
    }

    private SanPham getSanPhamCuaDoiTac(Authentication authentication, Integer maSanPham) {
        Integer maDoiTac = getMaDoiTac(authentication);

        SanPham sanPham = sanPhamDoiTacRepository.findById(maSanPham)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        if (!maDoiTac.equals(sanPham.getMaDoiTac())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thao tác sản phẩm này");
        }

        return sanPham;
    }

    private Integer getMaDoiTac(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập bằng tài khoản đối tác");
        }

        String tenDangNhap = authentication.getName();

        DoiTac doiTac = doiTacRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản hiện tại không phải đối tác"));

        return doiTac.getMaDoiTac();
    }

    private Specification<SanPham> buildSpec(
            Integer maDoiTac,
            String keyword,
            String loai,
            String vatLieu,
            String tonGiao,
            String mauSac,
            String trangThai,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("maDoiTac"), maDoiTac));

            if (!isBlank(keyword)) {
                String kw = like(keyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("tenSanPham")), kw),
                        cb.like(cb.lower(root.get("loai")), kw),
                        cb.like(cb.lower(root.get("vatLieu")), kw),
                        cb.like(cb.lower(root.get("mauSac")), kw)
                ));
            }

            if (!isBlank(loai)) {
                predicates.add(cb.like(cb.lower(root.get("loai")), like(loai)));
            }

            if (!isBlank(vatLieu)) {
                predicates.add(buildMultiLikePredicate(root.get("vatLieu"), vatLieu, cb));
            }

            if (!isBlank(tonGiao)) {
                predicates.add(buildMultiLikePredicate(root.get("tonGiao"), tonGiao, cb));
            }

            if (!isBlank(mauSac)) {
                predicates.add(cb.like(cb.lower(root.get("mauSac")), like(mauSac)));
            }

            if (!isBlank(trangThai) && !"ALL".equalsIgnoreCase(trangThai)) {
                predicates.add(cb.equal(cb.lower(root.get("trangThai")), trangThai.toLowerCase(Locale.ROOT)));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("giaTien"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("giaTien"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Predicate buildMultiLikePredicate(jakarta.persistence.criteria.Path<String> path, String rawValue, jakarta.persistence.criteria.CriteriaBuilder cb) {
        List<Predicate> parts = new ArrayList<>();
        for (String item : rawValue.split(",")) {
            if (!isBlank(item)) {
                parts.add(cb.like(cb.lower(path), like(item)));
            }
        }
        return cb.or(parts.toArray(new Predicate[0]));
    }

    private Sort buildSort(String sortBy) {
        if ("priceAsc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "giaTien");
        }
        if ("priceDesc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "giaTien");
        }
        if ("stockAsc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "soLuong");
        }
        if ("stockDesc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "soLuong");
        }
        return Sort.by(Sort.Direction.DESC, "maSanPham");
    }

    private void validateRequest(SanPhamDoiTacRequest request, boolean create) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu sản phẩm không hợp lệ");
        }

        if (create && isBlank(request.getTenSanPham())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên sản phẩm không được để trống");
        }

        if (create && isBlank(request.getLoai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại sản phẩm không được để trống");
        }

        if (request.getGiaTien() != null && request.getGiaTien().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá tiền không hợp lệ");
        }

        if (request.getSoLuong() != null && request.getSoLuong() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng không hợp lệ");
        }
    }

    private void applyRequest(SanPham sanPham, SanPhamDoiTacRequest request) {
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setLoai(request.getLoai());
        sanPham.setNoiThat(request.getNoiThat());
        sanPham.setQuyCach(request.getQuyCach());
        sanPham.setTonGiao(request.getTonGiao());
        sanPham.setGiaTien(request.getGiaTien());
        sanPham.setSoLuong(request.getSoLuong());
        sanPham.setThietKe(request.getThietKe());
        sanPham.setXuatXu(request.getXuatXu());
        sanPham.setGhiChu(request.getGhiChu());
        sanPham.setKhuyenMai(request.getKhuyenMai());
        sanPham.setMauSac(request.getMauSac());
        sanPham.setHinhAnh(request.getHinhAnh());
        sanPham.setVatLieu(request.getVatLieu());
        sanPham.setTrangThai(isBlank(request.getTrangThai()) ? TRANG_THAI_DANG_BAN : request.getTrangThai());
        sanPham.setKichThuoc(request.getKichThuoc());
        sanPham.setTrongLuong(request.getTrongLuong());
        sanPham.setCnsx(request.getCnsx());
    }

    private void saveChiTietVaHinhAnh(Integer maSanPham, SanPhamDoiTacRequest request) {
        if (request.getChiTietList() != null && !request.getChiTietList().isEmpty()) {
            for (var c : request.getChiTietList()) {
                vn.anyen.entity.SanPhamChiTiet ct = vn.anyen.entity.SanPhamChiTiet.builder()
                        .maSanPham(maSanPham)
                        .loaiKhoi(c.getLoaiKhoi())
                        .noiDung(c.getNoiDung())
                        .thuTu(c.getThuTu())
                        .build();
                sanPhamChiTietRepository.save(ct);
            }
        }

        if (request.getHinhAnhList() != null && !request.getHinhAnhList().isEmpty()) {
            for (var h : request.getHinhAnhList()) {
                vn.anyen.entity.SanPhamHinhAnh ha = vn.anyen.entity.SanPhamHinhAnh.builder()
                        .maSanPham(maSanPham)
                        .loaiHinhAnh(h.getLoaiHinhAnh())
                        .urlHinhAnh(h.getUrlHinhAnh())
                        .thuTu(h.getThuTu())
                        .build();
                sanPhamHinhAnhRepository.save(ha);
            }
        }
    }

    private SanPhamDoiTacResponse toResponse(SanPham sanPham) {
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
                .hinhAnh(sanPham.getHinhAnh())
                .vatLieu(sanPham.getVatLieu())
                .trangThai(sanPham.getTrangThai())
                .kichThuoc(sanPham.getKichThuoc())
                .trongLuong(sanPham.getTrongLuong())
                .cnsx(sanPham.getCnsx())
                .build();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
