<template>
  <section class="create-combo-page">
    <!-- Tiêu đề -->
    <header class="page-header">
      <div>
        <p class="page-label">ĐỐI TÁC</p>
        <h1>Tạo combo mới</h1>
        <p class="page-description">
          Nhập thông tin combo và chọn sản phẩm của bạn để gửi duyệt.
        </p>
      </div>

      <span class="pending-note">Combo mới sẽ ở trạng thái chờ duyệt</span>
    </header>

    <form class="combo-form" @submit.prevent="createCombo">
      <!-- Cột trái: thông tin combo -->
      <div class="form-card">
        <div class="card-heading">
          <span class="step-number">1</span>

          <div>
            <h2>Thông tin combo</h2>
            <p>Nhập các thông tin cơ bản của combo.</p>
          </div>
        </div>

        <div class="form-grid">
          <label class="form-group full-width">
            <span>Tên combo <b>*</b></span>

            <input
                v-model.trim="form.TenCombo"
                type="text"
                maxlength="255"
                placeholder="Ví dụ: Gói tang lễ cơ bản"
            />
          </label>

          <label class="form-group">
            <span>Giá combo <b>*</b></span>

            <input
                v-model.number="form.Gia"
                type="number"
                min="1"
                step="1000"
                placeholder="Nhập giá combo"
            />
          </label>

          <label class="form-group">
            <span>Trạng thái</span>

            <input value="Chờ duyệt" type="text" disabled />
          </label>

          <label class="form-group full-width">
            <span>Đường dẫn hình ảnh</span>

            <input
                v-model.trim="form.HinhAnh"
                type="text"
                maxlength="500"
                placeholder="/images/combo-co-ban.jpg"
            />
          </label>

          <label class="form-group full-width">
            <span>Mô tả combo</span>

            <textarea
                v-model.trim="form.MoTa"
                rows="5"
                placeholder="Nhập mô tả ngắn cho combo"
            ></textarea>
          </label>

          <label class="form-group full-width">
            <span>Nội dung hoặc dịch vụ bổ sung</span>

            <textarea
                v-model="form.NoiDungThem"
                rows="4"
                placeholder="Mỗi dòng là một nội dung. Ví dụ:&#10;Tư vấn 24/7&#10;Hỗ trợ thủ tục"
            ></textarea>

          </label>
        </div>

        <div class="image-preview">
          <div>
            <strong>Xem trước ảnh combo</strong>
            <p>Ảnh sẽ hiển thị từ đường dẫn bạn nhập.</p>
          </div>

          <img
              :src="form.HinhAnh || fallbackImage"
              alt="Ảnh xem trước combo"
              @error="useFallbackImage"
          />
        </div>
      </div>

      <!-- Cột phải: chọn sản phẩm -->
      <div class="form-card product-card">
        <div class="card-heading">
          <span class="step-number product-step">2</span>

          <div>
            <h2>Chọn sản phẩm</h2>
            <p>Chỉ hiển thị sản phẩm đang bán của đối tác hiện tại.</p>
          </div>
        </div>

        <div class="product-toolbar">
          <label class="product-search">
            <span>⌕</span>

            <input
                v-model.trim="productKeyword"
                type="search"
                placeholder="Tìm sản phẩm..."
            />
          </label>

          <span class="selected-count">
            Đã chọn {{ selectedProductIds.length }} sản phẩm
          </span>
        </div>

        <div class="product-list">
          <label
              v-for="product in filteredProducts"
              :key="product.MaSanPham"
              class="product-item"
              :class="{ selected: isSelected(product.MaSanPham) }"
          >
            <input
                v-model="selectedProductIds"
                type="checkbox"
                :value="product.MaSanPham"
            />

            <img
                :src="product.HinhAnh"
                :alt="product.tenSanPham"
                @error="useFallbackImage"
            />

            <div class="product-info">
              <strong>{{ product.tenSanPham }}</strong>
              <p>{{ product.loai }} · {{ product.vatLieu }}</p>

              <div class="product-meta">
                <span>{{ formatMoney(product.giaTien) }}</span>
                <small>Còn {{ product.SoLuong }}</small>
              </div>
            </div>
          </label>

          <div v-if="filteredProducts.length === 0" class="empty-products">
            Không tìm thấy sản phẩm phù hợp.
          </div>
        </div>
      </div>

      <!-- Tóm tắt -->
      <div class="summary-card">
        <div>
          <p>Sản phẩm đã chọn</p>
          <strong>{{ selectedProducts.length }}</strong>
        </div>

        <div>
          <p>Tổng giá sản phẩm</p>
          <strong>{{ formatMoney(totalProductPrice) }}</strong>
        </div>

        <div>
          <p>Giá combo</p>
          <strong class="combo-price">{{ formatMoney(form.Gia || 0) }}</strong>
        </div>

        <div>
          <p>Khách hàng tiết kiệm</p>
          <strong class="saving-price">{{ formatMoney(savingAmount) }}</strong>
        </div>
      </div>

      <!-- Nút thao tác -->
      <footer class="form-actions">
        <button class="btn btn-reset" type="button" @click="resetForm">
          Nhập lại
        </button>

        <button class="btn btn-submit" type="submit">
          Tạo combo
        </button>
      </footer>
    </form>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'

