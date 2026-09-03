<script setup>
import { useRouter } from "vue-router";

import heroSectionTrangSanPham from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";
import logoAnYen from "../../assets/images/icon/logoAnYen.png";
import flowerIcon from "../../assets/images/icon/flower_icon.png";
import dividerIcon from "../../assets/images/icon/divider_icon.png";

import chungNhanImg from "../../assets/images/TrangGioiThieu/chungNhanUyTin_TrangGioiThieu.png";
import tangLeImg from "../../assets/images/TrangGioiThieu/hinh-anh-tang-le-1.png";
import nhanVienImg from "../../assets/images/TrangGioiThieu/nhanVienTangLe.png";

import LeeMeiYu from "../../assets/images/TrangGioiThieu/LeeMeiYu-img.png";
import Cocolonet from "../../assets/images/TrangGioiThieu/cocolonet.png";
import YapKimSiong from "../../assets/images/TrangGioiThieu/YapKimSiong.png";

import BanKinhSu from "../../assets/images/TrangGioiThieu/BanKinhSu.png";
import HoaThuong from "../../assets/images/TrangGioiThieu/hoaThuong.png";
import DucCha from "../../assets/images/TrangGioiThieu/ducCha.png";

const router = useRouter();

const goTo = (path) => {
  router.push(path);
};

const highlights = [
  {
    number: "24/7",
    title: "Luôn sẵn sàng",
    desc: "Tiếp nhận và hỗ trợ gia đình mọi thời điểm.",
  },
  {
    number: "100%",
    title: "Minh bạch",
    desc: "Quy trình rõ ràng, hạn chế phát sinh chi phí.",
  },
  {
    number: "Trọn gói",
    title: "Đồng hành",
    desc: "Hỗ trợ gia đình trước, trong và sau tang lễ.",
  },
];

const reasons = [
  "Đội ngũ nhân sự tận tâm, lịch sự và chuyên nghiệp.",
  "Quy trình tổ chức tang lễ rõ ràng, trang trọng.",
  "Sản phẩm và dịch vụ được chuẩn hóa theo nhu cầu gia đình.",
  "Kết nối với nhiều đối tác uy tín trong ngành tang lễ.",
];

const values = [
  {
    title: "Tận tâm",
    desc: "Lắng nghe, thấu hiểu và đồng hành cùng gia quyến trong thời khắc khó khăn.",
  },
  {
    title: "Chuyên nghiệp",
    desc: "Tổ chức từng hạng mục theo quy trình chỉn chu, trang nghiêm và đúng nghi thức.",
  },
  {
    title: "Minh bạch",
    desc: "Thông tin dịch vụ, chi phí và tiến độ được trao đổi rõ ràng với khách hàng.",
  },
];

const experts = [
  {
    image: LeeMeiYu,
    name: "Bà Lee Mei Yu & Cộng Sự",
    desc: "Chuyên gia tư vấn 30 năm kinh nghiệm tổ chức tang lễ tại Đài Loan.",
  },
  {
    image: Cocolonet,
    name: "Tập đoàn Cocolonet Nhật Bản",
    desc: "80 năm kinh nghiệm trong ngành dịch vụ tang lễ.",
  },
  {
    image: YapKimSiong,
    name: "Ông Yap Kim Siong",
    desc: "Chuyên gia tư vấn 20 năm kinh nghiệm tổ chức tang lễ tại Malaysia.",
  },
];
</script>

