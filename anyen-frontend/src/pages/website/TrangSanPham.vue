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
        <router-link to="/">Trang chủ</router-link>
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
        <button
            class="mobile-filter-btn"
            @click="showFilter = !showFilter"
        >
          <span>Bộ lọc sản phẩm</span>
          <i
              class="fa-solid"
              :class="showFilter ? 'fa-chevron-up' : 'fa-chevron-down'"
          ></i>
        </button>

        <!-- SIDEBAR -->
        <aside
            class="sp-sidebar mobile-filter-panel"
            :class="{ active: showFilter }"
        >
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
                </label>
              </div>
            </div>


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
              <!-- LOAD SAN PHAM -->
              <div v-for="item in products" :key="item.id" class="product-card" @click="goToProductDetail(item.id)">
                <div class="product-image">
                  <img
                      :src="getProductImage(item)"
                      :alt="item.name || 'Sản phẩm An Yên'"
                      loading="lazy"
                      @error="handleImageError"
                  />

                  <span
                      v-if="item.badge"
                      class="product-badge"
                      :class="item.badge.type"
                  >
    {{ item.badge.label }}
  </span>

                  <button
                      class="wishlist-btn"
                      :class="{ active: isInCart(item.id) }"
                      type="button"
                      :aria-label="
      isInCart(item.id)
        ? 'Xóa khỏi giỏ hàng'
        : 'Thêm vào giỏ hàng'
    "
                      @click.stop="toggleWish(item)"
                  >
                    <i class="fa-solid fa-cart-shopping"></i>
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
import { useRouter } from 'vue-router'
import heroSectionTrangSanPham from '../../assets/images/TrangSanPham/heroSection_TrangSanPham.png'
import flowerIcon from '../../assets/images/icon/flower_icon.png'
import { getProducts, getFilterOptions } from '../../services/productService.js'
import noImage from '../../assets/images/noimage.jpg'
import { ElMessage } from 'element-plus'
import { useCart } from '../../services/useCart.js'

const router = useRouter()
const { isInCart, toggleCart} = useCart()
const isPriceOpen = ref(true)
const isMaterialOpen = ref(true)
const isReligionOpen = ref(true)
const loading = ref(false)
const showFilter = ref(false)

const keyword = ref('')
const sortBy = ref('newest')
const currentPage = ref(1)
const pageSize = ref(16)
const priceRange = ref([0, 999_999_999])
const selectedCategoryId = ref(null)
const selectedColor = ref(null)

const products = ref([])
const totalProducts = ref(0)
const categories = ref([])
const materials = ref([])
const religions = ref([])
const colors = ref([])
const filtersReady = ref(false)

let reloadTimer = null
let ignoreAutoWatch = false
let settingPageForFilter = false

const trustItems = [
  { icon: 'fa-solid fa-medal', title: 'Sản phẩm chất lượng', desc: 'Được tuyển chọn kỹ lưỡng' },
  { icon: 'fa-solid fa-truck', title: 'Giao hàng toàn quốc', desc: 'Nhanh chóng, an toàn' },
  { icon: 'fa-solid fa-headset', title: 'Tư vấn tận tâm', desc: 'Hỗ trợ 24/7' },
  { icon: 'fa-solid fa-rotate-left', title: 'Đổi trả dễ dàng', desc: 'Trong 7 ngày' }
]
function toggleWish(product) {
  const added = toggleCart({
    ...product,
    image: getProductImage(product)
  })

  if (added) {
    ElMessage.success('Đã thêm sản phẩm vào giỏ hàng')
  } else {
    ElMessage.info('Đã xóa sản phẩm khỏi giỏ hàng')
  }
}
const formatPrice = (val) => {
  if (val === null || val === undefined) return 'Liên hệ'
  return Number(val).toLocaleString('vi-VN') + ' đ'
}