const currentPartnerId = 1

const products = ref([
  {
    MaSanPham: 1,
    tenSanPham: 'Quan tài gỗ sồi',
    loai: 'Quan tài',
    vatLieu: 'Gỗ sồi',
    giaTien: 15000000,
    MaDoiTac: 1,
    SoLuong: 10,
    HinhAnh: '/images/quan-tai-go-soi.jpg',
    trangThai: 1
  },
  {
    MaSanPham: 2,
    tenSanPham: 'Quan tài gỗ mun',
    loai: 'Quan tài',
    vatLieu: 'Gỗ mun',
    giaTien: 25000000,
    MaDoiTac: 1,
    SoLuong: 5,
    HinhAnh: '/images/quan-tai-go-mun.jpg',
    trangThai: 1
  },
  {
    MaSanPham: 3,
    tenSanPham: 'Lọ hoa bằng gốm',
    loai: 'Lọ hoa',
    vatLieu: 'Gốm sứ',
    giaTien: 500000,
    MaDoiTac: 1,
    SoLuong: 50,
    HinhAnh: '/images/lo-hoa-gom.jpg',
    trangThai: 1
  },
  {
    MaSanPham: 4,
    tenSanPham: 'Nến hương',
    loai: 'Nến',
    vatLieu: 'Paraffin',
    giaTien: 100000,
    MaDoiTac: 1,
    SoLuong: 100,
    HinhAnh: '/images/nen-huong.jpg',
    trangThai: 1
  },
  {
    MaSanPham: 5,
    tenSanPham: 'Bộ quần áo tang',
    loai: 'Quần áo',
    vatLieu: 'Vải cotton',
    giaTien: 300000,
    MaDoiTac: 1,
    SoLuong: 50,
    HinhAnh: '/images/quan-ao-tang.jpg',
    trangThai: 1
  }
])

/* Dữ liệu của form */
const form = reactive({
  TenCombo: '',
  Gia: null,
  MoTa: '',
  HinhAnh: '',
  NoiDungThem: ''
})

const productKeyword = ref('')
const selectedProductIds = ref([])

