package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.BaoCaoSuCoRequest;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.request.HuyDonHangRequest;
import vn.anyen.dto.request.SoTienRequest;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.dto.response.NhanVienDeXuatResponse;
import vn.anyen.dto.response.PayooMockResponse;
import vn.anyen.dto.response.SanPhamTaoDonHangResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.service.DonHangService;
import vn.anyen.service.PayooMockService;
import vn.anyen.service.SanPhamService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nhan-vien/don-hang")
@RequiredArgsConstructor
public class NhanVienDonHangController {

    private final DonHangService donHangService;
    private final SanPhamService sanPhamService;
    private final NhanVienRepository nhanVienRepository;
    private final PayooMockService payooMockService;

    @GetMapping
    public ResponseEntity<List<DonHangResponse>> getDonHangCuaToi(
            Authentication authentication
    ) {
        NhanVien nhanVien = nhanVienRepository
                .findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên đăng nhập"
                ));

        List<DonHangResponse> donHangs = donHangService
                .getDonHangByNhanVien(nhanVien.getMaNhanVien());

        return ResponseEntity.ok(donHangs);
    }

    @PostMapping
    public ResponseEntity<DonHangResponse> taoDonHang(
            @Valid @RequestBody TaoDonHangRequest request,
            Authentication authentication
    ) {
        DonHangResponse response = donHangService.taoDonHang(
                request,
                authentication
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{maDonHang}/co-hop-dong")
    public ResponseEntity<Map<String, Boolean>> kiemTraDonHangCoHopDong(
            @PathVariable Integer maDonHang
    ) {
        boolean daCoHopDong = donHangService.kiemTraDonHangDaCoHopDong(maDonHang);

        return ResponseEntity.ok(Map.of("daCoHopDong", daCoHopDong));
    }

    @PutMapping("/{maDonHang}")
    public ResponseEntity<DonHangResponse> capNhatDonHang(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody TaoDonHangRequest request
    ) {
        return ResponseEntity.ok(
                donHangService.capNhatDonHang(maDonHang, request)
        );
    }

    @PutMapping("/{maDonHang}/trang-thai")
    public ResponseEntity<DonHangResponse> capNhatTrangThaiDonHang(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody CapNhatTrangThaiDonHangRequest request
    ) {
        DonHangResponse response = donHangService.capNhatTrangThaiNhanVien(
                maDonHang,
                request.getTrangThai()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{maDonHang}/huy")
    public DonHangResponse huyDonHang(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody HuyDonHangRequest request,
            Authentication authentication
    ) {
        return donHangService.huyDonHang(maDonHang, request, authentication);
    }

    @GetMapping("/san-pham-options")
    public ResponseEntity<List<SanPhamTaoDonHangResponse>> getSanPhamTaoDonHangOptions() {
        return ResponseEntity.ok(
                sanPhamService.getSanPhamTaoDonHangOptions()
        );
    }

    @GetMapping("/de-xuat-nhan-vien")
    public ResponseEntity<List<NhanVienDeXuatResponse>> getNhanVienDeXuat(
            @RequestParam Double lat,
            @RequestParam Double lng
    ) {
        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Thiếu tọa độ khách hàng");
        }

        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Tọa độ khách hàng không hợp lệ");
        }

        List<NhanVien> nhanVienList = nhanVienRepository.findNhanVienTrucTiepCoToaDoDangHoatDong();

        List<NhanVienDeXuatResponse> responses = nhanVienList.stream()
                .filter(nv -> nv.getLatitude() != null && nv.getLongitude() != null)
                .map(nv -> {
                    double distance = calculateDistance(
                            lat, lng,
                            nv.getLatitude().doubleValue(),
                            nv.getLongitude().doubleValue()
                    );

                    return NhanVienDeXuatResponse.builder()
                            .maNhanVien(nv.getMaNhanVien())
                            .hoTen(nv.getHoTen())
                            .soDienThoai(nv.getSoDienThoai())
                            .tinhThanh(null)
                            .trangThaiLamViec("RANH")
                            .trangThaiLamViecText("Rảnh")
                            .khoangCach(distance)
                            .khoangCachText(formatDistance(distance))
                            .donDangXuLy(0L)
                            .donHoanThanh(0L)
                            .diem(0.0)
                            .latitude(nv.getLatitude())
                            .longitude(nv.getLongitude())
                            .build();
                })
                .sorted(Comparator.comparing(NhanVienDeXuatResponse::getKhoangCach))
                .limit(5)
                .toList();

        return ResponseEntity.ok(responses);
    }
    
    // Tính khoảng cách giữa 2 điểm (Haversine formula)
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    // Format khoảng cách
    private String formatDistance(double distance) {
        if (distance < 1) {
            return String.format("%.0f m", distance * 1000);
        }
        return String.format("%.2f km", distance);
    }

    @PostMapping("/{maDonHang}/bao-cao-su-co")
    public ResponseEntity<Void> baoCaoSuCo(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody BaoCaoSuCoRequest request,
            Authentication authentication
    ) {
        donHangService.baoCaoSuCo(maDonHang, request.getLyDoSuCo(), authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{maDonHang}/giai-quyet-su-co")
    public ResponseEntity<Void> giaiQuyetSuCo(
            @PathVariable Integer maDonHang,
            Authentication authentication
    ) {
        donHangService.giaiQuyetSuCo(maDonHang, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{maDonHang}/payoo")
    public ResponseEntity<PayooMockResponse> taoPayooThanhToan(
            @PathVariable Integer maDonHang,
            @Valid @RequestBody SoTienRequest request
    ) {
        PayooMockResponse response = payooMockService.taoThanhToanDonHang(
                maDonHang,
                request.getSoTien()
        );
        return ResponseEntity.ok(response);
    }
}