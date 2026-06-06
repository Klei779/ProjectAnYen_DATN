<template>
  <header class="header">

    <h2>An Yên</h2>

    <nav>
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

    <!-- Chưa đăng nhập -->
    <el-button
        v-if="!user"
        class="login-btn"
        :icon="User"
        @click="showLogin = true"
    >
      ĐĂNG NHẬP
    </el-button>

    <!-- Đã đăng nhập -->
    <el-dropdown v-else>

      <span class="user-info">
        Xin chào {{ user.hoTen }}
      </span>

      <template #dropdown>
        <el-dropdown-menu>

          <el-dropdown-item disabled>
            {{ user.loaiTaiKhoan }}
          </el-dropdown-item>

          <el-dropdown-item divided @click="logout">
            Đăng xuất
          </el-dropdown-item>

        </el-dropdown-menu>
      </template>

    </el-dropdown>

    <LoginModal
        :show="showLogin"
        @close="showLogin = false"
        @login-success="handleLoginSuccess"
    />
      <el-button
          class="hotline-btn"
          type="danger"
          @click="showHotline = true"
      >
        ☎ HOTLINE
      </el-button>
    <HotlineModal
        :show="showHotline"
        @close="showHotline = false"
    />
  </header>
</template>

<script setup>
import { ref } from "vue";
import { User } from "@element-plus/icons-vue";
import LoginModal from "../components/PopDangNhap.vue";
import HotlineModal from "../components/PopLienHeHotline.vue";

const showLogin = ref(false);
const showHotline = ref(false);

const user = ref(
    JSON.parse(localStorage.getItem("user"))
);

const handleLoginSuccess = (userData) => {

  user.value = userData;

};

import { useRouter } from "vue-router";

const router = useRouter();

const logout = () => {

  localStorage.removeItem("user");

  user.value = null;

  router.push("/");

};
</script>

<style scoped>
.header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding:20px 40px;
  border-bottom:1px solid #eee;
}

nav{
  display:flex;
  gap:20px;
}

.user-info{
  cursor:pointer;
  font-weight:600;
}
</style>