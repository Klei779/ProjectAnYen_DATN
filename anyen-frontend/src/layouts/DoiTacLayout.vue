<template>
  <div class="container-fluid">
    <div class="row min-vh-100">

      <div
          class="col-auto p-0"
          style="width:280px"
      >
        <DoiTacSidebar />
      </div>

      <div class="col p-0">
        <router-view />
      </div>

    </div>
  </div>
</template>

<script setup>
import DoiTacSidebar from "../components/DoiTacSidebar.vue";
</script>