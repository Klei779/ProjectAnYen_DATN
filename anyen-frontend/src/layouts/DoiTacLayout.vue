<template>
  <div class="doi-tac-layout">
    <aside class="doi-tac-sidebar">
      <DoiTacSidebar/>
    </aside>

    <section class="doi-tac-main">
      <header class="page-topbar">
        <div class="topbar-left">
          <i class="fa-solid fa-bars"></i>
          <h2>{{ pageTitle }}</h2>
        </div>

        <div class="topbar-right">

          <!-- BẬT PUSH NOTIFICATION -->
          <button
              v-if="pushSupported && !pushEnabled"
              type="button"
              class="enable-push-btn"
              @click.stop="handleEnablePush"
          >
            <i class="fa-solid fa-bell"></i>
            Bật thông báo
          </button>

          <!-- ĐÃ BẬT PUSH -->
          <span
              v-else-if="pushSupported && pushEnabled"
              class="push-enabled"
          >
              <i class="fa-solid fa-circle-check"></i>
              Đã bật thông báo
          </span>

          <!-- Thông báo -->
          <div class="bell-wrapper" @click.stop="toggleMiniNoti">
            <i class="fa-regular fa-bell"></i>

            <span
                v-if="unreadCount > 0"
                class="bell-badge"
            >
              {{ unreadCount }}
            </span>

            <div
                v-if="showMiniNoti"
                class="mini-noti-dropdown"
                @click.stop
            >
              <div class="mini-header">
                <div class="header-title">
                  <h4>
                    Thông báo
                    <span class="badge">{{ unreadCount }}</span>
                  </h4>

                  <button
                      class="mark-read-btn"
                      :disabled="notifications.length === 0"
                      @click.stop="markAllAsRead"
                  >
                    Đánh dấu tất cả đã đọc
                  </button>
                </div>

                <button
                    class="close-mini-btn"
                    @click.stop="showMiniNoti = false"
                >
                  <i class="fa-solid fa-xmark"></i>
                </button>
              </div>

              <div class="mini-list">
                <div
                    v-for="item in miniNotifications"
                    :key="item.maThongBao"
                    class="mini-item"
                    :class="{
                    unread:
                      Number(item.trangThai) === 0 ||
                      Number(item.trangThai) === 4
                  }"
                >
                  <div
                      class="mini-icon"
                      :class="getMiniIconClass(item)"
                  >
                    <i :class="getMiniIconName(item)"></i>
                  </div>

                  <div class="mini-info">
                    <h5>{{ item.tieuDe || "Thông báo" }}</h5>
                    <p>{{ item.noiDung || "---" }}</p>
                    <small>{{ formatDateTime(item.ngayTao) }}</small>
                  </div>

                  <button
                      class="mini-view-btn"
                      @click="goToNotification"
                  >
                    Xem
                  </button>
                </div>

                <div
                    v-if="miniNotifications.length === 0"
                    class="empty-mini"
                >
                  Không có thông báo mới
                </div>
              </div>

              <div class="mini-footer">
                <button @click="goToAllNotifications">
                  Xem tất cả thông báo
                </button>
              </div>
            </div>
          </div>

        </div>
      </header>

      <main class="doi-tac-content">
        <router-view/>
      </main>
    </section>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  onMounted,
  onUnmounted
} from "vue";

import {
  useRoute,
  useRouter
} from "vue-router";

import api from "../api/api.js";

import DoiTacSidebar
  from "../components/DoiTacSidebar.vue";

import UserProfileDropdown
  from "../components/UserProfileDropdown.vue";

import {
  ElMessage
} from "element-plus";


// ======================================================
// PUSH NOTIFICATION
// GỘP TRỰC TIẾP VÀO DoiTacLayout.vue
// ======================================================

const pushPermission = ref(
    "Notification" in window
        ? Notification.permission
        : "unsupported"
);

/*
 * Không chỉ kiểm tra permission.
 *
 * Biến này chỉ true khi browser
 * thực sự có PushSubscription.
 */
const pushSubscriptionActive =
    ref(false);


// ======================================================
// KIỂM TRA TRÌNH DUYỆT CÓ HỖ TRỢ PUSH
// ======================================================

