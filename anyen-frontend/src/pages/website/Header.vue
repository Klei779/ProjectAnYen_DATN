<template>
  <header class="header">

    <!-- Mobile menu button -->
    <button
        class="mobile-menu-btn"
        :class="{ active: mobileMenuOpen }"
        @click="mobileMenuOpen = !mobileMenuOpen"
    >
      <span></span>
      <span></span>
      <span></span>
    </button>

    <!-- Logo -->
    <div class="logo-wrapper">
      <img
          :src="logoAnYen"
          alt="An Yên"
          class="logo-img"
      >
    </div>

    <!-- Desktop Menu -->
    <nav class="desktop-nav">
      <RouterLink to="/">Trang chủ</RouterLink>

      <RouterLink to="/san-pham">
        Sản phẩm
      </RouterLink>

      <RouterLink to="/dich-vu">
        Dịch vụ
      </RouterLink>

      <RouterLink to="/gioi-thieu">
        Giới thiệu
      </RouterLink>

      <RouterLink to="/lien-he">
        Liên hệ
      </RouterLink>
    </nav>

    <!-- Desktop User -->
    <div class="desktop-actions">

      <el-button
          v-if="!user"
          class="login-btn"
          :icon="User"
          @click="showLogin = true"
      >
        ĐĂNG NHẬP
      </el-button>

      <el-dropdown v-else>
      <span class="user-info">
        Xin chào {{ user.hoTen }}
      </span>

        <template #dropdown>
          <el-dropdown-menu>

            <el-dropdown-item disabled>
              {{ user.loaiTaiKhoan }}
            </el-dropdown-item>

            <el-dropdown-item divided class="logout-dropdown-item">
              <button
                  class="btn btn-outline-danger btn-sm rounded-pill px-4 fw-bold"
                  @click.stop="logout"
              >
                Đăng xuất
              </button>
            </el-dropdown-item>

          </el-dropdown-menu>
        </template>
      </el-dropdown>

    </div>

    <!-- Hotline -->
    <el-button
        class="hotline-btn"
        type="danger"
        @click="showHotline = true"
    >
      ☎
    </el-button>

    <!-- Mobile Menu -->
    <div
        class="mobile-menu"
        :class="{ active: mobileMenuOpen }"
    >
      <RouterLink to="/" @click="mobileMenuOpen=false">
        Trang chủ
      </RouterLink>

      <RouterLink to="/san-pham" @click="mobileMenuOpen=false">
        Sản phẩm
      </RouterLink>

      <RouterLink to="/dich-vu" @click="mobileMenuOpen=false">
        Dịch vụ
      </RouterLink>

      <RouterLink to="/gioi-thieu" @click="mobileMenuOpen=false">
        Giới thiệu
      </RouterLink>

      <RouterLink to="/lien-he" @click="mobileMenuOpen=false">
        Liên hệ
      </RouterLink>

      <el-button
          v-if="!user"
          class="mobile-login-btn"
          :icon="User"
          @click="showLogin = true"
      >
        Đăng nhập
      </el-button>

      <div
          v-if="user"
          class="mobile-user"
      >
        <div class="mobile-user-name">
          {{ user.hoTen }}
        </div>

        <button
            class="mobile-logout-btn"
            @click="logout"
        >
          Đăng xuất
        </button>
      </div>
    </div>

    <LoginModal
        :show="showLogin"
        @close="showLogin = false"
        @login-success="handleLoginSuccess"
    />

    <HotlineModal
        :show="showHotline"
        @close="showHotline = false"
    />

  </header>
</template>

<script setup>
import {ref, computed} from "vue";
import {useRouter} from "vue-router";
import {User} from "@element-plus/icons-vue";

import LoginModal from "./PopDangNhap.vue";
import HotlineModal from "./PopLienHeHotline.vue";
import logoAnYen from "../../assets/images/icon/logoAnYen.png";

const router = useRouter();
const mobileMenuOpen = ref(false)
const showLogin = ref(false);
const showHotline = ref(false);

const user = ref(
    JSON.parse(localStorage.getItem("user"))
);

const isNhanVien = computed(() =>
    user.value?.loaiTaiKhoan === "NHAN_VIEN"
);

const isDoiTac = computed(() =>
    user.value?.loaiTaiKhoan === "DOI_TAC"
);

const handleLoginSuccess = (userData) => {
  user.value = userData;

  // Lưu đủ dữ liệu để router guard kiểm tra
  localStorage.setItem("user", JSON.stringify(userData));
  localStorage.setItem("token", userData.token);
  localStorage.setItem("loaiTaiKhoan", userData.loaiTaiKhoan);
  localStorage.setItem("tenDangNhap", userData.tenDangNhap);
  localStorage.setItem("id", userData.id);

  showLogin.value = false;

  // Điều hướng theo loại tài khoản
  if (userData.loaiTaiKhoan === "NHAN_VIEN") {
    router.push("/nhan-vien/tong-quan");
  } else if (userData.loaiTaiKhoan === "DOI_TAC") {
    router.push("/doi-tac/tong-quan");
  }
};

const logout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");
  user.value=null;

  router.push("/");
};
</script>

<style src="../../assets/styles/components/Header.css"></style>