package vn.anyen.service;

import java.util.Map;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
import vn.anyen.dto.response.DonHangResponse.SanPhamDaKiemTra;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DonHang;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.KhachHang;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.SanPham;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DonHangRepository;
import vn.anyen.repository.KhachHangRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.SanPhamRepository;
import vn.anyen.repository.DoiTacRepository;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DonHangKhachHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanPhamRepository;

    /*
     * Dùng để kiểm tra đối tác của sản phẩm
     * đã mở Quỹ bảo đảm hay chưa.
     */
    private final DoiTacRepository doiTacRepository;

    private final DoiTacThongBaoService doiTacThongBaoService;

    /*
     * Tái sử dụng hàm map DonHang -> DonHangResponse
     * đang có trong DonHangService.
     */

    private final DonHangService donHangService;
    private final GuestLocationRedisService guestLocationRedisService;

    /*
     * Đơn khách tạo trên website sẽ được giao tạm
     * cho tài khoản nhân viên hệ thống này.
     *
     * application.properties:
     * app.customer-order.system-employee=website
     */
    @Value("${app.customer-order.system-employee:website}")
    private String systemEmployeeUsername;


    @Transactional
    public List<DonHangResponse> taoDonHangKhachHang(
            TaoDonHangRequest request
    ) {
        ThongTinKhachHangInput thongTinKhachHang =
                validateThongTinKhachHang(request);
        List<SanPhamDaKiemTra> sanPhams =
                validateVaLaySanPham(request);
        NhanVien nhanVienHeThong = nhanVienRepository
                .findByTenDangNhap(systemEmployeeUsername)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Chưa cấu hình nhân viên phụ trách đơn website. "
                                        + "Vui lòng tạo tài khoản nhân viên '"
                                        + systemEmployeeUsername
                                        + "'."
                        )
                );

        KhachHang khachHang = taoKhachHang(
                thongTinKhachHang,
                request,
                nhanVienHeThong
        );


        /*
         * =========================================
         * 5. NHÓM SẢN PHẨM THEO ĐỐI TÁC
         * =========================================
         *
         * Key:
         *      maDoiTac
         *
         * Value:
         *      danh sách sản phẩm của đối tác đó
         */
        Map<Integer, List<SanPhamDaKiemTra>> sanPhamTheoDoiTac =
                new LinkedHashMap<>();


        for (SanPhamDaKiemTra item : sanPhams) {

            Integer maDoiTac =
                    item.getSanPham().getMaDoiTac();


            /*
             * maDoiTac đã được validate ở
             * validateVaLaySanPham(),
             * nhưng check lại cho an toàn.
             */
            if (maDoiTac == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sản phẩm '"
                                + item.getSanPham().getTenSanPham()
                                + "' chưa có đối tác cung cấp."
                );
            }


            sanPhamTheoDoiTac
                    .computeIfAbsent(
                            maDoiTac,
                            key -> new ArrayList<>()
                    )
                    .add(item);
        }


        /*
         * 6. Danh sách đơn được tạo ra.
         */
        List<DonHangResponse> ketQua =
                new ArrayList<>();
        for (
                Map.Entry<Integer, List<SanPhamDaKiemTra>> entry
                : sanPhamTheoDoiTac.entrySet()
        ) {
            List<SanPhamDaKiemTra> sanPhamCuaDoiTac =
                    entry.getValue();
            DonHang donHang = DonHang.builder()
                    .khachHang(khachHang)
                    .nhanVien(nhanVienHeThong)
                    .ngayTaoDon(
                            LocalDate.now(
                                    ZoneId.of(
                                            "Asia/Ho_Chi_Minh"
                                    )
                            )
                    )

                    .tongTien(BigDecimal.ZERO)

                    .trangThai(
                            DonHang.TT_CHO_DOI_TAC_XAC_NHAN
                    )

                    .ghiChu(

                            request.getGhiChu()

                    )

                    .phuongThucThanhToan(
                            request.getPhuongThucThanhToan()
                                    != null

                                    ? request.getPhuongThucThanhToan()

                                    : DonHang.PT_CHUA_CHON
                    )

                    .trangThaiThanhToan(
                            request.getTrangThaiThanhToan()
                                    != null

                                    ? request.getTrangThaiThanhToan()

                                    : DonHang.TTTT_CHUA_THANH_TOAN
                    )

                    .build();


            DonHang donHangDaTao =
                    donHangRepository.save(donHang);


            /*
             * 7.2 Chỉ tạo chi tiết của
             * đối tác hiện tại.
             *
             * Ví dụ:
             *
             * Đối tác 1:
             * SP1
             * SP2
             *
             * => đơn này chỉ có SP1 + SP2
             */
            BigDecimal tongTien =
                    taoChiTietVaTruTonKho(
                            donHangDaTao,
                            sanPhamCuaDoiTac
                    );


            /*
             * 7.3 Tổng tiền RIÊNG của đơn
             */
            donHangDaTao.setTongTien(
                    tongTien
            );


            DonHang donHangDaLuu =
                    donHangRepository.save(
                            donHangDaTao
                    );


            /*
             * 7.4 Thông báo.
             *
             * Vì đơn hiện tại chỉ chứa sản phẩm
             * của 1 đối tác nên thông báo cũng
             * chỉ liên quan tới đối tác đó.
             */
            doiTacThongBaoService
                    .taoThongBaoChoDonHang(
                            donHangDaLuu.getMaDonHang()
                    );


            /*
             * 7.5 Map sang response
             */
            DonHangResponse response =
                    donHangService.getDonHangById(
                            donHangDaLuu.getMaDonHang()
                    );
            ketQua.add(response);
        }
        /*
         * 8. Trả về tất cả đơn vừa tạo
         */
        return ketQua;
    }

    private ThongTinKhachHangInput validateThongTinKhachHang(
            TaoDonHangRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu tạo đơn không hợp lệ"
            );
        }

        String tenKhachHang = trimToEmpty(request.getTenKhachHang());
        String soDienThoai = trimToEmpty(request.getSoDienThoai());
        String cccd = trimToEmpty(request.getCccd());
        String soNhaDuong = trimToEmpty(request.getSoNhaDuong());
        String phuongXa = trimToEmpty(request.getPhuongXa());
        String quanHuyen = trimToEmpty(request.getQuanHuyen());
        String tinhThanh = trimToEmpty(request.getTinhThanh());

        if (tenKhachHang.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên khách hàng không được để trống"
            );
        }

        if (tenKhachHang.length() > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên khách hàng tối đa 30 ký tự"
            );
        }

        if (soDienThoai.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số điện thoại không được để trống"
            );
        }

        if (!soDienThoai.matches(
                "^0(3|5|7|8|9)[0-9]{8}$"
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số điện thoại Việt Nam không hợp lệ"
            );
        }

        if (cccd.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CCCD không được để trống"
            );
        }

        if (!cccd.matches("^[0-9]{12}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CCCD phải gồm đúng 12 chữ số"
            );
        }

        if (soNhaDuong.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số nhà, tên đường không được để trống"
            );
        }

        if (soNhaDuong.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số nhà, tên đường tối đa 255 ký tự"
            );
        }

        validateThanhPhanDiaChi(phuongXa, "Phường/xã");
        validateThanhPhanDiaChi(quanHuyen, "Quận/huyện");
        validateThanhPhanDiaChi(tinhThanh, "Tỉnh/thành phố");

        return new ThongTinKhachHangInput(
                tenKhachHang,
                soDienThoai,
                cccd,
                soNhaDuong,
                phuongXa,
                quanHuyen,
                tinhThanh
        );
    }

    private void validateThanhPhanDiaChi(String value, String tenTruong) {
        if (value.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    tenTruong + " không được để trống"
            );
        }

        if (value.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    tenTruong + " tối đa 100 ký tự"
            );
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private List<SanPhamDaKiemTra> validateVaLaySanPham(
            TaoDonHangRequest request
    ) {

        /*
         * 1. Đơn phải có ít nhất một sản phẩm.
         */
        if (
                request.getItems() == null
                        || request.getItems().isEmpty()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn ít nhất 1 sản phẩm"
            );
        }


        List<SanPhamDaKiemTra> result =
                new ArrayList<>();


        /*
         * Dùng để tránh một sản phẩm
         * xuất hiện hai lần trong payload.
         */
        Set<Integer> maSanPhamDaCo =
                new HashSet<>();


        for (
                TaoDonHangRequest.SanPhamTrongDonRequest item
                : request.getItems()
        ) {

            /*
             * 2. Kiểm tra mã sản phẩm.
             */
            if (
                    item == null
                            || item.getMaSanPham() == null
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Mã sản phẩm không hợp lệ"
                );
            }


            /*
             * 3. Không cho trùng sản phẩm.
             */
            if (
                    !maSanPhamDaCo.add(
                            item.getMaSanPham()
                    )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm #"
                                + item.getMaSanPham()
                                + " bị trùng trong đơn hàng"
                );
            }


            /*
             * 4. Kiểm tra số lượng.
             */
            Integer soLuongDat =
                    item.getSoLuong();


            if (
                    soLuongDat == null
                            || soLuongDat <= 0
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Số lượng sản phẩm phải lớn hơn 0"
                );
            }


            /*
             * 5. Lấy sản phẩm từ database.
             *
             * Không tin dữ liệu frontend.
             */
            SanPham sanPham =
                    sanPhamRepository
                            .findById(
                                    item.getMaSanPham()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,

                                            "Không tìm thấy sản phẩm #"
                                                    + item.getMaSanPham()
                                    )
                            );


            /*
             * 6. Quan tài không được khách tự mua.
             *
             * Quan tài vẫn giữ luồng:
             *
             * Liên hệ
             * -> tư vấn
             * -> nhân viên tạo đơn
             * -> hợp đồng.
             */
            if (
                    laSanPhamQuanTai(
                            sanPham
                    )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' thuộc loại Quan tài. "
                                + "Vui lòng liên hệ nhân viên An Yên "
                                + "để được tư vấn và tạo hợp đồng."
                );
            }


            /*
             * =====================================
             * 7. KIỂM TRA QUỸ CỦA ĐỐI TÁC
             * =====================================
             *
             * Sản phẩm khách tự mua trên website
             * bắt buộc phải thuộc đối tác đã mở Quỹ.
             *
             * Chưa mở Quỹ:
             * Website chỉ được hiển thị "Liên hệ".
             */
            if (
                    sanPham.getMaDoiTac() == null
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' chưa có đối tác cung cấp."
                );
            }


            DoiTac doiTac =
                    doiTacRepository
                            .findById(
                                    sanPham.getMaDoiTac()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.BAD_REQUEST,

                                            "Không tìm thấy đối tác cung cấp "
                                                    + "sản phẩm '"
                                                    + sanPham.getTenSanPham()
                                                    + "'."
                                    )
                            );


            /*
             * Đối tác chưa mở Quỹ:
             *
             * Không cho khách lách frontend
             * bằng cách tự gọi API/Postman.
             */
            if (
                    !Boolean.TRUE.equals(
                            doiTac.getDaMoQuy()
                    )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' hiện chưa hỗ trợ mua trực tiếp. "
                                + "Đối tác chưa mở Quỹ bảo đảm. "
                                + "Vui lòng liên hệ để được tư vấn."
                );
            }


            /*
             * 8. Chỉ lấy sản phẩm đang bán.
             *
             * Theo database hiện tại:
             *
             * 0 = Ẩn
             * 1 = Đang bán
             * 2 = Chờ xác nhận
             */
            if (
                    sanPham.getTrangThai() == null
                            || sanPham.getTrangThai() != 1
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' hiện không được phép đặt hàng."
                );
            }


            /*
             * 9. Kiểm tra tồn kho.
             */
            Integer tonKho =
                    sanPham.getSoLuong() == null
                            ? 0
                            : sanPham.getSoLuong();


            if (
                    tonKho < soLuongDat
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' không đủ tồn kho. Còn: "
                                + tonKho
                );
            }


            /*
             * 10. Giá luôn lấy từ database.
             */
            BigDecimal donGia =
                    sanPham.getGiaTien() == null
                            ? BigDecimal.ZERO
                            : sanPham.getGiaTien();


            if (
                    donGia.compareTo(
                            BigDecimal.ZERO
                    ) <= 0
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,

                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' chưa có giá bán hợp lệ."
                );
            }


            /*
             * Đưa sản phẩm đã validate
             * sang bước tạo chi tiết đơn.
             */
            result.add(
                    new SanPhamDaKiemTra(
                            sanPham,
                            soLuongDat,
                            donGia
                    )
            );
        }


        return result;
    }
    private KhachHang taoKhachHang(
            ThongTinKhachHangInput thongTin,
            TaoDonHangRequest request,
            NhanVien nhanVien
    ) {
        BigDecimal latitude = null;
        BigDecimal longitude = null;
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            GuestLocationRedisService.GuestLocation loc =
                    guestLocationRedisService.getLocation(request.getSessionId());
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }
        }

        KhachHang khachHang = KhachHang.builder()
                .tenKhachHang(
                        thongTin.getTenKhachHang()
                )
                .soDienThoai(
                        thongTin.getSoDienThoai()
                )
                .cccd(thongTin.getCccd())
                .email(trimToEmpty(request.getEmail()))
                .soNhaDuong(thongTin.getSoNhaDuong())
                .phuongXa(thongTin.getPhuongXa())
                .quanHuyen(thongTin.getQuanHuyen())
                .tinhThanh(thongTin.getTinhThanh())
                .diaChi(thongTin.getSoNhaDuong() + ", "
                        + thongTin.getPhuongXa() + ", "
                        + thongTin.getQuanHuyen() + ", "
                        + thongTin.getTinhThanh())
                .maNhanVienPhuTrach(
                        nhanVien.getMaNhanVien()
                )
                .ngayDangKy(LocalDateTime.now())
                .nguonDangKy(
                        "Khách hàng tự tạo đơn từ website"
                )
                .latitude(latitude)
                .longitude(longitude)
                .build();

        return khachHangRepository.save(khachHang);
    }

    private BigDecimal taoChiTietVaTruTonKho(
            DonHang donHang,
            List<SanPhamDaKiemTra> sanPhams
    ) {
        BigDecimal tongTien =
                BigDecimal.ZERO;

        for (SanPhamDaKiemTra item : sanPhams) {
            SanPham sanPham = item.getSanPham();

            /*
             * Kiểm tra tồn kho lại ngay trước khi trừ.
             */
            Integer tonKho =
                    sanPham.getSoLuong() == null
                            ? 0
                            : sanPham.getSoLuong();

            if (tonKho < item.getSoLuong()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' không đủ tồn kho. Còn: "
                                + tonKho
                );
            }

            BigDecimal thanhTien =
                    item.getDonGia().multiply(
                            BigDecimal.valueOf(item.getSoLuong())
                    );

            tongTien =
                    tongTien.add(thanhTien);

            ChiTietDonHang chiTiet =
                    ChiTietDonHang.builder()
                            .donHang(donHang)
                            .sanPham(sanPham)
                            .soLuong(item.getSoLuong())
                            .giaTien(item.getDonGia())
                            .trangThaiDoiTac(0)
                            .build();

            chiTietDonHangRepository.save(chiTiet);

            sanPham.setSoLuong(
                    tonKho - item.getSoLuong()
            );

            sanPhamRepository.save(sanPham);
        }

        return tongTien;
    }

    private boolean laSanPhamQuanTai(
            SanPham sanPham
    ) {
        String loai =

                sanPham.getLoai()
                ;

        String tenSanPham =
                sanPham.getTenSanPham()
                ;

        return loai.contains("quan tài")
                || tenSanPham.contains("quan tài")|| loai.contains("quan tai")||loai.contains("dịch vụ")
                || tenSanPham.contains("dịch vụ")||loai.contains("dich vu");
    }

    private static class ThongTinKhachHangInput {
        private final String tenKhachHang;
        private final String soDienThoai;
        private final String cccd;
        private final String soNhaDuong;
        private final String phuongXa;
        private final String quanHuyen;
        private final String tinhThanh;

        private ThongTinKhachHangInput(
                String tenKhachHang,
                String soDienThoai,
                String cccd,
                String soNhaDuong,
                String phuongXa,
                String quanHuyen,
                String tinhThanh
        ) {
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.cccd = cccd;
            this.soNhaDuong = soNhaDuong;
            this.phuongXa = phuongXa;
            this.quanHuyen = quanHuyen;
            this.tinhThanh = tinhThanh;
        }

        private String getTenKhachHang() { return tenKhachHang; }
        private String getSoDienThoai() { return soDienThoai; }
        private String getCccd() { return cccd; }
        private String getSoNhaDuong() { return soNhaDuong; }
        private String getPhuongXa() { return phuongXa; }
        private String getQuanHuyen() { return quanHuyen; }
        private String getTinhThanh() { return tinhThanh; }
    }




}