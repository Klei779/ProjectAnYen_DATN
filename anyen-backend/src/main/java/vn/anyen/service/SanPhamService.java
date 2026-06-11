package vn.anyen.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.anyen.dto.SanPhamPageResponse;
import vn.anyen.dto.SanPhamResponse;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.SanPhamRepository;
import vn.anyen.dto.request.SanPhamRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public SanPhamPageResponse getSanPham(
            String keyword,
            String loai,
            String vatLieu,
            String tonGiao,
            String mauSac,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            Integer page,
            Integer pageSize
    ) {
        int pageIndex = page == null || page < 1 ? 0 : page - 1;
        int size = pageSize == null || pageSize < 1 ? 16 : pageSize;

        Pageable pageable = PageRequest.of(pageIndex, size, buildSort(sortBy));

        Specification<SanPham> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("tenSanPham")), kw),
                        cb.like(cb.lower(root.get("loai")), kw),
                        cb.like(cb.lower(root.get("tonGiao")), kw),
                        cb.like(cb.lower(root.get("mauSac")), kw),
                        cb.like(cb.lower(root.get("vatLieu")), kw)
                ));
            }

            if (loai != null && !loai.isBlank()) {
                predicates.add(
                        cb.equal(cb.lower(root.get("loai")), loai.trim().toLowerCase())
                );
            }

            List<String> vatLieuList = splitParam(vatLieu);
            if (!vatLieuList.isEmpty()) {
                predicates.add(cb.lower(root.get("vatLieu")).in(vatLieuList));
            }

            List<String> tonGiaoList = splitParam(tonGiao);
            if (!tonGiaoList.isEmpty()) {
                predicates.add(cb.lower(root.get("tonGiao")).in(tonGiaoList));
            }

            if (mauSac != null && !mauSac.isBlank()) {
                predicates.add(
                        cb.equal(cb.lower(root.get("mauSac")), mauSac.trim().toLowerCase())
                );
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("giaTien"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("giaTien"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SanPham> result = sanPhamRepository.findAll(spec, pageable);

        List<SanPhamResponse> items = result.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return SanPhamPageResponse.builder()
                .items(items)
                .total(result.getTotalElements())
                .build();
    }

    public SanPhamResponse updateSanPham(Integer id, SanPhamRequest request) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTenSanPham(request.getTenSanPham());
        sp.setLoai(request.getLoai());
        sp.setNoiThat(request.getNoiThat());
        sp.setQuyCach(request.getQuyCach());
        sp.setTonGiao(request.getTonGiao());
        sp.setGiaTien(request.getGiaTien());
        sp.setSoLuong(request.getSoLuong());
        sp.setThietKe(request.getThietKe());
        sp.setXuatXu(request.getXuatXu());
        sp.setGhiChu(request.getGhiChu());
        sp.setKhuyenMai(request.getKhuyenMai());
        sp.setMauSac(request.getMauSac());
        sp.setHinhAnh(request.getHinhAnh());
        sp.setVatLieu(request.getVatLieu());
        sp.setTrangThai(request.getTrangThai());
        sp.setKichThuoc(request.getKichThuoc());
        sp.setTrongLuong(request.getTrongLuong());
        sp.setCNSX(request.getCNXS());

        SanPham saved = sanPhamRepository.save(sp);

        return mapToResponse(saved);
    }

    public SanPhamResponse anSanPham(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTrangThai("Ẩn");

        SanPham saved = sanPhamRepository.save(sp);

        return mapToResponse(saved);
    }

    private List<String> splitParam(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList();
    }

    private Sort buildSort(String sortBy) {
        if ("oldest".equals(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "maSanPham");
        }

        if ("price_asc".equals(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "giaTien");
        }

        if ("price_desc".equals(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "giaTien");
        }

        return Sort.by(Sort.Direction.DESC, "maSanPham");
    }

    private SanPhamResponse mapToResponse(SanPham sp) {
        return SanPhamResponse.builder()
                .id(sp.getMaSanPham())
                .name(sp.getTenSanPham())
                .subname(sp.getLoai())
                .price(sp.getGiaTien())
                .oldPrice(sp.getKhuyenMai())
                .image(sp.getHinhAnh())
                .loai(sp.getLoai())
                .vatLieu(sp.getVatLieu())
                .tonGiao(sp.getTonGiao())
                .mauSac(sp.getMauSac())
                .trangThai(sp.getTrangThai())
                .build();
    }
}