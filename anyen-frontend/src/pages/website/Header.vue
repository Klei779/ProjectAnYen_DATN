<template>
  <header
      class="site-header"
      :class="{
      'home-mode': isHomePage,
      'inner-mode': !isHomePage,
      'is-scrolled': isScrolled,
      'menu-opened': mobileMenuOpen
    }"
  >
    <div class="header-inner">
      <!-- Logo -->
      <RouterLink
          to="/"
          class="header-logo"
          aria-label="Trang chủ An Yên"
          @click="closeMobileMenu"
      >
        <img
            :src="logoAnYen"
            alt="Logo An Yên"
            class="header-logo-image"
        />
      </RouterLink>

      <!-- Desktop navigation -->
      <nav class="desktop-nav" aria-label="Điều hướng chính">
        <RouterLink
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            :class="{ active: isActiveRoute(item.path) }"
        >
          {{ item.name }}
        </RouterLink>
      </nav>

      <!-- Desktop actions -->
      <div class="desktop-actions">
        <!-- Chưa đăng nhập -->
        <RouterLink
            to="/gio-hang"
            class="header-cart-button"
            aria-label="Mở giỏ hàng"
        >
          <i class="fa-solid fa-cart-shopping"></i>

          <span class="header-cart-count">
    {{ cartCount > 99 ? "99+" : cartCount }}
  </span>
        </RouterLink>
        <el-button
            v-if="!user"
            class="login-button"
            :icon="User"
            @click="showLogin = true"
        >
          Đăng nhập
        </el-button>

        <!-- Đã đăng nhập -->
        <el-dropdown
            v-else
            trigger="click"
            placement="bottom-end"
        >
          <button class="user-button" type="button">
            <span class="user-avatar">
              <i class="fa-solid fa-user"></i>
            </span>

            <span class="user-content">
              <small>Xin chào</small>
              <strong>{{ user.hoTen }}</strong>
            </span>

            <i class="fa-solid fa-chevron-down user-arrow"></i>
          </button>

          <template #dropdown>
            <el-dropdown-menu class="header-dropdown">
              <el-dropdown-item disabled>
                <div class="dropdown-account">
                  <strong>{{ user.hoTen }}</strong>

                  <span>
                    {{ getAccountLabel(user.loaiTaiKhoan) }}
                  </span>
                </div>
              </el-dropdown-item>

              <el-dropdown-item
                  divided
                  @click="goToManagement"
              >
                <i class="fa-solid fa-gauge"></i>
                Trang Quản lý
              </el-dropdown-item>

              <el-dropdown-item
                  divided
                  class="logout-dropdown-item"
                  @click="logout"
              >
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
                Đăng xuất
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- Hotline -->
        <button
            type="button"
            class="hotline-button"
            @click="openHotline"
        >
          <span class="hotline-icon">
            <i class="fa-solid fa-phone-volume"></i>
          </span>

          <span class="hotline-content">
            <small>Hotline miễn phí</small>
            <strong>0392 168 473</strong>
          </span>
        </button>
      </div>

      <!-- Mobile actions -->
      <div class="mobile-actions">
        <RouterLink
            to="/gio-hang"
            class="header-cart-button"
            aria-label="Mở giỏ hàng"
        >
          <i class="fa-solid fa-cart-shopping"></i>

          <span class="header-cart-count">
    {{ cartCount > 99 ? "99+" : cartCount }}
  </span>
        </RouterLink>
        <button
            type="button"
            class="mobile-hotline-button"
            aria-label="Mở hỗ trợ hotline"
            @click="openHotline"
        >
          <i class="fa-solid fa-phone"></i>
        </button>

        <button
            type="button"
            class="mobile-menu-button"
            :class="{ active: mobileMenuOpen }"
            :aria-expanded="mobileMenuOpen"
            aria-label="Mở menu"
            @click="toggleMobileMenu"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <!-- Mobile navigation -->
    <Transition name="mobile-navigation">
      <div
          v-if="mobileMenuOpen"
          class="mobile-navigation-wrapper"
      >
        <nav
            class="mobile-navigation"
            aria-label="Điều hướng trên điện thoại"
        >
          <RouterLink
              v-for="item in menuItems"
              :key="item.path"
              :to="item.path"
              :class="{ active: isActiveRoute(item.path) }"
              @click="closeMobileMenu"
          >
            <span>{{ item.name }}</span>
            <i class="fa-solid fa-chevron-right"></i>
          </RouterLink>
        </nav>

        <div class="mobile-account">
          <!-- Chưa đăng nhập -->
          <button
              v-if="!user"
              type="button"
              class="mobile-login-button"
              @click="openLogin"
          >
            <i class="fa-regular fa-user"></i>
            Đăng nhập
          </button>

          <!-- Đã đăng nhập -->
          <template v-else>
            <div class="mobile-user-information">
              <span class="mobile-user-avatar">
                <i class="fa-solid fa-user"></i>
              </span>

              <div>
                <small>Xin chào</small>

                <strong>{{ user.hoTen }}</strong>

                <p>
                  {{ getAccountLabel(user.loaiTaiKhoan) }}
                </p>
              </div>
            </div>

            <button
                type="button"
                class="mobile-logout-button"
                style="margin-bottom: 10px; background-color: #e0f2fe; color: #0284c7; border-color: #e0f2fe;"
                @click="goToManagement"
            >
              <i class="fa-solid fa-gauge"></i>
              Trang Quản Lý
            </button>

            <button
                type="button"
                class="mobile-logout-button"
                @click="logout"
            >
              <i class="fa-solid fa-arrow-right-from-bracket"></i>
              Đăng xuất
            </button>
          </template>

          <button
              type="button"
              class="mobile-hotline-full"
              @click="openHotline"
          >
            <i class="fa-solid fa-phone-volume"></i>

            <span>
              <small>Hotline hỗ trợ 24/7</small>
              <strong>1900 1234</strong>
            </span>
          </button>
        </div>
      </div>
    </Transition>

    <!-- Modal đăng nhập -->
    <LoginModal
        :show="showLogin"
        @close="showLogin = false"
        @login-success="handleLoginSuccess"
    />

    <!-- Modal hotline -->
    <HotlineModal
        :show="showHotline"
        @close="showHotline = false"
    />
  </header>
  <ChatBox/>

