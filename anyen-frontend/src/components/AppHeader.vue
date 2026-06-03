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
    <el-button class="login-btn":icon="User" @click="showLogin = true">
      ĐĂNG NHẬP
    </el-button>

    <LoginModal
        :show="showLogin"
        @close="showLogin = false"
    />

  </header>
</template>

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
</style>

<script setup>
import { User } from "@element-plus/icons-vue";
import { ref } from "vue";
import LoginModal from "../components/PopDangNhap.vue";
import {Lock} from "@element-plus/icons-vue";

const showLogin = ref(false);
</script>