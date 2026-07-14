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



          <!-- CHI TIẾT DỊCH VỤ -->

          <div class="content-card">

            <h2>CHI TIẾT DỊCH VỤ</h2>



            <p class="section-desc">

              {{ service.tenCombo }} bao gồm đầy đủ các hạng mục cần thiết cho một lễ tang trang trọng.

            </p>



            <!-- TRANG TRÍ: 3 HÌNH NGANG -->

            <div

                v-for="item in trangTriItems"

                :key="item.comboChiTietId"

                class="trang-tri-section"

            >

              <h3>{{ item.noiDung }}</h3>



              <p>

                Các mẫu trang trí sảnh tang được chuẩn bị trang nghiêm, phù hợp với từng gói dịch vụ.

              </p>



              <div class="trang-tri-images">

                <div

                    v-for="(img, index) in getTrangTriImages(item)"

                    :key="img.maHinhAnh || index"

                    class="trang-tri-image-frame"

                >

                  <img

                      :src="img.hinhAnh"

                      :alt="img.tenHinhAnh"

                      @error="setFakeImage"

                  />

                </div>

              </div>

            </div>



            <!-- CÁC MỤC CÒN LẠI -->

            <div class="service-item-grid">

              <div

                  v-for="item in normalDetailItems"

                  :key="item.comboChiTietId"

                  class="service-item-card"

              >

                <div class="service-item-text">

                  <h4>{{ item.noiDung }}</h4>

                  <p>{{ getImageName(item) }}</p>

                </div>



                <div class="service-card-image-frame">

                  <img

                      :src="getFirstImage(item)"

                      :alt="item.noiDung"

                      @error="setFakeImage"

                  />

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



  if (

      cleanPath.startsWith('http://') ||

      cleanPath.startsWith('https://') ||

      cleanPath.startsWith('data:image')

  ) {

    return cleanPath

  }



  if (cleanPath.startsWith('/')) {

    return cleanPath

  }



  return `/images/TrangDichVuChiTiet/${cleanPath}`

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



const getFirstImage = (item) => {

  const imagePath = item.hinhAnhs?.[0]?.hinhAnh

  return normalizeImagePath(imagePath)

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

  if (loai === 0) return 'Sản phẩm'

  if (loai === 1) return 'Dịch vụ'

  return 'Không xác định'

}



const resetData = () => {

  service.value = {

    tenCombo: '',

    gia: 0,

    moTa: ''

  }



  comboChiTiet.value = []

  images.value = [FAKE_IMAGE, FAKE_IMAGE, FAKE_IMAGE]

  currentIndex.value = 0

}



const loadCombo = async () => {

  try {

    const id = route.params.id



    const res = await axios.get(

        `http://localhost:8080/api/dich-vu/${id}`

    )



    service.value = {

      tenCombo: res.data.tenCombo || '',

      gia: res.data.gia || 0,

      moTa: res.data.moTa || ''

    }



  } catch (e) {

    console.log('Không lấy được dữ liệu gói dịch vụ', e)

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

      }))

    }))



    const quanTaiItem = comboChiTiet.value.find(item =>

        isQuanTaiItem(item)

    )



    const quanTaiImages = quanTaiItem?.hinhAnhs

        ?.map(img => normalizeImagePath(img.hinhAnh))

        ?.filter(Boolean)

        ?.slice(0, 3) || []



    while (quanTaiImages.length < 3) {

      quanTaiImages.push(FAKE_IMAGE)

    }



    images.value = quanTaiImages

    currentIndex.value = 0



  } catch (e) {

    console.log('Không lấy được chi tiết dịch vụ', e)

    images.value = [FAKE_IMAGE, FAKE_IMAGE, FAKE_IMAGE]

  }

}



const loadPage = async () => {

  window.scrollTo(0, 0)

  resetData()



  await Promise.all([

    loadCombo(),

    loadComboChiTiet()

  ])

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