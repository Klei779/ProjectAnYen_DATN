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

<style scoped>
.about-page {
  width: 100%;
  overflow-x: hidden;
  color: #0b3453;
  background: #fbf6ef;
  font-family: "Be Vietnam Pro", "Noto Sans", Arial, sans-serif;
}

.container {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
}

/* ================= HERO ================= */
.hero {
  position: relative;
  min-height: 620px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
      90deg,
      rgba(255, 250, 242, 0.9) 0%,
      rgba(255, 250, 242, 0.73) 37%,
      rgba(255, 250, 242, 0.28) 68%,
      rgba(255, 250, 242, 0.05) 100%
  );
}

.hero-inner {
  position: relative;
  z-index: 2;
  min-height: 620px;
  display: flex;
  align-items: center;
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
}

.hero-content {
  width: 520px;
  text-align: center;
}

.hero-logo {
  width: 205px;
  display: block;
  margin: 0 auto 16px;
}

.hero-label {
  margin: 0;
  color: #0b3453;
  font-size: 22px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.hero-flower {
  width: 122px;
  display: block;
  margin: 16px auto 14px;
}

.hero h1 {
  margin: 0;
  color: #a60b27;
  font-size: 45px;
  line-height: 1.1;
  font-weight: 950;
  text-transform: uppercase;
}

.hero h2 {
  margin: 6px 0 0;
  color: #a60b27;
  font-size: 31px;
  line-height: 1.15;
  font-weight: 950;
  text-transform: uppercase;
}

.hero-divider {
  width: 170px;
  display: block;
  margin: 18px auto;
}

.hero-desc {
  width: min(430px, 100%);
  margin: 0 auto 24px;
  color: #123d5d;
  font-size: 16px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn {
  height: 50px;
  padding: 0 28px;
  border-radius: 999px;
  border: 2px solid transparent;
  cursor: pointer;
  font-size: 15px;
  font-weight: 900;
  text-transform: uppercase;
  transition: 0.25s ease;
}

.btn-primary {
  color: #fff;
  background: #a60b27;
  border-color: #a60b27;
  box-shadow: 0 12px 26px rgba(166, 11, 39, 0.22);
}

.btn-primary:hover {
  background: #85091f;
  border-color: #85091f;
  transform: translateY(-2px);
}

.btn-outline {
  color: #a60b27;
  background: rgba(255, 255, 255, 0.76);
  border-color: #a60b27;
}

.btn-outline:hover {
  color: #fff;
  background: #a60b27;
  transform: translateY(-2px);
}

/* ================= HIGHLIGHT ================= */
.highlight-section {
  padding: 54px 0 18px;
  background: #fbf6ef;
}

.highlight-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 26px;
}

.highlight-card {
  padding: 30px 28px;
  border-radius: 22px;
  background: #fff;
  border: 1px solid rgba(166, 11, 39, 0.08);
  box-shadow: 0 14px 34px rgba(11, 52, 83, 0.1);
  transition: 0.25s ease;
}

.highlight-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 22px 46px rgba(11, 52, 83, 0.15);
}

.highlight-card strong {
  display: block;
  margin-bottom: 8px;
  color: #a60b27;
  font-size: 34px;
  line-height: 1;
  font-weight: 950;
}

.highlight-card h3 {
  margin: 0 0 8px;
  color: #0b3453;
  font-size: 21px;
  font-weight: 900;
}

.highlight-card p {
  margin: 0;
  color: #526a7d;
  font-size: 15.5px;
  line-height: 1.6;
}

