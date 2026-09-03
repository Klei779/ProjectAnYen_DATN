<template>
  <main class="service-page">
    <section
        class="service-hero"
        :style="{ backgroundImage: `url(${heroBanner})` }"
    >
      <div class="hero-overlay">
        <h1 class="hero-title">DỊCH VỤ MAI TÁNG AN YÊN</h1>

        <div class="hero-divider">
          <img :src="dividerIcon" alt="divider" class="divider-img" />
        </div>

        <p class="hero-desc">
          An Yên đồng hành cùng gia đình trong khoảnh khắc thiêng liêng,
          <br />
          mang đến sự an lành, tôn nghiêm và trọn vẹn nhất.
        </p>
      </div>
    </section>

    <section class="service-main">
      <div class="container page-layout">
        <div class="left-content">

          <!-- THÔNG TIN GÓI + ẢNH QUAN TÀI -->
          <div class="detail-layout">
            <div class="service-images">
              <div class="image-wrapper">
                <button class="image-nav prev" @click="prevImage">‹</button>

                <div class="main-image-frame">
                  <img
                      :src="mainImage"
                      class="main-image"
                      alt="Ảnh dịch vụ"
                      @error="setFakeImage"
                  />
                </div>

                <button class="image-nav next" @click="nextImage">›</button>
              </div>

              <div class="thumb-list">
                <div
                    v-for="(img, index) in images"
                    :key="index"
                    class="thumb-frame"
                    :class="{ active: currentIndex === index }"
                    @click="changeImage(index)"
                >
                  <img
                      :src="img"
                      alt="Ảnh dịch vụ"
                      @error="setFakeImage"
                  />
                </div>
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
                  {{ formatPrice(service.gia) }} đ
                </strong>
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

          <!-- CHI TIẾT DỊCH VỤ DẠNG HÌNH ẢNH BAO QUÁT -->
          <div v-if="serviceOverviewImage" class="service-overview-section">
            <div class="service-overview-image-frame">
              <img
                  :src="serviceOverviewImage"
                  :alt="service.tenCombo || 'Chi tiết gói dịch vụ'"
                  class="service-overview-image"
                  loading="lazy"
                  @error="handleOverviewImageError"
              />
            </div>
          </div>

          <!-- DANH SÁCH SẢN PHẨM TRONG COMBO -->
          <div v-if="comboChiTiet.length" class="combo-products-section">
            <h2>SẢN PHẨM TRONG GÓI DỊCH VỤ</h2>
            <p class="section-desc">
              Các sản phẩm và dịch vụ đã được tuyển chọn trong gói này.
            </p>

            <div
                v-for="group in groupedByLoai"
                :key="group.label"
                class="combo-product-group"
            >
              <h3 class="combo-product-group-title">
                <i :class="getGroupIcon(group.label)"></i>
                {{ group.label }}
              </h3>

              <div class="combo-product-grid">
                <div
                    v-for="item in group.items"
                    :key="item.comboChiTietId"
                    class="combo-product-card"
                >
                  <div class="combo-product-img-frame">
                    <img
                        :src="getFirstImage(item)"
                        :alt="item.noiDung || 'Sản phẩm'"
                        @error="setFakeImage"
                    />
                  </div>

                  <div class="combo-product-info">
                    <p class="combo-product-name">{{ item.noiDung || '—' }}</p>
                    <span class="combo-product-qty">
                      Số lượng: <b>{{ item.soLuong }}</b>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- QUY TRÌNH -->
          <div class="process-card">
            <h2>QUY TRÌNH THỰC HIỆN</h2>

            <p class="section-desc">
              An Yên đồng hành cùng gia đình trong từng bước, đảm bảo mọi việc được diễn ra chu đáo và trang trọng.
            </p>

            <div v-if="processImages.length" class="process-image-gallery">
              <figure
                  v-for="(image, index) in processImages"
                  :key="`${image}-${index}`"
                  class="process-image-card"
              >
                <img
                    :src="image"
                    :alt="`Quy trình thực hiện bước ${index + 1}`"
                    loading="lazy"
                    @error="setFakeImage"
                />
                <figcaption>Hình ảnh quy trình {{ index + 1 }}</figcaption>
              </figure>
            </div>

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

          <!-- LƯU Ý -->
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

