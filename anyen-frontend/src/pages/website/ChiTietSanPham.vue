<template>
  <!-- LOADING -->
  <div v-if="loading" class="page-state">
    <div class="state-spinner">
      <i class="fa-solid fa-spinner fa-spin"></i>
    </div>
    <p>Đang tải thông tin sản phẩm...</p>
  </div>

  <!-- NOT FOUND -->
  <div v-else-if="!product" class="page-state">
    <div class="state-icon">
      <i class="fa-solid fa-box-open"></i>
    </div>

    <h2>Không tìm thấy sản phẩm</h2>
    <p>Sản phẩm không tồn tại hoặc đã ngừng kinh doanh.</p>

    <button class="btn-primary" @click="router.push('/san-pham')">
      <i class="fa-solid fa-arrow-left"></i>
      Quay lại danh sách
    </button>
  </div>

  <!-- PRODUCT DETAIL -->
  <div v-else class="product-detail-page">
    <!-- BREADCRUMB -->
    <section class="breadcrumb-section">
      <div class="page-container">
        <nav class="breadcrumb-nav">
          <router-link to="/">
            <i class="fa-solid fa-house"></i>
            Trang chủ
          </router-link>

          <i class="fa-solid fa-chevron-right breadcrumb-divider"></i>

          <router-link to="/san-pham">
            Sản phẩm
          </router-link>

          <i class="fa-solid fa-chevron-right breadcrumb-divider"></i>

          <span>{{ product.name }}</span>
        </nav>
      </div>
    </section>

    <!-- TOP PRODUCT INFORMATION -->
    <section class="product-overview-section">
      <div class="page-container">
        <div class="product-overview-layout">
          <!-- GALLERY -->
          <div class="product-gallery">
            <div class="main-image-wrapper">
              <span
                  v-if="discountPercent > 0"
                  class="image-discount-badge"
              >
                -{{ discountPercent }}%
              </span>

              <button
                  class="main-wishlist-btn"
                  :class="{ active: isWished }"
                  type="button"
                  aria-label="Thêm vào giỏ hàng"
                  @click="toggleWish"
              >
                <i class="fa-solid fa-cart-shopping"></i>
              </button>

              <img
                  :src="selectedImage"
                  :alt="product.name"
                  @error="handleImageError"
              />
            </div>

            <div
                v-if="galleryImages.length > 1"
                class="thumbnail-list"
            >
              <button
                  v-for="(image, index) in galleryImages"
                  :key="`${image}-${index}`"
                  type="button"
                  class="thumbnail-item"
                  :class="{ active: selectedImage === image }"
                  @click="selectedImage = image"
              >
                <img
                    :src="image"
                    :alt="`${product.name} - ảnh ${index + 1}`"
                    @error="handleImageError"
                />
              </button>
            </div>

            <div class="gallery-note">
              <i class="fa-solid fa-expand"></i>
              <span>Hình ảnh sản phẩm mang tính minh họa thực tế</span>
            </div>
          </div>

          <!-- PRODUCT MAIN INFO -->
          <div class="product-main-info">
            <div class="product-category-label">
              {{ product.loai || "SẢN PHẨM AN YÊN" }}
            </div>

            <h1 class="product-title">
              {{ product.name }}
            </h1>

            <p class="product-subtitle">
              {{
                product.subname ||
                product.moTaNgan ||
                "Trang trọng – Chỉn chu – Tinh tế"
              }}
            </p>

            <div class="product-code-row">
              <span>
                Mã sản phẩm:
                <strong>{{ product.code || `SP${product.id}` }}</strong>
              </span>

              <span class="status-dot-row">
                <span
                    class="status-dot"
                    :class="{ available: isAvailable }"
                ></span>

                {{
                  isAvailable
                      ? `Còn ${product.soLuong || 0} sản phẩm`
                      : "Tạm hết hàng"
                }}
              </span>
            </div>

            <div class="lotus-divider">
              <span></span>
              <i class="fa-solid fa-spa"></i>
              <span></span>
            </div>

            <div class="price-area">
              <div class="price-label">Giá sản phẩm</div>

              <div class="price-row">
                <strong class="current-price">
                  {{ formatPrice(product.price) }}
                </strong>

                <del v-if="product.oldPrice" class="old-price">
                  {{ formatPrice(product.oldPrice) }}
                </del>
              </div>

              <div
                  v-if="discountAmount > 0"
                  class="saving-text"
              >
                Tiết kiệm {{ formatPrice(discountAmount) }}
              </div>
            </div>

            <div class="quick-features">
              <div class="quick-feature">
                <div class="quick-feature-icon">
                  <i class="fa-solid fa-headset"></i>
                </div>

                <div>
                  <strong>Tư vấn 24/7</strong>
                  <span>Hỗ trợ tận tâm</span>
                </div>
              </div>

              <div class="quick-feature">
                <div class="quick-feature-icon">
                  <i class="fa-regular fa-heart"></i>
                </div>

                <div>
                  <strong>Phục vụ chu đáo</strong>
                  <span>Đồng hành cùng gia đình</span>
                </div>
              </div>

              <div class="quick-feature">
                <div class="quick-feature-icon">
                  <i class="fa-solid fa-receipt"></i>
                </div>

                <div>
                  <strong>Chi phí minh bạch</strong>
                  <span>Không phát sinh</span>
                </div>
              </div>
            </div>

            <div class="product-spec-summary">
              <div class="summary-item">
                <span>Chất liệu</span>
                <strong>{{ product.vatLieu || "Đang cập nhật" }}</strong>
              </div>

              <div class="summary-item">
                <span>Màu sắc</span>
                <strong>{{ product.mauSac || "Theo mẫu" }}</strong>
              </div>

              <div class="summary-item">
                <span>Xuất xứ</span>
                <strong>{{ product.xuatXu || "Việt Nam" }}</strong>
              </div>

              <div class="summary-item">
                <span>Tôn giáo</span>
                <strong>
                  {{ product.tonGiao || "Không phân biệt" }}
                </strong>
              </div>
            </div>

            <div class="consult-inline-form">
              <label for="phone">
                Để lại số điện thoại để được tư vấn
              </label>

              <div class="phone-field">
                <i class="fa-solid fa-phone"></i>

                <input
                    id="phone"
                    v-model.trim="phoneNumber"
                    type="tel"
                    maxlength="11"
                    placeholder="Nhập số điện thoại"
                    @keyup.enter="requestConsultation"
                />

                <button
                    type="button"
                    @click="requestConsultation"
                >
                  Gửi yêu cầu
                </button>
              </div>
            </div>
          </div>

          <!-- SUMMARY CARD -->
          <aside class="product-summary-column">
            <div class="summary-card">
              <div class="summary-card-title">
                TÓM TẮT SẢN PHẨM
              </div>

              <div class="summary-card-icon">
                <i class="fa-solid fa-box-open"></i>
              </div>

              <h2>{{ product.name }}</h2>

              <p>
                {{
                  product.subname ||
                  product.loai ||
                  "Sản phẩm được tuyển chọn kỹ lưỡng"
                }}
              </p>

              <div class="summary-card-price">
                <span>Giá từ</span>
                <strong>{{ formatPrice(product.price) }}</strong>
                <small>Giá đã bao gồm VAT nếu có</small>
              </div>

              <button
                  class="summary-primary-btn"
                  type="button"
                  @click="requestConsultation"
              >
                <i class="fa-regular fa-calendar-check"></i>
                ĐĂNG KÝ TƯ VẤN
              </button>

              <a class="summary-outline-btn" href="tel:19001234">
                <i class="fa-solid fa-phone"></i>
                LIÊN HỆ NGAY
              </a>

              <ul class="summary-benefits">
                <li>
                  <i class="fa-regular fa-clock"></i>
                  Tư vấn miễn phí 24/7
                </li>

                <li>
                  <i class="fa-solid fa-clipboard-check"></i>
                  Khảo sát và báo giá nhanh chóng
                </li>

                <li>
                  <i class="fa-solid fa-circle-dollar-to-slot"></i>
                  Chi phí minh bạch, không phát sinh
                </li>

                <li>
                  <i class="fa-regular fa-user"></i>
                  Hỗ trợ tận tâm trong suốt quá trình
                </li>
              </ul>
            </div>

            <div class="support-card">
              <span>CẦN HỖ TRỢ?</span>

              <p>
                Đội ngũ An Yên luôn sẵn sàng đồng hành và hỗ trợ gia đình.
              </p>

              <a href="tel:19001234">
                <i class="fa-solid fa-phone"></i>

                <span>
                  <strong>1900 1234</strong>
                  <small>Hotline hoạt động 24/7</small>
                </span>
              </a>
            </div>
          </aside>
        </div>
      </div>
    </section>

    <!-- DETAILS -->
    <section class="product-content-section">
      <div class="page-container content-layout">
        <main class="detail-main-column">
          <div class="content-heading">
            <span></span>
            <h2>CHI TIẾT SẢN PHẨM</h2>
            <span></span>
          </div>

          <p class="content-intro">
            {{
              product.moTa ||
              "Sản phẩm được An Yên tuyển chọn nhằm bảo đảm tính trang trọng, phù hợp với nhu cầu và nghi thức của từng gia đình."
            }}
          </p>

          <!-- Chi tiết sản phẩm dạng bài viết -->
          <div v-if="product.sanPhamChiTiets && product.sanPhamChiTiets.length > 0" class="product-article-details">
            <div v-for="chiTiet in sortedChiTiets(product.sanPhamChiTiets)" :key="chiTiet.maChiTiet" class="article-detail-item">
              <div v-if="chiTiet.loaiKhoi === 'tieu_de'" class="article-detail-title">
                <h3>{{ chiTiet.noiDung }}</h3>
              </div>
              <div v-else-if="chiTiet.loaiKhoi === 'noi_dung'" class="article-detail-content">
                <p>{{ chiTiet.noiDung }}</p>
              </div>
              <div v-else-if="chiTiet.loaiKhoi === 'hinh_anh' && product.sanPhamHinhAnhs" class="article-detail-image">
                <img
                    v-for="hinhAnh in getHinhAnhByChiTiet(product.sanPhamHinhAnhs, chiTiet.maChiTiet)"
                    :key="hinhAnh.maHinhAnh"
                    :src="formatHinhAnhUrl(hinhAnh.urlHinhAnh)"
                    :alt="hinhAnh.loaiHinhAnh"
                    loading="lazy"
                />
              </div>
            </div>
          </div>

          <div class="detail-feature-grid">
            <article class="detail-feature-card">
              <div class="detail-feature-icon">
                <i class="fa-solid fa-ruler-combined"></i>
              </div>

              <h3>QUY CÁCH</h3>

              <p>
                {{
                  product.quyCach ||
                  "Sản phẩm được hoàn thiện theo tiêu chuẩn và yêu cầu thực tế."
                }}
              </p>
            </article>

            <article class="detail-feature-card">
              <div class="detail-feature-icon">
                <i class="fa-solid fa-layer-group"></i>
              </div>

              <h3>CHẤT LIỆU</h3>

              <p>
                {{
                  product.vatLieu ||
                  "Chất liệu được chọn lọc kỹ lưỡng, bền đẹp và trang trọng."
                }}
              </p>
            </article>

            <article class="detail-feature-card">
              <div class="detail-feature-icon">
                <i class="fa-solid fa-palette"></i>
              </div>

              <h3>THIẾT KẾ</h3>

              <p>
                {{
                  product.thietKe ||
                  "Thiết kế hài hòa, tinh tế và phù hợp với không gian tang lễ."
                }}
              </p>
            </article>

            <article class="detail-feature-card">
              <div class="detail-feature-icon">
                <i class="fa-solid fa-shield-heart"></i>
              </div>

              <h3>BẢO QUẢN</h3>

              <p>
                {{
                  product.huongDanBaoQuan ||
                  "Bảo quản tại nơi khô ráo, thoáng mát và tránh độ ẩm cao."
                }}
              </p>
            </article>
          </div>

          <div class="specification-section">
            <h3>Thông số sản phẩm</h3>

            <div class="specification-table">
              <div class="specification-row">
                <span>Mã sản phẩm</span>
                <strong>{{ product.code || `SP${product.id}` }}</strong>
              </div>

              <div class="specification-row">
                <span>Loại sản phẩm</span>
                <strong>{{ product.loai || "Đang cập nhật" }}</strong>
              </div>

              <div class="specification-row">
                <span>Chất liệu</span>
                <strong>{{ product.vatLieu || "Đang cập nhật" }}</strong>
              </div>

              <div class="specification-row">
                <span>Kích thước</span>
                <strong>{{ product.kichThuoc || "Theo mẫu" }}</strong>
              </div>

              <div class="specification-row">
                <span>Trọng lượng</span>
                <strong>{{ product.trongLuong || "Đang cập nhật" }}</strong>
              </div>

              <div class="specification-row">
                <span>Công nghệ sản xuất</span>
                <strong>{{ product.congNgheSX || "Tiêu chuẩn" }}</strong>
              </div>

              <div class="specification-row">
                <span>Nhà cung cấp</span>
                <strong>{{ product.nhaCungCap || "An Yên" }}</strong>
              </div>

              <div class="specification-row">
                <span>Xuất xứ</span>
                <strong>{{ product.xuatXu || "Việt Nam" }}</strong>
              </div>
            </div>
          </div>

          <div class="process-section">
            <div class="content-heading">
              <span></span>
              <h2>QUY TRÌNH TƯ VẤN</h2>
              <span></span>
            </div>

            <p class="content-intro">
              An Yên đồng hành cùng gia đình trong từng bước, bảo đảm quá
              trình lựa chọn sản phẩm diễn ra chu đáo và minh bạch.
            </p>

            <div class="process-list">
              <div
                  v-for="(step, index) in processSteps"
                  :key="step.title"
                  class="process-item"
              >
                <div class="process-icon">
                  <i :class="step.icon"></i>
                </div>

                <span class="process-number">
                  {{ index + 1 }}
                </span>

                <h3>{{ step.title }}</h3>
                <p>{{ step.description }}</p>

                <i
                    v-if="index < processSteps.length - 1"
                    class="fa-solid fa-arrow-right process-arrow"
                ></i>
              </div>
            </div>
          </div>

          <div class="custom-support">
            <div>
              <span>HỖ TRỢ TÙY CHỈNH</span>

              <h3>Sản phẩm có thể điều chỉnh theo nhu cầu gia đình</h3>

              <p>
                Màu sắc, kích thước, chất liệu và các chi tiết trang trí có
                thể được tư vấn riêng để phù hợp với từng nghi thức.
              </p>
            </div>

            <button type="button" @click="requestConsultation">
              TƯ VẤN MIỄN PHÍ
            </button>
          </div>
        </main>

        <aside class="quote-card">
          <i class="fa-solid fa-quote-left"></i>

          <p>
            An Yên trong tâm thức,<br />
            trọn vẹn trong từng khoảnh khắc.
          </p>

          <div class="lotus-decoration">
            <i class="fa-solid fa-spa"></i>
          </div>
        </aside>
      </div>
    </section>

    <!-- RELATED PRODUCTS -->
    <section
        v-if="relatedProducts.length"
        class="related-products-section"
    >
      <div class="page-container">
        <div class="related-heading">
          <span>SẢN PHẨM TƯƠNG TỰ</span>
          <h2>Sản phẩm liên quan</h2>
          <p>
            Những sản phẩm được lựa chọn dựa trên cùng nhu cầu và danh mục.
          </p>
        </div>

        <div class="related-product-grid">
          <article
              v-for="item in relatedProducts"
              :key="item.id"
              class="related-product-card"
              @click="goToProduct(item.id)"
          >
            <div class="related-product-image">
              <img
                  :src="getProductImage(item)"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImageError"
              />

              <button
                  type="button"
                  aria-label="Xem chi tiết sản phẩm"
                  @click.stop="goToProduct(item.id)"
              >
                <i class="fa-solid fa-arrow-right"></i>
              </button>
            </div>

            <div class="related-product-info">
              <span>{{ item.loai || "Sản phẩm An Yên" }}</span>
              <h3>{{ item.name }}</h3>

              <div class="related-price-row">
                <strong>{{ formatPrice(item.price) }}</strong>

                <del v-if="item.oldPrice">
                  {{ formatPrice(item.oldPrice) }}
                </del>
              </div>
            </div>
          </article>
        </div>

        <div class="view-all-wrapper">
          <button
              type="button"
              class="btn-outline-primary"
              @click="router.push('/san-pham')"
          >
            XEM TẤT CẢ SẢN PHẨM
            <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  onMounted,
  ref,
  watch
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  getProductById,
  getProducts
} from "../../services/productService.js";

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const product = ref(null);
const relatedProducts = ref([]);
const selectedImage = ref("");
const phoneNumber = ref("");
const isWished = ref(false);

