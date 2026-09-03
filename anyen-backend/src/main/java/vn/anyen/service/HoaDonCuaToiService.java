package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.response.HoaDonCuaToiResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HoaDon;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.HoaDonCuaToiRepository;
import vn.anyen.repository.HoaDonRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.projection.HoaDonCuaToiProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HoaDonCuaToiService {

    private final HoaDonCuaToiRepository hoaDonCuaToiRepository;
    private final NhanVienRepository nhanVienRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    /**
     * Lấy danh sách hóa đơn.
     *
     * Admin được xem toàn bộ hóa đơn.
     * Nhân viên chỉ được xem hóa đơn thuộc đơn hàng mình phụ trách.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHoaDonCuaToi(
            String tenDangNhap,
            String keyword,
            String trangThai,
            String phuongThucThanhToan,
            LocalDate tuNgay,
            LocalDate denNgay,
            Integer page,
            Integer pageSize
    ) {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        NhanVien nhanVien = nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy nhân viên đăng nhập")
                );

        boolean laAdmin = isAdmin(nhanVien);

        Integer maNhanVienCanLoc = laAdmin
                ? null
                : nhanVien.getMaNhanVien();

        int currentPage = page == null || page < 1
                ? 1
                : page;

        int size = pageSize == null || pageSize < 1
                ? 10
                : pageSize;

        Page<HoaDonCuaToiProjection> result =
                hoaDonCuaToiRepository.findHoaDonCuaToi(
                        maNhanVienCanLoc,
                        cleanFilter(keyword),
                        cleanFilter(trangThai),
                        cleanFilter(phuongThucThanhToan),
                        tuNgay,
                        denNgay,
                        PageRequest.of(currentPage - 1, size)
                );

        List<HoaDonCuaToiResponse> items = result.getContent()
                .stream()
                .map(this::mapResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();

        response.put("items", items);
        response.put("total", result.getTotalElements());
        response.put("page", currentPage);
        response.put("pageSize", size);
        response.put("totalPages", result.getTotalPages());
        response.put("admin", laAdmin);

        return response;
    }

    /**
     * Lấy chi tiết một hóa đơn.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChiTietHoaDon(Integer maHoaDon) {

        if (maHoaDon == null || maHoaDon <= 0) {
            throw new RuntimeException("Mã hóa đơn không hợp lệ");
        }

        HoaDon hoaDon = hoaDonRepository.findById(maHoaDon)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hóa đơn #" + maHoaDon
                        )
                );

        DonHang donHang = hoaDon.getDonHang();

        if (donHang == null) {
            throw new RuntimeException(
                    "Hóa đơn không có thông tin đơn hàng"
            );
        }

        List<ChiTietDonHang> danhSachChiTiet =
                chiTietDonHangRepository.findByDonHang_MaDonHang(
                        donHang.getMaDonHang()
                );

        Map<String, Object> response = new HashMap<>();

        response.put(
                "maHoaDon",
                formatCode("HD", hoaDon.getMaHoaDon())
        );

        response.put(
                "maHoaDonGoc",
                hoaDon.getMaHoaDon()
        );

        response.put(
                "maDonHang",
                formatCode("DH", donHang.getMaDonHang())
        );

        response.put(
                "maDonHangGoc",
                donHang.getMaDonHang()
        );

        response.put(
                "ngayIn",
                hoaDon.getNgayIn()
        );

        response.put(
                "tongTien",
                hoaDon.getTongTien() != null
                        ? hoaDon.getTongTien()
                        : BigDecimal.ZERO
        );

        /*
         * Backend trả mã số:
         * 0: Chưa cập nhật
         * 1: Tiền mặt
         * 2: Chuyển khoản
         *
         * Frontend sẽ chuyển mã thành chữ để hiển thị.
         */
        response.put(
                "phuongThucThanhToan",
                hoaDon.getPhuongThucThanhToan() != null
                        ? hoaDon.getPhuongThucThanhToan()
                        : 0
        );

        /*
         * Backend trả mã trạng thái.
         * Frontend sẽ chuyển mã thành chữ để hiển thị.
         */
        response.put(
                "trangThai",
                hoaDon.getTrangThai() != null
                        ? hoaDon.getTrangThai()
                        : 0
        );

        response.put(
                "khachHang",
                buildKhachHangResponse(donHang.getKhachHang())
        );

        response.put(
                "nhanVien",
                getTenNhanVien(donHang)
        );

        List<Map<String, Object>> chiTietResponse =
                danhSachChiTiet.stream()
                        .map(this::mapChiTietDonHang)
                        .toList();

        response.put(
                "chiTiet",
                chiTietResponse
        );

        return response;
    }

    /**
     * Chuyển projection sang response danh sách hóa đơn.
     */
    private HoaDonCuaToiResponse mapResponse(
            HoaDonCuaToiProjection projection
    ) {
        Integer maHoaDon = projection.getMaHoaDon();
        Integer maDonHang = projection.getMaDonHang();

        return HoaDonCuaToiResponse.builder()
                .maHoaDon(maHoaDon)
                .soHoaDon(formatCode("HD", maHoaDon))
                .maDonHang(maDonHang)
                .maDonHangCode(formatCode("DH", maDonHang))
                .tenKhachHang(
                        safeText(
                                projection.getTenKhachHang(),
                                "Không có"
                        )
                )
                .soDienThoai(
                        safeText(
                                projection.getSoDienThoai(),
                                "Không có"
                        )
                )
                .email(
                        safeText(
                                projection.getEmail(),
                                "Không có"
                        )
                )
                .diaChi(
                        safeText(
                                projection.getDiaChi(),
                                "Không có"
                        )
                )
                .tenNhanVien(
                        safeText(
                                projection.getTenNhanVien(),
                                "Chưa phân công"
                        )
                )
                .ngayIn(projection.getNgayIn())
                .tongTien(
                        projection.getTongTien() != null
                                ? projection.getTongTien()
                                : BigDecimal.ZERO
                )
                .phuongThucThanhToan(
                        projection.getPhuongThucThanhToan()
                )
                .trangThai(
                        projection.getTrangThai()
                )
                .build();
    }

    /**
     * Tạo dữ liệu khách hàng an toàn, không dùng Map.of()
     * vì Map.of() không chấp nhận giá trị null.
     */
    private Map<String, Object> buildKhachHangResponse(
            KhachHang khachHang
    ) {
        Map<String, Object> response = new HashMap<>();

        if (khachHang == null) {
            response.put("ten", "Không có");
            response.put("soDienThoai", "Không có");
            response.put("email", "Không có");
            response.put("soNhaDuong", "Không có");
            response.put("quanHuyen", "Không có");
            response.put("tinhThanh", "Không có");
            response.put("phuongXa", "Không có");

            return response;
        }

        response.put(
                "ten",
                safeText(
                        khachHang.getTenKhachHang(),
                        "Không có"
                )
        );

        response.put(
                "soDienThoai",
                safeText(
                        khachHang.getSoDienThoai(),
                        "Không có"
                )
        );

        response.put(
                "email",
                safeText(
                        khachHang.getEmail(),
                        "Không có"
                )
        );

        response.put(
                "soNhaDuong",
                safeText(
                        khachHang.getSoNhaDuong(),
                        "Không có"
                )
        );
        response.put(
                "tinhThanh",
                safeText(
                        khachHang.getTinhThanh(),
                        "Không có"
                )
        );
        response.put(
                "phuongXa",
                safeText(
                        khachHang.getPhuongXa(),
                        "Không có"
                )
        );
        response.put(
                "quanHuyen",
                safeText(
                        khachHang.getQuanHuyen(),
                        "Không có"
                )
        );

        return response;
    }

    /**
     * Chuyển chi tiết đơn hàng thành dữ liệu trả về frontend.
     */
    private Map<String, Object> mapChiTietDonHang(
            ChiTietDonHang chiTiet
    ) {
        Map<String, Object> item = new HashMap<>();

        String tenSanPham = "Sản phẩm không còn tồn tại";

        if (chiTiet.getSanPham() != null
                && chiTiet.getSanPham().getTenSanPham() != null
                && !chiTiet.getSanPham().getTenSanPham().isBlank()) {

            tenSanPham = chiTiet.getSanPham().getTenSanPham();
        }

        item.put(
                "tenSanPham",
                tenSanPham
        );

        item.put(
                "soLuong",
                chiTiet.getSoLuong() != null
                        ? chiTiet.getSoLuong()
                        : 0
        );

        item.put(
                "giaTien",
                chiTiet.getGiaTien() != null
                        ? chiTiet.getGiaTien()
                        : BigDecimal.ZERO
        );

        return item;
    }

    /**
     * Lấy tên nhân viên phụ trách.
     */
    private String getTenNhanVien(DonHang donHang) {
        if (donHang == null
                || donHang.getNhanVien() == null) {
            return "Chưa phân công";
        }

        return safeText(
                donHang.getNhanVien().getHoTen(),
                "Chưa phân công"
        );
    }

    /**
     * Tạo mã hiển thị, ví dụ HD000002.
     */
    private String formatCode(
            String prefix,
            Integer id
    ) {
        if (id == null) {
            return prefix + "000000";
        }

        return prefix + String.format("%06d", id);
    }

    /**
     * Kiểm tra tài khoản có phải Admin hay không.
     */
    private boolean isAdmin(NhanVien nhanVien) {
        if (nhanVien == null
                || nhanVien.getVaiTro() == null) {
            return false;
        }

        return nhanVien.getVaiTro() == 1;
    }

    /**
     * Tránh trả về null hoặc chuỗi rỗng.
     */
    private String safeText(
            String value,
            String defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Chuẩn hóa dữ liệu bộ lọc.
     */
    private String cleanFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        if ("Tất cả".equalsIgnoreCase(value.trim())) {
            return null;
        }

        return value.trim();
    }
}