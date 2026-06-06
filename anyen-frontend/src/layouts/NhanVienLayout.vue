<template>
  <div class="container-fluid">
    <div class="row min-vh-100">

      <!-- Sidebar -->
      <div
          class="col-auto p-0"
          style="width:280px"
      >
        <NhanVienSidebar />
      </div>

      <!-- Nội dung -->
      <div class="col p-0">
        <router-view />
      </div>

    </div>
  </div>
</template>

<script setup>
import NhanVienSidebar from "../components/NhanVienSidebar.vue";
</script>