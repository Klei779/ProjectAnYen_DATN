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
        <i class="bi bi-flower1"></i>
      </div>
      <div class="partner-info sidebar-text">
        <h6 class="mb-1">{{ user?.hoTen || 'Đối tác' }}</h6>
        <small>Nhà cung cấp</small>
      </div>

      <UserProfileDropdown 
        v-if="showProfile" 
        :user="user" 
        icon-class="bi bi-flower1"
        @logout="logout" 
      />
    </div>

    <nav class="menu">
      <RouterLink to="/doi-tac/tong-quan" class="menu-item" title="Tổng quan">
        <i class="fa-solid fa-chart-pie menu-icon"></i>
        <span class="sidebar-text">Tổng quan</span>
      </RouterLink>

      <RouterLink to="/doi-tac/quan-ly-san-pham" class="menu-item" title="Quản lý sản phẩm">
        <i class="fa-solid fa-box menu-icon"></i>
        <span class="sidebar-text">Quản lý sản phẩm</span>
      </RouterLink>

      <RouterLink to="/doi-tac/tao-san-pham" class="menu-item" title="Tạo sản phẩm">
        <i class="fa-solid fa-circle-plus menu-icon"></i>
        <span class="sidebar-text">Tạo sản phẩm</span>
      </RouterLink>

      <RouterLink to="/doi-tac/quan-ly-don-hang" class="menu-item" title="Quản lý đơn hàng">
        <i class="fa-solid fa-cart-shopping menu-icon"></i>
        <span class="sidebar-text">Quản lý đơn hàng</span>
      </RouterLink>

      <RouterLink to="/doi-tac/thong-bao" class="menu-item" title="Thông báo">
        <i class="fa-solid fa-bell menu-icon"></i>
        <span class="sidebar-text">Thông báo</span>
      </RouterLink>
      <RouterLink to="/doi-tac/thong-ke-doanh-thu" class="menu-item" title="Thống kê doanh thu">
        <i class="fa-solid fa-chart-line menu-icon"></i>
        <span class="sidebar-text">Thống kê doanh thu</span>
      </RouterLink>
      <RouterLink to="/doi-tac/thong-tin-tai-khoan" class="menu-item" title="Thông tin tài khoản">
        <i class="fa-solid fa-user menu-icon"></i>
        <span class="sidebar-text">Thông tin tài khoản</span>
      </RouterLink>

      <RouterLink to="/doi-tac/doi-mat-khau" class="menu-item" title="Đổi mật khẩu">
        <i class="fa-solid fa-lock menu-icon"></i>
        <span class="sidebar-text">Đổi mật khẩu</span>
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