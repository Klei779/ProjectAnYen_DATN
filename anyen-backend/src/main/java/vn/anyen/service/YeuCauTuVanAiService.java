package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.dto.request.CapNhatYeuCauTuVanAiRequest;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.PhienTuVan;
import vn.anyen.entity.ThongBao;
import vn.anyen.entity.YeuCauTuVanAi;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.PhienTuVanRepository;
import vn.anyen.repository.ThongBaoRepository;
import vn.anyen.repository.YeuCauTuVanAiRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class YeuCauTuVanAiService {

    private final YeuCauTuVanAiRepository
            yeuCauTuVanAiRepository;

    private final PhienTuVanRepository
            phienTuVanRepository;

    private final GeminiService geminiService;

    private final ThongBaoRepository
            thongBaoRepository;

    private final NhanVienRepository
            nhanVienRepository;

    /**
     * Lấy phiếu yêu cầu AI theo token.
     * Nếu chưa có thì tự tạo một phiếu mới.
     */
    @Transactional
    public YeuCauTuVanAi layHoacTaoTheoToken(
            String tokenPhien
    ) {
        if (tokenPhien == null
                || tokenPhien.isBlank()) {
            throw new RuntimeException(
                    "Token phiên tư vấn không được để trống"
            );
        }

        String token = tokenPhien.trim();

        PhienTuVan phienTuVan =
                phienTuVanRepository
                        .findByTokenPhien(token)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy phiên tư vấn"
                                )
                        );

        if (phienTuVan.getHetHanLuc() == null
                || phienTuVan
                .getHetHanLuc()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Phiên tư vấn đã hết hạn"
            );
        }

        return yeuCauTuVanAiRepository
                .findByMaPhien(
                        phienTuVan.getMaPhien()
                )
                .orElseGet(() -> {
                    YeuCauTuVanAi yeuCau =
                            new YeuCauTuVanAi();

                    yeuCau.setMaPhien(
                            phienTuVan.getMaPhien()
                    );

                    yeuCau.setHoTen(
                            phienTuVan.getTenKhachHang()
                    );

                    return yeuCauTuVanAiRepository
                            .save(yeuCau);
                });
    }

    /**
     * Cập nhật thông tin thủ công.
     */
    @Transactional
    public YeuCauTuVanAi capNhatThongTin(
            String tokenPhien,
            CapNhatYeuCauTuVanAiRequest request
    ) {
        if (request == null) {
            throw new RuntimeException(
                    "Dữ liệu cập nhật không được để trống"
            );
        }

        YeuCauTuVanAi yeuCau =
                layHoacTaoTheoToken(tokenPhien);

        capNhatNeuCo(
                request.getHoTen(),
                yeuCau::setHoTen
        );

        capNhatNeuCo(
                request.getSoDienThoai(),
                yeuCau::setSoDienThoai
        );

        capNhatNeuCo(
                request.getDiaChi(),
                yeuCau::setDiaChi
        );

        capNhatNeuCo(
                request.getNhuCau(),
                yeuCau::setNhuCau
        );

        capNhatNeuCo(
                request.getThoiGianMongMuon(),
                yeuCau::setThoiGianMongMuon
        );

        capNhatNeuCo(
                request.getGhiChu(),
                yeuCau::setGhiChu
        );

        if (request.getNganSachDuKien() != null) {
            yeuCau.setNganSachDuKien(
                    request.getNganSachDuKien()
            );
        }

        return yeuCauTuVanAiRepository
                .save(yeuCau);
    }

    /**
     * Nhận tin nhắn khách, gọi Gemini phân tích,
     * cập nhật thông tin và tự gửi Hotline khi khách xác nhận.
     */
    @Transactional
    public AiTrichXuatKhachHangResult
    phanTichTinNhan(
            String tokenPhien,
            String message
    ) {
        if (message == null || message.isBlank()) {
            throw new RuntimeException(
                    "Tin nhắn không được để trống"
            );
        }

        PhienTuVan phienTuVan =
                phienTuVanRepository
                        .findByTokenPhien(
                                tokenPhien.trim()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy phiên tư vấn"
                                )
                        );

        YeuCauTuVanAi yeuCau =
                layHoacTaoTheoToken(tokenPhien);

        AiTrichXuatKhachHangResult result =
                geminiService
                        .trichXuatThongTinKhachHang(
                                phienTuVan
                                        .getTenKhachHang(),
                                yeuCau,
                                message.trim()
                        );

        AiTrichXuatKhachHangResult.CustomerInfo info =
                result.getCustomerInfo();

        if (info != null) {
            capNhatNeuCo(
                    info.getHoTen(),
                    yeuCau::setHoTen
            );

            capNhatNeuCo(
                    info.getSoDienThoai(),
                    yeuCau::setSoDienThoai
            );

            capNhatNeuCo(
                    info.getDiaChi(),
                    yeuCau::setDiaChi
            );

            capNhatNeuCo(
                    info.getNhuCau(),
                    yeuCau::setNhuCau
            );

            capNhatNeuCo(
                    info.getThoiGianMongMuon(),
                    yeuCau::setThoiGianMongMuon
            );

            capNhatNeuCo(
                    info.getGhiChu(),
                    yeuCau::setGhiChu
            );

            if (info.getNganSachDuKien() != null) {
                yeuCau.setNganSachDuKien(
                        info.getNganSachDuKien()
                );
            }
        }

        boolean duThongTin =
                daDuThongTinBatBuoc(yeuCau);

        result.setReadyForHotline(
                duThongTin
        );

        /*
         * Chưa đủ thông tin:
         * trạng thái 0 = đang thu thập.
         *
         * Đủ thông tin nhưng chưa xác nhận:
         * trạng thái 1 = chờ khách xác nhận.
         */
        if (!duThongTin) {
            yeuCau.setTrangThai(0);
        } else if (!Boolean.TRUE.equals(
                yeuCau.getDaXacNhan()
        )) {
            yeuCau.setTrangThai(1);
        }

        yeuCauTuVanAiRepository.save(yeuCau);

        /*
         * Chỉ tự xác nhận và gửi Hotline khi:
         * - AI nhận ra khách đã xác nhận.
         * - Dữ liệu bắt buộc đã đủ.
         */
        boolean khachVuaXacNhan =
                Boolean.TRUE.equals(
                        result.getCustomerConfirmed()
                );

        if (khachVuaXacNhan && duThongTin) {

            if (!soDienThoaiHopLe(
                    yeuCau.getSoDienThoai()
            )) {
                result.setCustomerConfirmed(false);

                result.setReply(
                        "Số điện thoại anh/chị cung cấp "
                                + "có vẻ chưa hợp lệ. "
                                + "Anh/chị vui lòng kiểm tra "
                                + "và gửi lại giúp An Yên."
                );

                return result;
            }

            /*
             * Đánh dấu khách đã xác nhận.
             */
            if (!Boolean.TRUE.equals(
                    yeuCau.getDaXacNhan()
            )) {
                yeuCau.setDaXacNhan(true);
                yeuCau.setTrangThai(2);

                yeuCauTuVanAiRepository
                        .save(yeuCau);
            }

            /*
             * Chỉ gửi thông báo nếu chưa từng gửi.
             */
            if (!Boolean.TRUE.equals(
                    yeuCau.getDaGuiHotline()
            )) {
                taoThongBaoChoHotline(yeuCau);

                yeuCau.setDaGuiHotline(true);
                yeuCau.setTrangThai(3);

                yeuCauTuVanAiRepository
                        .save(yeuCau);

                result.setReply(
                        "An Yên đã ghi nhận xác nhận "
                                + "của anh/chị và đã chuyển "
                                + "thông tin đến nhân viên Hotline."
                );
            } else {
                result.setReply(
                        "Thông tin của anh/chị đã được "
                                + "chuyển đến nhân viên Hotline. "
                                + "An Yên sẽ hỗ trợ anh/chị "
                                + "trong thời gian sớm nhất."
                );
            }
        }

        return result;
    }

    /**
     * API xác nhận thủ công.
     */
    @Transactional
    public YeuCauTuVanAi xacNhanThongTin(
            String tokenPhien
    ) {
        YeuCauTuVanAi yeuCau =
                layHoacTaoTheoToken(tokenPhien);

        if (!daDuThongTinBatBuoc(yeuCau)) {
            throw new RuntimeException(
                    "Khách hàng chưa cung cấp đủ họ tên, "
                            + "số điện thoại, địa chỉ và nhu cầu."
            );
        }

        if (!soDienThoaiHopLe(
                yeuCau.getSoDienThoai()
        )) {
            throw new RuntimeException(
                    "Số điện thoại khách hàng không hợp lệ."
            );
        }

        yeuCau.setDaXacNhan(true);

        if (!Boolean.TRUE.equals(
                yeuCau.getDaGuiHotline()
        )) {
            yeuCau.setTrangThai(2);
        }

        return yeuCauTuVanAiRepository
                .save(yeuCau);
    }

    /**
     * API gửi Hotline thủ công.
     */
    @Transactional
    public YeuCauTuVanAi guiChoHotline(
            String tokenPhien
    ) {
        YeuCauTuVanAi yeuCau =
                layHoacTaoTheoToken(tokenPhien);

        if (!Boolean.TRUE.equals(
                yeuCau.getDaXacNhan()
        )) {
            throw new RuntimeException(
                    "Khách hàng chưa xác nhận thông tin."
            );
        }

        if (Boolean.TRUE.equals(
                yeuCau.getDaGuiHotline()
        )) {
            throw new RuntimeException(
                    "Yêu cầu này đã được gửi cho Hotline."
            );
        }

        taoThongBaoChoHotline(yeuCau);

        yeuCau.setDaGuiHotline(true);
        yeuCau.setTrangThai(3);

        return yeuCauTuVanAiRepository
                .save(yeuCau);
    }

    /**
     * Tạo thông báo cho tất cả nhân viên Hotline đang hoạt động.
     */
    private void taoThongBaoChoHotline(
            YeuCauTuVanAi yeuCau
    ) {
        List<NhanVien> danhSachHotline =
                nhanVienRepository
                        .findByVaiTroAndTrangThai(
                                4,
                                1
                        );

        if (danhSachHotline.isEmpty()) {
            throw new RuntimeException(
                    "Hiện không có nhân viên Hotline hoạt động."
            );
        }

        String noiDung = """
                Khách hàng %s đã cung cấp đủ thông tin.

                Số điện thoại: %s
                Địa chỉ: %s
                Nhu cầu: %s
                Thời gian mong muốn: %s
                Ngân sách dự kiến: %s
                Tổng tiền tham khảo: %s
                Ghi chú: %s
                """.formatted(
                safeText(yeuCau.getHoTen()),
                safeText(yeuCau.getSoDienThoai()),
                safeText(yeuCau.getDiaChi()),
                safeText(yeuCau.getNhuCau()),
                safeText(
                        yeuCau.getThoiGianMongMuon()
                ),
                safeMoney(
                        yeuCau.getNganSachDuKien()
                ),
                safeMoney(
                        yeuCau.getTongTienThamKhao()
                ),
                safeText(yeuCau.getGhiChu())
        );

        LocalDateTime now =
                LocalDateTime.now();

        for (NhanVien hotline : danhSachHotline) {
            ThongBao thongBao =
                    new ThongBao();

            thongBao.setTieuDe(
                    "Khách hàng đã đủ thông tin lên đơn"
            );

            thongBao.setNoiDung(noiDung);

            thongBao.setLoaiThongBao(
                    "AI_DU_THONG_TIN"
            );

            thongBao.setNguoiGuiId(null);

            thongBao.setNguoiNhanId(
                    hotline.getMaNhanVien()
            );

            thongBao.setTrangThai(0);
            thongBao.setDaDoc(false);
            thongBao.setNgayTao(now);
            thongBao.setNgayCapNhat(now);

            thongBaoRepository.save(thongBao);
        }
    }

    private boolean daDuThongTinBatBuoc(
            YeuCauTuVanAi yeuCau
    ) {
        return coNoiDung(yeuCau.getHoTen())
                && coNoiDung(
                yeuCau.getSoDienThoai()
        )
                && coNoiDung(yeuCau.getDiaChi())
                && coNoiDung(yeuCau.getNhuCau());
    }

    private void capNhatNeuCo(
            String value,
            Consumer<String> setter
    ) {
        if (value != null && !value.isBlank()) {
            setter.accept(value.trim());
        }
    }

    private boolean coNoiDung(
            String value
    ) {
        return value != null
                && !value.trim().isEmpty();
    }

    private boolean soDienThoaiHopLe(
            String soDienThoai
    ) {
        if (soDienThoai == null) {
            return false;
        }

        String normalized =
                soDienThoai.replaceAll(
                        "[^0-9]",
                        ""
                );

        return normalized.matches(
                "^(0|84)(3|5|7|8|9)[0-9]{8}$"
        );
    }

    private String safeText(
            Object value
    ) {
        if (value == null) {
            return "Chưa cung cấp";
        }

        String text =
                value.toString().trim();

        return text.isEmpty()
                ? "Chưa cung cấp"
                : text;
    }

    private String safeMoney(
            BigDecimal value
    ) {
        if (value == null) {
            return "Chưa có";
        }

        return String.format(
                "%,.0f VNĐ",
                value
        );
    }
}