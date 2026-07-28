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

        <p>
          Các gói dịch vụ được thiết kế đa dạng,
          phù hợp với nhu cầu và ngân sách của từng gia đình.
        </p>
      </div>

      <div class="package-grid">

        <!-- Đang tải -->
        <div
            v-if="loadingCombos"
            class="combo-message"
        >
          Đang tải các gói dịch vụ...
        </div>

        <!-- Lỗi tải dữ liệu -->
        <div
            v-else-if="comboError"
            class="combo-message combo-error"
        >
          {{ comboError }}
        </div>

        <!-- Không có dữ liệu -->
        <div
            v-else-if="packages.length === 0"
            class="combo-message"
        >
          Hiện chưa có gói dịch vụ đang hoạt động.
        </div>

        <!-- Danh sách combo từ database -->
        <template v-else>
          <div
              v-for="item in packages"
              :key="item.id"
              class="package-card"
          >
            <h3>{{ item.name }}</h3>

            <h5>{{ item.subtitle }}</h5>

            <!-- Ảnh đại diện lấy từ combo.HinhAnh -->
            <img
                :src="item.image"
                :alt="item.name"
                @error="handlePackageImageError"
            />

            <!-- Ghi chú lấy từ combo.GhiChu -->
            <ul
                v-if="item.benefits.length > 0"
                class="package-benefit-list"
            >
              <li
                  v-for="(benefit, index) in item.benefits"
                  :key="`${item.id}-${index}`"
              >
                <i class="fa-regular fa-circle-check"></i>

                <span>{{ benefit }}</span>
              </li>
            </ul>

            <div class="price">
              Từ <strong>{{ item.price }}</strong>
            </div>

            <button @click="goToDetail(item.id)">
              XEM CHI TIẾT
            </button>
          </div>
        </template>

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

        <p>
          Chúng tôi cung cấp các dịch vụ lễ linh hoạt
          theo nhu cầu của gia đình.
        </p>
      </div>

      <div class="single-service-grid">
        <div
            v-for="item in singleServices"
            :key="item.title"
            class="single-service-card"
        >
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

        <p>
          Quy trình chuyên nghiệp - Minh bạch - Chu đáo
        </p>
      </div>

      <div class="process-wrap">
        <div
            v-for="(step, index) in processSteps"
            :key="step.title"
            class="process-item"
        >
          <div class="process-icon">
            <i :class="step.icon"></i>
          </div>

          <h4>
            {{ index + 1 }}. {{ step.title }}
          </h4>

          <p>{{ step.desc }}</p>

          <span
              v-if="index < processSteps.length - 1"
              class="process-arrow"
          >
            →
          </span>
        </div>
      </div>

    </section>

    <!-- CTA -->
    <section class="service-cta">
      <div class="cta-left">

        <img :src="lotusImg" alt="Hoa sen An Yên" />

        <h3>
          AN YÊN ĐỒNG HÀNH CÙNG GIA ĐÌNH
          <br>
          TRONG KHOẢNH KHẮC THIÊNG LIÊNG NHẤT
        </h3>

      </div>
    </section>

  </main>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

import heroBanner from
      "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png";

import dividerIcon from
      "../../assets/images/icon/flower_icon.png";

const API_BASE_URL = "http://localhost:8080";

const lotusImg =
    "https://res.cloudinary.com/dnj7xhvs1/image/upload/v1780764451/lotus_qbceib.png";

const router = useRouter();

const packages = ref([]);
const loadingCombos = ref(false);
const comboError = ref("");

/**
 * Ảnh dự phòng khi combo chưa có ảnh
 * hoặc đường dẫn trong database bị lỗi.
 */
const fallbackImage =
    "data:image/svg+xml;charset=UTF-8," +
    encodeURIComponent(`
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="700"
      height="450"
    >
      <rect
        width="100%"
        height="100%"
        fill="#f4eee8"
      />

      <rect
        x="20"
        y="20"
        width="660"
        height="410"
        fill="none"
        stroke="#cdb9a7"
        stroke-width="3"
        stroke-dasharray="12 9"
      />

      <text
        x="50%"
        y="46%"
        text-anchor="middle"
        dominant-baseline="middle"
        font-family="Arial"
        font-size="30"
        fill="#8a6a52"
      >
        Đang cập nhật hình ảnh
      </text>

      <text
        x="50%"
        y="57%"
        text-anchor="middle"
        dominant-baseline="middle"
        font-family="Arial"
        font-size="21"
        fill="#aa8b70"
      >
        An Yên
      </text>
    </svg>
  `);

