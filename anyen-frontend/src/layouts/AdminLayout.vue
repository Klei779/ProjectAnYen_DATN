<template>
  <div class="admin-layout">

    <AdminSidebar />

    <!-- Nội dung -->
    <div class="admin-content">
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
                  <div v-for="item in miniNotifications" :key="item.maThongBao" class="mini-item" :class="{'unread': item.trangThai === 'CHUA_DOC'}">
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
                  <strong>{{ user?.hoTen || 'Quản lý' }}</strong>
                </div>
              </div>
              <UserProfileDropdown
                v-if="showProfile"
                :user="user"
                icon-class="fa-solid fa-user-shield"
                @logout="logout"
              />
            </div>

          </div>
        </header>

        <div class="router-container">
          <router-view />
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import api from "../api/api.js";
import AdminSidebar from "../components/AdminSidebar.vue";
import UserProfileDropdown from "../components/UserProfileDropdown.vue";

const route = useRoute();
const router = useRouter();

const user = ref(null);
const showProfile = ref(false);

const routeTitles = {
  "/admin/tong-quan": "Tổng quan",
  "/admin/quan-ly-doi-tac": "Quản lý đối tác",
  "/admin/quan-ly-nhan-vien": "Quản lý nhân viên",
  "/admin/quan-ly-hop-dong": "Quản lý hợp đồng",
  "/admin/thong-bao": "Thông báo công việc",
};

const pageTitle = computed(() => {
  return routeTitles[route.path] || "Trang Quản Lý";
});

// Notifications Logic
const notifications = ref([]);
const showMiniNoti = ref(false);
let pollingInterval = null;
const API_URL = "/api/nhan-vien/thong-bao";

const unreadCount = computed(() => {
  return notifications.value.filter(item => item.trangThai === 'CHUA_DOC').length;
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

const markAllAsRead = async () => {
  try {
    await api.put(`${API_URL}/da-doc-tat-ca`);
    notifications.value.forEach(n => {
      if(n.trangThai === 'CHUA_DOC') n.trangThai = 'DA_DOC';
    });
  } catch (error) {
    console.error(error);
  }
};

const goToNotification = (item) => {
  showMiniNoti.value = false;
  router.push('/admin/thong-bao');
};

const goToAllNotifications = () => {
  showMiniNoti.value = false;
  router.push('/admin/thong-bao');
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
});

const toggleProfile = () => {
  showProfile.value = !showProfile.value;
};

const logout = () => {
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
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.page-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 32px;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 9999;
  font-family: 'Inter', sans-serif;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.topbar-left i {
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
}

.topbar-left h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #111827;
}

/* Bell Styles */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.bell-wrapper {
  position: relative;
  cursor: pointer;
}

.bell-wrapper > i {
  font-size: 22px;
  color: #374151;
  transition: 0.2s;
}

.bell-wrapper:hover > i {
  color: #dc2626;
}

.bell-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: #dc2626;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid #fff;
}

.mini-noti-dropdown {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 40px;
  right: -10px;
  width: 400px;
  z-index: 1000;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  transform-origin: top right;
  animation: dropdownIn 0.2s ease;
}

.mini-noti-dropdown::before {
  content: "";
  position: absolute;
  top: -8px;
  right: 14px;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid #fff;
}

@keyframes dropdownIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.mini-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.header-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mini-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.mini-header .badge {
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 50%;
  margin-left: 6px;
}

.mark-read-btn {
  background: transparent;
  border: none;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  text-align: left;
}

.close-mini-btn {
  background: transparent;
  border: none;
  color: #9ca3af;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: 0.2s;
}

.close-mini-btn:hover {
  background: #f3f4f6;
  color: #111827;
}

.mini-list {
  max-height: 500px;
  overflow-y: auto;
}

.mini-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  position: relative;
}

.mini-item.unread {
  background: #f9fafb;
}

.mini-item.unread::before {
  content: "";
  position: absolute;
  left: 8px;
  top: 24px;
  width: 6px;
  height: 6px;
  background: #dc2626;
  border-radius: 50%;
}

.mini-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  color: #fff;
}

.bg-red { background: #dc2626; }
.bg-yellow { background: #f59e0b; }
.bg-blue { background: #2563eb; }
.bg-green { background: #059669; }
.bg-purple { background: #8b5cf6; }

.mini-info {
  flex: 1;
}

.mini-info h5 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.mini-info p {
  margin: 0 0 6px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mini-info small {
  font-size: 12px;
  color: #9ca3af;
}

.mini-view-btn {
  background: #fff;
  border: 1px solid #e5e7eb;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
}

.mini-footer {
  padding: 12px;
  text-align: center;
  border-top: 1px solid #e5e7eb;
}

.mini-footer button {
  background: transparent;
  border: none;
  color: #dc2626;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.empty-mini {
  padding: 30px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.avatar-small {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
}
.user-short-info strong {
  font-size: 14px;
  color: #374151;
}

/* ── Flexbox Layout ── */
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.admin-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  overflow: hidden;
}

.router-container {
  flex: 1;
  overflow: auto;
}

@media (max-width:768px){

  .page-topbar{
    padding: 12px 16px;
  }

  .topbar-left{
    gap: 10px;
    min-width: 0;
  }

  .topbar-left h2{
    font-size: 15px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 140px;
  }

  .topbar-right{
    gap: 12px;
  }

  .user-short-info{
    display:none;
  }
}
</style>