<template>
  <section
      class="service-hero"
      :style="{ backgroundImage: `url(${heroBanner})` }"
  >
    <div class="hero-overlay">
      <h1 class="hero-title">LIÊN HỆ VỚI AN YÊN</h1>

      <div class="hero-divider">
        <img
            :src="dividerIcon"
            alt="divider"
            class="divider-img"
        />
      </div>

      <p class="hero-desc">
        An Yên luôn sẵn sàng lắng nghe và hỗ trợ quý khách.
        <br />
        Hãy để chúng tôi đồng hành cùng gia đình bạn.
      </p>
    </div>
  </section>

  <div class="container contact-section py-5">
    <div class="row g-4">

      <div class="col-lg-4">
        <div class="contact-info">

          <h2 class="section-title">
            THÔNG TIN LIÊN HỆ
          </h2>

          <p class="section-desc">
            An Yên luôn sẵn sàng hỗ trợ bạn 24/7.
            <br />
            Vui lòng liên hệ qua các kênh dưới đây.
          </p>

          <div class="info-card">
            <div class="icon red">
              <i class="fas fa-phone"></i>
            </div>

            <div>
              <h6>Liên hệ 24/7</h6>
              <h5>0123 456 789</h5>
              <small>Luôn sẵn sàng phục vụ mọi lúc mọi nơi</small>
            </div>
          </div>

          <div class="info-card">
            <div class="icon blue">
              <i class="fas fa-envelope"></i>
            </div>

            <div>
              <h6>Email</h6>
              <h5>anyen@gmail.com</h5>
              <small>Phản hồi trong vòng 24 giờ</small>
            </div>
          </div>

          <div class="info-card">
            <div class="icon blue">
              <i class="fas fa-location-dot"></i>
            </div>

            <div>
              <h6>Địa chỉ</h6>
              <h5>
                123 Đường An Yên, TP.HCM
              </h5>
              <small>Đón tiếp quý khách 24/7</small>
            </div>
          </div>

          <div class="info-card">
            <div class="icon blue">
              <i class="fas fa-clock"></i>
            </div>

            <div>
              <h6>THỜI GIAN LÀM VIỆC</h6>
              <h5>24/7</h5>
              <small>Tất cả các ngày trong tuần</small>
            </div>
          </div>

        </div>
      </div>

      <div class="col-lg-8">
        <div class="contact-form">

          <h2 class="section-title">
            GỬI LIÊN HỆ
          </h2>

          <p class="section-desc">
            Vui lòng điền thông tin vào form bên dưới
            <br />
            Chúng tôi sẽ phản hồi bạn sớm nhất có thể
          </p>

          <form>

            <div class="row">
              <div class="col-md-6 mb-3">
                <input
                    type="text"
                    class="form-control"
                    placeholder="Họ và tên"
                />
              </div>

              <div class="col-md-6 mb-3">
                <input
                    type="email"
                    class="form-control"
                    placeholder="Email"
                />
              </div>
            </div>

            <div class="mb-3">
              <input
                  type="text"
                  class="form-control"
                  placeholder="Số điện thoại"
              />
            </div>

            <div class="mb-3">
              <input
                  type="text"
                  class="form-control"
                  placeholder="Tiêu đề"
              />
            </div>

            <div class="mb-3">
              <textarea
                  rows="6"
                  class="form-control"
                  placeholder="Nội dung liên hệ"
              ></textarea>
            </div>

            <button
                type="submit"
                class="btn-send"
            >
              GỬI NỘI DUNG
            </button>

            <p class="secure-text">
              Thông tin của bạn được bảo mật tuyệt đối
            </p>

          </form>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import dividerIcon from "../../assets/images/icon/flower_icon.png";
import heroBanner from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";
const text = ref('')
</script>
<style scoped src="../../assets/styles/TrangLienHe.css"></style>