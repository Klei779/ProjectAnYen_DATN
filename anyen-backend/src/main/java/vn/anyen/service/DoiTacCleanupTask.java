package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.DoiTacRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoiTacCleanupTask {

    private final DoiTacRepository doiTacRepository;

    /**
     * Chạy vào lúc 00:00 mỗi ngày để dọn dẹp các lời mời đối tác chưa được xác nhận sau 24h
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredInvitations() {
        log.info("Bắt đầu dọn dẹp các lời mời đối tác quá hạn...");

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        // Lấy tất cả đối tác trạng thái chờ xác nhận (2) và tạo trước 24h
        List<DoiTac> expiredDoiTacs = doiTacRepository.findByTrangThaiAndCreatedAtBefore(
                DoiTac.TT_CHO_XAC_NHAN,
                twentyFourHoursAgo
        );

        int count = 0;
        for (DoiTac dt : expiredDoiTacs) {
            // Đánh dấu là đã xóa (3) để hủy lời mời
            dt.setTrangThai(DoiTac.TT_DA_XOA);
            dt.setConfirmationToken(null);
            count++;
        }

        doiTacRepository.saveAll(expiredDoiTacs);
        log.info("Đã hủy {} lời mời đối tác quá hạn.", count);
    }
}
