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
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.HoaDon;
import vn.anyen.entity.HopDong;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.SanPham;
import vn.anyen.entity.ThongBaoDoiTac;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.HoaDonRepository;
import vn.anyen.repository.HopDongRepository;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.SanPhamRepository;
import vn.anyen.repository.ThongBaoDoiTacRepository;

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
    private final DoiTacRepository doiTacRepository;
    private final DoiTacThongBaoService doiTacThongBaoService;
    private final ThongBaoService thongBaoService;
    private final ThongBaoDoiTacRepository thongBaoDoiTacRepository;
    private final TaiChinhDoiTacService taiChinhDoiTacService;
    private final CongNoService congNoService;
    private static final List<Integer> TRANG_THAI_ORDER = Arrays.asList(
            DonHang.TT_MOI_TAO,
            DonHang.TT_CHO_DOI_TAC_XAC_NHAN,
            DonHang.TT_DA_XAC_NHAN,
            DonHang.TT_DANG_XU_LY,
            DonHang.TT_DA_GIAO,
            DonHang.TT_HOAN_THANH
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
                .trangThai(DonHang.TT_CHO_DOI_TAC_XAC_NHAN)
                .ghiChu(request.getGhiChu())
                .phuongThucThanhToan(
                        request.getPhuongThucThanhToan() != null
                                ? request.getPhuongThucThanhToan()
                                : DonHang.PT_CHUA_CHON
                )
                .trangThaiThanhToan(
                        request.getTrangThaiThanhToan() != null
                                ? request.getTrangThaiThanhToan()
                                : DonHang.TTTT_CHUA_THANH_TOAN
                )
                .build();

        DonHang savedDonHang = donHangRepository.save(donHang);

        BigDecimal tongTien = taoChiTietDonHangVaTruTonKho(
                savedDonHang,
                request.getItems(),
                null
        );

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
    public DonHangResponse capNhatDonHang(
            Integer maDonHang,
            TaoDonHangRequest request
    ) {
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

        // Kiểm tra xem có đang thêm sản phẩm của đối tác đã từ chối đơn hàng này không
        kiemTraKhongThemSanPhamCuaDoiTacTuChoi(maDonHang, request.getItems());

        if (DonHang.TT_DA_HUY.equals(donHang.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng đã hủy, không thể chỉnh sửa."
            );
        }

        if (DonHang.TT_HOAN_THANH.equals(donHang.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng đã hoàn thành, không thể chỉnh sửa."
            );
        }

        KhachHang khachHang = layHoacCapNhatKhachHangKhiSuaDon(
                request,
                donHang
        );

        donHang.setKhachHang(khachHang);
        donHang.setGhiChu(request.getGhiChu());

        donHang.setPhuongThucThanhToan(
                request.getPhuongThucThanhToan() != null
                        ? request.getPhuongThucThanhToan()
                        : DonHang.PT_CHUA_CHON
        );

        donHang.setTrangThaiThanhToan(
                request.getTrangThaiThanhToan() != null
                        ? request.getTrangThaiThanhToan()
                        : DonHang.TTTT_CHUA_THANH_TOAN
        );

        List<ChiTietDonHang> chiTietsCu = hoanTonKhoVaXoaChiTietCu(donHang);

        BigDecimal tongTienMoi = taoChiTietDonHangVaTruTonKho(
                donHang,
                request.getItems(),
                chiTietsCu
        );

        donHang.setTongTien(tongTienMoi);

        DonHang saved = donHangRepository.save(donHang);

        // Nếu đơn hàng ở trạng thái Mới tạo (sau khi đối tác từ chối), tự động gửi lại thông báo
        if (DonHang.TT_MOI_TAO.equals(saved.getTrangThai())) {
            doiTacThongBaoService.taoThongBaoChoDonHang(saved.getMaDonHang());
        }

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
                .soNhaDuong(request.getSoNhaDuong())
                .phuongXa(request.getPhuongXa())
                .quanHuyen(request.getQuanHuyen())
                .tinhThanh(request.getTinhThanh())
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
        if (request.getSoNhaDuong() != null) {
            khachHang.setSoNhaDuong(request.getSoNhaDuong().trim());
        }
        if (request.getPhuongXa() != null) {
            khachHang.setPhuongXa(request.getPhuongXa().trim());
        }
        if (request.getQuanHuyen() != null) {
            khachHang.setQuanHuyen(request.getQuanHuyen().trim());
        }
        if (request.getTinhThanh() != null) {
            khachHang.setTinhThanh(request.getTinhThanh().trim());
        }
    }

    private List<ChiTietDonHang> hoanTonKhoVaXoaChiTietCu(DonHang donHang) {
        List<ChiTietDonHang> chiTietsCu =
                chiTietDonHangRepository.findByDonHang_MaDonHang(
                        donHang.getMaDonHang()
                );

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
        
        return chiTietsCu;
    }

    private BigDecimal taoChiTietDonHangVaTruTonKho(
            DonHang donHang,
            List<TaoDonHangRequest.SanPhamTrongDonRequest> items,
            List<ChiTietDonHang> chiTietsCu
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
            
            Integer oldTrangThai = 0;
            if (chiTietsCu != null) {
                for (ChiTietDonHang ct : chiTietsCu) {
                    if (ct.getSanPham() != null && ct.getSanPham().getMaSanPham().equals(sanPham.getMaSanPham())) {
                        if (ct.getSoLuong() != null && ct.getSoLuong().equals(soLuongDat)) {
                            if (ct.getTrangThaiDoiTac() != null && ct.getTrangThaiDoiTac() == 1) {
                                oldTrangThai = 1;
                            }
                        }
                        break;
                    }
                }
            }

            ChiTietDonHang chiTiet = ChiTietDonHang.builder()
                    .donHang(donHang)
                    .sanPham(sanPham)
                    .soLuong(soLuongDat)
                    .giaTien(donGia)
                    .trangThaiDoiTac(oldTrangThai)
                    .build();

            chiTietDonHangRepository.save(chiTiet);

            sanPham.setSoLuong(tonKho - soLuongDat);
            sanPhamRepository.save(sanPham);
        }

        return tongTien;
    }

    private Integer chuanHoaTrangThaiDonHang(Integer trangThai) {
        // Trạng thái 0 là dữ liệu cũ/không hợp lệ. Quy đổi về trạng thái đầu tiên
        // để API không còn trả về 0 và đơn hàng vẫn có thể tiếp tục xử lý.
        if (trangThai == null || trangThai == 0) {
            return DonHang.TT_MOI_TAO;
        }

        return trangThai;
    }

    private String getTrangThaiString(Integer trangThai) {
        Integer tt = chuanHoaTrangThaiDonHang(trangThai);

        switch (tt) {
            case 1:
                return "Mới tạo";
            case 2:
                return "Xác nhận";
            case 3:
                return "Đã nhận";
            case 4:
                return "Xử lý";
            case 5:
                return "Thanh toán";
            case 6:
                return "Hoàn thành";
            case 7:
                return "Đã hủy";
            case 8:
                return "Từ chối";
            case 9:
                return "Đã giao";
            case 10:
                return "Đã thanh toán";
            case 11:
                return "Gặp sự cố";
            default:
                return "Không rõ";
        }
    }

    public List<DonHangResponse> getAllDonHang() {
        List<DonHang> donHangs = donHangRepository.findAll();

        return donHangs.stream()
                // Sắp xếp theo maDonHang descending (ID càng lớn = đơn càng mới)
                .sorted((a, b) -> b.getMaDonHang().compareTo(a.getMaDonHang()))
                .map(this::mapToDonHangResponse)
                .collect(Collectors.toList());
    }

    public List<DonHangResponse> getDonHangByNhanVien(Integer maNhanVien) {
        List<DonHang> donHangs = donHangRepository
                .findByNhanVien_MaNhanVienOrderByNgayTaoDonDesc(maNhanVien);

        return donHangs.stream()
                // Sắp xếp theo maDonHang descending (ID càng lớn = đơn càng mới)
                .sorted((a, b) -> b.getMaDonHang().compareTo(a.getMaDonHang()))
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
    public DonHangResponse capNhatTrangThai(
            Integer maDonHang,
            Integer trangThaiMoi
    ) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        if (trangThaiMoi == null) {
            throw new RuntimeException("Trạng thái không được để trống.");
        }

        Integer trangThaiHienTai = chuanHoaTrangThaiDonHang(
                donHang.getTrangThai()
        );

        if (!trangThaiHienTai.equals(donHang.getTrangThai())) {
            donHang.setTrangThai(trangThaiHienTai);
        }

        if (DonHang.TT_DA_HUY.equals(trangThaiHienTai)) {
            throw new RuntimeException(
                    "Đơn hàng đã bị hủy, không thể cập nhật trạng thái."
            );
        }

        if (DonHang.TT_HOAN_THANH.equals(trangThaiHienTai)) {
            throw new RuntimeException(
                    "Đơn hàng đã hoàn thành, không thể cập nhật tiếp."
            );
        }

        if (DonHang.TT_DOI_TAC_TU_CHOI.equals(trangThaiHienTai)) {
            throw new RuntimeException(
                    "Đơn hàng đã bị đối tác từ chối, không thể cập nhật tiếp."
            );
        }

        // Nếu đơn hàng ở trạng thái Mới tạo (sau khi đối tác từ chối), cho phép cập nhật
        // để nhân viên có thể gửi lại đơn hàng
        if (!DonHang.TT_MOI_TAO.equals(trangThaiHienTai)) {
            if (coDoiTacTuChoi(maDonHang)) {
                donHang.setTrangThai(DonHang.TT_DOI_TAC_TU_CHOI);
                donHangRepository.save(donHang);

                throw new RuntimeException(
                        "Có đối tác đã từ chối đơn hàng, không thể cập nhật tiếp."
                );
            }
        }

        if (!DonHang.TT_DA_HUY.equals(trangThaiMoi)
                && !TRANG_THAI_ORDER.contains(trangThaiMoi)) {
            throw new RuntimeException(
                    "Trạng thái '" + trangThaiMoi + "' không hợp lệ."
            );
        }

        /*
         * Chặn chính:
         * Nếu đơn đang chờ đối tác xác nhận,
         * chỉ cho chuyển sang "Đã xác nhận" khi tất cả đối tác đã xác nhận.
         */
        if (DonHang.TT_CHO_DOI_TAC_XAC_NHAN.equals(trangThaiHienTai)) {
            if (!DonHang.TT_DA_XAC_NHAN.equals(trangThaiMoi)) {
                throw new RuntimeException(
                        "Đơn hàng đang chờ đối tác xác nhận, chưa thể cập nhật trạng thái khác."
                );
            }

            if (!tatCaDoiTacDaChapNhan(maDonHang)) {
                throw new RuntimeException(
                        "Chưa thể chuyển sang Đã xác nhận vì chưa đủ đối tác xác nhận."
                );
            }
        }

        // Chặn nhân viên chuyển sang các trạng thái chỉ dành cho đối tác
        if (DonHang.TT_DANG_XU_LY.equals(trangThaiMoi) 
                || DonHang.TT_DA_GIAO.equals(trangThaiMoi)) {
            throw new RuntimeException(
                    "Nhân viên không thể chuyển sang trạng thái Đang xử lý hoặc Đã giao. "
                            + "Các trạng thái này chỉ dành cho đối tác."
            );
        }

        if (!DonHang.TT_DA_HUY.equals(trangThaiMoi)) {
            int currentIdx = TRANG_THAI_ORDER.indexOf(trangThaiHienTai);
            int nextIdx = TRANG_THAI_ORDER.indexOf(trangThaiMoi);

            if (currentIdx == -1) {
                throw new RuntimeException(
                        "Trạng thái hiện tại '" + trangThaiHienTai + "' không hợp lệ."
                );
            }

            // Cho phép chuyển từ Đã giao (9) sang Hoàn thành (6) trực tiếp
            if (DonHang.TT_DA_GIAO.equals(trangThaiHienTai) && DonHang.TT_HOAN_THANH.equals(trangThaiMoi)) {
                // Cho phép chuyển trực tiếp
            } else if (nextIdx != currentIdx + 1) {
                throw new RuntimeException(
                        "Chỉ có thể chuyển sang trạng thái tiếp theo. "
                                + "Hiện tại: '" + trangThaiHienTai
                                + "', yêu cầu: '" + trangThaiMoi + "'."
                );
            }
        }

        if (DonHang.TT_DANG_XU_LY.equals(trangThaiMoi)) {
            thongBaoService.taoThongBaoDonHangDangXuLy(
                    donHang.getMaDonHang()
            );
        } else if (
                DonHang.TT_HOAN_THANH.equals(
                        trangThaiMoi
                )
        ) {

            /*
             * Đối tác đã giao hàng và
             * trực tiếp thu tiền khách.
             *
             * Nhân viên bấm Hoàn thành
             * => mới được quyết toán Quỹ.
             */
            taiChinhDoiTacService
                    .quyetToanDonHang(
                            donHang.getMaDonHang()
                    );


            donHang.setTrangThaiThanhToan(
                    DonHang.TTTT_DA_THANH_TOAN
            );


            thongBaoService
                    .taoThongBaoDonHangThanhToan(
                            donHang.getMaDonHang()
                    );

            // Tự động tạo công nợ cho các đối tác khi đơn hàng hoàn thành
            congNoService.taoCongNoTuDonHang(
                    donHang.getMaDonHang()
            );
        }

        donHang.setTrangThai(trangThaiMoi);
        donHangRepository.save(donHang);

        return mapToDonHangResponse(donHang);
    }

    @Transactional
    public DonHangResponse huyDonHang(Integer maDonHang) {
        return capNhatTrangThai(maDonHang, DonHang.TT_DA_HUY);
    }

    @Transactional
    public DonHangResponse guiDonChoDoiTac(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        // Cho phép gửi lại đơn hàng khi:
        // - Trạng thái là Mới tạo (TT_MOI_TAO) - sau khi đối tác từ chối
        // - Hoặc trạng thái là Chờ đối tác xác nhận (để gửi lại nếu chưa gửi đủ)
        if (!DonHang.TT_MOI_TAO.equals(donHang.getTrangThai()) 
                && !DonHang.TT_CHO_DOI_TAC_XAC_NHAN.equals(donHang.getTrangThai())) {
            throw new RuntimeException(
                    "Chỉ có thể gửi đơn hàng ở trạng thái Mới tạo hoặc Chờ đối tác xác nhận"
            );
        }

        donHang.setTrangThai(DonHang.TT_CHO_DOI_TAC_XAC_NHAN);
        DonHang saved = donHangRepository.save(donHang);

        doiTacThongBaoService.taoThongBaoChoDonHang(maDonHang);

        return mapToDonHangResponse(saved);
    }

    @Transactional
    public DonHangResponse taoHopDong(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        if (!DonHang.TT_DA_XAC_NHAN.equals(donHang.getTrangThai())) {
            throw new RuntimeException(
                    "Chỉ có thể tạo hợp đồng cho đơn hàng ở trạng thái Đã xác nhận"
            );
        }

        if (hopDongRepository.existsByDonHang_MaDonHang(maDonHang)) {
            throw new RuntimeException(
                    "Đơn hàng đã có hợp đồng"
            );
        }

        HopDong hopDong = HopDong.builder()
                .donHang(donHang)
                .ngayKyHD(LocalDate.now())
                .trangThai(HopDong.DA_KY)
                .build();

        hopDongRepository.save(hopDong);

        // Gửi thông báo đã tạo hợp đồng cho các đối tác
        doiTacThongBaoService.taoThongBaoDaTaoHopDong(maDonHang);

        return mapToDonHangResponse(donHang);
    }

    @Transactional
    public DonHangResponse doiTacBaoDaGiao(
            Integer maDonHang,
            Integer maDoiTac
    ) {
        DonHang donHang =
                donHangRepository
                        .findById(maDonHang)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy đơn hàng #" +
                                                maDonHang
                                )
                        );

        // Chỉ lấy sản phẩm thuộc CHÍNH đối tác hiện tại
        List<ChiTietDonHang> chiTietsCuaDoiTac =
                chiTietDonHangRepository
                        .findByDonHangAndDoiTac(
                                maDonHang,
                                maDoiTac
                        );

        if (chiTietsCuaDoiTac.isEmpty()) {
            throw new RuntimeException(
                    "Đối tác này không có sản phẩm trong đơn hàng"
            );
        }

        // =========================================
        // BẮT BUỘC PHẢI ĐANG XỬ LÝ
        // 2 = đang xử lý
        // =========================================
        boolean dangXuLy =
                chiTietsCuaDoiTac.stream()
                        .allMatch(ct ->
                                Integer.valueOf(2)
                                        .equals(
                                                ct.getTrangThaiDoiTac()
                                        )
                        );

        if (!dangXuLy) {
            throw new RuntimeException(
                    "Bạn phải bắt đầu xử lý đơn hàng trước khi báo đã giao"
            );
        }

        // =========================================
        // 2 -> 3
        // ĐANG XỬ LÝ -> ĐÃ GIAO
        // =========================================
        for (ChiTietDonHang ct : chiTietsCuaDoiTac) {
            ct.setTrangThaiDoiTac(3);

            chiTietDonHangRepository.save(ct);
        }

        // =========================================
        // KIỂM TRA TOÀN BỘ ĐƠN
        // =========================================
        List<ChiTietDonHang> tatCaChiTiet =
                chiTietDonHangRepository
                        .findByDonHang_MaDonHang(
                                maDonHang
                        );

        boolean tatCaDaGiao =
                tatCaChiTiet.stream()
                        .allMatch(ct ->
                                Integer.valueOf(3)
                                        .equals(
                                                ct.getTrangThaiDoiTac()
                                        )
                        );

        if (tatCaDaGiao) {
            donHang.setTrangThai(
                    DonHang.TT_DA_GIAO
            );
        }

        DonHang saved =
                donHangRepository.save(
                        donHang
                );

        return mapToDonHangResponse(
                saved
        );
    }
    @Transactional
    public DonHangResponse thanhToanDonHang(
            Integer maDonHang
    ) {

        DonHang donHang =
                donHangRepository
                        .findById(maDonHang)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy đơn hàng #"
                                                + maDonHang
                                )
                        );


        if (
                !DonHang.TT_DA_GIAO.equals(
                        donHang.getTrangThai()
                )
        ) {

            throw new RuntimeException(
                    "Chỉ có thể thanh toán cho đơn hàng "
                            + "ở trạng thái Đã giao"
            );
        }


        /*
         * 20% An Yên
         * 80% Ví đối tác.
         */
        taiChinhDoiTacService
                .quyetToanDonHang(
                        maDonHang
                );


        donHang.setTrangThaiThanhToan(
                DonHang.TTTT_DA_THANH_TOAN
        );


        donHang.setTrangThai(
                DonHang.TT_HOAN_THANH
        );


        // Tự động tạo công nợ cho các đối tác khi đơn hàng hoàn thành
        congNoService.taoCongNoTuDonHang(
                maDonHang
        );


        DonHang saved =
                donHangRepository.save(
                        donHang
                );


        return mapToDonHangResponse(
                saved
        );
    }

    private DonHangResponse mapToDonHangResponse(DonHang donHang) {
        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository.findByDonHang_MaDonHang(
                        donHang.getMaDonHang()
                );

        HoaDon hoaDon = hoaDonRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang())
                .orElse(null);

        HopDong hopDong = hopDongRepository
                .findByDonHang_MaDonHang(donHang.getMaDonHang())
                .orElse(null);

        List<DonHangResponse.ChiTietDonHangResponse> sanPhams =
                chiTiets.stream()
                        .map(ct -> DonHangResponse.ChiTietDonHangResponse.builder()
                                .MaSanPham(ct.getSanPham().getMaSanPham())
                                .tenSanPham(ct.getSanPham().getTenSanPham())
                                .maSKU("SP" + String.format(
                                        "%03d",
                                        ct.getSanPham().getMaSanPham()
                                ))
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

        Integer currentTrangThai = donHang.getTrangThai();
        int currentIdx = TRANG_THAI_ORDER.indexOf(currentTrangThai);
        boolean isDaHuy = DonHang.TT_DA_HUY.equals(currentTrangThai);

        List<DonHangResponse.LichSuDonHangResponse> lichSu =
                new ArrayList<>();

        for (int i = 0; i < TRANG_THAI_ORDER.size(); i++) {
            Integer stepIdx = TRANG_THAI_ORDER.get(i);
            String step = getTrangThaiString(stepIdx);
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

            switch (stepIdx) {
                case 1:
                    color = "yellow";
                    break;
                case 2:
                    color = "pink";
                    break;
                case 3:
                    color = "blue";
                    break;
                case 4:
                    color = "orange";
                    break;
                case 5:
                    color = "purple";
                    break;
                case 6:
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
                .trangThai(chuanHoaTrangThaiDonHang(donHang.getTrangThai()))
                .GhiChu(donHang.getGhiChu())
                .phuongThucThanhToan(donHang.getPhuongThucThanhToan())
                .trangThaiThanhToan(donHang.getTrangThaiThanhToan())

                .maHoaDon(hoaDon != null ? hoaDon.getMaHoaDon() : null)
                .daCoHoaDon(hoaDon != null)
                .trangThaiHoaDon(hoaDon != null ? String.valueOf(hoaDon.getTrangThai()) : null)

                .daCoHopDong(hopDong != null)
                .maHopDong(hopDong != null ? hopDong.getMaHopDong() : null)
                .trangThaiHopDong(hopDong != null ? hopDong.getTrangThai() : null)

                .ngayGiaoDuKien(donHang.getNgayGiaoDuKien() != null ? donHang.getNgayGiaoDuKien().toString() : null)

                .phuongThucGiaoHang("Giao hàng tận nơi")
                .phiVanChuyen(BigDecimal.ZERO)
                .giamGia(BigDecimal.ZERO)

                .nguoiBaoCaoSuCo(donHang.getNguoiBaoCaoSuCo())
                .lyDoSuCo(donHang.getLyDoSuCo())

                .sanPhams(sanPhams)
                .lichSu(lichSu)
                .build();
    }

    @Transactional
    public DonHangResponse capNhatTrangThaiNhanVien(
            Integer maDonHang,
            Integer trangThaiMoi
    ) {
        return capNhatTrangThai(maDonHang, trangThaiMoi);
    }

    @Transactional
    public DonHangResponse huyDonHang(
            Integer maDonHang,
            HuyDonHangRequest request,
            Authentication authentication
    ) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đơn hàng"
                ));

        String lyDo = request.getLyDoHuy() == null
                ? ""
                : request.getLyDoHuy().trim();

        if (lyDo.length() <= 3) {
            throw new RuntimeException("Lý do hủy phải trên 3 ký tự");
        }

        if (DonHang.TT_DA_HUY.equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng này đã bị hủy trước đó");
        }

        if (DonHang.TT_HOAN_THANH.equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }

        donHang.setTrangThai(DonHang.TT_DA_HUY);
        donHang.setLyDoHuy(lyDo);

        DonHang saved = donHangRepository.save(donHang);

        // Tạo thông báo cho bên kia
        try {
            String nguoiHuy = authentication.getName();
            String tenNguoiHuy = nguoiHuy;

            // Xác định người hủy là nhân viên hay đối tác
            boolean laNhanVien = nhanVienRepository.findByTenDangNhap(nguoiHuy).isPresent();
            if (laNhanVien) {
                NhanVien nv = nhanVienRepository.findByTenDangNhap(nguoiHuy).get();
                tenNguoiHuy = nv.getHoTen();
                // Tạo thông báo cho đối tác
                thongBaoService.taoThongBaoHuyDonChoDoiTac(maDonHang, tenNguoiHuy, lyDo);
            } else {
                // Tạo thông báo cho nhân viên
                thongBaoService.taoThongBaoHuyDonChoNhanVien(maDonHang, tenNguoiHuy, lyDo);
            }
        } catch (Exception e) {
            // Không throw lỗi nếu tạo thông báo thất bại, đơn hàng vẫn đã được hủy
            System.err.println("Lỗi khi tạo thông báo hủy đơn: " + e.getMessage());
        }

        return mapToDonHangResponse(saved);
    }

    private DoiTacDonHangResponse mapToDoiTacDonHangResponse(
            DonHang donHang,
            Integer maDoiTac
    ) {
        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository.findByDonHangAndDoiTac(
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

        List<DoiTacDonHangResponse.SanPhamTrongDonResponse> sanPhams =
                new ArrayList<>();

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
                                    giaTien.multiply(
                                            BigDecimal.valueOf(soLuong)
                                    )
                            )
                            .ngayGiaoDuKien(ct.getNgayGiaoDuKien() != null ? ct.getNgayGiaoDuKien().toString() : "")
                            .build()
            );
        }

        KhachHang khachHang = donHang.getKhachHang();
        NhanVien nhanVien = donHang.getNhanVien();

        // Tính trạng thái riêng của đối tác dựa trên trangThaiDoiTac của các sản phẩm
        // 0 = chưa nhận, 1 = đã nhận, 2 = đang xử lý, 3 = đã giao
        // 0 = chờ xác nhận
