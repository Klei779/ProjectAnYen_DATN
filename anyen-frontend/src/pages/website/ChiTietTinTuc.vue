<template>
  <div class="chi-tiet-tin-tuc-page">
    <!-- Trạng thái tải dữ liệu -->
    <div
        v-if="loading"
        class="status-container"
    >
      <div class="loading-spinner"></div>
      <p>Đang tải nội dung bài viết...</p>
    </div>

    <!-- Trạng thái lỗi -->
    <div
        v-else-if="errorMessage"
        class="status-container error-container"
    >
      <h2>Không thể tải bài viết</h2>

      <p>
        {{ errorMessage }}
      </p>

      <button
          type="button"
          class="back-button"
          @click="goBack"
      >
        Quay lại trang tin tức
      </button>
    </div>

    <!-- Nội dung bài viết -->
    <article
        v-else-if="tinTuc"
        class="article-wrapper"
    >
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        <router-link to="/">
          Trang chủ
        </router-link>

        <span>/</span>

        <router-link to="/tin-tuc">
          Tin tức
        </router-link>

        <span>/</span>

        <span class="current">
          {{ tinTuc.tieuDe }}
        </span>
      </nav>

      <!-- Phần đầu bài viết -->
      <header class="article-header">
        <div class="category">
          {{ getLoaiTinText(tinTuc.loaiTin) }}
        </div>

        <h1>
          {{ tinTuc.tieuDe }}
        </h1>

        <p
            v-if="tinTuc.tomTat"
            class="summary"
        >
          {{ tinTuc.tomTat }}
        </p>

        <div class="article-meta">
          <span class="meta-item">
            <span class="meta-icon">📅</span>
            {{ formatDate(tinTuc.ngayDang) }}
          </span>

          <span class="meta-item">
            <span class="meta-icon">📰</span>
            An Yên
          </span>
        </div>
      </header>

      <!-- Ảnh đại diện -->
      <div class="thumbnail-wrapper">
        <img
            :src="getImageUrl(tinTuc.anhDaiDien)"
            :alt="tinTuc.tieuDe"
            class="thumbnail"
            @error="handleImageError"
        />
      </div>

      <!-- Nội dung HTML lấy từ database -->
      <section
          class="article-content"
          v-html="tinTuc.noiDung"
      ></section>

      <!-- Cuối bài viết -->
      <footer class="article-footer">
        <div class="divider"></div>

        <button
            type="button"
            class="back-button"
            @click="goBack"
        >
          ← Quay lại trang tin tức
        </button>
      </footer>
    </article>

    <!-- Không có dữ liệu -->
    <div
        v-else
        class="status-container"
    >
      <h2>Không tìm thấy bài viết</h2>

      <button
          type="button"
          class="back-button"
          @click="goBack"
      >
        Quay lại trang tin tức
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

import defaultImage from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";

const route = useRoute();
const router = useRouter();

/*
 * Có thể khai báo trong file .env:
 *
 * VITE_API_BASE_URL=http://localhost:8080
 */
const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080";

const tinTuc = ref(null);
const loading = ref(false);
const errorMessage = ref("");

/**
 * Lấy ID bài viết từ URL.
 *
 * Hỗ trợ cả hai dạng route:
 * /tin-tuc/:id
 * /tin-tuc/:maTinTuc
 */
const getTinTucId = () => {
  return route.params.id || route.params.maTinTuc;
};

/**
 * Gọi API lấy chi tiết tin tức.
 */
const getChiTietTinTuc = async () => {
  const id = getTinTucId();

  if (!id) {
    errorMessage.value = "Không xác định được mã bài viết.";
    tinTuc.value = null;
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await axios.get(
        `${API_BASE_URL}/api/tin-tuc/${id}`
    );

    /*
     * Hỗ trợ cả hai kiểu API:
     *
     * response.data = dữ liệu bài viết
     *
     * hoặc:
     *
     * response.data.data = dữ liệu bài viết
     */
    const responseData =
        response.data?.data ?? response.data;

    if (!responseData) {
      throw new Error("Không tìm thấy dữ liệu bài viết.");
    }

    tinTuc.value = responseData;
  } catch (error) {
    console.error(
        "Lỗi lấy chi tiết tin tức:",
        error
    );

    tinTuc.value = null;

    if (error.response?.status === 404) {
      errorMessage.value =
          "Bài viết không tồn tại hoặc đã bị xóa.";
    } else {
      errorMessage.value =
          "Không thể tải nội dung bài viết. Vui lòng thử lại sau.";
    }
  } finally {
    loading.value = false;
  }
};

