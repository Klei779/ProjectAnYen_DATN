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

    /*
     * Web Push:
     * dùng để đẩy notification ra ngoài browser/Windows
     * giống Zalo.
     */
    private final PushNotificationService pushNotificationService;


    // =========================================================
    // THÔNG BÁO NHÂN VIÊN
    // =========================================================
    public void guiThongBaoNhanVien(
            Integer maNhanVien,
            ThongBao thongBao
    ) {

        if (maNhanVien != null) {

            messagingTemplate.convertAndSend(
                    "/topic/nhanvien/" + maNhanVien,
                    "Có thông báo mới"
            );

        } else {

            messagingTemplate.convertAndSend(
                    "/topic/nhanvien",
                    "Có thông báo mới chung"
            );
        }
    }


    // =========================================================
    // THÔNG BÁO ĐỐI TÁC
    // =========================================================
    public void guiThongBaoDoiTac(
            Integer maDoiTac,
            ThongBaoDoiTac thongBaoDoiTac
    ) {

        if (maDoiTac == null) {
            return;
        }


        /*
         * 1. GIỮ NGUYÊN LUỒNG CŨ
         *
         * WebSocket này đang giúp đối tác
         * nhận notification ngay trong website.
         */
        messagingTemplate.convertAndSend(
                "/topic/doitac/" + maDoiTac,
                "Có thông báo mới"
        );


        /*
         * 2. THÊM WEB PUSH
         *
         * Đây là phần giúp notification
         * nổi ngoài màn hình giống Zalo.
         *
         * PushNotificationService đã chạy @Async,
         * nên không làm chậm luồng nghiệp vụ.
         */
        if (thongBaoDoiTac != null) {

            pushNotificationService.guiThongBaoDoiTac(
                    maDoiTac,

                    thongBaoDoiTac.getTieuDe(),

                    thongBaoDoiTac.getNoiDung(),

                    "/doi-tac/thong-bao",

                    "doi-tac-thong-bao-"
                            + thongBaoDoiTac.getMaThongBao(),

                    // ID thông báo
                    thongBaoDoiTac.getMaThongBao(),

                    // CTDH - tạm thời lấy từ nội dung [CTDH:xx]
                    layIdTrongNoiDung(
                            thongBaoDoiTac.getNoiDung(),
                            "[CTDH:"
                    ),

                    // Sản phẩm gợi ý - lấy từ [SPSUGGEST:xx]
                    layIdTrongNoiDung(
                            thongBaoDoiTac.getNoiDung(),
                            "[SPSUGGEST:"
                    )
            );
        }
    }


    // =========================================================
    // XÓA THÔNG BÁO ĐƠN HÀNG ĐỐI TÁC
    // =========================================================
    public void guiThongBaoXoaDoiTac(
            Integer maDoiTac,
            Integer maDonHang
    ) {

        if (maDoiTac != null) {

            messagingTemplate.convertAndSend(

                    "/topic/doitac/"
                            + maDoiTac
                            + "/delete/"
                            + maDonHang,

                    "Xóa thông báo đơn hàng"
            );
        }
    }

    private Integer layIdTrongNoiDung(
            String noiDung,
            String prefix
    ) {

        if (noiDung == null || prefix == null) {
            return null;
        }

        int start =
                noiDung.indexOf(prefix);

        if (start < 0) {
            return null;
        }

        start += prefix.length();

        int end =
                noiDung.indexOf("]", start);

        if (end < 0) {
            return null;
        }

        try {

            return Integer.valueOf(
                    noiDung
                            .substring(start, end)
                            .trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    public void guiThongBaoDonThayTheDaCoNguoiNhan(
            Integer maDoiTac,
            Integer maDonHang,
            Integer maThongBao
    ) {

        if (maDoiTac == null) {
            return;
        }

        /*
         * Nếu đối tác đang mở web
         * thì frontend nhận realtime để reload thông báo.
         */
        messagingTemplate.convertAndSend(
                "/topic/doitac/" + maDoiTac,
                "Đơn thay thế đã có đối tác tiếp nhận"
        );

        /*
         * Push ra Windows/browser.
         *
         * 2 tham số cuối để null
         * => đây KHÔNG phải notification mời thay thế
         * => không hiện nút Xác nhận / Từ chối.
         */
        pushNotificationService.guiThongBaoDoiTac(
                maDoiTac,
                "Đơn hàng đã có đối tác tiếp nhận",
                "Đơn hàng #DH"
                        + String.format("%03d", maDonHang)
                        + " đã được đối tác khác tiếp nhận.",
                "/doi-tac/thong-bao",
                "replacement-taken-"
                        + maDonHang
                        + "-"
                        + maDoiTac,
                maThongBao,
                null,
                null
        );
    }
}