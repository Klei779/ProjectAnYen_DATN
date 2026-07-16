package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.anyen.entity.HDongCT;
import vn.anyen.repository.HDongCTRepository;
import java.time.LocalDateTime;
import vn.anyen.dto.request.HopDongCreateRequest;
import vn.anyen.dto.response.DonHangHopDongDetailResponse;
import vn.anyen.dto.response.DonHangHopDongOptionResponse;
import vn.anyen.dto.response.HopDongPageResponse;
import vn.anyen.dto.response.HopDongResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HopDong;
import vn.anyen.entity.KhachHang;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HopDongRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HopDongService {

    private final HopDongRepository hopDongRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final HDongCTRepository hDongCTRepository;

    public HopDongPageResponse getHopDongs(
            String keyword,
            String trangThai,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "maHopDong")
        );

        String searchKeyword = normalizeSearchKeyword(keyword);

        Page<HopDong> hopDongPage =
                hopDongRepository.searchHopDong(searchKeyword, trangThai, pageable);

        return HopDongPageResponse.builder()
                .items(
                        hopDongPage.getContent()
                                .stream()
                                .map(this::toResponse)
                                .toList()
                )
                .total(hopDongPage.getTotalElements())
                .build();
    }

    public HopDongResponse getChiTiet(Integer id) {
        HopDong hopDong = hopDongRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy hợp đồng"
                        )
                );

        return toResponse(hopDong);
    }

    @Transactional
    public HopDongResponse huyHopDong(Integer id) {
        HopDong hopDong = hopDongRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy hợp đồng"
                        )
                );

        if (laHopDongChuaKy(hopDong.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hợp đồng chưa ký không cần hủy. Bạn có thể xóa hợp đồng"
            );
        }

        if (laHopDongDaHuy(hopDong.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hợp đồng này đã bị hủy trước đó"
            );
        }

        hopDong.setTrangThai("Đã hủy");

        HopDong saved = hopDongRepository.save(hopDong);

        return toResponse(saved);
    }
    public List<DonHangHopDongOptionResponse> getDonHangOptions() {
        return donHangRepository
                .findAll(Sort.by(Sort.Direction.DESC, "maDonHang"))
                .stream()
                .map(this::toDonHangOptionResponse)
                .toList();
    }

    public DonHangHopDongDetailResponse getDonHangDetail(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy đơn hàng"
                        )
                );

        return toDonHangDetailResponse(donHang);
    }

    @Transactional
    public HopDongResponse taoHopDong(HopDongCreateRequest request) {

        if (request.getMaDonHang() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn đơn hàng trước khi lưu hợp đồng"
            );
        }

        DonHang donHang = donHangRepository.findById(request.getMaDonHang())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Đơn hàng không tồn tại"
                        )
                );

        boolean daCoHopDong =
                hopDongRepository.existsByDonHang_MaDonHang(request.getMaDonHang());

        if (daCoHopDong) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng này đã có hợp đồng rồi"
            );
        }

        LocalDate today = LocalDate.now();

        LocalDate ngayKyHD = parseDateOrDefault(request.getNgayKyHD(), today);
        LocalDate ngayViet = parseDateOrDefault(request.getNgayViet(), today);

        LocalDate thoiHanKetThuc = parseDateOrNull(request.getThoiHanKetThuc());

        if (thoiHanKetThuc == null) {
            thoiHanKetThuc = parseDateOrNull(request.getNgayKetThuc());
        }

        HopDong hopDong = HopDong.builder()
                .donHang(donHang)
                .ngayKyHD(ngayKyHD)
                .ngayViet(ngayViet)
                .thoiHanKetThuc(thoiHanKetThuc)
                .trangThai(
                        request.getTrangThai() != null
                                && !request.getTrangThai().isBlank()
                                ? request.getTrangThai()
                                : "Chờ ký"
                )
                .build();

        /*
         * MaHopDong là INT AUTO_INCREMENT trong database.
         * Khi save, database tự tăng MaHopDong.
         * Sau đó backend format MaHopDong thành HD0000001 để trả ra frontend.
         */
        HopDong saved = hopDongRepository.saveAndFlush(hopDong);

        if (hasHopDongChiTiet(request)) {
            HDongCT chiTiet = HDongCT.builder()
                    .hopDong(saved)
                    .hoTenNguoiMat(request.getHoTenNguoiMat())
                    .ngayMat(parseDateOrNull(request.getNgayMat()))
                    .ngaySinh(parseDateOrNull(request.getNgaySinh()))
                    .gioiTinh(request.getGioiTinh())
                    .soGiayBaoTu(request.getSoGiayBaoTu())
                    .noiCapGiayBaoTu(request.getNoiCapGiayBaoTu())
                    .coSoMaiTang(request.getCoSoMaiTang())
                    .khuMo(request.getKhuMo())
                    .soMo(request.getSoMo())
                    .ngayGioAnTang(parseDateTimeOrNull(request.getNgayGioAnTang()))
                    .build();

            hDongCTRepository.save(chiTiet);
        }

        return toResponse(saved);
    }

    private DonHangHopDongOptionResponse toDonHangOptionResponse(DonHang donHang) {
        KhachHang khachHang = donHang.getKhachHang();

        return DonHangHopDongOptionResponse.builder()
                .maDonHang(donHang.getMaDonHang())
                .maDonHangText(formatDonHangCode(donHang.getMaDonHang()))
                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : null
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : null
                )
                .ngayTaoDon(donHang.getNgayTaoDon())
                .tongTien(donHang.getTongTien())
                .trangThai(readableDonHangStatus(donHang.getTrangThai()))
                .daCoHopDong(
                        hopDongRepository.existsByDonHang_MaDonHang(
                                donHang.getMaDonHang()
                        )
                )
                .build();
    }

    private DonHangHopDongDetailResponse toDonHangDetailResponse(DonHang donHang) {
        KhachHang khachHang = donHang.getKhachHang();

        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository.findByDonHang_MaDonHang(
                        donHang.getMaDonHang()
                );

        List<DonHangHopDongDetailResponse.SanPhamTrongDonHangResponse> sanPhams =
                chiTiets.stream()
                        .map(ct -> {
                            BigDecimal giaTien =
                                    ct.getGiaTien() != null
                                            ? ct.getGiaTien()
                                            : BigDecimal.ZERO;

                            int soLuong =
                                    ct.getSoLuong() != null
                                            ? ct.getSoLuong()
                                            : 0;

                            return DonHangHopDongDetailResponse
                                    .SanPhamTrongDonHangResponse
                                    .builder()
                                    .maSanPham(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getMaSanPham()
                                                    : null
                                    )
                                    .tenSanPham(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getTenSanPham()
                                                    : null
                                    )
                                    .loai(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getLoai()
                                                    : null
                                    )
                                    .soLuong(soLuong)
                                    .giaTien(giaTien)
                                    .thanhTien(
                                            giaTien.multiply(
                                                    BigDecimal.valueOf(soLuong)
                                            )
                                    )
                                    .hinhAnh(
                                            ct.getSanPham() != null
                                                    ? ct.getSanPham().getHinhAnh()
                                                    : null
                                    )
                                    .build();
                        })
                        .toList();

        return DonHangHopDongDetailResponse.builder()
                .maDonHang(donHang.getMaDonHang())
                .maDonHangText(formatDonHangCode(donHang.getMaDonHang()))
                .ngayTaoDon(donHang.getNgayTaoDon())
                .tongTien(donHang.getTongTien())
                .trangThai(readableDonHangStatus(donHang.getTrangThai()))
                .ghiChu(donHang.getGhiChu())

                .maKhachHang(
                        khachHang != null
                                ? khachHang.getMaKhachHang()
                                : null
                )
                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : null
                )
                .cccd(
                        khachHang != null
                                ? khachHang.getCccd()
                                : null
                )
                .email(
                        khachHang != null
                                ? khachHang.getEmail()
                                : null
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : null
                )
                .diaChi(
                        khachHang != null
                                ? khachHang.getDiaChi()
                                : null
                )

                .maNhanVien(
                        donHang.getNhanVien() != null
                                ? donHang.getNhanVien().getMaNhanVien()
                                : null
                )
                .tenNhanVien(
                        donHang.getNhanVien() != null
                                ? donHang.getNhanVien().getHoTen()
                                : null
                )
                .sanPhams(sanPhams)
                .build();
    }

    public HopDongResponse toResponse(HopDong hopDong) {
        DonHang donHang = hopDong.getDonHang();

        KhachHang khachHang =
                donHang != null
                        ? donHang.getKhachHang()
                        : null;

        String maHopDongText = formatHopDongCode(hopDong.getMaHopDong());

        HDongCT chiTiet = hDongCTRepository
                .findFirstByHopDong_MaHopDong(hopDong.getMaHopDong())
                .orElse(null);

        HopDongResponse.HopDongResponseBuilder builder = HopDongResponse.builder()
                .maHopDong(hopDong.getMaHopDong())
                .soHopDong(maHopDongText)
                .maHopDongText(maHopDongText)

                .maDonHang(
                        donHang != null
                                ? donHang.getMaDonHang()
                                : null
                )
                .maDonHangText(
                        donHang != null
                                ? formatDonHangCode(donHang.getMaDonHang())
                                : null
                )
                .ngayTaoDon(
                        donHang != null
                                ? donHang.getNgayTaoDon()
                                : null
                )
                .giaTriHopDong(
                        donHang != null
                                ? donHang.getTongTien()
                                : null
                )

                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : null
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : null
                )

                .ngayKyHD(hopDong.getNgayKyHD())
                .ngayViet(hopDong.getNgayViet())
                .thoiHanKetThuc(hopDong.getThoiHanKetThuc())
                .ngayKetThuc(hopDong.getThoiHanKetThuc())
                .ngayHetHan(hopDong.getThoiHanKetThuc())
                .trangThai(hopDong.getTrangThai())
                .an(Boolean.TRUE.equals(hopDong.getAn()));

        if (chiTiet != null) {
            builder
                    .maHDongCT(chiTiet.getMaHDongCT())
                    .hoTenNguoiMat(chiTiet.getHoTenNguoiMat())
                    .ngayMat(chiTiet.getNgayMat())
                    .ngaySinh(chiTiet.getNgaySinh())
                    .gioiTinh(chiTiet.getGioiTinh())
                    .soGiayBaoTu(chiTiet.getSoGiayBaoTu())
                    .noiCapGiayBaoTu(chiTiet.getNoiCapGiayBaoTu())
                    .coSoMaiTang(chiTiet.getCoSoMaiTang())
                    .khuMo(chiTiet.getKhuMo())
                    .soMo(chiTiet.getSoMo())
                    .ngayGioAnTang(chiTiet.getNgayGioAnTang());
        }

        return builder.build();
    }

    private String formatHopDongCode(Integer id) {
        if (id == null) return "";
        return String.format("HD%07d", id);
    }

    private String formatDonHangCode(Integer id) {
        if (id == null) return "";
        return String.format("DH%04d", id);
    }

    private String normalizeSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }

        String value = keyword.trim();
        String upperValue = value.toUpperCase();

        /*
         * Nếu frontend search HD0000001 thì database chỉ có MaHopDong = 1.
         * Nên đổi HD0000001 thành 1 để query tìm được.
         */
        if (upperValue.matches("^HD0*\\d+$")) {
            return upperValue.replaceFirst("^HD0*", "");
        }

        if (upperValue.matches("^DH0*\\d+$")) {
            return upperValue.replaceFirst("^DH0*", "");
        }

        return value;
    }

    private String readableDonHangStatus(Integer status) {
        if (DonHang.TT_MOI_TAO.equals(status)) return "Mới tạo";
        if (DonHang.TT_CHO_DOI_TAC_XAC_NHAN.equals(status)) return "Chờ đối tác xác nhận";
        if (DonHang.TT_DA_XAC_NHAN.equals(status)) return "Đã xác nhận";
        if (DonHang.TT_DANG_XU_LY.equals(status)) return "Đang xử lý";
        if (DonHang.TT_CHO_THANH_TOAN.equals(status)) return "Chờ thanh toán";
        if (DonHang.TT_HOAN_THANH.equals(status)) return "Hoàn thành";
        if (DonHang.TT_DA_HUY.equals(status)) return "Đã hủy";
        return "Chưa cập nhật";
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultValue) {
        LocalDate parsedDate = parseDateOrNull(value);
        return parsedDate != null ? parsedDate : defaultValue;
    }

    private LocalDate parseDateOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String dateText = value.trim();

        /*
         * Nhận các kiểu:
         * 2026-06-18
         * 2026-06-18T10:30:00
         * 18/06/2026
         * 18/06/2026, 10:30
         */
        if (dateText.length() >= 10) {
            dateText = dateText.substring(0, 10);
        }

        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(
                    dateText,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
        } catch (DateTimeParseException ignored) {
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ngày không hợp lệ: " + value
        );
    }
    public String getNextHopDongCode() {
        Integer maxId = hopDongRepository.getMaxMaHopDong();

        int nextId = maxId == null ? 1 : maxId + 1;

        return formatHopDongCode(nextId);
    }
    private boolean hasHopDongChiTiet(HopDongCreateRequest request) {
        return isNotBlank(request.getHoTenNguoiMat())
                || isNotBlank(request.getNgayMat())
                || isNotBlank(request.getNgaySinh())
                || isNotBlank(request.getGioiTinh())
                || isNotBlank(request.getSoGiayBaoTu())
                || isNotBlank(request.getNoiCapGiayBaoTu())
                || isNotBlank(request.getCoSoMaiTang())
                || isNotBlank(request.getKhuMo())
                || isNotBlank(request.getSoMo())
                || isNotBlank(request.getNgayGioAnTang());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private LocalDateTime parseDateTimeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String dateText = value.trim();

        try {
            return LocalDateTime.parse(dateText);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(
                    dateText,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(dateText.substring(0, 10)).atStartOfDay();
        } catch (Exception ignored) {
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ngày giờ không hợp lệ: " + value
        );
    }
    private boolean laHopDongChuaKy(String trangThai) {
        if (trangThai == null) {
            return false;
        }

        String value = trangThai.trim();

        return "Chờ ký".equalsIgnoreCase(value)
                || "Mới tạo".equalsIgnoreCase(value)
                || "Chưa ký".equalsIgnoreCase(value);
    }

    private boolean laHopDongDaHuy(String trangThai) {
        return trangThai != null
                && "Đã hủy".equalsIgnoreCase(trangThai.trim());
    }

    /**
     * Chỉ xóa hợp đồng khi chưa ký.
     * Không xóa đơn hàng, hóa đơn hoặc công nợ.
     */
    @Transactional
    public void xoaHopDongChuaKy(Integer id) {
        HopDong hopDong = hopDongRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy hợp đồng"
                        )
                );

        if (!laHopDongChuaKy(hopDong.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hợp đồng đã ký không thể xóa. Bạn chỉ có thể hủy hợp đồng"
            );
        }

        hDongCTRepository.deleteAll(
                hDongCTRepository.findByHopDong_MaHopDong(
                        hopDong.getMaHopDong()
                )
        );

        hopDongRepository.delete(hopDong);
    }
}