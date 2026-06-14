<template>
  <footer class="footer">

    <div class="container-fluid footer-container">

      <!-- Logo -->
      <div class="footer-brand">

        <img
            :src="logofooter"
            alt="An Yên"
            class="logo"
        >

        <p class="footer-desc">
          An Yên - Luôn đồng hành cùng gia đình trong
          những khoảnh khắc thiêng liêng
        </p>

        <div class="social">
          <i class="fab fa-facebook-f"></i>
          <i class="fab fa-instagram"></i>
          <i class="fab fa-youtube"></i>
          <i class="fab fa-tiktok"></i>
        </div>

      </div>

      <!-- Menu -->
      <div class="row footer-row">

        <!-- Về An Yên -->
        <div class="col-6 col-lg-3 footer-col">
          <h2>VỀ AN YÊN</h2>

          <p>Giới thiệu</p>
          <p>Sứ mệnh - Tầm nhìn</p>
          <p>Giá trị cốt lõi</p>
        </div>

        <!-- Sản phẩm -->
        <div class="col-6 col-lg-3 footer-col">
          <h2>SẢN PHẨM</h2>

          <p>Bàn thờ & Phụ kiện</p>
          <p>Hương & Nhang</p>
          <p>Đồ thờ cúng</p>
        </div>

        <!-- Dịch vụ -->
        <div class="col-6 col-lg-3 footer-col">
          <h2>DỊCH VỤ</h2>

          <p>Gói tang lễ</p>
          <p>Dịch vụ lễ</p>
          <p>Quy trình phục vụ</p>
        </div>

        <!-- Liên hệ -->
        <div class="col-6 col-lg-3 footer-col">
          <h2>LIÊN HỆ</h2>

          <p>📞 0000 000 000</p>
          <p>✉️ info@anyen.vn</p>
          <p>🌐 www.anyen.vn</p>
          <p>📍 Q12, HCM</p>
        </div>

      </div>

    </div>

    <div class="footer-bottom">

      <div class="container-fluid">

        <div class="footer-bottom-inner">

          <span>
            © 2024 An Yên. All rights reserved.
          </span>

          <div class="footer-links">
            <p>Chính sách bảo mật</p>
            <p>Điều khoản sử dụng</p>
          </div>

        </div>

      </div>

    </div>

  </footer>
</template>

<script setup>
import logofooter from "../assets/images/icon/logofooter.png";
</script>

<style src="../assets/styles/footer.css"></style>