function isPushSupported() {

  return (
      "serviceWorker" in navigator
      &&
      "PushManager" in window
      &&
      "Notification" in window
  );
}

const pushSupported =
    isPushSupported();


// ======================================================
// LẤY PERMISSION
// ======================================================

function getPushPermission() {

  if (!("Notification" in window)) {
    return "unsupported";
  }

  return Notification.permission;
}


// ======================================================
// TRẠNG THÁI HIỂN THỊ "ĐÃ BẬT"
// ======================================================

const pushEnabled =
    computed(() => {

      return (
          pushPermission.value === "granted"
          &&
          pushSubscriptionActive.value
      );
    });


// ======================================================
// BASE64 URL -> Uint8Array
//
// VAPID public key từ backend phải chuyển
// thành Uint8Array trước khi truyền cho
// pushManager.subscribe()
// ======================================================

function urlBase64ToUint8Array(
    base64String
) {

  const padding =
      "=".repeat(
          (4 - base64String.length % 4) % 4
      );

  const base64 =
      (
          base64String
          + padding
      )
          .replace(/-/g, "+")
          .replace(/_/g, "/");

  const rawData =
      window.atob(base64);

  const outputArray =
      new Uint8Array(
          rawData.length
      );

  for (
      let i = 0;
      i < rawData.length;
      i++
  ) {

    outputArray[i] =
        rawData.charCodeAt(i);
  }

  return outputArray;
}


// ======================================================
// SO SÁNH APPLICATION SERVER KEY
//
// Dùng để phát hiện trường hợp:
// Browser subscribe bằng VAPID key cũ
// nhưng backend đã đổi sang VAPID key mới.
// ======================================================

function applicationServerKeyEquals(
    subscription,
    publicKeyArray
) {

  const currentKey =
      subscription
          ?.options
          ?.applicationServerKey;

  if (!currentKey) {

    /*
     * Browser không expose key.
     * Không kết luận là sai.
     */
    return true;
  }

  const currentArray =
      new Uint8Array(
          currentKey
      );

  if (
      currentArray.length
      !==
      publicKeyArray.length
  ) {

    return false;
  }

  for (
      let i = 0;
      i < currentArray.length;
      i++
  ) {

    if (
        currentArray[i]
        !==
        publicKeyArray[i]
    ) {

      return false;
    }
  }

  return true;
}


// ======================================================
// REGISTER SERVICE WORKER
// ======================================================

async function getServiceWorkerRegistration() {

  if (!pushSupported) {

    throw new Error(
        "Trình duyệt không hỗ trợ Web Push"
    );
  }

  /*
   * localhost được browser xem là
   * secure context khi develop.
   */
  if (!window.isSecureContext) {

    throw new Error(
        "Web Push yêu cầu HTTPS hoặc localhost"
    );
  }

  console.log(
      "[PUSH] Register /sw.js"
  );

  const registration =
      await navigator
          .serviceWorker
          .register(
              "/sw.js",
              {
                scope: "/",

                /*
                 * Khi develop, tránh lấy
                 * sw.js cũ từ HTTP cache.
                 */
                updateViaCache:
                    "none"
              }
          );

  console.log(
      "[PUSH] Service Worker registered:",
      registration
  );

  const readyRegistration =
      await navigator
          .serviceWorker
          .ready;

  console.log(
      "[PUSH] Service Worker ready:",
      readyRegistration
  );

  return readyRegistration;
}


// ======================================================
// LẤY VAPID PUBLIC KEY TỪ BACKEND
// ======================================================

async function getVapidPublicKey() {

  console.log(
      "[PUSH] Đang lấy VAPID public key"
  );

  const response =
      await api.get(
          "/api/doi-tac/push/public-key"
      );

  const publicKey =
      response
          ?.data
          ?.publicKey;

  if (
      !publicKey
      ||
      !String(publicKey).trim()
  ) {

    throw new Error(
        "Backend chưa cấu hình VAPID public key"
    );
  }

  console.log(
      "[PUSH] Đã lấy VAPID public key"
  );

  return String(
      publicKey
  ).trim();
}


// ======================================================
// GỬI SUBSCRIPTION LÊN BACKEND
// ======================================================

