package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.TuChoiThongBaoRequest;
import vn.anyen.dto.response.DoiTacDonHangPageResponse;
import vn.anyen.dto.response.DoiTacDonHangResponse;
import vn.anyen.dto.response.DoiTacThongBaoResponse;
import vn.anyen.dto.response.XuLyThongBaoResponse;
import vn.anyen.entity.*;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.ThongBaoDoiTacRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import vn.anyen.dto.request.CapNhatTrangThaiDonHangRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class DoiTacThongBaoService {

    private final ThongBaoDoiTacRepository thongBaoRepository;
    private final DoiTacRepository doiTacRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private static final String LOAI_DUYET_SAN_PHAM = "DUYET_SAN_PHAM";

    private static final String LOAI_DON_HANG = "DON_HANG";
    private static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    private static final String DA_CHAP_NHAN = "DA_CHAP_NHAN";
    private static final String DA_TU_CHOI = "DA_TU_CHOI";

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
        }

        return XuLyThongBaoResponse.builder()
                .success(true)
                .message("Đã chấp nhận đơn hàng")
                .redirectUrl("/doi-tac/quan-ly-don-hang")
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
            donHang.setTrangThai("Đối tác đã từ chối");

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

        if (request.getTrangThai() == null
                || request.getTrangThai().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái không được để trống"
            );
        }

        DonHang donHang = thongBao.getDonHang();

        donHang.setTrangThai(request.getTrangThai().trim());

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
                        .status(donHang.getTrangThai())
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
                .trangThai(donHang.getTrangThai())
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

        if ("Chờ đối tác xác nhận".equals(donHang.getTrangThai())
                || "Mới tạo".equals(donHang.getTrangThai())
                || "Đối tác đã chấp nhận".equals(donHang.getTrangThai())) {

            donHang.setTrangThai("Đã xác nhận");
            donHangRepository.save(donHang);
        }
    }
    private DoiTacThongBaoResponse mapToSanPhamThongBaoResponse(
            ThongBaoDoiTac thongBao
    ) {
        SanPham sanPham = thongBao.getSanPham();

        boolean daDuyet = DA_CHAP_NHAN.equals(thongBao.getTrangThaiThongBao());
        boolean daTuChoi = DA_TU_CHOI.equals(thongBao.getTrangThaiThongBao());

        String actionText;
        if (daDuyet) {
            actionText = "Sản phẩm đã được duyệt và đang bán";
        } else if (daTuChoi) {
            actionText = "Sản phẩm đã bị từ chối";
        } else {
            actionText = "Thông báo sản phẩm";
        }

        return DoiTacThongBaoResponse.builder()
                .id(thongBao.getMaThongBao())
                .category("product")
                .type("product")
                .icon(daDuyet ? "fa-solid fa-circle-check" : "fa-solid fa-circle-xmark")
                .title(thongBao.getTieuDe())
                .desc(thongBao.getNoiDung())
                .actionText(actionText)
                .time(formatDateTime(thongBao.getThoiGianTao()))
                .isNew(!Boolean.TRUE.equals(thongBao.getDaDoc()))
                .trangThaiThongBao(thongBao.getTrangThaiThongBao())
                .lyDoTuChoi(thongBao.getLyDoTuChoi())
                .order(null)
                .customer(null)
                .product(DoiTacThongBaoResponse.ProductInfo.builder()
                        .id(sanPham != null ? sanPham.getMaSanPham() : null)
                        .name(sanPham != null ? sanPham.getTenSanPham() : "Sản phẩm đã bị xóa")
                        .desc(sanPham != null ? sanPham.getLoai() : "")
                        .quantity(sanPham != null && sanPham.getSoLuong() != null ? sanPham.getSoLuong() : 0)
                        .price(sanPham != null && sanPham.getGiaTien() != null ? sanPham.getGiaTien() : BigDecimal.ZERO)
                        .image(sanPham != null ? sanPham.getHinhAnh() : null)
                        .build())
                .note(thongBao.getLyDoTuChoi())
                .build();
    }
}