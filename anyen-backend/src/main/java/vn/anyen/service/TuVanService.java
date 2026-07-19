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

    private final PhienTuVanRepository phienTuVanRepository;
    private final TinNhanTuVanRepository tinNhanTuVanRepository;
    private final NhanVienRepository nhanVienRepository;

    public TuVanService(
            PhienTuVanRepository phienTuVanRepository,
            TinNhanTuVanRepository tinNhanTuVanRepository,
            NhanVienRepository nhanVienRepository
    ) {
        this.phienTuVanRepository = phienTuVanRepository;
        this.tinNhanTuVanRepository = tinNhanTuVanRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    @Transactional
    public PhienTuVanResponse taoPhien(TaoPhienTuVanRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PhienTuVan phien = new PhienTuVan();
        phien.setTokenPhien(UUID.randomUUID().toString().replace("-", ""));
        phien.setTenKhachHang(request.getTenKhachHang().trim());
        phien.setTrangThai(PhienTuVan.TRANG_THAI_CHO_TIEP_NHAN);
        phien.setTinNhanCuoi("Khách hàng vừa bắt đầu phiên tư vấn");
        phien.setThoiGianTinNhanCuoi(now);
        phien.setSoTinNhanChuaDocNhanVien(0);
        phien.setSoTinNhanChuaDocKhach(0);
        phien.setCreatedAt(now);
        phien.setUpdatedAt(now);

        return toPhienResponse(phienTuVanRepository.save(phien), null, true);
    }

    @Transactional(readOnly = true)
    public PhienTuVanResponse getPhienKhach(String tokenPhien) {
        return toPhienResponse(requirePhienByToken(tokenPhien), null, true);
    }

    @Transactional(readOnly = true)
    public List<TinNhanTuVanResponse> getTinNhanKhach(String tokenPhien) {
        PhienTuVan phien = requirePhienByToken(tokenPhien);
        return toTinNhanResponses(phien);
    }

    @Transactional
    public TinNhanTuVanResponse guiTinNhanKhach(
            String tokenPhien,
            GuiTinNhanTuVanRequest request
    ) {
        PhienTuVan phien = requirePhienByTokenForUpdate(tokenPhien);
        ensureOpen(phien);

        LocalDateTime now = LocalDateTime.now();
        String content = normalizeMessage(request.getNoiDung());
        TinNhanTuVan message = new TinNhanTuVan();
        message.setMaPhien(phien.getMaPhien());
        message.setNguoiGui(TinNhanTuVan.NGUOI_GUI_KHACH_HANG);
        message.setNoiDung(content);
        message.setDaDoc(false);
        message.setCreatedAt(now);
        TinNhanTuVan saved = tinNhanTuVanRepository.save(message);

        phien.setTinNhanCuoi(shorten(content));
        phien.setThoiGianTinNhanCuoi(now);
        phien.setSoTinNhanChuaDocNhanVien(
                safeCount(phien.getSoTinNhanChuaDocNhanVien()) + 1
        );
        phien.setUpdatedAt(now);
        phienTuVanRepository.save(phien);

        return toTinNhanResponse(saved, null);
    }

    @Transactional
    public void danhDauKhachDaDoc(String tokenPhien) {
        PhienTuVan phien = requirePhienByTokenForUpdate(tokenPhien);
        tinNhanTuVanRepository.markRead(
                phien.getMaPhien(),
                TinNhanTuVan.NGUOI_GUI_NHAN_VIEN
        );
        phien.setSoTinNhanChuaDocKhach(0);
        phien.setUpdatedAt(LocalDateTime.now());
        phienTuVanRepository.save(phien);
    }

    @Transactional(readOnly = true)
    public List<PhienTuVanResponse> getDanhSachPhienNhanVien(Authentication authentication) {
        NhanVien currentEmployee = requireNhanVien(authentication);
        List<PhienTuVan> sessions = phienTuVanRepository.findVisibleForEmployee(
                currentEmployee.getMaNhanVien(),
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
                .map(session -> toPhienResponse(session, employeeMap.get(session.getMaNhanVien()), false))
                .toList();
    }

    @Transactional
    public PhienTuVanResponse nhanPhien(
            Authentication authentication,
            Long maPhien
    ) {
        NhanVien currentEmployee = requireNhanVien(authentication);
        PhienTuVan phien = requirePhienForUpdate(maPhien);
        ensureOpen(phien);

        if (phien.getMaNhanVien() != null
                && !phien.getMaNhanVien().equals(currentEmployee.getMaNhanVien())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phiên tư vấn đang được " + employeeName(phien.getMaNhanVien()) + " phụ trách"
            );
        }

        phien.setMaNhanVien(currentEmployee.getMaNhanVien());
        phien.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
        phien.setUpdatedAt(LocalDateTime.now());
        return toPhienResponse(phienTuVanRepository.save(phien), currentEmployee, false);
    }

    @Transactional
    public List<TinNhanTuVanResponse> getTinNhanNhanVien(
            Authentication authentication,
            Long maPhien
    ) {
        NhanVien currentEmployee = requireNhanVien(authentication);
        PhienTuVan phien = requirePhienForUpdate(maPhien);

        ensureEmployeeOwnsSession(phien, currentEmployee);

        tinNhanTuVanRepository.markRead(
                phien.getMaPhien(),
                TinNhanTuVan.NGUOI_GUI_KHACH_HANG
        );
        phien.setSoTinNhanChuaDocNhanVien(0);
        phien.setUpdatedAt(LocalDateTime.now());
        phienTuVanRepository.save(phien);

        return toTinNhanResponses(phien);
    }

    @Transactional
    public TinNhanTuVanResponse guiTinNhanNhanVien(
            Authentication authentication,
            Long maPhien,
            GuiTinNhanTuVanRequest request
    ) {
        NhanVien currentEmployee = requireNhanVien(authentication);
        PhienTuVan phien = requirePhienForUpdate(maPhien);
        ensureOpen(phien);

        if (phien.getMaNhanVien() != null
                && !phien.getMaNhanVien().equals(currentEmployee.getMaNhanVien())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phiên tư vấn đang được " + employeeName(phien.getMaNhanVien()) + " phụ trách"
            );
        }

        if (phien.getMaNhanVien() == null) {
            phien.setMaNhanVien(currentEmployee.getMaNhanVien());
            phien.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
        }

        LocalDateTime now = LocalDateTime.now();
        String content = normalizeMessage(request.getNoiDung());
        TinNhanTuVan message = new TinNhanTuVan();
        message.setMaPhien(phien.getMaPhien());
        message.setNguoiGui(TinNhanTuVan.NGUOI_GUI_NHAN_VIEN);
        message.setMaNhanVien(currentEmployee.getMaNhanVien());
        message.setNoiDung(content);
        message.setDaDoc(false);
        message.setCreatedAt(now);
        TinNhanTuVan saved = tinNhanTuVanRepository.save(message);

        phien.setTinNhanCuoi(shorten(content));
        phien.setThoiGianTinNhanCuoi(now);
        phien.setSoTinNhanChuaDocKhach(
                safeCount(phien.getSoTinNhanChuaDocKhach()) + 1
        );
        phien.setUpdatedAt(now);
        phienTuVanRepository.save(phien);

        return toTinNhanResponse(saved, currentEmployee);
    }

    @Transactional
    public PhienTuVanResponse dongPhien(
            Authentication authentication,
            Long maPhien
    ) {
        NhanVien currentEmployee = requireNhanVien(authentication);
        PhienTuVan phien = requirePhienForUpdate(maPhien);

        ensureEmployeeOwnsSession(phien, currentEmployee);

        phien.setTrangThai(PhienTuVan.TRANG_THAI_DA_DONG);
        phien.setUpdatedAt(LocalDateTime.now());
        return toPhienResponse(phienTuVanRepository.save(phien), currentEmployee, false);
    }

    private List<TinNhanTuVanResponse> toTinNhanResponses(PhienTuVan phien) {
        List<TinNhanTuVan> messages =
                tinNhanTuVanRepository.findByMaPhienOrderByCreatedAtAscMaTinNhanAsc(phien.getMaPhien());

        Map<Integer, NhanVien> employeeMap = nhanVienRepository.findAllById(
                        messages.stream()
                                .map(TinNhanTuVan::getMaNhanVien)
                                .filter(id -> id != null)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(NhanVien::getMaNhanVien, Function.identity()));

        return messages.stream()
                .map(message -> toTinNhanResponse(message, employeeMap.get(message.getMaNhanVien())))
                .toList();
    }

    private PhienTuVanResponse toPhienResponse(
            PhienTuVan phien,
            NhanVien employee,
            boolean includeToken
    ) {
        NhanVien resolvedEmployee = employee;
        if (resolvedEmployee == null && phien.getMaNhanVien() != null) {
            resolvedEmployee = nhanVienRepository.findById(phien.getMaNhanVien()).orElse(null);
        }

        return new PhienTuVanResponse(
                phien.getMaPhien(),
                includeToken ? phien.getTokenPhien() : null,
                phien.getTenKhachHang(),
                phien.getMaNhanVien(),
                includeToken
                        ? (phien.getMaNhanVien() == null ? null : "Nhân viên tư vấn")
                        : (resolvedEmployee == null ? null : resolvedEmployee.getHoTen()),
                phien.getTrangThai(),
                statusLabel(phien.getTrangThai()),
                phien.getTinNhanCuoi(),
                phien.getThoiGianTinNhanCuoi(),
                safeCount(phien.getSoTinNhanChuaDocNhanVien()),
                safeCount(phien.getSoTinNhanChuaDocKhach()),
                phien.getCreatedAt()
        );
    }

    private TinNhanTuVanResponse toTinNhanResponse(TinNhanTuVan message, NhanVien employee) {
        String senderName = TinNhanTuVan.NGUOI_GUI_KHACH_HANG.equals(message.getNguoiGui())
                ? "Khách hàng"
                : "Nhân viên tư vấn";

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

    private PhienTuVan requirePhien(Long maPhien) {
        return phienTuVanRepository.findById(maPhien)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy phiên tư vấn"
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

    private void ensureEmployeeOwnsSession(PhienTuVan phien, NhanVien currentEmployee) {
        if (phien.getMaNhanVien() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bạn cần tiếp nhận phiên tư vấn trước khi xem tin nhắn"
            );
        }

        if (!phien.getMaNhanVien().equals(currentEmployee.getMaNhanVien())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không phải nhân viên đang phụ trách phiên tư vấn này"
            );
        }
    }

    private void ensureOpen(PhienTuVan phien) {
        if (PhienTuVan.TRANG_THAI_DA_DONG.equals(phien.getTrangThai())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phiên tư vấn đã kết thúc"
            );
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
