package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.response.ThongBaoResponse;
import vn.anyen.entity.*;
import vn.anyen.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DoiTacRepository doiTacRepository;
    private final ThongBaoDoiTacRepository thongBaoDoiTacRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    private static final String LOAI_DON_HANG = "DON_HANG";
    private static final String LOAI_CONG_VIEC = "CONG_VIEC";
    private static final String LOAI_DUYET_SAN_PHAM = "DUYET_SAN_PHAM";

    private static final Integer TRANG_THAI_CHUA_DOC = 0;
    private static final Integer TRANG_THAI_DA_DOC = 1;
    private static final Integer TRANG_THAI_DA_CHAP_NHAN = 2;
    private static final Integer TRANG_THAI_DA_TU_CHOI = 3;


    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    /**
     * Lấy danh sách thông báo cho nhân viên.
     * Bao gồm:
     * - Thông báo cá nhân
     * - Thông báo broadcast có NguoiNhanId = null
     */
    public List<ThongBaoResponse> getThongBaoByNguoiNhan(Integer nguoiNhanId) {
        List<ThongBao> list = thongBaoRepository.findByNguoiNhan(nguoiNhanId);

        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đếm thông báo chưa đọc.
     */
    public long countChuaDoc(Integer nguoiNhanId) {
        return thongBaoRepository.countChuaDoc(nguoiNhanId);
    }

    /**
     * Chấp nhận thông báo.
     *
     * Nếu là CONG_VIEC:
     * - Gán nhân viên phụ trách khách hàng.
     *
     * Nếu là DUYET_SAN_PHAM:
     * - Chuyển sản phẩm sang Đang bán.
     * - Bật HienThi = true.
     * - Gửi thông báo về đối tác: sản phẩm đã được duyệt.
     */
    @Transactional
    public void chapNhan(Integer maThongBao, Integer nguoiNhanId) {
        ThongBao thongBao = thongBaoRepository.findById(maThongBao)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (TRANG_THAI_DA_CHAP_NHAN.equals(thongBao.getTrangThai())
                || TRANG_THAI_DA_TU_CHOI.equals(thongBao.getTrangThai())) {
            throw new RuntimeException("Thông báo này đã được xử lý");
        }

        if (LOAI_DUYET_SAN_PHAM.equals(thongBao.getLoaiThongBao())) {
            chapNhanDuyetSanPham(thongBao, nguoiNhanId);
            return;
        }

        if (LOAI_CONG_VIEC.equals(thongBao.getLoaiThongBao())) {
            chapNhanCongViec(thongBao, nguoiNhanId);
            return;
        }

        throw new RuntimeException("Loại thông báo này không hỗ trợ chấp nhận");
    }

    private void chapNhanDuyetSanPham(ThongBao thongBao, Integer nguoiNhanId) {
        if (thongBao.getMaSanPham() == null) {
            throw new RuntimeException("Thông báo này không có mã sản phẩm");
        }

        SanPham sanPham = sanPhamRepository.findById(thongBao.getMaSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!SanPham.TRANG_THAI_CHO_XAC_NHAN.equals(sanPham.getTrangThai())) {
            throw new RuntimeException("Sản phẩm này không ở trạng thái chờ xác nhận");
        }

        // 1. Duyệt sản phẩm: cho bán và bật hiển thị.
        sanPham.setTrangThai(SanPham.TRANG_THAI_DANG_BAN);
        sanPhamRepository.save(sanPham);

        // 2. Cập nhật thông báo bên nhân viên.
        thongBao.setTrangThai(TRANG_THAI_DA_CHAP_NHAN);
        thongBao.setNguoiNhanId(nguoiNhanId);
        thongBao.setLyDoTuChoi(null);
        thongBaoRepository.save(thongBao);

        // 3. Gửi thông báo về đối tác.
        // Trường hợp duyệt thì sản phẩm vẫn còn trong DB, nên được phép lưu FK sản phẩm.
        guiThongBaoDoiTacVeKetQuaDuyetSanPham(
                sanPham,
                true,
                null
        );
    }

    private void chapNhanCongViec(ThongBao thongBao, Integer nguoiNhanId) {
        thongBao.setTrangThai(TRANG_THAI_DA_CHAP_NHAN);
        thongBao.setNguoiNhanId(nguoiNhanId);
        thongBaoRepository.save(thongBao);

        if (thongBao.getMaKhachHang() != null) {
            KhachHang kh = khachHangRepository.findById(thongBao.getMaKhachHang())
                    .orElse(null);

            if (kh != null) {
                kh.setMaNhanVienPhuTrach(nguoiNhanId);
                khachHangRepository.save(kh);
            }
        }
    }

    /**
     * Từ chối thông báo.
     *
     * Nếu là CONG_VIEC:
     * - Lưu lý do.
     * - Gửi phản hồi về người gửi.
     *
     * Nếu là DUYET_SAN_PHAM:
     * - Gửi thông báo về đối tác: sản phẩm bị từ chối.
     * - Bỏ MaSanPham trong thông báo nhân viên.
     * - Xóa sản phẩm khỏi database.
     */
    @Transactional
    public void tuChoi(Integer maThongBao, Integer nguoiNhanId, String lyDoTuChoi) {
        if (lyDoTuChoi == null || lyDoTuChoi.trim().length() < 3) {
            throw new RuntimeException("Lý do từ chối phải từ 3 ký tự trở lên");
        }

        ThongBao thongBao = thongBaoRepository.findById(maThongBao)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (TRANG_THAI_DA_CHAP_NHAN.equals(thongBao.getTrangThai())
                || TRANG_THAI_DA_TU_CHOI.equals(thongBao.getTrangThai())) {
            throw new RuntimeException("Thông báo này đã được xử lý");
        }

        if (LOAI_DUYET_SAN_PHAM.equals(thongBao.getLoaiThongBao())) {
            tuChoiDuyetSanPham(thongBao, nguoiNhanId, lyDoTuChoi.trim());
            return;
        }

        if (LOAI_CONG_VIEC.equals(thongBao.getLoaiThongBao())) {
            tuChoiCongViec(thongBao, nguoiNhanId, lyDoTuChoi.trim());
            return;
        }

        throw new RuntimeException("Loại thông báo này không hỗ trợ từ chối");
    }

    private void tuChoiDuyetSanPham(ThongBao thongBao, Integer nguoiNhanId, String lyDoTuChoi) {
        if (thongBao.getMaSanPham() == null) {
            throw new RuntimeException("Thông báo này không có mã sản phẩm");
        }

        SanPham sanPham = sanPhamRepository.findById(thongBao.getMaSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!SanPham.TRANG_THAI_CHO_XAC_NHAN.equals(sanPham.getTrangThai())) {
            throw new RuntimeException("Sản phẩm này không ở trạng thái chờ xác nhận");
        }

        // 1. Gửi thông báo về đối tác trước khi xóa sản phẩm.
        // QUAN TRỌNG:
        // Trường hợp từ chối thì trong hàm dưới sẽ KHÔNG lưu FK sanPham,
        // vì lát nữa sản phẩm sẽ bị xóa khỏi database.
        guiThongBaoDoiTacVeKetQuaDuyetSanPham(
                sanPham,
                false,
                lyDoTuChoi
        );

        // 2. Cập nhật thông báo bên nhân viên.
        thongBao.setTrangThai(TRANG_THAI_DA_TU_CHOI);
        thongBao.setNguoiNhanId(nguoiNhanId);
        thongBao.setLyDoTuChoi(lyDoTuChoi);

        // 3. Bỏ liên kết MaSanPham trong bảng thongbao.
        // Nếu không bỏ, xóa sản phẩm có thể lỗi khóa ngoại thongbao -> sanpham.
        thongBao.setMaSanPham(null);
        thongBaoRepository.save(thongBao);

        // 4. Xóa sản phẩm khỏi database.
        sanPhamRepository.delete(sanPham);
    }

    private void tuChoiCongViec(ThongBao thongBao, Integer nguoiNhanId, String lyDoTuChoi) {
        thongBao.setTrangThai(TRANG_THAI_DA_TU_CHOI);
        thongBao.setNguoiNhanId(nguoiNhanId);
        thongBao.setLyDoTuChoi(lyDoTuChoi);
        thongBaoRepository.save(thongBao);

        if (thongBao.getNguoiGuiId() != null) {
            String tenNguoiTuChoi = "Nhân viên";
            NhanVien nv = nhanVienRepository.findById(nguoiNhanId).orElse(null);

            if (nv != null) {
                tenNguoiTuChoi = nv.getHoTen();
            }

            String tenKhachHang = "";
            if (thongBao.getMaKhachHang() != null) {
                KhachHang kh = khachHangRepository.findById(thongBao.getMaKhachHang()).orElse(null);
                if (kh != null) {
                    tenKhachHang = kh.getTenKhachHang();
                }
            }

            ThongBao phanHoi = ThongBao.builder()
                    .tieuDe("Từ chối tiếp nhận khách hàng")
                    .noiDung(tenNguoiTuChoi + " đã từ chối tiếp nhận khách hàng "
                            + tenKhachHang + ". Lý do: " + lyDoTuChoi)
                    .loaiThongBao("TU_CHOI")
                    .nguoiGuiId(nguoiNhanId)
                    .nguoiNhanId(thongBao.getNguoiGuiId())
                    .maKhachHang(thongBao.getMaKhachHang())
                    .maSanPham(null)
                    .trangThai(TRANG_THAI_CHUA_DOC)
                    .lyDoTuChoi(lyDoTuChoi)
                    .build();

            thongBaoRepository.save(phanHoi);
        }
    }

    /**
     * Đánh dấu đã đọc.
     */
    @Transactional
    public void danhDauDaDoc(Integer maThongBao) {
        ThongBao thongBao = thongBaoRepository.findById(maThongBao)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        // Không đổi CHO_XAC_NHAN thành DA_DOC,
        // vì CHO_XAC_NHAN là trạng thái chờ duyệt sản phẩm.
        if (TRANG_THAI_CHUA_DOC.equals(thongBao.getTrangThai())) {
            thongBao.setTrangThai(TRANG_THAI_DA_DOC);
            thongBaoRepository.save(thongBao);
        }
    }

    /**
     * Đánh dấu tất cả đã đọc.
     */
    @Transactional
    public void danhDauTatCaDaDoc(Integer nguoiNhanId) {
        thongBaoRepository.markAllAsRead(nguoiNhanId);
    }

    /**
     * Map Entity -> DTO.
     */
    private ThongBaoResponse toResponse(ThongBao tb) {
        ThongBaoResponse.ThongBaoResponseBuilder builder = ThongBaoResponse.builder()
                .maThongBao(tb.getMaThongBao())
                .tieuDe(tb.getTieuDe())
                .noiDung(tb.getNoiDung())
                .loaiThongBao(tb.getLoaiThongBao())
                .trangThai(tb.getTrangThai())
                .lyDoTuChoi(tb.getLyDoTuChoi())
                .nguoiGuiId(tb.getNguoiGuiId())
                .maKhachHang(tb.getMaKhachHang())
                .maSanPham(tb.getMaSanPham());

        if (tb.getNgayTao() != null) {
            builder.ngayTao(tb.getNgayTao().format(FORMATTER));
        }

        if (tb.getNguoiGuiId() != null) {
            NhanVien nguoiGui = nhanVienRepository
                    .findById(tb.getNguoiGuiId()).orElse(null);

            if (nguoiGui != null) {
                builder.tenNguoiGui(nguoiGui.getHoTen());
            }
        }

        if (tb.getMaKhachHang() != null) {
            KhachHang kh = khachHangRepository
                    .findById(tb.getMaKhachHang()).orElse(null);

            if (kh != null) {
                builder.tenKhachHang(kh.getTenKhachHang())
                        .soDienThoai(kh.getSoDienThoai())
                        .email(kh.getEmail())
                        .diaChi(kh.getDiaChi())
                        .cccd(kh.getCccd())
                        .nguonDangKy(kh.getNguonDangKy())
                        .nhuCauHoTro(kh.getNhuCauHoTro())
                        .ghiChu(kh.getGhiChu());

                if (kh.getNgayDangKy() != null) {
                    builder.ngayDangKy(kh.getNgayDangKy().format(FORMATTER));
                }
            }
        }

        return builder.build();
    }

    /**
     * Gửi thông báo kết quả duyệt sản phẩm về đối tác.
     */
    private void guiThongBaoDoiTacVeKetQuaDuyetSanPham(
            SanPham sanPham,
            boolean duocDuyet,
            String lyDoTuChoi
    ) {
        if (sanPham.getMaDoiTac() == null) {
            return;
        }

        DoiTac doiTac = doiTacRepository.findById(sanPham.getMaDoiTac())
                .orElse(null);

        if (doiTac == null) {
            return;
        }

        String tenSanPham = sanPham.getTenSanPham();

        String tieuDe = duocDuyet
                ? "Sản phẩm đã được duyệt"
                : "Sản phẩm đã bị từ chối";

        String noiDung = duocDuyet
                ? "Sản phẩm \"" + tenSanPham
                + "\" đã được nhân viên duyệt và đang được bày bán trên hệ thống."
                : "Sản phẩm \"" + tenSanPham
                + "\" đã bị nhân viên từ chối và đã bị xóa khỏi hệ thống."
                + " Lý do: " + lyDoTuChoi;

        ThongBaoDoiTac thongBaoDoiTac = ThongBaoDoiTac.builder()
                .doiTac(doiTac)

                // Thông báo duyệt sản phẩm không liên quan đơn hàng.
                .donHang(null)

                // QUAN TRỌNG:
                // Nếu được duyệt thì sản phẩm vẫn còn trong DB, có thể lưu FK.
                // Nếu bị từ chối thì sản phẩm sẽ bị xóa, tuyệt đối không lưu FK sản phẩm.
                .sanPham(duocDuyet ? sanPham : null)

                .loai(LOAI_DUYET_SAN_PHAM)
                .tieuDe(tieuDe)
                .noiDung(noiDung)
                .trangThaiThongBao(duocDuyet ? TRANG_THAI_DA_CHAP_NHAN : TRANG_THAI_DA_TU_CHOI)
                .lyDoTuChoi(duocDuyet ? null : lyDoTuChoi)
                .daDoc(false)
                .thoiGianTao(LocalDateTime.now())
                .thoiGianXuLy(LocalDateTime.now())
                .build();

        thongBaoDoiTacRepository.save(thongBaoDoiTac);
    }
    @Transactional
    public void taoThongBaoChapNhanDonHang(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng"
                ));

        if (donHang.getNhanVien() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng chưa có nhân viên phụ trách"
            );
        }

        Integer nguoiNhanId = donHang.getNhanVien().getMaNhanVien();

        String tenKhachHang = "Không có";
        if (donHang.getKhachHang() != null) {
            tenKhachHang = donHang.getKhachHang().getTenKhachHang();
        }

        String maDonHangText = "DH" + String.format("%03d", donHang.getMaDonHang());

        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe("Đối tác đã xác nhận đơn hàng");
        thongBao.setNoiDung(
                "Đơn hàng " + maDonHangText +
                        " của khách hàng " + tenKhachHang +
                        " đã được đối tác xác nhận."
        );
        thongBao.setLoaiThongBao("DON_HANG");
        thongBao.setNguoiGuiId(null);
        thongBao.setNguoiNhanId(nguoiNhanId);

        if (donHang.getKhachHang() != null) {
            thongBao.setMaKhachHang(donHang.getKhachHang().getMaKhachHang());
        }

        thongBao.setTrangThai(TRANG_THAI_CHUA_DOC);
        thongBao.setNgayTao(LocalDateTime.now());
        thongBao.setNgayCapNhat(LocalDateTime.now());

        thongBaoRepository.save(thongBao);
    }

    @Transactional
    public void taoThongBaoChapNhanDonHangChoAdmin(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng"
                ));

        List<NhanVien> admins = nhanVienRepository.findByVaiTro(1);

        if (admins == null || admins.isEmpty()) {
            return;
        }

        String tenKhachHang = donHang.getKhachHang() != null
                ? donHang.getKhachHang().getTenKhachHang()
                : "Không có";

        String tenNhanVien = donHang.getNhanVien() != null
                ? donHang.getNhanVien().getHoTen()
                : "Chưa có nhân viên phụ trách";

        String maDonHangText = "DH" + String.format("%03d", donHang.getMaDonHang());

        for (NhanVien admin : admins) {
            ThongBao thongBao = new ThongBao();

            thongBao.setTieuDe("Thông báo đơn hàng mới Đơn hàng"+ maDonHangText);

            thongBao.setNoiDung(
                    "Đơn hàng " + maDonHangText
                            + " của khách hàng " + tenKhachHang
                            + " đã được đối tác xác nhận. "
                            + "Nhân viên phụ trách: " + tenNhanVien + "."
            );

            thongBao.setLoaiThongBao("DON_HANG");
            thongBao.setNguoiGuiId(null);
            thongBao.setNguoiNhanId(admin.getMaNhanVien());

            if (donHang.getKhachHang() != null) {
                thongBao.setMaKhachHang(donHang.getKhachHang().getMaKhachHang());
            }

            thongBao.setMaSanPham(null);
            thongBao.setTrangThai(TRANG_THAI_CHUA_DOC);
            thongBao.setLyDoTuChoi(null);
            thongBao.setNgayTao(LocalDateTime.now());
            thongBao.setNgayCapNhat(LocalDateTime.now());

            thongBaoRepository.save(thongBao);
        }
    }
    @Transactional
    public void taoThongBaoDonHangDangXuLy(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng"
                ));

        List<NhanVien> admins = nhanVienRepository.findByVaiTro(1);

        if (admins == null || admins.isEmpty()) {
            return;
        }

        String tenKhachHang = donHang.getKhachHang() != null
                ? donHang.getKhachHang().getTenKhachHang()
                : "Không có";

        String tenNhanVien = donHang.getNhanVien() != null
                ? donHang.getNhanVien().getHoTen()
                : "Chưa có nhân viên phụ trách";

        String maDonHangText = "DH" + String.format("%03d", donHang.getMaDonHang());

        for (NhanVien admin : admins) {
            ThongBao thongBao = new ThongBao();

            thongBao.setTieuDe("Thông báo Đơn hàng"+ maDonHangText + " đang xử lý");

            thongBao.setNoiDung(
                    "Đơn hàng " + maDonHangText
                            + " của khách hàng " + tenKhachHang
                            + " đang xử lý. "
                            + "Nhân viên phụ trách: " + tenNhanVien + "."
            );

            thongBao.setLoaiThongBao("DON_HANG");
            thongBao.setNguoiGuiId(null);
            thongBao.setNguoiNhanId(admin.getMaNhanVien());

            if (donHang.getKhachHang() != null) {
                thongBao.setMaKhachHang(donHang.getKhachHang().getMaKhachHang());
            }

            thongBao.setMaSanPham(null);
            thongBao.setTrangThai(TRANG_THAI_CHUA_DOC);
            thongBao.setLyDoTuChoi(null);
            thongBao.setNgayTao(LocalDateTime.now());
            thongBao.setNgayCapNhat(LocalDateTime.now());

            thongBaoRepository.save(thongBao);
        }
    }
    @Transactional
    public void taoThongBaoDonHangThanhToan(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng"
                ));

        List<NhanVien> admins = nhanVienRepository.findByVaiTro(1);

        if (admins == null || admins.isEmpty()) {
            return;
        }

        String tenKhachHang = donHang.getKhachHang() != null
                ? donHang.getKhachHang().getTenKhachHang()
                : "Không có";

        String tenNhanVien = donHang.getNhanVien() != null
                ? donHang.getNhanVien().getHoTen()
                : "Chưa có nhân viên phụ trách";

        String maDonHangText = "DH" + String.format("%03d", donHang.getMaDonHang());

        for (NhanVien admin : admins) {
            ThongBao thongBao = new ThongBao();

            thongBao.setTieuDe("Thông báo Đơn hàng"+ maDonHangText + " đã thanh toán");

            thongBao.setNoiDung(
                    "Đơn hàng " + maDonHangText
                            + " của khách hàng " + tenKhachHang
                            + " đã thanh toán. "
                            + "Nhân viên phụ trách: " + tenNhanVien + "."
            );

            thongBao.setLoaiThongBao("DON_HANG");
            thongBao.setNguoiGuiId(null);
            thongBao.setNguoiNhanId(admin.getMaNhanVien());

            if (donHang.getKhachHang() != null) {
                thongBao.setMaKhachHang(donHang.getKhachHang().getMaKhachHang());
            }

            thongBao.setMaSanPham(null);
            thongBao.setTrangThai(TRANG_THAI_CHUA_DOC);
            thongBao.setLyDoTuChoi(null);
            thongBao.setNgayTao(LocalDateTime.now());
            thongBao.setNgayCapNhat(LocalDateTime.now());

            thongBaoRepository.save(thongBao);
        }
    }
}