const DEFAULT_PRODUCT_IMAGE = noImage
const getProductImage = (product) => {
  if (!product) return DEFAULT_PRODUCT_IMAGE

  let image =
      product.image ||
      product.hinhAnh ||
      product.anhDaiDien ||
      product.urlAnh ||
      product.imageUrl ||
      product.hinhAnhUrl ||
      product.sanPhamAnh ||
      product.sanPhamAnhs ||
      product.danhSachAnh ||
      product.images ||
      null

  // Trường hợp backend trả mảng ảnh
  if (Array.isArray(image)) {
    image = image[0]
  }

  // Trường hợp mỗi ảnh là một object
  if (image && typeof image === 'object') {
    image =
        image.url ||
        image.Url ||
        image.imageUrl ||
        image.hinhAnh ||
        image.hinhAnhUrl ||
        image.duongDan ||
        null
  }

  if (typeof image !== 'string' || image.trim() === '') {
    return DEFAULT_PRODUCT_IMAGE
  }

  image = image.trim()

  if (
      image.startsWith('http://') ||
      image.startsWith('https://') ||
      image.startsWith('blob:') ||
      image.startsWith('data:')
  ) {
    return image
  }

  if (image.startsWith('/')) {
    return image
  }

  return `/images/${image}`
}
const handleImageError = (event) => {
  const img = event.currentTarget

  // Ngăn fallback lỗi chạy lặp gây chớp
  if (img.dataset.fallbackApplied === 'true') {
    return
  }

  img.dataset.fallbackApplied = 'true'
  img.src = DEFAULT_PRODUCT_IMAGE
}
function searchProducts() {
  queueLoad(true)
}
async function loadFilterOptions() {
  const options = await getFilterOptions()

  categories.value = options.categories
  materials.value = options.materials.map((m) => ({ ...m, checked: false }))
  religions.value = options.religions.map((r) => ({ ...r, checked: false }))
  colors.value = options.colors
}

//HAM LOAD SAN PHAM
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

//GOI TOI API LOAD SAN PHAM CUNG CAC TRUONG
    const { items, total } = await getProducts({
      keyword: keyword.value,
      loai: selectedCategory ? selectedCategory.name : '',
      vatLieu: selectedMaterials,
      tonGiao: selectedReligions,
      priceRange: priceRange.value,
      sortBy: sortBy.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })

    products.value = items
    totalProducts.value = total
  } catch (error) {
    console.error('Lỗi tải sản phẩm website:', error)
    products.value = []
    totalProducts.value = 0
  } finally {
    loading.value = false
  }
}
function queueLoad(resetPage = true) {
  if (!filtersReady.value || ignoreAutoWatch) return

  clearTimeout(reloadTimer)

  if (resetPage && currentPage.value !== 1) {
    settingPageForFilter = true
    currentPage.value = 1
  }

  reloadTimer = setTimeout(async () => {
    settingPageForFilter = false
    await loadProducts()
  }, 300)
}
function resetFilter() {
  ignoreAutoWatch = true

  selectedCategoryId.value = null
  selectedColor.value = null
  priceRange.value = [0, 999_999_999]
  materials.value.forEach((m) => (m.checked = false))
  religions.value.forEach((r) => (r.checked = false))
  keyword.value = ''
  sortBy.value = 'newest'
  currentPage.value = 1

  ignoreAutoWatch = false
  clearTimeout(reloadTimer)
  loadProducts()
}
function goToProductDetail(productId) {
  router.push(`/san-pham/${productId}`)
}

watch(keyword, () => queueLoad(true))
watch(sortBy, () => queueLoad(true))
watch(selectedCategoryId, () => queueLoad(true))
watch(selectedColor, () => queueLoad(true))
watch(priceRange, () => queueLoad(true), { deep: true })

watch(
    () => materials.value.map((m) => m.checked),
    () => queueLoad(true),
    { deep: true }
)

watch(
    () => religions.value.map((r) => r.checked),
    () => queueLoad(true),
    { deep: true }
)
watch(currentPage, () => {
  if (!filtersReady.value || ignoreAutoWatch || settingPageForFilter) return
  loadProducts()
})

//GOI HAM LOAD SAN PHAM KHI COMPOMENT HIEN LEN
onMounted(async () => {
  await loadFilterOptions()
  filtersReady.value = true
  await loadProducts()
})
</script>

<style scoped src="../../assets/styles/website/TrangSanPham.css"></style>