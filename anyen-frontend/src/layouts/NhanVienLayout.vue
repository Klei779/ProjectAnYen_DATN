<template>
  <div class="container-fluid">
    <div class="row min-vh-100">
      <div class="col-auto p-0">
        <NhanVienSidebar />
      </div>

      <div
          class="col p-0"
          style="background-color: #f8f9fa; display: flex; flex-direction: column;"
      >
        <header class="page-topbar">
          <div class="topbar-left">
            <i class="fa-solid fa-bars"></i>
            <h2>{{ pageTitle }}</h2>
          </div>

          <div class="topbar-right">
            <div class="bell-wrapper" @click.stop="toggleMiniNoti">
              <i class="fa-regular fa-bell"></i>

              <span class="bell-badge" v-if="unreadCount > 0">
                {{ unreadCount }}
              </span>

              <div class="mini-noti-dropdown" v-if="showMiniNoti" @click.stop>
                <div class="mini-header">
                  <div class="header-title">
                    <h4>
                      Thông báo <span class="badge">{{ unreadCount }}</span>
                    </h4>

                    <button
                        class="mark-read-btn"
                        @click.stop="markAllAsRead"
                        :disabled="notifications.length === 0"
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
                      :class="{ unread: item.trangThai === 'CHUA_DOC' }"
                  >
                    <div class="mini-icon" :class="getMiniIconClass(item)">
                      <i :class="getMiniIconName(item)"></i>
                    </div>

                    <div class="mini-info">
                      <h5>{{ item.tieuDe || "Thông báo" }}</h5>
                      <p>{{ item.noiDung || "---" }}</p>
                      <small>{{ formatDateTime(item.ngayTao) }}</small>
                    </div>

                    <button class="mini-view-btn" @click="goToNotification(item)">
                      Xem
                    </button>
                  </div>

                  <div v-if="miniNotifications.length === 0" class="empty-mini">
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

            <div
                class="user-profile-wrapper"
                style="position: relative; cursor: pointer; margin-left: 20px"
                @click.stop="toggleProfile"
            >
              <div style="display: flex; align-items: center; gap: 10px;">
                <div class="avatar-small">
                  <i class="fa-solid fa-user"></i>
                </div>

                <div class="user-short-info">
                  <strong>{{ user?.hoTen || "Nhân viên" }}</strong>
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

        <div style="flex: 1;">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import api from "../api/api.js";
import NhanVienSidebar from "../components/NhanVienSidebar.vue";
import UserProfileDropdown from "../components/UserProfileDropdown.vue";

const route = useRoute();
const router = useRouter();

const user = ref(null);
const showProfile = ref(false);

const notifications = ref([]);
const showMiniNoti = ref(false);

let pollingInterval = null;
let notificationApiFailed = false;

const API_URL = "/api/nhan-vien/thong-bao";

const routeTitles = {
  "/nhan-vien/tong-quan": "Tổng quan",
  "/nhan-vien/quan-ly-doi-tac": "Quản lý đối tác",
  "/nhan-vien/quan-ly-don-hang": "Quản lý đơn hàng",
  "/nhan-vien/thong-bao": "Thông báo công việc",
  "/nhan-vien/thong-tin-tai-khoan": "Thông tin tài khoản",
  "/nhan-vien/quan-ly-khach-hang": "Quản lý khách hàng",
  "/nhan-vien/quan-ly-hop-dong": "Quản lý hợp đồng",
};

const pageTitle = computed(() => {
  return routeTitles[route.path] || "Trang Nhân Viên";
});

const unreadCount = computed(() => {
  return notifications.value.filter((item) => item.trangThai === "CHUA_DOC")
      .length;
});

const miniNotifications = computed(() => {
  return notifications.value.slice(0, 10);
});

const toggleMiniNoti = () => {
  showMiniNoti.value = !showMiniNoti.value;
};

const toggleProfile = () => {
  showProfile.value = !showProfile.value;
};

const handleDocumentClick = () => {
  if (showMiniNoti.value) {
    showMiniNoti.value = false;
  }

  if (showProfile.value) {
    showProfile.value = false;
  }
};

const formatDateTime = (value) => {
  if (!value) return "";

  try {
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return date.toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return value;
  }
};

const loadNotifications = async () => {
  if (notificationApiFailed) {
    return;
  }

  try {
    const res = await api.get(API_URL);

    notifications.value = Array.isArray(res.data) ? res.data : [];

    notificationApiFailed = false;
  } catch (error) {
    notifications.value = [];

    notificationApiFailed = true;
    stopPolling();

    console.warn(
        "Tạm tắt load thông báo vì API chưa kết nối được:",
        error.message
    );
  }
};

const startPolling = () => {
  stopPolling();

  pollingInterval = setInterval(() => {
    loadNotifications();
  }, 30000);
};

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
};

const markAllAsRead = async () => {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);

    notifications.value.forEach((item) => {
      if (item.trangThai === "CHUA_DOC") {
        item.trangThai = "DA_DOC";
      }
    });
  } catch (error) {
    console.warn("Không thể đánh dấu đã đọc:", error.message);
  }
};

const goToNotification = () => {
  showMiniNoti.value = false;
  router.push("/nhan-vien/thong-bao");
};

const goToAllNotifications = () => {
  showMiniNoti.value = false;
  router.push("/nhan-vien/thong-bao");
};

const getMiniIconClass = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "bg-red";
  if (item.loaiThongBao === "HE_THONG") return "bg-blue";
  if (item.loaiThongBao === "TU_CHOI") return "bg-yellow";
  if (item.trangThai === "DA_CHAP_NHAN") return "bg-green";

  return "bg-purple";
};

const getMiniIconName = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "fa-solid fa-briefcase";
  if (item.loaiThongBao === "HE_THONG") return "fa-solid fa-gear";
  if (item.loaiThongBao === "TU_CHOI") return "fa-solid fa-xmark";
  if (item.trangThai === "DA_CHAP_NHAN") return "fa-solid fa-check";

  return "fa-solid fa-bell";
};

const logout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");

  window.dispatchEvent(new Event("session-updated"));

  router.push("/");
};

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);

  const userStr = localStorage.getItem("user");

  if (userStr) {
    try {
      user.value = JSON.parse(userStr);
    } catch {
      user.value = null;
    }
  }

  loadNotifications();
  startPolling();
});

onUnmounted(() => {
  document.removeEventListener("click", handleDocumentClick);
  stopPolling();
});
</script>

<style scoped src="../assets/styles/layouts/Layout.css"></style>