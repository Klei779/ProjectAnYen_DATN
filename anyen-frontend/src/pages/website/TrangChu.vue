<template>
  <div class="home-page">
    <!-- ================= HEADER ================= -->

    <!-- ================= HERO ================= -->
    <section
        class="hero-section"
        :style="{ backgroundImage: `url(${heroBackground})` }"
    >
      <div class="hero-light-overlay"></div>

      <div class="hero-container">
        <div class="hero-content">
          <img
              :src="logoAnYen"
              alt="Dịch vụ mai táng An Yên"
              class="hero-main-logo"
          />

          <div class="hero-title-wrapper">
            <span class="hero-title-line"></span>

            <h1>Dịch vụ mai táng An Yên</h1>

            <span class="hero-title-line"></span>
          </div>

          <div class="hero-flower" aria-hidden="true">
            <img :src="flowerIcon" alt="" />

          </div>

          <p class="hero-slogan">
            An lành tiễn biệt – Yên tâm vĩnh hằng
          </p>


          <p class="hero-description">
            An Yên đồng hành cùng gia đình trong khoảnh khắc thiêng liêng,
            mang đến sự an lành, tôn nghiêm và trọn vẹn nhất.
          </p>

          <div class="hero-actions">
            <RouterLink
                to="/dich-vu"
                class="hero-button primary-button"
            >
              Tìm hiểu dịch vụ
            </RouterLink>

            <RouterLink
                to="/gioi-thieu"
                class="hero-button outline-button"
            >
              Về chúng tôi
            </RouterLink>

          </div>
        </div>
      </div>
    </section>

    <!-- ================= DỊCH VỤ ================= -->
    <section class="services-section">
      <div class="section-container">
        <div class="section-heading">
          <p class="section-label">Dịch vụ tận tâm</p>

          <h2>Dịch vụ của chúng tôi</h2>

          <div class="heading-decoration" aria-hidden="true">

            <img :src="flowerIcon" alt="" />

          </div>
        </div>

        <div class="services-grid">
          <article
              v-for="service in services"
              :key="service.title"
              class="service-card"
          >
            <div
                class="service-icon"
                :class="
                service.iconColor === 'blue'
                  ? 'icon-blue'
                  : 'icon-red'
              "
            >
              <i :class="service.icon"></i>
            </div>

            <h3>{{ service.title }}</h3>

            <p>{{ service.description }}</p>

            <RouterLink :to="service.path" class="service-link">
              Xem chi tiết
              <i class="fa-solid fa-arrow-right"></i>
            </RouterLink>
          </article>
        </div>
      </div>
    </section>


    <!-- ================= THỐNG KÊ ================= -->
    <section class="statistics-section">
      <div class="statistics-container">
        <article
            v-for="(statistic, index) in statistics"
            :key="statistic.number"
            class="statistic-item"
        >
          <div class="statistic-icon">
            <i :class="statistic.icon"></i>
          </div>

          <div class="statistic-content">
            <strong>{{ statistic.number }}</strong>
            <span>{{ statistic.label }}</span>
          </div>

          <div
              v-if="index < statistics.length - 1"
              class="statistic-divider"
          ></div>
        </article>
      </div>
    </section>

    <!-- ================= VỀ AN YÊN ================= -->
    <section class="about-section">
      <div
          class="about-image"
          :style="{ backgroundImage: `url(${heroBackground})` }"
      >
        <div class="about-image-overlay"></div>
      </div>

      <div class="about-content">
        <div class="about-content-inner">
          <p class="about-label">Về An Yên</p>

          <h2>
            Đồng hành bằng sự
            <span>tận tâm và thấu hiểu</span>
          </h2>

          <div class="about-title-line"></div>

          <p class="about-description">
            Với tâm niệm
            <strong>
              “An lành tiễn biệt – Yên tâm vĩnh hằng”
            </strong>,
            An Yên luôn nỗ lực mang đến dịch vụ mai táng nhân văn,
            chuyên nghiệp và tận tâm nhất.
          </p>

          <div class="values-grid">
            <article
                v-for="value in coreValues"
                :key="value.title"
                class="value-item"
            >
              <div
                  class="value-icon"
                  :class="
                  value.color === 'blue'
                    ? 'value-blue'
                    : 'value-red'
                "
              >
                <i :class="value.icon"></i>
              </div>

              <div class="value-content">
                <h3>{{ value.title }}</h3>
                <p>{{ value.description }}</p>
              </div>
            </article>
          </div>

          <RouterLink to="/gioi-thieu" class="about-button">
            Tìm hiểu thêm
          </RouterLink>
        </div>
      </div>
    </section>


  </div>