<template>
  <main class="about-page">
    <!-- HERO -->
    <section
        class="hero"
        :style="{ backgroundImage: `url(${heroSectionTrangSanPham})` }"
    >
      <div class="hero-overlay"></div>

      <div class="hero-inner">
        <div class="hero-content">
          <img :src="logoAnYen" alt="An Yên" class="hero-logo" />

          <p class="hero-label">Dịch vụ mai táng An Yên</p>

          <img :src="flowerIcon" alt="Hoa sen" class="hero-flower" />

          <h1>An lành tiễn biệt</h1>
          <h2>Yên tâm vĩnh hằng</h2>

          <img :src="dividerIcon" alt="Divider" class="hero-divider" />

          <p class="hero-desc">
            Đồng hành cùng gia đình trong từng khoảnh khắc tiễn biệt bằng sự
            tận tâm, trang nghiêm và minh bạch.
          </p>

          <div class="hero-actions">
            <button class="btn btn-primary" @click="goTo('/dich-vu')">
              Xem dịch vụ
            </button>

            <button class="btn btn-outline" @click="goTo('/lien-he')">
              Liên hệ
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- HIGHLIGHT -->
    <section class="highlight-section">
      <div class="container highlight-grid">
        <article
            v-for="item in highlights"
            :key="item.title"
            class="highlight-card"
        >
          <strong>{{ item.number }}</strong>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </article>
      </div>
    </section>

    <!-- STORY -->
    <section class="story-section">
      <div class="container story-grid">
        <div class="image-box">
          <img :src="tangLeImg" alt="Tang lễ An Yên" />
        </div>

        <div class="story-content">
          <p class="section-label">Về An Yên</p>

          <h2>Sự ra đời của An Yên</h2>

          <p>
            An Yên ra đời với mong muốn mang đến một dịch vụ tang lễ hiện đại,
            trang trọng và chuyên nghiệp, giúp gia đình an tâm hơn trong thời
            khắc tiễn biệt người thân.
          </p>

          <p>
            Chúng tôi không chỉ cung cấp dịch vụ, mà còn đồng hành cùng gia quyến
            từ khâu chuẩn bị, tổ chức nghi lễ cho đến những hỗ trợ sau tang lễ.
          </p>

          <div class="quote-box">
            <span>“</span>
            <p>
              Mỗi tang lễ không chỉ là một nghi thức, mà còn là lời tiễn biệt
              cuối cùng đầy yêu thương và kính trọng.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- WHY -->
    <section class="why-section">
      <div class="container why-grid">
        <div class="why-content">
          <p class="section-label">Tại sao chọn An Yên?</p>

          <h2>Chu đáo trong từng chi tiết</h2>

          <div class="reason-list">
            <div
                v-for="reason in reasons"
                :key="reason"
                class="reason-item"
            >
              {{ reason }}
            </div>
          </div>
        </div>

        <div class="image-box">
          <img :src="nhanVienImg" alt="Đội ngũ An Yên" />
        </div>
      </div>
    </section>

    <!-- VALUES -->
    <section class="value-section">
      <div class="container">
        <div class="section-heading section-heading-light">
          <p class="section-label">Giá trị cốt lõi</p>
          <h2>Điều An Yên luôn gìn giữ</h2>
        </div>

        <div class="value-grid">
          <article
              v-for="item in values"
              :key="item.title"
              class="value-card"
          >
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </article>
        </div>
      </div>
    </section>

    <!-- EXPERT -->
    <section class="expert-section">
      <div class="container">
        <div class="section-heading">
          <p class="section-label">Ban cố vấn nghiệp vụ</p>

          <h2>Chuyên gia nước ngoài</h2>

          <p>
            An Yên được tư vấn và hướng dẫn bởi đội ngũ chuyên gia quốc tế có
            nhiều năm kinh nghiệm trong lĩnh vực tang lễ.
          </p>
        </div>

        <div class="expert-grid">
          <article
              v-for="expert in experts"
              :key="expert.name"
              class="expert-card"
          >
            <img :src="expert.image" :alt="expert.name" />
            <h3>{{ expert.name }}</h3>
            <p>{{ expert.desc }}</p>
          </article>
        </div>
      </div>
    </section>
    <!-- RITUAL -->
    <section class="ritual-section">
      <div class="container ritual-layout">
        <div class="ritual-content">
          <p class="section-label">Ban tư vấn nghi lễ</p>

          <h2>Trọn vẹn theo từng tín ngưỡng</h2>

          <p>
            Phật giáo, Công giáo, Cao Đài, Tin Lành, Người gốc Hoa… thường có
            những nghi thức tang lễ khác nhau. An Yên phối hợp cùng Ban tư vấn
            nghi lễ để tổ chức tang lễ trang nghiêm, đúng nghi thức và phù hợp
            với từng gia đình.
          </p>
        </div>

        <div class="ritual-cards">
          <article class="ritual-card">
            <img :src="BanKinhSu" alt="Ban Kinh Sư" />
            <h3>Ban Kinh Sư Chuyên Trách</h3>
            <p>Điều hành và cố vấn nghi lễ Phật giáo.</p>
          </article>

          <article class="ritual-card">
            <div class="ritual-double">
              <img :src="HoaThuong" alt="Quý Hòa Thượng" />
              <img :src="DucCha" alt="Đức Cha" />
            </div>

            <h3>Quý Hòa Thượng, Đức Cha</h3>
            <p>Đồng hành cùng Ban Hành Giáo tại Việt Nam.</p>
          </article>
        </div>
      </div>
    </section>

    <!-- CERTIFICATE -->
    <section class="certificate-section">
      <img :src="chungNhanImg" alt="Chứng nhận An Yên" />

      <button class="certificate-btn" @click="goTo('/dich-vu')">
        Tìm hiểu dịch vụ
      </button>
    </section>
  </main>
</template>

<style scoped src="../../assets/styles/website/TrangGioiThieu.css"></style>