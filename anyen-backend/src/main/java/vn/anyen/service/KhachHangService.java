package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.request.KhachHangRequest;
import vn.anyen.dto.response.KhachHangLichSuResponse;
import vn.anyen.dto.response.KhachHangResponse;
import vn.anyen.entity.HoaDon;
import vn.anyen.entity.HopDong;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.ThongBao;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HoaDonRepository;
import vn.anyen.repository.HopDongRepository;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.ThongBaoRepository;
import vn.anyen.constants.AppLabels;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final DonHangRepository donHangRepository;
    private final HopDongRepository hopDongRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThongBaoRepository thongBaoRepository;

    private static final String STATUS_PREFIX = "[[TRANG_THAI_LAM_VIEC=";
    private static final String STATUS_SUFFIX = "]]";
    private static final Integer TB_CHUA_DOC = 0;
    private static final Integer TB_DA_DOC = 1;
    private static final Integer TB_DA_CHAP_NHAN = 2;
    private static final Integer TB_DA_TU_CHOI = 3;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<KhachHangResponse> getByNhanVien(Integer maNhanVien) {
        return khachHangRepository.findByMaNhanVienPhuTrach(maNhanVien)
                .stream()
                .map(kh -> toResponse(kh, maNhanVien))
                .toList();
    }

    @Transactional(readOnly = true)
    public KhachHangResponse getByIdResponse(Integer maKhachHang, Integer maNhanVien) {
        KhachHang kh = getById(maKhachHang);
        return toResponse(kh, maNhanVien);
    }

    public KhachHang getById(Integer maKhachHang) {
        return khachHangRepository.findById(maKhachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    @Transactional
    public KhachHangResponse create(KhachHangRequest request, Integer maNhanVienDangNhap) {
        KhachHang khachHang = KhachHang.builder()
                .tenKhachHang(trim(request.getTenKhachHang()))
                .cccd(trim(request.getCccd()))
                .diaChi(trim(request.getDiaChi()))
                .email(trim(request.getEmail()))
                .soDienThoai(trim(request.getSoDienThoai()))
                .maNhanVienPhuTrach(
                        request.getMaNhanVienPhuTrach() != null
                                ? request.getMaNhanVienPhuTrach()
                                : maNhanVienDangNhap
                )
                .ngayDangKy(
                        request.getNgayDangKy() != null
                                ? request.getNgayDangKy()
                                : LocalDateTime.now()
                )
                .nguonDangKy(trim(request.getNguonDangKy()))
                .nhuCauHoTro(trim(request.getNhuCauHoTro()))
                .ghiChu(cleanGhiChu(request.getGhiChu()))
                .build();

        KhachHang saved = khachHangRepository.save(khachHang);
        return toResponse(saved, maNhanVienDangNhap);
    }

    @Transactional
    public KhachHangResponse updateTrangThaiLamViec(
            Integer maKhachHang,
            Integer maNhanVien,
            String trangThaiLamViec
    ) {
        String status = normalizeStatus(trangThaiLamViec);
        KhachHang kh = getById(maKhachHang);

        if (kh.getMaNhanVienPhuTrach() != null
                && !kh.getMaNhanVienPhuTrach().equals(maNhanVien)) {
            throw new RuntimeException("Bạn không phụ trách khách hàng này");
        }

        kh.setGhiChu(putTrangThaiVaoGhiChu(kh.getGhiChu(), status));

        KhachHang saved = khachHangRepository.save(kh);
        return toResponse(saved, maNhanVien);
    }

    @Transactional(readOnly = true)
    public List<KhachHangLichSuResponse> getLichSu(Integer maKhachHang, Integer maNhanVien) {
        KhachHang kh = getById(maKhachHang);

        if (kh.getMaNhanVienPhuTrach() != null
                && !kh.getMaNhanVienPhuTrach().equals(maNhanVien)) {
            throw new RuntimeException("Bạn không phụ trách khách hàng này");
        }

        List<HistoryRow> rows = new ArrayList<>();

        if (kh.getNgayDangKy() != null) {
            rows.add(new HistoryRow(
                    kh.getNgayDangKy(),
                    "Khách hàng đăng ký",
                    "Nguồn: " + defaultText(kh.getNguonDangKy(), "Chưa cập nhật"),
                    "KHACH_HANG",
                    "Đã ghi nhận"
            ));
        }


        thongBaoRepository.findByMaKhachHangOrderByNgayTaoDesc(maKhachHang).forEach(tb -> {
            if (tb.getNgayTao() != null) {
                rows.add(new HistoryRow(
                        tb.getNgayTao(),
                        titleThongBao(tb),
                        defaultText(tb.getNoiDung(), "Không có nội dung"),
                        "THONG_BAO",
                        readableThongBaoStatus(tb.getTrangThai())
                ));
            }
        });

        donHangRepository.findByKhachHang_MaKhachHangOrderByNgayTaoDonDesc(maKhachHang).forEach(dh -> {
            LocalDateTime time = toDateTime(dh.getNgayTaoDon());

            rows.add(new HistoryRow(
                    time,
                    "Tạo đơn hàng #" + dh.getMaDonHang(),
                    "Tổng tiền: " + (dh.getTongTien() != null ? dh.getTongTien() + "đ" : "Chưa cập nhật")
                            + " • Thanh toán: " + defaultText(dh.getTrangThaiThanhToan(), "Chưa thanh toán"),
                    "DON_HANG",
                    defaultText(dh.getTrangThai(), "Chưa cập nhật")
            ));
        });

        hopDongRepository.findByDonHang_KhachHang_MaKhachHang(maKhachHang).forEach(hd -> {
            LocalDate date = hd.getNgayKyHD() != null ? hd.getNgayKyHD() : hd.getNgayViet();

            rows.add(new HistoryRow(
                    toDateTime(date),
                    "Tạo hợp đồng #" + hd.getMaHopDong(),
                    hd.getDonHang() != null
                            ? "Liên kết đơn hàng #" + hd.getDonHang().getMaDonHang()
                            : "Chưa liên kết đơn hàng",
                    "HOP_DONG",
                    defaultText(hd.getTrangThai(), "Chưa cập nhật")
            ));
        });

        hoaDonRepository.findByDonHang_KhachHang_MaKhachHang(maKhachHang).forEach(hd -> {
            rows.add(new HistoryRow(
                    toDateTime(hd.getNgayIn()),
                    "Tạo hóa đơn #" + hd.getMaHoaDon(),
                    "Tổng tiền: " + (hd.getTongTien() != null ? hd.getTongTien() + "đ" : "Chưa cập nhật")
                            + " • Phương thức: " + defaultText(hd.getPhuongThucThanhToan(), "Chưa cập nhật"),
                    "HOA_DON",
                    defaultText(hd.getTrangThai(), "Chưa cập nhật")
            ));
        });

        rows.sort(
                Comparator.comparing(
                        HistoryRow::time,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed()
        );

        return rows.stream()
                .map(row -> KhachHangLichSuResponse.builder()
                        .thoiGian(formatDateTime(row.time()))
                        .tieuDe(row.tieuDe())
                        .noiDung(row.noiDung())
                        .loai(row.loai())
                        .trangThai(row.trangThai())
                        .build())
                .toList();
    }

    private KhachHangResponse toResponse(KhachHang kh, Integer maNhanVien) {
        String stage = getGiaiDoanHienTai(kh, maNhanVien);
        String status = getTrangThaiHienTai(kh, stage);

        return KhachHangResponse.builder()
                .maKhachHang(kh.getMaKhachHang())
                .tenKhachHang(kh.getTenKhachHang())
                .cccd(kh.getCccd())
                .diaChi(kh.getDiaChi())
                .email(kh.getEmail())
                .soDienThoai(kh.getSoDienThoai())
                .maNhanVienPhuTrach(kh.getMaNhanVienPhuTrach())
                .ngayDangKy(formatDateTime(kh.getNgayDangKy()))
                .nguonDangKy(kh.getNguonDangKy())
                .nhuCauHoTro(kh.getNhuCauHoTro())
                .ghiChu(cleanGhiChu(kh.getGhiChu()))
                .trangThaiHienTai(status)
                .giaiDoanHienTai(stage)
                .avatar(getAvatar(kh.getTenKhachHang()))
                .build();
    }

    private String getGiaiDoanHienTai(KhachHang kh, Integer maNhanVien) {
        Integer maKhachHang = kh.getMaKhachHang();

        if (donHangRepository.existsDonHangDaThanhToanByKhachHang(maKhachHang)
                || hoaDonRepository.existsHoaDonDaThanhToanByKhachHang(maKhachHang)) {
            return "Hoàn thành";
        }

        if (hopDongRepository.existsByDonHang_KhachHang_MaKhachHang(maKhachHang)) {
            return "Quản lý dịch vụ";
        }

        if (donHangRepository.existsByKhachHang_MaKhachHang(maKhachHang)) {
            return "Chốt hợp đồng";
        }

        boolean daTiepNhan = false;

        if (maNhanVien != null) {
            daTiepNhan = thongBaoRepository.existsByMaKhachHangAndNguoiNhanIdAndTrangThai(
                    maKhachHang,
                    maNhanVien,
                    TB_DA_CHAP_NHAN
            );
        }

        if (daTiepNhan) {
            return "Chốt sản phẩm";
        }

        return "Hỗ trợ khách hàng";
    }

    private String getTrangThaiHienTai(KhachHang kh, String stage) {
        if ("Hoàn thành".equals(stage)) {
            return "Hoàn thành";
        }

        String saved = extractTrangThai(kh.getGhiChu());

        if (saved != null && !saved.isBlank()) {
            return saved;
        }

        if (!"Hỗ trợ khách hàng".equals(stage)) {
            return "Đang làm việc";
        }

        return "Tư vấn mới";
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        String value = status.trim();

        List<String> valid = List.of(
                "Tư vấn mới",
                "Đang làm việc",
                "Tạm dừng",
                "Hoàn thành"
        );

        if (!valid.contains(value)) {
            throw new RuntimeException(
                    "Trạng thái chỉ được là: Tư vấn mới, Đang làm việc, Tạm dừng, Hoàn thành"
            );
        }

        return value;
    }

    private String putTrangThaiVaoGhiChu(String ghiChu, String status) {
        String clean = cleanGhiChu(ghiChu);
        return STATUS_PREFIX + status + STATUS_SUFFIX + (clean.isBlank() ? "" : "\n" + clean);
    }

    private String extractTrangThai(String ghiChu) {
        if (ghiChu == null) return null;

        int start = ghiChu.indexOf(STATUS_PREFIX);
        if (start < 0) return null;

        int valueStart = start + STATUS_PREFIX.length();
        int end = ghiChu.indexOf(STATUS_SUFFIX, valueStart);

        if (end < 0) return null;

        return ghiChu.substring(valueStart, end).trim();
    }

    private String cleanGhiChu(String ghiChu) {
        if (ghiChu == null) return "";

        String result = ghiChu;

        while (result.contains(STATUS_PREFIX)) {
            int start = result.indexOf(STATUS_PREFIX);
            int end = result.indexOf(STATUS_SUFFIX, start);

            if (end < 0) break;

            result = result.substring(0, start)
                    + result.substring(end + STATUS_SUFFIX.length());
        }

        return result.trim();
    }

    private String getAvatar(String name) {
        if (name == null || name.isBlank()) return "KH";

        String[] arr = name.trim().split("\\s+");

        if (arr.length == 1) {
            return arr[0].substring(0, Math.min(2, arr[0].length())).toUpperCase();
        }

        return (arr[0].substring(0, 1)
                + arr[arr.length - 1].substring(0, 1)).toUpperCase();
    }

    private String titleThongBao(ThongBao tb) {
        if ("DA_CHAP_NHAN".equals(tb.getTrangThai())) return "Tiếp nhận khách hàng";
        if ("DA_TU_CHOI".equals(tb.getTrangThai())) return "Từ chối tiếp nhận";

        return defaultText(tb.getTieuDe(), "Thông báo công việc");
    }

    private String readableThongBaoStatus(Integer status) {
        if (TB_DA_CHAP_NHAN.equals(status)) return "Đã chấp nhận";
        if (TB_DA_TU_CHOI.equals(status)) return "Đã từ chối";
        if (TB_DA_DOC.equals(status)) return "Đã đọc";
        if (TB_CHUA_DOC.equals(status)) return "Chưa đọc";

        return defaultText(status, "Chưa cập nhật");
    }

    private LocalDateTime toDateTime(LocalDate date) {
        return date == null ? null : LocalDateTime.of(date, LocalTime.MIN);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String defaultText(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String text = String.valueOf(value);

        return text.isBlank() ? fallback : text;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private record HistoryRow(
            LocalDateTime time,
            String tieuDe,
            String noiDung,
            String loai,
            String trangThai
    ) {
    }
}