<template>
  <aside class="sidebar" :class="{ 'collapsed': isCollapsed }">
    <button class="toggle-btn" @click="toggleSidebar">
      <i class="fa-solid" :class="isCollapsed ? 'fa-chevron-right' : 'fa-chevron-left'"></i>
    </button>

    <div class="logo-section">
      <img
          src="../assets/images/icon/logoAnYen.png"
          alt="An Yên"
          class="logo-img"
      />
    </div>

    <div class="partner-card" style="position: relative; cursor: pointer" @click="toggleProfile">
      <div class="avatar">
        <i class="bi bi-shield-lock"></i>
      </div>
      <div class="partner-info sidebar-text">
        <h6 class="mb-1">{{ user?.hoTen || 'Quản lý' }}</h6>
        <small>Admin An Yên</small>
      </div>
      
      <UserProfileDropdown 
        v-if="showProfile" 
        :user="user" 
        icon-class="bi bi-shield-lock"
        @logout="logout" 
      />
    </div>

    <nav class="menu">
      <RouterLink to="/admin/tong-quan" class="menu-item" title="Tổng quan">
        <i class="fa-solid fa-chart-pie menu-icon"></i>
        <span class="sidebar-text">Tổng quan</span>
      </RouterLink>
      
      <RouterLink to="/admin/quan-ly-doi-tac" class="menu-item" title="Quản lý đối tác">
        <i class="fa-solid fa-handshake menu-icon"></i>
        <span class="sidebar-text">Quản lý đối tác</span>
      </RouterLink>
      <RouterLink to="/admin/quan-ly-nhan-vien" class="menu-item" title="Quản lý nhân viên">
        <i class="fa-solid fa-handshake menu-icon"></i>
        <span class="sidebar-text">Quản lý nhân viên</span>
      </RouterLink>

      <RouterLink to="/admin/thong-bao" class="menu-item" title="Thông báo">
        <i class="fa-solid fa-bell menu-icon"></i>
        <span class="sidebar-text">Thông báo</span>
      </RouterLink>
      <RouterLink to="/admin/thong-ke-doanh-thu" class="menu-item" title="Thống kê doanh thu">
        <i class="fa-solid fa-chart-line menu-icon"></i>
        <span class="sidebar-text">Thống kê doanh thu</span>
      </RouterLink>
      <RouterLink
          to="/admin/quan-ly-hoa-don"
          class="menu-item"
          title="Quản lý hóa đơn"
      >
        <i class="fa-solid fa-file-invoice-dollar menu-icon"></i>
        <span class="sidebar-text">Quản lý hóa đơn</span>
      </RouterLink>
      <RouterLink to="/admin/thong-tin-tai-khoan" class="menu-item" title="Thông tin tài khoản">
        <i class="fa-solid fa-user menu-icon"></i>
        <span class="sidebar-text">Thông tin tài khoản</span>
      </RouterLink>
      <div class="logout-btn-container mt-3">
        <button
            class="btn btn-outline-danger btn-sm rounded-pill px-4 fw-bold logout-btn"
            @click="logout"
            title="Đăng xuất"
        >
          <i class="fa-solid fa-right-from-bracket"></i>
          <span class="sidebar-text">Đăng xuất</span>
        </button>
      </div>
    </nav>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import UserProfileDropdown from "./UserProfileDropdown.vue";

const router = useRouter();
const isCollapsed = ref(false);
const showProfile = ref(false);
const user = ref(null);

onMounted(() => {
  const userStr = localStorage.getItem("user");
  if (userStr) {
    user.value = JSON.parse(userStr);
  }
  document.addEventListener('click', closeProfile);
});

onUnmounted(() => {
  document.removeEventListener('click', closeProfile);
});

const closeProfile = (e) => {
  if (!e.target.closest('.partner-card')) {
    showProfile.value = false;
  }
};

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

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
</script>

<style scoped src="../assets/styles/components/SideBar.css"></style>