</template>

<script setup>
import { computed, ref } from "vue";

import heroBackground from "../../assets/images/TrangChu/imageTrangChu.png";
import logoAnYen from "../../assets/images/icon/logoAnYen.png";
import logoFooter from "../../assets/images/icon/logofooter.png";
import flowerIcon from "../../assets/images/icon/flower_icon.png";

const mobileMenuOpen = ref(false);

const currentYear = computed(() => new Date().getFullYear());

const menuItems = [
  {
    name: "TRANG CHỦ",
    path: "/",
  },
  {
    name: "GIỚI THIỆU",
    path: "/gioi-thieu",
  },
  {
    name: "DỊCH VỤ",
    path: "/dich-vu",
  },
  {
    name: "SẢN PHẨM",
    path: "/san-pham",
  },
  {
    name: "TIN TỨC",
    path: "/tin-tuc",
  },
  {
    name: "LIÊN HỆ",
    path: "/lien-he",
  },
];

const services = [
  {
    title: "TANG LỄ TRỌN GÓI",
    description:
        "Tổ chức tang lễ trọn gói, chu đáo từ A đến Z, đảm bảo trang nghiêm và đúng nghi thức.",
    icon: "fa-solid fa-spa",
    iconColor: "red",
    path: "/dich-vu",
  },
  {
    title: "NHÀ TANG LỄ",
    description:
        "Hệ thống nhà tang lễ hiện đại, không gian trang nghiêm, tiện nghi và riêng tư.",
    icon: "fa-solid fa-landmark",
    iconColor: "blue",
    path: "/dich-vu",
  },
  {
    title: "HỎA TÁNG",
    description:
        "Dịch vụ hỏa táng văn minh, hiện đại, thân thiện với môi trường và tôn trọng tâm linh.",
    icon: "fa-solid fa-fire-flame-curved",
    iconColor: "red",
    path: "/dich-vu",
  },
  {
    title: "HẬU SỰ SAU TANG",
    description:
        "Hỗ trợ gia đình các thủ tục sau tang lễ, pháp lý, cải táng và tưởng niệm.",
    icon: "fa-solid fa-feather",
    iconColor: "blue",
    path: "/dich-vu",
  },
];

const coreValues = [
  {
    title: "TẬN TÂM",
    description:
        "Đặt sự thấu hiểu và đồng hành của gia đình lên hàng đầu.",
    icon: "fa-regular fa-heart",
    color: "red",
  },
  {
    title: "TRANG NGHIÊM",
    description:
        "Tôn trọng nghi lễ truyền thống, giữ trọn sự trang nghiêm.",
    icon: "fa-solid fa-shield-halved",
    color: "blue",
  },
  {
    title: "NHÂN VĂN",
    description:
        "Dịch vụ được thực hiện với sự nhân văn và yêu thương.",
    icon: "fa-solid fa-feather",
    color: "blue",
  },
  {
    title: "CHUYÊN NGHIỆP",
    description:
        "Đội ngũ giàu kinh nghiệm, quy trình rõ ràng, minh bạch.",
    icon: "fa-solid fa-people-group",
    color: "red",
  },
];

const statistics = [
  {
    number: "10.000+",
    label: "Gia đình đã tin tưởng",
    icon: "fa-solid fa-users",
  },
  {
    number: "15+",
    label: "Năm kinh nghiệm",
    icon: "fa-solid fa-spa",
  },
  {
    number: "24/7",
    label: "Hỗ trợ tận tâm",
    icon: "fa-solid fa-hand-holding-heart",
  },
  {
    number: "20+",
    label: "Chi nhánh toàn quốc",
    icon: "fa-solid fa-location-dot",
  },
];
</script>

<style src="../../assets/styles/components/TrangChu.css"></style>