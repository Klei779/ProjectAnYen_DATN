package vn.anyen.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.constants.AppLabels;
import vn.anyen.dto.SanPhamFilterOptionResponse;
import vn.anyen.dto.SanPhamFilterResponse;
import vn.anyen.dto.SanPhamPageResponse;
import vn.anyen.dto.SanPhamResponse;
import vn.anyen.dto.request.SanPhamRequest;
import vn.anyen.dto.response.SanPhamTaoDonHangResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.SanPhamRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final DoiTacRepository doiTacRepository;
    private final DoiTacThongBaoService doiTacThongBaoService;

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

            // Chỉ load sản phẩm đã được duyệt (TRANG_THAI_DANG_BAN = 1)
            predicates.add(cb.equal(root.get("trangThai"), SanPham.TRANG_THAI_DANG_BAN));

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
                        cb.equal(
                                cb.lower(root.get("loai")),
                                loai.trim().toLowerCase()
                        )
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
                        cb.equal(
                                cb.lower(root.get("mauSac")),
                                mauSac.trim().toLowerCase()
                        )
                );
            }

            if (minPrice != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("giaTien"), minPrice)
                );
            }

            if (maxPrice != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("giaTien"), maxPrice)
                );
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

    public SanPhamFilterResponse getBoLocSanPham() {
        return SanPhamFilterResponse.builder()
                .categories(mapOptions(sanPhamRepository.countVisibleByLoai()))
                .materials(mapOptions(sanPhamRepository.countVisibleByVatLieu()))
                .religions(mapOptions(sanPhamRepository.countVisibleByTonGiao()))
                .colors(mapOptions(sanPhamRepository.countVisibleByMauSac()))
                .build();
    }

    public SanPhamResponse getSanPhamById(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return mapToResponse(sp);
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
        sp.setCnsx(request.getCnsx());

        SanPham saved = sanPhamRepository.save(sp);

        return mapToResponse(saved);
    }

    public SanPhamResponse anSanPham(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTrangThai(SanPham.TRANG_THAI_AN);

        SanPham saved = sanPhamRepository.save(sp);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SanPhamPageResponse getSanPhamChoDuyet(Integer page, Integer pageSize) {
        int pageIndex = page == null || page < 1 ? 0 : page - 1;
        int size = pageSize == null || pageSize < 1 ? 16 : pageSize;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "maSanPham"));

        // Chỉ so sánh với hằng số Integer chuẩn chỉnh
        Specification<SanPham> spec = (root, query, cb) ->
                cb.equal(root.get("trangThai"), SanPham.TRANG_THAI_CHO_XAC_NHAN);

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

    public SanPhamResponse duyetSanPham(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTrangThai(SanPham.TRANG_THAI_DANG_BAN);
        SanPham saved = sanPhamRepository.save(sp);
        try {
            doiTacThongBaoService.taoThongBaoDuyetSanPham(saved);
        } catch (Exception e) {
            // Bao bọc trong try-catch để nếu lỗi gửi thông báo (ví dụ lỗi DB thông báo)
            // thì hành động từ chối sản phẩm chính vẫn thành công, tránh nghẽn hệ thống.
            System.err.println("Lỗi phát sinh khi tạo thông báo từ chối sản phẩm: " + e.getMessage());
        }

        // 4. Trả về Response cho Frontend
        return mapToResponse(saved);
    }


    public SanPhamResponse tuChoiSanPham(Integer id, String lyDoTuChoi) {
        // 1. Tìm sản phẩm
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // 2. Cập nhật trạng thái sản phẩm sang 4 (Từ chối)
        sp.setTrangThai(4);

        // Lưu lý do trực tiếp vào ghi chú của sản phẩm nếu cần thiết
        sp.setGhiChu("Từ chối duyệt. Lý do: " + lyDoTuChoi);

        SanPham saved = sanPhamRepository.save(sp);

        // 3. TÍCH HỢP: Gọi hàm gửi thông báo đến đối tác
        try {
            doiTacThongBaoService.taoThongBaoTuChoiSanPham(saved, lyDoTuChoi);
        } catch (Exception e) {
            // Bao bọc trong try-catch để nếu lỗi gửi thông báo (ví dụ lỗi DB thông báo)
            // thì hành động từ chối sản phẩm chính vẫn thành công, tránh nghẽn hệ thống.
            System.err.println("Lỗi phát sinh khi tạo thông báo từ chối sản phẩm: " + e.getMessage());
        }

        // 4. Trả về Response cho Frontend
        return mapToResponse(saved);
    }




    public List<SanPhamTaoDonHangResponse> getSanPhamTaoDonHangOptions() {
        List<SanPham> sanPhams =
                sanPhamRepository.findAllVisibleForTaoDonHang();

        Map<Integer, DoiTac> doiTacMap = doiTacRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        DoiTac::getMaDoiTac,
                        Function.identity()
                ));

        return sanPhams.stream()
                .map(sp -> {
                    DoiTac doiTac = doiTacMap.get(sp.getMaDoiTac());

                    return SanPhamTaoDonHangResponse.builder()
                            .maSanPham(sp.getMaSanPham())
                            .tenSanPham(sp.getTenSanPham())
                            .loai(sp.getLoai())
                            .giaTien(sp.getGiaTien())
                            .tonKho(sp.getSoLuong() == null ? 0 : sp.getSoLuong())
                            .maDoiTac(sp.getMaDoiTac())
                            .tenDoiTac(
                                    doiTac != null
                                            ? doiTac.getTenDoiTac()
                                            : "Không rõ đối tác"
                            )
                            .hinhAnh(sp.getHinhAnh())
                            .trangThai(sp.getTrangThai())
                            .build();
                })
                .toList();
    }

    private List<SanPhamFilterOptionResponse> mapOptions(List<Object[]> rows) {
        return rows.stream()
                .map(row -> {
                    String name = String.valueOf(row[0]);
                    Long total = ((Number) row[1]).longValue();

                    return SanPhamFilterOptionResponse.builder()
                            .id(name)
                            .name(name)
                            .total(total)
                            .build();
                })
                .toList();
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
        String tenDT = (sp.getMaDoiTac() != null) ? sanPhamRepository.findTenDoiTacByMaDoiTac(sp.getMaDoiTac()) : "Không rõ đối tác";
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
                .code("SP" + String.format("%05d", sp.getMaSanPham()))
                .quyCach(sp.getQuyCach())
                .kichThuoc(sp.getKichThuoc())
                .trongLuong(sp.getTrongLuong())
                .xuatXu(sp.getXuatXu())
                .nhaCungCap(sp.getMaDoiTac() != null ? "Đối tác #" + sp.getMaDoiTac() : "N/A")
                .nhaSanXuat(sp.getCnsx())
                .tenTrangThai(AppLabels.getLabel(AppLabels.TRANG_THAI_SAN_PHAM, sp.getTrangThai()))
                .tenDoiTac(tenDT)
                .soLuong(sp.getSoLuong())
                .ngayCapNhat("N/A")
                .discount(sp.getKhuyenMai() != null && sp.getGiaTien() != null 
                    ? sp.getGiaTien().subtract(sp.getKhuyenMai()) 
                    : null)
                .moTa(sp.getGhiChu())
                .huongDanBaoQuan("Sản phẩm nên được bảo quản ở nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp và độ ẩm cao.")
                .build();
    }
}