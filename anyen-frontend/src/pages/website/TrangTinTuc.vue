<template>
  <div class="tin-tuc-page">
    <!-- Banner -->
    <section
        class="banner"
        :style="{
        backgroundImage: `
          linear-gradient(
            rgba(0, 0, 0, 0.35),
            rgba(0, 0, 0, 0.35)
          ),
          url('${heroSectionTrangSanPham}')
        `
      }"
    >
      <div class="banner-content">
        <h1>Tin tức An Yên</h1>

        <p>
          Cập nhật những thông tin hữu ích về tang lễ,
          phong tục và hoạt động của An Yên.
        </p>
      </div>
    </section>

    <!-- Danh sách tin tức -->
    <section class="container">
      <div class="title">
        <h2>Tin tức mới nhất</h2>
      </div>

      <!-- Hiển thị khi đang tải -->
      <div
          v-if="loading"
          class="status-message"
      >
        Đang tải danh sách tin tức...
      </div>

      <!-- Hiển thị khi xảy ra lỗi -->
      <div
          v-else-if="errorMessage"
          class="status-message error-message"
      >
        {{ errorMessage }}
      </div>

      <!-- Danh sách tin tức -->
      <el-row
          v-else-if="tinTucList.length > 0"
          :gutter="30"
      >
        <el-col
            v-for="item in tinTucList"
            :key="item.maTinTuc"
            :xs="24"
            :sm="12"
            :md="8"
        >
          <article class="card">
            <img
                :src="getImageUrl(item.anhDaiDien)"
                :alt="item.tieuDe"
                class="image"
                loading="lazy"
                decoding="async"
                @error="handleImageError"
            />

            <div class="content">
              <h3>
                {{ item.tieuDe }}
              </h3>

              <p class="date">
                {{ formatDate(item.ngayDang) }}
              </p>

              <p class="summary">
                {{ item.tomTat }}
              </p>

              <button
                  type="button"
                  @click="goDetail(item.maTinTuc)"
              >
                Đọc tiếp
              </button>
            </div>
          </article>
        </el-col>
      </el-row>

      <!-- Không có dữ liệu -->
      <div
          v-else
          class="status-message"
      >
        Hiện chưa có bài viết nào.
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

import heroSectionTrangSanPham
  from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";

const router = useRouter();

/*
 * Địa chỉ backend.
 *
 * Khi deploy, bạn có thể tạo file .env:
 * VITE_API_BASE_URL=http://localhost:8080
 */
const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080";

const tinTucList = ref([]);
const loading = ref(false);
const errorMessage = ref("");

/*
 * Ảnh mặc định khi bài viết không có ảnh
 * hoặc ảnh trên backend không tồn tại.
 */
const defaultImage = heroSectionTrangSanPham;

/**
 * Lấy danh sách tin tức từ backend.
 */
const getTinTuc = async () => {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await axios.get(
        `${API_BASE_URL}/api/tin-tuc`
    );

    tinTucList.value = Array.isArray(response.data)
        ? response.data
        : [];
  } catch (error) {
    console.error(
        "Lỗi lấy danh sách tin tức:",
        error
    );

    errorMessage.value =
        "Không thể tải danh sách tin tức. Vui lòng thử lại sau.";
  } finally {
    loading.value = false;
  }
};

/**
 * Chuyển dữ liệu AnhDaiDien thành đường dẫn hoàn chỉnh.
 *
 * Các trường hợp được hỗ trợ:
 *
 * tintuc1.jpg
 * images/tintuc/tintuc1.jpg
 * /images/tintuc/tintuc1.jpg
 * http://localhost:8080/images/tintuc/tintuc1.jpg
 */
