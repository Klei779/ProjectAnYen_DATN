package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;
import vn.anyen.dto.request.TuChoiThongBaoRequest;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.dto.response.DoiTacThongBaoResponse;
import vn.anyen.dto.response.XuLyThongBaoResponse;
import vn.anyen.entity.*;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.SanPhamRepository;
import vn.anyen.repository.ThongBaoDoiTacRepository;
import vn.anyen.constants.AppLabels;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class DoiTacThongBaoService {

    private final ThongBaoDoiTacRepository thongBaoRepository;
    private final DoiTacRepository doiTacRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final ThongBaoService thongBaoService;
    private final SanPhamRepository sanPhamRepository;

    // Loai dùng String để khớp VARCHAR trong DB
    private static final String LOAI_DON_HANG = ThongBaoDoiTac.LOAI_DON_HANG;
    private static final String LOAI_DUYET_SAN_PHAM = ThongBaoDoiTac.LOAI_DUYET_SAN_PHAM;

    // TrangThaiThongBao dùng String để khớp VARCHAR trong DB
    private static final String CHO_XAC_NHAN = ThongBaoDoiTac.TRANG_THAI_CHO_XAC_NHAN;
    private static final String DA_CHAP_NHAN = ThongBaoDoiTac.TRANG_THAI_DA_CHAP_NHAN;
    private static final String DA_TU_CHOI = ThongBaoDoiTac.TRANG_THAI_DA_TU_CHOI;

    // Pattern để parse maSanPham từ NoiDung: [MASP:123]
    private static final Pattern MASP_PATTERN = Pattern.compile("\\[MASP:(\\d+)\\]");
    private static final Pattern TEN_SAN_PHAM_PATTERN = Pattern.compile(
            "Sản phẩm\\s+\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    @Transactional(readOnly = true)
    public List<DoiTacThongBaoResponse> getThongBao(Authentication authentication) {
        DoiTac doiTac = getDoiTacDangNhap(authentication);

        List<ThongBaoDoiTac> thongBaos =
                thongBaoRepository.findByDoiTac_MaDoiTacOrderByThoiGianTaoDesc(
                        doiTac.getMaDoiTac()
                );

        return thongBaos.stream()
                .map(tb -> {
                    if (LOAI_DUYET_SAN_PHAM.equals(tb.getLoai())) {
                        return mapToSanPhamThongBaoResponse(tb);
                    }

                    return mapToThongBaoResponse(tb, doiTac.getMaDoiTac());
                })
                .toList();
    }

    public XuLyThongBaoResponse chapNhanThongBao(
            Integer maThongBao,
            Authentication authentication
    ) {
        DoiTac doiTac = getDoiTacDangNhap(authentication);

        ThongBaoDoiTac thongBao =
                getThongBaoCuaDoiTac(maThongBao, doiTac.getMaDoiTac());

        kiemTraChoXacNhan(thongBao);

        thongBao.setTrangThaiThongBao(DA_CHAP_NHAN);
        thongBao.setDaDoc(true);
        thongBao.setThoiGianXuLy(LocalDateTime.now());

        DonHang donHang = thongBao.getDonHang();

        thongBaoRepository.save(thongBao);

        if (donHang != null) {
            capNhatTrangThaiDonHangKhiTatCaDoiTacChapNhan(donHang);

            thongBaoService.taoThongBaoChapNhanDonHang(donHang.getMaDonHang());
            thongBaoService.taoThongBaoChapNhanDonHangChoAdmin(donHang.getMaDonHang());
        }

        return XuLyThongBaoResponse.builder()
                .success(true)
                .message("Đã chấp nhận thông báo")
                .redirectUrl(donHang != null ? "/doi-tac/quan-ly-don-hang" : null)
                .maThongBao(thongBao.getMaThongBao())
                .maDonHang(donHang != null ? donHang.getMaDonHang() : null)
                .build();
    }

    public XuLyThongBaoResponse tuChoiThongBao(
            Integer maThongBao,
            TuChoiThongBaoRequest request,
            Authentication authentication
    ) {
        DoiTac doiTac = getDoiTacDangNhap(authentication);
        ThongBaoDoiTac thongBao =
                getThongBaoCuaDoiTac(maThongBao, doiTac.getMaDoiTac());

        kiemTraChoXacNhan(thongBao);

        thongBao.setTrangThaiThongBao(DA_TU_CHOI);
        thongBao.setLyDoTuChoi(request.getLyDo());
        thongBao.setDaDoc(true);
        thongBao.setThoiGianXuLy(LocalDateTime.now());

        DonHang donHang = thongBao.getDonHang();

        if (donHang != null) {
            donHang.setTrangThai(DonHang.TT_DOI_TAC_TU_CHOI);

            String ghiChuCu = donHang.getGhiChu() == null
                    ? ""
                    : donHang.getGhiChu();

            String ghiChuMoi =
                    ghiChuCu
                            + "\nĐối tác "
                            + doiTac.getTenDoiTac()
                            + " từ chối. Lý do: "
                            + request.getLyDo();

            donHang.setGhiChu(ghiChuMoi.trim());
            donHangRepository.save(donHang);
        }

        thongBaoRepository.save(thongBao);

        return XuLyThongBaoResponse.builder()
                .success(true)
                .message("Đã từ chối đơn hàng")
                .redirectUrl(null)
                .maThongBao(thongBao.getMaThongBao())
                .maDonHang(donHang != null ? donHang.getMaDonHang() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public DoiTacDonHangPageResponse getDonHangDaChapNhan(
            Authentication authentication
    ) {
        DoiTac doiTac = getDoiTacDangNhap(authentication);

        List<ThongBaoDoiTac> thongBaos =
                thongBaoRepository
                        .findByDoiTac_MaDoiTacAndLoaiAndTrangThaiThongBaoOrderByThoiGianXuLyDesc(
                                doiTac.getMaDoiTac(),
                                LOAI_DON_HANG,
                                DA_CHAP_NHAN
                        );

        List<DoiTacDonHangResponse> items =
                thongBaos.stream()
                        .filter(tb -> tb.getDonHang() != null)
                        .map(tb -> mapToDonHangResponse(
                                tb.getDonHang(),
                                doiTac.getMaDoiTac()
                        ))
                        .toList();

        return DoiTacDonHangPageResponse.builder()
                .items(items)
                .total(items.size())
                .build();
    }

    @Transactional(readOnly = true)
    public DoiTacDonHangResponse getChiTietDonHang(
            Integer maDonHang,
            Authentication authentication
    ) {
        DoiTac doiTac = getDoiTacDangNhap(authentication);

        ThongBaoDoiTac thongBao =
                thongBaoRepository
                        .findByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoai(
                                doiTac.getMaDoiTac(),
                                maDonHang,
                                LOAI_DON_HANG
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy đơn hàng của đối tác"
                        ));

        if (!DA_CHAP_NHAN.equals(thongBao.getTrangThaiThongBao())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng này chưa được đối tác chấp nhận"
            );
        }

        return mapToDonHangResponse(
                thongBao.getDonHang(),
                doiTac.getMaDoiTac()
        );
    }

    public XuLyThongBaoResponse updateTrangThaiDonHang(
            Integer maDonHang,
            CapNhatTrangThaiDonHangRequest request,
            Authentication authentication
    ) {
        DoiTac doiTac = getDoiTacDangNhap(authentication);

        ThongBaoDoiTac thongBao =
                thongBaoRepository
                        .findByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoai(
                                doiTac.getMaDoiTac(),
                                maDonHang,
                                LOAI_DON_HANG
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy đơn hàng của đối tác"
                        ));

        if (!DA_CHAP_NHAN.equals(thongBao.getTrangThaiThongBao())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ được cập nhật đơn hàng đã chấp nhận"
            );
        }

        if (request.getTrangThai() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái không được để trống"
            );
        }

        DonHang donHang = thongBao.getDonHang();

        donHang.setTrangThai(request.getTrangThai());

        donHangRepository.save(donHang);

        return XuLyThongBaoResponse.builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .redirectUrl(null)
                .maThongBao(thongBao.getMaThongBao())
                .maDonHang(donHang.getMaDonHang())
                .build();
    }

    public void taoThongBaoChoDonHang(Integer maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng"
                ));

        List<Integer> maDoiTacs =
                chiTietDonHangRepository.findMaDoiTacsByDonHang(maDonHang);

        for (Integer maDoiTac : maDoiTacs) {
            DoiTac doiTac = doiTacRepository.findById(maDoiTac)
                    .orElse(null);

            if (doiTac == null) {
                continue;
            }

            boolean daCoThongBao =
                    thongBaoRepository
                            .existsByDoiTac_MaDoiTacAndDonHang_MaDonHangAndLoai(
                                    doiTac.getMaDoiTac(),
                                    maDonHang,
                                    LOAI_DON_HANG
                            );

            if (daCoThongBao) {
                continue;
            }

            String tenKhachHang = donHang.getKhachHang() != null
                    ? donHang.getKhachHang().getTenKhachHang()
                    : "Khách hàng";

            String tongTien = donHang.getTongTien() != null
                    ? donHang.getTongTien().toPlainString()
                    : "0";

            ThongBaoDoiTac thongBao = ThongBaoDoiTac.builder()
                    .doiTac(doiTac)
                    .donHang(donHang)
                    .loai(LOAI_DON_HANG)
                    .tieuDe("Đơn hàng mới #DH"
                            + String.format("%03d", maDonHang))
                    .noiDung("Khách hàng: " + tenKhachHang
                            + " - Tổng tiền: " + tongTien + " đ")
                    .trangThaiThongBao(CHO_XAC_NHAN)
                    .daDoc(false)
                    .thoiGianTao(LocalDateTime.now())
                    .build();

            thongBaoRepository.save(thongBao);
        }
    }

    // ========================
    // Private helpers
    // ========================

    private DoiTac getDoiTacDangNhap(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        return doiTacRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Không tìm thấy tài khoản đối tác"
                ));
    }

    private ThongBaoDoiTac getThongBaoCuaDoiTac(
            Integer maThongBao,
            Integer maDoiTac
    ) {
        return thongBaoRepository
                .findByMaThongBaoAndDoiTac_MaDoiTac(
                        maThongBao,
                        maDoiTac
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông báo"
                ));
    }

    private void kiemTraChoXacNhan(ThongBaoDoiTac thongBao) {
        if (!CHO_XAC_NHAN.equals(thongBao.getTrangThaiThongBao())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông báo này đã được xử lý"
            );
        }
    }

    /**
     * Parse maSanPham từ chuỗi NoiDung.
     * Format: "...nội dung... [MASP:123]"
     */
    private Integer parseMaSanPhamFromNoiDung(String noiDung) {
        if (noiDung == null) return null;
        Matcher matcher = MASP_PATTERN.matcher(noiDung);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String parseTenSanPhamFromNoiDung(String noiDung) {
        if (noiDung == null || noiDung.isBlank()) {
            return null;
        }

        Matcher matcher = TEN_SAN_PHAM_PATTERN.matcher(noiDung);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String cleanNoiDungSanPham(String noiDung) {
        if (noiDung == null) {
            return "";
        }

        return MASP_PATTERN.matcher(noiDung)
                .replaceAll("")
                .trim();
    }

    private DoiTacThongBaoResponse mapToThongBaoResponse(
            ThongBaoDoiTac thongBao,
            Integer maDoiTac
    ) {
        DonHang donHang = thongBao.getDonHang();

        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository
                        .findByDonHang_MaDonHang(donHang.getMaDonHang())
                        .stream()
                        .filter(ct ->
                                ct.getSanPham() != null
                                        && ct.getSanPham().getMaDoiTac() != null
                                        && maDoiTac.equals(
                                        ct.getSanPham().getMaDoiTac()
                                )
                        )
                        .toList();

        ChiTietDonHang first =
                chiTiets.isEmpty() ? null : chiTiets.get(0);

        BigDecimal tongTienCuaDoiTac =
                chiTiets.stream()
                        .map(ct -> {
                            BigDecimal gia = ct.getGiaTien() == null
                                    ? BigDecimal.ZERO
                                    : ct.getGiaTien();

                            int soLuong = ct.getSoLuong() == null
                                    ? 0
                                    : ct.getSoLuong();

                            return gia.multiply(BigDecimal.valueOf(soLuong));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        int tongSoLuong =
                chiTiets.stream()
                        .mapToInt(ct -> ct.getSoLuong() == null
                                ? 0
                                : ct.getSoLuong())
                        .sum();

        String tenKhachHang = donHang.getKhachHang() != null
                ? donHang.getKhachHang().getTenKhachHang()
                : "";

        return DoiTacThongBaoResponse.builder()
                .id(thongBao.getMaThongBao())
                .category("order")
                .type("order")
                .icon("fa-regular fa-clipboard")
                .title(thongBao.getTieuDe())
                .desc("Khách hàng: " + tenKhachHang)
                .actionText("Vui lòng xác nhận đơn hàng")
                .time(formatDateTime(thongBao.getThoiGianTao()))
                .isNew(!Boolean.TRUE.equals(thongBao.getDaDoc()))
                .trangThaiThongBao(thongBao.getTrangThaiThongBao())
                .lyDoTuChoi(thongBao.getLyDoTuChoi())
                .order(DoiTacThongBaoResponse.OrderInfo.builder()
                        .id(donHang.getMaDonHang())
                        .code("#DH" + String.format("%03d",
                                donHang.getMaDonHang()))
                        .date(formatDate(donHang.getNgayTaoDon()))
                        .status(donHang.getTrangThai() != null ? String.valueOf(donHang.getTrangThai()) : "")
                        .payment("Chuyển khoản")
                        .build())
                .customer(DoiTacThongBaoResponse.CustomerInfo.builder()
                        .id(donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getMaKhachHang()
                                : null)
                        .name(tenKhachHang)
                        .phone(donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getSoDienThoai()
                                : "")
                        .email(donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getEmail()
                                : "")
                        .address(donHang.getKhachHang() != null
                                ? donHang.getKhachHang().getDiaChi()
                                : "")
                        .build())
                .product(DoiTacThongBaoResponse.ProductInfo.builder()
                        .id(first != null && first.getSanPham() != null
                                ? first.getSanPham().getMaSanPham()
                                : null)
                        .name(first != null && first.getSanPham() != null
                                ? first.getSanPham().getTenSanPham()
                                : "Sản phẩm / dịch vụ")
                        .desc(first != null && first.getSanPham() != null
                                ? first.getSanPham().getLoai()
                                : "")
                        .quantity(tongSoLuong)
                        .price(tongTienCuaDoiTac)
                        .image(first != null && first.getSanPham() != null
                                ? first.getSanPham().getHinhAnh()
                                : null)
                        .build())
                .note(donHang.getGhiChu())
                .build();
    }

    private DoiTacDonHangResponse mapToDonHangResponse(
            DonHang donHang,
            Integer maDoiTac
    ) {
        List<ChiTietDonHang> chiTiets =
                chiTietDonHangRepository
                        .findByDonHang_MaDonHang(donHang.getMaDonHang())
                        .stream()
                        .filter(ct ->
                                ct.getSanPham() != null
                                        && ct.getSanPham().getMaDoiTac() != null
                                        && maDoiTac.equals(
                                        ct.getSanPham().getMaDoiTac()
                                )
                        )
                        .toList();

        List<DoiTacDonHangResponse.SanPhamTrongDonResponse> sanPhams =
                chiTiets.stream()
                        .map(ct -> {
                            int index = chiTiets.indexOf(ct) + 1;

                            BigDecimal donGia = ct.getGiaTien() == null
                                    ? BigDecimal.ZERO
                                    : ct.getGiaTien();

                            int soLuong = ct.getSoLuong() == null
                                    ? 0
                                    : ct.getSoLuong();

                            return DoiTacDonHangResponse
                                    .SanPhamTrongDonResponse
                                    .builder()
                                    .stt(index)
                                    .ten(ct.getSanPham().getTenSanPham())
                                    .soLuong(soLuong)
                                    .donGia(donGia)
                                    .thanhTien(donGia.multiply(
                                            BigDecimal.valueOf(soLuong)
                                    ))
                                    .build();
                        })
                        .toList();

        BigDecimal tongCong =
                sanPhams.stream()
                        .map(DoiTacDonHangResponse
                                .SanPhamTrongDonResponse::getThanhTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DoiTacDonHangResponse.builder()
                .maDonHang(donHang.getMaDonHang())
                .maCode("DH" + String.format("%03d",
                        donHang.getMaDonHang()))
                .tenKhachHang(donHang.getKhachHang() != null
                        ? donHang.getKhachHang().getTenKhachHang()
                        : "")
                .soDienThoai(donHang.getKhachHang() != null
                        ? donHang.getKhachHang().getSoDienThoai()
                        : "")
                .email(donHang.getKhachHang() != null
                        ? donHang.getKhachHang().getEmail()
                        : "")
                .diaChi(donHang.getKhachHang() != null
                        ? donHang.getKhachHang().getDiaChi()
                        : "")
                .ngayDat(formatDate(donHang.getNgayTaoDon()))
                .nhanVien(donHang.getNhanVien() != null
                        ? donHang.getNhanVien().getHoTen()
                        : "")
                .ghiChu(donHang.getGhiChu())
                .trangThai(donHang.getTrangThai() != null ? String.valueOf(donHang.getTrangThai()) : "")
                .tongCong(tongCong)
                .sanPhams(sanPhams)
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        return dateTime.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
        );
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";

        return date.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
    }

    private void capNhatTrangThaiDonHangKhiTatCaDoiTacChapNhan(DonHang donHang) {
        List<ThongBaoDoiTac> thongBaos =
                thongBaoRepository.findByDonHang_MaDonHangAndLoai(
                        donHang.getMaDonHang(),
                        LOAI_DON_HANG
                );

        if (thongBaos == null || thongBaos.isEmpty()) {
            return;
        }

        boolean tatCaDoiTacDaChapNhan = thongBaos.stream()
                .allMatch(tb -> DA_CHAP_NHAN.equals(tb.getTrangThaiThongBao()));

        if (!tatCaDoiTacDaChapNhan) {
            return;
        }

        if (DonHang.TT_CHO_DOI_TAC_XAC_NHAN.equals(donHang.getTrangThai())
                || DonHang.TT_MOI_TAO.equals(donHang.getTrangThai())) {

            donHang.setTrangThai(DonHang.TT_DA_XAC_NHAN);
            donHangRepository.save(donHang);
        }
    }

    /**
     * Map thông báo duyệt sản phẩm (Loai=1).
     * SanPham được look up qua maSanPham parse từ NoiDung [MASP:id].
     */
    private DoiTacThongBaoResponse mapToSanPhamThongBaoResponse(
            ThongBaoDoiTac thongBao
    ) {
        Integer maSanPham = parseMaSanPhamFromNoiDung(thongBao.getNoiDung());
        SanPham sanPham = null;

        if (maSanPham != null) {
            sanPham = sanPhamRepository.findById(maSanPham).orElse(null);
        }

        // Tương thích dữ liệu cũ: thông báo từ chối trước đây chưa gắn [MASP:id].
        // Khôi phục sản phẩm bằng tên nằm trong chuỗi: Sản phẩm "..." đã bị từ chối.
        if (sanPham == null && thongBao.getDoiTac() != null) {
            String tenSanPham = parseTenSanPhamFromNoiDung(thongBao.getNoiDung());

            if (tenSanPham != null) {
                sanPham = sanPhamRepository
                        .findFirstByMaDoiTacAndTenSanPhamIgnoreCaseOrderByMaSanPhamDesc(
                                thongBao.getDoiTac().getMaDoiTac(),
                                tenSanPham
                        )
                        .orElse(null);

                if (sanPham != null) {
                    maSanPham = sanPham.getMaSanPham();
                }
            }
        }

        boolean daDuyet = DA_CHAP_NHAN.equals(thongBao.getTrangThaiThongBao());
        boolean daTuChoi = DA_TU_CHOI.equals(thongBao.getTrangThaiThongBao());

        String actionText;
        String icon;

        if (daDuyet) {
            actionText = "Xem sản phẩm đã được duyệt";
            icon = "fa-solid fa-circle-check";
        } else if (daTuChoi) {
            actionText = "Xem lý do từ chối";
            icon = "fa-solid fa-circle-xmark";
        } else {
            actionText = "Xem thông báo sản phẩm";
            icon = "fa-regular fa-clock";
        }

        final SanPham sp = sanPham;
        return DoiTacThongBaoResponse.builder()
                .id(thongBao.getMaThongBao())
                .category("product")
                .type("product")
                .icon(icon)
                .title(thongBao.getTieuDe())
                .desc(cleanNoiDungSanPham(thongBao.getNoiDung()))
                .actionText(actionText)
                .time(formatDateTime(thongBao.getThoiGianTao()))
                .isNew(!Boolean.TRUE.equals(thongBao.getDaDoc()))
                .trangThaiThongBao(thongBao.getTrangThaiThongBao())
                .lyDoTuChoi(thongBao.getLyDoTuChoi())
                .order(null)
                .customer(null)
                .product(DoiTacThongBaoResponse.ProductInfo.builder()
                        .id(sp != null ? sp.getMaSanPham() : maSanPham)
                        .name(sp != null ? sp.getTenSanPham() : "Không tìm thấy sản phẩm")
                        .desc(sp != null ? sp.getLoai() : "")
                        .quantity(sp != null && sp.getSoLuong() != null ? sp.getSoLuong() : 0)
                        .price(sp != null && sp.getGiaTien() != null ? sp.getGiaTien() : BigDecimal.ZERO)
                        .image(sp != null ? sp.getHinhAnh() : null)
                        .build())
                .note(thongBao.getLyDoTuChoi())
                .build();
    }

    public void taoThongBaoTuChoiSanPham(SanPham sanPham, String lyDoTuChoi) {
        if (sanPham == null || sanPham.getMaDoiTac() == null) {
            return;
        }
        // Tìm đối tác sở hữu sản phẩm
        DoiTac doiTac = doiTacRepository.findById(sanPham.getMaDoiTac()).orElse(null);
        if (doiTac == null) {
            return;
        }
        String tieuDe = "Sản phẩm: " + sanPham.getTenSanPham() + " bị từ chối duyệt" ;
        // Bắt buộc phải có format [MASP:ID] ở cuối để hàm mapToSanPhamThongBaoResponse parse được ảnh/thông tin sản phẩm
        String noiDung = "Lý do: " + lyDoTuChoi + "[MASP:" + sanPham.getMaSanPham() +"]";
        ThongBaoDoiTac thongBao = ThongBaoDoiTac.builder()
                .doiTac(doiTac)
                .donHang(null) // Từ chối sản phẩm nên không có đơn hàng
                .loai(ThongBaoDoiTac.LOAI_DUYET_SAN_PHAM) // Loại = 1 (Duyệt sản phẩm)
                .tieuDe(tieuDe)
                .noiDung(noiDung)
                .trangThaiThongBao(DA_TU_CHOI) // Trạng thái thông báo chuyển thẳng sang Đã từ chối (2)
                .lyDoTuChoi(lyDoTuChoi)
                .daDoc(false)
                .thoiGianTao(LocalDateTime.now())
                .build();
        thongBaoRepository.save(thongBao);
    }
    public void taoThongBaoDuyetSanPham(SanPham sanPham) {
        if (sanPham == null || sanPham.getMaDoiTac() == null) {
            return;
        }
        // Tìm đối tác sở hữu sản phẩm
        DoiTac doiTac = doiTacRepository.findById(sanPham.getMaDoiTac()).orElse(null);
        if (doiTac == null) {
            return;
        }

        String tieuDe = "Sản phẩm: " + sanPham.getTenSanPham() + " đã được duyệt và đang bán hiện tại";
        // Bắt buộc phải có format [MASP:ID] ở cuối để hàm mapToSanPhamThongBaoResponse parse được ảnh/thông tin sản phẩm
        String noiDung = "[MASP:" + sanPham.getMaSanPham() +"]"+ "MASP: SP#" + sanPham.getMaSanPham() +  " Đã được đuyệt";
        ThongBaoDoiTac thongBao = ThongBaoDoiTac.builder()
                .doiTac(doiTac)
                .donHang(null) // Từ chối sản phẩm nên không có đơn hàng
                .loai(ThongBaoDoiTac.LOAI_DUYET_SAN_PHAM) // Loại = 1 (Duyệt sản phẩm)
                .tieuDe(tieuDe)
                .noiDung(noiDung)
                .trangThaiThongBao(DA_CHAP_NHAN)
                .daDoc(false)
                .thoiGianTao(LocalDateTime.now())
                .build();
        thongBaoRepository.save(thongBao);
    }

}