import heroBanner from "../../assets/images/TrangSanPham/heroSection_TrangSanPham.png"
import dividerIcon from "../../assets/images/icon/flower_icon.png"

const route = useRoute()

const makeFakeImage = () => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="800" height="500">
      <rect width="100%" height="100%" fill="#f4eee8"/>
      <rect x="20" y="20" width="760" height="460" fill="none" stroke="#cdb9a7" stroke-width="4" stroke-dasharray="14 10"/>
      <text x="50%" y="46%" text-anchor="middle" font-size="34" fill="#8a6a52" font-family="Arial">
        Đang cập nhật hình ảnh
      </text>
      <text x="50%" y="56%" text-anchor="middle" font-size="22" fill="#aa8b70" font-family="Arial">
        An Yên
      </text>
    </svg>
  `

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const FAKE_IMAGE = makeFakeImage()

const service = ref({
  tenCombo: '',
  gia: 0,
  moTa: ''
})

const comboChiTiet = ref([])
const comboImages = ref([])
const processImages = ref([])
const images = ref([FAKE_IMAGE, FAKE_IMAGE, FAKE_IMAGE])
const currentIndex = ref(0)

const mainImage = computed(() => {
  return images.value[currentIndex.value] || FAKE_IMAGE
})

const formatPrice = (value) => {
  return Number(value || 0).toLocaleString('vi-VN')
}

const normalizeImagePath = (path) => {
  if (!path || typeof path !== 'string') {
    return FAKE_IMAGE
  }

  const cleanPath = path.trim()

  if (!cleanPath) {
    return FAKE_IMAGE
  }

  // URL Cloudinary đầy đủ
  if (
      cleanPath.startsWith('http://') ||
      cleanPath.startsWith('https://') ||
      cleanPath.startsWith('data:image')
  ) {
    return cleanPath
  }

  // Trường hợp Cloudinary trả về //res.cloudinary.com/...
  if (cleanPath.startsWith('//')) {
    return `https:${cleanPath}`
  }

  // Ảnh upload cục bộ được backend phục vụ.
  if (cleanPath.startsWith('/uploads/')) {
    return `http://localhost:8080${cleanPath}`
  }
  if (cleanPath.startsWith('uploads/')) {
    return `http://localhost:8080/${cleanPath}`
  }

  // Ảnh tĩnh trong public của frontend.
  if (cleanPath.startsWith('/')) {
    return cleanPath
  }

  return `/images/TrangDichVuChiTiet/${cleanPath}`
}

const extractProcessImages = (data) => {
  if (!Array.isArray(data?.hinhAnhQuyTrinhs)) return []

  return [
    ...new Set(
        data.hinhAnhQuyTrinhs
            .map(item => normalizeImagePath(
                typeof item === 'string'
                    ? item
                    : item?.hinhAnh || item?.url
            ))
            .filter(image => image && image !== FAKE_IMAGE)
    )
  ]
}

const extractComboImages = (data) => {
  const rawImages = []

  // Trường hợp backend trả danh sách hinhAnhs
  if (Array.isArray(data?.hinhAnhs)) {
    rawImages.push(...data.hinhAnhs)
  }

  // Trường hợp backend trả images
  if (Array.isArray(data?.images)) {
    rawImages.push(...data.images)
  }

  // Trường hợp backend chỉ trả một ảnh hinhAnh
  if (data?.hinhAnh) {
    rawImages.push(data.hinhAnh)
  }

  return [
    ...new Set(
        rawImages
            .map(item => {
              if (typeof item === 'string') {
                return normalizeImagePath(item)
              }

              return normalizeImagePath(
                  item?.hinhAnh
                  || item?.url
                  || item?.secureUrl
                  || item?.secure_url
              )
            })
            .filter(image => image && image !== FAKE_IMAGE)
    )
  ]
}

