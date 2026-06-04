<script setup>
import heroSectionTrangSanPham from '../assets/images/TrangSanPham/heroSection_TrangSanPham.png'
import logoAnYen from "../assets/images/icon/logoAnYen.png";
import flowerIcon from "../assets/images/icon/flower_icon.png";
import dividerIcon from "../assets/images/icon/divider_icon.png";
import chungNhanImg from "../assets/images/TrangGioiThieu/chungNhanUyTin_TrangGioiThieu.png";
import anhNen1Img from "../assets/images/TrangGioiThieu/anhNen1.png";
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
    <div class="view-more-btn">
    <button class="XemDichVu-btn">XEM DỊCH VỤ</button>
    <button class="LienHe-btn">LIÊN HỆ VỚI CHÚNG TÔI </button>
    </div>

  </section>
  <section class="hero-section-2" :style="{ backgroundImage: `url(${anhNen1Img})` }">
    <div class="card">
      <div class="card-container">
          <p>Tầm Nhìn</p>
          <br>
          <p>Blackstones là công ty cung cấp dịch vụ tang lễ trọn gói chuyên nghiệp tại thị trường Việt Nam</p>
      </div>
      <div class="card-container">
        <p>Tầm Nhìn</p>
        <br>
        <p>Blackstones là công ty cung cấp dịch vụ tang lễ trọn gói chuyên nghiệp tại thị trường Việt Nam</p>
      </div>
      <div class="card-container">
        <p>Tầm Nhìn</p>
        <br>
        <p>Blackstones là công ty cung cấp dịch vụ tang lễ trọn gói chuyên nghiệp tại thị trường Việt Nam</p>
      </div>
    </div>
  </section>

  <section class="hero-section-2" :style="{ backgroundImage: `url(${chungNhanImg})` }">
    <div class="view-more-btn-2">
      <button class="TimHieu-btn">TÌM HIỂU DỊCH VỤ</button>
    </div>
  </section>


</template>
<style scoped src="../assets/styles/TrangGioiThieu.css"></style>