// 1 = đã nhận
// 2 = đang xử lý
// 3 = đã giao
        String trangThaiRieng = "Chờ xác nhận";

        if (!chiTiets.isEmpty()) {

            boolean tatCaDaGiao =
                    chiTiets.stream()
                            .allMatch(ct ->
                                    Integer.valueOf(3)
                                            .equals(ct.getTrangThaiDoiTac())
                            );

            boolean tatCaDangXuLy =
                    chiTiets.stream()
                            .allMatch(ct ->
                                    Integer.valueOf(2)
                                            .equals(ct.getTrangThaiDoiTac())
                            );

            boolean tatCaDaNhan =
                    chiTiets.stream()
                            .allMatch(ct ->
                                    Integer.valueOf(1)
                                            .equals(ct.getTrangThaiDoiTac())
                            );

            if (tatCaDaGiao) {
                trangThaiRieng = "Đã giao";

            } else if (tatCaDangXuLy) {
                trangThaiRieng = "Đang xử lý";

            } else if (tatCaDaNhan) {
                trangThaiRieng = "Đã nhận";

            } else {
                trangThaiRieng = "Chờ xác nhận";
            }
        }

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
                .tenDangNhapNhanVien(
                        nhanVien != null
                                ? nhanVien.getTenDangNhap()
                                : ""
                )
                .phuongThucThanhToan(
                        getPhuongThucThanhToanString(donHang.getPhuongThucThanhToan())
                )

                .ghiChu(donHang.getGhiChu())
                .trangThai(getTrangThaiString(donHang.getTrangThai()))
                .trangThaiRieng(trangThaiRieng)
                .tongCong(tongCong)
                .coHopDong(hopDongRepository.existsByDonHang_MaDonHang(donHang.getMaDonHang()))
                .nguoiBaoCaoSuCo(donHang.getNguoiBaoCaoSuCo())
                .lyDoSuCo(donHang.getLyDoSuCo())

                .sanPhams(sanPhams)
                .build();
    }

    private String getPhuongThucThanhToanString(Integer pt) {
        if (pt == null) return "Chưa cập nhật";
        return switch (pt) {
            case 1 -> "Tiền mặt";
            case 2 -> "Chuyển khoản";
            case 3 -> "Payoo";
            default -> "Chưa cập nhật";
        };
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
                .map(donHang -> mapToDoiTacDonHangResponse(
                        donHang,
                        maDoiTac
                ))
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

    @Transactional
    public void xuLyDonHangDoiTac(
            Integer maDonHang,
            Integer maDoiTac,
            LocalDate ngayGiaoDuKien
    ) {
        DonHang donHang = donHangRepository
                .findById(maDonHang)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy đơn hàng"
                        )
                );

        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository
                        .findByDonHangAndDoiTac(
                                maDonHang,
                                maDoiTac
                        );

        if (chiTiets.isEmpty()) {
            throw new RuntimeException(
                    "Đơn hàng không thuộc đối tác này"
            );
        }

        // =========================================
        // BẮT BUỘC PHẢI CHẤP NHẬN TRƯỚC
        // 1 = đã nhận
        // =========================================
        boolean daChapNhan =
                chiTiets.stream()
                        .allMatch(ct ->
                                Integer.valueOf(1)
                                        .equals(
                                                ct.getTrangThaiDoiTac()
                                        )
                        );

        if (!daChapNhan) {
            throw new RuntimeException(
                    "Bạn phải chấp nhận đơn hàng trước khi bắt đầu xử lý"
            );
        }

        // =========================================
        // KIỂM TRA HỢP ĐỒNG NẾU CÓ QUAN TÀI
        // =========================================
        boolean coHopDong =
                hopDongRepository
                        .existsByDonHang_MaDonHang(
                                maDonHang
                        );

        boolean coQuanTai =
                donHangCoSanPhamQuanTai(
                        maDonHang
                );

        if (coQuanTai && !coHopDong) {
            throw new RuntimeException(
                    "Đơn hàng có sản phẩm quan tài. " +
                            "Cần tạo hợp đồng trước khi xử lý."
            );
        }

        // =========================================
        // 1 -> 2
        // ĐÃ NHẬN -> ĐANG XỬ LÝ
        // =========================================
        for (ChiTietDonHang ct : chiTiets) {
            ct.setNgayGiaoDuKien(
                    ngayGiaoDuKien
            );

            ct.setTrangThaiDoiTac(2);

            chiTietDonHangRepository.save(ct);
        }

        /*
         * Chỉ đổi trạng thái tổng sang Đang xử lý
         * khi tất cả chi tiết của toàn đơn đã >= 2.
         */
        List<ChiTietDonHang> tatCaChiTiet =
                chiTietDonHangRepository
                        .findByDonHang_MaDonHang(
                                maDonHang
                        );

        boolean tatCaDangXuLy =
                tatCaChiTiet.stream()
                        .allMatch(ct ->
                                ct.getTrangThaiDoiTac() != null
                                        &&
                                        ct.getTrangThaiDoiTac() >= 2
                        );

        if (tatCaDangXuLy) {
            donHang.setTrangThai(
                    DonHang.TT_DANG_XU_LY
            );

            donHangRepository.save(
                    donHang
            );
        }

        thongBaoService
                .taoThongBaoDoiTacXuLyDonHang(
                        maDonHang,
                        maDoiTac,
                        ngayGiaoDuKien
                );
    }

    private boolean tatCaDoiTacDaChapNhan(Integer maDonHang) {
        long tongDoiTac =
                donHangRepository.countDoiTacTrongDonHang(maDonHang);

        long soDoiTacDaChapNhan =
                donHangRepository.countDoiTacDaChapNhan(maDonHang);

        return tongDoiTac > 0 && tongDoiTac == soDoiTacDaChapNhan;
    }

    private boolean coDoiTacTuChoi(Integer maDonHang) {
        return donHangRepository.countDoiTacTuChoi(maDonHang) > 0;
    }

    private void kiemTraKhongThemSanPhamCuaDoiTacTuChoi(
            Integer maDonHang,
            List<TaoDonHangRequest.SanPhamTrongDonRequest> items
    ) {
        // Lấy đơn hàng để kiểm tra ghi chú
        DonHang donHang = donHangRepository.findById(maDonHang).orElse(null);
        if (donHang == null || donHang.getGhiChu() == null) {
            return; // Không có ghi chú, không cần kiểm tra
        }

        // Parse ghi chú để lấy danh sách mã sản phẩm bị từ chối
        // Format: "Sản phẩm bị từ chối: [1,2,3]"
        List<Integer> maSanPhamsBiTuChoi = new ArrayList<>();
        String ghiChu = donHang.getGhiChu();
        
        // Tìm tất cả các pattern "Sản phẩm bị từ chối: [...]"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Sản phẩm bị từ chối: \\[(.*?)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(ghiChu);
        
        while (matcher.find()) {
            String sanPhamStr = matcher.group(1);
            String[] parts = sanPhamStr.split(",");
            for (String part : parts) {
                try {
                    maSanPhamsBiTuChoi.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu không parse được
                }
            }
        }

        if (maSanPhamsBiTuChoi.isEmpty()) {
            return; // Không có sản phẩm nào bị từ chối, không cần kiểm tra
        }

        // Kiểm tra từng sản phẩm trong request
        for (TaoDonHangRequest.SanPhamTrongDonRequest item : items) {
            if (maSanPhamsBiTuChoi.contains(item.getMaSanPham())) {
                SanPham sanPham = sanPhamRepository.findById(item.getMaSanPham()).orElse(null);
                String tenSanPham = sanPham != null ? sanPham.getTenSanPham() : "sản phẩm này";

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Không thể thêm sản phẩm \"" + tenSanPham + "\" vì sản phẩm này đã bị đối tác từ chối. Vui lòng chọn sản phẩm khác."
                );
            }
        }
    }

    @Transactional
    public void baoCaoSuCo(Integer maDonHang, String lyDoSuCo, Authentication authentication) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        // Kiểm tra trạng thái hiện tại - không thể báo cáo sự cố nếu đã hủy hoặc hoàn thành
        if (DonHang.TT_DA_HUY.equals(donHang.getTrangThai()) ||
            DonHang.TT_HOAN_THANH.equals(donHang.getTrangThai()) ||
            DonHang.TT_DOI_TAC_TU_CHOI.equals(donHang.getTrangThai()) ||
            DonHang.TT_GAP_SU_CO.equals(donHang.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn hàng không thể báo cáo sự cố ở trạng thái hiện tại");
        }

        // Lưu trạng thái cũ trước khi gặp sự cố
        String nguoiBaoCao = authentication.getName();

        donHang.setTrangThaiTruocSuCo(donHang.getTrangThai());
        donHang.setTrangThai(DonHang.TT_GAP_SU_CO);
        donHang.setLyDoSuCo(lyDoSuCo);
        donHang.setNguoiBaoCaoSuCo(nguoiBaoCao);
        donHangRepository.save(donHang);

        // Tạo thông báo cho bên kia
        try {
            String tenNguoiBaoCao = nguoiBaoCao;

            // Xác định người báo cáo là nhân viên hay đối tác
            boolean laNhanVien = nhanVienRepository.findByTenDangNhap(nguoiBaoCao).isPresent();
            if (laNhanVien) {
                NhanVien nv = nhanVienRepository.findByTenDangNhap(nguoiBaoCao).get();
                tenNguoiBaoCao = nv.getHoTen();
                // Tạo thông báo cho đối tác
                thongBaoService.taoThongBaoSuCoChoDoiTac(maDonHang, tenNguoiBaoCao, lyDoSuCo);
            } else {
                // Tạo thông báo cho nhân viên
                thongBaoService.taoThongBaoSuCoChoNhanVien(maDonHang, tenNguoiBaoCao, lyDoSuCo);
            }
        } catch (Exception e) {
            // Không throw lỗi nếu tạo thông báo thất bại, đơn hàng vẫn đã được báo cáo sự cố
            System.err.println("Lỗi khi tạo thông báo sự cố: " + e.getMessage());
        }
    }

    @Transactional
    public void giaiQuyetSuCo(Integer maDonHang, Authentication authentication) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (!DonHang.TT_GAP_SU_CO.equals(donHang.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn hàng không ở trạng thái gặp sự cố");
        }

        if (donHang.getTrangThaiTruocSuCo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy trạng thái trước khi gặp sự cố");
        }

        // Khôi phục trạng thái cũ
        donHang.setTrangThai(donHang.getTrangThaiTruocSuCo());
        donHang.setTrangThaiTruocSuCo(null);
        donHang.setLyDoSuCo(null);
        donHangRepository.save(donHang);

        // Tạo thông báo đã giải quyết sự cố
        String nguoiGiaiQuyet = authentication.getName();
        String tenNguoiGiaiQuyet = nguoiGiaiQuyet;

        boolean laNhanVien = nhanVienRepository.findByTenDangNhap(nguoiGiaiQuyet).isPresent();
        if (laNhanVien) {
            NhanVien nv = nhanVienRepository.findByTenDangNhap(nguoiGiaiQuyet).get();
            tenNguoiGiaiQuyet = nv.getHoTen();
            thongBaoService.taoThongBaoDaGiaiQuyetSuCoChoDoiTac(maDonHang, tenNguoiGiaiQuyet);
        } else {
            thongBaoService.taoThongBaoDaGiaiQuyetSuCoChoNhanVien(maDonHang, tenNguoiGiaiQuyet);
        }
    }
    private boolean donHangCoSanPhamQuanTai(
            Integer maDonHang
    ) {
        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository
                        .findByDonHang_MaDonHang(
                                maDonHang
                        );

        return chiTiets.stream()
                .map(ChiTietDonHang::getSanPham)
                .filter(sanPham -> sanPham != null)
                .anyMatch(this::laSanPhamQuanTai);
    }

    private boolean laSanPhamQuanTai(
            SanPham sanPham
    ) {
        String loai = chuanHoaLoaiSanPham(
                sanPham.getLoai()
        );

        String ten = chuanHoaLoaiSanPham(
                sanPham.getTenSanPham()
        );

        return loai.contains("quan tai")
                || ten.contains("quan tai");
    }

    private String chuanHoaLoaiSanPham(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return java.text.Normalizer
                .normalize(
                        value,
                        java.text.Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D")
                .toLowerCase(
                        java.util.Locale.ROOT
                )
                .trim();
    }
}