<template>
  <footer class="footer">

    <div class="container-fluid footer-container">

      <div class="row footer-row">

        <!-- Logo -->
        <div class="col-lg-3 col-md-6 mb-4">
          <img :src="logoAnYen" alt="An Yên" class="logo">

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

        <!-- Về An Yên -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h2>VỀ AN YÊN</h2>

          <p>Giới thiệu</p>
          <p>Sứ mệnh - Tầm nhìn</p>
          <p>Giá trị cốt lõi</p>
        </div>

        <!-- Sản phẩm -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h2>SẢN PHẨM</h2>

          <p>Bàn thờ & Phụ kiện</p>
          <p>Hương & Nhang</p>
          <p>Đồ thờ cúng</p>
        </div>

        <!-- Dịch vụ -->
        <div class="col-lg-2 col-md-6 mb-4">
          <h2>DỊCH VỤ</h2>

          <p>Gói tang lễ</p>
          <p>Dịch vụ lễ</p>
          <p>Quy trình phục vụ</p>
        </div>

        <!-- Liên hệ -->
        <div class="col-lg-2 col-md-6 mb-4 footer-contact">
          <h2>LIÊN HỆ</h2>

          <p>📞 0000 000 000</p>
          <p>✉️ info@anyen.vn</p>
          <p>🌐 www.anyen.vn</p>
          <p>📍 Q12, HCM</p>
        </div>

      </div>

    </div>

    <!-- Bottom -->

    <div class="footer-bottom">

      <div class="container-fluid px-5">

        <div class="d-flex justify-content-between align-items-center">

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
import logoAnYen from "../assets/images/icon/logoAnYen.png";
</script>

<style src="../assets/styles/footer.css"></style>