/**
 * Chuyển cột GhiChu dạng HTML:
 *
 * <ul>
 *   <li>Nội dung 1</li>
 *   <li>Nội dung 2</li>
 * </ul>
 *
 * thành mảng:
 *
 * [
 *   "Nội dung 1",
 *   "Nội dung 2"
 * ]
 */
const parseBenefits = (ghiChu) => {
  if (!ghiChu || typeof ghiChu !== "string") {
    return [];
  }

  const cleanValue = ghiChu.trim();

  if (!cleanValue) {
    return [];
  }

  try {
    const parser = new DOMParser();

    const htmlDocument = parser.parseFromString(
        cleanValue,
        "text/html"
    );

    const listItems = Array.from(
        htmlDocument.querySelectorAll("li")
    )
        .map((element) =>
            element.textContent?.trim()
        )
        .filter(Boolean);

    /*
     * Nếu GhiChu có thẻ li thì lấy nội dung thẻ li.
     */
    if (listItems.length > 0) {
      return listItems;
    }

    /*
     * Hỗ trợ trường hợp database lưu text xuống dòng,
     * không có HTML.
     */
    const plainText =
        htmlDocument.body.textContent?.trim() || "";

    return plainText
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter(Boolean);
  } catch (error) {
    console.error(
        "Không thể xử lý GhiChu của combo:",
        error
    );

    return [];
  }
};

/**
 * Chuẩn hóa ảnh lấy từ combo.HinhAnh.
 *
 * Hỗ trợ:
 * - URL Cloudinary
 * - URL http/https
 * - /images/TrangDichVu/goi1.png
 * - goi1.png
 */
const normalizeImageUrl = (imagePath) => {
  if (!imagePath || typeof imagePath !== "string") {
    return fallbackImage;
  }

  const cleanPath = imagePath.trim();

  if (!cleanPath) {
    return fallbackImage;
  }

  /*
   * Cloudinary hoặc URL đầy đủ.
   */
  if (
      cleanPath.startsWith("http://") ||
      cleanPath.startsWith("https://") ||
      cleanPath.startsWith("data:image") ||
      cleanPath.startsWith("blob:")
  ) {
    return cleanPath;
  }

  /*
   * URL dạng:
   * //res.cloudinary.com/...
   */
  if (cleanPath.startsWith("//")) {
    return `https:${cleanPath}`;
  }

  /*
   * Đường dẫn đã bắt đầu bằng /.
   *
   * Ví dụ:
   * /images/TrangDichVu/goi1.png
   */
  if (cleanPath.startsWith("/")) {
    return cleanPath;
  }

  /*
   * Database chỉ lưu tên file:
   * goi1.png
   *
   * Ảnh cần nằm trong:
   * public/images/TrangDichVu/goi1.png
   */
  return `/images/TrangDichVu/${cleanPath}`;
};

const formatPrice = (value) => {
  const numericValue = Number(value || 0);

  return (
      numericValue.toLocaleString(
          "vi-VN",
          {
            maximumFractionDigits: 0,
          }
      ) + " đ"
  );
};

const getComboId = (item) => {
  return (
      item.comboId ??
      item.ComboId ??
      item.id ??
      null
  );
};

const getComboStatus = (item) => {
  return Number(
      item.trangThai ??
      item.TrangThai ??
      0
  );
};

const handlePackageImageError = (event) => {
  event.target.onerror = null;
  event.target.src = fallbackImage;
};

const goToDetail = (id) => {
  if (!id) {
    return;
  }

  router.push(`/dich-vu/${id}`);
};

