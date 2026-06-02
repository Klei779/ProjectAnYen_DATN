<script setup>
import heroSectionTrangSanPham from '../assets/images/TrangSanPham/heroSection_TrangSanPham.png'
import logoAnYen from "../assets/images/icon/logoAnYen.png";
import flowerIcon from "../assets/images/icon/flower_icon.png";
import dividerIcon from "../assets/images/icon/divider_icon.png";
</script>
<template>
  <section class="hero-section" :style="{ backgroundImage: `url(${heroSectionTrangSanPham})` }">
    <div class="hero-logo">
      <img :src="logoAnYen" alt="logo" class="hero-AnYen" style = ""/>
    </div>
    <p class="hero-subtitle-1">DỊCH VỤ MAI TÁNG AN YÊN</p>
    <div class="hero-divider">
      <img :src="flowerIcon" alt="Hoa" class="lotus-icon-img" />
    </div>
    <p class="hero-subtitle-2">AN LÀNH TIỄN BIỆT - YÊN TÂM VĨNH HẰNG</p>
    <div class="hero-divider">
      <img :src="dividerIcon" alt="Ngan" class="divider-icon" />
    </div>
  </section>
</template>
<style scoped src="../assets/styles/TrangGioiThieu.css"></style>