</template>

<script setup>
import {
  computed,
  nextTick,
  onMounted,
  onUnmounted,
  ref,
  watch
} from "vue";

import { useRoute, useRouter } from "vue-router";
import { User } from "@element-plus/icons-vue";

import LoginModal from "./PopDangNhap.vue";
import HotlineModal from "./PopLienHeHotline.vue";

import logoAnYen from "../../assets/images/icon/logoAnYen.png";
import ChatBox from "./ChatBox.vue";
import { useCart } from "../../services/useCart.js";

const route = useRoute();
const router = useRouter();
const {cartCount} = useCart();
const mobileMenuOpen = ref(false);
const showLogin = ref(false);
const showHotline = ref(false);
const isScrolled = ref(false);

let scrollFrame = null;

/* ==================================================
   USER
================================================== */

const getSavedUser = () => {
  try {
    const savedUser = localStorage.getItem("user");

    return savedUser
        ? JSON.parse(savedUser)
        : null;
  } catch (error) {
    console.error(
        "Không thể đọc thông tin đăng nhập:",
        error
    );

    return null;
  }
};

const user = ref(getSavedUser());

/* ==================================================
   MENU
================================================== */

const menuItems = [
  {
    name: "Trang chủ",
    path: "/"
  },
  {
    name: "Giới thiệu",
    path: "/gioi-thieu"
  },
  {
    name: "Sản phẩm",
    path: "/san-pham"
  },
  {
    name: "Dịch vụ",
    path: "/dich-vu"
  },
  {
    name: "Tin tức",
    path: "/tin-tuc"
  },
  {
    name: "Liên hệ",
    path: "/lien-he"
  }
];

const isHomePage = computed(() => {
  return route.path === "/";
});

const isActiveRoute = (path) => {
  if (path === "/") {
    return route.path === "/";
  }

  return route.path.startsWith(path);
};

/* ==================================================
   SCROLL HEADER
================================================== */

const updateScrollState = () => {
  /*
   * Tất cả các trang đều nhận class is-scrolled
   * khi người dùng cuộn quá 25px.
   */
  isScrolled.value = window.scrollY > 25;
};