/* ================= COMMON ================= */
.section-label {
  margin: 0 0 12px;
  color: #b48226;
  font-size: 14px;
  font-weight: 950;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.section-heading {
  max-width: 820px;
  margin: 0 auto 42px;
  text-align: center;
}

.section-heading h2,
.story-content h2,
.why-content h2,
.ritual-content h2 {
  margin: 0 0 18px;
  color: #a60b27;
  font-size: 42px;
  line-height: 1.2;
  font-weight: 950;
}

.section-heading p,
.story-content p,
.ritual-content p {
  margin: 0 0 16px;
  color: #123d5d;
  font-size: 17px;
  line-height: 1.75;
}

/* ================= STORY ================= */
.story-section {
  padding: 62px 0 76px;
  background: #fbf6ef;
}

.story-grid {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 58px;
  align-items: center;
}

.image-box {
  position: relative;
}

.image-box::after {
  content: "";
  position: absolute;
  right: -18px;
  bottom: -18px;
  width: 92%;
  height: 92%;
  border-radius: 28px;
  background: rgba(166, 11, 39, 0.13);
  z-index: -1;
}

.image-box img {
  width: 100%;
  height: 390px;
  display: block;
  object-fit: cover;
  border-radius: 28px;
  box-shadow: 0 18px 42px rgba(11, 52, 83, 0.18);
}

.quote-box {
  margin-top: 26px;
  padding: 24px 26px;
  border-radius: 22px;
  background: #fff;
  border-left: 5px solid #a60b27;
  box-shadow: 0 12px 30px rgba(11, 52, 83, 0.08);
  display: flex;
  gap: 16px;
}

.quote-box span {
  color: #a60b27;
  font-size: 52px;
  line-height: 0.9;
  font-weight: 950;
}

.quote-box p {
  margin: 0;
}

/* ================= WHY ================= */
.why-section {
  padding: 76px 0;
  background: #efe2d6;
}

.why-grid {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 58px;
  align-items: center;
}

.reason-list {
  display: grid;
  gap: 14px;
  margin-top: 26px;
}

.reason-item {
  position: relative;
  padding: 16px 18px 16px 50px;
  border-radius: 16px;
  background: #fff;
  color: #0b3453;
  font-size: 16px;
  line-height: 1.65;
  box-shadow: 0 8px 22px rgba(11, 52, 83, 0.07);
}

.reason-item::before {
  content: "✓";
  position: absolute;
  left: 20px;
  top: 16px;
  color: #a60b27;
  font-weight: 950;
}

/* ================= VALUES ================= */
.value-section {
  padding: 78px 0;
  background: #0b3453;
}

.section-heading-light h2 {
  color: #fff;
}

.section-heading-light .section-label {
  color: #f0c879;
}

.value-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 26px;
}

.value-card {
  padding: 34px 30px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.16);
  color: #fff;
}

.value-card h3 {
  margin: 0 0 14px;
  color: #f0c879;
  font-size: 28px;
  font-weight: 950;
}

.value-card p {
  margin: 0;
  color: #eef6fb;
  font-size: 16px;
  line-height: 1.7;
}

/* ================= EXPERT ================= */
.expert-section {
  padding: 78px 0;
  background: #fbf6ef;
}

.expert-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 26px;
}

.expert-card,
.ritual-card {
  padding: 34px 28px;
  border-radius: 24px;
  text-align: center;
  background: #fff;
  box-shadow: 0 16px 38px rgba(11, 52, 83, 0.09);
  border: 1px solid rgba(180, 130, 38, 0.16);
  transition: 0.25s ease;
}

.expert-card:hover,
.ritual-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 48px rgba(11, 52, 83, 0.14);
}

.expert-card img,
.ritual-card img {
  width: 118px;
  height: 118px;
  object-fit: contain;
  padding: 7px;
  margin-bottom: 18px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid #b48226;
}

.expert-card h3,
.ritual-card h3 {
  margin: 0 0 12px;
  color: #b48226;
  font-size: 22px;
  font-weight: 950;
}

.expert-card p,
.ritual-card p {
  margin: 0;
  color: #123d5d;
  font-size: 16px;
  line-height: 1.65;
}

/* ================= RITUAL ================= */
.ritual-section {
  padding: 76px 0;
  background: #f1e6dc;
}

.ritual-layout {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 54px;
  align-items: center;
}

