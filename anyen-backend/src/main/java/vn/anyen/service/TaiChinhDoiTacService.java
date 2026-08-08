package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.response.DoiTacTaiChinhResponse;
import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.DamBaoDonHangDoiTac;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.DamBaoDonHangDoiTacRepository;
import vn.anyen.repository.DoiTacRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaiChinhDoiTacService {

    /*
     * Phí sàn cố định 20%.
     */
    private static final BigDecimal TY_LE_PHI_SAN =
            new BigDecimal("0.20");


    private final DoiTacRepository doiTacRepository;

    private final ChiTietDonHangRepository
            chiTietDonHangRepository;

    private final DamBaoDonHangDoiTacRepository
            damBaoRepository;


    // =====================================
    // LẤY THÔNG TIN QUỸ + VÍ
    // =====================================

    @Transactional(readOnly = true)
    public DoiTacTaiChinhResponse getThongTin(
            Authentication authentication
    ) {

        DoiTac doiTac =
                getDoiTacDangNhap(authentication);

        return mapResponse(doiTac);
    }


    // =====================================
    // MỞ QUỸ
    // =====================================

    @Transactional
    public DoiTacTaiChinhResponse moQuy(
            Authentication authentication
    ) {

        DoiTac doiTac =
                getDoiTacDangNhap(authentication);


        if (!Boolean.TRUE.equals(
                doiTac.getDaMoQuy()
        )) {

            doiTac.setDaMoQuy(true);

            doiTacRepository.save(doiTac);
        }


        return mapResponse(doiTac);
    }


    // =====================================
    // NẠP QUỸ
    // =====================================

    /*
     * Hiện tại làm dạng demo đồ án:
     * xác nhận nạp là cộng tiền luôn.
     *
     * Sau này tích hợp VNPay/Momo thì
     * chỉ cộng sau callback thanh toán thành công.
     */
    @Transactional
    public DoiTacTaiChinhResponse napQuy(
            Authentication authentication,
            BigDecimal soTien
    ) {

        DoiTac doiTac =
                getDoiTacDangNhap(authentication);


        if (!Boolean.TRUE.equals(
                doiTac.getDaMoQuy()
        )) {

            throw new RuntimeException(
                    "Vui lòng mở quỹ trước khi nạp tiền"
            );
        }


        if (
                soTien == null
                        || soTien.compareTo(
                        new BigDecimal("1000")
                ) < 0
        ) {

            throw new RuntimeException(
                    "Số tiền nạp tối thiểu là 1.000đ"
            );
        }


        BigDecimal soDuHienTai =
                valueOrZero(
                        doiTac.getSoDuQuy()
                );


        doiTac.setSoDuQuy(
                soDuHienTai.add(soTien)
        );


        doiTacRepository.save(doiTac);


        return mapResponse(doiTac);
    }


    // =====================================
    // KHÓA QUỸ KHI NHẬN ĐƠN
    // =====================================

    @Transactional
    public void khoaQuyChoDonHang(
            Integer maDonHang,
            Integer maDoiTac
    ) {

        /*
         * Một đối tác chỉ có 1 khoản đảm bảo
         * cho một đơn.
         *
         * Kiểm tra trước để tránh bấm Chấp nhận
         * hai lần làm khóa hai lần.
         */
        DamBaoDonHangDoiTac old =
                damBaoRepository
                        .findByMaDonHangAndMaDoiTac(
                                maDonHang,
                                maDoiTac
                        )
                        .orElse(null);


        if (old != null) {

            if (
                    DamBaoDonHangDoiTac
                            .TT_DANG_KHOA
                            .equals(
                                    old.getTrangThai()
                            )

                            ||

                            DamBaoDonHangDoiTac
                                    .TT_DA_QUYET_TOAN
                                    .equals(
                                            old.getTrangThai()
                                    )
            ) {

                return;
            }
        }


        DoiTac doiTac =
                doiTacRepository
                        .findById(maDoiTac)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy đối tác"
                                )
                        );


        /*
         * Đơn cũ / đơn luồng liên hệ
         * vẫn giữ nguyên.
         *
         * Chỉ đối tác mở quỹ mới chạy
         * cơ chế khóa quỹ.
         */
        if (!Boolean.TRUE.equals(
                doiTac.getDaMoQuy()
        )) {

            return;
        }


        /*
         * Tính đúng phần đơn thuộc
         * đối tác đang nhận.
         */
        BigDecimal giaTriPhanDon =
                tinhGiaTriPhanDon(
                        maDonHang,
                        maDoiTac
                );


        if (
                giaTriPhanDon.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            throw new RuntimeException(
                    "Không xác định được giá trị "
                            + "phần đơn của đối tác"
            );
        }


        BigDecimal soDuQuy =
                valueOrZero(
                        doiTac.getSoDuQuy()
                );


        BigDecimal dangKhoa =
                valueOrZero(
                        doiTac
                                .getSoDuQuyDangKhoa()
                );


        /*
         * Quỹ khả dụng =
         * tổng quỹ - tiền đang giữ.
         */
        BigDecimal khaDung =
                soDuQuy.subtract(
                        dangKhoa
                );


        /*
         * QUAN TRỌNG:
         *
         * Yêu cầu đủ 100% giá trị
         * phần đơn của đối tác.
         */
        if (
                khaDung.compareTo(
                        giaTriPhanDon
                ) < 0
        ) {

            BigDecimal canNapThem =
                    giaTriPhanDon.subtract(
                            khaDung
                    );


            throw new RuntimeException(
                    "Quỹ khả dụng không đủ để nhận đơn. "
                            + "Cần thêm "
                            + formatMoney(
                            canNapThem
                    )
            );
        }


        /*
         * Không trừ SoDuQuy ở đây.
         *
         * Chỉ chuyển một phần
         * sang trạng thái "đang khóa".
         */
        doiTac.setSoDuQuyDangKhoa(
                dangKhoa.add(
                        giaTriPhanDon
                )
        );


        doiTacRepository.save(doiTac);


        DamBaoDonHangDoiTac damBao =
                old != null
                        ? old
                        : new DamBaoDonHangDoiTac();


        damBao.setMaDonHang(
                maDonHang
        );

        damBao.setMaDoiTac(
                maDoiTac
        );

        damBao.setSoTienKhoa(
                giaTriPhanDon
        );

        damBao.setPhiSan(
                BigDecimal.ZERO
        );

        damBao.setTienVaoVi(
                BigDecimal.ZERO
        );

        damBao.setTrangThai(
                DamBaoDonHangDoiTac
                        .TT_DANG_KHOA
        );


        damBaoRepository.save(
                damBao
        );
    }


    // =====================================
    // QUYẾT TOÁN ĐƠN
    // =====================================

    /*
     * Gọi khi:
     *
     * - đối tác đã giao hàng
     * - đối tác đã trực tiếp thu tiền khách
     * - nhân viên xác nhận hoàn thành
     *
     *
     * Ví dụ:
     *
     * Quỹ khóa = 10 triệu
     *
     * 2 triệu = An Yên
     * 8 triệu = Ví đối tác
     */
    @Transactional
    public void quyetToanDonHang(
            Integer maDonHang
    ) {

        List<DamBaoDonHangDoiTac>
                danhSach =
                damBaoRepository
                        .findByMaDonHang(
                                maDonHang
                        );


        for (
                DamBaoDonHangDoiTac damBao
                : danhSach
        ) {

            /*
             * Chỉ xử lý khoản đang khóa.
             *
             * Nhờ vậy nếu API bị gọi lần 2
             * cũng không cộng Ví lần 2.
             */
            if (
                    !DamBaoDonHangDoiTac
                            .TT_DANG_KHOA
                            .equals(
                                    damBao.getTrangThai()
                            )
            ) {

                continue;
            }


            DoiTac doiTac =
                    doiTacRepository
                            .findById(
                                    damBao
                                            .getMaDoiTac()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy đối tác để quyết toán"
                                    )
                            );


            BigDecimal soTienKhoa =
                    valueOrZero(
                            damBao.getSoTienKhoa()
                    );


            /*
             * An Yên lấy 20%.
             */
            BigDecimal phiSan =
                    soTienKhoa
                            .multiply(
                                    TY_LE_PHI_SAN
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );


            /*
             * 80% chuyển vào Ví.
             */
            BigDecimal tienVaoVi =
                    soTienKhoa
                            .subtract(
                                    phiSan
                            );


            BigDecimal soDuQuy =
                    valueOrZero(
                            doiTac.getSoDuQuy()
                    );


            BigDecimal dangKhoa =
                    valueOrZero(
                            doiTac
                                    .getSoDuQuyDangKhoa()
                    );


            if (
                    soDuQuy.compareTo(
                            soTienKhoa
                    ) < 0

                            ||

                            dangKhoa.compareTo(
                                    soTienKhoa
                            ) < 0
            ) {

                throw new RuntimeException(
                        "Dữ liệu quỹ của đối tác #"
                                + doiTac.getMaDoiTac()
                                + " không hợp lệ"
                );
            }


            /*
             * Vì 100% tiền đang khóa
             * được quyết toán:
             *
             * Tổng Quỹ -= 100%
             * Đang khóa -= 100%
             */
            doiTac.setSoDuQuy(
                    soDuQuy.subtract(
                            soTienKhoa
                    )
            );


            doiTac.setSoDuQuyDangKhoa(
                    dangKhoa.subtract(
                            soTienKhoa
                    )
            );


            /*
             * Chuyển 80% vào Ví.
             */
            doiTac.setSoDuVi(
                    valueOrZero(
                            doiTac.getSoDuVi()
                    ).add(
                            tienVaoVi
                    )
            );


            doiTacRepository.save(
                    doiTac
            );


            /*
             * Ghi lại kết quả quyết toán.
             */
            damBao.setPhiSan(
                    phiSan
            );

            damBao.setTienVaoVi(
                    tienVaoVi
            );

            damBao.setTrangThai(
                    DamBaoDonHangDoiTac
                            .TT_DA_QUYET_TOAN
            );


            damBaoRepository.save(
                    damBao
            );
        }
    }


    // =====================================
    // TÍNH GIÁ TRỊ PHẦN ĐƠN
    // =====================================

    private BigDecimal tinhGiaTriPhanDon(
            Integer maDonHang,
            Integer maDoiTac
    ) {

        return chiTietDonHangRepository
                .findByDonHangAndDoiTac(
                        maDonHang,
                        maDoiTac
                )
                .stream()
                .map(this::thanhTien)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    private BigDecimal thanhTien(
            ChiTietDonHang ct
    ) {

        BigDecimal gia =
                valueOrZero(
                        ct.getGiaTien()
                );


        int soLuong =
                ct.getSoLuong() == null
                        ? 0
                        : ct.getSoLuong();


        return gia.multiply(
                BigDecimal.valueOf(
                        soLuong
                )
        );
    }


    // =====================================
    // RESPONSE
    // =====================================

    private DoiTacTaiChinhResponse mapResponse(
            DoiTac doiTac
    ) {

        BigDecimal tongQuy =
                valueOrZero(
                        doiTac.getSoDuQuy()
                );


        BigDecimal dangKhoa =
                valueOrZero(
                        doiTac
                                .getSoDuQuyDangKhoa()
                );


        return DoiTacTaiChinhResponse
                .builder()

                .daMoQuy(
                        Boolean.TRUE.equals(
                                doiTac.getDaMoQuy()
                        )
                )

                .soDuQuy(
                        tongQuy
                )

                .soDuQuyDangKhoa(
                        dangKhoa
                )

                .soDuQuyKhaDung(
                        tongQuy.subtract(
                                dangKhoa
                        )
                )

                .soDuVi(
                        valueOrZero(
                                doiTac.getSoDuVi()
                        )
                )

                .build();
    }


    public DoiTac getDoiTacDangNhap(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || authentication.getName() == null
        ) {

            throw new RuntimeException(
                    "Chưa đăng nhập"
            );
        }


        return doiTacRepository
                .findByTenDangNhap(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy đối tác đăng nhập"
                        )
                );
    }


    private BigDecimal valueOrZero(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }


    private String formatMoney(
            BigDecimal value
    ) {

        return String
                .format(
                        "%,.0fđ",
                        value
                )
                .replace(
                        ',',
                        '.'
                );
    }
    @Transactional
    public DoiTacTaiChinhResponse chuyenViVaoQuy(
            Authentication authentication,
            BigDecimal soTien
    ) {

        if (
                soTien == null
                        ||
                        soTien.compareTo(
                                new BigDecimal(
                                        "1000"
                                )
                        ) < 0
        ) {

            throw new RuntimeException(
                    "Số tiền tối thiểu là 1.000đ"
            );
        }


        DoiTac doiTac =
                getDoiTacDangNhap(
                        authentication
                );


        if (
                !Boolean.TRUE.equals(
                        doiTac.getDaMoQuy()
                )
        ) {

            throw new RuntimeException(
                    "Vui lòng mở Quỹ trước"
            );
        }


        BigDecimal soDuVi =
                doiTac.getSoDuVi() == null
                        ? BigDecimal.ZERO
                        : doiTac.getSoDuVi();


        if (
                soDuVi.compareTo(
                        soTien
                ) < 0
        ) {

            throw new RuntimeException(
                    "Số dư Ví không đủ"
            );
        }


        BigDecimal soDuQuy =
                doiTac.getSoDuQuy() == null
                        ? BigDecimal.ZERO
                        : doiTac.getSoDuQuy();


        /*
         * Trừ Ví.
         */
        doiTac.setSoDuVi(
                soDuVi.subtract(
                        soTien
                )
        );


        /*
         * Cộng Quỹ.
         */
        doiTac.setSoDuQuy(
                soDuQuy.add(
                        soTien
                )
        );


        doiTacRepository.save(
                doiTac
        );


        return mapResponse(
                doiTac
        );
    }
}