const handleScroll = () => {
  if (scrollFrame) {
    return;
  }

  scrollFrame = window.requestAnimationFrame(() => {
    updateScrollState();
    scrollFrame = null;
  });
};

/* ==================================================
   MOBILE MENU
================================================== */

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value;
};

const closeMobileMenu = () => {
  mobileMenuOpen.value = false;
};

const openLogin = () => {
  closeMobileMenu();
  showLogin.value = true;
};

const openHotline = () => {
  closeMobileMenu();
  showHotline.value = true;
};

/* ==================================================
   ACCOUNT
================================================== */

const getAccountLabel = (accountType) => {
  const labels = {
    NHAN_VIEN: "Nhân viên",
    NHANVIEN: "Nhân viên",

    DOI_TAC: "Đối tác",
    DOITAC: "Đối tác",

    QUAN_LY: "Quản lý",
    QUANLY: "Quản lý",

    ADMIN: "Quản trị viên"
  };

  return labels[accountType]
      || accountType
      || "Tài khoản";
};

const handleLoginSuccess = (userData) => {
  user.value = userData;

  localStorage.setItem(
      "user",
      JSON.stringify(userData)
  );

  if (userData.token) {
    localStorage.setItem(
        "token",
        userData.token
    );
  }

  if (userData.loaiTaiKhoan) {
    localStorage.setItem(
        "loaiTaiKhoan",
        userData.loaiTaiKhoan
    );
  }

  if (userData.tenDangNhap) {
    localStorage.setItem(
        "tenDangNhap",
        userData.tenDangNhap
    );
  }

  if (
      userData.id !== undefined
      && userData.id !== null
  ) {
    localStorage.setItem(
        "id",
        String(userData.id)
    );
  }

  showLogin.value = false;
  closeMobileMenu();

  const accountType = userData.vaiTroChiTiet || userData.loaiTaiKhoan;

  if (
      accountType === "NHAN_VIEN"
      || accountType === "NHANVIEN"
  ) {
    router.push("/nhan-vien/thong-tin-tai-khoan");
    return;
  }

  if (
      accountType === "DOI_TAC"
      || accountType === "DOITAC"
  ) {
    router.push("/doi-tac/thong-tin-tai-khoan");
  }
  
  if (accountType === "ADMIN") {
    router.push("/admin/thong-tin-tai-khoan");
  }
  
  if (accountType === "HOTLINE") {
    router.push("/hotline/thong-tin-tai-khoan");
  }
};

const goToManagement = () => {
  if (!user.value) return;
  const accountType = user.value.vaiTroChiTiet || user.value.loaiTaiKhoan;
  
  if (accountType === "DOITAC" || accountType === "DOI_TAC") {
    router.push("/doi-tac/thong-tin-tai-khoan");
  } else if (accountType === "ADMIN") {
    router.push("/admin/thong-tin-tai-khoan");
  } else if (accountType === "HOTLINE") {
    router.push("/hotline/thong-tin-tai-khoan");
  } else if (accountType === "NHANVIEN" || accountType === "NHAN_VIEN") {
    router.push("/nhan-vien/thong-tin-tai-khoan");
  }
};

const logout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  localStorage.removeItem("loaiTaiKhoan");
  localStorage.removeItem("tenDangNhap");
  localStorage.removeItem("id");

  user.value = null;

  closeMobileMenu();

  router.push("/");
};

/* ==================================================
   WATCH
================================================== */

watch(
    () => route.fullPath,
    async () => {
      closeMobileMenu();

      await nextTick();

      updateScrollState();
    }
);

watch(mobileMenuOpen, (isOpen) => {
  if (
      isOpen
      && window.innerWidth <= 992
  ) {
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = "";
  }
});

/* ==================================================
   LIFECYCLE
================================================== */

onMounted(() => {
  updateScrollState();

  window.addEventListener(
      "scroll",
      handleScroll,
      {
        passive: true
      }
  );
});

onUnmounted(() => {
  window.removeEventListener(
      "scroll",
      handleScroll
  );

  if (scrollFrame) {
    window.cancelAnimationFrame(scrollFrame);
  }

  document.body.style.overflow = "";
});
</script>

<style src="../../assets/styles/components/Header.css"></style>