.ritual-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.ritual-double {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* ================= CERTIFICATE ================= */
.certificate-section {
  position: relative;
  width: 100%;
  background: #fbf6ef;
  overflow: hidden;
}

.certificate-section img {
  width: 100%;
  min-height: 420px;
  display: block;
  object-fit: cover;
  object-position: center;
}

.certificate-btn {
  position: absolute;
  left: 73.5%;
  bottom: 8%;
  transform: translateX(-50%);

  height: 52px;
  padding: 0 34px;
  border-radius: 999px;
  border: 2px solid #a60b27;

  background: rgba(255, 255, 255, 0.86);
  color: #a60b27;

  cursor: pointer;
  font-size: 15px;
  font-weight: 900;
  text-transform: uppercase;
  white-space: nowrap;

  backdrop-filter: blur(4px);
  transition: 0.25s ease;
}

.certificate-btn:hover {
  background: #a60b27;
  color: #fff;
  transform: translateX(-50%) translateY(-2px);
}
/* ================= RESPONSIVE ================= */
@media (max-width: 1024px) {
  .hero,
  .hero-inner {
    min-height: 580px;
  }

  .hero-content {
    width: 500px;
  }

  .hero h1 {
    font-size: 40px;
  }

  .hero h2 {
    font-size: 29px;
  }

  .story-grid,
  .why-grid,
  .ritual-layout {
    grid-template-columns: 1fr;
  }

  .image-box {
    max-width: 650px;
    margin: 0 auto;
  }

  .highlight-grid,
  .value-grid,
  .expert-grid {
    grid-template-columns: 1fr;
  }

  .highlight-card,
  .value-card,
  .expert-card {
    max-width: 680px;
    margin: 0 auto;
  }

  .ritual-cards {
    grid-template-columns: 1fr;
  }
}

/* =========================================================
   MOBILE BEAUTY FIX - DÁN CUỐI FILE CSS
   ========================================================= */

/* =========================================================
   MOBILE RESPONSIVE FIX - ĐẸP HƠN CHO ĐIỆN THOẠI
   ========================================================= */

@media (max-width: 768px) {
  .about-page {
    background: #fbf6ef;
  }

  .container {
    width: calc(100% - 28px);
  }

  /* HERO */
  .hero,
  .hero-inner {
    min-height: 500px;
  }

  .hero {
    background-position: 62% center;
  }

  .hero-overlay {
    background: linear-gradient(
        180deg,
        rgba(255, 250, 242, 0.82) 0%,
        rgba(255, 250, 242, 0.72) 48%,
        rgba(255, 250, 242, 0.42) 100%
    );
  }

  .hero-inner {
    width: calc(100% - 24px);
    justify-content: center;
  }

  .hero-content {
    width: 100%;
    max-width: 330px;
    text-align: center;
  }

  .hero-logo {
    width: 128px;
    margin-bottom: 10px;
  }

  .hero-label {
    font-size: 13px;
    letter-spacing: 0.7px;
  }

  .hero-flower {
    width: 78px;
    margin: 10px auto 8px;
  }

  .hero h1 {
    font-size: 24px;
    line-height: 1.12;
  }

  .hero h2 {
    margin-top: 4px;
    font-size: 18px;
    line-height: 1.15;
  }

  .hero-divider {
    width: 110px;
    margin: 10px auto 12px;
  }

  .hero-desc {
    max-width: 290px;
    margin-bottom: 14px;
    font-size: 13px;
    line-height: 1.55;
  }

  .hero-actions {
    flex-direction: row;
    justify-content: center;
    gap: 10px;
  }

  .btn {
    width: auto;
    min-width: 108px;
    height: 36px;
    padding: 0 14px;
    font-size: 11.5px;
    border-radius: 999px;
  }

  /* HIGHLIGHT */
  .highlight-section {
    padding: 28px 0 10px;
  }

  .highlight-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .highlight-card {
    width: 100%;
    max-width: none;
    display: grid;
    grid-template-columns: 72px 1fr;
    column-gap: 14px;
    align-items: center;
    padding: 15px 16px;
    border-radius: 18px;
    text-align: left;
  }

  .highlight-card strong {
    grid-row: span 2;
    margin: 0;
    font-size: 26px;
    text-align: center;
  }

  .highlight-card h3 {
    margin: 0 0 3px;
    font-size: 17px;
  }

  .highlight-card p {
    font-size: 13.5px;
    line-height: 1.45;
  }

  /* SECTION */
  .story-section,
  .why-section,
  .value-section,
  .expert-section,
  .ritual-section {
    padding: 42px 0;
  }

  .story-grid,
  .why-grid,
  .ritual-layout {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .why-content {
    order: 2;
  }

  .why-section .image-box {
    order: 1;
  }

  .image-box {
    max-width: 100%;
    margin: 0;
  }

  .image-box::after {
    right: -8px;
    bottom: -8px;
    border-radius: 18px;
  }

  .image-box img {
    width: 100%;
    height: 220px;
    border-radius: 18px;
    object-fit: cover;
  }

  .section-label {
    margin-bottom: 8px;
    font-size: 12px;
    letter-spacing: 1.5px;
  }

  .section-heading {
    margin-bottom: 26px;
  }

  .section-heading h2,
  .story-content h2,
  .why-content h2,
  .ritual-content h2 {
    margin-bottom: 12px;
    font-size: 25px;
    line-height: 1.25;
  }

  .section-heading p,
  .story-content p,
  .ritual-content p {
    margin-bottom: 12px;
    font-size: 14.5px;
    line-height: 1.65;
  }

  .quote-box {
    margin-top: 18px;
    padding: 16px 18px;
    border-radius: 16px;
    gap: 10px;
  }

  .quote-box span {
    font-size: 34px;
  }

  .quote-box p {
    font-size: 14px;
    line-height: 1.6;
  }

  .reason-list {
    margin-top: 18px;
    gap: 10px;
  }

  .reason-item {
    padding: 13px 14px 13px 42px;
    border-radius: 14px;
    font-size: 14px;
    line-height: 1.55;
  }

  .reason-item::before {
    left: 16px;
    top: 13px;
  }

  /* VALUE */
  .value-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .value-card {
    max-width: none;
    padding: 22px 20px;
    border-radius: 18px;
  }

  .value-card h3 {
    margin-bottom: 8px;
    font-size: 22px;
  }

  .value-card p {
    font-size: 14px;
    line-height: 1.6;
  }

  /* EXPERT */
  .expert-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .expert-card,
  .ritual-card {
    max-width: none;
    padding: 24px 20px;
    border-radius: 18px;
  }

  .expert-card img,
  .ritual-card img {
    width: 88px;
    height: 88px;
    margin-bottom: 12px;
  }

  .expert-card h3,
  .ritual-card h3 {
    margin-bottom: 8px;
    font-size: 18px;
  }

  .expert-card p,
  .ritual-card p {
    font-size: 14px;
    line-height: 1.55;
  }

  /* RITUAL */
  .ritual-cards {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .ritual-double {
    gap: 8px;
  }

  .ritual-double img {
    width: 82px;
    height: 82px;
  }

  /* CERTIFICATE - FIX PHẦN ĐANG BỊ KỲ */
  .certificate-section {
    position: relative;
    height: 230px;
    padding: 0;
    background: #fbf6ef;
    overflow: hidden;
  }

  .certificate-section img {
    width: 100%;
    height: 100%;
    min-height: unset;
    display: block;
    object-fit: cover;
    object-position: center;
  }

  .certificate-section::after {
    content: "";
    position: absolute;
    inset: 0;
    background: linear-gradient(
        180deg,
        rgba(11, 52, 83, 0.02) 0%,
        rgba(11, 52, 83, 0.05) 48%,
        rgba(11, 52, 83, 0.18) 100%
    );
    pointer-events: none;
  }


  .certificate-btn {
    position: absolute;
    left: 69%;
    bottom: 14px;
    z-index: 2;

    width: auto;
    min-width: 156px;
    height: 34px;
    margin: 0;
    padding: 0 14px;

    display: flex;
    align-items: center;
    justify-content: center;

    border-radius: 999px;
    border: 2px solid #a60b27;
    background: rgba(255, 255, 255, 0.92);
    color: #a60b27;

    font-size: 11px;
    font-weight: 900;
    text-transform: uppercase;
    white-space: nowrap;

    transform: translateX(-50%);
    backdrop-filter: blur(4px);
  }

  .certificate-btn:hover {
    transform: translateX(-50%) translateY(-2px);
  }
}

@media (max-width: 420px) {
  .hero,
  .hero-inner {
    min-height: 480px;
  }

  .hero-content {
    max-width: 315px;
  }

  .hero-logo {
    width: 120px;
  }

  .hero h1 {
    font-size: 22px;
  }

  .hero h2 {
    font-size: 17px;
  }

  .hero-desc {
    font-size: 12.8px;
  }

  .btn {
    min-width: 102px;
    height: 34px;
    padding: 0 12px;
    font-size: 11px;
  }

  .image-box img {
    height: 205px;
  }

  .section-heading h2,
  .story-content h2,
  .why-content h2,
  .ritual-content h2 {
    font-size: 23px;
  }

  .certificate-section {
    height: 210px;
  }

  .certificate-btn {
    min-width: 102px;
    height: 30px;
    font-size: 10px;
  }
}
</style>