<template>
  <div class="container-fluid">
    <div class="row min-vh-100">

      <div class="col-auto p-0">
        <DoiTacSidebar />
      </div>

      <div class="col p-0" style="background-color: #f8f9fa; display: flex; flex-direction: column;">


        <div style="flex: 1;">
          <router-view />
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
// Layout đối tác chỉ chịu trách nhiệm hiển thị sidebar và router-view.
// Danh sách thông báo được tải tại TrangThongBaoDT khi người dùng mở trang,
// tránh polling nền /api/doi-tac/thong-bao liên tục trong toàn bộ phiên đăng nhập.
import DoiTacSidebar from "../components/DoiTacSidebar.vue";
</script>

<style scoped src="../assets/styles/layouts/Layout.css"></style>