<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="footer-container">
        <!-- Thông tin thương hiệu -->
        <div class="footer-brand">
          <RouterLink to="/" class="footer-logo-link">
            <img
                :src="logofooter"
                alt="Logo An Yên"
                class="footer-logo"
            />
          </RouterLink>

          <p class="footer-description">
            An Yên – Đồng hành cùng gia đình trong những khoảnh khắc thiêng liêng,
            mang đến sự an lành và yên tâm trọn vẹn.
          </p>

          <div class="footer-social">
            <a href="#" aria-label="Facebook">
              <i class="fab fa-facebook-f"></i>
            </a>

            <a href="#" aria-label="YouTube">
              <i class="fab fa-youtube"></i>
            </a>

            <a href="#" aria-label="Zalo">
              <span class="zalo-text">Zalo</span>
            </a>

            <a href="#" aria-label="TikTok">
              <i class="fab fa-tiktok"></i>
            </a>
          </div>
        </div>

        <!-- Về An Yên -->
        <div class="footer-column">
          <h2>VỀ AN YÊN</h2>

          <nav class="footer-menu">
            <RouterLink to="/gioi-thieu">Giới thiệu</RouterLink>
            <RouterLink to="/gioi-thieu">Sứ mệnh - Tầm nhìn</RouterLink>
            <RouterLink to="/gioi-thieu">Giá trị cốt lõi</RouterLink>
          </nav>
        </div>

        <!-- Sản phẩm -->
        <div class="footer-column">
          <h2>SẢN PHẨM</h2>

          <nav class="footer-menu">
            <RouterLink to="/san-pham">Bàn thờ & Phụ kiện</RouterLink>
            <RouterLink to="/san-pham">Hương & Nhang</RouterLink>
            <RouterLink to="/san-pham">Đồ thờ cúng</RouterLink>
            <RouterLink to="/san-pham">Vật phẩm tâm linh</RouterLink>
            <RouterLink to="/san-pham" class="footer-more">
              Xem thêm
            </RouterLink>
          </nav>
        </div>

        <!-- Dịch vụ -->
        <div class="footer-column">
          <h2>DỊCH VỤ</h2>

          <nav class="footer-menu">
            <RouterLink to="/dich-vu">Gói tang lễ</RouterLink>
            <RouterLink to="/dich-vu">Dịch vụ lễ</RouterLink>
            <RouterLink to="/dich-vu">Quy trình phục vụ</RouterLink>
            <RouterLink to="/dich-vu">Chính sách bảo hành</RouterLink>
          </nav>
        </div>

        <!-- Tin tức -->
        <div class="footer-column">
          <h2>TIN TỨC</h2>

          <nav class="footer-menu">
            <RouterLink to="/tin-tuc">Tin tức</RouterLink>
            <RouterLink to="/tin-tuc">Kiến thức tang lễ</RouterLink>
            <RouterLink to="/tin-tuc">Hướng dẫn</RouterLink>
            <RouterLink to="/tin-tuc">Phong tục</RouterLink>
          </nav>
        </div>

        <!-- Liên hệ -->
        <div class="footer-column footer-contact">
          <h2>LIÊN HỆ</h2>

          <a href="tel:19001234" class="contact-item">
            <i class="fas fa-phone"></i>

            <span>1900 1234</span>
          </a>

          <a href="mailto:info@anyen.vn" class="contact-item">
            <i class="fas fa-envelope"></i>

            <span>info@anyen.vn</span>
          </a>

          <a
              href="https://www.anyen.vn"
              target="_blank"
              rel="noopener noreferrer"
              class="contact-item"
          >
            <i class="fas fa-globe"></i>

            <span>www.anyen.vn</span>
          </a>

          <div class="contact-item contact-address">
            <i class="fas fa-map-marker-alt"></i>

            <span>
              Quận 12, Thành phố Hồ Chí Minh
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer cuối -->
    <div class="footer-bottom">
      <div class="footer-bottom-container">
        <p>
          © {{ currentYear }} An Yên. All rights reserved.
        </p>

        <div class="footer-bottom-links">
          <a href="#" @click.prevent>
            Chính sách bảo mật
          </a>

          <span></span>

          <a href="#" @click.prevent>
            Điều khoản sử dụng
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from "vue";
import logofooter from "../../assets/images/icon/logofooter.png";

const currentYear = computed(() => new Date().getFullYear());
</script>

<style src="../../assets/styles/components/Footer.css"></style>