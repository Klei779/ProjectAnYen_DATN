package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.GiaoCongViecRequest;
import vn.anyen.dto.response.GiaoCongViecResponse;
import vn.anyen.dto.response.NhanVienGanNhatResponse;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.ThongBao;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.ThongBaoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotlineCongViecService {

    private static final Integer VAI_TRO_NHAN_VIEN_TRUC_TIEP = NhanVien.VAI_TRO_BAN_HANG;

    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThongBaoRepository thongBaoRepository;

    @Transactional(readOnly = true)
    public List<NhanVienGanNhatResponse> getNhanVienTrucTiep(
            Authentication authentication,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        requireHotline(authentication);

        return nhanVienRepository
                .findByVaiTroAndTrangThaiOrderByHoTenAsc(
                        VAI_TRO_NHAN_VIEN_TRUC_TIEP,
                        NhanVien.TRANG_THAI_HOAT_DONG
                )
                .stream()
                .map(nv -> toNearestResponse(nv, latitude, longitude))
                .sorted(Comparator
                        .comparing(
                                NhanVienGanNhatResponse::getKhoangCachKm,
                                Comparator.nullsLast(Double::compareTo)
                        )
                        .thenComparing(NhanVienGanNhatResponse::getHoTen, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional
    public GiaoCongViecResponse giaoCongViec(
            Authentication authentication,
            GiaoCongViecRequest request
    ) {
        NhanVien hotline = requireHotline(authentication);

        NhanVien nhanVien = nhanVienRepository.findById(request.getMaNhanVien())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy nhân viên trực tiếp"
                ));

        if (!VAI_TRO_NHAN_VIEN_TRUC_TIEP.equals(nhanVien.getVaiTro())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ được giao việc cho nhân viên trực tiếp"
            );
        }

        if (!NhanVien.TRANG_THAI_HOAT_DONG.equals(nhanVien.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nhân viên được chọn hiện không hoạt động"
            );
        }

        KhachHang khachHang = KhachHang.builder()
                .tenKhachHang(trim(request.getTenKhachHang()))
                .soDienThoai(trim(request.getSoDienThoai()))
                .diaChi(trim(request.getDiaChi()))
                .maNhanVienPhuTrach(null)
                .ngayDangKy(LocalDateTime.now())
                .nguonDangKy("Hotline")
                .nhuCauHoTro(trimToNull(request.getNhuCauHoTro()))
                .ghiChu(trimToNull(request.getGhiChu()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        KhachHang savedCustomer = khachHangRepository.save(khachHang);

        String noiDung = "Khách hàng " + savedCustomer.getTenKhachHang()
                + " cần được tiếp nhận tại " + savedCustomer.getDiaChi();

        ThongBao thongBao = ThongBao.builder()
                .tieuDe("Công việc mới từ Hotline")
                .noiDung(noiDung)
                .loaiThongBao("CONG_VIEC")
                .nguoiGuiId(hotline.getMaNhanVien())
                .nguoiNhanId(nhanVien.getMaNhanVien())
                .maKhachHang(savedCustomer.getMaKhachHang())
                .trangThai(0)
                .build();

        ThongBao savedNotification = thongBaoRepository.save(thongBao);

        return GiaoCongViecResponse.builder()
                .maKhachHang(savedCustomer.getMaKhachHang())
                .maThongBao(savedNotification.getMaThongBao())
                .maNhanVien(nhanVien.getMaNhanVien())
                .tenNhanVien(nhanVien.getHoTen())
                .message("Đã giao công việc cho " + nhanVien.getHoTen())
                .build();
    }

    private NhanVien requireHotline(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }

        NhanVien hotline = nhanVienRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Không tìm thấy tài khoản Hotline"
                ));

        boolean laNhanVienTrucTuyen = NhanVien.VAI_TRO_TU_VAN.equals(hotline.getVaiTro());
        boolean laNhanVienHotline = NhanVien.VAI_TRO_HOTLINE.equals(hotline.getVaiTro());

        if (!laNhanVienTrucTuyen && !laNhanVienHotline) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ nhân viên trực tuyến hoặc Hotline được phép giao công việc"
            );
        }

        return hotline;
    }

    private NhanVienGanNhatResponse toNearestResponse(
            NhanVien nv,
            BigDecimal customerLat,
            BigDecimal customerLng
    ) {
        Double distance = calculateDistanceKm(
                customerLat,
                customerLng,
                nv.getLatitude(),
                nv.getLongitude()
        );

        return NhanVienGanNhatResponse.builder()
                .maNhanVien(nv.getMaNhanVien())
                .hoTen(nv.getHoTen())
                .soDienThoai(nv.getSoDienThoai())
                .diaChi(nv.getDiaChi())
                .latitude(nv.getLatitude())
                .longitude(nv.getLongitude())
                .khoangCachKm(distance)
                .trangThai("Sẵn sàng")
                .build();
    }

    private Double calculateDistanceKm(
            BigDecimal lat1,
            BigDecimal lon1,
            BigDecimal lat2,
            BigDecimal lon2
    ) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        double earthRadiusKm = 6371.0088;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double originLat = Math.toRadians(lat1.doubleValue());
        double destinationLat = Math.toRadians(lat2.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(originLat) * Math.cos(destinationLat)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(earthRadiusKm * c)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
