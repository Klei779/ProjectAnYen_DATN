import api from "../api/api.js";

function isPushSupported() {
    return (
        "serviceWorker" in navigator &&
        "PushManager" in window &&
        "Notification" in window
    );
}

function urlBase64ToUint8Array(base64String) {
    const padding =
        "=".repeat(
            (4 - (base64String.length % 4)) % 4
        );

    const base64 =
        (base64String + padding)
            .replace(/-/g, "+")
            .replace(/_/g, "/");

    const rawData =
        window.atob(base64);

    return Uint8Array.from(
        [...rawData].map(
            (char) => char.charCodeAt(0)
        )
    );
}

async function getServiceWorkerRegistration() {

    if (!isPushSupported()) {
        throw new Error(
            "Trình duyệt không hỗ trợ Web Push"
        );
    }

    await navigator.serviceWorker.register(
        "/sw.js",
        {
            scope: "/"
        }
    );

    return navigator.serviceWorker.ready;
}

/*
 * Đăng ký subscription lên backend.
 */
async function sendSubscriptionToServer(
    subscription
) {

    if (!subscription) {
        return;
    }

    const json =
        subscription.toJSON();

    await api.post(
        "/api/doi-tac/push/subscribe",
        {
            endpoint:
            json.endpoint,

            keys: {
                p256dh:
                json.keys?.p256dh,

                auth:
                json.keys?.auth
            }
        }
    );
}

/*
 * User bấm "Bật thông báo".
 */
export async function enablePushNotifications() {

    if (!isPushSupported()) {
        throw new Error(
            "Trình duyệt hiện tại không hỗ trợ Web Push"
        );
    }

    console.log(
        "[PUSH] permission hiện tại:",
        Notification.permission
    );

    console.log(
        "[PUSH] origin:",
        window.location.origin
    );

    console.log(
        "[PUSH] secure:",
        window.isSecureContext
    );

    let permission =
        Notification.permission;

    /*
     * Chỉ request khi browser CHƯA hỏi.
     */
    if (permission === "default") {

        permission =
            await Notification.requestPermission();

        console.log(
            "[PUSH] permission sau khi hỏi:",
            permission
        );
    }

    /*
     * Nếu đã Block thì JS không thể tự bật lại.
     */
    if (permission === "denied") {

        throw new Error(
            "Bạn đã chặn thông báo An Yên. "
            + "Hãy vào quyền của website và chuyển Notifications thành Cho phép."
        );
    }

    if (permission !== "granted") {

        throw new Error(
            "Bạn chưa cho phép An Yên gửi thông báo"
        );
    }

    const registration =
        await getServiceWorkerRegistration();

    let subscription =
        await registration
            .pushManager
            .getSubscription();

    /*
     * Chưa có PushSubscription
     * thì tạo mới.
     */
    if (!subscription) {

        console.log(
            "[PUSH] Chưa có subscription, đang tạo..."
        );

        const response =
            await api.get(
                "/api/doi-tac/push/public-key"
            );

        const publicKey =
            response.data?.publicKey;

        if (!publicKey) {

            throw new Error(
                "Server chưa cấu hình VAPID Public Key"
            );
        }

        subscription =
            await registration
                .pushManager
                .subscribe({
                    userVisibleOnly: true,

                    applicationServerKey:
                        urlBase64ToUint8Array(
                            publicKey
                        )
                });

        console.log(
            "[PUSH] Đã tạo subscription:",
            subscription
        );
    } else {

        console.log(
            "[PUSH] Đã có subscription:",
            subscription
        );
    }

    /*
     * Lưu/chuyển subscription
     * sang tài khoản đối tác đang login.
     */
    await sendSubscriptionToServer(
        subscription
    );

    console.log(
        "[PUSH] Đồng bộ backend thành công"
    );

    return subscription;
}

/*
 * Khi user mở lại website:
 *
 * Nếu trước đó đã cấp permission
 * thì đồng bộ subscription lại backend.
 *
 * KHÔNG hiện permission popup.
 */
export async function syncPushSubscription() {

    if (!isPushSupported()) {
        return false;
    }

    console.log(
        "[PUSH-SYNC] permission:",
        Notification.permission
    );

    /*
     * Không tự bật popup khi load trang.
     */
    if (
        Notification.permission
        !== "granted"
    ) {
        return false;
    }

    const registration =
        await getServiceWorkerRegistration();

    let subscription =
        await registration
            .pushManager
            .getSubscription();

    /*
     * Permission vẫn granted
     * nhưng subscription đã bị unsubscribe
     * khi tài khoản trước logout.
     *
     * Tạo subscription mới.
     */
    if (!subscription) {

        console.log(
            "[PUSH-SYNC] Subscription không còn, tạo lại"
        );

        const response =
            await api.get(
                "/api/doi-tac/push/public-key"
            );

        const publicKey =
            response.data?.publicKey;

        if (!publicKey) {
            throw new Error(
                "Server chưa cấu hình VAPID Public Key"
            );
        }

        subscription =
            await registration
                .pushManager
                .subscribe({
                    userVisibleOnly: true,

                    applicationServerKey:
                        urlBase64ToUint8Array(
                            publicKey
                        )
                });
    }

    await sendSubscriptionToServer(
        subscription
    );

    console.log(
        "[PUSH-SYNC] Đồng bộ subscription thành công"
    );

    return true;
}

/*
 * LOGOUT thì bỏ subscription.
 *
 * Chỉ logout mới gọi.
 * Đóng browser tuyệt đối không gọi.
 */
export async function disablePushNotifications() {

    if (!isPushSupported()) {
        return;
    }

    const registration =
        await navigator
            .serviceWorker
            .getRegistration("/");

    if (!registration) {
        return;
    }

    const subscription =
        await registration
            .pushManager
            .getSubscription();

    if (!subscription) {
        return;
    }

    /*
     * Xóa trên backend trước vì lúc này
     * JWT vẫn còn trong localStorage.
     */
    try {

        await api.delete(
            "/api/doi-tac/push/unsubscribe",
            {
                data: {
                    endpoint:
                    subscription.endpoint
                }
            }
        );

    } catch (error) {

        console.error(
            "Lỗi unsubscribe backend:",
            error
        );
    }

    try {

        await subscription.unsubscribe();

    } catch (error) {

        console.error(
            "Lỗi unsubscribe browser:",
            error
        );
    }
}

export function getPushPermission() {

    if (!isPushSupported()) {
        return "unsupported";
    }

    return Notification.permission;
}

export {
    isPushSupported
};