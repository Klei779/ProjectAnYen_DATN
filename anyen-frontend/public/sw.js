/*
 * Service Worker của An Yên.
 *
 * File này hoạt động độc lập với Vue.
 * Khi trang web đóng, browser Push Service
 * vẫn có thể đánh thức Service Worker.
 */
const SW_VERSION = "ANYEN-SW-2026-08-13-01";

console.log(
    "[SW] ĐANG CHẠY VERSION:",
    SW_VERSION
);

self.addEventListener(
    "install",
    () => {
        self.skipWaiting();
    }
);

self.addEventListener(
    "activate",
    (event) => {

        event.waitUntil(
            self.clients.claim()
        );
    }
);

/*
 * Nhận Push từ backend.
 */
self.addEventListener("push", (event) => {

    console.log(
        "[SW] PUSH - VERSION:",
        SW_VERSION
    );

    let data = {};

    try {

        data =
            event.data
                ? event.data.json()
                : {};

    } catch (error) {

        data = {
            title: "An Yên",
            body: event.data
                ? event.data.text()
                : "Có thông báo mới"
        };
    }


    // ==============================
    // BACKEND QUYẾT ĐỊNH LOẠI PUSH
    // ==============================

    const isReplacement =
        data.isReplacement === true;


    console.log(
        "[SW] PUSH DATA:",
        {
            maDoiTac: data.maDoiTac,
            tenDoiTac: data.tenDoiTac,
            maThongBao: data.maThongBao,
            CTDH: data.maChiTietDonHang,
            SP: data.maSanPhamGoiY,
            isReplacement: isReplacement
        }
    );


    // ==============================
    // LUÔN HIỆN ĐỐI TÁC NHẬN
    // ==============================

    const tenDoiTac =
        data.tenDoiTac ||
        (
            data.maDoiTac
                ? `Đối tác #${data.maDoiTac}`
                : "Đối tác"
        );

    const title =
        `${data.title || "An Yên"} - ${tenDoiTac}`;


    const options = {

        body:
            data.body ||
            "Có thông báo mới",

        tag:
            data.tag ||
            `anyen-${Date.now()}`,

        requireInteraction: true,

        data: {

            url:
                data.url ||
                "/doi-tac/thong-bao",

            maDoiTac:
                data.maDoiTac ?? null,

            tenDoiTac:
            tenDoiTac,

            maThongBao:
                data.maThongBao ?? null,

            maChiTietDonHang:
                data.maChiTietDonHang ?? null,

            maSanPhamGoiY:
                data.maSanPhamGoiY ?? null,

            isReplacement:
            isReplacement
        }
    };


    // ====================================
    // CHỈ LỜI MỜI THAY THẾ CÓ 2 NÚT
    // ====================================

    if (isReplacement === true) {

        options.actions = [
            {
                action: "accept",
                title: "Xác nhận"
            },
            {
                action: "reject",
                title: "Từ chối"
            }
        ];

    } else {

        // ÉP notification thường KHÔNG có action
        options.actions = [];
    }


    event.waitUntil(

        self.registration.showNotification(
            title,
            options
        )
    );
});

/*
 * Đối tác click notification
 * ngoài màn hình.
 */
self.addEventListener(
    "notificationclick",
    (event) => {

        event.notification.close();

        const data =
            event.notification.data || {};

        const action =
            event.action;

        let targetUrl =
            data.url ||
            "/doi-tac/thong-bao";

        // =========================
        // XÁC NHẬN ĐƠN THAY THẾ
        // =========================
        if (
            action === "accept" &&
            data.isReplacement
        ) {

            const params =
                new URLSearchParams();

            params.set(
                "pushAction",
                "accept"
            );

            params.set(
                "maThongBao",
                data.maThongBao
            );

            params.set(
                "maChiTietDonHang",
                data.maChiTietDonHang
            );

            params.set(
                "maSanPhamGoiY",
                data.maSanPhamGoiY
            );

            targetUrl =
                `/doi-tac/thong-bao?${params.toString()}`;
        }

            // =========================
            // TỪ CHỐI ĐƠN THAY THẾ
        // =========================
        else if (
            action === "reject" &&
            data.isReplacement
        ) {

            const params =
                new URLSearchParams();

            params.set(
                "pushAction",
                "reject"
            );

            params.set(
                "maThongBao",
                data.maThongBao
            );

            targetUrl =
                `/doi-tac/thong-bao?${params.toString()}`;
        }

        event.waitUntil(
            clients
                .matchAll({
                    type: "window",
                    includeUncontrolled: true
                })
                .then((clientList) => {

                    for (const client of clientList) {

                        if ("focus" in client) {

                            return client
                                .focus()
                                .then(() =>
                                    client.navigate(
                                        targetUrl
                                    )
                                );
                        }
                    }

                    return clients.openWindow(
                        targetUrl
                    );
                })
        );
    }
);