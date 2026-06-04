<template>
  <main class="service-page">

    <!-- HERO -->
    <section
        class="service-hero"
        :style="{ backgroundImage: `url(${heroBanner})` }"
    >
      <div class="hero-overlay">

        <h1 class="hero-title">
          DỊCH VỤ MAI TÁNG AN YÊN
        </h1>

        <div class="hero-divider">
          <img
              :src="dividerIcon"
              alt="divider"
              class="divider-img"
          />
        </div>


        <p class="hero-desc">
          An Yên đồng hành cùng gia đình trong khoảnh khắc thiêng liêng,
          <br>
          mang đến sự an lành, tôn nghiêm và trọn vẹn nhất.
        </p>

      </div>
    </section>

    <!-- GÓI DỊCH VỤ -->
    <section class="service-section">
      <div class="section-heading">
        <h2>DỊCH VỤ TRỌN GÓI</h2>
        <div class="small-divider">
          <span></span>
          <i class="fa-solid fa-spa"></i>
          <span></span>
        </div>
        <p>Các gói dịch vụ được thiết kế đa dạng, phù hợp với nhu cầu và ngân sách của từng gia đình.</p>
      </div>

      <div class="package-grid">
        <div
            v-for="item in packages"
            :key="item.name"
            class="package-card"
            :class="{ popular: item.popular }"
        >
          <div v-if="item.popular" class="popular-badge">PHỔ BIẾN</div>

          <h3>{{ item.name }}</h3>
          <h5>{{ item.subtitle }}</h5>

          <img :src="item.image" alt="" />

          <ul>
            <li v-for="benefit in item.benefits" :key="benefit">
              <i class="fa-regular fa-circle-check"></i>
              {{ benefit }}
            </li>
          </ul>

          <div class="price">
            Từ <strong>{{ item.price }}</strong>
          </div>

          <button @click="goToDetail(item.id)">
            XEM CHI TIẾT
          </button>
        </div>
      </div>

      <!-- TRUST BAR -->
      <div class="trust-bar">
        <div class="trust-item">
          <i class="fa-regular fa-heart"></i>
          <div>
            <strong>TẬN TÂM</strong>
            <span>Phục vụ bằng cả tấm lòng</span>
          </div>
        </div>

        <div class="trust-item">
          <i class="fa-solid fa-shield-halved"></i>
          <div>
            <strong>UY TÍN</strong>
            <span>Cam kết minh bạch, đúng thỏa thuận</span>
          </div>
        </div>

        <div class="trust-item">
          <i class="fa-solid fa-people-group"></i>
          <div>
            <strong>CHUYÊN NGHIỆP</strong>
            <span>Đội ngũ giàu kinh nghiệm</span>
          </div>
        </div>

        <div class="trust-item">
          <i class="fa-brands fa-pagelines"></i>
          <div>
            <strong>TÔN NGHIÊM</strong>
            <span>Đảm bảo đúng nghi thức</span>
          </div>
        </div>

        <div class="trust-item">
          <i class="fa-regular fa-clock"></i>
          <div>
            <strong>HỖ TRỢ 24/7</strong>
            <span>Luôn sẵn sàng đồng hành</span>
          </div>
        </div>
      </div>
    </section>

    <!-- DỊCH VỤ LẺ -->
    <section class="service-section">
      <div class="section-heading">
        <h2>DỊCH VỤ LẺ</h2>
        <div class="small-divider">
          <span></span>
          <i class="fa-solid fa-spa"></i>
          <span></span>
        </div>
        <p>Chúng tôi cung cấp các dịch vụ lễ linh hoạt theo nhu cầu của gia đình.</p>
      </div>

      <div class="single-service-grid">
        <div v-for="item in singleServices" :key="item.title" class="single-service-card">
          <i :class="item.icon"></i>
          <h4>{{ item.title }}</h4>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </section>

    <!-- QUY TRÌNH -->
    <section class="service-section">
      <div class="section-heading">
        <h2>QUY TRÌNH PHỤC VỤ</h2>
        <div class="small-divider">
          <span></span>
          <i class="fa-solid fa-spa"></i>
          <span></span>
        </div>
        <p>Quy trình chuyên nghiệp - Minh bạch - Chu đáo</p>
      </div>

      <div class="process-wrap">
        <div v-for="(step, index) in processSteps" :key="step.title" class="process-item">
          <div class="process-icon">
            <i :class="step.icon"></i>
          </div>

          <h4>{{ index + 1 }}. {{ step.title }}</h4>
          <p>{{ step.desc }}</p>

          <span v-if="index < processSteps.length - 1" class="process-arrow">→</span>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="service-cta">
      <div class="cta-left">
        <img :src="lotusImg" alt="" />
        <h3>
          AN YÊN ĐỒNG HÀNH CÙNG GIA ĐÌNH <br />
          TRONG KHOẢNH KHẮC THIÊNG LIÊNG NHẤT
        </h3>
      </div>

    </section>

  </main>
</template>

<script setup>
import heroBanner from "../assets/images/TrangSanPham/heroSection_TrangSanPham.png";
import dividerIcon from "../assets/images/icon/flower_icon.png";

