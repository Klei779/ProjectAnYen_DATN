<template>
  <div class="doi-tac-layout">
    <aside class="doi-tac-sidebar">
      <DoiTacSidebar />
    </aside>

    <section class="doi-tac-main">
      <header class="page-topbar">
        <div class="topbar-left">
          <i class="fa-solid fa-bars"></i>
          <h2>{{ pageTitle }}</h2>
        </div>

        <div class="topbar-right">
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

          <!-- Tài khoản -->
          <div
              class="user-profile-wrapper"
              @click.stop="toggleProfile"
          >
            <div class="user-profile-trigger">
              <div class="avatar-small">
                <i class="fa-solid fa-user"></i>
              </div>

              <div class="user-short-info">
                <strong>
                  {{ user?.hoTen || user?.tenDoiTac || "Đối tác" }}
                </strong>
              </div>
            </div>

            <UserProfileDropdown
                v-if="showProfile"
                :user="user"
                icon-class="fa-solid fa-user"
                @logout="logout"
            />
          </div>
        </div>
      </header>

      <main class="doi-tac-content">
        <router-view />
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
import { useRoute, useRouter } from "vue-router";
import api from "../api/api.js";
import DoiTacSidebar from "../components/DoiTacSidebar.vue";
import UserProfileDropdown from "../components/UserProfileDropdown.vue";

const route = useRoute();
const router = useRouter();

const user = ref(null);
const showProfile = ref(false);
const notifications = ref([]);
const showMiniNoti = ref(false);

let pollingInterval = null;

const API_URL = "/api/doi-tac/thong-bao";

const routeTitles = {
  "/doi-tac/tong-quan": "Tổng quan",
  "/doi-tac/quan-ly-san-pham": "Quản lý sản phẩm",
  "/doi-tac/quan-ly-combo": "Quản lý combo",
  "/doi-tac/tao-combo": "Tạo combo",
  "/doi-tac/tao-san-pham": "Tạo sản phẩm",
  "/doi-tac/quan-ly-don-hang": "Quản lý đơn hàng",
  "/doi-tac/thong-bao": "Thông báo",
  "/doi-tac/thong-ke-doanh-thu": "Thống kê doanh thu",
  "/doi-tac/thong-tin-tai-khoan": "Thông tin tài khoản",
  "/doi-tac/doi-mat-khau": "Đổi mật khẩu"
};

const pageTitle = computed(() => {
  return routeTitles[route.path] || "Trang đối tác";
});

const unreadCount = computed(() => {
  return notifications.value.filter((item) => {
    const status = Number(item.trangThai);

    return status === 0 || status === 4;
  }).length;
});

const miniNotifications = computed(() => {
  return notifications.value.slice(0, 10);
});

function toggleMiniNoti() {
  showMiniNoti.value = !showMiniNoti.value;
  showProfile.value = false;
}

function toggleProfile() {
  showProfile.value = !showProfile.value;
  showMiniNoti.value = false;
}

async function loadNotifications() {
  try {
    const response = await api.get(API_URL);

    notifications.value = Array.isArray(response.data)
        ? response.data
        : [];
  } catch (error) {
    console.error("Lỗi tải thông báo đối tác:", error);
  }
}

function startPolling() {
  stopPolling();

  pollingInterval = window.setInterval(() => {
    loadNotifications();
  }, 5000);
}

function stopPolling() {
  if (pollingInterval !== null) {
    window.clearInterval(pollingInterval);
    pollingInterval = null;
  }
}

async function markAllAsRead() {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);

    notifications.value.forEach((notification) => {
      const status = Number(notification.trangThai);

      if (status === 0 || status === 4) {
        notification.trangThai = 1;
      }
    });
  } catch (error) {
    console.error("Lỗi đánh dấu thông báo:", error);
  }
}

function goToNotification() {
  showMiniNoti.value = false;
  router.push("/doi-tac/thong-bao");
}

function goToAllNotifications() {
  showMiniNoti.value = false;
  router.push("/doi-tac/thong-bao");
}

function getMiniIconClass(item) {
  if (item.loaiThongBao === "CONG_VIEC") {
    return "bg-red";
  }

  if (item.loaiThongBao === "HE_THONG") {
    return "bg-blue";
  }

  if (item.loaiThongBao === "TU_CHOI") {
    return "bg-yellow";
  }

  if (Number(item.trangThai) === 2) {
    return "bg-green";
  }

  return "bg-purple";
}

function getMiniIconName(item) {
  if (item.loaiThongBao === "CONG_VIEC") {
    return "fa-solid fa-briefcase";
  }

  if (item.loaiThongBao === "HE_THONG") {
    return "fa-solid fa-gear";
  }

  if (item.loaiThongBao === "TU_CHOI") {
    return "fa-solid fa-xmark";
  }

  if (Number(item.trangThai) === 2) {
    return "fa-solid fa-check";
  }

  return "fa-solid fa-bell";
}

function formatDateTime(dateStr) {
  if (!dateStr) {
    return "";
  }

  const date = new Date(dateStr);

  if (Number.isNaN(date.getTime())) {
    return dateStr;
  }

  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric"
  }).format(date);
}

function handleDocumentClick() {
  showMiniNoti.value = false;
  showProfile.value = false;
}

function loadUserFromStorage() {
  const userStr = localStorage.getItem("user");

  if (!userStr) {
    user.value = null;
    return;
  }

  try {
    user.value = JSON.parse(userStr);
  } catch (error) {
    console.error("Dữ liệu tài khoản không hợp lệ:", error);
    user.value = null;
  }
}

function logout() {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");

  window.dispatchEvent(new Event("session-updated"));

  router.push("/");
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);

  loadUserFromStorage();
  loadNotifications();
  startPolling();
});

onUnmounted(() => {
  document.removeEventListener("click", handleDocumentClick);
  stopPolling();
});
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
</style>