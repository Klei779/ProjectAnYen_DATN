package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.anyen.entity.ThongBao;
import vn.anyen.entity.ThongBaoDoiTac;

@Service
@RequiredArgsConstructor
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void guiThongBaoNhanVien(Integer maNhanVien, ThongBao thongBao) {
        if (maNhanVien != null) {
            messagingTemplate.convertAndSend("/topic/nhanvien/" + maNhanVien, "Có thông báo mới");
        } else {
            messagingTemplate.convertAndSend("/topic/nhanvien", "Có thông báo mới chung");
        }
    }

    public void guiThongBaoDoiTac(Integer maDoiTac, ThongBaoDoiTac thongBaoDoiTac) {
        if (maDoiTac != null) {
            messagingTemplate.convertAndSend("/topic/doitac/" + maDoiTac, "Có thông báo mới");
        }
    }

    public void guiThongBaoXoaDoiTac(Integer maDoiTac, Integer maDonHang) {
        if (maDoiTac != null) {
            messagingTemplate.convertAndSend("/topic/doitac/" + maDoiTac + "/delete/" + maDonHang, "Xóa thông báo đơn hàng");
        }
    }
}