import package1 from "../assets/images/TrangDichVu/goi1.png";
import package2 from "../assets/images/TrangDichVu/goi2.png";
import package3 from "../assets/images/TrangDichVu/goi3.png";
import package4 from "../assets/images/TrangDichVu/goi4.png";
import lotusImg from "../assets/images/TrangDichVu/lotus.png";

import { useRouter } from 'vue-router'

const router = useRouter()

const goToDetail = (id) => {
  router.push(`/dich-vu/${id}`)
}

const packages = [
  {
    id: 1,
    name: "GÓI TIẾT KIỆM",
    subtitle: "Trang nghiêm - Chuẩn mực",
    image: package1,
    price: "15.000.000 đ",
    benefits: [
      "Tư vấn và hỗ trợ 24/7",
      "Xe đưa đón theo chương trình",
      "Sảnh, bàn ghế, phông rạp cơ bản",
      "Áo quan, đồ dùng cơ bản",
      "Nhân sự phục vụ tang lễ",
    ],
  },
  {
    id: 2,
    name: "GÓI TIÊU CHUẨN",
    subtitle: "Trang trọng - Chu đáo",
    image: package2,
    price: "25.000.000 đ",
    popular: true,
    benefits: [
      "Tư vấn và hỗ trợ 24/7",
      "Xe đưa đón theo chương trình",
      "Sảnh, bàn ghế, phông rạp cao cấp",
      "Áo quan, đồ dùng cao cấp",
      "Nhân sự phục vụ chuyên nghiệp",
      "Nghi thức theo tôn giáo nếu có",
    ],
  },
  {
    id: 3,
    name: "GÓI CAO CẤP",
    subtitle: "Tận tâm - Chu toàn",
    image: package3,
    price: "40.000.000 đ",
    benefits: [
      "Tư vấn và hỗ trợ 24/7",
      "Xe đưa đón cao cấp",
      "Sảnh, bàn ghế, phông rạp cao cấp",
      "Áo quan cao cấp, nhập khẩu",
      "Nhân sự phục vụ chuyên nghiệp",
      "Nghi thức theo tôn giáo nếu có",
      "Quay phim, chụp ảnh tang lễ",
    ],
  },
  {
    id: 4,
    name: "GÓI VIP",
    subtitle: "Vĩnh biệt - Vẹn tròn",
    image: package4,
    price: "80.000.000 đ",
    benefits: [
      "Tư vấn và hỗ trợ 24/7",
      "Xe đưa đón hạng sang",
      "Sảnh, bàn ghế, phông rạp cao cấp",
      "Áo quan cao cấp, nhập khẩu",
      "Nhân sự phục vụ tận tâm 1:1",
      "Nghi thức theo tôn giáo nếu có",
      "Quay phim, chụp ảnh, live stream",
      "Hậu sự, cải táng, lưu tro cốt",
    ],
  },
];

const singleServices = [
  {
    icon: "fa-solid fa-van-shuttle",
    title: "XE TANG LỄ",
    desc: "Đa dạng các dòng xe tang lễ hiện đại",
  },
  {
    icon: "fa-solid fa-table",
    title: "TRANG TRÍ TANG LỄ",
    desc: "Trang trí sảnh, bàn thờ, phông rạp theo yêu cầu",
  },
  {
    icon: "fa-solid fa-box",
    title: "ÁO QUAN",
    desc: "Nhiều mẫu mã, chất liệu cao cấp",
  },
  {
    icon: "fa-brands fa-pagelines",
    title: "VÒNG HOA",
    desc: "Vòng hoa tươi, hoa lan, hoa sen cao cấp",
  },
  {
    icon: "fa-regular fa-file-lines",
    title: "THIỆP TANG",
    desc: "Thiệp báo tin, cảm ơn thiết kế trang trọng",
  },
  {
    icon: "fa-solid fa-jar",
    title: "HẬU SỰ",
    desc: "Hỏa táng, lưu tro cốt, cải táng",
  },
  {
    icon: "fa-solid fa-people-group",
    title: "NHÂN SỰ",
    desc: "Cung cấp nhân sự phục vụ tang lễ chuyên nghiệp",
  },
];

const processSteps = [
  {
    icon: "fa-solid fa-phone",
    title: "TIẾP NHẬN",
    desc: "Tiếp nhận thông tin, tư vấn ban đầu 24/7",
  },
  {
    icon: "fa-regular fa-clipboard",
    title: "KHẢO SÁT & TƯ VẤN",
    desc: "Khảo sát, tư vấn gói dịch vụ phù hợp",
  },
  {
    icon: "fa-regular fa-file-lines",
    title: "THỎA THUẬN",
    desc: "Báo giá chi tiết, ký kết hợp đồng minh bạch",
  },
  {
    icon: "fa-solid fa-people-group",
    title: "TRIỂN KHAI",
    desc: "Chuẩn bị và triển khai theo đúng cam kết",
  },
  {
    icon: "fa-regular fa-heart",
    title: "ĐỒNG HÀNH",
    desc: "Đồng hành xuyên suốt, hỗ trợ tận tâm",
  },
  {
    icon: "fa-brands fa-pagelines",
    title: "HẬU MÃI",
    desc: "Hỗ trợ hậu sự và chăm sóc sau tang lễ",
  },
];

</script>

<style scoped src="../assets/styles/TrangDichVu.css"></style>