const DEFAULT_IMAGE =
    "data:image/svg+xml;charset=UTF-8," +
    encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="900" height="700">
      <rect width="100%" height="100%" fill="#f4f0eb"/>
      <circle cx="450" cy="300" r="75" fill="#e6ddd4"/>
      <path d="M410 320l42-46 34 38 31-27 73 85H310z" fill="#c8b7a6"/>
      <text
        x="50%"
        y="440"
        text-anchor="middle"
        font-family="Arial, sans-serif"
        font-size="28"
        fill="#8b7662"
      >
        Chưa có hình ảnh
      </text>
    </svg>
  `);

const processSteps = [
  {
    icon: "fa-solid fa-phone",
    title: "Tiếp nhận thông tin",
    description: "Lắng nghe nhu cầu và tư vấn sản phẩm phù hợp."
  },
  {
    icon: "fa-solid fa-file-signature",
    title: "Xác nhận sản phẩm",
    description: "Thống nhất mẫu mã, số lượng và chi phí."
  },
  {
    icon: "fa-solid fa-box",
    title: "Chuẩn bị sản phẩm",
    description: "Kiểm tra kỹ sản phẩm trước khi bàn giao."
  },
  {
    icon: "fa-solid fa-truck-fast",
    title: "Giao hàng tận nơi",
    description: "Giao đúng thời gian và địa điểm yêu cầu."
  },
  {
    icon: "fa-solid fa-hand-holding-heart",
    title: "Hỗ trợ sau giao",
    description: "Tiếp tục đồng hành khi gia đình cần hỗ trợ."
  }
];

const formatPrice = (value) => {
  if (
      value === null ||
      value === undefined ||
      value === "" ||
      Number.isNaN(Number(value))
  ) {
    return "Liên hệ";
  }

  return `${Number(value).toLocaleString("vi-VN")} ₫`;
};

const normalizeImage = (image) => {
  if (!image) return null;

  if (typeof image === "object") {
    image =
        image.url ||
        image.Url ||
        image.imageUrl ||
        image.hinhAnh ||
        image.hinhAnhUrl ||
        image.duongDan ||
        image.anh ||
        null;
  }

  if (typeof image !== "string" || !image.trim()) {
    return null;
  }

  const normalized = image.trim();

  if (
      normalized.startsWith("http://") ||
      normalized.startsWith("https://") ||
      normalized.startsWith("blob:") ||
      normalized.startsWith("data:")
  ) {
    return normalized;
  }

  if (normalized.startsWith("/")) {
    return normalized;
  }

  return `/images/${normalized}`;
};

const getProductImages = (item) => {
  if (!item) return [DEFAULT_IMAGE];

  const images = [];

  const arraySources = [
    item.images,
    item.sanPhamAnh,
    item.sanPhamAnhs,
    item.danhSachAnh,
    item.hinhAnhs,
    item.anhSanPham
  ];

  arraySources.forEach((source) => {
    if (Array.isArray(source)) {
      source.forEach((image) => {
        const normalized = normalizeImage(image);

        if (normalized) {
          images.push(normalized);
        }
      });
    }
  });

  const singleSources = [
    item.image,
    item.hinhAnh,
    item.anhDaiDien,
    item.urlAnh,
    item.imageUrl,
    item.hinhAnhUrl
  ];

  singleSources.forEach((source) => {
    const normalized = normalizeImage(source);

    if (normalized) {
      images.push(normalized);
    }
  });

  const uniqueImages = [...new Set(images)];

  return uniqueImages.length ? uniqueImages : [DEFAULT_IMAGE];
};

const getProductImage = (item) => {
  return getProductImages(item)[0];
};

const galleryImages = computed(() => {
  return getProductImages(product.value);
});

const discountAmount = computed(() => {
  const price = Number(product.value?.price || 0);
  const oldPrice = Number(product.value?.oldPrice || 0);

  if (oldPrice > price) {
    return oldPrice - price;
  }

  const discount = Number(product.value?.discount || 0);

  return discount > 0 ? discount : 0;
});

const discountPercent = computed(() => {
  const price = Number(product.value?.price || 0);
  const oldPrice = Number(product.value?.oldPrice || 0);

  if (oldPrice <= price || oldPrice <= 0) {
    return 0;
  }

  return Math.round(((oldPrice - price) / oldPrice) * 100);
});

const isAvailable = computed(() => {
  const status = String(product.value?.trangThai || "").toLowerCase();
  const quantity = Number(product.value?.soLuong || 0);

  return (
      quantity > 0 &&
      !status.includes("ngừng") &&
      !status.includes("hết")
  );
});

const handleImageError = (event) => {
  const image = event.currentTarget;

  if (image.dataset.fallbackApplied === "true") {
    return;
  }

  image.dataset.fallbackApplied = "true";
  image.src = DEFAULT_IMAGE;
};

// Sắp xếp chi tiết sản phẩm theo thứ tự
const sortedChiTiets = (chiTiets) => {
  if (!chiTiets || chiTiets.length === 0) return []
  return [...chiTiets].sort((a, b) => (a.thuTu || 0) - (b.thuTu || 0))
}

// Lấy hình ảnh theo chi tiết
const getHinhAnhByChiTiet = (hinhAnhs, maChiTiet) => {
  if (!hinhAnhs || hinhAnhs.length === 0) return []
  return hinhAnhs.filter(ha => ha.maChiTiet === maChiTiet)
}

// Format URL hình ảnh
const formatHinhAnhUrl = (url) => {
  if (!url) return DEFAULT_IMAGE
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) {
    return url
  }
  return `/images/${url}`
}

const toggleWish = () => {
  isWished.value = !isWished.value;

  ElMessage({
    type: "success",
    message: isWished.value
        ? "Đã thêm sản phẩm vào yêu thích"
        : "Đã xóa sản phẩm khỏi danh sách yêu thích"
  });
};

const requestConsultation = () => {
  const normalizedPhone = phoneNumber.value.replace(/\s+/g, "");

  if (!normalizedPhone) {
    ElMessage.warning("Vui lòng nhập số điện thoại để được tư vấn");
    return;
  }

  if (!/^(0|\+84)[0-9]{9,10}$/.test(normalizedPhone)) {
    ElMessage.warning("Số điện thoại chưa đúng định dạng");
    return;
  }

  ElMessage.success(
      "An Yên đã tiếp nhận yêu cầu và sẽ liên hệ với bạn sớm nhất"
  );

  phoneNumber.value = "";
};

const goToProduct = (id) => {
  router.push(`/san-pham/${id}`);
};

const loadRelatedProducts = async () => {
  relatedProducts.value = [];

  if (!product.value?.loai) {
    return;
  }

  try {
    const response = await getProducts({
      loai: product.value.loai,
      page: 1,
      pageSize: 8
    });

    relatedProducts.value = (response.items || [])
        .filter(
            (item) =>
                String(item.id) !== String(product.value.id)
        )
        .slice(0, 4);
  } catch (error) {
    console.error("Lỗi tải sản phẩm liên quan:", error);
  }
};

const loadProduct = async () => {
  loading.value = true;
  product.value = null;

  try {
    const productId = route.params.id;
    const result = await getProductById(productId);

    product.value = result;

    if (product.value) {
      selectedImage.value = getProductImage(product.value);
      await loadRelatedProducts();
    }
  } catch (error) {
    console.error("Lỗi tải chi tiết sản phẩm:", error);
    product.value = null;
  } finally {
    loading.value = false;

    await nextTick();

    window.scrollTo({
      top: 0,
      behavior: "smooth"
    });
  }
};

watch(
    () => route.params.id,
    (newId, oldId) => {
      if (newId && newId !== oldId) {
        loadProduct();
      }
    }
);

watch(galleryImages, (images) => {
  if (
      images.length &&
      !images.includes(selectedImage.value)
  ) {
    selectedImage.value = images[0];
  }
});

onMounted(loadProduct);
</script>

<style scoped>
.product-detail-page {
  --primary: #a20c1b;
  --primary-dark: #7d0713;
  --primary-soft: #f8ebed;
  --navy: #0e2b47;
  --navy-light: #173d60;
  --cream: #faf7f2;
  --cream-dark: #f3ede6;
  --white: #ffffff;
  --text: #182536;
  --text-soft: #66717e;
  --border: #e7e0d8;
  --success: #268256;
  --shadow: 0 14px 40px rgba(44, 34, 28, 0.08);

  background: var(--cream);
  color: var(--text);
  font-family: "Noto Sans", sans-serif;
}

.page-container {
  width: min(100% - 40px, 1380px);
  margin: 0 auto;
}

.page-state {
  min-height: 620px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 40px 20px;
  text-align: center;
  background: #faf7f2;
}

.page-state h2 {
  margin: 5px 0 0;
  color: #0e2b47;
  font-family: "Faustina", serif;
  font-size: 30px;
}

.page-state p {
  margin: 0;
  color: #6a737d;
}

.state-spinner,
.state-icon {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #ffffff;
  color: #a20c1b;
  box-shadow: 0 10px 30px rgba(44, 34, 28, 0.08);
  font-size: 30px;
}

.btn-primary {
  margin-top: 10px;
  min-height: 48px;
  padding: 0 24px;
  border: 0;
  border-radius: 7px;
  background: #a20c1b;
  color: #ffffff;
  font-weight: 700;
  cursor: pointer;
}

.breadcrumb-section {
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.88);
}

.breadcrumb-nav {
  min-height: 62px;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  color: var(--text-soft);
  font-size: 13px;
}

.breadcrumb-nav a {
  color: var(--text-soft);
  text-decoration: none;
  transition: color 0.2s ease;
  white-space: nowrap;
}

.breadcrumb-nav a:hover {
  color: var(--primary);
}

.breadcrumb-nav span {
  overflow: hidden;
  color: var(--primary);
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.breadcrumb-divider {
  flex: 0 0 auto;
  color: #a9a29b;
  font-size: 9px;
}

.product-overview-section {
  padding: 42px 0 55px;
  background: var(--white);
}

.product-overview-layout {
  display: grid;
  grid-template-columns:
    minmax(390px, 1.08fr)
    minmax(370px, 1fr)
    minmax(290px, 0.7fr);
  gap: 34px;
  align-items: start;
}

.product-gallery {
  min-width: 0;
}

.main-image-wrapper {
  position: relative;
  aspect-ratio: 1.13;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--cream-dark);
}

.main-image-wrapper > img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.main-image-wrapper:hover > img {
  transform: scale(1.035);
}

.image-discount-badge {
  position: absolute;
  z-index: 2;
  top: 16px;
  left: 16px;
  min-width: 54px;
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--primary);
  color: var(--white);
  font-size: 13px;
  font-weight: 800;
  text-align: center;
}

.main-wishlist-btn {
  position: absolute;
  z-index: 2;
  top: 16px;
  right: 16px;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(162, 12, 27, 0.16);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.94);
  color: var(--navy);
  cursor: pointer;
  transition: 0.25s ease;
}

.main-wishlist-btn:hover,
.main-wishlist-btn.active {
  border-color: var(--primary);
  background: var(--primary);
  color: var(--white);
}

.thumbnail-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.thumbnail-item {
  aspect-ratio: 1.2;
  overflow: hidden;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 7px;
  background: var(--cream-dark);
  cursor: pointer;
  transition: 0.22s ease;
}

.thumbnail-item:hover,
.thumbnail-item.active {
  border-color: var(--primary);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.gallery-note {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 13px;
  color: var(--text-soft);
  font-size: 12px;
}

.product-main-info {
  min-width: 0;
  padding-top: 4px;
}

.product-category-label {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  padding: 0 12px;
  border-radius: 4px;
  background: var(--primary);
  color: var(--white);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.45px;
  text-transform: uppercase;
}

.product-title {
  margin: 12px 0 3px;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: clamp(32px, 3vw, 44px);
  font-weight: 700;
  line-height: 1.12;
}

.product-subtitle {
  margin: 0;
  color: var(--text);
  font-family: "Faustina", serif;
  font-size: 18px;
  line-height: 1.5;
}

.product-code-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 10px 20px;
  margin-top: 18px;
  color: var(--text-soft);
  font-size: 12px;
}

.product-code-row strong {
  color: var(--navy);
}

.status-dot-row {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #a9a9a9;
}

.status-dot.available {
  background: var(--success);
  box-shadow: 0 0 0 4px rgba(38, 130, 86, 0.12);
}

.lotus-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 22px 0;
  color: var(--primary);
}

.lotus-divider span {
  width: 90px;
  height: 1px;
  background: linear-gradient(
      to right,
      transparent,
      rgba(162, 12, 27, 0.55)
  );
}

.lotus-divider span:last-child {
  background: linear-gradient(
      to left,
      transparent,
      rgba(162, 12, 27, 0.55)
  );
}

.price-label {
  margin-bottom: 5px;
  color: var(--text-soft);
  font-size: 12px;
}

.price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 13px;
}

.current-price {
  color: var(--primary);
  font-size: 30px;
  line-height: 1.2;
}

.old-price {
  color: #8d939a;
  font-size: 14px;
}

.saving-text {
  margin-top: 5px;
  color: var(--success);
  font-size: 12px;
  font-weight: 600;
}

.quick-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  margin-top: 24px;
  padding: 17px 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.quick-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border-right: 1px solid var(--border);
}

.quick-feature:first-child {
  padding-left: 0;
}

.quick-feature:last-child {
  padding-right: 0;
  border-right: 0;
}

.quick-feature-icon {
  flex: 0 0 auto;
  width: 37px;
  height: 37px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(162, 12, 27, 0.2);
  border-radius: 50%;
  color: var(--primary);
}

.quick-feature strong,
.quick-feature span {
  display: block;
}

.quick-feature strong {
  color: var(--navy);
  font-size: 11px;
}

.quick-feature span {
  margin-top: 2px;
  color: var(--text-soft);
  font-size: 9px;
}

.product-spec-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 9px 18px;
  margin-top: 21px;
}

.summary-item {
  min-width: 0;
  padding-bottom: 9px;
  border-bottom: 1px dashed var(--border);
}

.summary-item span,
.summary-item strong {
  display: block;
}

.summary-item span {
  margin-bottom: 3px;
  color: var(--text-soft);
  font-size: 11px;
}

.summary-item strong {
  overflow: hidden;
  color: var(--navy);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.consult-inline-form {
  margin-top: 22px;
}

.consult-inline-form label {
  display: block;
  margin-bottom: 8px;
  color: var(--navy);
  font-size: 12px;
  font-weight: 700;
}

.phone-field {
  min-height: 50px;
  display: flex;
  align-items: center;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--white);
}

.phone-field > i {
  margin-left: 16px;
  color: var(--primary);
}

.phone-field input {
  min-width: 0;
  flex: 1;
  height: 48px;
  padding: 0 12px;
  border: 0;
  outline: none;
  color: var(--text);
  font: inherit;
  font-size: 13px;
}

.phone-field button {
  align-self: stretch;
  padding: 0 20px;
  border: 0;
  background: var(--primary);
  color: var(--white);
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.2s ease;
}

.phone-field button:hover {
  background: var(--primary-dark);
}

.product-summary-column {
  position: sticky;
  top: 100px;
  display: grid;
  gap: 18px;
}

.summary-card,
.support-card {
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--white);
  box-shadow: 0 10px 30px rgba(37, 29, 24, 0.045);
}

.summary-card {
  padding: 24px 22px;
  text-align: center;
}

.summary-card-title {
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border);
  color: var(--navy);
  font-size: 13px;
  font-weight: 800;
  text-align: left;
}

.summary-card-icon {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  margin: 21px auto 11px;
  border-radius: 50%;
  background: var(--navy);
  color: var(--white);
  font-size: 22px;
}

.summary-card h2 {
  margin: 0;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 20px;
}

.summary-card > p {
  margin: 5px 0 18px;
  color: var(--text-soft);
  font-size: 11px;
  line-height: 1.55;
}

.summary-card-price {
  padding: 16px 0;
  border-top: 1px solid var(--border);
}

.summary-card-price span,
.summary-card-price strong,
.summary-card-price small {
  display: block;
}

.summary-card-price span {
  color: var(--text-soft);
  font-size: 12px;
}

.summary-card-price strong {
  margin: 3px 0;
  color: var(--primary);
  font-size: 24px;
}

.summary-card-price small {
  color: var(--text-soft);
  font-size: 10px;
}

.summary-primary-btn,
.summary-outline-btn {
  width: 100%;
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
}

.summary-primary-btn {
  margin-top: 4px;
  border: 1px solid var(--primary);
  background: var(--primary);
  color: var(--white);
}

.summary-primary-btn:hover {
  background: var(--primary-dark);
}

.summary-outline-btn {
  margin-top: 10px;
  border: 1px solid var(--primary);
  background: var(--white);
  color: var(--primary);
}

.summary-outline-btn:hover {
  background: var(--primary-soft);
}

.summary-benefits {
  display: grid;
  gap: 12px;
  margin: 21px 0 0;
  padding: 19px 0 0;
  border-top: 1px solid var(--border);
  list-style: none;
  text-align: left;
}

.summary-benefits li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-soft);
  font-size: 11px;
}

.summary-benefits i {
  width: 17px;
  color: var(--navy);
  text-align: center;
}

.support-card {
  padding: 22px;
}

.support-card > span {
  color: var(--navy);
  font-size: 13px;
  font-weight: 800;
}

.support-card > p {
  margin: 12px 0 17px;
  color: var(--text-soft);
  font-size: 11px;
  line-height: 1.65;
}

.support-card a {
  display: flex;
  align-items: center;
  gap: 13px;
  color: var(--primary);
  text-decoration: none;
}

.support-card a > i {
  font-size: 23px;
}

.support-card a span {
  display: grid;
}

.support-card a strong {
  font-size: 21px;
}

.support-card a small {
  color: var(--text-soft);
  font-size: 10px;
}

.product-content-section {
  padding: 45px 0 55px;
  border-top: 1px solid var(--border);
  background: var(--cream);
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 32px;
  align-items: start;
}

.detail-main-column {
  padding: 32px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--white);
}

.content-heading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 17px;
}

.content-heading span {
  width: 85px;
  height: 1px;
  background: rgba(162, 12, 27, 0.4);
}

.content-heading h2 {
  margin: 0;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 22px;
  text-align: center;
}

.content-intro {
  max-width: 820px;
  margin: 17px auto 31px;
  color: var(--text-soft);
  font-size: 13px;
  line-height: 1.8;
  text-align: center;
}

/* Chi tiết sản phẩm dạng bài viết */
.product-article-details {
  margin: 24px 0 32px;
  padding: 28px;
  background: var(--white);
  border-radius: 12px;
  border: 1px solid var(--border);
}

.article-detail-item {
  margin-bottom: 20px;
}

.article-detail-item:last-child {
  margin-bottom: 0;
}

.article-detail-title h3 {
  font-family: "Faustina", serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--navy);
  margin: 0 0 12px 0;
  letter-spacing: 0.3px;
}

.article-detail-content p {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text);
  margin: 0 0 12px 0;
}

.article-detail-image {
  margin-top: 12px;
}

.article-detail-image img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 10px;
}

.article-detail-image img:last-child {
  margin-bottom: 0;
}

.detail-feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.detail-feature-card {
  min-width: 0;
  padding: 27px 19px;
  border-right: 1px solid var(--border);
  text-align: center;
}

.detail-feature-card:last-child {
  border-right: 0;
}

.detail-feature-icon {
  width: 53px;
  height: 53px;
  display: grid;
  place-items: center;
  margin: 0 auto 13px;
  color: var(--primary);
  font-size: 27px;
}

.detail-feature-card h3 {
  margin: 0 0 9px;
  color: var(--navy);
  font-size: 12px;
}

.detail-feature-card p {
  margin: 0;
  color: var(--text-soft);
  font-size: 11px;
  line-height: 1.7;
}

.specification-section {
  margin-top: 35px;
}

.specification-section h3 {
  margin: 0 0 16px;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 20px;
}

.specification-table {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  border: 1px solid var(--border);
}

.specification-row {
  min-height: 50px;
  display: grid;
  grid-template-columns: minmax(130px, 0.8fr) 1.2fr;
  align-items: center;
  border-bottom: 1px solid var(--border);
}

.specification-row:nth-child(odd) {
  border-right: 1px solid var(--border);
}

.specification-row:nth-last-child(-n + 2) {
  border-bottom: 0;
}

.specification-row span,
.specification-row strong {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 12px 15px;
  font-size: 12px;
}

.specification-row span {
  background: var(--cream);
  color: var(--text-soft);
}

.specification-row strong {
  color: var(--navy);
  font-weight: 600;
}

.process-section {
  margin-top: 45px;
  padding-top: 36px;
  border-top: 1px solid var(--border);
}

.process-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px;
}

.process-item {
  position: relative;
  text-align: center;
}

.process-icon {
  width: 59px;
  height: 59px;
  display: grid;
  place-items: center;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: var(--primary);
  color: var(--white);
  font-size: 22px;
}

.process-number {
  display: block;
  margin-bottom: 6px;
  color: var(--primary);
  font-size: 11px;
  font-weight: 800;
}

.process-item h3 {
  margin: 0 0 6px;
  color: var(--navy);
  font-size: 12px;
}

.process-item p {
  margin: 0;
  color: var(--text-soft);
  font-size: 10px;
  line-height: 1.6;
}

.process-arrow {
  position: absolute;
  top: 24px;
  right: -17px;
  color: rgba(162, 12, 27, 0.42);
  font-size: 14px;
}

.custom-support {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  margin-top: 39px;
  padding: 24px 26px;
  border-left: 4px solid var(--primary);
  background: var(--primary-soft);
}

.custom-support span {
  color: var(--primary);
  font-size: 11px;
  font-weight: 800;
}

.custom-support h3 {
  margin: 5px 0 6px;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 19px;
}

.custom-support p {
  max-width: 680px;
  margin: 0;
  color: var(--text-soft);
  font-size: 11px;
  line-height: 1.7;
}

.custom-support button {
  flex: 0 0 auto;
  min-height: 43px;
  padding: 0 20px;
  border: 1px solid var(--primary);
  border-radius: 4px;
  background: var(--white);
  color: var(--primary);
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
}

.custom-support button:hover {
  background: var(--primary);
  color: var(--white);
}

.quote-card {
  position: sticky;
  top: 100px;
  min-height: 410px;
  overflow: hidden;
  padding: 35px 27px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background:
      radial-gradient(
          circle at 75% 90%,
          rgba(211, 151, 163, 0.4),
          transparent 25%
      ),
      linear-gradient(145deg, #f8eceb, #f6f1e9);
}

.quote-card > i {
  color: rgba(14, 43, 71, 0.44);
  font-size: 27px;
}

.quote-card p {
  margin: 45px 0 0;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 20px;
  line-height: 1.65;
}

.lotus-decoration {
  position: absolute;
  right: -20px;
  bottom: -25px;
  color: rgba(162, 12, 27, 0.18);
  font-size: 145px;
}

.related-products-section {
  padding: 58px 0 70px;
  background: var(--white);
}

.related-heading {
  text-align: center;
}

.related-heading > span {
  color: var(--primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.4px;
}

.related-heading h2 {
  margin: 7px 0;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 31px;
}

.related-heading p {
  margin: 0;
  color: var(--text-soft);
  font-size: 12px;
}

.related-product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 21px;
  margin-top: 31px;
}

.related-product-card {
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--white);
  cursor: pointer;
  transition: 0.3s ease;
}

.related-product-card:hover {
  transform: translateY(-6px);
  border-color: rgba(162, 12, 27, 0.4);
  box-shadow: var(--shadow);
}

.related-product-image {
  position: relative;
  aspect-ratio: 1.2;
  overflow: hidden;
  background: var(--cream-dark);
}

.related-product-image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.related-product-card:hover .related-product-image img {
  transform: scale(1.05);
}

.related-product-image button {
  position: absolute;
  right: 13px;
  bottom: 13px;
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 50%;
  background: var(--white);
  color: var(--primary);
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(33, 24, 19, 0.14);
}

.related-product-info {
  padding: 17px;
}

.related-product-info > span {
  color: var(--primary);
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
}

.related-product-info h3 {
  min-height: 44px;
  margin: 7px 0 11px;
  color: var(--navy);
  font-family: "Faustina", serif;
  font-size: 18px;
  line-height: 1.25;
}

.related-price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.related-price-row strong {
  color: var(--primary);
  font-size: 17px;
}

.related-price-row del {
  color: #939393;
  font-size: 11px;
}

.view-all-wrapper {
  margin-top: 31px;
  text-align: center;
}

.btn-outline-primary {
  min-height: 45px;
  padding: 0 22px;
  border: 1px solid var(--primary);
  border-radius: 4px;
  background: var(--white);
  color: var(--primary);
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
}

.btn-outline-primary:hover {
  background: var(--primary);
  color: var(--white);
}

@media (max-width: 1180px) {
  .product-overview-layout {
    grid-template-columns: minmax(360px, 1fr) minmax(360px, 1fr);
  }

  .product-summary-column {
    position: static;
    grid-column: 1 / -1;
    grid-template-columns: 1fr 1fr;
  }

  .summary-card,
  .support-card {
    height: 100%;
  }

  .detail-feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-feature-card:nth-child(2) {
    border-right: 0;
  }

  .detail-feature-card:nth-child(-n + 2) {
    border-bottom: 1px solid var(--border);
  }

  .process-list {
    grid-template-columns: repeat(3, 1fr);
    row-gap: 30px;
  }

  .process-arrow {
    display: none;
  }
}

@media (max-width: 900px) {
  .product-overview-layout,
  .content-layout {
    grid-template-columns: 1fr;
  }

  .product-gallery,
  .product-summary-column,
  .quote-card {
    position: static;
  }

  .product-summary-column {
    grid-template-columns: 1fr 1fr;
  }

  .quote-card {
    min-height: 270px;
  }

  .specification-table {
    grid-template-columns: 1fr;
  }

  .specification-row:nth-child(odd) {
    border-right: 0;
  }

  .specification-row:nth-last-child(-n + 2) {
    border-bottom: 1px solid var(--border);
  }

  .specification-row:last-child {
    border-bottom: 0;
  }

  .related-product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 650px) {
  .page-container {
    width: min(100% - 28px, 1380px);
  }

  .breadcrumb-nav {
    min-height: 52px;
    font-size: 11px;
  }

  .product-overview-section {
    padding: 22px 0 32px;
  }

  .product-title {
    font-size: 30px;
  }

  .product-subtitle {
    font-size: 16px;
  }

  .quick-features {
    grid-template-columns: 1fr;
    gap: 13px;
  }

  .quick-feature {
    padding: 0;
    border-right: 0;
  }

  .product-spec-summary {
    grid-template-columns: 1fr;
  }

  .phone-field {
    flex-wrap: wrap;
    padding-top: 3px;
  }

  .phone-field input {
    min-width: 180px;
  }

  .phone-field button {
    width: 100%;
    min-height: 44px;
  }

  .product-summary-column {
    grid-template-columns: 1fr;
  }

  .detail-main-column {
    padding: 23px 16px;
  }

  .content-heading {
    gap: 10px;
  }

  .content-heading span {
    width: 40px;
  }

  .content-heading h2 {
    font-size: 19px;
  }

  .detail-feature-grid {
    grid-template-columns: 1fr;
  }

  .detail-feature-card {
    border-right: 0;
    border-bottom: 1px solid var(--border);
  }

  .detail-feature-card:last-child {
    border-bottom: 0;
  }

  .process-list {
    grid-template-columns: repeat(2, 1fr);
  }

  .custom-support {
    align-items: stretch;
    flex-direction: column;
  }

  .custom-support button {
    width: 100%;
  }

  .related-product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .related-product-info {
    padding: 13px;
  }

  .related-product-info h3 {
    min-height: 39px;
    font-size: 15px;
  }

  .related-price-row strong {
    font-size: 14px;
  }

  .thumbnail-list {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 420px) {
  .product-code-row {
    flex-direction: column;
  }

  .price-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 5px;
  }

  .process-list {
    grid-template-columns: 1fr;
  }

  .related-product-grid {
    grid-template-columns: 1fr;
  }
}
</style>