const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return defaultImage;
  }

  const normalizedPath = String(imagePath)
      .trim()
      .replaceAll("\\", "/");

  /*
   * Nếu database đã lưu URL đầy đủ
   * thì sử dụng trực tiếp.
   */
  if (
      normalizedPath.startsWith("http://") ||
      normalizedPath.startsWith("https://") ||
      normalizedPath.startsWith("data:") ||
      normalizedPath.startsWith("blob:")
  ) {
    return normalizedPath;
  }

  /*
   * Ví dụ:
   * /images/tintuc/tintuc1.jpg
   */
  if (normalizedPath.startsWith("/")) {
    return `${API_BASE_URL}${normalizedPath}`;
  }

  /*
   * Ví dụ:
   * images/tintuc/tintuc1.jpg
   */
  if (normalizedPath.startsWith("images/")) {
    return `${API_BASE_URL}/${normalizedPath}`;
  }

  /*
   * Nếu database chỉ lưu:
   * tintuc1.jpg
   *
   * Thì tự động chuyển thành:
   * http://localhost:8080/images/tintuc/tintuc1.jpg
   */
  return `${API_BASE_URL}/images/tintuc/${normalizedPath}`;
};

/**
 * Dùng ảnh mặc định nếu ảnh bài viết bị lỗi.
 */
const handleImageError = (event) => {
  const imageElement = event.currentTarget;

  if (
      imageElement.dataset.fallbackApplied === "true"
  ) {
    return;
  }

  imageElement.dataset.fallbackApplied = "true";
  imageElement.src = defaultImage;
};

/**
 * Chuyển đến trang chi tiết tin tức.
 */
const goDetail = (id) => {
  router.push(`/tin-tuc/${id}`);
};

/**
 * Định dạng ngày tháng theo tiếng Việt.
 */
const formatDate = (date) => {
  if (!date) {
    return "";
  }

  const parsedDate = new Date(date);

  if (Number.isNaN(parsedDate.getTime())) {
    return "";
  }

  return parsedDate.toLocaleDateString(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
      }
  );
};

onMounted(() => {
  getTinTuc();
});
</script>

<style scoped>
.tin-tuc-page {
  min-height: 100vh;
  background: #f8f5f2;
}

/* Banner */

.banner {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;

  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.banner-content {
  padding: 20px;
  text-align: center;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.45);
}

.banner h1 {
  margin: 0 0 15px;
  font-size: 42px;
}

.banner p {
  max-width: 650px;
  margin: 0 auto;
  font-size: 18px;
  line-height: 1.6;
}

/* Content */

.container {
  width: min(1200px, 90%);
  margin: 50px auto;
}

.title {
  margin-bottom: 40px;
  text-align: center;
}

.title h2 {
  margin: 0;
  color: #8b5e3c;
  font-size: 32px;
}

/* Trạng thái */

.status-message {
  padding: 40px 20px;
  text-align: center;
  color: #666666;
  font-size: 17px;
}

.error-message {
  color: #a40019;
}

/* Card */

.card {
  height: calc(100% - 30px);
  margin-bottom: 30px;
  overflow: hidden;

  display: flex;
  flex-direction: column;

  background: #ffffff;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);

  transition:
      transform 0.3s ease,
      box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.13);
}

.image {
  display: block;
  width: 100%;
  height: 220px;

  object-fit: cover;
  object-position: center;

  background: #eeeeee;
}

.content {
  flex: 1;
  padding: 20px;

  display: flex;
  flex-direction: column;
}

.content h3 {
  min-height: 55px;
  margin: 0 0 10px;

  color: #8b5e3c;
  font-size: 20px;
  line-height: 1.4;
}

.date {
  margin: 0 0 12px;
  color: #999999;
  font-size: 14px;
}

.summary {
  margin: 0;
  overflow: hidden;

  color: #555555;
  line-height: 1.6;

  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

button {
  align-self: flex-start;
  margin-top: auto;
  padding: 10px 20px;

  color: #ffffff;
  background: #8b5e3c;
  border: none;
  border-radius: 20px;

  cursor: pointer;
  transition: background-color 0.25s ease;
}

button:hover {
  background: #6f452c;
}

/* Responsive */

@media (max-width: 768px) {
  .banner {
    height: 260px;
  }

  .banner h1 {
    font-size: 34px;
  }

  .banner p {
    font-size: 16px;
  }

  .container {
    margin: 35px auto;
  }

  .title h2 {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .banner {
    height: 230px;
  }

  .banner h1 {
    font-size: 29px;
  }

  .image {
    height: 200px;
  }
}
</style>