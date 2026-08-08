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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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


        if (
                conLai.compareTo(
                        soTien
                ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,

                    "Số tiền thanh toán vượt "
                            + "công nợ còn lại"
            );
        }


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

        PayooMockTransaction tx =
                getTransaction(
                        maGiaoDich
                );


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


        return map(tx);
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
    }


    // =================================================
    // CALLBACK RÚT QUỸ
    // =================================================

    private void xuLyRutQuy(
            PayooMockTransaction tx
    ) {

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


        doiTac.setSoDuQuy(
                soDuQuy.subtract(
                        tx.getSoTien()
                )
        );


        doiTacRepository.save(
                doiTac
        );
    }


    // =================================================
    // CALLBACK RÚT VÍ
    // =================================================

    private void xuLyRutVi(
            PayooMockTransaction tx
    ) {

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


        doiTac.setSoDuVi(
                soDuVi.subtract(
                        tx.getSoTien()
                )
        );


        doiTacRepository.save(
                doiTac
        );
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


        /*
         * Kiểm tra lại khi callback.
         */
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


        /*
         * Cập nhật trạng thái.
         */
        if (
                conLaiMoi.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            congNo.setTrangThai(
                    CongNo
                            .TT_DA_THANH_TOAN
            );

        } else {

            congNo.setTrangThai(
                    CongNo
                            .TT_THANH_TOAN_MOT_PHAN
            );
        }


        congNoRepository.save(
                congNo
        );


        /*
         * Lưu lịch sử.
         */
        LichSuCongNo lichSu =
                LichSuCongNo.builder()

                        .congNo(
                                congNo
                        )

                        .soTienThanhToan(
                                tx.getSoTien()
                        )

                        .phuongThucThanhToan(
                                LichSuCongNo
                                        .PT_PAYOO
                        )

                        /*
                         * Demo nên có thể để null.
                         * Sau này muốn lưu admin nào
                         * thanh toán thì map Authentication.
                         */
                        .nhanVien(null)

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
    // TẠO MÃ GIAO DỊCH
    // =================================================

    private PayooMockTransaction taoGiaoDich(
            String loai,
            Integer maDoiTac,
            Integer maCongNo,
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