const loadCombos = async () => {
  loadingCombos.value = true;
  comboError.value = "";

  try {
    const response = await axios.get(
        `${API_BASE_URL}/api/dich-vu`
    );

    console.log(
        "Dữ liệu combo từ API:",
        response.data
    );

    const comboList = Array.isArray(response.data)
        ? response.data
        : [];

    packages.value = comboList
        /*
         * Chỉ hiện:
         * TrangThai = 1
         */
        .filter((item) =>
            getComboStatus(item) === 1
        )
        .map((item) => ({
          id: getComboId(item),

          name:
              item.tenCombo ??
              item.TenCombo ??
              "Gói dịch vụ",

          subtitle:
              item.moTa ??
              item.MoTa ??
              "Gói dịch vụ mai táng của An Yên.",

          /*
           * Một ảnh đại diện từ combo.HinhAnh.
           */
          image: normalizeImageUrl(
              item.hinhAnh ??
              item.HinhAnh
          ),

          /*
           * Danh sách quyền lợi từ combo.GhiChu.
           */
          benefits: parseBenefits(
              item.ghiChu ??
              item.GhiChu
          ),

          price: formatPrice(
              item.gia ??
              item.Gia
          ),

          /*
           * Có thể giữ mã đối tác nếu sau này
           * cần lọc hoặc hiển thị tên đối tác.
           */
          maDoiTac:
              item.maDoiTac ??
              item.MaDoiTac ??
              null,
        }));
  } catch (error) {
    console.error(
        "Lỗi tải danh sách combo:",
        error
    );

    packages.value = [];

    comboError.value =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Không thể tải danh sách gói dịch vụ.";
  } finally {
    loadingCombos.value = false;
  }
};

const singleServices = [
  {
    icon: "fa-solid fa-van-shuttle",
    title: "XE TANG LỄ",
    desc: "Đa dạng các dòng xe tang lễ hiện đại",
  },
  {
    icon: "fa-solid fa-table",
    title: "TRANG TRÍ TANG LỄ",
    desc:
        "Trang trí sảnh, bàn thờ, phông rạp theo yêu cầu",
  },
  {
    icon: "fa-solid fa-box",
    title: "ÁO QUAN",
    desc: "Nhiều mẫu mã, chất liệu cao cấp",
  },
  {
    icon: "fa-brands fa-pagelines",
    title: "VÒNG HOA",
    desc:
        "Vòng hoa tươi, hoa lan, hoa sen cao cấp",
  },
  {
    icon: "fa-regular fa-file-lines",
    title: "THIỆP TANG",
    desc:
        "Thiệp báo tin, cảm ơn thiết kế trang trọng",
  },
  {
    icon: "fa-solid fa-jar",
    title: "HẬU SỰ",
    desc:
        "Hỏa táng, lưu tro cốt, cải táng",
  },
  {
    icon: "fa-solid fa-people-group",
    title: "NHÂN SỰ",
    desc:
        "Cung cấp nhân sự phục vụ tang lễ chuyên nghiệp",
  },
];

const processSteps = [
  {
    icon: "fa-solid fa-phone",
    title: "TIẾP NHẬN",
    desc:
        "Tiếp nhận thông tin, tư vấn ban đầu 24/7",
  },
  {
    icon: "fa-regular fa-clipboard",
    title: "KHẢO SÁT & TƯ VẤN",
    desc:
        "Khảo sát, tư vấn gói dịch vụ phù hợp",
  },
  {
    icon: "fa-regular fa-file-lines",
    title: "THỎA THUẬN",
    desc:
        "Báo giá chi tiết, ký kết hợp đồng minh bạch",
  },
  {
    icon: "fa-solid fa-people-group",
    title: "TRIỂN KHAI",
    desc:
        "Chuẩn bị và triển khai theo đúng cam kết",
  },
  {
    icon: "fa-regular fa-heart",
    title: "ĐỒNG HÀNH",
    desc:
        "Đồng hành xuyên suốt, hỗ trợ tận tâm",
  },
  {
    icon: "fa-brands fa-pagelines",
    title: "HẬU MÃI",
    desc:
        "Hỗ trợ hậu sự và chăm sóc sau tang lễ",
  },
];

onMounted(() => {
  loadCombos();
});
</script>

<style
    scoped
    src="../../assets/styles/website/TrangDichVu.css"
></style>