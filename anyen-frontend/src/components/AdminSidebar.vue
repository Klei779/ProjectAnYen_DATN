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
      <!-- Quản lý nhân sự -->
      <div class="menu-group">
        <div class="menu-group-header" @click="toggleMenu('users')">
          <i class="fa-solid fa-users-gear menu-icon"></i>
          <span class="sidebar-text">Quản lý nhân sự</span>
          <i class="fa-solid menu-chevron" :class="expandedMenus.users ? 'fa-chevron-down' : 'fa-chevron-right'"></i>
        </div>
        <transition name="slide-fade">
          <div class="submenu" v-if="expandedMenus.users">
            <RouterLink to="/admin/quan-ly-doi-tac" class="menu-item submenu-item" title="Quản lý đối tác">
              <i class="fa-solid fa-handshake menu-icon"></i>
              <span class="sidebar-text">Quản lý đối tác</span>
            </RouterLink>
            <RouterLink to="/admin/quan-ly-nhan-vien" class="menu-item submenu-item" title="Quản lý nhân viên">
              <i class="fa-solid fa-user-tie menu-icon"></i>
              <span class="sidebar-text">Quản lý nhân viên</span>
            </RouterLink>
          </div>
        </transition>
      </div>

      <!-- Quản lý khách hàng -->
      <RouterLink to="/admin/quan-ly-khach-hang" class="menu-item" title="Quản lý khách hàng">
        <i class="fa-solid fa-user menu-icon"></i>
        <span class="sidebar-text">Quản lý khách hàng</span>
      </RouterLink>

      <!-- Đơn hàng & Hợp đồng -->
      <div class="menu-group">
        <div class="menu-group-header" @click="toggleMenu('orders')">
          <i class="fa-solid fa-clipboard-list menu-icon"></i>
          <span class="sidebar-text">Đơn hàng & Hợp đồng</span>
          <i class="fa-solid menu-chevron" :class="expandedMenus.orders ? 'fa-chevron-down' : 'fa-chevron-right'"></i>
        </div>
        <transition name="slide-fade">
          <div class="submenu" v-if="expandedMenus.orders">
            <RouterLink to="/admin/quan-ly-don-hang" class="menu-item submenu-item" title="Quản lý đơn hàng">
              <i class="fa-solid fa-box menu-icon"></i>
              <span class="sidebar-text">Quản lý đơn hàng</span>
            </RouterLink>
            <RouterLink to="/admin/quan-ly-hop-dong" class="menu-item submenu-item" title="Quản lý hợp đồng">
              <i class="fa-solid fa-file-contract menu-icon"></i>
              <span class="sidebar-text">Quản lý hợp đồng</span>
            </RouterLink>
            <RouterLink to="/admin/quan-ly-hoa-don" class="menu-item submenu-item" title="Quản lý hóa đơn">
              <i class="fa-solid fa-file-invoice-dollar menu-icon"></i>
              <span class="sidebar-text">Quản lý hóa đơn</span>
            </RouterLink>
          </div>
        </transition>
      </div>

      <!-- Quản lý nội dung -->
      <div class="menu-group">
        <div class="menu-group-header" @click="toggleMenu('content')">
          <i class="fa-solid fa-newspaper menu-icon"></i>
          <span class="sidebar-text">Quản lý nội dung</span>
          <i class="fa-solid menu-chevron" :class="expandedMenus.content ? 'fa-chevron-down' : 'fa-chevron-right'"></i>
        </div>
        <transition name="slide-fade">
          <div class="submenu" v-if="expandedMenus.content">
            <RouterLink to="/admin/duyet-san-pham" class="menu-item submenu-item" title="Quản lý sản phẩm">
              <i class="fa-solid fa-box-open menu-icon"></i>
              <span class="sidebar-text">Quản lý sản phẩm</span>
            </RouterLink>
            <RouterLink to="/admin/quan-ly-combo" class="menu-item submenu-item" title="Quản lý combo">
              <i class="fa-solid fa-layer-group menu-icon"></i>
              <span class="sidebar-text">Quản lý combo</span>
            </RouterLink>
            <RouterLink to="/admin/quan-ly-tin-tuc" class="menu-item submenu-item" title="Quản lý tin tức">
              <i class="fa-regular fa-newspaper menu-icon"></i>
              <span class="sidebar-text">Quản lý tin tức</span>
            </RouterLink>
          </div>
        </transition>
      </div>

      <!-- Quản lý tài chính -->
      <div class="menu-group">
        <div class="menu-group-header" @click="toggleMenu('finance')">
          <i class="fa-solid fa-coins menu-icon"></i>
          <span class="sidebar-text">Quản lý tài chính</span>
          <i class="fa-solid menu-chevron" :class="expandedMenus.finance ? 'fa-chevron-down' : 'fa-chevron-right'"></i>
        </div>
        <transition name="slide-fade">
          <div class="submenu" v-if="expandedMenus.finance">
            <RouterLink to="/admin/quan-ly-cong-no" class="menu-item submenu-item" title="Quản lý công nợ">
              <i class="fa-solid fa-file-invoice menu-icon"></i>
              <span class="sidebar-text">Quản lý công nợ</span>
            </RouterLink>
            <RouterLink to="/admin/thong-ke-doanh-thu" class="menu-item submenu-item" title="Thống kê doanh thu">
              <i class="fa-solid fa-chart-line menu-icon"></i>
              <span class="sidebar-text">Thống kê doanh thu</span>
            </RouterLink>
          </div>
        </transition>
      </div>

      <RouterLink to="/admin/thong-bao" class="menu-item" title="Thông báo">
        <i class="fa-solid fa-bell menu-icon"></i>
        <span class="sidebar-text">Thông báo</span>
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

const expandedMenus = ref({
  users: false,
  orders: false,
  content: false,
  finance: false
});

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

const toggleMenu = (menuName) => {
  expandedMenus.value[menuName] = !expandedMenus.value[menuName];
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