const setFakeImage = (event) => {
  event.target.onerror = null
  event.target.src = FAKE_IMAGE
}

const changeImage = (index) => {
  currentIndex.value = index
}
const nextImage = () => {
  if (images.value.length === 0) return

  currentIndex.value =
      (currentIndex.value + 1) % images.value.length
}

const prevImage = () => {
  if (images.value.length === 0) return

  currentIndex.value =
      (currentIndex.value - 1 + images.value.length) % images.value.length
}

const isQuanTaiItem = (item) => {
  const text = (item.noiDung || '').toLowerCase()

  return text.includes('quan tài') || text.includes('áo quan')
}

const isTrangTriItem = (item) => {
  const text = (item.noiDung || '').toLowerCase()

  return text.includes('trang trí')
      || text.includes('sảnh')
      || text.includes('bàn ghế')
      || text.includes('phông rạp')
}

const trangTriItems = computed(() => {
  return comboChiTiet.value.filter(item =>
      isTrangTriItem(item)
  )
})

const normalDetailItems = computed(() => {
  return comboChiTiet.value.filter(item =>
      !isTrangTriItem(item) && !isQuanTaiItem(item)
  )
})

const groupedByLoai = computed(() => {
  const groups = {}

  comboChiTiet.value.forEach(item => {
    // Ưu tiên tên loại thực từ SanPham (ví dụ: "Quan tài", "Vòng hoa")
    const groupKey = (item.tenLoaiSanPham && item.tenLoaiSanPham.trim())
        ? item.tenLoaiSanPham.trim()
        : 'Tiện ích / Dịch vụ'

    if (!groups[groupKey]) {
      groups[groupKey] = {
        label: groupKey,
        loai: item.loai,
        items: []
      }
    }
    groups[groupKey].items.push(item)
  })

  // Sắp xếp theo tên loại (alphabetical, tiếng Việt)
  return Object.values(groups).sort((a, b) =>
      a.label.localeCompare(b.label, 'vi')
  )
})

const getFirstImage = (item) => {
  // Ưu tiên 1: ảnh riêng của combo chi tiết (bảng combochitiet_hinhanh) – đã normalize khi load
  const chiTietImg = item.hinhAnhs?.[0]?.hinhAnh
  if (chiTietImg && chiTietImg !== FAKE_IMAGE) {
    return chiTietImg
  }

  // Ưu tiên 2: ảnh đại diện của sản phẩm lấy từ SanPham.hinhAnh – đã normalize khi load
  const sanPhamImg = item.hinhAnhSanPham
  if (sanPhamImg && sanPhamImg !== FAKE_IMAGE) {
    return sanPhamImg
  }

  return FAKE_IMAGE
}

const getImageName = (item) => {
  return item.hinhAnhs?.[0]?.tenHinhAnh
      || 'Hạng mục được chuẩn bị theo tiêu chuẩn của gói dịch vụ.'
}

const getTrangTriImages = (item) => {
  const list = item.hinhAnhs || []

  const result = list.slice(0, 3).map(img => ({
    ...img,
    hinhAnh: normalizeImagePath(img.hinhAnh)
  }))

  while (result.length < 3) {
    result.push({
      maHinhAnh: `fake-${result.length}`,
      tenHinhAnh: 'Đang cập nhật hình ảnh',
      hinhAnh: FAKE_IMAGE
    })
  }

  return result
}

const getLoaiText = (loai) => {
  if (loai === 1) return 'Sản phẩm'
  if (loai === 0) return 'Tiện ích / dịch vụ'
  return 'Không xác định'
}

const getGroupIcon = (label) => {
  const l = (label || '').toLowerCase()
  if (l.includes('quan tài') || l.includes('áo quan') || l.includes('hòm')) {
    return 'fa-solid fa-box'
  }
  if (l.includes('vòng hoa') || l.includes('hoa')) {
    return 'fa-solid fa-leaf'
  }
  if (l.includes('đèn') || l.includes('nến')) {
    return 'fa-solid fa-fire'
  }
  if (l.includes('trang trí') || l.includes('sảnh') || l.includes('phông')) {
    return 'fa-solid fa-star'
  }
  if (l.includes('tiện ích') || l.includes('dịch vụ')) {
    return 'fa-solid fa-spa'
  }
  return 'fa-solid fa-tag'
}


