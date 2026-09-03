package vn.anyen.service;

import org.springframework.stereotype.Service;
import vn.anyen.entity.NhanVien;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NhanVienOnlineService {

    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(70);

    private final ConcurrentHashMap<Integer, LocalDateTime> lastSeenByEmployee =
            new ConcurrentHashMap<>();

    public void markOnline(NhanVien employee) {
        if (!isChatEmployee(employee)) {
            return;
        }

        lastSeenByEmployee.put(
                employee.getMaNhanVien(),
                LocalDateTime.now()
        );
    }

    public void markOffline(Integer employeeId) {
        if (employeeId != null) {
            lastSeenByEmployee.remove(employeeId);
        }
    }

    public boolean isOnline(Integer employeeId) {
        if (employeeId == null) {
            return false;
        }

        LocalDateTime lastSeen = lastSeenByEmployee.get(employeeId);
        if (lastSeen == null) {
            return false;
        }

        boolean online = lastSeen.plus(ONLINE_TIMEOUT).isAfter(LocalDateTime.now());
        if (!online) {
            lastSeenByEmployee.remove(employeeId, lastSeen);
        }

        return online;
    }

    public Set<Integer> getOnlineEmployeeIds() {
        LocalDateTime now = LocalDateTime.now();

        lastSeenByEmployee.entrySet().removeIf(entry ->
                entry.getValue().plus(ONLINE_TIMEOUT).isBefore(now)
        );

        return lastSeenByEmployee.keySet()
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isChatEmployee(NhanVien employee) {
        if (employee == null
                || employee.getMaNhanVien() == null
                || !NhanVien.TRANG_THAI_HOAT_DONG.equals(employee.getTrangThai())) {
            return false;
        }

        return NhanVien.VAI_TRO_TU_VAN.equals(employee.getVaiTro())
                || NhanVien.VAI_TRO_HOTLINE.equals(employee.getVaiTro());
    }
}
