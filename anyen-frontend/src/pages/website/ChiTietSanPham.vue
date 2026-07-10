<template>
  <div v-if="loading" class="loading-container">
    <i class="fa-solid fa-spinner fa-spin"></i>
    <span>Đang tải...</span>
  </div>

  <div v-else-if="product" class="product-detail-container">
    <!-- BREADCRUMB -->
    <div class="breadcrumb-bar">
      <div class="sp-container">
        <nav class="breadcrumb-nav">
          <a href="/anyen-frontend/public">Trang chủ</a>
          <span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span>
          <a href="/anyen-frontend/public/san-pham">Sản phẩm</a>
          <span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span>
          <span class="bc-active">{{ product.name }}</span>
        </nav>
      </div>
    </div>

    <!-- PRODUCT DETAIL SECTION -->
    <section class="product-detail-section">
      <div class="sp-container">
        <div class="product-detail-layout">
          
          <!-- PRODUCT IMAGES -->
          <div class="product-gallery">
            <div class="main-image">
              <img :src="getProductImage(product.image)" :alt="product.name" />
            </div>
          </div>

          <!-- PRODUCT INFO -->
          <div class="product-info-panel">
            <h1 class="product-title">{{ product.name }}</h1>
            
            <div class="product-code">
              <span>Mã sản phẩm: </span>
              <strong>{{ product.code || 'SP00000' }}</strong>
            </div>

            <div class="product-price-section">
              <div class="current-price">{{ formatPrice(product.price) }}</div>
              <div v-if="product.oldPrice" class="old-price">{{ formatPrice(product.oldPrice) }}</div>
              <div v-if="product.discount" class="discount-badge">
                - {{ formatPrice(product.discount) }}
              </div>
            </div>

            <div class="product-meta">
              <div class="meta-row">
                <span class="meta-label">Loại sản phẩm:</span>
                <span class="meta-value">{{ product.loai || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Chất liệu:</span>
                <span class="meta-value">{{ product.vatLieu || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Quy cách:</span>
                <span class="meta-value">{{ product.quyCach || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Tôn giáo:</span>
                <span class="meta-value">{{ product.tonGiao || 'Không phân biệt' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Màu sắc:</span>
                <span class="meta-value">{{ product.mauSac || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Kích thước:</span>
                <span class="meta-value">{{ product.kichThuoc || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Trọng lượng:</span>
                <span class="meta-value">{{ product.trongLuong || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Nhà cung cấp:</span>
                <span class="meta-value">{{ product.nhaCungCap || 'N/A' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Xuất xứ:</span>
                <span class="meta-value">{{ product.xuatXu || 'Việt Nam' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Số lượng:</span>
                <span class="meta-value">{{ product.soLuong || 0 }} sản phẩm</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Trạng thái:</span>
                <span class="meta-value status" :class="{ 'in-stock': product.trangThai === 'Đang bán' }">
                  {{ product.trangThai || 'N/A' }}
                </span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Cập nhật:</span>
                <span class="meta-value">{{ product.ngayCapNhat || 'N/A' }}</span>
              </div>
            </div>

            <div class="contact-form">
              <div class="phone-input-group">
                <input 
                  v-model="phoneNumber" 
                  type="tel" 
                  class="phone-input" 
                  placeholder="Nhập số điện thoại của bạn"
                />
                <button class="btn-contact">
                  <i class="fa-solid fa-phone"></i>
                  Liên hệ tư vấn
                </button>
              </div>
              <button 
                class="btn-favorite" 
                :class="{ active: isWished }"
                @click="toggleWish"
              >
                <i :class="isWished ? 'fa-solid fa-heart' : 'fa-regular fa-heart'"></i>
                Yêu thích
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- PRODUCT TABS -->
    <section class="product-tabs-section">
      <div class="sp-container">
        <div class="tabs-container">
          <div class="tabs-header">
            <button 
              v-for="tab in tabs" 
              :key="tab.id"
              class="tab-btn"
              :class="{ active: activeTab === tab.id }"
              @click="activeTab = tab.id"
            >
              {{ tab.label }}
            </button>
          </div>
          
          <div class="tabs-content">
            <div v-if="activeTab === 'description'" class="tab-panel">
              <div class="product-description">
                <p>{{ product.moTa || 'Chưa có mô tả sản phẩm' }}</p>
              </div>
            </div>
            
            <div v-if="activeTab === 'specs'" class="tab-panel">
              <div class="specs-table">
                <div class="spec-row">
                  <span class="spec-label">Mã sản phẩm</span>
                  <span class="spec-value">{{ product.code || 'SP00000' }}</span>
                </div>
                <div class="spec-row">
                  <span class="spec-label">Loại sản phẩm</span>
                  <span class="spec-value">{{ product.loai || 'N/A' }}</span>
                </div>
                <div class="spec-row">
                  <span class="spec-label">Chất liệu</span>
                  <span class="spec-value">{{ product.vatLieu || 'N/A' }}</span>
                </div>
                <div class="spec-row">
                  <span class="spec-label">Quy cách</span>
                  <span class="spec-value">{{ product.quyCach || 'N/A' }}</span>
                </div>
                <div class="spec-row">
                  <span class="spec-label">Kích thước</span>
                  <span class="spec-value">{{ product.kichThuoc || 'N/A' }}</span>
                </div>
                <div class="spec-row">
                  <span class="spec-label">Trọng lượng</span>
                  <span class="spec-value">{{ product.trongLuong || 'N/A' }}</span>
                </div>
                <div class="spec-row">
                  <span class="spec-label">Xuất xứ</span>
                  <span class="spec-value">{{ product.xuatXu || 'Việt Nam' }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="activeTab === 'partner'" class="tab-panel">
              <div class="partner-info">
                <div class="partner-row">
                  <span class="partner-label">Nhà cung cấp:</span>
                  <span class="partner-value">{{ product.nhaCungCap || 'N/A' }}</span>
                </div>
                <div class="partner-row">
                  <span class="partner-label">Nhà sản xuất:</span>
                  <span class="partner-value">{{ product.nhaSanXuat || 'N/A' }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="activeTab === 'storage'" class="tab-panel">
              <div class="storage-info">
                <p>{{ product.huongDanBaoQuan || 'Sản phẩm nên được bảo quản ở nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp và độ ẩm cao.' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- RELATED PRODUCTS -->
    <section class=" related-products-section">
      <div class="sp-container">
        <h2 class="section-title">Sản phẩm liên quan</h2>
        <div class="related-products-grid">
          <div 
            v-for="item in relatedProducts" 
            :key="item.id" 
            class="related-product-card"
            @click="goToProduct(item.id)"
          >
            <div class="related-product-image">
              <img :src="getProductImage(item.image)" :alt="item.name" />
            </div>
            <div class="related-product-info">
              <h4 class="related-product-name">{{ item.name }}</h4>
              <p class="related-product-price">{{ formatPrice(item.price) }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>

  <div v-else class="error-container">
    <i class="fa-solid fa-exclamation-circle"></i>
    <p>Không tìm thấy sản phẩm</p>
    <button class="btn-back" @click="$router.push('/san-pham')">Quay lại danh sách sản phẩm</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById, getProducts } from '../../services/productService.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const product = ref(null)
const relatedProducts = ref([])
const isWished = ref(false)
const phoneNumber = ref('')

const tabs = [
  { id: 'description', label: 'MÔ TẢ SẢN PHẨM' },
  { id: 'specs', label: 'THÔNG SỐ KỸ THUẬT' },
  { id: 'partner', label: 'THÔNG TIN ĐỐI TÁC' },
  { id: 'storage', label: 'HƯỚNG DẪN BẢO QUẢN' }
]
const activeTab = ref('description')

const formatPrice = (val) => {
  if (val === null || val === undefined) return 'Liên hệ'
  return Number(val).toLocaleString('vi-VN') + ' ₫'
}

const getProductImage = (image) => {
  if (!image) return '/no-image.png'
  if (image.startsWith('http') || image.startsWith('/') || image.startsWith('blob:')) return image
  return `/images/${image}`
}

const toggleWish = () => {
  isWished.value = !isWished.value
}

const goToProduct = (id) => {
  router.push(`/san-pham/${id}`)
}

async function loadProduct() {
  loading.value = true
  try {
    const productId = route.params.id
    product.value = await getProductById(productId)
    
    // Load related products (same category)
    if (product.value && product.value.loai) {
      const { items } = await getProducts({
        loai: product.value.loai,
        pageSize: 4
      })
      relatedProducts.value = items.filter(item => item.id !== product.value.id).slice(0, 4)
    }
  } catch (error) {
    console.error('Lỗi tải sản phẩm:', error)
    product.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
/* Color Variables */
:root {
  --primary-gold: #d4a017;
  --primary-gold-dark: #b8860b;
  --primary-gold-light: #f5d77a;
  --text-dark: #1a1a1a;
  --text-gray: #666666;
  --text-light: #999999;
  --bg-light: #f9f9f9;
  --bg-white: #ffffff;
  --border-color: #e8e8e8;
  --success-green: #4caf50;
  --error-red: #ff4444;
}

/* Container */
.sp-container {
  max-width: 1320px;
  margin: 0 auto;
  padding: 0 20px;
  width: 100%;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  gap: 20px;
  font-size: 16px;
  color: var(--text-gray);
  font-family: 'Noto Sans', sans-serif;
}

.loading-container i {
  font-size: 40px;
  color: var(--primary-gold);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Error State */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  gap: 20px;
  text-align: center;
  padding: 40px 20px;
}

.error-container i {
  font-size: 64px;
  color: var(--text-light);
}

.error-container p {
  font-size: 18px;
  color: var(--text-gray);
  margin: 0;
}

.btn-back {
  padding: 14px 32px;
  background: var(--primary-gold);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  font-family: 'Noto Sans', sans-serif;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: var(--primary-gold-dark);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(212, 160, 23, 0.3);
}

/* Breadcrumb */
.breadcrumb-bar {
  background: var(--bg-light);
  padding: 18px 0;
  border-bottom: 1px solid var(--border-color);
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-family: 'Noto Sans', sans-serif;
}

.breadcrumb-nav a {
  color: var(--text-gray);
  text-decoration: none;
  transition: color 0.2s ease;
}

.breadcrumb-nav a:hover {
  color: var(--primary-gold);
}

.bc-sep {
  color: var(--text-light);
  font-size: 11px;
}

.bc-active {
  color: var(--text-dark);
  font-weight: 600;
}

/* Product Detail Section */
.product-detail-section {
  padding: 50px 0;
  background: var(--bg-white);
}

.product-detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 50px;
  align-items: start;
}

/* Product Gallery */
.product-gallery {
  position: sticky;
  top: 100px;
}

.main-image {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-light);
  aspect-ratio: 4/3;
  max-height: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.main-image:hover {
  transform: scale(1.02);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Product Info Panel */
.product-info-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-dark);
  margin: 0;
  line-height: 1.3;
  font-family: 'Faustina', serif;
}

.product-code {
  font-size: 13px;
  color: var(--text-gray);
  font-family: 'Noto Sans', sans-serif;
}

.product-code strong {
  color: var(--text-dark);
  font-weight: 600;
}

/* Price Section */
.product-price-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-top: 2px solid var(--border-color);
  border-bottom: 2px solid var(--border-color);
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-gold);
  font-family: 'Noto Sans', sans-serif;
}

.old-price {
  font-size: 15px;
  color: var(--text-light);
  text-decoration: line-through;
  font-weight: 500;
}

.discount-badge {
  padding: 4px 10px;
  background: var(--error-red);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  font-family: 'Noto Sans', sans-serif;
}

/* Product Meta */
.product-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  background: var(--bg-light);
  padding: 16px;
  border-radius: 12px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  font-family: 'Noto Sans', sans-serif;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-color);
}

.meta-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.meta-label {
  color: var(--text-gray);
  font-weight: 500;
}

.meta-value {
  color: var(--text-dark);
  font-weight: 600;
  text-align: right;
}

.meta-value.status {
  color: var(--success-green);
}

.meta-value.status.in-stock {
  color: var(--success-green);
}

/* Contact Form */
.contact-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.phone-input-group {
  display: flex;
  gap: 10px;
}

.phone-input {
  flex: 1;
  padding: 14px 16px;
  border: 2px solid var(--border-color);
  border-radius: 10px;
  font-size: 14px;
  font-family: 'Noto Sans', sans-serif;
  outline: none;
  transition: border-color 0.3s ease;
}

.phone-input:focus {
  border-color: var(--primary-gold);
}

.phone-input::placeholder {
  color: var(--text-light);
}

/* Action Buttons */
.btn-contact {
  padding: 14px 24px;
  background: linear-gradient(135deg, var(--primary-gold) 0%, var(--primary-gold-dark) 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: 'Noto Sans', sans-serif;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(212, 160, 23, 0.3);
  white-space: nowrap;
}

.btn-contact:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(212, 160, 23, 0.4);
}

.btn-contact:active {
  transform: translateY(-1px);
}

.btn-favorite {
  padding: 14px 24px;
  background: white;
  color: var(--text-dark);
  border: 2px solid var(--border-color);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: 'Noto Sans', sans-serif;
  transition: all 0.3s ease;
}

.btn-favorite:hover {
  border-color: var(--primary-gold);
  color: var(--primary-gold);
  background: var(--bg-light);
}

.btn-favorite.active {
  background: #fff5f5;
  border-color: var(--error-red);
  color: var(--error-red);
}

/* Tabs Section */
.product-tabs-section {
  padding: 50px 0;
  background: var(--bg-light);
}

.tabs-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.tabs-header {
  display: flex;
  border-bottom: 2px solid var(--border-color);
  background: white;
}

.tab-btn {
  flex: 1;
  padding: 18px 24px;
  background: white;
  border: none;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-gray);
  cursor: pointer;
  border-bottom: 3px solid transparent;
  font-family: 'Noto Sans', sans-serif;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tab-btn:hover {
  color: var(--primary-gold);
  background: var(--bg-light);
}

.tab-btn.active {
  color: var(--primary-gold);
  border-bottom-color: var(--primary-gold);
  background: white;
}

.tabs-content {
  padding: 40px;
  min-height: 250px;
}

.tab-panel {
  line-height: 1.8;
}

.product-description {
  color: var(--text-dark);
  font-size: 15px;
  font-family: 'Noto Sans', sans-serif;
}

.specs-table {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.spec-row {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  font-family: 'Noto Sans', sans-serif;
}

.spec-row:last-child {
  border-bottom: none;
}

.spec-label {
  color: var(--text-gray);
  font-weight: 600;
  font-size: 14px;
}

.spec-value {
  color: var(--text-dark);
  font-weight: 500;
  font-size: 14px;
}

.partner-info {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.partner-row {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  font-family: 'Noto Sans', sans-serif;
}

.partner-row:last-child {
  border-bottom: none;
}

.partner-label {
  color: var(--text-gray);
  font-weight: 600;
  font-size: 14px;
}

.partner-value {
  color: var(--text-dark);
  font-weight: 500;
  font-size: 14px;
}

.storage-info {
  color: var(--text-dark);
  font-size: 15px;
  font-family: 'Noto Sans', sans-serif;
  line-height: 1.8;
}

/* Related Products */
.related-products-section {
  padding: 50px 0;
  background: var(--bg-white);
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 32px;
  font-family: 'Faustina', serif;
  text-align: center;
}

.related-products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.related-product-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  border: 1px solid var(--border-color);
}

.related-product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: var(--primary-gold);
}

.related-product-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--bg-light);
}

.related-product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.related-product-card:hover .related-product-image img {
  transform: scale(1.1);
}

.related-product-info {
  padding: 16px;
  text-align: center;
}

.related-product-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-dark);
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Noto Sans', sans-serif;
  line-height: 1.4;
  min-height: 20px;
}

.related-product-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-gold);
  margin: 0;
  font-family: 'Noto Sans', sans-serif;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .product-detail-layout {
    gap: 30px;
  }
  
  .related-products-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
  
  .product-title {
    font-size: 22px;
  }
  
  .current-price {
    font-size: 26px;
  }
  
  .product-meta {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .sp-container {
    padding: 0 16px;
  }
  
  .product-detail-layout {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .product-gallery {
    position: static;
  }
  
  .main-image {
    max-height: 350px;
  }
  
  .product-detail-section {
    padding: 30px 0;
  }
  
  .product-tabs-section {
    padding: 30px 0;
  }
  
  .related-products-section {
    padding: 30px 0;
  }
  
  .product-title {
    font-size: 20px;
  }
  
  .current-price {
    font-size: 24px;
  }
  
  .old-price {
    font-size: 14px;
  }
  
  .product-price-section {
    padding: 14px 0;
    gap: 12px;
  }
  
  .product-meta {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .phone-input-group {
    flex-direction: column;
  }
  
  .btn-contact,
  .btn-favorite {
    width: 100%;
    padding: 14px 20px;
  }
  
  .tabs-header {
    flex-wrap: wrap;
  }
  
  .tab-btn {
    flex: 1 1 50%;
    padding: 14px 16px;
    font-size: 13px;
  }
  
  .tabs-content {
    padding: 24px;
  }
  
  .related-products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .section-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
  
  .breadcrumb-bar {
    padding: 12px 0;
  }
  
  .breadcrumb-nav {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .product-title {
    font-size: 18px;
  }
  
  .current-price {
    font-size: 22px;
  }
  
  .old-price {
    font-size: 13px;
  }
  
  .product-meta {
    padding: 14px;
  }
  
  .meta-row {
    font-size: 12px;
  }
  
  .tab-btn {
    flex: 1 1 100%;
  }
  
  .related-products-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .tabs-content {
    padding: 20px;
  }
  
  .section-title {
    font-size: 22px;
  }
  
  .phone-input {
    font-size: 13px;
    padding: 12px 14px;
  }
  
  .btn-contact,
  .btn-favorite {
    font-size: 13px;
    padding: 12px 16px;
  }
}
</style>