const resetData = () => {
  service.value = {
    tenCombo: '',
    gia: 0,
    moTa: '',
    hinhAnh: ''
  }

  comboChiTiet.value = []
  comboImages.value = []
  processImages.value = []
  images.value = [FAKE_IMAGE, FAKE_IMAGE, FAKE_IMAGE]
  currentIndex.value = 0
}

const loadCombo = async () => {
  try {
    const id = route.params.id

    const res = await axios.get(
        `http://localhost:8080/api/dich-vu/${id}`
    )

    console.log('Dữ liệu combo chi tiết:', res.data)

    service.value = {
      tenCombo: res.data.tenCombo || '',
      gia: res.data.gia || 0,
      moTa: res.data.moTa || '',
      hinhAnh: res.data.hinhAnh || '',
      hinhAnhChiTiet: res.data.hinhAnhChiTiet
          ? normalizeImagePath(res.data.hinhAnhChiTiet)
          : null
    }

    const loadedComboImages =
        extractComboImages(res.data)

    comboImages.value = loadedComboImages
    processImages.value = extractProcessImages(res.data)

    // Ưu tiên ảnh được upload lúc tạo combo
    if (loadedComboImages.length > 0) {
      images.value = loadedComboImages
      currentIndex.value = 0
    }

  } catch (e) {
    console.log(
        'Không lấy được dữ liệu gói dịch vụ',
        e
    )
  }
}

const loadComboChiTiet = async () => {
  try {
    const id = route.params.id

    const res = await axios.get(
        `http://localhost:8080/api/dich-vu/${id}/chitiet`
    )

    comboChiTiet.value = res.data.map(item => ({
      ...item,
      hinhAnhs: (item.hinhAnhs || []).map(img => ({
        ...img,
        hinhAnh: normalizeImagePath(img.hinhAnh)
      })),
      // Normalize ảnh sản phẩm ngay khi load để dùng trực tiếp không cần normalize lại
      hinhAnhSanPham: item.hinhAnhSanPham
          ? normalizeImagePath(item.hinhAnhSanPham)
          : null
    }))

    const quanTaiItem = comboChiTiet.value.find(item =>
        isQuanTaiItem(item)
    )

    const quanTaiImages = quanTaiItem?.hinhAnhs
            ?.map(img => img.hinhAnh)
            ?.filter(image => image && image !== FAKE_IMAGE)
        || []

// Ưu tiên ảnh combo đã upload.
    const galleryImages =
        comboImages.value.length > 0
            ? comboImages.value
            : quanTaiImages

    images.value =
        galleryImages.length > 0
            ? galleryImages
            : [FAKE_IMAGE, FAKE_IMAGE, FAKE_IMAGE]

    currentIndex.value = 0

  } catch (e) {
    console.log('Không lấy được chi tiết dịch vụ', e)
    images.value = [FAKE_IMAGE, FAKE_IMAGE, FAKE_IMAGE]
  }
}

const removeVietnameseTones = (str) => {
  if (!str) return ''
  str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, 'a')
  str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, 'e')
  str = str.replace(/ì|í|ị|ỉ|ĩ/g, 'i')
  str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, 'o')
  str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, 'u')
  str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, 'y')
  str = str.replace(/đ/g, 'd')
  return str.toLowerCase().trim()
}