/**
 * Chuyển đường dẫn ảnh trong database
 * thành URL đầy đủ.
 *
 * Hỗ trợ:
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
   * URL đầy đủ hoặc ảnh dạng dữ liệu.
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
   * Database lưu:
   * /images/tintuc/tintuc1.jpg
   */
  if (normalizedPath.startsWith("/")) {
    return `${API_BASE_URL}${normalizedPath}`;
  }

  /*
   * Database lưu:
   * images/tintuc/tintuc1.jpg
   */
  if (normalizedPath.startsWith("images/")) {
    return `${API_BASE_URL}/${normalizedPath}`;
  }

  /*
   * Database chỉ lưu:
   * tintuc1.jpg
   */
  return `${API_BASE_URL}/images/tintuc/${normalizedPath}`;
};

/**
 * Thay bằng ảnh mặc định khi ảnh bị lỗi.
 */
const handleImageError = (event) => {
  const imageElement = event.currentTarget;

  /*
   * Ngăn sự kiện lỗi lặp vô hạn
   * trong trường hợp ảnh mặc định cũng lỗi.
   */
  if (
      imageElement.dataset.fallbackApplied === "true"
  ) {
    return;
  }

  imageElement.dataset.fallbackApplied = "true";
  imageElement.src = defaultImage;
};

/**
 * Hiển thị tên loại tin.
 */
const getLoaiTinText = (loaiTin) => {
  const loaiTinMap = {
    1: "Kiến thức",
    2: "Phong tục",
    3: "Thông báo",
    4: "Hoạt động"
  };

  return loaiTinMap[Number(loaiTin)] || "Tin tức";
};

/**
 * Định dạng ngày đăng.
 */
const formatDate = (dateValue) => {
  if (!dateValue) {
    return "Chưa cập nhật";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "Chưa cập nhật";
  }

  return date.toLocaleDateString(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
      }
  );
};

/**
 * Quay lại danh sách tin tức.
 */
const goBack = () => {
  router.push("/tin-tuc");
};

/*
 * Lấy dữ liệu khi mở trang.
 */
onMounted(() => {
  getChiTietTinTuc();
});

/*
 * Gọi lại API khi chuyển sang bài viết khác
 * nhưng vẫn dùng cùng component.
 */
watch(
    () => route.params.id || route.params.maTinTuc,
    () => {
      getChiTietTinTuc();
      window.scrollTo({
        top: 0,
        behavior: "smooth"
      });
    }
);
</script>

<style scoped>
.chi-tiet-tin-tuc-page {
  min-height: 100vh;
  padding: 40px 20px 70px;
  background: #f8f5f2;
}

.article-wrapper {
  width: min(1000px, 100%);
  margin: 0 auto;
  padding: 35px 45px 50px;
  box-sizing: border-box;

  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.08);
}

/* Breadcrumb */

.breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;

  margin-bottom: 30px;
  color: #888888;
  font-size: 14px;
}

.breadcrumb a {
  color: #8b0016;
  text-decoration: none;
}

.breadcrumb a:hover {
  text-decoration: underline;
}

.breadcrumb .current {
  max-width: 500px;
  overflow: hidden;
  color: #666666;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Header */

.article-header {
  margin-bottom: 30px;
  text-align: center;
}

.category {
  display: inline-flex;
  align-items: center;

  margin-bottom: 16px;
  padding: 7px 18px;

  color: #ffffff;
  background: #8b0016;
  border-radius: 20px;

  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
}

.article-header h1 {
  max-width: 850px;
  margin: 0 auto 18px;

  color: #7d0014;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 42px;
  line-height: 1.3;
}

.summary {
  max-width: 760px;
  margin: 0 auto 20px;

  color: #555555;
  font-size: 18px;
  font-style: italic;
  line-height: 1.7;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;

  color: #777777;
  font-size: 14px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  font-size: 15px;
}

/* Ảnh đại diện */

.thumbnail-wrapper {
  width: 100%;
  margin-bottom: 35px;
  overflow: hidden;

  background: #eeeeee;
  border-radius: 14px;
}

.thumbnail {
  display: block;
  width: 100%;
  max-height: 580px;

  object-fit: cover;
  object-position: center;
}

/* Nội dung bài viết */

.article-content {
  color: #333333;
  font-size: 17px;
  line-height: 1.85;
}

/*
 * Vì nội dung dùng v-html nên cần :deep()
 * để CSS scoped áp dụng được vào các thẻ HTML.
 */

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  color: #7d0014;
  font-family: Georgia, "Times New Roman", serif;
  line-height: 1.4;
}

