package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.constants.AppLabels;
import vn.anyen.dto.request.CapNhatNhanVienRequest;
import vn.anyen.dto.request.QuanLyNhanVienRequest;
import vn.anyen.dto.response.NhanVienDeXuatResponse;
import vn.anyen.dto.response.QuanLyNhanVienResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.NhanVienRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuanLyNhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final DonHangRepository donHangRepository;

    @Transactional
    public QuanLyNhanVienResponse createQuanLyNhanVien(QuanLyNhanVienRequest request) {
        String tenDangNhap = request.getTenDangNhap().trim();
        String email = request.getEmail().trim();
        String soDienThoai = request.getSoDienThoai().trim();

        if (nhanVienRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (nhanVienRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (nhanVienRepository.existsBySoDienThoai(soDienThoai)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        NhanVien nhanVien = NhanVien.builder()
                .hoTen(request.getHoTen().trim())
                .tenDangNhap(tenDangNhap)
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .email(email)
                .soDienThoai(soDienThoai)
                .diaChi(trimToNull(request.getDiaChi()))
                .vaiTro(request.getVaiTro())
                .trangThai(NhanVien.TRANG_THAI_HOAT_DONG)
                .build();

        return mapToResponse(nhanVienRepository.save(nhanVien));
    }

    /**
     * Cập nhật thông tin nhân viên.
     * SecurityConfig chỉ cho ROLE_ADMIN, tương ứng nhân viên có VaiTro = 1,
     * truy cập endpoint quản lý nhân viên.
     */
    @Transactional
    public QuanLyNhanVienResponse capNhatNhanVien(
            Integer maNhanVien,
            CapNhatNhanVienRequest request
    ) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với mã: " + maNhanVien
                ));

        String tenDangNhap = request.getTenDangNhap().trim();
        String email = request.getEmail().trim();
        String soDienThoai = request.getSoDienThoai().trim();

        if (nhanVienRepository.existsByTenDangNhapAndMaNhanVienNot(
                tenDangNhap, maNhanVien)) {
            throw new RuntimeException("Tên đăng nhập đã được nhân viên khác sử dụng");
        }
        if (nhanVienRepository.existsByEmailAndMaNhanVienNot(email, maNhanVien)) {
            throw new RuntimeException("Email đã được nhân viên khác sử dụng");
        }
        if (nhanVienRepository.existsBySoDienThoaiAndMaNhanVienNot(
                soDienThoai, maNhanVien)) {
            throw new RuntimeException("Số điện thoại đã được nhân viên khác sử dụng");
        }

        nhanVien.setHoTen(request.getHoTen().trim());
        nhanVien.setTenDangNhap(tenDangNhap);
        nhanVien.setEmail(email);
        nhanVien.setSoDienThoai(soDienThoai);
        nhanVien.setDiaChi(trimToNull(request.getDiaChi()));
        nhanVien.setVaiTro(request.getVaiTro());

        // Mật khẩu là tùy chọn khi sửa. Để trống/null thì giữ mật khẩu cũ.
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            nhanVien.setMatKhau(passwordEncoder.encode(request.getMatKhau().trim()));
        }

        return mapToResponse(nhanVienRepository.save(nhanVien));
    }

    @Transactional
    public QuanLyNhanVienResponse nghiViecNhanVien(Integer maNhanVien) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với mã: " + maNhanVien
                ));

        if (NhanVien.TRANG_THAI_NGHI_VIEC.equals(nhanVien.getTrangThai())) {
            throw new RuntimeException("Nhân viên này đã nghỉ việc từ trước");
        }

        nhanVien.setTrangThai(NhanVien.TRANG_THAI_NGHI_VIEC);
        return mapToResponse(nhanVienRepository.save(nhanVien));
    }

    @Transactional(readOnly = true)
    public List<QuanLyNhanVienResponse> getAllNhanVien() {
        return nhanVienRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private QuanLyNhanVienResponse mapToResponse(NhanVien nhanVien) {
        return QuanLyNhanVienResponse.builder()
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .tenDangNhap(nhanVien.getTenDangNhap())
                .email(nhanVien.getEmail())
                .soDienThoai(nhanVien.getSoDienThoai())
                .diaChi(nhanVien.getDiaChi())
                .vaiTro(nhanVien.getVaiTro())
                .trangThai(nhanVien.getTrangThai())
                .tenTrangThai(AppLabels.getLabel(
                        AppLabels.TRANG_THAI_NHAN_VIEN,
                        nhanVien.getTrangThai()
                ))
                .tenVaiTro(AppLabels.getLabel(AppLabels.TEN_VAI_TRO,nhanVien.getVaiTro()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<NhanVienDeXuatResponse> getNhanVienDeXuat(Double customerLat, Double customerLng) {
        // Lấy danh sách nhân viên tư vấn đang hoạt động và có tọa độ
        List<NhanVien> activeConsultants = nhanVienRepository.findActiveConsultants();

        return activeConsultants.stream()
                .map(nv -> {
                    // Tính khoảng cách bằng Haversine formula
                    double distance = calculateDistance(
                            customerLat, customerLng,
                            nv.getLatitude() != null ? nv.getLatitude().doubleValue() : 0,
                            nv.getLongitude() != null ? nv.getLongitude().doubleValue() : 0
                    );

                    // Đếm số đơn đang xử lý (loại trừ hoàn thành/hủy)
                    long donDangXuLy = donHangRepository.countDonDangXuLyByNhanVien(nv.getMaNhanVien());

                    // Tính điểm: khoảng cách (km) + (số đơn đang xử lý * 2)
                    // Càng thấp càng tốt
                    double diem = distance + (donDangXuLy * 2);

                    return NhanVienDeXuatResponse.builder()
                            .maNhanVien(nv.getMaNhanVien())
                            .hoTen(nv.getHoTen())
                            .soDienThoai(nv.getSoDienThoai())
                            .diaChiDayDu(nv.getDiaChiDayDu())
                            .tinhThanh(nv.getTinhThanh())
                            .trangThaiLamViec(nv.getTrangThaiLamViec() != null ? nv.getTrangThaiLamViec() : "RANH")
                            .trangThaiLamViecText(formatTrangThaiLamViec(nv.getTrangThaiLamViec()))
                            .khoangCach(distance)
                            .khoangCachText(formatKhoangCach(distance))
                            .donDangXuLy(donDangXuLy)
                            .donHoanThanh(0L) // TODO: Cần thêm query đếm đơn hoàn thành
                            .diem(diem)
                            .latitude(nv.getLatitude())
                            .longitude(nv.getLongitude())
                            .build();
                })
                .sorted(Comparator.comparing(NhanVienDeXuatResponse::getDiem))
                .limit(5)
                .collect(Collectors.toList());
    }

    // Haversine formula để tính khoảng cách giữa 2 tọa độ (km)
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // Bán kính trái đất (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private String formatKhoangCach(double distance) {
        if (distance < 1) {
            return String.format("%.0f m", distance * 1000);
        }
        return String.format("%.1f km", distance);
    }

    private String formatTrangThaiLamViec(String trangThai) {
        if (trangThai == null) return "Rảnh";
        return switch (trangThai) {
            case "RANH" -> "Rảnh";
            case "BAN" -> "Bận";
            case "NGHI" -> "Nghỉ";
            default -> "Rảnh";
        };
    }
}