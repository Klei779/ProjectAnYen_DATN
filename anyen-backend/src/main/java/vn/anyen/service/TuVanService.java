package vn.anyen.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.GuiTinNhanTuVanRequest;
import vn.anyen.dto.request.TaoPhienTuVanRequest;
import vn.anyen.dto.response.PhienTuVanResponse;
import vn.anyen.dto.response.TinNhanTuVanResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.PhienTuVan;
import vn.anyen.entity.TinNhanTuVan;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.PhienTuVanRepository;
import vn.anyen.repository.TinNhanTuVanRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TuVanService {

    private static final String WELCOME_MESSAGE =
            "Xin chào, Trợ lý AI An Yên sẵn sàng tiếp nhận nhu cầu của anh/chị. " +
            "Khi cần, toàn bộ cuộc trò chuyện sẽ được chuyển nguyên vẹn cho nhân viên tư vấn.";

    private final PhienTuVanRepository phienTuVanRepository;
    private final TinNhanTuVanRepository tinNhanTuVanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NhanVienOnlineService nhanVienOnlineService;
    private final PhanCongTuVanService phanCongTuVanService;
    private final JwtService jwtService;
    private final ChatRedisService chatRedisService;

    public TuVanService(
            PhienTuVanRepository phienTuVanRepository,
            TinNhanTuVanRepository tinNhanTuVanRepository,
            NhanVienRepository nhanVienRepository,
            NhanVienOnlineService nhanVienOnlineService,
            PhanCongTuVanService phanCongTuVanService,
            JwtService jwtService,
            ChatRedisService chatRedisService
    ) {
        this.phienTuVanRepository = phienTuVanRepository;
        this.tinNhanTuVanRepository = tinNhanTuVanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nhanVienOnlineService = nhanVienOnlineService;
        this.phanCongTuVanService = phanCongTuVanService;
        this.jwtService = jwtService;
        this.chatRedisService = chatRedisService;
    }

    @Transactional
    public PhienTuVanResponse taoPhien(TaoPhienTuVanRequest request) {
        LocalDateTime now = LocalDateTime.now();

        PhienTuVan session = new PhienTuVan();
        session.setTokenPhien(UUID.randomUUID().toString().replace("-", ""));
        session.setTenKhachHang(request.getTenKhachHang().trim());
        session.setTrangThai(PhienTuVan.TRANG_THAI_CHO_TIEP_NHAN);
        session.setTinNhanCuoi(shorten(WELCOME_MESSAGE));
        session.setThoiGianTinNhanCuoi(now);
        session.setSoTinNhanChuaDocNhanVien(0);
        session.setSoTinNhanChuaDocKhach(0);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setHetHanLuc(now.plusDays(2));
        session = phienTuVanRepository.save(session);

        TinNhanTuVan welcome = new TinNhanTuVan();
        welcome.setMaPhien(session.getMaPhien());
        welcome.setNguoiGui(TinNhanTuVan.NGUOI_GUI_AI);
        welcome.setMaNhanVien(null);
        welcome.setNoiDung(WELCOME_MESSAGE);
        welcome.setDaDoc(true);
        welcome.setCreatedAt(now);
        tinNhanTuVanRepository.save(welcome);

        NhanVien routedEmployee =
                phanCongTuVanService.tuDongGanDeTheoDoi(session);

        String guestToken = jwtService.generateGuestChatToken(session.getTokenPhien());
        chatRedisService.rememberGuestSession(
                jwtService.getTokenId(guestToken),
                session.getTokenPhien()
        );
        chatRedisService.evictMessages(session.getTokenPhien());

        return toPhienResponse(session, routedEmployee, true, guestToken);
    }

    @Transactional(readOnly = true)
    public PhienTuVanResponse getPhienKhach(String tokenPhien) {
        return toPhienResponse(requirePhienByToken(tokenPhien), null, true, null);
    }

    @Transactional(readOnly = true)
    public List<TinNhanTuVanResponse> getTinNhanKhach(String tokenPhien) {
        return toTinNhanResponses(requirePhienByToken(tokenPhien));
    }

    @Transactional
    public TinNhanTuVanResponse guiTinNhanKhach(
            String tokenPhien,
            GuiTinNhanTuVanRequest request
    ) {
        PhienTuVan session = requirePhienByTokenForUpdate(tokenPhien);
        ensureOpen(session);

        LocalDateTime now = LocalDateTime.now();
        String content = normalizeMessage(request.getNoiDung());

        TinNhanTuVan message = new TinNhanTuVan();
        message.setMaPhien(session.getMaPhien());
        message.setNguoiGui(TinNhanTuVan.NGUOI_GUI_KHACH_HANG);
        message.setNoiDung(content);
        message.setDaDoc(false);
        message.setCreatedAt(now);
        TinNhanTuVan saved = tinNhanTuVanRepository.save(message);

        session.setTinNhanCuoi(shorten(content));
        session.setThoiGianTinNhanCuoi(now);
        session.setSoTinNhanChuaDocNhanVien(
                safeCount(session.getSoTinNhanChuaDocNhanVien()) + 1
        );
        session.setUpdatedAt(now);
        phienTuVanRepository.save(session);
        chatRedisService.evictMessages(session.getTokenPhien());

        return toTinNhanResponse(saved);
    }

    @Transactional
    public void danhDauKhachDaDoc(String tokenPhien) {
        PhienTuVan session = requirePhienByTokenForUpdate(tokenPhien);
        tinNhanTuVanRepository.markRead(
                session.getMaPhien(),
                TinNhanTuVan.NGUOI_GUI_NHAN_VIEN
        );
        session.setSoTinNhanChuaDocKhach(0);
        session.setUpdatedAt(LocalDateTime.now());
        phienTuVanRepository.save(session);
        chatRedisService.evictMessages(session.getTokenPhien());
    }

    @Transactional
    public List<PhienTuVanResponse> getDanhSachPhienNhanVien(Authentication authentication) {
        NhanVien employee = requireNhanVien(authentication);
        touchOnline(employee);
        phanCongTuVanService.phanCongCacPhienDangCho();

        List<PhienTuVan> sessions = phienTuVanRepository.findVisibleForEmployee(
                employee.getMaNhanVien(),
                PhienTuVan.TRANG_THAI_DA_DONG
        );

        Map<Integer, NhanVien> employeeMap = nhanVienRepository.findAllById(
                        sessions.stream()
                                .map(PhienTuVan::getMaNhanVien)
                                .filter(id -> id != null)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(NhanVien::getMaNhanVien, Function.identity()));

        return sessions.stream()
                .map(session -> toPhienResponse(
                        session,
                        employeeMap.get(session.getMaNhanVien()),
                        false,
                        null
                ))
                .toList();
    }

    @Transactional
    public PhienTuVanResponse nhanPhien(Authentication authentication, Long maPhien) {
        NhanVien employee = requireNhanVien(authentication);
        touchOnline(employee);
        PhienTuVan session = requirePhienForUpdate(maPhien);
        ensureOpen(session);

        if (session.getMaNhanVien() != null
                && !session.getMaNhanVien().equals(employee.getMaNhanVien())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phiên tư vấn đang được " + employeeName(session.getMaNhanVien()) + " phụ trách"
            );
        }

        session.setMaNhanVien(employee.getMaNhanVien());
        session.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
        session.setUpdatedAt(LocalDateTime.now());
        return toPhienResponse(phienTuVanRepository.save(session), employee, false, null);
    }

    @Transactional
    public List<TinNhanTuVanResponse> getTinNhanNhanVien(
            Authentication authentication,
            Long maPhien
    ) {
        NhanVien employee = requireNhanVien(authentication);
        touchOnline(employee);
        PhienTuVan session = requirePhienForUpdate(maPhien);
        ensureEmployeeOwnsSession(session, employee);

        tinNhanTuVanRepository.markRead(
                session.getMaPhien(),
                TinNhanTuVan.NGUOI_GUI_KHACH_HANG
        );
        session.setSoTinNhanChuaDocNhanVien(0);
        session.setUpdatedAt(LocalDateTime.now());
        phienTuVanRepository.save(session);
        chatRedisService.evictMessages(session.getTokenPhien());

        return toTinNhanResponses(session);
    }

    @Transactional
    public TinNhanTuVanResponse guiTinNhanNhanVien(
            Authentication authentication,
            Long maPhien,
            GuiTinNhanTuVanRequest request
    ) {
        NhanVien employee = requireNhanVien(authentication);
        touchOnline(employee);
        PhienTuVan session = requirePhienForUpdate(maPhien);
        ensureOpen(session);

        if (session.getMaNhanVien() != null
                && !session.getMaNhanVien().equals(employee.getMaNhanVien())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phiên tư vấn đang được " + employeeName(session.getMaNhanVien()) + " phụ trách"
            );
        }

        if (session.getMaNhanVien() == null) {
            session.setMaNhanVien(employee.getMaNhanVien());
        }

        /*
         * Tin nhắn đầu tiên của nhân viên là thời điểm bàn giao chính thức.
         * Kể cả phiên đã được tự động gán trước đó, phải chuyển trạng thái sang
         * ĐANG_TU_VAN để chatbot dừng và quyền sở hữu được giữ cố định.
         */
        session.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);

        LocalDateTime now = LocalDateTime.now();
        String content = normalizeMessage(request.getNoiDung());

        TinNhanTuVan message = new TinNhanTuVan();
        message.setMaPhien(session.getMaPhien());
        message.setNguoiGui(TinNhanTuVan.NGUOI_GUI_NHAN_VIEN);
        message.setMaNhanVien(employee.getMaNhanVien());
        message.setNoiDung(content);
        message.setDaDoc(false);
        message.setCreatedAt(now);
        TinNhanTuVan saved = tinNhanTuVanRepository.save(message);

        session.setTinNhanCuoi(shorten(content));
        session.setThoiGianTinNhanCuoi(now);
        session.setSoTinNhanChuaDocKhach(
                safeCount(session.getSoTinNhanChuaDocKhach()) + 1
        );
        session.setUpdatedAt(now);
        phienTuVanRepository.save(session);
        chatRedisService.evictMessages(session.getTokenPhien());

        return toTinNhanResponse(saved);
    }

    @Transactional
    public PhienTuVanResponse dongPhien(Authentication authentication, Long maPhien) {
        NhanVien employee = requireNhanVien(authentication);
        touchOnline(employee);
        PhienTuVan session = requirePhienForUpdate(maPhien);
        ensureEmployeeOwnsSession(session, employee);

        session.setTrangThai(PhienTuVan.TRANG_THAI_DA_DONG);
        session.setUpdatedAt(LocalDateTime.now());
        return toPhienResponse(phienTuVanRepository.save(session), employee, false, null);
    }

    @Transactional
    public void heartbeat(Authentication authentication) {
        NhanVien employee = requireNhanVien(authentication);
        touchOnline(employee);
        phanCongTuVanService.phanCongCacPhienDangCho();
    }

    @Transactional(readOnly = true)
    public boolean isOnline(Authentication authentication) {
        NhanVien employee = requireNhanVien(authentication);
        return nhanVienOnlineService.isOnline(employee.getMaNhanVien());
    }

    @Transactional
    public void offline(Authentication authentication) {
        NhanVien employee = requireNhanVien(authentication);
        nhanVienOnlineService.markOffline(employee.getMaNhanVien());
        phanCongTuVanService.phanCongCacPhienDangCho();
    }

    private void touchOnline(NhanVien employee) {
        nhanVienOnlineService.markOnline(employee);
    }

    private List<TinNhanTuVanResponse> toTinNhanResponses(PhienTuVan session) {
        List<TinNhanTuVanResponse> cached = chatRedisService
                .getCachedMessages(session.getTokenPhien());
        if (!cached.isEmpty()) {
            return cached;
        }

        List<TinNhanTuVanResponse> messages = tinNhanTuVanRepository
                .findByMaPhienOrderByCreatedAtAscMaTinNhanAsc(session.getMaPhien())
                .stream()
                .map(this::toTinNhanResponse)
                .toList();

        chatRedisService.cacheMessages(session.getTokenPhien(), messages);
        return messages;
    }

    private PhienTuVanResponse toPhienResponse(
            PhienTuVan session,
            NhanVien employee,
            boolean includeToken,
            String guestToken
    ) {
        NhanVien resolved = employee;
        if (resolved == null && session.getMaNhanVien() != null) {
            resolved = nhanVienRepository.findById(session.getMaNhanVien()).orElse(null);
        }

        return new PhienTuVanResponse(
                session.getMaPhien(),
                includeToken ? session.getTokenPhien() : null,
                session.getTenKhachHang(),
                session.getMaNhanVien(),
                includeToken
                        ? (session.getMaNhanVien() == null ? null : "Nhân viên tư vấn")
                        : (resolved == null ? null : resolved.getHoTen()),
                session.getTrangThai(),
                statusLabel(session.getTrangThai()),
                session.getTinNhanCuoi(),
                session.getThoiGianTinNhanCuoi(),
                safeCount(session.getSoTinNhanChuaDocNhanVien()),
                safeCount(session.getSoTinNhanChuaDocKhach()),
                session.getCreatedAt(),
                guestToken
        );
    }

    private TinNhanTuVanResponse toTinNhanResponse(TinNhanTuVan message) {
        String senderName;
        if (TinNhanTuVan.NGUOI_GUI_KHACH_HANG.equals(message.getNguoiGui())) {
            senderName = "Khách hàng";
        } else if (TinNhanTuVan.NGUOI_GUI_AI.equals(message.getNguoiGui())) {
            senderName = "Trợ lý AI An Yên";
        } else {
            senderName = "Nhân viên tư vấn";
        }

        return new TinNhanTuVanResponse(
                message.getMaTinNhan(),
                message.getMaPhien(),
                message.getNguoiGui(),
                message.getMaNhanVien(),
                senderName,
                message.getNoiDung(),
                message.getDaDoc(),
                message.getCreatedAt()
        );
    }

    private PhienTuVan requirePhienByToken(String tokenPhien) {
        if (tokenPhien == null || tokenPhien.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên tư vấn");
        }
        return phienTuVanRepository.findByTokenPhien(tokenPhien.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Phiên tư vấn không tồn tại hoặc đã hết hiệu lực"
                ));
    }

    private PhienTuVan requirePhienByTokenForUpdate(String tokenPhien) {
        if (tokenPhien == null || tokenPhien.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên tư vấn");
        }
        return phienTuVanRepository.findByTokenPhienForUpdate(tokenPhien.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Phiên tư vấn không tồn tại hoặc đã hết hiệu lực"
                ));
    }

    private PhienTuVan requirePhienForUpdate(Long maPhien) {
        return phienTuVanRepository.findByIdForUpdate(maPhien)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy phiên tư vấn"
                ));
    }

    private NhanVien requireNhanVien(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }
        return nhanVienRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Không tìm thấy tài khoản nhân viên"
                ));
    }

    private void ensureEmployeeOwnsSession(PhienTuVan session, NhanVien employee) {
        if (session.getMaNhanVien() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bạn cần tiếp nhận phiên tư vấn trước khi xem tin nhắn"
            );
        }
        if (!session.getMaNhanVien().equals(employee.getMaNhanVien())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không phải nhân viên đang phụ trách phiên tư vấn này"
            );
        }
    }

    private void ensureOpen(PhienTuVan session) {
        if (PhienTuVan.TRANG_THAI_DA_DONG.equals(session.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiên tư vấn đã kết thúc");
        }
    }

    private String normalizeMessage(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nội dung tin nhắn không được để trống"
            );
        }
        return normalized;
    }

    private String shorten(String content) {
        return content.length() <= 500 ? content : content.substring(0, 497) + "...";
    }

    private int safeCount(Integer count) {
        return count == null || count < 0 ? 0 : count;
    }

    private String statusLabel(Integer status) {
        if (PhienTuVan.TRANG_THAI_DANG_TU_VAN.equals(status)) return "Đang tư vấn";
        if (PhienTuVan.TRANG_THAI_DA_DONG.equals(status)) return "Đã kết thúc";
        return "Chờ tiếp nhận";
    }

    private String employeeName(Integer employeeId) {
        return nhanVienRepository.findById(employeeId)
                .map(NhanVien::getHoTen)
                .orElse("một nhân viên khác");
    }
}
