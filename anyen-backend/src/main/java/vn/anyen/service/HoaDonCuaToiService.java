package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.HoaDonCuaToiResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.HoaDonCuaToiRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.projection.HoaDonCuaToiProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HoaDonCuaToiService {

    private final HoaDonCuaToiRepository hoaDonCuaToiRepository;
    private final NhanVienRepository nhanVienRepository;

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên đăng nhập"));

        boolean laAdmin = isAdmin(nhanVien);

        Integer maNhanVienCanLoc = laAdmin ? null : nhanVien.getMaNhanVien();

        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<HoaDonCuaToiProjection> result = hoaDonCuaToiRepository.findHoaDonCuaToi(
                maNhanVienCanLoc,
                keyword,
                trangThai,
                phuongThucThanhToan,
                tuNgay,
                denNgay,
                PageRequest.of(currentPage - 1, size)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("items", result.getContent().stream().map(this::mapResponse).toList());
        response.put("total", result.getTotalElements());
        response.put("page", currentPage);
        response.put("pageSize", size);
        response.put("totalPages", result.getTotalPages());
        response.put("admin", laAdmin);

        return response;
    }

    private HoaDonCuaToiResponse mapResponse(HoaDonCuaToiProjection p) {
        Integer maHoaDon = p.getMaHoaDon();
        Integer maDonHang = p.getMaDonHang();

        return HoaDonCuaToiResponse.builder()
                .maHoaDon(maHoaDon)
                .soHoaDon(formatCode("HD", maHoaDon))
                .maDonHang(maDonHang)
                .maDonHangCode(formatCode("DH", maDonHang))
                .tenKhachHang(p.getTenKhachHang())
                .soDienThoai(p.getSoDienThoai())
                .email(p.getEmail())
                .diaChi(p.getDiaChi())
                .tenNhanVien(p.getTenNhanVien())
                .ngayIn(p.getNgayIn())
                .tongTien(p.getTongTien() == null ? BigDecimal.ZERO : p.getTongTien())
                .phuongThucThanhToan(p.getPhuongThucThanhToan())
                .trangThai(p.getTrangThai())
                .build();
    }

    private String formatCode(String prefix, Integer id) {
        if (id == null) {
            return prefix + "000000";
        }

        return prefix + String.format("%06d", id);
    }

    private boolean isAdmin(NhanVien nhanVien) {
        if (nhanVien == null || nhanVien.getVaiTro() == null) {
            return false;
        }

        int vaiTro = nhanVien.getVaiTro();

        return vaiTro ==1;
    }
}