const fallbackImage =
    'data:image/svg+xml;charset=UTF-8,' +
    encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="500" height="350">
      <rect width="100%" height="100%" fill="#eef2f7"/>
      <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle"
            fill="#6b7280" font-family="Arial" font-size="24">
        Chưa có hình ảnh
      </text>
    </svg>
  `)

/* Chỉ lấy sản phẩm đang bán của đối tác hiện tại */
const partnerProducts = computed(() => {
  return products.value.filter((product) => {
    return (
        product.MaDoiTac === currentPartnerId &&
        product.trangThai === 1
    )
  })
})

/* Tìm kiếm sản phẩm */
const filteredProducts = computed(() => {
  const keyword = productKeyword.value.toLowerCase()

  if (!keyword) {
    return partnerProducts.value
  }

  return partnerProducts.value.filter((product) => {
    return (
        product.tenSanPham.toLowerCase().includes(keyword) ||
        product.loai.toLowerCase().includes(keyword)
    )
  })
})

/* Danh sách sản phẩm đã chọn */
const selectedProducts = computed(() => {
  return partnerProducts.value.filter((product) => {
    return selectedProductIds.value.includes(product.MaSanPham)
  })
})

const totalProductPrice = computed(() => {
  return selectedProducts.value.reduce((total, product) => {
    return total + Number(product.giaTien)
  }, 0)
})

const savingAmount = computed(() => {
  const comboPrice = Number(form.Gia) || 0
  return Math.max(0, totalProductPrice.value - comboPrice)
})

function isSelected(productId) {
  return selectedProductIds.value.includes(productId)
}

/* Kiểm tra dữ liệu trước khi tạo */
function validateForm() {
  const tenCombo = form.TenCombo.trim()
  const giaCombo = Number(form.Gia)

  // Kiểm tra tên combo
  if (!tenCombo) {
    alert('Vui lòng nhập tên combo!')
    return false
  }

  if (tenCombo.length < 5) {
    alert('Tên combo phải có ít nhất 5 ký tự!')
    return false
  }

  if (tenCombo.length > 255) {
    alert('Tên combo không được vượt quá 255 ký tự!')
    return false
  }

  // Kiểm tra giá combo
  if (!giaCombo || giaCombo <= 0) {
    alert('Giá combo phải lớn hơn 0!')
    return false
  }

  if (giaCombo > 1000000000) {
    alert('Giá combo không được vượt quá 1 tỷ đồng!')
    return false
  }

  // Kiểm tra mô tả
  if (!form.MoTa.trim()) {
    alert('Vui lòng nhập mô tả combo!')
    return false
  }

  if (form.MoTa.trim().length < 10) {
    alert('Mô tả combo phải có ít nhất 10 ký tự!')
    return false
  }

  // Kiểm tra sản phẩm
  if (selectedProductIds.value.length === 0) {
    alert('Vui lòng chọn ít nhất một sản phẩm!')
    return false
  }

  // Kiểm tra sản phẩm có tồn tại không
  if (
      selectedProducts.value.length !==
      selectedProductIds.value.length
  ) {
    alert('Có sản phẩm không tồn tại hoặc không hợp lệ!')
    return false
  }

  // Kiểm tra sản phẩm có thuộc đối tác hiện tại không
  const productNotOwned = selectedProducts.value.find((product) => {
    return product.MaDoiTac !== currentPartnerId
  })

  if (productNotOwned) {
    alert(
        'Sản phẩm "' +
        productNotOwned.tenSanPham +
        '" không thuộc đối tác hiện tại!'
    )
    return false
  }

  // Kiểm tra trạng thái sản phẩm
  const stoppedProduct = selectedProducts.value.find((product) => {
    return product.trangThai !== 1
  })

  if (stoppedProduct) {
    alert(
        'Sản phẩm "' +
        stoppedProduct.tenSanPham +
        '" hiện không được bán!'
    )
    return false
  }

  // Kiểm tra số lượng tồn kho
  const outOfStockProduct = selectedProducts.value.find((product) => {
    return Number(product.SoLuong) <= 0
  })

  if (outOfStockProduct) {
    alert(
        'Sản phẩm "' +
        outOfStockProduct.tenSanPham +
        '" đã hết hàng!'
    )
    return false
  }

  return true
}

/* Tạo JSON đúng theo bảng combo và combochitiet */
function createCombo() {
  if (!validateForm()) {
    return
  }

  const productDetails = selectedProducts.value.map((product) => {
    return {
      ComboChiTietId: null,
      MaSanPham: product.MaSanPham,
      ComboID: null,
      Loai: 0,
      NoiDung: product.tenSanPham
    }
  })

  const extraDetails = form.NoiDungThem
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line !== '')
      .map((line) => {
        return {
          ComboChiTietId: null,
          MaSanPham: null,
          ComboID: null,
          Loai: 1,
          NoiDung: line
        }
      })

  const payload = {
    combo: {
      ComboId: null,
      TenCombo: form.TenCombo.trim(),
      Gia: Number(form.Gia),
      MoTa: form.MoTa.trim(),
      HinhAnh: form.HinhAnh.trim(),
      TrangThai: 2
    },

    combochitiet: [
      ...productDetails,
      ...extraDetails
    ]
  }

  console.log('Dữ liệu tạo combo:', payload)

  alert('Tạo combo thành công! Combo đang chờ duyệt.')

  resetForm()
}

function resetForm() {
  form.TenCombo = ''
  form.Gia = null
  form.MoTa = ''
  form.HinhAnh = ''
  form.NoiDungThem = ''

  productKeyword.value = ''
  selectedProductIds.value = []
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value) || 0) + ' ₫'
}

function useFallbackImage(event) {
  event.target.src = fallbackImage
}
</script>

<style
    scoped
    src="../../assets/styles/doitac/QLcombo/TaoCombo.css"
></style>
