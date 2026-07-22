package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.DoanhThuDoiTuongResponse;
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
import vn.anyen.repository.projection.DoanhThuDoiTuongProjection;
import vn.anyen.repository.projection.DoanhThuPhuongThucProjection;
import vn.anyen.repository.projection.DoanhThuSanPhamProjection;
import vn.anyen.repository.projection.DoanhThuTheoThoiGianProjection;
import vn.anyen.repository.projection.DoanhThuTongQuanProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThongKeDoanhThuService {

    /*
     * Tỷ lệ doanh thu:
     * - Đối tác nhận 80%.
     * - Admin nhận 20%.
     * - Nhân viên hiển thị toàn bộ giá trị đơn mình phụ trách.
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
     * Thống kê dành cho ADMIN và NHANVIEN.
     *
     * ADMIN:
     * - Xem dữ liệu của toàn hệ thống.
     * - Doanh thu thực nhận là 20%.
     * - Có top nhân viên và top đối tác.
     *
     * NHANVIEN:
     * - Chỉ xem những đơn hàng mình phụ trách.
     * - Doanh thu hiển thị là 100% giá trị các đơn phụ trách.
     * - Không xem top nhân viên và top đối tác.
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
         * ADMIN truyền null để repository lấy toàn hệ thống.
         * NHANVIEN truyền mã nhân viên để repository lọc
         * theo người phụ trách.
         */
        Integer maNhanVienCanLoc = laAdmin
                ? null
                : nhanVien.getMaNhanVien();

        BigDecimal tyLeDoanhThu = laAdmin
                ? TY_LE_ADMIN
                : TY_LE_NHAN_VIEN;

        KhoangNgay khoangNgay =
                chuanHoaKhoangNgay(
                        tuNgay,
                        denNgay
                );

        String kieu =
                chuanHoaKieuThongKe(
                        kieuThongKe
                );

        DoanhThuTongQuanProjection tongQuanProjection =
                thongKeDoanhThuRepository
                        .getTongQuanNhanVien(
                                maNhanVienCanLoc,
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay()
                        );

        List<DoanhThuTheoThoiGianResponse> bieuDo =
                thongKeDoanhThuRepository
                        .getBieuDoNhanVien(
                                maNhanVienCanLoc,
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay(),
                                kieu
                        )
                        .stream()
                        .map(this::mapBieuDo)
                        .toList();

        List<DoanhThuSanPhamResponse> topSanPham =
                thongKeDoanhThuRepository
                        .getTopSanPhamNhanVien(
                                maNhanVienCanLoc,
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay()
                        )
                        .stream()
                        .map(this::mapSanPham)
                        .toList();

        List<DoanhThuDoiTuongResponse> topNhanVien;

        List<DoanhThuDoiTuongResponse> topDoiTac;

        if (laAdmin) {
            topNhanVien =
                    thongKeDoanhThuRepository
                            .getTopNhanVienAdmin(
                                    khoangNgay.tuNgay(),
                                    khoangNgay.denNgay()
                            )
                            .stream()
                            .map(this::mapDoiTuong)
                            .toList();

            topDoiTac =
                    thongKeDoanhThuRepository
                            .getTopDoiTacAdmin(
                                    khoangNgay.tuNgay(),
                                    khoangNgay.denNgay()
                            )
                            .stream()
                            .map(this::mapDoiTuong)
                            .toList();
        } else {
            /*
             * Không trả dữ liệu toàn hệ thống
             * cho tài khoản nhân viên thường.
             */
            topNhanVien = List.of();
            topDoiTac = List.of();
        }

        List<DoanhThuPhuongThucResponse> phuongThucThanhToan =
                thongKeDoanhThuRepository
                        .getPhuongThucNhanVien(
                                maNhanVienCanLoc,
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay()
                        )
                        .stream()
                        .map(this::mapPhuongThuc)
                        .toList();

        return ThongKeDoanhThuResponse.builder()
                .tuNgay(khoangNgay.tuNgay())
                .denNgay(khoangNgay.denNgay())
                .kieuThongKe(kieu)

                /*
                 * Tổng giá trị đơn hàng là 100%.
                 * Doanh thu thực nhận tính theo quyền:
                 * - ADMIN: 20%.
                 * - NHANVIEN: 100%.
                 */
                .tongQuan(
                        mapTongQuan(
                                tongQuanProjection,
                                tyLeDoanhThu
                        )
                )

                /*
                 * Biểu đồ luôn hiển thị giá trị gốc 100%.
                 */
                .bieuDoDoanhThu(bieuDo)

                /*
                 * Top sản phẩm luôn hiển thị giá trị bán gốc.
                 */
                .topSanPham(topSanPham)

                /*
                 * Chỉ ADMIN có dữ liệu hai danh sách này.
                 */
                .topNhanVien(topNhanVien)
                .topDoiTac(topDoiTac)

                /*
                 * Phương thức thanh toán hiển thị
                 * tổng tiền thanh toán gốc.
                 */
                .phuongThucThanhToan(
                        phuongThucThanhToan
                )

                .build();
    }

    /**
     * Thống kê dành cho đối tác.
     *
     * Đối tác:
     * - Chỉ xem dữ liệu các sản phẩm thuộc đối tác.
     * - Tổng giá trị sản phẩm hiển thị là 100%.
     * - Doanh thu thực nhận là 80%.
     * - Không trả top nhân viên và top đối tác.
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
                chuanHoaKhoangNgay(
                        tuNgay,
                        denNgay
                );

        String kieu =
                chuanHoaKieuThongKe(
                        kieuThongKe
                );

        DoanhThuTongQuanProjection tongQuanProjection =
                thongKeDoanhThuRepository
                        .getTongQuanDoiTac(
                                doiTac.getMaDoiTac(),
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay()
                        );

        List<DoanhThuTheoThoiGianResponse> bieuDo =
                thongKeDoanhThuRepository
                        .getBieuDoDoiTac(
                                doiTac.getMaDoiTac(),
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay(),
                                kieu
                        )
                        .stream()
                        .map(this::mapBieuDo)
                        .toList();

        List<DoanhThuSanPhamResponse> topSanPham =
                thongKeDoanhThuRepository
                        .getTopSanPhamDoiTac(
                                doiTac.getMaDoiTac(),
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay()
                        )
                        .stream()
                        .map(this::mapSanPham)
                        .toList();

        List<DoanhThuPhuongThucResponse> phuongThucThanhToan =
                thongKeDoanhThuRepository
                        .getPhuongThucDoiTac(
                                doiTac.getMaDoiTac(),
                                khoangNgay.tuNgay(),
                                khoangNgay.denNgay()
                        )
                        .stream()
                        .map(this::mapPhuongThuc)
                        .toList();

        return ThongKeDoanhThuResponse.builder()
                .tuNgay(khoangNgay.tuNgay())
                .denNgay(khoangNgay.denNgay())
                .kieuThongKe(kieu)

                /*
                 * Tổng giá trị sản phẩm là 100%.
                 * Doanh thu thực nhận của đối tác là 80%.
                 */
                .tongQuan(
                        mapTongQuan(
                                tongQuanProjection,
                                TY_LE_DOI_TAC
                        )
                )

                /*
                 * Những dữ liệu bên dưới giữ nguyên
                 * giá trị bán gốc 100%.
                 */
                .bieuDoDoanhThu(bieuDo)
                .topSanPham(topSanPham)

                /*
                 * Đối tác không được xem dữ liệu xếp hạng
                 * nhân viên và đối tác toàn hệ thống.
                 */
                .topNhanVien(List.of())
                .topDoiTac(List.of())

                .phuongThucThanhToan(
                        phuongThucThanhToan
                )

                .build();
    }

    /**
     * Kiểm tra tài khoản nhân viên hiện tại
     * có phải ADMIN hay không.
     *
     * Theo cấu trúc project hiện tại:
     * vaiTro = 1 là ADMIN.
     */
    private boolean isAdmin(
            NhanVien nhanVien
    ) {
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
     * Khi không truyền ngày:
     * - Từ ngày: ngày đầu tháng hiện tại.
     * - Đến ngày: ngày hiện tại.
     *
     * Nếu ngày kết thúc nhỏ hơn ngày bắt đầu,
     * tự động đảo hai ngày.
     */
    private KhoangNgay chuanHoaKhoangNgay(
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
        LocalDate homNay = LocalDate.now();

        LocalDate ngayBatDau =
                tuNgay != null
                        ? tuNgay
                        : homNay.withDayOfMonth(1);

        LocalDate ngayKetThuc =
                denNgay != null
                        ? denNgay
                        : homNay;

        if (
                ngayKetThuc.isBefore(
                        ngayBatDau
                )
        ) {
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
                kieuThongKe
                        .trim()
                        .toUpperCase();

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
     * Chuyển projection tổng quan sang response.
     *
     * tongDoanhThu:
     * - Giá trị gốc 100%.
     *
     * doanhThuThucNhan:
     * - ADMIN: 20%.
     * - Đối tác: 80%.
     * - Nhân viên: 100%.
     */
    private DoanhThuTongQuanResponse mapTongQuan(
            DoanhThuTongQuanProjection projection,
            BigDecimal tyLeDoanhThu
    ) {
        BigDecimal tongGiaTriDonHang =
                projection == null
                        ? BigDecimal.ZERO
                        : nvl(
                        projection
                        .getTongDoanhThu()
                );

        BigDecimal doanhThuTrungBinh =
                projection == null
                        ? BigDecimal.ZERO
                        : nvl(
                        projection
                        .getDoanhThuTrungBinh()
                );

        Long tongHoaDon =
                projection == null
                        ? 0L
                        : nvl(
                        projection
                        .getTongHoaDon()
                );

        Long tongDonHang =
                projection == null
                        ? 0L
                        : nvl(
                        projection
                        .getTongDonHang()
                );

        BigDecimal doanhThuThucNhan =
                tinhDoanhThuThucNhan(
                        tongGiaTriDonHang,
                        tyLeDoanhThu
                );

        Integer tyLePhanTram =
                chuyenTyLeThanhPhanTram(
                        tyLeDoanhThu
                );

        return DoanhThuTongQuanResponse
                .builder()

                /*
                 * Tổng giá trị đơn hàng/sản phẩm,
                 * luôn giữ nguyên 100%.
                 */
                .tongDoanhThu(
                        tongGiaTriDonHang
                )

                /*
                 * Phần doanh thu được hưởng
                 * theo quyền tài khoản.
                 */
                .doanhThuThucNhan(
                        doanhThuThucNhan
                )

                .tyLeDoanhThu(
                        tyLePhanTram
                )

                .tongHoaDon(
                        tongHoaDon
                )

                .tongDonHang(
                        tongDonHang
                )

                /*
                 * Trung bình trên mỗi đơn hàng
                 * giữ nguyên giá trị gốc.
                 */
                .doanhThuTrungBinh(
                        doanhThuTrungBinh
                )

                .build();
    }

    /**
     * Chuyển dữ liệu biểu đồ sang response.
     */
    private DoanhThuTheoThoiGianResponse mapBieuDo(
            DoanhThuTheoThoiGianProjection projection
    ) {
        if (projection == null) {
            return DoanhThuTheoThoiGianResponse
                    .builder()
                    .thoiGian("")
                    .doanhThu(BigDecimal.ZERO)
                    .soDonHang(0L)
                    .build();
        }

        return DoanhThuTheoThoiGianResponse
                .builder()

                .thoiGian(
                        projection.getThoiGian()
                )

                .doanhThu(
                        nvl(
                                projection
                                        .getDoanhThu()
                        )
                )

                .soDonHang(
                        nvl(
                                projection
                                        .getSoDonHang()
                        )
                )

                .build();
    }

    /**
     * Chuyển dữ liệu top sản phẩm sang response.
     */
    private DoanhThuSanPhamResponse mapSanPham(
            DoanhThuSanPhamProjection projection
    ) {
        if (projection == null) {
            return DoanhThuSanPhamResponse
                    .builder()
                    .maSanPham(null)
                    .tenSanPham("---")
                    .soLuongBan(0L)
                    .doanhThu(BigDecimal.ZERO)
                    .build();
        }

        return DoanhThuSanPhamResponse
                .builder()

                .maSanPham(
                        projection.getMaSanPham()
                )

                .tenSanPham(
                        projection.getTenSanPham() == null
                                || projection
                                .getTenSanPham()
                                .isBlank()
                                ? "---"
                                : projection
                                  .getTenSanPham()
                )

                .soLuongBan(
                        nvl(
                                projection
                                        .getSoLuongBan()
                        )
                )

                .doanhThu(
                        nvl(
                                projection
                                        .getDoanhThu()
                        )
                )

                .build();
    }

    /**
     * Chuyển dữ liệu top nhân viên hoặc
     * top đối tác sang response dùng chung.
     */
    private DoanhThuDoiTuongResponse mapDoiTuong(
            DoanhThuDoiTuongProjection projection
    ) {
        if (projection == null) {
            return DoanhThuDoiTuongResponse
                    .builder()
                    .maDoiTuong(null)
                    .tenDoiTuong("---")
                    .soDonHang(0L)
                    .doanhThu(BigDecimal.ZERO)
                    .build();
        }

        String tenDoiTuong =
                projection.getTenDoiTuong();

        if (
                tenDoiTuong == null
                        || tenDoiTuong.isBlank()
        ) {
            tenDoiTuong = "---";
        }

        return DoanhThuDoiTuongResponse
                .builder()

                .maDoiTuong(
                        projection.getMaDoiTuong()
                )

                .tenDoiTuong(
                        tenDoiTuong
                )

                .soDonHang(
                        nvl(
                                projection
                                        .getSoDonHang()
                        )
                )

                .doanhThu(
                        nvl(
                                projection
                                        .getDoanhThu()
                        )
                )

                .build();
    }

    /**
     * Chuyển dữ liệu phương thức thanh toán
     * sang response.
     */
    private DoanhThuPhuongThucResponse mapPhuongThuc(
            DoanhThuPhuongThucProjection projection
    ) {
        if (projection == null) {
            return DoanhThuPhuongThucResponse
                    .builder()
                    .phuongThucThanhToan(
                            "Chưa cập nhật"
                    )
                    .soHoaDon(0L)
                    .doanhThu(BigDecimal.ZERO)
                    .build();
        }

        String phuongThucThanhToan =
                projection
                        .getPhuongThucThanhToan();

        if (
                phuongThucThanhToan == null
                        || phuongThucThanhToan.isBlank()
        ) {
            phuongThucThanhToan =
                    "Chưa cập nhật";
        }

        return DoanhThuPhuongThucResponse
                .builder()

                .phuongThucThanhToan(
                        phuongThucThanhToan
                )

                .soHoaDon(
                        nvl(
                                projection
                                        .getSoHoaDon()
                        )
                )

                .doanhThu(
                        nvl(
                                projection
                                        .getDoanhThu()
                        )
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
        BigDecimal doanhThu =
                tongDoanhThu == null
                        ? BigDecimal.ZERO
                        : tongDoanhThu;

        BigDecimal tyLe =
                tyLeDoanhThu == null
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
     * Chuyển tỷ lệ:
     * - 0.80 thành 80.
     * - 0.20 thành 20.
     * - 1.00 thành 100.
     */
    private Integer chuyenTyLeThanhPhanTram(
            BigDecimal tyLeDoanhThu
    ) {
        BigDecimal tyLe =
                tyLeDoanhThu == null
                        ? BigDecimal.ONE
                        : tyLeDoanhThu;

        return tyLe
                .multiply(
                        new BigDecimal("100")
                )
                .setScale(
                        0,
                        RoundingMode.HALF_UP
                )
                .intValue();
    }

    /**
     * Tránh giá trị BigDecimal null.
     */
    private BigDecimal nvl(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    /**
     * Tránh giá trị Long null.
     */
    private Long nvl(
            Long value
    ) {
        return value == null
                ? 0L
                : value;
    }

    /**
     * Đối tượng lưu khoảng ngày đã chuẩn hóa.
     */
    private record KhoangNgay(
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
    }
}