<template>
  <main class="service-page">
    <section
        class="service-hero"
        :style="{ backgroundImage: `url(${heroBanner})` }"
    >
      <div class="hero-overlay">
        <h1 class="hero-title">DỊCH VỤ MAI TÁNG AN YÊN</h1>

        <div class="hero-divider">
          <img :src="dividerIcon" alt="divider" class="divider-img"/>
        </div>

        <p class="hero-desc">
          An Yên đồng hành cùng gia đình trong khoảnh khắc thiêng liêng,
          <br/>
          mang đến sự an lành, tôn nghiêm và trọn vẹn nhất.
        </p>
      </div>
    </section>

    <section class="service-main">
      <div class="container page-layout">
        <div class="left-content">
          <div class="detail-layout">
            <div class="service-images">
              <div class="image-wrapper">
                <button class="image-nav prev" @click="prevImage">‹</button>

                <img :src="mainImage" class="main-image" alt="Ảnh dịch vụ"/>

                <button class="image-nav next" @click="nextImage">›</button>
              </div>

              <div class="thumb-list">
                <img
                    v-for="(img, index) in images"
                    :key="index"
                    :src="img"
                    :class="{ active: currentIndex === index }"
                    @click="changeImage(index)"
                    alt="Ảnh dịch vụ"
                />
              </div>
            </div>

            <div class="service-info">
              <span class="badge">{{ service.tenCombo }}</span>

              <h1>{{ service.tenCombo }}</h1>
              <h3>Trang trọng – Chu đáo – Tiết kiệm</h3>

              <p>{{ service.moTa }}</p>

              <div class="divider">
                <span></span>
                <i class="fa-solid fa-spa"></i>
                <span></span>
              </div>

              <div class="price">
                Từ
                <strong>
                  {{ Number(service.gia).toLocaleString('vi-VN') }} đ
                </strong>
                <small>(Đã bao gồm VAT)</small>
              </div>

              <div class="features">
                <div>
                  <i class="fa-regular fa-clock"></i>
                  <p><b>Tư vấn 24/7</b><br/>Hỗ trợ tận tâm</p>
                </div>

                <div>
                  <i class="fa-regular fa-heart"></i>
                  <p><b>Phục vụ chu đáo</b><br/>Đội ngũ chuyên nghiệp</p>
                </div>

                <div>
                  <i class="fa-regular fa-clipboard"></i>
                  <p><b>Minh bạch chi phí</b><br/>Không phát sinh</p>
                </div>
              </div>
            </div>
          </div>

          <div class="content-card">
            <h2>CHI TIẾT DỊCH VỤ</h2>

            <p class="section-desc">
              Gói Tiêu Chuẩn bao gồm đầy đủ các hạng mục cần thiết cho một lễ tang trang trọng.
            </p>

            <div class="detail-grid">
              <div class="detail-item">
                <i class="fa-solid fa-cake-candles"></i>

                <h4>Chi tiết gói dịch vụ</h4>

                <ul>
                  <li
                      v-for="item in comboChiTiet"
                      :key="item.comboChiTietId"
                  >
                    {{ item.noiDung }}
                  </li>
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
      </div>
    </section>
  </main>
</template>

<script setup>
import {ref, computed} from 'vue'
import axios from 'axios'
import {onMounted} from 'vue'

import service1 from '../../assets/images/TrangDichVu/goi1.png'
import service2 from '../../assets/images/TrangDichVu/goi2.png'
import service3 from '../../assets/images/TrangDichVu/goi3.png'
import service4 from '../../assets/images/TrangDichVu/goi4.png'

const service = ref({
  tenCombo: '',
  gia: 0,
  moTa: ''
})

const images = [
  service1,
  service2,
  service3,
  service4
]

const currentIndex = ref(0)

const mainImage = computed(() => {
  return images[currentIndex.value]
})

const changeImage = (index) => {
  currentIndex.value = index
}

const nextImage = () => {
  currentIndex.value =
      (currentIndex.value + 1) % images.length
}

const prevImage = () => {
  currentIndex.value =
      (currentIndex.value - 1 + images.length)
      % images.length
}

import {useRoute} from 'vue-router'
import heroBanner from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";
import dividerIcon from "../../assets/images/icon/flower_icon.png";

const route = useRoute()

const id = route.params.id

console.log(id)

const loadCombo = async () => {
  try {

    const res = await axios.get(
        `http://localhost:8080/api/dich-vu/${id}`
    )

    service.value = res.data

  } catch (e) {

    console.log(
        'Không lấy được dữ liệu, dùng dữ liệu mặc định'
    )

  }
}

const comboChiTiet = ref([])

const loadComboChiTiet = async () => {

  try {

    const res = await axios.get(
        `http://localhost:8080/api/dich-vu/${id}/chitiet`
    )

    comboChiTiet.value = res.data

  } catch (e) {

    console.log(
        'Không lấy được chi tiết'
    )

  }

}

onMounted(() => {
  window.scrollTo(0, 0)

  loadCombo()
  loadComboChiTiet()
})

</script>

<style scoped src="../../assets/styles/website/TrangDichVuChiTiet.css"></style>