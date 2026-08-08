package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.TaoDonHangRequest;
import vn.anyen.dto.response.DonHangResponse;
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
    public DonHangResponse taoDonHangKhachHang(
            TaoDonHangRequest request
    ) {
        /*
         * 1. Kiểm tra thông tin khách hàng
         */
        ThongTinKhachHang thongTinKhachHang =
                validateThongTinKhachHang(request);

        /*
         * 2. Kiểm tra sản phẩm, tồn kho và quan tài.
         *
         * Giá sản phẩm được lấy từ database,
         * không lấy giá frontend gửi lên.
         */
        List<SanPhamDaKiemTra> sanPhams =
                validateVaLaySanPham(request);

        /*
         * 3. Tìm nhân viên hệ thống phụ trách
         * đơn khách tự tạo.
         */
        NhanVien nhanVienHeThong = nhanVienRepository
                .findByTenDangNhap(systemEmployeeUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Chưa cấu hình nhân viên phụ trách đơn website. "
                                + "Vui lòng tạo tài khoản nhân viên '"
                                + systemEmployeeUsername + "'."
                ));

        /*
         * 4. Tạo khách hàng
         */
        KhachHang khachHang = taoKhachHang(
                thongTinKhachHang,
                request,
                nhanVienHeThong
        );

        /*
         * 5. Tạo đơn hàng.
         *
         * Không gọi HopDongRepository.
         * Không tạo entity HopDong.
         */
        DonHang donHang = DonHang.builder()
                .khachHang(khachHang)
                .nhanVien(nhanVienHeThong)
                .ngayTaoDon(
                        LocalDate.now(
                                ZoneId.of("Asia/Ho_Chi_Minh")
                        )
                )
                .tongTien(BigDecimal.ZERO)
                .trangThai(
                        DonHang.TT_CHO_DOI_TAC_XAC_NHAN
                )
                .ghiChu(chuanHoaChuoi(request.getGhiChu()))
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

        DonHang donHangDaTao =
                donHangRepository.save(donHang);

        /*
         * 6. Tạo chi tiết và trừ tồn kho
         */
        BigDecimal tongTien =
                taoChiTietVaTruTonKho(
                        donHangDaTao,
                        sanPhams
                );

        donHangDaTao.setTongTien(tongTien);

        DonHang donHangDaLuu =
                donHangRepository.save(donHangDaTao);

        /*
         * 7. Thông báo cho các đối tác có sản phẩm
         * trong đơn.
         */
        doiTacThongBaoService.taoThongBaoChoDonHang(
                donHangDaLuu.getMaDonHang()
        );

        /*
         * DonHangService đã có hàm public
         * getDonHangById để map response.
         */
        return donHangService.getDonHangById(
                donHangDaLuu.getMaDonHang()
        );
    }

    private ThongTinKhachHang validateThongTinKhachHang(
            TaoDonHangRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu tạo đơn không hợp lệ"
            );
        }

        String tenKhachHang =
                chuanHoaChuoi(request.getTenKhachHang());

        String soDienThoai =
                chuanHoaSoDienThoai(
                        request.getSoDienThoai()
                );

        String cccd =
                chiLaySo(request.getCccd());

        String diaChi =
                chuanHoaChuoi(request.getDiaChi());

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

        if (diaChi.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Địa chỉ không được để trống"
            );
        }

        if (diaChi.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Địa chỉ tối đa 255 ký tự"
            );
        }

        return new ThongTinKhachHang(
                tenKhachHang,
                soDienThoai,
                cccd,
                diaChi
        );
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
            ThongTinKhachHang thongTin,
            TaoDonHangRequest request,
            NhanVien nhanVien
    ) {
        KhachHang khachHang = KhachHang.builder()
                .tenKhachHang(
                        thongTin.tenKhachHang
                )
                .soDienThoai(
                        thongTin.soDienThoai
                )
                .cccd(thongTin.cccd)
                .email(chuanHoaChuoi(request.getEmail()))
                .diaChi(thongTin.diaChi)
                .maNhanVienPhuTrach(
                        nhanVien.getMaNhanVien()
                )
                .ngayDangKy(LocalDateTime.now())
                .nguonDangKy(
                        "Khách hàng tự tạo đơn từ website"
                )
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
            SanPham sanPham = item.sanPham;

            /*
             * Kiểm tra tồn kho lại ngay trước khi trừ.
             */
            Integer tonKho =
                    sanPham.getSoLuong() == null
                            ? 0
                            : sanPham.getSoLuong();

            if (tonKho < item.soLuong) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sản phẩm '"
                                + sanPham.getTenSanPham()
                                + "' không đủ tồn kho. Còn: "
                                + tonKho
                );
            }

            BigDecimal thanhTien =
                    item.donGia.multiply(
                            BigDecimal.valueOf(item.soLuong)
                    );

            tongTien =
                    tongTien.add(thanhTien);

            ChiTietDonHang chiTiet =
                    ChiTietDonHang.builder()
                            .donHang(donHang)
                            .sanPham(sanPham)
                            .soLuong(item.soLuong)
                            .giaTien(item.donGia)
                            .trangThaiDoiTac(0)
                            .build();

            chiTietDonHangRepository.save(chiTiet);

            sanPham.setSoLuong(
                    tonKho - item.soLuong
            );

            sanPhamRepository.save(sanPham);
        }

        return tongTien;
    }

    private boolean laSanPhamQuanTai(
            SanPham sanPham
    ) {
        String loai =
                chuanHoaTiengViet(
                        sanPham.getLoai()
                );

        String tenSanPham =
                chuanHoaTiengViet(
                        sanPham.getTenSanPham()
                );

        return loai.contains("quan tai")
                || tenSanPham.contains("quan tai");
    }

    private String chuanHoaTiengViet(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String chuanHoaSoDienThoai(
            String value
    ) {
        String phone =
                value == null
                        ? ""
                        : value.trim()
                        .replaceAll("\\s+", "");

        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        }

        phone = phone.replaceAll("\\D", "");

        if (
                phone.startsWith("84")
                        && phone.length() == 11
        ) {
            phone = "0" + phone.substring(2);
        }

        return phone;
    }

    private String chiLaySo(
            String value
    ) {
        return value == null
                ? ""
                : value.replaceAll("\\D", "");
    }

    private String chuanHoaChuoi(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }

    private static class ThongTinKhachHang {

        private final String tenKhachHang;
        private final String soDienThoai;
        private final String cccd;
        private final String diaChi;

        private ThongTinKhachHang(
                String tenKhachHang,
                String soDienThoai,
                String cccd,
                String diaChi
        ) {
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.cccd = cccd;
            this.diaChi = diaChi;
        }
    }

    private static class SanPhamDaKiemTra {

        private final SanPham sanPham;
        private final Integer soLuong;
        private final BigDecimal donGia;

        private SanPhamDaKiemTra(
                SanPham sanPham,
                Integer soLuong,
                BigDecimal donGia
        ) {
            this.sanPham = sanPham;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }
    }
}