const serviceOverviewImage = computed(() => {
  // 1. Ưu tiên ảnh do Admin tải lên qua khung ảnh chi tiết
  if (service.value?.hinhAnhChiTiet) {
    return service.value.hinhAnhChiTiet
  }

  // 2. Dữ liệu fallback nếu là gói mẫu cũ (1-4) chưa upload ảnh riêng
  const id = String(route.params.id || '').trim()
  const legacyIdMap = {
    '1': '/images/TrangDichVuChiTiet/goi-an-lac.png',
    '2': '/images/TrangDichVuChiTiet/goi-an-tam.png',
    '3': '/images/TrangDichVuChiTiet/goi-truyen-thong.png',
    '4': '/images/TrangDichVuChiTiet/goi-cao-cap.png'
  }

  if (legacyIdMap[id]) {
    return legacyIdMap[id]
  }

  return null
})

const handleOverviewImageError = (event) => {
  event.target.onerror = null
  event.target.src = FAKE_IMAGE
}

const loadPage = async () => {
  window.scrollTo(0, 0)
  resetData()

  await loadCombo()
  await loadComboChiTiet()
}

onMounted(() => {
  loadPage()
})

watch(
    () => route.params.id,
    () => {
      loadPage()
    }
)
</script>

<style scoped src="../../assets/styles/website/TrangDichVuChiTiet.css"></style>

<style scoped>
/* ============================================================
   KHỐI HÌNH ẢNH TỔNG QUAN CHI TIẾT DỊCH VỤ
============================================================ */
.service-overview-section {
  width: 100%;
  margin: 32px 0 40px;
}

.service-overview-image-frame {
  width: 100%;
  border-radius: 18px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #ede3d8;
  box-shadow: 0 10px 36px rgba(20, 45, 77, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.service-overview-image-frame:hover {
  box-shadow: 0 14px 44px rgba(20, 45, 77, 0.12);
}

.service-overview-image {
  width: 100%;
  height: auto;
  display: block;
  object-fit: contain;
}

.process-image-gallery {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 24px 0 30px;
}

.process-image-card {
  margin: 0;
  overflow: hidden;
  border: 1px solid #eadfd4;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(69, 45, 30, 0.08);
}

.process-image-card img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.process-image-card figcaption {
  padding: 10px 12px;
  color: #765b47;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

@media (max-width: 900px) {
  .process-image-gallery {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .process-image-gallery {
    grid-template-columns: 1fr;
  }
}

.combo-quantity-badge {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  padding: 3px 7px;
  border-radius: 999px;
  background: #fff1f2;
  color: #9f1239;
  font-size: 12px;
  font-weight: 800;
}

/* ============================================================
   DANH SÁCH SẢN PHẨM TRONG COMBO
============================================================ */
.combo-products-section {
  background: #fffdfb;
  border: 1px solid #eadfd7;
  border-radius: 8px;
  padding: 26px;
  margin-bottom: 24px;
}

.combo-products-section > h2 {
  text-align: center;
  font-family: 'Faustina', serif;
  font-size: 26px;
  color: #142d4d;
  margin: 0 0 8px;
}

.combo-product-group {
  margin-top: 24px;
}

.combo-product-group-title {
  display: flex;
  align-items: center;
  gap: 9px;
  font-family: 'Faustina', serif;
  font-size: 19px;
  color: #8b1024;
  font-weight: 700;
  margin: 0 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0e3d9;
}

.combo-product-group-title i {
  font-size: 16px;
  color: #8b1024;
}

.combo-product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.combo-product-card {
  background: #fffaf5;
  border: 1px solid #e8ddd4;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(20, 45, 77, 0.07);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.combo-product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 22px rgba(20, 45, 77, 0.12);
}

.combo-product-img-frame {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #f4eee8;
  overflow: hidden;
}

.combo-product-img-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.combo-product-info {
  padding: 10px 12px 12px;
}

.combo-product-name {
  font-size: 13px;
  font-weight: 600;
  color: #142d4d;
  margin: 0 0 6px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.combo-product-qty {
  font-size: 12px;
  color: #6b5a4e;
}

.combo-product-qty b {
  color: #8b1024;
  font-weight: 700;
}

@media (max-width: 900px) {
  .combo-product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .combo-product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 400px) {
  .combo-product-grid {
    grid-template-columns: 1fr;
  }
}
</style>