<template>
  <main class="service-page">

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

    <section class="service-main">
      <div class="container detail-layout">
        <div class="service-images">
          <img :src="mainImage" class="main-image" alt="Gói tiêu chuẩn" />

          <div class="thumb-list">
            <img
                v-for="(img, index) in images"
                :key="index"
                :src="img"
                :class="{ active: img === mainImage }"
                @click="mainImage = img"
                alt="Ảnh dịch vụ"
            />
          </div>
        </div>

        <div class="service-info">
          <span class="badge">GÓI TIÊU CHUẨN</span>

          <h1>Gói Tiêu Chuẩn</h1>
          <h3>Trang trọng – Chu đáo – Tiết kiệm</h3>

          <p>
            Gói Tiêu Chuẩn được thiết kế phù hợp với các gia đình mong muốn
            một lễ tang trang trọng, đầy đủ nghi thức với chi phí hợp lý.
          </p>

          <div class="divider">
            <span></span>
            <i class="fa-solid fa-spa"></i>
            <span></span>
          </div>

          <div class="price">
            Từ <strong>25.000.000 đ</strong>
            <small>(Đã bao gồm VAT)</small>
          </div>

          <div class="features">
            <div>
              <i class="fa-regular fa-clock"></i>
              <p><b>Tư vấn 24/7</b><br />Hỗ trợ tận tâm</p>
            </div>

            <div>
              <i class="fa-regular fa-heart"></i>
              <p><b>Phục vụ chu đáo</b><br />Đội ngũ chuyên nghiệp</p>
            </div>

            <div>
              <i class="fa-regular fa-clipboard"></i>
              <p><b>Minh bạch chi phí</b><br />Không phát sinh</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="service-content">
      <div class="container">
        <div class="content-card">
          <h2>CHI TIẾT DỊCH VỤ</h2>
          <p class="section-desc">
            Gói Tiêu Chuẩn bao gồm đầy đủ các hạng mục cần thiết cho một lễ tang trang trọng.
          </p>

          <div class="detail-grid">
            <div class="detail-item">
              <i class="fa-solid fa-cake-candles"></i>
              <h4>1. Tang lễ & nghi thức</h4>
              <ul>
                <li>Nhân viên phục vụ chuyên nghiệp</li>
                <li>Tư vấn và hỗ trợ 24/7</li>
                <li>Nhạc tang lễ cơ bản</li>
                <li>Dẫn chương trình lễ tang</li>
                <li>Thực hiện nghi thức theo tôn giáo</li>
              </ul>
            </div>

            <div class="detail-item">
              <i class="fa-solid fa-fan"></i>
              <h4>2. Trang trí tang lễ</h4>
              <ul>
                <li>Phông rạp, bàn thờ, bàn ghi sổ</li>
                <li>Vòng hoa viếng</li>
                <li>Hoa trang trí bàn thờ</li>
                <li>Đèn, nến, hương, lư đồng</li>
                <li>Bảng rôn, câu đối</li>
              </ul>
            </div>

            <div class="detail-item">
              <i class="fa-solid fa-cross"></i>
              <h4>3. Hậu sự</h4>
              <ul>
                <li>Vệ sinh, khâm liệm</li>
                <li>Áo quan tiêu chuẩn</li>
                <li>Xe đưa đón linh cữu</li>
                <li>Xe đưa tang</li>
                <li>Đài kèn tây cơ bản</li>
              </ul>
            </div>

            <div class="detail-item">
              <i class="fa-solid fa-gift"></i>
              <h4>4. Tiện ích khác</h4>
              <ul>
                <li>Nước uống, khăn lạnh</li>
                <li>Sổ tang, bút</li>
                <li>Hỗ trợ thủ tục giấy tờ</li>
                <li>Tư vấn phong tục địa phương</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="process-card">
          <h2>QUY TRÌNH THỰC HIỆN</h2>
          <p class="section-desc">
            An Yên đồng hành cùng gia đình trong từng bước, đảm bảo mọi việc được diễn ra chu đáo và trang trọng.
          </p>

          <div class="process-list">
            <div class="process-item">
              <span>1</span>
              <h4>Tiếp nhận thông tin</h4>
              <p>Lắng nghe, tư vấn và khảo sát nhu cầu</p>
            </div>

            <div class="process-item">
              <span>2</span>
              <h4>Báo giá & ký kết</h4>
              <p>Đề xuất gói phù hợp, minh bạch chi phí</p>
            </div>

            <div class="process-item">
              <span>3</span>
              <h4>Chuẩn bị & triển khai</h4>
              <p>Chuẩn bị đầy đủ theo kế hoạch</p>
            </div>

            <div class="process-item">
              <span>4</span>
              <h4>Tổ chức lễ tang</h4>
              <p>Thực hiện nghi thức trang trọng</p>
            </div>

            <div class="process-item">
              <span>5</span>
              <h4>Hậu sự & hỗ trợ</h4>
              <p>Hoàn tất thủ tục, hỗ trợ gia đình</p>
            </div>
          </div>
        </div>

        <div class="note-card">
          <h3>LƯU Ý</h3>
          <ul>
            <li>Giá gói có thể thay đổi tùy theo khu vực và yêu cầu riêng của gia đình.</li>
            <li>Các hạng mục chưa bao gồm: hỏa táng, mộ phần, bia mộ nếu có.</li>
          </ul>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'

import service1 from '../assets/images/TrangDichvu/goi1.png'
import service2 from '../assets/images/TrangDichvu/goi1.png'
import service3 from '../assets/images/TrangDichvu/goi1.png'
import service4 from '../assets/images/TrangDichvu/goi1.png'

const images = [service1, service2, service3, service4]
const mainImage = ref(service1)

import { useRoute } from 'vue-router'
import heroBanner from "../assets/images/TrangSanPham/heroSection_TrangSanPham.png";
import dividerIcon from "../assets/images/icon/flower_icon.png";

const route = useRoute()

const id = route.params.id

console.log(id)
</script>

<style scoped src="../assets/styles/TrangDichVuChiTiet.css"></style>