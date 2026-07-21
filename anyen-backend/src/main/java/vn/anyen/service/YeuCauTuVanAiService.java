package vn.anyen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import vn.anyen.dto.request.CapNhatYeuCauTuVanAiRequest;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.PhienTuVan;
import vn.anyen.entity.ThongBao;
import vn.anyen.entity.TinNhanTuVan;
import vn.anyen.entity.YeuCauTuVanAi;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.PhienTuVanRepository;
import vn.anyen.repository.ThongBaoRepository;
import vn.anyen.repository.TinNhanTuVanRepository;
import vn.anyen.repository.YeuCauTuVanAiRepository;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
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

    private final TinNhanTuVanRepository
            tinNhanTuVanRepository;

    private final PhanCongTuVanService
            phanCongTuVanService;

    private final ChatRedisService
            chatRedisService;

    private final PlatformTransactionManager
            transactionManager;

    public YeuCauTuVanAiService(
            YeuCauTuVanAiRepository yeuCauTuVanAiRepository,
            PhienTuVanRepository phienTuVanRepository,
            GeminiService geminiService,
            ThongBaoRepository thongBaoRepository,
            NhanVienRepository nhanVienRepository,
            TinNhanTuVanRepository tinNhanTuVanRepository,
            PhanCongTuVanService phanCongTuVanService,
            ChatRedisService chatRedisService,
            PlatformTransactionManager transactionManager
    ) {
        this.yeuCauTuVanAiRepository = yeuCauTuVanAiRepository;
        this.phienTuVanRepository = phienTuVanRepository;
        this.geminiService = geminiService;
        this.thongBaoRepository = thongBaoRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.tinNhanTuVanRepository = tinNhanTuVanRepository;
        this.phanCongTuVanService = phanCongTuVanService;
        this.chatRedisService = chatRedisService;
        this.transactionManager = transactionManager;
    }

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
     * Gọi Gemini ngoài transaction để nhân viên vẫn có thể tiếp quản phiên
     * trong lúc AI đang xử lý. Trước khi lưu câu trả lời, backend khóa lại
     * phiên và kiểm tra lần cuối; nếu nhân viên đã trả lời thì kết quả AI bị bỏ.
     */
    public AiTrichXuatKhachHangResult phanTichTinNhan(
            String tokenPhien,
            String message
    ) {
        if (message == null || message.isBlank()) {
            throw new RuntimeException("Tin nhắn không được để trống");
        }
        if (tokenPhien == null || tokenPhien.isBlank()) {
            throw new RuntimeException("Token phiên tư vấn không được để trống");
        }

        String token = tokenPhien.trim();
        String normalizedMessage = message.trim();

        PhienTuVan sessionSnapshot = phienTuVanRepository
                .findByTokenPhien(token)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiên tư vấn"
                ));

        if (sessionSnapshot.getHetHanLuc() == null
                || sessionSnapshot.getHetHanLuc().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Phiên tư vấn đã hết hạn");
        }

        if (PhienTuVan.TRANG_THAI_DA_DONG.equals(
                sessionSnapshot.getTrangThai()
        )) {
            throw new RuntimeException("Phiên tư vấn đã kết thúc");
        }

        if (daChuyenSangNhanVien(sessionSnapshot)) {
            return taoKetQuaNhanVienDaTiepQuan();
        }

        YeuCauTuVanAi requestSnapshot = layHoacTaoTheoToken(token);

        AiTrichXuatKhachHangResult geminiResult = geminiService
                .trichXuatThongTinKhachHang(
                        sessionSnapshot.getTenKhachHang(),
                        requestSnapshot,
                        normalizedMessage
                );

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        AiTrichXuatKhachHangResult finalized = transactionTemplate.execute(
                status -> hoanTatPhanTichTrongTransaction(
                        token,
                        normalizedMessage,
                        geminiResult
                )
        );

        if (finalized == null) {
            throw new RuntimeException("Không thể hoàn tất xử lý tin nhắn AI");
        }
        return finalized;
    }

    private AiTrichXuatKhachHangResult hoanTatPhanTichTrongTransaction(
            String tokenPhien,
            String message,
            AiTrichXuatKhachHangResult result
    ) {
        PhienTuVan phienTuVan = phienTuVanRepository
                .findByTokenPhienForUpdate(tokenPhien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiên tư vấn"
                ));

        if (PhienTuVan.TRANG_THAI_DA_DONG.equals(phienTuVan.getTrangThai())) {
            throw new RuntimeException("Phiên tư vấn đã kết thúc");
        }

        /*
         * Đây là chốt chống race condition: nhân viên có thể gửi tin trong
         * thời gian Gemini đang chạy. Khi đó không lưu và không trả thêm tin AI.
         */
        if (daChuyenSangNhanVien(phienTuVan)) {
            return taoKetQuaNhanVienDaTiepQuan();
        }

        YeuCauTuVanAi yeuCau = yeuCauTuVanAiRepository
                .findByMaPhien(phienTuVan.getMaPhien())
                .orElseGet(() -> {
                    YeuCauTuVanAi newRequest = new YeuCauTuVanAi();
                    newRequest.setMaPhien(phienTuVan.getMaPhien());
                    newRequest.setHoTen(phienTuVan.getTenKhachHang());
                    return newRequest;
                });

        AiTrichXuatKhachHangResult.CustomerInfo info =
                result.getCustomerInfo();

        if (info != null) {
            capNhatNeuCo(info.getHoTen(), yeuCau::setHoTen);
            capNhatNeuCo(info.getSoDienThoai(), yeuCau::setSoDienThoai);
            capNhatNeuCo(info.getDiaChi(), yeuCau::setDiaChi);
            capNhatNeuCo(info.getNhuCau(), yeuCau::setNhuCau);
            capNhatNeuCo(
                    info.getThoiGianMongMuon(),
                    yeuCau::setThoiGianMongMuon
            );
            capNhatNeuCo(info.getGhiChu(), yeuCau::setGhiChu);

            if (info.getNganSachDuKien() != null) {
                yeuCau.setNganSachDuKien(info.getNganSachDuKien());
            }
        }

        boolean duThongTin = daDuThongTinBatBuoc(yeuCau);
        result.setReadyForHotline(duThongTin);

        if (!duThongTin) {
            yeuCau.setTrangThai(0);
        } else if (!Boolean.TRUE.equals(yeuCau.getDaXacNhan())) {
            yeuCau.setTrangThai(1);
        }

        yeuCauTuVanAiRepository.save(yeuCau);

        boolean khachVuaXacNhan = Boolean.TRUE.equals(
                result.getCustomerConfirmed()
        );
        boolean khachYeuCauNhanVien = laYeuCauNhanVienTrucTiep(message);

        if (khachYeuCauNhanVien) {
            if (!Boolean.TRUE.equals(yeuCau.getDaGuiHotline())) {
                taoThongBaoChoHotline(yeuCau);
                yeuCau.setDaGuiHotline(true);
                yeuCau.setTrangThai(3);
                yeuCauTuVanAiRepository.save(yeuCau);
            }

            phienTuVan.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
            phienTuVan.setUpdatedAt(LocalDateTime.now());
            phienTuVanRepository.save(phienTuVan);

            result.setReadyForHotline(true);
            result.setCustomerConfirmed(true);
            result.setHumanTakeover(true);
            result.setReply(
                    "An Yên đã chuyển toàn bộ cuộc trò chuyện này "
                            + "đến nhân viên tư vấn. "
                            + "Anh/chị có thể tiếp tục nhắn ngay tại đây."
            );
        } else if (khachVuaXacNhan && duThongTin) {
            if (!soDienThoaiHopLe(yeuCau.getSoDienThoai())) {
                result.setCustomerConfirmed(false);
                result.setReply(
                        "Số điện thoại anh/chị cung cấp có vẻ chưa hợp lệ. "
                                + "Anh/chị vui lòng kiểm tra và gửi lại giúp An Yên."
                );
                luuTinNhanAi(phienTuVan, result.getReply());
                return result;
            }

            if (!Boolean.TRUE.equals(yeuCau.getDaXacNhan())) {
                yeuCau.setDaXacNhan(true);
                yeuCau.setTrangThai(2);
                yeuCauTuVanAiRepository.save(yeuCau);
            }

            if (!Boolean.TRUE.equals(yeuCau.getDaGuiHotline())) {
                taoThongBaoChoHotline(yeuCau);
                yeuCau.setDaGuiHotline(true);
                yeuCau.setTrangThai(3);
                yeuCauTuVanAiRepository.save(yeuCau);

                result.setReply(
                        "An Yên đã ghi nhận xác nhận của anh/chị và đã chuyển "
                                + "thông tin đến nhân viên Hotline."
                );
            } else {
                result.setReply(
                        "Thông tin của anh/chị đã được chuyển đến nhân viên Hotline. "
                                + "An Yên sẽ hỗ trợ anh/chị trong thời gian sớm nhất."
                );
            }

            phienTuVan.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
            phienTuVan.setUpdatedAt(LocalDateTime.now());
            phienTuVanRepository.save(phienTuVan);
            result.setHumanTakeover(true);
        }

        luuTinNhanAi(phienTuVan, result.getReply());
        return result;
    }

    private boolean daChuyenSangNhanVien(PhienTuVan phienTuVan) {
        return PhienTuVan.TRANG_THAI_DANG_TU_VAN.equals(
                phienTuVan.getTrangThai()
        ) || tinNhanTuVanRepository.existsByMaPhienAndNguoiGui(
                phienTuVan.getMaPhien(),
                TinNhanTuVan.NGUOI_GUI_NHAN_VIEN
        );
    }

    private AiTrichXuatKhachHangResult taoKetQuaNhanVienDaTiepQuan() {
        AiTrichXuatKhachHangResult result =
                new AiTrichXuatKhachHangResult();
        result.setReply(null);
        result.setReadyForHotline(true);
        result.setCustomerConfirmed(true);
        result.setHumanTakeover(true);
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

        PhienTuVan phienTuVan = phienTuVanRepository
                .findByIdForUpdate(yeuCau.getMaPhien())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiên tư vấn"
                ));
        phienTuVan.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
        phienTuVan.setUpdatedAt(LocalDateTime.now());
        phienTuVanRepository.save(phienTuVan);

        return yeuCauTuVanAiRepository
                .save(yeuCau);
    }

    /**
     * Gán phiên cho một nhân viên đang online rồi chỉ gửi thông báo
     * cho người đó. Nếu chưa có ai online, phiên vẫn nằm trong hàng chờ
     * và thông báo được gửi cho các tài khoản tư vấn/Hotline hoạt động.
     */
    private void taoThongBaoChoHotline(
            YeuCauTuVanAi yeuCau
    ) {
        PhienTuVan phienTuVan = phienTuVanRepository
                .findByIdForUpdate(yeuCau.getMaPhien())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiên tư vấn"
                ));

        NhanVien assignedEmployee =
                phanCongTuVanService.tuDongGanNeuCo(phienTuVan);

        List<NhanVien> recipients = new ArrayList<>();
        if (assignedEmployee != null) {
            recipients.add(assignedEmployee);
        } else {
            Map<Integer, NhanVien> uniqueEmployees = new LinkedHashMap<>();

            nhanVienRepository.findByVaiTroAndTrangThai(
                    NhanVien.VAI_TRO_TU_VAN,
                    NhanVien.TRANG_THAI_HOAT_DONG
            ).forEach(employee -> uniqueEmployees.put(
                    employee.getMaNhanVien(), employee
            ));

            nhanVienRepository.findByVaiTroAndTrangThai(
                    NhanVien.VAI_TRO_HOTLINE,
                    NhanVien.TRANG_THAI_HOAT_DONG
            ).forEach(employee -> uniqueEmployees.put(
                    employee.getMaNhanVien(), employee
            ));

            recipients.addAll(uniqueEmployees.values());
        }

        if (recipients.isEmpty()) {
            throw new RuntimeException(
                    "Hiện không có nhân viên tư vấn/Hotline hoạt động."
            );
        }

        String assignedText = assignedEmployee == null
                ? "Phiên đang chờ một nhân viên online tiếp nhận."
                : "Phiên đã tự động giao cho: " + assignedEmployee.getHoTen() + ".";

        String noiDung = """
                Khách hàng %s đã cung cấp thông tin và cần nhân viên hỗ trợ.
                Mã phiên: %s
                %s

                Số điện thoại: %s
                Địa chỉ: %s
                Nhu cầu: %s
                Thời gian mong muốn: %s
                Ngân sách dự kiến: %s
                Tổng tiền tham khảo: %s
                Ghi chú: %s
                """.formatted(
                safeText(yeuCau.getHoTen()),
                yeuCau.getMaPhien(),
                assignedText,
                safeText(yeuCau.getSoDienThoai()),
                safeText(yeuCau.getDiaChi()),
                safeText(yeuCau.getNhuCau()),
                safeText(yeuCau.getThoiGianMongMuon()),
                safeMoney(yeuCau.getNganSachDuKien()),
                safeMoney(yeuCau.getTongTienThamKhao()),
                safeText(yeuCau.getGhiChu())
        );

        LocalDateTime now = LocalDateTime.now();

        for (NhanVien recipient : recipients) {
            ThongBao thongBao = new ThongBao();
            thongBao.setTieuDe("Khách hàng cần tiếp tục tư vấn từ chatbot");
            thongBao.setNoiDung(noiDung);
            thongBao.setLoaiThongBao("AI_DU_THONG_TIN");
            thongBao.setNguoiGuiId(null);
            thongBao.setNguoiNhanId(recipient.getMaNhanVien());
            thongBao.setTrangThai(0);
            thongBao.setDaDoc(false);
            thongBao.setNgayTao(now);
            thongBao.setNgayCapNhat(now);
            thongBaoRepository.save(thongBao);
        }
    }

    /**
     * Câu trả lời AI phải được lưu chung bảng tin nhắn để nhân viên
     * nhìn thấy toàn bộ lịch sử và tiếp tục đúng cuộc trò chuyện đó.
     */
    private void luuTinNhanAi(
            PhienTuVan phienTuVan,
            String reply
    ) {
        if (reply == null || reply.isBlank()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String content = reply.trim();

        TinNhanTuVan message = new TinNhanTuVan();
        message.setMaPhien(phienTuVan.getMaPhien());
        message.setNguoiGui(TinNhanTuVan.NGUOI_GUI_AI);
        message.setMaNhanVien(null);
        message.setNoiDung(content);
        message.setDaDoc(true);
        message.setCreatedAt(now);
        tinNhanTuVanRepository.save(message);

        phienTuVan.setTinNhanCuoi(
                content.length() <= 500
                        ? content
                        : content.substring(0, 497) + "..."
        );
        phienTuVan.setThoiGianTinNhanCuoi(now);
        phienTuVan.setUpdatedAt(now);
        phienTuVanRepository.save(phienTuVan);
        chatRedisService.evictMessages(phienTuVan.getTokenPhien());
    }

    private boolean laYeuCauNhanVienTrucTiep(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = Normalizer
                .normalize(message.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');

        return normalized.contains("gap nhan vien")
                || normalized.contains("can nhan vien")
                || normalized.contains("chuyen nhan vien")
                || normalized.contains("noi chuyen voi nhan vien")
                || normalized.contains("gap tu van vien")
                || normalized.contains("can tu van vien")
                || normalized.contains("goi hotline")
                || normalized.contains("ho tro truc tiep")
                || normalized.contains("nguoi that");
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