async function sendSubscriptionToServer(
    subscription
) {

  if (!subscription) {

    throw new Error(
        "PushSubscription không tồn tại"
    );
  }

  const json =
      subscription.toJSON();

  console.log(
      "[PUSH] Gửi subscription lên backend:",
      {
        endpoint:
        json.endpoint,

        hasP256dh:
            Boolean(
                json.keys?.p256dh
            ),

        hasAuth:
            Boolean(
                json.keys?.auth
            )
      }
  );

  if (
      !json.endpoint
      ||
      !json.keys?.p256dh
      ||
      !json.keys?.auth
  ) {

    throw new Error(
        "PushSubscription thiếu endpoint hoặc keys"
    );
  }

  const response =
      await api.post(
          "/api/doi-tac/push/subscribe",
          {
            endpoint:
            json.endpoint,

            keys: {

              p256dh:
              json.keys.p256dh,

              auth:
              json.keys.auth
            }
          }
      );

  console.log(
      "[PUSH] Backend lưu subscription thành công:",
      response.data
  );

  return response.data;
}


// ======================================================
// TẠO / ĐỒNG BỘ PUSH SUBSCRIPTION
// ======================================================

async function createOrSyncSubscription() {

  const registration =
      await getServiceWorkerRegistration();

  const vapidPublicKey =
      await getVapidPublicKey();

  const applicationServerKey =
      urlBase64ToUint8Array(
          vapidPublicKey
      );

  let subscription =
      await registration
          .pushManager
          .getSubscription();

  console.log(
      "[PUSH] Subscription hiện tại:",
      subscription
  );


  /*
   * Nếu browser đã subscribe bằng
   * VAPID public key cũ
   * => xóa subscription cũ.
   */
  if (
      subscription
      &&
      !applicationServerKeyEquals(
          subscription,
          applicationServerKey
      )
  ) {

    console.warn(
        "[PUSH] VAPID key đã thay đổi. Xóa subscription cũ."
    );

    await subscription.unsubscribe();

    subscription = null;
  }


  /*
   * Chưa subscribe
   * => tạo subscription mới.
   */
  if (!subscription) {

    console.log(
        "[PUSH] Đang tạo PushSubscription..."
    );

    subscription =
        await registration
            .pushManager
            .subscribe({

              userVisibleOnly:
                  true,

              applicationServerKey:
              applicationServerKey
            });

    console.log(
        "[PUSH] Tạo PushSubscription thành công:",
        subscription
    );

  } else {

    console.log(
        "[PUSH] Đang dùng PushSubscription hiện tại"
    );
  }


  /*
   * Luôn sync lên backend.
   *
   * Nhờ vậy nếu DB mất row nhưng
   * browser vẫn có subscription,
   * reload trang sẽ tự lưu lại.
   */
  await sendSubscriptionToServer(
      subscription
  );

  pushSubscriptionActive.value =
      true;

  return subscription;
}


// ======================================================
// USER BẤM "BẬT THÔNG BÁO"
// ======================================================

async function enablePushNotifications() {

  if (!pushSupported) {

    throw new Error(
        "Trình duyệt không hỗ trợ Web Push"
    );
  }

  console.log(
      "[PUSH] Permission hiện tại:",
      Notification.permission
  );


  let permission =
      Notification.permission;


  if (permission === "default") {

    permission =
        await Notification
            .requestPermission();
  }


  pushPermission.value =
      permission;


  if (permission === "denied") {

    throw new Error(
        "Bạn đã chặn quyền thông báo. Hãy bật lại trong cài đặt trình duyệt."
    );
  }


  if (permission !== "granted") {

    throw new Error(
        "Bạn chưa cấp quyền thông báo"
    );
  }


  return await createOrSyncSubscription();
}


// ======================================================
// SYNC KHI RELOAD TRANG
// ======================================================

async function syncPushSubscription() {

  if (!pushSupported) {

    pushSubscriptionActive.value =
        false;

    return null;
  }


  pushPermission.value =
      Notification.permission;


  if (
      Notification.permission
      !==
      "granted"
  ) {

    pushSubscriptionActive.value =
        false;

    return null;
  }


  try {

    const subscription =
        await createOrSyncSubscription();

    pushSubscriptionActive.value =
        Boolean(subscription);

    return subscription;

  } catch (error) {

    pushSubscriptionActive.value =
        false;

    throw error;
  }
}


