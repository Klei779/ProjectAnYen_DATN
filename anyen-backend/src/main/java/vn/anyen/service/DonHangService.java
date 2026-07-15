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

    private static final List<Integer> TRANG_THAI_ORDER = Arrays.asList(
            DonHang.TT_MOI_TAO,
            DonHang.TT_CHO_DOI_TAC_XAC_NHAN,
            DonHang.TT_DA_XAC_NHAN,
            DonHang.TT_DANG_XU_LY,
            DonHang.TT_CHO_THANH_TOAN,
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
                .trangThai(DonHang.TT_MOI_TAO)
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
                request.getItems()
        );

        savedDonHang.setTongTien(tongTien);

        DonHang donHangDaLuu = donHangRepository.save(savedDonHang);

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
    public DonHangResponse doiTacChapNhanDonHang(Integer maDonHang, Integer maDoiTac) {
        return capNhatTrangThai(maDonHang, DonHang.TT_DA_XAC_NHAN);
    }

    @Transactional
    public DonHangResponse doiTacTuChoiDonHang(Integer maDonHang, Integer maDoiTac) {
        return capNhatTrangThai(maDonHang, DonHang.TT_DOI_TAC_TU_CHOI);
    }

    @Transactional
    public DonHangResponse doiTacXuLyDonHang(Integer maDonHang, Integer maDoiTac, String thoiGianUocTinh) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));
        
        // Thêm ghi chú thời gian ước tính
        String ghiChuMoi = (donHang.getGhiChu() == null ? "" : donHang.getGhiChu() + "\n") 
                + "Đối tác hẹn giao lúc: " + thoiGianUocTinh;
        donHang.setGhiChu(ghiChuMoi.trim());
        donHangRepository.save(donHang);
        
        // Gửi thông báo cho nhân viên
        thongBaoService.taoThongBaoDonHangDangXuLy(maDonHang);
        
        return capNhatTrangThai(maDonHang, DonHang.TT_DANG_XU_LY);
    }

    @Transactional
    public DonHangResponse doiTacDaGiaoDonHang(Integer maDonHang, Integer maDoiTac) {
        return capNhatTrangThai(maDonHang, DonHang.TT_CHO_THANH_TOAN);
    }

    @Transactional
    public DonHangResponse doiTacHuyDonHang(Integer maDonHang, Integer maDoiTac, String lyDo) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));
        
        String ghiChuMoi = (donHang.getGhiChu() == null ? "" : donHang.getGhiChu() + "\n") 
                + "Đối tác hủy đơn. Lý do: " + lyDo;
        donHang.setGhiChu(ghiChuMoi.trim());
        donHangRepository.save(donHang);
        
        return capNhatTrangThai(maDonHang, DonHang.TT_DA_HUY);
    }

    @Transactional
    public void doiTacBaoCaoSuCo(Integer maDonHang, Integer maDoiTac, String lyDo) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));
        
        String noiDung = "Đối tác báo cáo sự cố đơn hàng #DH" + String.format("%03d", maDonHang) + ". Lý do: " + lyDo;
        // ThongBaoService doesn't have a direct method for this, so we'll use a generic method if available, or just create one.
        thongBaoService.taoThongBaoHeThongChoNhanVien(
            donHang.getNhanVien().getMaNhanVien(),
            "Báo cáo sự cố đơn hàng #DH" + String.format("%03d", maDonHang),
            noiDung
        );
    }

    @Transactional
    public DonHangResponse guiDoiTac(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng #" + maDonHang
                ));

        if (!DonHang.TT_MOI_TAO.equals(donHang.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ có thể gửi đối tác khi đơn hàng ở trạng thái Mới tạo"
            );
        }

        donHang.setTrangThai(DonHang.TT_CHO_DOI_TAC_XAC_NHAN);
        donHangRepository.save(donHang);

        doiTacThongBaoService.taoThongBaoChoDonHang(maDonHang);

        return mapToDonHangResponse(donHang);
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

        hoanTonKhoVaXoaChiTietCu(donHang);

        BigDecimal tongTienMoi = taoChiTietDonHangVaTruTonKho(
                donHang,
                request.getItems()
        );

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

    private String getTrangThaiString(Integer tt) {
        if(tt == null) return "Khong xac dinh";
        switch(tt) {
            case 1: return "Mới tạo";
            case 2: return "Chờ đối tác xác nhận";
            case 3: return "Đã xác nhận";
            case 4: return "Đang xử lý";
            case 5: return "Chờ thanh toán";
            case 6: return "Hoàn thành";
            case 7: return "Đã hủy";
            case 8: return "Đối tác đã từ chối";
            default: return "Khong xac dinh";
        }
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

        Integer trangThaiHienTai = donHang.getTrangThai();

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

        if (coDoiTacTuChoi(maDonHang)) {
            donHang.setTrangThai(DonHang.TT_DOI_TAC_TU_CHOI);
            donHangRepository.save(donHang);

            throw new RuntimeException(
                    "Có đối tác đã từ chối đơn hàng, không thể cập nhật tiếp."
            );
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

        if (!DonHang.TT_DA_HUY.equals(trangThaiMoi)) {
            int currentIdx = TRANG_THAI_ORDER.indexOf(trangThaiHienTai);
            int nextIdx = TRANG_THAI_ORDER.indexOf(trangThaiMoi);

            if (currentIdx == -1) {
                throw new RuntimeException(
                        "Trạng thái hiện tại '" + trangThaiHienTai + "' không hợp lệ."
                );
            }

            if (nextIdx != currentIdx + 1) {
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
        } else if (DonHang.TT_HOAN_THANH.equals(trangThaiMoi)) {
            thongBaoService.taoThongBaoDonHangThanhToan(
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
                .trangThai(donHang.getTrangThai())
                .GhiChu(donHang.getGhiChu())
                .phuongThucThanhToan(donHang.getPhuongThucThanhToan())
                .trangThaiThanhToan(donHang.getTrangThaiThanhToan())

                .maHoaDon(hoaDon != null ? hoaDon.getMaHoaDon() : null)
                .daCoHoaDon(hoaDon != null)
                .trangThaiHoaDon(hoaDon != null ? String.valueOf(hoaDon.getTrangThai()) : null)

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
            Integer trangThaiMoi
    ) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + maDonHang));
                
        Integer trangThaiHienTai = donHang.getTrangThai();
        
        if (!DonHang.TT_HOAN_THANH.equals(trangThaiMoi)) {
            throw new RuntimeException("Nhân viên chỉ có thể cập nhật trạng thái Hoàn thành.");
        }
        
        if (!DonHang.TT_CHO_THANH_TOAN.equals(trangThaiHienTai)) {
            throw new RuntimeException("Chỉ có thể Hoàn thành đơn hàng khi đang ở trạng thái Chờ thanh toán.");
        }
        
        return capNhatTrangThai(maDonHang, trangThaiMoi);
    }

    @Transactional
    public DonHangResponse huyDonHang(
            Integer maDonHang,
            HuyDonHangRequest request
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
                .trangThai(getTrangThaiString(donHang.getTrangThai()))
                .tongCong(tongCong)
                .daCoHopDong(hopDongRepository.existsByDonHang_MaDonHang(donHang.getMaDonHang()))

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
}