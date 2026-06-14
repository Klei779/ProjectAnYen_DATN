<template>
  <aside class="sidebar" :class="{ 'collapsed': isCollapsed }">
    <button class="toggle-btn" @click="toggleSidebar">
      <i class="fa-solid" :class="isCollapsed ? 'fa-chevron-right' : 'fa-chevron-left'"></i>
    </button>

    <div class="logo-section">
      <img
          src="../assets/images/icon/logoAnYen.png.png"
          alt="An Yên"
          class="logo-img"
      />
    </div>

    <div class="partner-card">
      <div class="avatar">
        <i class="bi bi-person"></i>
      </div>
      <div class="partner-info sidebar-text">
        <h6 class="mb-1">Nhân viên An Yên</h6>
        <small>Quản trị hệ thống</small>
      </div>
    </div>

    <nav class="menu">
      <RouterLink to="/nhan-vien/tong-quan" class="menu-item" title="Tổng quan">
        <i class="fa-solid fa-chart-pie menu-icon"></i>
        <span class="sidebar-text">Tổng quan</span>
      </RouterLink>

      <RouterLink to="/nhan-vien/quan-ly-doi-tac" class="menu-item" title="Quản lý đối tác">
        <i class="fa-solid fa-handshake menu-icon"></i>
        <span class="sidebar-text">Quản lý đối tác</span>
      </RouterLink>

      <RouterLink to="/nhan-vien/quan-ly-don-hang" class="menu-item" title="Quản lý đơn hàng">
        <i class="fa-solid fa-cart-shopping menu-icon"></i>
        <span class="sidebar-text">Quản lý đơn hàng</span>
      </RouterLink>

      <RouterLink to="/nhan-vien/thong-bao" class="menu-item" title="Thông báo">
        <i class="fa-solid fa-bell menu-icon"></i>
        <span class="sidebar-text">Thông báo</span>
      </RouterLink>

      <RouterLink to="/nhan-vien/thong-tin-tai-khoan" class="menu-item" title="Thông tin tài khoản">
        <i class="fa-solid fa-user menu-icon"></i>
        <span class="sidebar-text">Thông tin tài khoản</span>
      </RouterLink>

      <RouterLink to="/nhan-vien/quan-ly-khach-hang" class="menu-item" title="Quản lý khách hàng">
        <i class="fa-solid fa-users menu-icon"></i>
        <span class="sidebar-text">Quản lý khách hàng</span>
      </RouterLink>

      <RouterLink to="/nhan-vien/quan-ly-hop-dong" class="menu-item" title="Quản lý hợp đồng">
        <i class="fa-solid fa-file-contract menu-icon"></i>
        <span class="sidebar-text">Quản lý hợp đồng</span>
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
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const logout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");

  router.push("/");
};
</script>

<style scoped src="../assets/styles/DoiTacSidebar.css"></style>