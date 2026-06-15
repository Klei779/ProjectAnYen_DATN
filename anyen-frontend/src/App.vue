<template>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';

const logSession = () => {
  const token = localStorage.getItem("token");
  const userStr = localStorage.getItem("user");
  console.log("============= SESSION INFO =============");
  if (token && userStr) {
    try {
      const user = JSON.parse(userStr);
      console.log("Status: Logged In");
      console.log("User:", user.hoTen || user.tenDangNhap);
      console.log("Role:", user.vaiTroChiTiet || user.loaiTaiKhoan);
      console.log("Token:", token.substring(0, 20) + "...");
    } catch (e) {
      console.log("Status: Error parsing user data");
    }
  } else {
    console.log("Status: Not Logged In");
  }
  console.log("========================================");
};

onMounted(() => {
  logSession();
  window.addEventListener('session-updated', logSession);
});

onUnmounted(() => {
  window.removeEventListener('session-updated', logSession);
});
</script>

<style>
.layout{
  min-height:100vh;
  display:flex;
  flex-direction:column;
}

.content{
  flex:1;
}
</style>