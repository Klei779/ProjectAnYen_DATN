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

import LoginModal from "../components/PopDangNhap.vue";
import HotlineModal from "../components/PopLienHeHotline.vue";
import logoAnYen from "../assets/images/icon/logoAnYen.png";

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

<style scoped>
.header{
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding:16px 40px;
  border-bottom:1px solid #eee;
  background:#fff;
  position:relative;
  z-index:1000;
}

.logo{
  margin:0;
  color:#8b1024;
  font-size:30px;
  font-weight:700;
  font-family:'Faustina',serif;
}

.desktop-nav{
  display:flex;
  gap:24px;
}

.desktop-nav a{
  text-decoration:none;
  color:#333;
  font-weight:500;
  transition:.25s;
}

.desktop-nav a:hover{
  color:#8b1024;
}

.desktop-actions{
  display:flex;
  align-items:center;
  gap:12px;
}

.user-info{
  cursor:pointer;
  font-weight:600;
}

.mobile-menu-btn{
  display:none;
  width:40px;
  height:40px;
  border:none;
  background:none;
  font-size:24px;
  cursor:pointer;
}

.mobile-menu{
  display:none;
}

.hotline-btn{
  margin-left:12px;
}

:deep(.logout-dropdown-item){
  padding:8px 12px !important;
  background:transparent !important;
}

:deep(.logout-dropdown-item:hover){
  background:transparent !important;
}

.logo-wrapper{
  display:flex;
  align-items:center;
}

.logo-img{
  width:clamp(80px,10vw,100px);
  height:auto;
  object-fit:contain;
  transition:.3s;
}

.logo-img:hover{
  transform:scale(1.03);
}

/* TABLET */

@media (max-width:1024px){

  .header{
    padding:14px 20px;
  }

  .desktop-nav{
    gap:14px;
  }
}

/* MOBILE */

@media (max-width:768px){

  .header{
    padding:12px 16px;
  }

  .mobile-menu-btn{
    display:flex;
    align-items:center;
    justify-content:center;
  }

  .logo{
    flex:1;
    text-align:center;
    font-size:26px;
  }

  .desktop-nav{
    display:none;
  }

  .desktop-actions{
    display:none;
  }

  .hotline-btn{
    padding:8px 10px;
    min-width:auto;
    margin-left:0;
  }

  .mobile-menu{
    display:flex;
    flex-direction:column;

    position:absolute;
    top:100%;
    left:0;
    right:0;

    background:#fff;

    max-height:0;
    overflow:hidden;

    opacity:0;
    transform:translateY(-15px);

    transition:
        max-height .35s ease,
        opacity .3s ease,
        transform .3s ease;
  }

  .mobile-menu.active{
    max-height:500px;
    opacity:1;
    transform:translateY(0);
    display:flex;
    flex-direction:column;
    position:absolute;
    top:100%;
    left:0;
    right:0;
    background:#fff;
    border-top:1px solid #eee;
    box-shadow:0 8px 30px rgba(0,0,0,.08);
    z-index:999;
  }

  .mobile-menu a{
    padding:14px 20px;
    text-decoration:none;
    color:#333;
    border-bottom:1px solid #f5f5f5;
  }

  .mobile-login-btn{
    margin:16px;
  }

  .mobile-user{
    padding:16px;
  }

  .mobile-user-name{
    font-weight:600;
    margin-bottom:10px;
  }

  .mobile-logout-btn{
    width:100%;
    border:none;
    background:#8b1024;
    color:#fff;
    padding:10px;
    border-radius:8px;
    font-weight:600;
  }

  .mobile-menu-btn{
    width:40px;
    height:40px;
    position:relative;
    border:none;
    background:none;
    cursor:pointer;
  }

  .mobile-menu-btn span{
    position:absolute;
    left:8px;
    width:24px;
    height:2px;
    background:#8b1024;
    transition:.3s;
  }

  .mobile-menu-btn span:nth-child(1){
    top:12px;
  }

  .mobile-menu-btn span:nth-child(2){
    top:19px;
  }

  .mobile-menu-btn span:nth-child(3){
    top:26px;
  }

  .mobile-menu-btn.active span:nth-child(1){
    transform:rotate(45deg);
    top:19px;
  }

  .mobile-menu-btn.active span:nth-child(2){
    opacity:0;
  }

  .mobile-menu-btn.active span:nth-child(3){
    transform:rotate(-45deg);
    top:19px;
  }
}
</style>