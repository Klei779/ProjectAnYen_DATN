<template>
  <div class="container-fluid">
    <div class="row min-vh-100">

      <div class="col-auto p-0">
        <HotlineSidebar />
      </div>

      <!-- Nội dung -->
      <div class="col p-0" style="background-color: #f8f9fa; display: flex; flex-direction: column;">
        <header class="page-topbar">
          <div class="topbar-left">
            <i class="fa-solid fa-bars"></i>
            <h2>{{ pageTitle }}</h2>
          </div>
          
          <div class="topbar-right">
            <div class="bell-wrapper" @click.stop="toggleMiniNoti">
              <i class="fa-regular fa-bell"></i>
              <span class="bell-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>

              <!-- Mini Notification Dropdown -->
              <div class="mini-noti-dropdown" v-if="showMiniNoti" @click.stop>
                <div class="mini-header">
                  <div class="header-title">
                     <h4>Thông báo <span class="badge">{{ unreadCount }}</span></h4>
                     <button class="mark-read-btn" @click.stop="markAllAsRead">Đánh dấu tất cả đã đọc</button>
                  </div>
                  <button class="close-mini-btn" @click.stop="showMiniNoti = false">
                    <i class="fa-solid fa-xmark"></i>
                  </button>
                </div>

                <div class="mini-list">
                  <div v-for="item in miniNotifications" :key="item.maThongBao" class="mini-item" :class="{'unread': Number(item.trangThai) === 0 || Number(item.trangThai) === 4}">
                    <div class="mini-icon" :class="getMiniIconClass(item)">
                      <i :class="getMiniIconName(item)"></i>
                    </div>
                    <div class="mini-info">
                      <h5>{{ item.tieuDe }}</h5>
                      <p>{{ item.noiDung }}</p>
                      <small>{{ item.ngayTao }}</small>
                    </div>
                    <button class="mini-view-btn" @click="goToNotification(item)">Xem</button>
                  </div>
                  <div v-if="miniNotifications.length === 0" class="empty-mini">Không có thông báo mới</div>
                </div>

                <div class="mini-footer">
                  <button @click="goToAllNotifications">Xem tất cả thông báo</button>
                </div>
              </div>
            </div>

            <div class="user-profile-wrapper" style="position: relative; cursor: pointer; margin-left: 20px" @click.stop="toggleProfile">
              <div style="display: flex; align-items: center; gap: 10px;">
                <div class="avatar-small">
                  <i class="fa-solid fa-user"></i>
                </div>
                <div class="user-short-info">
                  <strong>{{ user?.hoTen || 'Hotline' }}</strong>
                </div>
              </div>
              <UserProfileDropdown 
                v-if="showProfile" 
                :user="user" 
                icon-class="bi bi-headset"
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
import HotlineSidebar from "../components/HotlineSidebar.vue";
import UserProfileDropdown from "../components/UserProfileDropdown.vue";
import { heartbeatStaffChat, markStaffOffline } from "../services/tuVanService.js";

const route = useRoute();
const router = useRouter();

const user = ref(null);
const showProfile = ref(false);

const routeTitles = {
  "/hotline/quan-ly-cong-viec": "Quản lý công việc",
  "/hotline/quan-ly-don-hang": "Quản lý đơn hàng",
  "/hotline/nhan-tin": "Tin nhắn tư vấn",
  "/hotline/thong-bao": "Thông báo",
  "/hotline/thong-tin-tai-khoan": "Thông tin cá nhân",
};

const pageTitle = computed(() => {
  return routeTitles[route.path] || "Trang Hotline";
});

// Notifications Logic
const notifications = ref([]);
const showMiniNoti = ref(false);
let pollingInterval = null;
let presenceInterval = null;
const API_URL = "/api/nhan-vien/thong-bao";

const unreadCount = computed(() => {
  return notifications.value.filter(item => Number(item.trangThai) === 0 || Number(item.trangThai) === 4).length;
});

const miniNotifications = computed(() => {
  return notifications.value.slice(0, 10);
});

const toggleMiniNoti = () => {
  showMiniNoti.value = !showMiniNoti.value;
};

const loadNotifications = async () => {
  try {
    const res = await api.get(API_URL);
    notifications.value = res.data;
  } catch (error) {
    console.error("Lỗi load thông báo:", error);
  }
};

const startPolling = () => {
  pollingInterval = setInterval(() => {
    loadNotifications();
  }, 5000);
};

const stopPolling = () => {
  if (pollingInterval) clearInterval(pollingInterval);
};

const updatePresence = async () => {
  try {
    await heartbeatStaffChat();
  } catch (error) {
    console.error("Không thể cập nhật trạng thái online:", error);
  }
};

const startPresenceHeartbeat = () => {
  updatePresence();
  presenceInterval = setInterval(updatePresence, 20000);
};

const stopPresenceHeartbeat = () => {
  if (presenceInterval) clearInterval(presenceInterval);
};

const markAllAsRead = async () => {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);
    notifications.value.forEach(n => {
      if (Number(n.trangThai) === 0) n.trangThai = 1;
    });
  } catch (error) {
    console.error(error);
  }
};

const goToNotification = (item) => {
  showMiniNoti.value = false;
  router.push('/hotline/thong-bao');
};

const goToAllNotifications = () => {
  showMiniNoti.value = false;
  router.push('/hotline/thong-bao');
};

const getMiniIconClass = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "bg-red";
  if (item.loaiThongBao === "HE_THONG") return "bg-blue";
  if (item.loaiThongBao === "TU_CHOI") return "bg-yellow";
  if (Number(item.trangThai) === 2) return "bg-green";
  return "bg-purple";
};

const getMiniIconName = (item) => {
  if (item.loaiThongBao === "CONG_VIEC") return "fa-solid fa-briefcase";
  if (item.loaiThongBao === "HE_THONG") return "fa-solid fa-gear";
  if (item.loaiThongBao === "TU_CHOI") return "fa-solid fa-xmark";
  if (Number(item.trangThai) === 2) return "fa-solid fa-check";
  return "fa-solid fa-bell";
};

onMounted(() => {
  document.addEventListener('click', () => {
    if (showMiniNoti.value) showMiniNoti.value = false;
    if (showProfile.value) showProfile.value = false;
  });
  
  const userStr = localStorage.getItem("user");
  if (userStr) {
    user.value = JSON.parse(userStr);
  }

  loadNotifications();
  startPolling();
  startPresenceHeartbeat();
});

const toggleProfile = () => {
  showProfile.value = !showProfile.value;
};

const logout = async () => {
  try {
    await markStaffOffline();
  } catch (error) {
    console.error("Không thể cập nhật trạng thái offline:", error);
  }

  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");
  window.dispatchEvent(new Event('session-updated'));
  router.push("/");
};

onUnmounted(() => {
  stopPolling();
  stopPresenceHeartbeat();
});
</script>
<style scoped src="../assets/styles/layouts/Layout.css"></style>