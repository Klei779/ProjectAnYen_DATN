<template>
  <aside
      class="sidebar"
      :class="{ collapsed: isCollapsed }"
  >
    <button
        class="toggle-btn"
        type="button"
        @click="toggleSidebar"
    >
      <i
          class="fa-solid"
          :class="
          isCollapsed
            ? 'fa-chevron-right'
            : 'fa-chevron-left'
        "
      ></i>
    </button>

    <div class="logo-section">
      <img
          src="../assets/images/icon/logoAnYen.png"
          alt="An Yên"
          class="logo-img"
      />
    </div>

    <div
        class="partner-card"
        style="position: relative; cursor: pointer"
        @click="toggleProfile"
    >
      <div class="avatar">
        <i class="bi bi-headset"></i>
      </div>

      <div class="partner-info sidebar-text">
        <h6 class="mb-1">
          {{ user?.hoTen || "Hotline" }}
        </h6>

        <small>Nhân viên Hotline</small>
      </div>

      <UserProfileDropdown
          v-if="showProfile"
          :user="user"
          icon-class="bi bi-headset"
          @logout="logout"
      />
    </div>

    <nav class="menu">
      <RouterLink
          to="/hotline/quan-ly-cong-viec"
          class="menu-item"
          title="Quản lý công việc"
      >
        <i class="fa-solid fa-briefcase menu-icon"></i>
        <span class="sidebar-text">
          Quản lý công việc
        </span>
      </RouterLink>

      <RouterLink
          to="/hotline/quan-ly-don-hang"
          class="menu-item"
          title="Quản lý đơn hàng"
      >
        <i class="fa-solid fa-cart-shopping menu-icon"></i>
        <span class="sidebar-text">
          Quản lý đơn hàng
        </span>
      </RouterLink>

      <RouterLink
          to="/hotline/thong-bao"
          class="menu-item"
          title="Thông báo"
      >
        <i class="fa-solid fa-bell menu-icon"></i>
        <span class="sidebar-text">
          Thông báo
        </span>
      </RouterLink>

      <RouterLink
          to="/hotline/thong-tin-tai-khoan"
          class="menu-item"
          title="Thông tin cá nhân"
      >
        <i class="fa-solid fa-user-pen menu-icon"></i>
        <span class="sidebar-text">
          Thông tin cá nhân
        </span>
      </RouterLink>

      <div class="logout-btn-container mt-3">
        <button
            class="btn btn-outline-danger btn-sm rounded-pill px-4 fw-bold logout-btn"
            type="button"
            title="Đăng xuất"
            @click="logout"
        >
          <i class="fa-solid fa-right-from-bracket"></i>
          <span class="sidebar-text">
            Đăng xuất
          </span>
        </button>
      </div>
    </nav>
  </aside>

  <!-- Khung chat AI nổi ở góc phải -->
  <AiChatBox />
</template>

<script setup>
import {
  ref,
  onMounted,
  onUnmounted
} from "vue";

import { useRouter } from "vue-router";

import UserProfileDropdown
  from "./UserProfileDropdown.vue";

import AiChatBox
  from "../pages/hotline/AiChatBox.vue";

const router = useRouter();

const isCollapsed = ref(false);
const showProfile = ref(false);
const user = ref(null);

const closeProfile = (event) => {
  if (!event.target.closest(".partner-card")) {
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

  window.dispatchEvent(
      new Event("session-updated")
  );

  router.push("/");
};

onMounted(() => {
  const userStr = localStorage.getItem("user");

  if (userStr) {
    try {
      user.value = JSON.parse(userStr);
    } catch (error) {
      console.error(
          "Không đọc được thông tin người dùng:",
          error
      );

      user.value = null;
    }
  }

  document.addEventListener(
      "click",
      closeProfile
  );
});

onUnmounted(() => {
  document.removeEventListener(
      "click",
      closeProfile
  );
});
</script>

<style
    scoped
    src="../assets/styles/components/SideBar.css"
></style>