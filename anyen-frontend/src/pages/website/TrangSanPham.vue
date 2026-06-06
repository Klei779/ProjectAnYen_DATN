<template>
  <!-- HERO SECTION -->
  <section class="hero-section" :style="{ backgroundImage: `url(${heroSectionTrangSanPham})` }">
    <div class="hero-overlay">
      <h1 class="hero-title">SẢN PHẨM</h1>
      <div class="hero-divider">
        <img :src="flowerIcon" alt="Hoa" class="lotus-icon-img" />
      </div>
      <p class="hero-subtitle">Tịnh tuyển từng sản phẩm – Giữ trọn lòng thành kính</p>
      <div class="search-wrapper">
        <i
            class="fa-solid fa-magnifying-glass search-icon"
            @click="searchProducts"
        ></i>

        <input
            v-model="keyword"
            class="search-box"
            placeholder="Tìm kiếm sản phẩm..."
            type="text"
            @keyup.enter="searchProducts"
        />
      </div>
    </div>
  </section>

  <!-- BREADCRUMB -->
  <div class="breadcrumb-bar">
    <div class="sp-container">
      <nav class="breadcrumb-nav">
        <a href="/anyen-frontend/public">Trang chủ</a>
        <span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span>
        <span class="bc-active">Sản phẩm</span>
      </nav>
    </div>
  </div>

  <!-- PRODUCT SECTION -->
  <section class="product-section">
    <div class="sp-container">
      <div class="sp-layout">

        <!-- SIDEBAR -->
        <aside class="sp-sidebar">
          <div class="sidebar-panel">

            <h3 class="filter-header">DANH MỤC SẢN PHẨM</h3>

            <!-- CATEGORY LIST -->
            <ul class="category-list">
              <li v-for="item in categories" :key="item.id" class="category-item"
                  :class="{ active: selectedCategoryId === item.id }"
                  @click="selectedCategoryId = (selectedCategoryId === item.id ? null : item.id)">
                <span class="cat-icon"><i :class="item.icon"></i></span>
                <span class="cat-name">{{ item.name }}</span>
                <span class="cat-count">{{ item.total }}</span>
              </li>
            </ul>

            <!-- PRICE -->
            <div class="filter-group">
              <div class="filter-header" @click="isPriceOpen = !isPriceOpen">
                <span>KHOẢNG GIÁ</span>
                <i class="fa-solid" :class="isPriceOpen ? 'fa-chevron-up' : 'fa-chevron-down'" />
              </div>
              <div v-show="isPriceOpen" class="filter-content">
                <el-slider v-model="priceRange" range :max="50000000" :step="100000" />
                <div class="price-boxes">
                  <div class="price-box">
                    <small>Từ</small>
                    <span>{{ formatPrice(priceRange[0]) }}</span>
                  </div>
                  <div class="price-sep">–</div>
                  <div class="price-box">
                    <small>Đến</small>
                    <span>{{ formatPrice(priceRange[1]) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- MATERIAL -->
            <div class="filter-group">
              <div class="filter-header" @click="isMaterialOpen = !isMaterialOpen">
                <span>CHẤT LIỆU</span>
                <i class="fa-solid" :class="isMaterialOpen ? 'fa-chevron-up' : 'fa-chevron-down'" />
              </div>
              <div v-show="isMaterialOpen" class="filter-content">
                <label v-for="item in materials" :key="item.id" class="checkbox-row">
                  <el-checkbox v-model="item.checked" />
                  <span class="checkbox-name">{{ item.name }}</span>
                  <span class="checkbox-count">{{ item.total }}</span>
                </label>
                <button class="view-more-btn">Xem thêm <i class="fa-solid fa-plus"></i></button>
              </div>
            </div>

            <!-- COLOR -->
            <div class="filter-group">
              <div class="filter-header" @click="isColorOpen = !isColorOpen">
                <span>MÀU SẮC</span>
                <i class="fa-solid" :class="isColorOpen ? 'fa-chevron-up' : 'fa-chevron-down'" />
              </div>
              <div v-show="isColorOpen" class="filter-content">
                <div class="color-list">
                  <div v-for="color in colors" :key="color.hex" class="color-item"
                       :style="{ background: color.hex }"
                       :class="{ 'color-selected': selectedColor === color.name }"
                       @click="selectedColor = selectedColor === color.name ? null : color.name" />
                </div>
                <button class="view-more-btn mt-2">Xem thêm <i class="fa-solid fa-chevron-down"></i></button>
              </div>
            </div>

            <!-- RELIGION -->
            <div class="filter-group">
              <div class="filter-header" @click="isReligionOpen = !isReligionOpen">
                <span>TÔN GIÁO</span>
                <i class="fa-solid" :class="isReligionOpen ? 'fa-chevron-up' : 'fa-chevron-down'" />
              </div>
              <div v-show="isReligionOpen" class="filter-content">
                <label v-for="item in religions" :key="item.id" class="checkbox-row">
                  <el-checkbox v-model="item.checked" />
                  <span class="checkbox-name">{{ item.name }}</span>
                  <span class="checkbox-count">{{ item.total }}</span>
                </label>
                <button class="view-more-btn">Xem thêm <i class="fa-solid fa-plus"></i></button>
              </div>
            </div>

            <button class="apply-filter-btn" @click="applyFilter">ÁP DỤNG BỘ LỌC</button>
            <button class="reset-filter-btn" @click="resetFilter">XÓA BỘ LỌC</button>

          </div>
        </aside>

        <!-- MAIN CONTENT -->
        <main class="sp-main">
          <div class="content-panel">

            <!-- TOOLBAR -->
            <div class="product-toolbar">
              <div class="product-count">
                Tất cả sản phẩm (<strong>{{ totalProducts }}</strong>)
              </div>
              <div class="toolbar-actions">
                <span class="sort-label">Sắp xếp theo:</span>
                <el-select v-model="sortBy" class="sort-select">
                  <el-option label="Mới nhất" value="newest" />
                  <el-option label="Cũ nhất" value="oldest" />
                  <el-option label="Giá tăng dần" value="price_asc" />
                  <el-option label="Giá giảm dần" value="price_desc" />
                </el-select>
              </div>
            </div>

            <!-- LOADING OVERLAY -->
            <div v-if="loading" class="loading-overlay">
              <i class="fa-solid fa-spinner fa-spin"></i>
              <span>Đang tải...</span>
            </div>

            <!-- PRODUCT GRID -->
            <div v-else class="product-grid">
              <!-- Không có kết quả -->
              <div v-if="products.length === 0" class="empty-state">
                <i class="fa-solid fa-box-open"></i>
                <p>Không tìm thấy sản phẩm phù hợp</p>
              </div>

              <div v-for="item in products" :key="item.id" class="product-card">
                <div class="product-image">
                  <img :src="item.image" :alt="item.name" />

                  <!-- BADGE -->
                  <span v-if="item.badge" class="product-badge" :class="item.badge.type">
                    {{ item.badge.label }}
                  </span>

                  <!-- WISHLIST -->
                  <button
                      class="wishlist-btn"
                      :class="{ active: isWished(item.id) }"
                      @click="toggleWish(item.id)"
                  >
                    <i :class="isWished(item.id) ? 'fa-solid fa-heart' : 'fa-regular fa-heart'"></i>
                  </button>
                </div>

                <div class="product-info">
                  <h4 class="product-name">{{ item.name }}</h4>
                  <p class="product-subname">{{ item.subname }}</p>
                  <div class="product-price-row">
                    <span class="product-price">{{ formatPrice(item.price) }}</span>
                    <span v-if="item.oldPrice" class="product-old-price">{{ formatPrice(item.oldPrice) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- PAGINATION -->
            <div class="pagination-wrapper">
              <el-pagination background layout="prev, pager, next" :total="totalProducts"
                             :page-size="pageSize" :current-page="currentPage"
                             @current-change="currentPage = $event" />
            </div>

          </div>
        </main>

      </div>
    </div>
  </section>

  <!-- TRUST BAR -->
  <section class="trust-bar">
    <div class="sp-container">
      <div class="trust-grid">
        <div class="trust-item" v-for="trust in trustItems" :key="trust.title">
          <div class="trust-icon">
            <i :class="trust.icon"></i>
          </div>
          <div class="trust-text">
            <strong>{{ trust.title }}</strong>
            <span>{{ trust.desc }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>

</template>


<script setup>
import { ref, watch, onMounted } from 'vue'
import heroSectionTrangSanPham from '../../assets/images/TrangSanPham/heroSection_TrangSanPham.png'
import flowerIcon from '../../assets/images/icon/flower_icon.png'
import {
  getProducts,
  getCategories,
  getMaterials,
  getReligions,
  getColors
} from '../../services/productService.js'

// ─── UI state ────────────────────────────────────────────────
const isPriceOpen    = ref(true)
const isMaterialOpen = ref(true)
const isColorOpen    = ref(true)
const isReligionOpen = ref(true)
const loading        = ref(false)

// ─── Filter / Sort state ─────────────────────────────────────
const keyword           = ref('')
const sortBy            = ref('newest')
const currentPage       = ref(1)
const pageSize          = ref(16)
const priceRange        = ref([0, 999_999_999])
const selectedCategoryId = ref(null)   // null = tất cả
const selectedColor     = ref(null)    // null = tất cả

// ─── Data từ service ──────────────────────────────────────────
const products   = ref([])
const totalProducts = ref(0)

// Bộ lọc — mỗi phần tử có thêm `checked` để bind v-model
const categories = ref([])
const materials  = ref([])
const religions  = ref([])
const colors     = ref([])

// Danh sách id chất liệu & tôn giáo đang được chọn (computed từ checked)
const selectedMaterialIds = ref([])
const selectedReligionIds = ref([])

// Trust bar (tĩnh — không cần JSON riêng)
const trustItems = [
  { icon: 'fa-solid fa-medal',       title: 'Sản phẩm chất lượng', desc: 'Được tuyển chọn kỹ lưỡng' },
  { icon: 'fa-solid fa-truck',       title: 'Giao hàng toàn quốc', desc: 'Nhanh chóng, an toàn'     },
  { icon: 'fa-solid fa-headset',     title: 'Tư vấn tận tâm',      desc: 'Hỗ trợ 24/7'             },
  { icon: 'fa-solid fa-rotate-left', title: 'Đổi trả dễ dàng',     desc: 'Trong 7 ngày'            }
]

// ─── Wishlist (dùng ref<number[]> thay vì Set để tránh vấn đề reactive) ──
const wishedIds = ref([])
const isWished  = (id) => wishedIds.value.includes(id)
const toggleWish = (id) => {
  const idx = wishedIds.value.indexOf(id)
  if (idx === -1) wishedIds.value.push(id)
  else wishedIds.value.splice(idx, 1)
}

// ─── Format ──────────────────────────────────────────────────
const formatPrice = (val) => {
  if (val === null || val === undefined) return 'Liên hệ'
  return val.toLocaleString('vi-VN') + ' đ'
}

// ─── Load filter options (chạy một lần) ─────────────────────
async function loadFilterOptions() {
  const [cats, mats, rels, cols] = await Promise.all([
    getCategories(),
    getMaterials(),
    getReligions(),
    getColors()
  ])

  categories.value = cats
  // Thêm checked để bind với el-checkbox
  materials.value  = mats.map((m) => ({ ...m, checked: false }))
  religions.value  = rels.map((r) => ({ ...r, checked: false }))
  colors.value     = cols
}

// ─── Load products ────────────────────────────────────────────
async function loadProducts() {
  loading.value = true

  try {
    const selectedCategory = categories.value.find(
        (c) => c.id === selectedCategoryId.value
    )

    const selectedMaterials = materials.value
        .filter((m) => m.checked)
        .map((m) => m.name)

    const selectedReligions = religions.value
        .filter((r) => r.checked)
        .map((r) => r.name)

    const { items, total } = await getProducts({
      keyword: keyword.value,
      loai: selectedCategory ? selectedCategory.name : '',
      vatLieu: selectedMaterials,
      tonGiao: selectedReligions,
      mauSac: selectedColor.value || '',
      priceRange: priceRange.value,
      sortBy: sortBy.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })

    products.value = items
    totalProducts.value = total
  } finally {
    loading.value = false
  }
}

// ─── Đồng bộ selectedMaterialIds / selectedReligionIds khi checkbox thay đổi ─
watch(
    () => materials.value.map((m) => m.checked),
    () => {
      selectedMaterialIds.value = materials.value
          .filter((m) => m.checked)
          .map((m) => m.id)
    },
    { deep: true }
)

watch(
    () => religions.value.map((r) => r.checked),
    () => {
      selectedReligionIds.value = religions.value
          .filter((r) => r.checked)
          .map((r) => r.id)
    },
    { deep: true }
)

// ─── Áp dụng bộ lọc khi người dùng nhấn nút ─────────────────
function applyFilter() {
  currentPage.value = 1
  loadProducts()
}

function resetFilter() {
  selectedCategoryId.value = null
  selectedColor.value = null
  priceRange.value = [0, 999_999_99]
  materials.value.forEach((m) => (m.checked = false))
  religions.value.forEach((r) => (r.checked = false))
  keyword.value = ''
  sortBy.value = 'newest'
  currentPage.value = 1
  loadProducts()
}

// ─── Tự động tải lại khi sortBy hoặc trang thay đổi ─────────
watch([sortBy, currentPage], () => loadProducts())

// ─── Khởi tạo ────────────────────────────────────────────────
onMounted(async () => {
  await loadFilterOptions()
  await loadProducts()
})

function searchProducts() {
  currentPage.value = 1
  loadProducts()
}
</script>

<style scoped src="../../assets/styles/TrangSanPham.css"></style>