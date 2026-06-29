package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.HuyDonHangRequest;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HoaDon;
import vn.anyen.entity.HopDong;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HoaDonRepository;
import vn.anyen.repository.HopDongRepository;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.SanPhamRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HopDongRepository hopDongRepository;

    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DoiTacThongBaoService doiTacThongBaoService;
    private final ThongBaoService thongBaoService;

    private static final List<String> TRANG_THAI_ORDER = Arrays.asList(
            "Mới tạo",
            "Chờ đối tác xác nhận",
            "Đã xác nhận",
            "Đang xử lý",
            "Chờ thanh toán",
            "Hoàn thành"
    );

    @Transactional
    public DonHangResponse taoDonHang(
            TaoDonHangRequest request,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        validateItems(request);

        NhanVien nhanVien = nhanVienRepository
                .findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Không tìm thấy nhân viên đăng nhập"
                ));

        KhachHang khachHang = layHoacTaoKhachHang(request, nhanVien);

        DonHang donHang = DonHang.builder()
                .khachHang(khachHang)
                .nhanVien(nhanVien)
                .ngayTaoDon(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .tongTien(BigDecimal.ZERO)
                .trangThai("Chờ đối tác xác nhận")
                .ghiChu(request.getGhiChu())
                .phuongThucThanhToan(
                        request.getPhuongThucThanhToan() != null
                                ? request.getPhuongThucThanhToan()
                                : "Chưa chọn"
                )
                .trangThaiThanhToan(
                        request.getTrangThaiThanhToan() != null
                                ? request.getTrangThaiThanhToan()
                                : "Chưa thanh toán"
                )
                .build();

        DonHang savedDonHang = donHangRepository.save(donHang);

        BigDecimal tongTien = taoChiTietDonHangVaTruTonKho(savedDonHang, request.getItems());

        savedDonHang.setTongTien(tongTien);
        DonHang donHangDaLuu = donHangRepository.save(savedDonHang);

        doiTacThongBaoService.taoThongBaoChoDonHang(
                donHangDaLuu.getMaDonHang()
        );

        return mapToDonHangResponse(donHangDaLuu);
    }

    @Transactional(readOnly = true)
    public boolean kiemTraDonHangDaCoHopDong(Integer maDonHang) {
        if (!donHangRepository.existsById(maDonHang)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy đơn hàng #" + maDonHang
            );
        }

        return hopDongRepository.existsByDonHang_MaDonHang(maDonHang);
    }

    @Transactional
    public DonHangResponse capNhatDonHang(Integer maDonHang, TaoDonHangRequest request) {
        if (hopDongRepository.existsByDonHang_MaDonHang(maDonHang)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đơn hàng đã ký hợp đồng, không thể chỉnh sửa. Nếu có sai sót thông tin vui lòng hủy đơn hàng và hủy hợp đồng."
            );
        }

        validateItems(request);

        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        if ("Đã hủy".equalsIgnoreCase(donHang.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng đã hủy, không thể chỉnh sửa."
            );
        }

        if ("Hoàn thành".equalsIgnoreCase(donHang.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng đã hoàn thành, không thể chỉnh sửa."
            );
        }

        KhachHang khachHang = layHoacCapNhatKhachHangKhiSuaDon(request, donHang);
        donHang.setKhachHang(khachHang);

        donHang.setGhiChu(request.getGhiChu());
        donHang.setPhuongThucThanhToan(
                request.getPhuongThucThanhToan() != null
                        ? request.getPhuongThucThanhToan()
                        : "Chưa chọn"
        );
        donHang.setTrangThaiThanhToan(
                request.getTrangThaiThanhToan() != null
                        ? request.getTrangThaiThanhToan()
                        : "Chưa thanh toán"
        );

        hoanTonKhoVaXoaChiTietCu(donHang);

        BigDecimal tongTienMoi = taoChiTietDonHangVaTruTonKho(donHang, request.getItems());
        donHang.setTongTien(tongTienMoi);

        DonHang saved = donHangRepository.save(donHang);

        return mapToDonHangResponse(saved);
    }

    private void validateItems(TaoDonHangRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn ít nhất 1 sản phẩm"
            );
        }
    }

    private KhachHang layHoacTaoKhachHang(
            TaoDonHangRequest request,
            NhanVien nhanVien
    ) {
        if (request.getMaKhachHang() != null) {
            return khachHangRepository.findById(request.getMaKhachHang())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy khách hàng #" + request.getMaKhachHang()
                    ));
        }

        if (request.getTenKhachHang() == null
                || request.getTenKhachHang().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên khách hàng không được để trống"
            );
        }

        if (request.getSoDienThoai() == null
                || request.getSoDienThoai().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số điện thoại không được để trống"
            );
        }

        KhachHang khachHang = KhachHang.builder()
                .tenKhachHang(request.getTenKhachHang().trim())
                .soDienThoai(request.getSoDienThoai().trim())
                .cccd(request.getCccd())
                .email(request.getEmail())
                .diaChi(request.getDiaChi())
                .maNhanVienPhuTrach(nhanVien.getMaNhanVien())
                .ngayDangKy(LocalDateTime.now())
                .nguonDangKy("Tạo từ đơn hàng")
                .build();

        return khachHangRepository.save(khachHang);
    }

    private KhachHang layHoacCapNhatKhachHangKhiSuaDon(
            TaoDonHangRequest request,
            DonHang donHang
    ) {
        if (request.getMaKhachHang() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getMaKhachHang())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy khách hàng #" + request.getMaKhachHang()
                    ));

            capNhatThongTinKhachHang(khachHang, request);
            return khachHangRepository.save(khachHang);
        }

        KhachHang khachHang = donHang.getKhachHang();

        if (khachHang == null) {
            NhanVien nhanVien = donHang.getNhanVien();

            if (nhanVien == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Đơn hàng chưa có nhân viên phụ trách"
                );
            }

            return layHoacTaoKhachHang(request, nhanVien);
        }

        capNhatThongTinKhachHang(khachHang, request);
        return khachHangRepository.save(khachHang);
    }

    private void capNhatThongTinKhachHang(
            KhachHang khachHang,
            TaoDonHangRequest request
    ) {
        if (request.getTenKhachHang() != null
                && !request.getTenKhachHang().trim().isEmpty()) {
            khachHang.setTenKhachHang(request.getTenKhachHang().trim());
        }

        if (request.getSoDienThoai() != null
                && !request.getSoDienThoai().trim().isEmpty()) {
            khachHang.setSoDienThoai(request.getSoDienThoai().trim());
        }

        khachHang.setCccd(request.getCccd());
        khachHang.setEmail(request.getEmail());
        khachHang.setDiaChi(request.getDiaChi());
    }

    private void hoanTonKhoVaXoaChiTietCu(DonHang donHang) {
        List<ChiTietDonHang> chiTietsCu = chiTietDonHangRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang());

        for (ChiTietDonHang ct : chiTietsCu) {
            SanPham sanPham = ct.getSanPham();

            if (sanPham != null) {
                Integer tonKhoHienTai = sanPham.getSoLuong() == null
                        ? 0
                        : sanPham.getSoLuong();

                Integer soLuongTrongDon = ct.getSoLuong() == null
                        ? 0
                        : ct.getSoLuong();

                sanPham.setSoLuong(tonKhoHienTai + soLuongTrongDon);
                sanPhamRepository.save(sanPham);
            }
        }

        chiTietDonHangRepository.deleteAll(chiTietsCu);
        chiTietDonHangRepository.flush();
    }

    private BigDecimal taoChiTietDonHangVaTruTonKho(
            DonHang donHang,
            List<TaoDonHangRequest.SanPhamTrongDonRequest> items
    ) {
        BigDecimal tongTien = BigDecimal.ZERO;

        for (TaoDonHangRequest.SanPhamTrongDonRequest item : items) {
            SanPham sanPham = sanPhamRepository
                    .findById(item.getMaSanPham())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy sản phẩm #" + item.getMaSanPham()
                    ));

            Integer soLuongDat = item.getSoLuong();

            if (soLuongDat == null || soLuongDat <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Số lượng sản phẩm phải lớn hơn 0"
                );
            }

            Integer tonKho = sanPham.getSoLuong() == null
                    ? 0
                    : sanPham.getSoLuong();

            if (tonKho < soLuongDat) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sản phẩm '" + sanPham.getTenSanPham()
                                + "' không đủ tồn kho. Còn: " + tonKho
                );
            }

            BigDecimal donGia = sanPham.getGiaTien() == null
                    ? BigDecimal.ZERO
                    : sanPham.getGiaTien();

            tongTien = tongTien.add(
                    donGia.multiply(BigDecimal.valueOf(soLuongDat))
            );

            ChiTietDonHang chiTiet = ChiTietDonHang.builder()
                    .donHang(donHang)
                    .sanPham(sanPham)
                    .soLuong(soLuongDat)
                    .giaTien(donGia)
                    .build();

            chiTietDonHangRepository.save(chiTiet);

            sanPham.setSoLuong(tonKho - soLuongDat);
            sanPhamRepository.save(sanPham);
        }

        return tongTien;
    }

    public List<DonHangResponse> getAllDonHang() {
        List<DonHang> donHangs = donHangRepository.findAll();
        return donHangs.stream()
                .map(this::mapToDonHangResponse)
                .collect(Collectors.toList());
    }

    public DonHangResponse getDonHangById(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        return mapToDonHangResponse(donHang);
    }

    @Transactional
    public DonHangResponse capNhatTrangThai(Integer maDonHang, String trangThaiMoi) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        String trangThaiHienTai = donHang.getTrangThai();

        if ("Đã hủy".equals(trangThaiHienTai)) {
            throw new RuntimeException(
                    "Đơn hàng đã bị hủy, không thể cập nhật trạng thái."
            );
        }

        if (!"Đã hủy".equals(trangThaiMoi)
                && !TRANG_THAI_ORDER.contains(trangThaiMoi)) {
            throw new RuntimeException(
                    "Trạng thái '" + trangThaiMoi + "' không hợp lệ."
            );
        }

        if (!"Đã hủy".equals(trangThaiMoi)) {
            int currentIdx = TRANG_THAI_ORDER.indexOf(trangThaiHienTai);
            int nextIdx = TRANG_THAI_ORDER.indexOf(trangThaiMoi);

            if (nextIdx != currentIdx + 1) {
                throw new RuntimeException(
                        "Chỉ có thể chuyển sang trạng thái tiếp theo. "
                                + "Hiện tại: '" + trangThaiHienTai
                                + "', yêu cầu: '" + trangThaiMoi + "'."
                );
            }
        }

        if ("Đang xử lý".equals(trangThaiMoi)) {
            thongBaoService.taoThongBaoDonHangDangXuLy(donHang.getMaDonHang());
        } else if ("Hoàn thành".equals(trangThaiMoi)) {
            thongBaoService.taoThongBaoDonHangThanhToan(donHang.getMaDonHang());
        }

        donHang.setTrangThai(trangThaiMoi);
        donHangRepository.save(donHang);

        return mapToDonHangResponse(donHang);
    }

    @Transactional
    public DonHangResponse huyDonHang(Integer maDonHang) {
        return capNhatTrangThai(maDonHang, "Đã hủy");
    }

    private DonHangResponse mapToDonHangResponse(DonHang donHang) {
        List<ChiTietDonHang> chiTiets = chiTietDonHangRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang());

        HoaDon hoaDon = hoaDonRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang())
                .orElse(null);

        HopDong hopDong = hopDongRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang())
                .orElse(null);

        List<DonHangResponse.ChiTietDonHangResponse> sanPhams = chiTiets
                .stream()
                .map(ct -> DonHangResponse.ChiTietDonHangResponse.builder()
                        .MaSanPham(ct.getSanPham().getMaSanPham())
                        .tenSanPham(ct.getSanPham().getTenSanPham())
                        .maSKU("SP" + String.format("%03d", ct.getSanPham().getMaSanPham()))
                        .phanLoai(ct.getSanPham().getLoai())
                        .HinhAnh(ct.getSanPham().getHinhAnh())
                        .giaTien(ct.getGiaTien())
                        .SoLuong(ct.getSoLuong())
                        .thanhTien(
                                ct.getGiaTien().multiply(
                                        BigDecimal.valueOf(ct.getSoLuong())
                                )
                        )
                        .build())
                .collect(Collectors.toList());

        String currentTrangThai = donHang.getTrangThai();
        int currentIdx = TRANG_THAI_ORDER.indexOf(currentTrangThai);
        boolean isDaHuy = "Đã hủy".equals(currentTrangThai);

        List<DonHangResponse.LichSuDonHangResponse> lichSu = new ArrayList<>();

        for (int i = 0; i < TRANG_THAI_ORDER.size(); i++) {
            String step = TRANG_THAI_ORDER.get(i);
            boolean done = !isDaHuy && i <= currentIdx;
            String time = "";
            String moTa = "";

            if (i == 0 && donHang.getNgayTaoDon() != null) {
                time = donHang.getNgayTaoDon().toString();
                moTa = "Đơn hàng đã được tạo.";
            } else if (done) {
                moTa = "Đã hoàn thành.";
            } else {
                moTa = "Chưa cập nhật";
            }

            String color;
            switch (step) {
                case "Mới tạo":
                    color = "yellow";
                    break;
                case "Chờ đối tác xác nhận":
                    color = "pink";
                    break;
                case "Đã xác nhận":
                    color = "blue";
                    break;
                case "Đang xử lý":
                    color = "orange";
                    break;
                case "Chờ thanh toán":
                    color = "purple";
                    break;
                case "Hoàn thành":
                    color = "green";
                    break;
                default:
                    color = "gray";
                    break;
            }

            lichSu.add(DonHangResponse.LichSuDonHangResponse.builder()
                    .trangThai(step)
                    .thoiGian(time)
                    .moTa(moTa)
                    .color(color)
                    .done(done)
                    .build());
        }

        return DonHangResponse.builder()
                .MaDonHang(donHang.getMaDonHang())
                .maCode(String.format("DH%04d", donHang.getMaDonHang()))

                .MaKhachHang(
                        donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getMaKhachHang()
                                : null
                )
                .tenKhachHang(
                        donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getTenKhachHang()
                                : null
                )
                .emailKH(
                        donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getEmail()
                                : null
                )
                .soDienThoaiKH(
                        donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getSoDienThoai()
                                : null
                )
                .diaChiKH(
                        donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getDiaChi()
                                : null
                )

                .loaiKH("Thường")
                .tongDonKH(1)
                .tongChiTieuKH(
                        donHang.getTongTien() != null
                                ? donHang.getTongTien()
                                : BigDecimal.ZERO
                )
                .ghiChuKH("")
                .ghiChuNoiBo(donHang.getGhiChu())

                .MaNhanVien(
                        donHang.getNhanVien() != null
                                ? donHang.getNhanVien().getMaNhanVien()
                                : null
                )
                .tenNhanVien(
                        donHang.getNhanVien() != null
                                ? donHang.getNhanVien().getHoTen()
                                : null
                )

                .NgayTaoDon(donHang.getNgayTaoDon())
                .tongTien(donHang.getTongTien())
                .trangThai(donHang.getTrangThai())
                .GhiChu(donHang.getGhiChu())
                .phuongThucThanhToan(donHang.getPhuongThucThanhToan())
                .trangThaiThanhToan(donHang.getTrangThaiThanhToan())

                .maHoaDon(hoaDon != null ? hoaDon.getMaHoaDon() : null)
                .daCoHoaDon(hoaDon != null)
                .trangThaiHoaDon(hoaDon != null ? hoaDon.getTrangThai() : null)

                .daCoHopDong(hopDong != null)
                .maHopDong(hopDong != null ? hopDong.getMaHopDong() : null)
                .trangThaiHopDong(hopDong != null ? hopDong.getTrangThai() : null)

                .phuongThucGiaoHang("Giao hàng tận nơi")
                .phiVanChuyen(BigDecimal.ZERO)
                .giamGia(BigDecimal.ZERO)

                .sanPhams(sanPhams)
                .lichSu(lichSu)
                .build();
    }

    @Transactional
    public DonHangResponse capNhatTrangThaiNhanVien(
            Integer maDonHang,
            String trangThaiMoi
    ) {
        if (trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) {
            throw new RuntimeException("Trạng thái đơn hàng không được để trống.");
        }

        trangThaiMoi = trangThaiMoi.trim();

        List<String> trangThaiHopLe = Arrays.asList(
                "Mới tạo",
                "Chờ đối tác xác nhận",
                "Đã xác nhận",
                "Đang xử lý",
                "Chờ thanh toán",
                "Hoàn thành",
                "Đã hủy"
        );

        if (!trangThaiHopLe.contains(trangThaiMoi)) {
            throw new RuntimeException(
                    "Trạng thái '" + trangThaiMoi + "' không hợp lệ."
            );
        }

        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        donHang.setTrangThai(trangThaiMoi);

        DonHang saved = donHangRepository.save(donHang);

        return mapToDonHangResponse(saved);
    }

    @Transactional
    public DonHangResponse huyDonHang(
            Integer maDonHang,
            HuyDonHangRequest request
    ) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String lyDo = request.getLyDoHuy() == null
                ? ""
                : request.getLyDoHuy().trim();

        if (lyDo.length() <= 3) {
            throw new RuntimeException("Lý do hủy phải trên 3 ký tự");
        }

        if ("Đã hủy".equalsIgnoreCase(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng này đã bị hủy trước đó");
        }

        if ("Hoàn thành".equalsIgnoreCase(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }

        donHang.setTrangThai("Đã hủy");
        donHang.setLyDoHuy(lyDo);

        DonHang saved = donHangRepository.save(donHang);

        return mapToDonHangResponse(saved);
    }

    private DoiTacDonHangResponse mapToDoiTacDonHangResponse(
            DonHang donHang,
            Integer maDoiTac
    ) {
        List<ChiTietDonHang> chiTiets = chiTietDonHangRepository
                .findByDonHangAndDoiTac(
                        donHang.getMaDonHang(),
                        maDoiTac
                );

        BigDecimal tongCong = chiTiets.stream()
                .map(ct -> {
                    BigDecimal giaTien = ct.getGiaTien() == null
                            ? BigDecimal.ZERO
                            : ct.getGiaTien();

                    Integer soLuong = ct.getSoLuong() == null
                            ? 0
                            : ct.getSoLuong();

                    return giaTien.multiply(BigDecimal.valueOf(soLuong));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DoiTacDonHangResponse.SanPhamTrongDonResponse> sanPhams = new ArrayList<>();

        for (int i = 0; i < chiTiets.size(); i++) {
            ChiTietDonHang ct = chiTiets.get(i);
            SanPham sp = ct.getSanPham();

            BigDecimal giaTien = ct.getGiaTien() == null
                    ? BigDecimal.ZERO
                    : ct.getGiaTien();

            Integer soLuong = ct.getSoLuong() == null
                    ? 0
                    : ct.getSoLuong();

            sanPhams.add(
                    DoiTacDonHangResponse.SanPhamTrongDonResponse.builder()
                            .stt(i + 1)
                            .ten(sp != null ? sp.getTenSanPham() : "")
                            .soLuong(soLuong)
                            .donGia(giaTien)
                            .thanhTien(
                                    giaTien.multiply(BigDecimal.valueOf(soLuong))
                            )
                            .build()
            );
        }

        KhachHang khachHang = donHang.getKhachHang();
        NhanVien nhanVien = donHang.getNhanVien();

        return DoiTacDonHangResponse.builder()
                .maDonHang(donHang.getMaDonHang())
                .maCode(String.format("DH%04d", donHang.getMaDonHang()))

                .tenKhachHang(
                        khachHang != null
                                ? khachHang.getTenKhachHang()
                                : ""
                )
                .cccd(
                        khachHang != null
                                ? khachHang.getCccd()
                                : ""
                )
                .soDienThoai(
                        khachHang != null
                                ? khachHang.getSoDienThoai()
                                : ""
                )
                .email(
                        khachHang != null
                                ? khachHang.getEmail()
                                : ""
                )
                .diaChi(
                        khachHang != null
                                ? khachHang.getDiaChi()
                                : ""
                )

                .ngayDat(
                        donHang.getNgayTaoDon() != null
                                ? donHang.getNgayTaoDon().toString()
                                : ""
                )
                .nhanVien(
                        nhanVien != null
                                ? nhanVien.getHoTen()
                                : ""
                )

                .ghiChu(donHang.getGhiChu())
                .trangThai(donHang.getTrangThai())
                .tongCong(tongCong)

                .sanPhams(sanPhams)
                .build();
    }

    @Transactional(readOnly = true)
    public DoiTacDonHangPageResponse getDoiTacDonHangs(
            Integer maDoiTac,
            String keyword,
            String trangThai,
            Integer page,
            Integer pageSize
    ) {
        int pageIndex = page == null || page < 1 ? 0 : page - 1;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<DonHang> donHangPage = donHangRepository.findDoiTacDonHangs(
                maDoiTac,
                keyword,
                trangThai,
                pageable
        );

        List<DoiTacDonHangResponse> items = donHangPage.getContent()
                .stream()
                .map(donHang -> mapToDoiTacDonHangResponse(donHang, maDoiTac))
                .collect(Collectors.toList());

        return DoiTacDonHangPageResponse.builder()
                .items(items)
                .total((int) donHangPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public DoiTacDonHangResponse getDoiTacDonHangDetail(
            Integer maDonHang,
            Integer maDoiTac
    ) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        DoiTacDonHangResponse response = mapToDoiTacDonHangResponse(
                donHang,
                maDoiTac
        );

        if (response.getSanPhams() == null
                || response.getSanPhams().isEmpty()) {
            throw new RuntimeException("Đơn hàng không thuộc đối tác này");
        }

        return response;
    }
}