.article-content :deep(h2) {
  margin: 35px 0 18px;
  font-size: 30px;
}

.article-content :deep(h3) {
  margin: 30px 0 15px;
  font-size: 23px;
}

.article-content :deep(h4) {
  margin: 25px 0 12px;
  font-size: 20px;
}

.article-content :deep(p) {
  margin: 0 0 18px;
  text-align: justify;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 15px 0 22px;
  padding-left: 30px;
}

.article-content :deep(li) {
  margin-bottom: 10px;
}

.article-content :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;

  margin: 25px auto;
  border-radius: 10px;
}

.article-content :deep(a) {
  color: #8b0016;
  text-decoration: underline;
}

.article-content :deep(blockquote) {
  margin: 25px 0;
  padding: 18px 22px;

  color: #555555;
  background: #faf3f4;
  border-left: 4px solid #8b0016;
  border-radius: 0 8px 8px 0;

  font-style: italic;
}

.article-content :deep(table) {
  width: 100%;
  margin: 25px 0;
  border-collapse: collapse;
}

.article-content :deep(th),
.article-content :deep(td) {
  padding: 12px;
  border: 1px solid #dddddd;
  text-align: left;
}

.article-content :deep(th) {
  color: #ffffff;
  background: #8b0016;
}

/* Footer */

.article-footer {
  margin-top: 45px;
}

.divider {
  width: 100%;
  height: 1px;
  margin-bottom: 25px;
  background: #e5e5e5;
}

.back-button {
  padding: 12px 24px;

  color: #ffffff;
  background: #8b0016;
  border: none;
  border-radius: 25px;

  font-size: 15px;
  font-weight: 600;
  cursor: pointer;

  transition:
      background-color 0.25s ease,
      transform 0.25s ease;
}

.back-button:hover {
  background: #650010;
  transform: translateY(-2px);
}

/* Loading và lỗi */

.status-container {
  width: min(700px, 100%);
  margin: 80px auto;
  padding: 50px 25px;
  box-sizing: border-box;

  text-align: center;
  background: #ffffff;
  border-radius: 15px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.08);
}

.status-container h2 {
  margin: 0 0 16px;
  color: #8b0016;
}

.status-container p {
  margin: 0 0 25px;
  color: #666666;
  line-height: 1.6;
}

.loading-spinner {
  width: 45px;
  height: 45px;
  margin: 0 auto 20px;

  border: 4px solid #eeeeee;
  border-top-color: #8b0016;
  border-radius: 50%;

  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */

@media (max-width: 768px) {
  .chi-tiet-tin-tuc-page {
    padding: 25px 14px 50px;
  }

  .article-wrapper {
    padding: 25px 22px 40px;
  }

  .article-header h1 {
    font-size: 32px;
  }

  .summary {
    font-size: 16px;
  }

  .thumbnail {
    max-height: 420px;
  }

  .article-content {
    font-size: 16px;
    line-height: 1.75;
  }

  .article-content :deep(h2) {
    font-size: 26px;
  }

  .article-content :deep(h3) {
    font-size: 21px;
  }
}

@media (max-width: 480px) {
  .article-wrapper {
    padding: 22px 16px 35px;
    border-radius: 12px;
  }

  .breadcrumb .current {
    max-width: 180px;
  }

  .article-header h1 {
    font-size: 27px;
  }

  .article-meta {
    flex-direction: column;
    gap: 8px;
  }

  .thumbnail {
    max-height: 300px;
  }

  .article-content :deep(p) {
    text-align: left;
  }

  .back-button {
    width: 100%;
  }
}
</style>