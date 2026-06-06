<template>
  <router-view />
</template>

<script setup>
import AppHeader from "./components/AppHeader.vue";
import AppFooter from "./components/AppFooter.vue";
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