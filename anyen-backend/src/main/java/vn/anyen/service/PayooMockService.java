package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.response.PayooMockResponse;
import vn.anyen.entity.*;
import vn.anyen.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayooMockService {

    private final PayooMockTransactionRepository
            payooRepository;

    private final DoiTacRepository
            doiTacRepository;

    private final CongNoRepository
            congNoRepository;

    private final LichSuCongNoRepository
            lichSuCongNoRepository;

    private final DonHangRepository
            donHangRepository;

    private final DonHangService
            donHangService;

    private final HoaDonRepository
            hoaDonRepository;

    private final LichSuGiaoDichDoiTacRepository
            lichSuGiaoDichDoiTacRepository;


    // =================================================
    // NẠP QUỸ
    // =================================================

    @Transactional
    public PayooMockResponse taoNapQuy(
            Integer maDoiTac,
            BigDecimal soTien
    ) {

        DoiTac doiTac =
                getDoiTac(maDoiTac);


        if (
                !Boolean.TRUE.equals(
                        doiTac.getDaMoQuy()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng mở Quỹ trước khi nạp tiền"
            );
        }


        PayooMockTransaction tx =
                taoGiaoDich(
                        PayooMockTransaction
                                .LOAI_NAP_QUY,

                        maDoiTac,

                        null,

                        soTien,

                        "Nạp Quỹ bảo đảm qua Payoo Mock"
                );


        return map(tx);
    }


    // =================================================
    // RÚT QUỸ
    // =================================================

    @Transactional
    public PayooMockResponse taoRutQuy(
            Integer maDoiTac,
            BigDecimal soTien
    ) {

        DoiTac doiTac =
                getDoiTac(maDoiTac);


        BigDecimal soDuQuy =
                zero(
                        doiTac.getSoDuQuy()
                );


        BigDecimal dangKhoa =
                zero(
                        doiTac
                                .getSoDuQuyDangKhoa()
                );


        BigDecimal khaDung =
                soDuQuy.subtract(
                        dangKhoa
                );


        if (
                khaDung.compareTo(
                        soTien
                ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,

                    "Số dư Quỹ khả dụng không đủ. "
                            + "Bạn chỉ có thể rút tối đa "
                            + khaDung.toPlainString()
                            + "đ"
            );
        }


        PayooMockTransaction tx =
                taoGiaoDich(
                        PayooMockTransaction
                                .LOAI_RUT_QUY,

                        maDoiTac,

                        null,

                        soTien,

                        "Rút Quỹ bảo đảm qua Payoo Mock"
                );


        return map(tx);
    }


    // =================================================
    // RÚT VÍ
    // =================================================

    @Transactional
    public PayooMockResponse taoRutVi(
            Integer maDoiTac,
            BigDecimal soTien
    ) {

        DoiTac doiTac =
                getDoiTac(maDoiTac);


        if (
                zero(
                        doiTac.getSoDuVi()
                )
                        .compareTo(
                                soTien
                        ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số dư Ví không đủ"
            );
        }


        PayooMockTransaction tx =
                taoGiaoDich(
                        PayooMockTransaction
                                .LOAI_RUT_VI,

                        maDoiTac,

                        null,

                        soTien,

                        "Rút tiền từ Ví qua Payoo Mock"
                );


        return map(tx);
    }


    // =================================================
    // THANH TOÁN CÔNG NỢ
    // =================================================

    @Transactional
    public PayooMockResponse taoThanhToanCongNo(
            Integer maCongNo,
            BigDecimal soTien
    ) {

        // Kiểm tra số tiền >= mức tối thiểu
        validateSoTien(soTien);


        CongNo congNo =
                congNoRepository
                        .findById(maCongNo)
                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy công nợ"
                                )
                        );


        BigDecimal conLai =
                zero(
                        congNo.getConLai()
                );


        // ================================
        // ĐÃ THANH TOÁN ĐỦ
        // ================================

        if (
                conLai.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Công nợ đã được thanh toán đủ"
            );
        }


        // ================================
        // KHÔNG CHO TRẢ VƯỢT
        // ================================

        if (
                conLai.compareTo(
                        soTien
                ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền thanh toán vượt công nợ còn lại"
            );
        }


        // =====================================
        // KIỂM TRA SỐ TIỀN CÒN LẠI
        // =====================================

        BigDecimal conLaiSauThanhToan =
                conLai.subtract(soTien);


        /*
         * Ví dụ:
         *
         * Công nợ = 100.000
         * Thanh toán = 99.500
         * Còn 500
         *
         * Lần sau Payoo yêu cầu tối thiểu 1000
         * => không thể trả tiếp.
         *
         * Nên không cho trường hợp này.
         */
        if (
                conLaiSauThanhToan
                        .compareTo(
                                BigDecimal.ZERO
                        ) > 0

                        &&

                        conLaiSauThanhToan
                                .compareTo(
                                        new BigDecimal("1000")
                                ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng thanh toán toàn bộ hoặc để số dư còn lại tối thiểu 1.000đ"
            );
        }


        // =====================================
        // TẠO GIAO DỊCH PAYOO
        // =====================================

        PayooMockTransaction tx =
                taoGiaoDich(

                        PayooMockTransaction
                                .LOAI_THANH_TOAN_CONG_NO,

                        congNo
                                .getDoiTac()
                                .getMaDoiTac(),

                        maCongNo,

                        soTien,

                        "Thanh toán công nợ #"
                                + maCongNo
                                + " qua Payoo Mock"
                );


        /*
         * Chú ý:
         *
         * CHƯA trừ công nợ ở đây.
         *
         * Phải đợi Payoo callback thành công
         * mới gọi xuLyCongNo().
         */
        return map(tx);
    }


    // =================================================
    // THANH TOÁN ĐƠN HÀNG
    // =================================================

    @Transactional
    public PayooMockResponse taoThanhToanDonHang(
            Integer maDonHang,
            BigDecimal soTien
    ) {

        validateSoTien(soTien);

        DonHang donHang =
                donHangRepository
                        .findById(maDonHang)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy đơn hàng #" + maDonHang
                                )
                        );

        if (
                DonHang.TT_HOAN_THANH
                        .equals(
                                donHang.getTrangThai()
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đơn hàng đã hoàn thành trước đó"
            );
        }

        PayooMockTransaction tx =
                taoGiaoDich(
                        PayooMockTransaction
                                .LOAI_THANH_TOAN_DON_HANG,

                        null,

                        null,

                        maDonHang,

                        soTien,

                        "Thanh toán đơn hàng #"
                                + (donHang.getMaDonHang())
                                + " qua Payoo Mock"
                );

        return map(tx);
    }


    // =================================================
    // XEM GIAO DỊCH
    // =================================================

    @Transactional(readOnly = true)
    public PayooMockResponse getById(
            String maGiaoDich
    ) {

        return map(
                getTransaction(
                        maGiaoDich
                )
        );
    }


    // =================================================
    // PAYOO CALLBACK GIẢ LẬP
    // =================================================

    /*
     * Khi:
     *
     * - click QR
     * hoặc
     * - điện thoại scan QR
     *
     * frontend gọi endpoint này.
     *
     * Nó đóng vai trò callback từ Payoo.
     */
    @Transactional
    public PayooMockResponse xacNhanThanhCong(
            String maGiaoDich
    ) {

        try {
            System.out.println("=== XÁC NHẬN THANH CÔNG START ===");
            System.out.println("MaGiaoDich: " + maGiaoDich);

            PayooMockTransaction tx =
                    getTransaction(
                            maGiaoDich
                    );

            System.out.println("LoaiGiaoDich: " + tx.getLoaiGiaoDich());
            System.out.println("TrangThai: " + tx.getTrangThai());


            /*
             * Chống xử lý 2 lần.
             */
            if (
                    PayooMockTransaction
                            .TT_THANH_CONG
                            .equals(
                                    tx.getTrangThai()
                            )
            ) {

                System.out.println("Giao dịch đã thành công trước đó");
                return map(tx);
            }


            if (
                    PayooMockTransaction
                            .TT_THAT_BAI
                            .equals(
                                    tx.getTrangThai()
                            )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Giao dịch đã thất bại"
                );
            }


            tx.setTrangThai(
                    PayooMockTransaction
                            .TT_DANG_XU_LY
            );


            payooRepository.save(tx);


            /*
             * Xử lý nghiệp vụ tùy loại.
             */
            switch (
                    tx.getLoaiGiaoDich()
            ) {

                case PayooMockTransaction
                             .LOAI_NAP_QUY
                        -> xuLyNapQuy(tx);


                case PayooMockTransaction
                             .LOAI_RUT_QUY
                        -> xuLyRutQuy(tx);


                case PayooMockTransaction
                             .LOAI_RUT_VI
                        -> xuLyRutVi(tx);


                case PayooMockTransaction
                             .LOAI_THANH_TOAN_CONG_NO
                        -> xuLyCongNo(tx);


                case PayooMockTransaction
                             .LOAI_THANH_TOAN_DON_HANG
                        -> xuLyDonHang(tx);


                default ->
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Loại giao dịch Payoo không hợp lệ"
                        );
            }


            tx.setTrangThai(
                    PayooMockTransaction
                            .TT_THANH_CONG
            );


            tx.setCompletedAt(
                    LocalDateTime.now()
            );


            payooRepository.save(tx);

            System.out.println("=== XÁC NHẬN THANH CÔNG END ===");
            System.out.println("Transaction đã hoàn tất và sẽ commit");


            return map(tx);

        } catch (Exception e) {
            System.out.println("=== XÁC NHẬN THANH CÔNG LỖI ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    // =================================================
    // CALLBACK NẠP QUỸ
    // =================================================

    private void xuLyNapQuy(
            PayooMockTransaction tx
    ) {

        DoiTac doiTac =
                getDoiTac(
                        tx.getMaDoiTac()
                );


        if (
                !Boolean.TRUE.equals(
                        doiTac.getDaMoQuy()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quỹ chưa được mở"
            );
        }


        doiTac.setSoDuQuy(

                zero(
                        doiTac.getSoDuQuy()
                )
                        .add(
                                tx.getSoTien()
                        )
        );


        doiTacRepository.save(
                doiTac
        );


        ghiNhanLichSuGiaoDich(
                doiTac,
                "QUY",
                "+",
                tx.getSoTien(),
                "Nạp Quỹ qua Payoo"
        );
    }


    // =================================================
    // CALLBACK RÚT QUỸ
    // =================================================

    private void xuLyRutQuy(
            PayooMockTransaction tx
    ) {

        try {
            DoiTac doiTac =
                    getDoiTac(
                            tx.getMaDoiTac()
                    );


            BigDecimal soDuQuy =
                    zero(
                            doiTac.getSoDuQuy()
                    );


            BigDecimal dangKhoa =
                    zero(
                            doiTac
                                    .getSoDuQuyDangKhoa()
                    );


            BigDecimal khaDung =
                    soDuQuy.subtract(
                            dangKhoa
                    );


            /*
             * Kiểm tra lại lúc Payoo callback.
             */
            if (
                    khaDung.compareTo(
                            tx.getSoTien()
                    ) < 0
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quỹ khả dụng không còn đủ để rút"
                );
            }


            System.out.println("=== RÚT QUỸ DEBUG ===");
            System.out.println("Trước khi rút - SoDuQuy: " + doiTac.getSoDuQuy());
            System.out.println("Trước khi rút - SoDuVi: " + doiTac.getSoDuVi());
            System.out.println("Số tiền rút: " + tx.getSoTien());


            // Rút từ Quỹ: trừ Quỹ
            doiTac.setSoDuQuy(
                    soDuQuy.subtract(
                            tx.getSoTien()
                    )
            );


            // Chuyển vào Ví: cộng Ví
            doiTac.setSoDuVi(
                    zero(doiTac.getSoDuVi()).add(
                            tx.getSoTien()
                    )
            );


            System.out.println("Sau khi rút - SoDuQuy: " + doiTac.getSoDuQuy());
            System.out.println("Sau khi rút - SoDuVi: " + doiTac.getSoDuVi());


            doiTacRepository.save(
                    doiTac
            );

            doiTacRepository.flush();

            System.out.println("Đã flush doiTac");


            // Ghi nhận lịch sử: trừ Quỹ
            ghiNhanLichSuGiaoDich(
                    doiTac,
                    "QUY",
                    "-",
                    tx.getSoTien(),
                    "Rút từ Quỹ vào Ví"
            );

            // Ghi nhận lịch sử: cộng Ví
            ghiNhanLichSuGiaoDich(
                    doiTac,
                    "VI",
                    "+",
                    tx.getSoTien(),
                    "Nhận từ Quỹ"
            );

            System.out.println("=== RÚT QUỸ THÀNH CÔNG ===");

        } catch (Exception e) {
            System.out.println("=== RÚT QUỸ LỖI ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    // =================================================
    // CALLBACK RÚT VÍ
    // =================================================

    private void xuLyRutVi(
            PayooMockTransaction tx
    ) {

        try {
            DoiTac doiTac =
                    getDoiTac(
                            tx.getMaDoiTac()
                    );


            BigDecimal soDuVi =
                    zero(
                            doiTac.getSoDuVi()
                    );


            if (
                    soDuVi.compareTo(
                            tx.getSoTien()
                    ) < 0
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Số dư Ví không còn đủ để rút"
                );
            }


            System.out.println("=== RÚT VÍ DEBUG ===");
            System.out.println("Trước khi rút - SoDuVi: " + doiTac.getSoDuVi());
            System.out.println("Số tiền rút: " + tx.getSoTien());


            doiTac.setSoDuVi(
                    soDuVi.subtract(
                            tx.getSoTien()
                    )
            );


            System.out.println("Sau khi rút - SoDuVi: " + doiTac.getSoDuVi());


            doiTacRepository.save(
                    doiTac
            );

            doiTacRepository.flush();

            System.out.println("Đã flush doiTac");


            ghiNhanLichSuGiaoDich(
                    doiTac,
                    "VI",
                    "-",
                    tx.getSoTien(),
                    "Rút Ví qua Payoo"
            );

            System.out.println("=== RÚT VÍ THÀNH CÔNG ===");

        } catch (Exception e) {
            System.out.println("=== RÚT VÍ LỖI ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    // =================================================
    // CALLBACK CÔNG NỢ
    // =================================================

    private void xuLyCongNo(
            PayooMockTransaction tx
    ) {

        CongNo congNo =
                congNoRepository
                        .findById(
                                tx.getMaCongNo()
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy công nợ"
                                )
                        );


        BigDecimal conLai =
                zero(
                        congNo.getConLai()
                );


        if (
                conLai.compareTo(
                        tx.getSoTien()
                ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền vượt công nợ còn lại"
            );
        }


        BigDecimal daThanhToanMoi =
                zero(
                        congNo.getDaThanhToan()
                )
                        .add(
                                tx.getSoTien()
                        );


        BigDecimal conLaiMoi =
                conLai.subtract(
                        tx.getSoTien()
                );


        congNo.setDaThanhToan(
                daThanhToanMoi
        );


        congNo.setConLai(
                conLaiMoi
        );


        // Hết nợ
        if (
                conLaiMoi.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            congNo.setTrangThai(
                    CongNo.TT_DA_THANH_TOAN
            );

        } else {

            // Còn nợ
            congNo.setTrangThai(
                    CongNo.TT_THANH_TOAN_MOT_PHAN
            );
        }


        congNoRepository.save(
                congNo
        );


        // ================================
        // LƯU LỊCH SỬ
        // ================================

        LichSuCongNo lichSu =
                LichSuCongNo
                        .builder()

                        .congNo(
                                congNo
                        )

                        .soTienThanhToan(
                                tx.getSoTien()
                        )

                        .phuongThucThanhToan(
                                LichSuCongNo.PT_PAYOO
                        )

                        .nhanVien(
                                null
                        )

                        .maGiaoDich(
                                tx.getMaGiaoDich()
                        )

                        .ghiChu(
                                "Thanh toán công nợ qua Payoo Mock"
                        )

                        .build();


        lichSuCongNoRepository.save(
                lichSu
        );
    }


    // =================================================
    // CALLBACK THANH TOÁN ĐƠN HÀNG
    // =================================================

    private void xuLyDonHang(
            PayooMockTransaction tx
    ) {

        if (tx.getMaDonHang() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giao dịch không có mã đơn hàng"
            );
        }

        DonHang donHang =
                donHangRepository
                        .findById(tx.getMaDonHang())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy đơn hàng #" + tx.getMaDonHang()
                                )
                        );

        // Thiết lập phương thức thanh toán Payoo
        donHang.setPhuongThucThanhToan(DonHang.PT_PAYOO);
        donHangRepository.save(donHang);

        // Cập nhật trạng thái sang Hoàn thành (quyết toán Quỹ, tạo công nợ và thông báo)
        donHangService.capNhatTrangThai(
                donHang.getMaDonHang(),
                DonHang.TT_HOAN_THANH
        );

        // Tự động tạo hóa đơn nếu chưa có
        List<HoaDon> hoaDons = hoaDonRepository.findAll();
        boolean daCoHoaDon = hoaDons.stream()
                .anyMatch(hd -> hd.getDonHang() != null && hd.getDonHang().getMaDonHang().equals(donHang.getMaDonHang()));

        if (!daCoHoaDon) {
            HoaDon hoaDon = HoaDon.builder()
                    .donHang(donHang)
                    .ngayIn(LocalDate.now())
                    .tongTien(tx.getSoTien())
                    .phuongThucThanhToan(HoaDon.PT_PAYOO)
                    .trangThai(HoaDon.TT_DA_TAO)
                    .build();

            hoaDonRepository.save(hoaDon);
        }
    }


    // =================================================
    // TẠO MÃ GIAO DỊCH
    // =================================================

    private PayooMockTransaction taoGiaoDich(
            String loai,
            Integer maDoiTac,
            Integer maCongNo,
            BigDecimal soTien,
            String noiDung
    ) {
        return taoGiaoDich(loai, maDoiTac, maCongNo, null, soTien, noiDung);
    }

    private PayooMockTransaction taoGiaoDich(
            String loai,
            Integer maDoiTac,
            Integer maCongNo,
            Integer maDonHang,
            BigDecimal soTien,
            String noiDung
    ) {

        validateSoTien(
                soTien
        );


        String prefix =
                switch (loai) {

                    case PayooMockTransaction.LOAI_NAP_QUY ->
                            "PAYOO-NAP";

                    case PayooMockTransaction.LOAI_RUT_QUY ->
                            "PAYOO-RQ";

                    case PayooMockTransaction.LOAI_RUT_VI ->
                            "PAYOO-RV";

                    case PayooMockTransaction.LOAI_THANH_TOAN_CONG_NO ->
                            "PAYOO-CN";

                    case PayooMockTransaction.LOAI_THANH_TOAN_DON_HANG ->
                            "PAYOO-DH";

                    default ->
                            "PAYOO";
                };


        String ma =
                prefix
                        + "-"
                        + LocalDateTime
                        .now()
                        .format(
                                DateTimeFormatter
                                        .ofPattern(
                                                "yyyyMMddHHmmss"
                                        )
                        )

                        + "-"

                        + UUID
                        .randomUUID()
                        .toString()
                        .substring(
                                0,
                                6
                        )
                        .toUpperCase();


        PayooMockTransaction tx =
                PayooMockTransaction
                        .builder()

                        .maGiaoDich(
                                ma
                        )

                        .loaiGiaoDich(
                                loai
                        )

                        .maDoiTac(
                                maDoiTac
                        )

                        .maCongNo(
                                maCongNo
                        )

                        .maDonHang(
                                maDonHang
                        )

                        .soTien(
                                soTien
                        )

                        .trangThai(
                                PayooMockTransaction
                                        .TT_CHO_XU_LY
                        )

                        .noiDung(
                                noiDung
                        )

                        .build();


        return payooRepository.save(
                tx
        );
    }


    private DoiTac getDoiTac(
            Integer maDoiTac
    ) {

        return doiTacRepository
                .findById(
                        maDoiTac
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy đối tác"
                        )
                );
    }


    private PayooMockTransaction getTransaction(
            String id
    ) {

        return payooRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy giao dịch Payoo"
                        )
                );
    }


    private void validateSoTien(
            BigDecimal value
    ) {

        if (
                value == null
                        ||
                        value.compareTo(
                                new BigDecimal(
                                        "1000"
                                )
                        ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền tối thiểu là 1.000đ"
            );
        }
    }


    private BigDecimal zero(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }


    // =====================================
    // GHI NHẬN LỊCH SỬ GIAO DỊCH
    // =====================================

    private void ghiNhanLichSuGiaoDich(
            DoiTac doiTac,
            String loaiVi,
            String loaiGiaoDich,
            BigDecimal soTien,
            String noiDung
    ) {

        LichSuGiaoDichDoiTac lichSu =
                new LichSuGiaoDichDoiTac();

        lichSu.setDoiTac(doiTac);
        lichSu.setLoaiVi(loaiVi);
        lichSu.setLoaiGiaoDich(loaiGiaoDich);
        lichSu.setSoTien(soTien);
        lichSu.setNoiDung(noiDung);

        lichSuGiaoDichDoiTacRepository.save(
                lichSu
        );
    }


    private PayooMockResponse map(
            PayooMockTransaction tx
    ) {

        String trangThaiText =
                switch (
                        tx.getTrangThai()
                        ) {

                    case 0 ->
                            "Chờ xử lý";

                    case 1 ->
                            "Đang xử lý";

                    case 2 ->
                            "Thành công";

                    case 3 ->
                            "Thất bại";

                    default ->
                            "Không rõ";
                };


        return PayooMockResponse
                .builder()

                .maGiaoDich(
                        tx.getMaGiaoDich()
                )

                .loaiGiaoDich(
                        tx.getLoaiGiaoDich()
                )

                .maDoiTac(
                        tx.getMaDoiTac()
                )

                .maCongNo(
                        tx.getMaCongNo()
                )

                .maDonHang(
                        tx.getMaDonHang()
                )

                .soTien(
                        tx.getSoTien()
                )

                .trangThai(
                        tx.getTrangThai()
                )

                .trangThaiText(
                        trangThaiText
                )

                .noiDung(
                        tx.getNoiDung()
                )

                .createdAt(
                        tx.getCreatedAt()
                )

                .completedAt(
                        tx.getCompletedAt()
                )

                .build();
    }
}