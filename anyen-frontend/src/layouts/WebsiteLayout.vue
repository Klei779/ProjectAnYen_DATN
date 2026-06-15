<template>
  <AppHeader />

  <router-view />

  <AppFooter />
</template>

<script setup>
import AppHeader from "../pages/website/Header.vue";
import AppFooter from "../pages/website/Footer.vue";
</script>