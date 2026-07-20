package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.DoanhThuPhuongThucResponse;
import vn.anyen.dto.response.DoanhThuSanPhamResponse;
import vn.anyen.dto.response.DoanhThuTheoThoiGianResponse;
import vn.anyen.dto.response.DoanhThuTongQuanResponse;
import vn.anyen.dto.response.ThongKeDoanhThuResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongKeDoanhThuRepository;
import vn.anyen.repository.projection.DoanhThuPhuongThucProjection;
import vn.anyen.repository.projection.DoanhThuSanPhamProjection;
import vn.anyen.repository.projection.DoanhThuTheoThoiGianProjection;
import vn.anyen.repository.projection.DoanhThuTongQuanProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ThongKeDoanhThuService {

    /*
     * Tỷ lệ doanh thu:
     * - Đối tác nhận 80%
     * - Admin nhận 20%
     * - Nhân viên thường hiển thị toàn bộ giá trị đơn mình phụ trách
     */
    private static final BigDecimal TY_LE_DOI_TAC =
            new BigDecimal("0.80");

    private static final BigDecimal TY_LE_ADMIN =
            new BigDecimal("0.20");

    private static final BigDecimal TY_LE_NHAN_VIEN =
            BigDecimal.ONE;

    private final ThongKeDoanhThuRepository thongKeDoanhThuRepository;
    private final DoiTacRepository doiTacRepository;
    private final NhanVienRepository nhanVienRepository;

    /**
     * Thống kê dành cho admin và nhân viên.
     *
     * Admin:
     * - Xem tổng giá trị đơn hàng toàn hệ thống.
     * - Doanh thu thực nhận là 20%.
     *
     * Nhân viên:
     * - Chỉ xem các đơn hàng mình phụ trách.
     * - Doanh thu thực nhận đang để 100%.
     */
    public ThongKeDoanhThuResponse thongKeNhanVien(
            String tenDangNhap,
            LocalDate tuNgay,
            LocalDate denNgay,
            String kieuThongKe
    ) {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        NhanVien nhanVien = nhanVienRepository
                .findByTenDangNhap(tenDangNhap)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy nhân viên đăng nhập"
                        )
                );

        boolean laAdmin = isAdmin(nhanVien);

        /*
         * Admin truyền null để repository lấy dữ liệu toàn hệ thống.
         * Nhân viên truyền mã nhân viên để chỉ lấy đơn mình phụ trách.
         */
        Integer maNhanVienCanLoc = laAdmin
                ? null
                : nhanVien.getMaNhanVien();

        BigDecimal tyLeDoanhThu = laAdmin
                ? TY_LE_ADMIN
                : TY_LE_NHAN_VIEN;

        KhoangNgay khoangNgay =
                chuanHoaKhoangNgay(tuNgay, denNgay);

        String kieu =
                chuanHoaKieuThongKe(kieuThongKe);

        DoanhThuTongQuanProjection tongQuanProjection =
                thongKeDoanhThuRepository.getTongQuanNhanVien(
                        maNhanVienCanLoc,
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                );

        return ThongKeDoanhThuResponse.builder()
                .tuNgay(khoangNgay.tuNgay())
                .denNgay(khoangNgay.denNgay())
                .kieuThongKe(kieu)

                /*
                 * Chỉ tổng quan có thêm:
                 * - Tổng giá trị đơn hàng 100%
                 * - Doanh thu thực nhận theo tỷ lệ
                 */
                .tongQuan(
                        mapTongQuan(
                                tongQuanProjection,
                                tyLeDoanhThu
                        )
                )

                /*
                 * Biểu đồ giữ nguyên tổng giá trị 100%.
                 * Không nhân thêm 20% hoặc 80%.
                 */
                .bieuDoDoanhThu(
                        thongKeDoanhThuRepository
                                .getBieuDoNhanVien(
                                        maNhanVienCanLoc,
                                        khoangNgay.tuNgay(),
                                        khoangNgay.denNgay(),
                                        kieu
                                )
                                .stream()
                                .map(this::mapBieuDo)
                                .toList()
                )

                /*
                 * Top sản phẩm giữ nguyên tổng giá trị bán.
                 */
                .topSanPham(
                        thongKeDoanhThuRepository
                                .getTopSanPhamNhanVien(
                                        maNhanVienCanLoc,
                                        khoangNgay.tuNgay(),
                                        khoangNgay.denNgay()
                                )
                                .stream()
                                .map(this::mapSanPham)
                                .toList()
                )

                /*
                 * Doanh thu theo phương thức thanh toán
                 * cũng giữ nguyên giá trị 100%.
                 */
                .phuongThucThanhToan(
                        thongKeDoanhThuRepository
                                .getPhuongThucNhanVien(
                                        maNhanVienCanLoc,
                                        khoangNgay.tuNgay(),
                                        khoangNgay.denNgay()
                                )
                                .stream()
                                .map(this::mapPhuongThuc)
                                .toList()
                )

                .build();
    }

    /**
     * Thống kê dành cho đối tác.
     *
     * Đối tác:
     * - Xem tổng giá trị các sản phẩm của mình đã bán.
     * - Doanh thu thực nhận là 80%.
     */
    public ThongKeDoanhThuResponse thongKeDoiTac(
            String tenDangNhap,
            LocalDate tuNgay,
            LocalDate denNgay,
            String kieuThongKe
    ) {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        DoiTac doiTac = doiTacRepository
                .findByTenDangNhap(tenDangNhap)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy đối tác đăng nhập"
                        )
                );

        KhoangNgay khoangNgay =
                chuanHoaKhoangNgay(tuNgay, denNgay);

        String kieu =
                chuanHoaKieuThongKe(kieuThongKe);

        DoanhThuTongQuanProjection tongQuanProjection =
                thongKeDoanhThuRepository.getTongQuanDoiTac(
                        doiTac.getMaDoiTac(),
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                );

        return ThongKeDoanhThuResponse.builder()
                .tuNgay(khoangNgay.tuNgay())
                .denNgay(khoangNgay.denNgay())
                .kieuThongKe(kieu)

                /*
                 * Tổng giá trị sản phẩm bán được vẫn là 100%.
                 * Doanh thu thực nhận được tính bằng 80%.
                 */
                .tongQuan(
                        mapTongQuan(
                                tongQuanProjection,
                                TY_LE_DOI_TAC
                        )
                )

                /*
                 * Biểu đồ hiển thị tổng giá trị sản phẩm bán được,
                 * không nhân 80%.
                 */
                .bieuDoDoanhThu(
                        thongKeDoanhThuRepository
                                .getBieuDoDoiTac(
                                        doiTac.getMaDoiTac(),
                                        khoangNgay.tuNgay(),
                                        khoangNgay.denNgay(),
                                        kieu
                                )
                                .stream()
                                .map(this::mapBieuDo)
                                .toList()
                )

                /*
                 * Top sản phẩm hiển thị giá trị bán gốc 100%.
                 */
                .topSanPham(
                        thongKeDoanhThuRepository
                                .getTopSanPhamDoiTac(
                                        doiTac.getMaDoiTac(),
                                        khoangNgay.tuNgay(),
                                        khoangNgay.denNgay()
                                )
                                .stream()
                                .map(this::mapSanPham)
                                .toList()
                )

                /*
                 * Phương thức thanh toán hiển thị
                 * tổng giá trị thanh toán gốc 100%.
                 */
                .phuongThucThanhToan(
                        thongKeDoanhThuRepository
                                .getPhuongThucDoiTac(
                                        doiTac.getMaDoiTac(),
                                        khoangNgay.tuNgay(),
                                        khoangNgay.denNgay()
                                )
                                .stream()
                                .map(this::mapPhuongThuc)
                                .toList()
                )

                .build();
    }

    /**
     * Kiểm tra nhân viên hiện tại có phải admin hay không.
     *
     * Theo project hiện tại:
     * vaiTro = 1 là ADMIN.
     */
    private boolean isAdmin(NhanVien nhanVien) {
        if (
                nhanVien == null
                        || nhanVien.getVaiTro() == null
        ) {
            return false;
        }

        return nhanVien.getVaiTro() == 1;
    }

    /**
     * Chuẩn hóa khoảng ngày thống kê.
     *
     * Nếu không truyền:
     * - Từ ngày: ngày đầu tháng hiện tại.
     * - Đến ngày: ngày hiện tại.
     *
     * Nếu người dùng truyền ngược ngày thì tự đảo lại.
     */
    private KhoangNgay chuanHoaKhoangNgay(
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
        LocalDate homNay = LocalDate.now();

        LocalDate ngayBatDau = tuNgay != null
                ? tuNgay
                : homNay.withDayOfMonth(1);

        LocalDate ngayKetThuc = denNgay != null
                ? denNgay
                : homNay;

        if (ngayKetThuc.isBefore(ngayBatDau)) {
            return new KhoangNgay(
                    ngayKetThuc,
                    ngayBatDau
            );
        }

        return new KhoangNgay(
                ngayBatDau,
                ngayKetThuc
        );
    }

    /**
     * Chuẩn hóa kiểu thống kê thành:
     * - NGAY
     * - THANG
     * - NAM
     */
    private String chuanHoaKieuThongKe(
            String kieuThongKe
    ) {
        if (
                kieuThongKe == null
                        || kieuThongKe.isBlank()
        ) {
            return "NGAY";
        }

        String giaTri =
                kieuThongKe.trim().toUpperCase();

        return switch (giaTri) {
            case "THANG", "THÁNG", "MONTH" ->
                    "THANG";

            case "NAM", "NĂM", "YEAR" ->
                    "NAM";

            default ->
                    "NGAY";
        };
    }

    /**
     * Chuyển dữ liệu tổng quan từ projection sang response.
     *
     * tongDoanhThu:
     * - Giữ nguyên tổng giá trị đơn hàng 100%.
     *
     * doanhThuThucNhan:
     * - Admin: tongDoanhThu * 20%.
     * - Đối tác: tongDoanhThu * 80%.
     */
    private DoanhThuTongQuanResponse mapTongQuan(
            DoanhThuTongQuanProjection projection,
            BigDecimal tyLeDoanhThu
    ) {
        BigDecimal tongGiaTriDonHang = projection == null
                ? BigDecimal.ZERO
                : nvl(projection.getTongDoanhThu());

        BigDecimal doanhThuTrungBinh = projection == null
                ? BigDecimal.ZERO
                : nvl(projection.getDoanhThuTrungBinh());

        Long tongHoaDon = projection == null
                ? 0L
                : nvl(projection.getTongHoaDon());

        Long tongDonHang = projection == null
                ? 0L
                : nvl(projection.getTongDonHang());

        BigDecimal doanhThuThucNhan =
                tinhDoanhThuThucNhan(
                        tongGiaTriDonHang,
                        tyLeDoanhThu
                );

        Integer tyLePhanTram =
                chuyenTyLeThanhPhanTram(tyLeDoanhThu);

        return DoanhThuTongQuanResponse.builder()
                /*
                 * Tổng giá trị đơn hàng/sản phẩm đã bán,
                 * luôn giữ nguyên 100%.
                 */
                .tongDoanhThu(tongGiaTriDonHang)

                /*
                 * Phần doanh thu thực nhận:
                 * đối tác 80%, admin 20%.
                 */
                .doanhThuThucNhan(doanhThuThucNhan)
                .tyLeDoanhThu(tyLePhanTram)

                .tongHoaDon(tongHoaDon)
                .tongDonHang(tongDonHang)

                /*
                 * Giá trị trung bình giữ nguyên giá trị gốc 100%.
                 */
                .doanhThuTrungBinh(doanhThuTrungBinh)

                .build();
    }

    /**
     * Biểu đồ hiển thị giá trị gốc 100%.
     */
    private DoanhThuTheoThoiGianResponse mapBieuDo(
            DoanhThuTheoThoiGianProjection projection
    ) {
        if (projection == null) {
            return DoanhThuTheoThoiGianResponse.builder()
                    .thoiGian("")
                    .doanhThu(BigDecimal.ZERO)
                    .soDonHang(0L)
                    .build();
        }

        return DoanhThuTheoThoiGianResponse.builder()
                .thoiGian(projection.getThoiGian())
                .doanhThu(nvl(projection.getDoanhThu()))
                .soDonHang(nvl(projection.getSoDonHang()))
                .build();
    }

    /**
     * Top sản phẩm hiển thị doanh thu bán hàng gốc 100%.
     */
    private DoanhThuSanPhamResponse mapSanPham(
            DoanhThuSanPhamProjection projection
    ) {
        if (projection == null) {
            return DoanhThuSanPhamResponse.builder()
                    .maSanPham(null)
                    .tenSanPham("")
                    .soLuongBan(0L)
                    .doanhThu(BigDecimal.ZERO)
                    .build();
        }

        return DoanhThuSanPhamResponse.builder()
                .maSanPham(projection.getMaSanPham())
                .tenSanPham(projection.getTenSanPham())
                .soLuongBan(nvl(projection.getSoLuongBan()))
                .doanhThu(nvl(projection.getDoanhThu()))
                .build();
    }

    /**
     * Thống kê phương thức thanh toán
     * hiển thị giá trị gốc 100%.
     */
    private DoanhThuPhuongThucResponse mapPhuongThuc(
            DoanhThuPhuongThucProjection projection
    ) {
        if (projection == null) {
            return DoanhThuPhuongThucResponse.builder()
                    .phuongThucThanhToan("Chưa cập nhật")
                    .soHoaDon(0L)
                    .doanhThu(BigDecimal.ZERO)
                    .build();
        }

        return DoanhThuPhuongThucResponse.builder()
                .phuongThucThanhToan(
                        projection.getPhuongThucThanhToan()
                )
                .soHoaDon(
                        nvl(projection.getSoHoaDon())
                )
                .doanhThu(
                        nvl(projection.getDoanhThu())
                )
                .build();
    }

    /**
     * Tính số tiền thực nhận theo tỷ lệ.
     */
    private BigDecimal tinhDoanhThuThucNhan(
            BigDecimal tongDoanhThu,
            BigDecimal tyLeDoanhThu
    ) {
        BigDecimal doanhThu = tongDoanhThu == null
                ? BigDecimal.ZERO
                : tongDoanhThu;

        BigDecimal tyLe = tyLeDoanhThu == null
                ? BigDecimal.ONE
                : tyLeDoanhThu;

        return doanhThu
                .multiply(tyLe)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * Chuyển 0.80 thành 80, 0.20 thành 20.
     */
    private Integer chuyenTyLeThanhPhanTram(
            BigDecimal tyLeDoanhThu
    ) {
        BigDecimal tyLe = tyLeDoanhThu == null
                ? BigDecimal.ONE
                : tyLeDoanhThu;

        return tyLe
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private Long nvl(Long value) {
        return value == null
                ? 0L
                : value;
    }

    private record KhoangNgay(
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
    }
}