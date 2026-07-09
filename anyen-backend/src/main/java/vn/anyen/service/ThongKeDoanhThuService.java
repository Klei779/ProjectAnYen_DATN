package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.*;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongKeDoanhThuRepository;
import vn.anyen.repository.projection.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ThongKeDoanhThuService {

    private final ThongKeDoanhThuRepository thongKeDoanhThuRepository;
    private final DoiTacRepository doiTacRepository;
    private final NhanVienRepository nhanVienRepository;

    public ThongKeDoanhThuResponse thongKeNhanVien(
            String tenDangNhap,
            LocalDate tuNgay,
            LocalDate denNgay,
            String kieuThongKe
    ) {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        NhanVien nhanVien = nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên đăng nhập"));

        boolean laAdmin = isAdmin(nhanVien);

        // Admin truyền null => query lấy toàn hệ thống
        // Nhân viên thường truyền MaNhanVien => query chỉ lấy doanh thu của nhân viên đó
        Integer maNhanVienCanLoc = laAdmin ? null : nhanVien.getMaNhanVien();

        KhoangNgay khoangNgay = chuanHoaKhoangNgay(tuNgay, denNgay);
        String kieu = chuanHoaKieuThongKe(kieuThongKe);

        return ThongKeDoanhThuResponse.builder()
                .tuNgay(khoangNgay.tuNgay())
                .denNgay(khoangNgay.denNgay())
                .kieuThongKe(kieu)
                .tongQuan(mapTongQuan(thongKeDoanhThuRepository.getTongQuanNhanVien(
                        maNhanVienCanLoc,
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                )))
                .bieuDoDoanhThu(thongKeDoanhThuRepository.getBieuDoNhanVien(
                        maNhanVienCanLoc,
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay(),
                        kieu
                ).stream().map(this::mapBieuDo).toList())
                .topSanPham(thongKeDoanhThuRepository.getTopSanPhamNhanVien(
                        maNhanVienCanLoc,
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                ).stream().map(this::mapSanPham).toList())
                .phuongThucThanhToan(thongKeDoanhThuRepository.getPhuongThucNhanVien(
                        maNhanVienCanLoc,
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                ).stream().map(this::mapPhuongThuc).toList())
                .build();
    }

    public ThongKeDoanhThuResponse thongKeDoiTac(
            String tenDangNhap,
            LocalDate tuNgay,
            LocalDate denNgay,
            String kieuThongKe
    ) {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        DoiTac doiTac = doiTacRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác đăng nhập"));

        KhoangNgay khoangNgay = chuanHoaKhoangNgay(tuNgay, denNgay);
        String kieu = chuanHoaKieuThongKe(kieuThongKe);

        return ThongKeDoanhThuResponse.builder()
                .tuNgay(khoangNgay.tuNgay())
                .denNgay(khoangNgay.denNgay())
                .kieuThongKe(kieu)
                .tongQuan(mapTongQuan(thongKeDoanhThuRepository.getTongQuanDoiTac(
                        doiTac.getMaDoiTac(),
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                )))
                .bieuDoDoanhThu(thongKeDoanhThuRepository.getBieuDoDoiTac(
                        doiTac.getMaDoiTac(),
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay(),
                        kieu
                ).stream().map(this::mapBieuDo).toList())
                .topSanPham(thongKeDoanhThuRepository.getTopSanPhamDoiTac(
                        doiTac.getMaDoiTac(),
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                ).stream().map(this::mapSanPham).toList())
                .phuongThucThanhToan(thongKeDoanhThuRepository.getPhuongThucDoiTac(
                        doiTac.getMaDoiTac(),
                        khoangNgay.tuNgay(),
                        khoangNgay.denNgay()
                ).stream().map(this::mapPhuongThuc).toList())
                .build();
    }

    private boolean isAdmin(NhanVien nhanVien) {
        if (nhanVien == null || nhanVien.getVaiTro() == null) {
            return false;
        }

        int vaiTro = nhanVien.getVaiTro();

        return vaiTro == 1;
    }

    private KhoangNgay chuanHoaKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate today = LocalDate.now();

        LocalDate start = tuNgay != null
                ? tuNgay
                : today.withDayOfMonth(1);

        LocalDate end = denNgay != null
                ? denNgay
                : today;

        if (end.isBefore(start)) {
            return new KhoangNgay(end, start);
        }

        return new KhoangNgay(start, end);
    }

    private String chuanHoaKieuThongKe(String kieuThongKe) {
        if (kieuThongKe == null || kieuThongKe.isBlank()) {
            return "NGAY";
        }

        String value = kieuThongKe.trim().toUpperCase();

        return switch (value) {
            case "THANG", "THÁNG", "MONTH" -> "THANG";
            case "NAM", "NĂM", "YEAR" -> "NAM";
            default -> "NGAY";
        };
    }

    private DoanhThuTongQuanResponse mapTongQuan(DoanhThuTongQuanProjection p) {
        if (p == null) {
            return DoanhThuTongQuanResponse.builder()
                    .tongDoanhThu(BigDecimal.ZERO)
                    .tongHoaDon(0L)
                    .tongDonHang(0L)
                    .doanhThuTrungBinh(BigDecimal.ZERO)
                    .build();
        }

        return DoanhThuTongQuanResponse.builder()
                .tongDoanhThu(nvl(p.getTongDoanhThu()))
                .tongHoaDon(nvl(p.getTongHoaDon()))
                .tongDonHang(nvl(p.getTongDonHang()))
                .doanhThuTrungBinh(nvl(p.getDoanhThuTrungBinh()))
                .build();
    }

    private DoanhThuTheoThoiGianResponse mapBieuDo(DoanhThuTheoThoiGianProjection p) {
        return DoanhThuTheoThoiGianResponse.builder()
                .thoiGian(p.getThoiGian())
                .doanhThu(nvl(p.getDoanhThu()))
                .soDonHang(nvl(p.getSoDonHang()))
                .build();
    }

    private DoanhThuSanPhamResponse mapSanPham(DoanhThuSanPhamProjection p) {
        return DoanhThuSanPhamResponse.builder()
                .maSanPham(p.getMaSanPham())
                .tenSanPham(p.getTenSanPham())
                .soLuongBan(nvl(p.getSoLuongBan()))
                .doanhThu(nvl(p.getDoanhThu()))
                .build();
    }

    private DoanhThuPhuongThucResponse mapPhuongThuc(DoanhThuPhuongThucProjection p) {
        return DoanhThuPhuongThucResponse.builder()
                .phuongThucThanhToan(p.getPhuongThucThanhToan())
                .soHoaDon(nvl(p.getSoHoaDon()))
                .doanhThu(nvl(p.getDoanhThu()))
                .build();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long nvl(Long value) {
        return value == null ? 0L : value;
    }

    private record KhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
    }
}