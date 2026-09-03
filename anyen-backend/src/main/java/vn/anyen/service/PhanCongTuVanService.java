package vn.anyen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.entity.NhanVien;
import vn.anyen.entity.PhienTuVan;
import vn.anyen.entity.TinNhanTuVan;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.repository.PhienTuVanRepository;
import vn.anyen.repository.TinNhanTuVanRepository;
import vn.anyen.repository.YeuCauTuVanAiRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class PhanCongTuVanService {

    private final NhanVienOnlineService nhanVienOnlineService;
    private final NhanVienRepository nhanVienRepository;
    private final PhienTuVanRepository phienTuVanRepository;
    private final YeuCauTuVanAiRepository yeuCauTuVanAiRepository;
    private final TinNhanTuVanRepository tinNhanTuVanRepository;

    public PhanCongTuVanService(
            NhanVienOnlineService nhanVienOnlineService,
            NhanVienRepository nhanVienRepository,
            PhienTuVanRepository phienTuVanRepository,
            YeuCauTuVanAiRepository yeuCauTuVanAiRepository,
            TinNhanTuVanRepository tinNhanTuVanRepository
    ) {
        this.nhanVienOnlineService = nhanVienOnlineService;
        this.nhanVienRepository = nhanVienRepository;
        this.phienTuVanRepository = phienTuVanRepository;
        this.yeuCauTuVanAiRepository = yeuCauTuVanAiRepository;
        this.tinNhanTuVanRepository = tinNhanTuVanRepository;
    }

    /**
     * Gán phiên AI mới cho đúng một nhân viên đang online để người đó có thể
     * theo dõi toàn bộ hội thoại. Phiên vẫn ở trạng thái chờ nên chatbot tiếp tục
     * trả lời cho đến khi nhân viên thật sự gửi tin hoặc khách yêu cầu hỗ trợ trực tiếp.
     */
    @Transactional
    public NhanVien tuDongGanDeTheoDoi(PhienTuVan session) {
        return assignToOnlineEmployee(session, false);
    }

    /**
     * Bàn giao phiên cho nhân viên. Khi đã bàn giao, trạng thái chuyển sang
     * ĐANG_TU_VAN để frontend dừng chatbot và chỉ nhận phản hồi của nhân viên.
     */
    @Transactional
    public NhanVien tuDongGanNeuCo(PhienTuVan session) {
        return assignToOnlineEmployee(session, true);
    }

    /**
     * Chạy khi nhân viên đăng nhập/heartbeat. Mọi phiên chưa có người phụ trách
     * được chia đều cho nhân viên đang online; phiên đã gửi Hotline được kích hoạt
     * ngay, còn phiên AI thông thường chỉ được gán để theo dõi.
     */
    @Transactional
    public void phanCongCacPhienDangCho() {
        /*
         * Thu hồi các phiên đang thuộc tài khoản đã mất heartbeat. Trạng thái
         * ĐANG_TU_VAN được giữ lại để chatbot không tự bật lại khi failover.
         */
        List<PhienTuVan> assignedOpenSessions =
                phienTuVanRepository.findAssignedOpenForUpdate(
                        PhienTuVan.TRANG_THAI_DA_DONG
                );

        for (PhienTuVan session : assignedOpenSessions) {
            boolean employeeAlreadyReplied = tinNhanTuVanRepository
                    .existsByMaPhienAndNguoiGui(
                            session.getMaPhien(),
                            TinNhanTuVan.NGUOI_GUI_NHAN_VIEN
                    );

            /*
             * Khi nhân viên đã gửi ít nhất một tin, quyền sở hữu phiên là cố định.
             * Dù tài khoản tạm mất heartbeat hoặc đăng xuất, phiên vẫn thuộc người đó
             * và sẽ xuất hiện lại khi họ đăng nhập. Chỉ các phiên mới được gán để theo
             * dõi nhưng chưa ai trả lời mới được đưa lại vào hàng chờ.
             */
            if (!employeeAlreadyReplied
                    && !nhanVienOnlineService.isOnline(session.getMaNhanVien())) {
                session.setMaNhanVien(null);
                session.setUpdatedAt(LocalDateTime.now());
                phienTuVanRepository.save(session);
            }
        }

        List<PhienTuVan> waitingSessions =
                phienTuVanRepository.findUnassignedOpenForUpdate(
                        PhienTuVan.TRANG_THAI_DA_DONG
                );

        for (PhienTuVan session : waitingSessions) {
            /*
             * Khôi phục dữ liệu cũ: nếu phiên từng bị bỏ MaNhanVien do bug
             * heartbeat nhưng đã có tin của nhân viên, trả lại đúng người đã
             * gửi tin gần nhất thay vì phân cho tài khoản online khác.
             */
            Integer replyingEmployeeId = tinNhanTuVanRepository
                    .findFirstByMaPhienAndNguoiGuiOrderByCreatedAtDescMaTinNhanDesc(
                            session.getMaPhien(),
                            TinNhanTuVan.NGUOI_GUI_NHAN_VIEN
                    )
                    .map(TinNhanTuVan::getMaNhanVien)
                    .orElse(null);

            if (replyingEmployeeId != null
                    && nhanVienRepository.existsById(replyingEmployeeId)) {
                session.setMaNhanVien(replyingEmployeeId);
                session.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
                session.setUpdatedAt(LocalDateTime.now());
                phienTuVanRepository.save(session);
                continue;
            }

            NhanVien selectedEmployee = selectLeastBusyOnlineEmployee();
            if (selectedEmployee == null) {
                return;
            }

            boolean activateHumanSupport =
                    PhienTuVan.TRANG_THAI_DANG_TU_VAN.equals(session.getTrangThai())
                            || yeuCauTuVanAiRepository
                            .existsByMaPhienAndDaGuiHotlineTrue(session.getMaPhien());

            assign(session, selectedEmployee, activateHumanSupport);
        }
    }

    private NhanVien assignToOnlineEmployee(
            PhienTuVan session,
            boolean activateHumanSupport
    ) {
        if (session == null) {
            return null;
        }

        if (session.getMaNhanVien() != null) {
            Integer currentEmployeeId = session.getMaNhanVien();
            NhanVien current = nhanVienRepository
                    .findById(currentEmployeeId)
                    .orElse(null);

            boolean employeeAlreadyReplied = tinNhanTuVanRepository
                    .existsByMaPhienAndNguoiGui(
                            session.getMaPhien(),
                            TinNhanTuVan.NGUOI_GUI_NHAN_VIEN
                    );

            boolean canKeepCurrentOwner = current != null
                    && (employeeAlreadyReplied
                    || nhanVienOnlineService.isOnline(currentEmployeeId));

            if (canKeepCurrentOwner) {
                if (activateHumanSupport
                        && !PhienTuVan.TRANG_THAI_DA_DONG.equals(session.getTrangThai())) {
                    session.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
                    session.setUpdatedAt(LocalDateTime.now());
                    phienTuVanRepository.save(session);
                }
                return current;
            }

            /* Chủ cũ đã offline: trả phiên về hàng chờ để chọn người online khác. */
            session.setMaNhanVien(null);
        }

        NhanVien selectedEmployee = selectLeastBusyOnlineEmployee();
        if (selectedEmployee == null) {
            return null;
        }

        assign(session, selectedEmployee, activateHumanSupport);
        return selectedEmployee;
    }

    private NhanVien selectLeastBusyOnlineEmployee() {
        Set<Integer> onlineEmployeeIds = nhanVienOnlineService.getOnlineEmployeeIds();
        if (onlineEmployeeIds.isEmpty()) {
            return null;
        }

        return nhanVienRepository.findAllById(onlineEmployeeIds)
                .stream()
                .filter(this::isEligible)
                .min(
                        Comparator
                                .comparingLong((NhanVien employee) ->
                                        phienTuVanRepository.countByMaNhanVienAndTrangThaiNot(
                                                employee.getMaNhanVien(),
                                                PhienTuVan.TRANG_THAI_DA_DONG
                                        )
                                )
                                .thenComparing(NhanVien::getMaNhanVien)
                )
                .orElse(null);
    }

    private boolean isEligible(NhanVien employee) {
        if (employee == null
                || !NhanVien.TRANG_THAI_HOAT_DONG.equals(employee.getTrangThai())) {
            return false;
        }

        return NhanVien.VAI_TRO_TU_VAN.equals(employee.getVaiTro())
                || NhanVien.VAI_TRO_HOTLINE.equals(employee.getVaiTro());
    }

    private void assign(
            PhienTuVan session,
            NhanVien employee,
            boolean activateHumanSupport
    ) {
        session.setMaNhanVien(employee.getMaNhanVien());
        if (activateHumanSupport) {
            session.setTrangThai(PhienTuVan.TRANG_THAI_DANG_TU_VAN);
        }
        session.setUpdatedAt(LocalDateTime.now());
        phienTuVanRepository.save(session);
    }
}