// ======================================================
// TẮT PUSH
// ======================================================

async function disablePushNotifications() {

  if (!pushSupported) {

    return;
  }


  const registration =
      await navigator
          .serviceWorker
          .ready;


  const subscription =
      await registration
          .pushManager
          .getSubscription();


  if (!subscription) {

    pushSubscriptionActive.value =
        false;

    return;
  }


  const endpoint =
      subscription.endpoint;


  /*
   * Xóa subscription trong DB trước.
   */
  try {

    await api.delete(
        "/api/doi-tac/push/unsubscribe",
        {
          data: {
            endpoint:
            endpoint
          }
        }
    );

    console.log(
        "[PUSH] Đã xóa subscription khỏi backend"
    );

  } catch (error) {

    console.error(
        "[PUSH] Không xóa được subscription backend:",
        error
    );
  }


  /*
   * Sau đó unsubscribe browser.
   */
  try {

    await subscription
        .unsubscribe();

    console.log(
        "[PUSH] Browser đã unsubscribe"
    );

  } catch (error) {

    console.error(
        "[PUSH] Browser unsubscribe lỗi:",
        error
    );
  }


  pushSubscriptionActive.value =
      false;
}


// ======================================================
// BUTTON BẬT PUSH
// ======================================================

async function handleEnablePush() {

  try {

    console.log(
        "========================================"
    );

    console.log(
        "[PUSH] BẮT ĐẦU BẬT PUSH"
    );

    console.log(
        "[PUSH] permission trước:",
        Notification.permission
    );

    console.log(
        "[PUSH] origin:",
        location.origin
    );

    console.log(
        "[PUSH] secure context:",
        window.isSecureContext
    );


    const subscription =
        await enablePushNotifications();


    console.log(
        "[PUSH] subscription:",
        subscription
    );

    console.log(
        "[PUSH] endpoint:",
        subscription?.endpoint
    );

    console.log(
        "[PUSH] permission sau:",
        Notification.permission
    );


    pushPermission.value =
        getPushPermission();

    pushSubscriptionActive.value =
        Boolean(subscription);


    ElMessage.success(
        "Đã bật thông báo An Yên trên thiết bị"
    );


    console.log(
        "[PUSH] BẬT PUSH THÀNH CÔNG"
    );

    console.log(
        "========================================"
    );

  } catch (error) {

    console.error(
        "[PUSH] KHÔNG BẬT ĐƯỢC PUSH:",
        error
    );

    console.log(
        "[PUSH] permission hiện tại:",
        "Notification" in window
            ? Notification.permission
            : "unsupported"
    );


    pushPermission.value =
        getPushPermission();

    pushSubscriptionActive.value =
        false;


    ElMessage.error(
        error?.response?.data?.message
        ||
        error?.message
        ||
        "Không bật được thông báo"
    );
  }
}


// ======================================================
// ROUTER / LAYOUT
// ======================================================

const route =
    useRoute();

const router =
    useRouter();

const user =
    ref(null);

const showProfile =
    ref(false);

const notifications =
    ref([]);

const showMiniNoti =
    ref(false);

let pollingInterval =
    null;

const API_URL =
    "/api/doi-tac/thong-bao";


const routeTitles = {

  "/doi-tac/tong-quan":
      "Tổng quan",

  "/doi-tac/quan-ly-san-pham":
      "Quản lý sản phẩm",

  "/doi-tac/quan-ly-combo":
      "Quản lý combo",

  "/doi-tac/tao-combo":
      "Tạo combo",

  "/doi-tac/tao-san-pham":
      "Tạo sản phẩm",

  "/doi-tac/quan-ly-don-hang":
      "Quản lý đơn hàng",

  "/doi-tac/thong-bao":
      "Thông báo",

  "/doi-tac/thong-ke-doanh-thu":
      "Thống kê doanh thu",

  "/doi-tac/thong-tin-tai-khoan":
      "Thông tin tài khoản",

  "/doi-tac/doi-mat-khau":
      "Đổi mật khẩu"
};


const pageTitle =
    computed(() => {

      return (
          routeTitles[route.path]
          ||
          "Trang đối tác"
      );
    });


