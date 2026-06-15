<template>
  <div class="container-fluid">
    <div class="row min-vh-100">

      <div class="col-auto p-0">
        <DoiTacSidebar />
      </div>

      <div class="col p-0" style="background-color: #f8f9fa; display: flex; flex-direction: column;">


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
import DoiTacSidebar from "../components/DoiTacSidebar.vue";
import UserProfileDropdown from "../components/UserProfileDropdown.vue";

const route = useRoute();
const router = useRouter();

const user = ref(null);
const showProfile = ref(false);

const routeTitles = {
  "/doi-tac/tong-quan": "Tổng quan",
  "/doi-tac/quan-ly-san-pham": "Quản lý sản phẩm",
  "/doi-tac/quan-ly-don-hang": "Quản lý đơn hàng",
  "/doi-tac/thong-bao": "Thông báo",
  "/doi-tac/thong-tin-tai-khoan": "Thông tin tài khoản",
  "/doi-tac/doi-mat-khau": "Đổi mật khẩu",
};

const pageTitle = computed(() => {
  return routeTitles[route.path] || "Trang Đối Tác";
});

// Notifications Logic
const notifications = ref([]);
const showMiniNoti = ref(false);
let pollingInterval = null;
const API_URL = "/api/doi-tac/thong-bao";

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
  router.push('/doi-tac/thong-bao');
};

const goToAllNotifications = () => {
  showMiniNoti.value = false;
  router.push('/doi-tac/thong-bao');
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

<style scoped src="../assets/styles/layouts/Layout.css"></style>