package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.anyen.dto.response.CongNoResponse;

import vn.anyen.entity.ChiTietDonHang;
import vn.anyen.entity.CongNo;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.DonHang;

import vn.anyen.repository.ChiTietDonHangRepository;
import vn.anyen.repository.CongNoRepository;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.DonHangRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CongNoService {

    private final CongNoRepository congNoRepository;

    private final DonHangRepository donHangRepository;

    private final ChiTietDonHangRepository
            chiTietDonHangRepository;

    private final DoiTacRepository doiTacRepository;


    // =================================================
    // DANH SÁCH CÔNG NỢ
    // =================================================

    /*
     * Hàm này chính là hàm Controller
     * đang bị đỏ:
     *
     * congNoService.getDanhSach(...)
     */
    @Transactional(readOnly = true)
    public Page<CongNoResponse> getDanhSach(
            int page,
            int size,
            Integer trangThai
    ) {

        /*
         * Chống page âm.
         */
        int pageSafe =
                Math.max(
                        page,
                        0
                );


        /*
         * Chống size <= 0
         * hoặc lấy quá nhiều.
         */
        int sizeSafe;

        if (size <= 0) {

            sizeSafe = 10;

        } else {

            sizeSafe =
                    Math.min(
                            size,
                            100
                    );
        }


        Pageable pageable =
                PageRequest.of(
                        pageSafe,
                        sizeSafe
                );


        Page<CongNo> pageCongNo;


        /*
         * Không truyền trạng thái:
         *
         * GET /api/admin/congno
         *
         * => lấy tất cả.
         */
        if (trangThai == null) {

            pageCongNo =
                    congNoRepository
                            .findAll(
                                    pageable
                            );

        } else {

            /*
             * Có trạng thái:
             *
             * ?trangThai=0
             *
             * => lọc.
             */
            pageCongNo =
                    congNoRepository
                            .findByTrangThai(
                                    trangThai,
                                    pageable
                            );
        }


        /*
         * Entity -> DTO.
         */
        return pageCongNo.map(
                this::mapToResponse
        );
    }


    // =================================================
    // MAP ENTITY -> RESPONSE
    // =================================================

    private CongNoResponse mapToResponse(
            CongNo congNo
    ) {

        DonHang donHang =
                congNo.getDonHang();


        DoiTac doiTac =
                congNo.getDoiTac();


        // =============================
        // TÊN ĐỐI TÁC
        // =============================

        String tenDoiTac =
                "Không xác định";


        if (doiTac != null) {

            /*
             * Ưu tiên tên doanh nghiệp.
             */
            if (
                    doiTac.getTenDoanhNghiep() != null
                            &&
                            !doiTac
                                    .getTenDoanhNghiep()
                                    .isBlank()
            ) {

                tenDoiTac =
                        doiTac
                                .getTenDoanhNghiep();

            } else if (
                    doiTac.getTenDoiTac() != null
            ) {

                tenDoiTac =
                        doiTac
                                .getTenDoiTac();
            }
        }


        return CongNoResponse
                .builder()

                .maCongNo(
                        congNo.getMaCongNo()
                )

                .maDonHang(
                        donHang != null
                                ? donHang.getMaDonHang()
                                : null
                )

                .maDoiTac(
                        doiTac != null
                                ? doiTac.getMaDoiTac()
                                : null
                )

                .tenDoiTac(
                        tenDoiTac
                )

                .tongTien(
                        zero(
                                congNo.getTongTien()
                        )
                )

                .daThanhToan(
                        zero(
                                congNo.getDaThanhToan()
                        )
                )

                .conLai(
                        zero(
                                congNo.getConLai()
                        )
                )

                .hanThanhToan(
                        congNo.getHanThanhToan()
                )

                .trangThai(
                        congNo.getTrangThai()
                )

                .trangThaiText(
                        getTrangThaiCongNoText(
                                congNo
                        )
                )

                .ghiChu(
                        congNo.getGhiChu()
                )

                .createdAt(
                        congNo.getCreatedAt()
                )

                .updatedAt(
                        congNo.getUpdatedAt()
                )

                /*
                 * Thông tin đơn hàng.
                 */
                .ngayTaoDon(
                        donHang != null
                                &&
                                donHang.getNgayTaoDon() != null

                                ? donHang
                                .getNgayTaoDon()
                                .toString()

                                : null
                )

                .trangThaiDonHang(
                        donHang != null

                                ? getTrangThaiDonHangText(
                                donHang.getTrangThai()
                        )

                                : null
                )

                .build();
    }


    // =================================================
    // TEXT TRẠNG THÁI CÔNG NỢ
    // =================================================

    private String getTrangThaiCongNoText(
            CongNo congNo
    ) {

        if (
                congNo == null
        ) {

            return "Không xác định";
        }


        /*
         * Nếu chưa thanh toán hết
         * và đã quá hạn.
         *
         * Chỉ hiển thị là Quá hạn.
         */
        if (
                zero(
                        congNo.getConLai()
                )
                        .compareTo(
                                BigDecimal.ZERO
                        ) > 0
                        &&
                        congNo.getHanThanhToan() != null
                        &&
                        congNo
                                .getHanThanhToan()
                                .isBefore(
                                        LocalDate.now()
                                )
        ) {

            return "Quá hạn";
        }


        Integer trangThai =
                congNo.getTrangThai();


        if (trangThai == null) {

            return "Không xác định";
        }


        if (
                trangThai.equals(
                        CongNo.TT_CHUA_THANH_TOAN
                )
        ) {

            return "Chưa thanh toán";
        }


        if (
                trangThai.equals(
                        CongNo.TT_THANH_TOAN_MOT_PHAN
                )
        ) {

            return "Thanh toán một phần";
        }


        if (
                trangThai.equals(
                        CongNo.TT_DA_THANH_TOAN
                )
        ) {

            return "Đã thanh toán";
        }


        if (
                trangThai.equals(
                        CongNo.TT_QUA_HAN
                )
        ) {

            return "Quá hạn";
        }


        return "Không xác định";
    }


    // =================================================
    // TEXT TRẠNG THÁI ĐƠN
    // =================================================

    private String getTrangThaiDonHangText(
            Integer trangThai
    ) {

        if (trangThai == null) {

            return "Không xác định";
        }


        if (
                trangThai.equals(
                        DonHang.TT_MOI_TAO
                )
        ) {

            return "Mới tạo";
        }


        if (
                trangThai.equals(
                        DonHang.TT_CHO_DOI_TAC_XAC_NHAN
                )
        ) {

            return "Chờ đối tác xác nhận";
        }


        if (
                trangThai.equals(
                        DonHang.TT_DA_XAC_NHAN
                )
        ) {

            return "Đã xác nhận";
        }


        if (
                trangThai.equals(
                        DonHang.TT_DANG_XU_LY
                )
        ) {

            return "Đang xử lý";
        }


        if (
                trangThai.equals(
                        DonHang.TT_CHO_THANH_TOAN
                )
        ) {

            return "Chờ thanh toán";
        }


        if (
                trangThai.equals(
                        DonHang.TT_HOAN_THANH
                )
        ) {

            return "Hoàn thành";
        }


        if (
                trangThai.equals(
                        DonHang.TT_DA_HUY
                )
        ) {

            return "Đã hủy";
        }


        if (
                trangThai.equals(
                        DonHang.TT_DOI_TAC_TU_CHOI
                )
        ) {

            return "Đối tác từ chối";
        }


        if (
                trangThai.equals(
                        DonHang.TT_DA_GIAO
                )
        ) {

            return "Đã giao";
        }


        if (
                trangThai.equals(
                        DonHang.TT_DA_THANH_TOAN
                )
        ) {

            return "Đã thanh toán";
        }


        if (
                trangThai.equals(
                        DonHang.TT_GAP_SU_CO
                )
        ) {

            return "Gặp sự cố";
        }


        return "Không xác định";
    }


    // =================================================
    // TẠO CÔNG NỢ TỪ ĐƠN
    // =================================================

    @Transactional
    public void taoCongNoTuDonHang(
            Integer maDonHang
    ) {

        DonHang donHang =
                donHangRepository
                        .findById(
                                maDonHang
                        )
                        .orElse(null);


        if (donHang == null) {

            return;
        }


        List<ChiTietDonHang> chiTietList =
                chiTietDonHangRepository
                        .findByDonHang_MaDonHang(
                                maDonHang
                        );


        if (
                chiTietList == null
                        ||
                        chiTietList.isEmpty()
        ) {

            return;
        }


        /*
         * Gom tổng tiền theo từng đối tác.
         */
        Map<Integer, BigDecimal>
                tongTienTheoDoiTac =
                new HashMap<>();


        for (
                ChiTietDonHang chiTiet
                : chiTietList
        ) {

            if (
                    chiTiet.getSanPham() == null
                            ||
                            chiTiet
                                    .getSanPham()
                                    .getMaDoiTac() == null
            ) {

                continue;
            }


            Integer maDoiTac =
                    chiTiet
                            .getSanPham()
                            .getMaDoiTac();


            BigDecimal gia =
                    zero(
                            chiTiet.getGiaTien()
                    );


            Integer soLuong =
                    chiTiet.getSoLuong() == null
                            ? 0
                            : chiTiet.getSoLuong();


            BigDecimal thanhTien =
                    gia.multiply(
                            BigDecimal.valueOf(
                                    soLuong
                            )
                    );


            BigDecimal tongHienTai =
                    tongTienTheoDoiTac
                            .getOrDefault(
                                    maDoiTac,
                                    BigDecimal.ZERO
                            );


            tongTienTheoDoiTac.put(
                    maDoiTac,
                    tongHienTai.add(
                            thanhTien
                    )
            );
        }


        /*
         * Tạo công nợ riêng
         * cho từng đối tác.
         */
        for (
                Map.Entry<
                        Integer,
                        BigDecimal
                        > entry

                : tongTienTheoDoiTac
                .entrySet()
        ) {

            Integer maDoiTac =
                    entry.getKey();


            BigDecimal tongTien =
                    entry.getValue();


            /*
             * Không tạo lại nếu đơn này
             * đã có công nợ của đối tác.
             */
            boolean daTonTai =
                    congNoRepository
                            .existsByDonHang_MaDonHangAndDoiTac_MaDoiTac(
                                    maDonHang,
                                    maDoiTac
                            );


            if (daTonTai) {

                continue;
            }


            Optional<DoiTac> doiTacOpt =
                    doiTacRepository
                            .findById(
                                    maDoiTac
                            );


            if (
                    doiTacOpt.isEmpty()
            ) {

                continue;
            }


            CongNo congNo =
                    CongNo
                            .builder()

                            .donHang(
                                    donHang
                            )

                            .doiTac(
                                    doiTacOpt.get()
                            )

                            .tongTien(
                                    tongTien
                            )

                            .daThanhToan(
                                    BigDecimal.ZERO
                            )

                            .conLai(
                                    tongTien
                            )

                            .hanThanhToan(
                                    LocalDate
                                            .now()
                                            .plusDays(30)
                            )

                            .trangThai(
                                    CongNo
                                            .TT_CHUA_THANH_TOAN
                            )

                            .ghiChu(
                                    "Công nợ sinh tự động "
                                            + "từ đơn hàng #"
                                            + maDonHang
                            )

                            .build();


            congNoRepository.save(
                    congNo
            );
        }
    }


    // =================================================
    // NULL BIGDECIMAL -> 0
    // =================================================

    private BigDecimal zero(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}