const unreadCount =
    computed(() => {

      return notifications.value
          .filter(
              (item) => {

                const status =
                    Number(
                        item.trangThai
                    );

                return (
                    status === 0
                    ||
                    status === 4
                );
              }
          )
          .length;
    });


const miniNotifications =
    computed(() => {

      return notifications.value
          .slice(
              0,
              10
          );
    });


function toggleMiniNoti() {

  showMiniNoti.value =
      !showMiniNoti.value;

  showProfile.value =
      false;
}


function toggleProfile() {

  showProfile.value =
      !showProfile.value;

  showMiniNoti.value =
      false;
}


// ======================================================
// LOAD THÔNG BÁO TRONG WEB
// ======================================================

async function loadNotifications() {

  try {

    const response =
        await api.get(
            API_URL
        );


    notifications.value =
        Array.isArray(
            response.data
        )
            ? response.data
            : [];

  } catch (error) {

    console.error(
        "Lỗi tải thông báo đối tác:",
        error
    );
  }
}


// ======================================================
// POLLING
// ======================================================

function startPolling() {

  stopPolling();


  pollingInterval =
      window.setInterval(
          () => {

            loadNotifications();

          },
          5000
      );
}


function stopPolling() {

  if (
      pollingInterval
      !==
      null
  ) {

    window.clearInterval(
        pollingInterval
    );

    pollingInterval =
        null;
  }
}


// ======================================================
// ĐÁNH DẤU ĐÃ ĐỌC
// ======================================================

async function markAllAsRead() {

  try {

    await api.put(
        `${API_URL}/da-doc-tat-ca`
    );


    notifications.value
        .forEach(
            (notification) => {

              const status =
                  Number(
                      notification.trangThai
                  );


              if (
                  status === 0
                  ||
                  status === 4
              ) {

                notification.trangThai =
                    1;
              }
            }
        );

  } catch (error) {

    console.error(
        "Lỗi đánh dấu thông báo:",
        error
    );
  }
}


// ======================================================
// ĐI TỚI TRANG THÔNG BÁO
// ======================================================

function goToNotification() {

  showMiniNoti.value =
      false;

  router.push(
      "/doi-tac/thong-bao"
  );
}


function goToAllNotifications() {

  showMiniNoti.value =
      false;

  router.push(
      "/doi-tac/thong-bao"
  );
}


// ======================================================
// ICON THÔNG BÁO
// ======================================================

function getMiniIconClass(
    item
) {

  if (
      item.loaiThongBao
      ===
      "CONG_VIEC"
  ) {

    return "bg-red";
  }


  if (
      item.loaiThongBao
      ===
      "HE_THONG"
  ) {

    return "bg-blue";
  }


  if (
      item.loaiThongBao
      ===
      "TU_CHOI"
  ) {

    return "bg-yellow";
  }


  if (
      Number(
          item.trangThai
      )
      ===
      2
  ) {

    return "bg-green";
  }


  return "bg-purple";
}


function getMiniIconName(
    item
) {

  if (
      item.loaiThongBao
      ===
      "CONG_VIEC"
  ) {

    return "fa-solid fa-briefcase";
  }


  if (
      item.loaiThongBao
      ===
      "HE_THONG"
  ) {

    return "fa-solid fa-gear";
  }


  if (
      item.loaiThongBao
      ===
      "TU_CHOI"
  ) {

    return "fa-solid fa-xmark";
  }


  if (
      Number(
          item.trangThai
      )
      ===
      2
  ) {

    return "fa-solid fa-check";
  }


  return "fa-solid fa-bell";
}


// ======================================================
// FORMAT DATE
// ======================================================

function formatDateTime(
    dateStr
) {

  if (!dateStr) {

    return "";
  }


  const date =
      new Date(
          dateStr
      );


  if (
      Number.isNaN(
          date.getTime()
      )
  ) {

    return dateStr;
  }


  return new Intl.DateTimeFormat(
      "vi-VN",
      {
        hour:
            "2-digit",

        minute:
            "2-digit",

        day:
            "2-digit",

        month:
            "2-digit",

        year:
            "numeric"
      }
  ).format(
      date
  );
}


// ======================================================
// CLICK NGOÀI
// ======================================================

function handleDocumentClick() {

  showMiniNoti.value =
      false;

  showProfile.value =
      false;
}


// ======================================================
// USER STORAGE
// ======================================================

