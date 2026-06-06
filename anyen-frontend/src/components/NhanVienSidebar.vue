<template>
  <aside class="sidebar">

    <div class="logo-section">
      <img
          src="../assets/images/icon/logoAnYen.png.png"
          alt="An Yên"
      />
    </div>

    <div class="partner-card">

      <div class="avatar">
        <i class="bi bi-person"></i>
      </div>

      <div>
        <h6 class="mb-1">Nhân viên An Yên</h6>
        <small>Quản trị hệ thống</small>
      </div>

    </div>

    <nav class="menu">

      <RouterLink
          to="/nhan-vien/tong-quan"
          class="menu-item"
      >
        Tổng quan
      </RouterLink>

      <RouterLink
          to="/nhan-vien/quan-ly-doi-tac"
          class="menu-item"
      >
        Quản lý đối tác
      </RouterLink>

      <RouterLink
          to="/nhan-vien/quan-ly-don-hang"
          class="menu-item"
      >
        Quản lý đơn hàng
      </RouterLink>

      <RouterLink
          to="/nhan-vien/thong-bao"
          class="menu-item"
      >
        Thông báo
      </RouterLink>

      <RouterLink
          to="/nhan-vien/thong-tin-tai-khoan"
          class="menu-item"
      >
        Thông tin tài khoản
      </RouterLink>

      <button
          class="logout-btn"
          @click="logout"
      >
        Đăng xuất
      </button>

    </nav>

  </aside>
</template>

<style scoped src="../assets/styles/DoiTacSidebar.css"></style>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const logout = () => {

  localStorage.removeItem("user");

  router.push("/");

};
</script>