function loadUserFromStorage() {

  const userStr =
      localStorage.getItem(
          "user"
      );


  if (!userStr) {

    user.value =
        null;

    return;
  }


  try {

    user.value =
        JSON.parse(
            userStr
        );

  } catch (error) {

    console.error(
        "Dữ liệu tài khoản không hợp lệ:",
        error
    );

    user.value =
        null;
  }
}


// ======================================================
// LOGOUT
// ======================================================

async function logout() {

  try {

    /*
     * Khi logout:
     * xóa subscription tài khoản
     * khỏi backend.
     */
    await disablePushNotifications();

  } catch (error) {

    console.error(
        "Lỗi tắt Push khi đăng xuất:",
        error
    );
  }


  localStorage.removeItem(
      "user"
  );

  localStorage.removeItem(
      "token"
  );

  localStorage.removeItem(
      "loaiTaiKhoan"
  );

  localStorage.removeItem(
      "tenDangNhap"
  );

  localStorage.removeItem(
      "id"
  );


  window.dispatchEvent(
      new Event(
          "session-updated"
      )
  );


  router.push(
      "/"
  );
}


// ======================================================
// MOUNTED
// ======================================================

onMounted(
    async () => {

      document.addEventListener(
          "click",
          handleDocumentClick
      );


      loadUserFromStorage();

      loadNotifications();

      startPolling();


      console.log(
          "[PUSH] Browser support:",
          pushSupported
      );


      if (pushSupported) {

        console.log(
            "[PUSH] Permission:",
            Notification.permission
        );

        console.log(
            "[PUSH] Secure context:",
            window.isSecureContext
        );
      }


      /*
       * Nếu user trước đó đã cho phép:
       * tự sync browser subscription
       * với DB backend.
       */
      if (
          pushSupported
          &&
          Notification.permission
          ===
          "granted"
      ) {

        try {

          const subscription =
              await syncPushSubscription();


          console.log(
              "[PUSH] Sync khi mounted:",
              subscription
          );


          pushPermission.value =
              getPushPermission();

          pushSubscriptionActive.value =
              Boolean(subscription);

        } catch (error) {

          console.error(
              "[PUSH] Lỗi đồng bộ Push:",
              error
          );

          pushSubscriptionActive.value =
              false;
        }
      }
    }
);


// ======================================================
// UNMOUNTED
// ======================================================

onUnmounted(
    () => {

      document.removeEventListener(
          "click",
          handleDocumentClick
      );

      stopPolling();
    }
);
</script>

<style scoped src="../assets/styles/layouts/Layout.css"></style>

<style scoped>
.doi-tac-layout {
  display: flex;
  flex-wrap: nowrap;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background: #f8f9fa;
}

.doi-tac-sidebar {
  flex: 0 0 265px;
  width: 265px;
  min-width: 265px;
  max-width: 265px;
  min-height: 100vh;
  background: #ffffff;
  position: relative;
  z-index: 20;
}

.doi-tac-main {
  flex: 1 1 0;
  width: 0;
  min-width: 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.page-topbar {
  flex: 0 0 auto;
  width: 100%;
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: relative;
  z-index: 15;
}

.topbar-left,
.topbar-right,
.user-profile-trigger {
  display: flex;
  align-items: center;
}

.topbar-left {
  gap: 14px;
}

.topbar-left h2 {
  margin: 0;
  color: #1f2937;
  font-size: 22px;
  font-weight: 700;
}

.topbar-right {
  gap: 18px;
}

.user-profile-wrapper {
  position: relative;
  cursor: pointer;
}

.user-profile-trigger {
  gap: 10px;
}

.doi-tac-content {
  flex: 1 1 auto;
  width: 100%;
  min-width: 0;
  min-height: 0;
  display: block;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
}

.doi-tac-content :deep(.combo-page) {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  width: 100% !important;
  min-width: 0 !important;
}

@media (max-width: 992px) {
  .doi-tac-sidebar {
    flex-basis: 220px;
    width: 220px;
    min-width: 220px;
    max-width: 220px;
  }

  .page-topbar {
    padding: 0 18px;
  }
}

.enable-push-btn {
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
}

.enable-push-btn i {
  margin-right: 5px;
}

.push-enabled {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
}
</style>