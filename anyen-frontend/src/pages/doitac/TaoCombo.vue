<template>
  <section class="create-combo-page">
    <header class="page-header">
      <div>
        <p class="page-label">ĐỐI TÁC</p>
        <h1>Tạo combo mới</h1>
        <p class="page-description">
          Chỉ các sản phẩm đã được duyệt và thuộc tài khoản đối tác đang đăng nhập mới xuất hiện ở đây.
        </p>
      </div>
      <RouterLink class="manage-link" to="/doi-tac/quan-ly-combo">Quản lý combo</RouterLink>
    </header>

    <form class="combo-form" @submit.prevent="submitCombo">
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
            <input v-model.trim="form.tenCombo" maxlength="255" placeholder="Ví dụ: Gói tang lễ cơ bản" />
          </label>

          <label class="form-group">
            <span>Giá combo <b>*</b></span>
            <input v-model.number="form.gia" type="number" min="0" step="1000" placeholder="Nhập giá combo" />
          </label>

          <label class="form-group">
            <span>Trạng thái</span>
            <select v-model.number="form.trangThai">
              <option :value="1">Đang hoạt động</option>
              <option :value="0">Đang ẩn</option>
              <option :value="2">Ngừng kinh doanh</option>
            </select>
          </label>

          <label class="form-group full-width">
            <span>Đường dẫn hình ảnh</span>
            <input v-model.trim="form.hinhAnh" maxlength="500" placeholder="https://... hoặc /images/..." />
          </label>

          <label class="form-group full-width">
            <span>Mô tả combo</span>
            <textarea v-model.trim="form.moTa" rows="5" maxlength="5000" placeholder="Nhập mô tả ngắn cho combo"></textarea>
          </label>
        </div>

        <div class="image-preview">
          <div>
            <strong>Xem trước ảnh combo</strong>
            <p>Ảnh sẽ hiển thị từ đường dẫn bạn nhập.</p>
          </div>
          <img :src="form.hinhAnh || fallbackImage" alt="Ảnh xem trước combo" @error="useFallbackImage" />
        </div>
      </div>

      <div class="form-card product-card">
        <div class="card-heading">
          <span class="step-number product-step">2</span>
          <div>
            <h2>Chọn sản phẩm</h2>
            <p>Dữ liệu được lấy từ backend theo tài khoản đối tác hiện tại.</p>
          </div>
        </div>

        <div class="product-toolbar">
          <label class="product-search">
            <span>⌕</span>
            <input v-model.trim="productKeyword" type="search" placeholder="Tìm sản phẩm..." />
          </label>
          <span class="selected-count">Đã chọn {{ selectedProductIds.length }} sản phẩm</span>
        </div>

        <div v-if="loadingProducts" class="empty-products">Đang tải sản phẩm...</div>
        <div v-else class="product-list">
          <label
              v-for="product in filteredProducts"
              :key="product.maSanPham"
              class="product-item"
              :class="{ selected: isSelected(product.maSanPham) }"
          >
            <input v-model="selectedProductIds" type="checkbox" :value="product.maSanPham" />
            <img :src="product.hinhAnh || fallbackImage" :alt="product.tenSanPham" @error="useFallbackImage" />
            <div class="product-info">
              <strong>{{ product.tenSanPham }}</strong>
              <p>Mã SP: {{ product.maSanPham }} · Đã duyệt</p>
              <div class="product-meta">
                <span>{{ formatMoney(product.giaTien) }}</span>
                <small>Còn {{ product.soLuong ?? 0 }}</small>
              </div>
            </div>
          </label>

          <div v-if="filteredProducts.length === 0" class="empty-products">
            Không có sản phẩm đã duyệt phù hợp. Hãy tạo sản phẩm và chờ nhân viên duyệt trước.
          </div>
        </div>
      </div>

      <div class="summary-card">
        <div><p>Sản phẩm đã chọn</p><strong>{{ selectedProducts.length }}</strong></div>
        <div><p>Tổng giá sản phẩm</p><strong>{{ formatMoney(totalProductPrice) }}</strong></div>
        <div><p>Giá combo</p><strong class="combo-price">{{ formatMoney(form.gia || 0) }}</strong></div>
        <div><p>Khách hàng tiết kiệm</p><strong class="saving-price">{{ formatMoney(savingAmount) }}</strong></div>
      </div>

      <footer class="form-actions">
        <button class="btn btn-reset" type="button" :disabled="submitting" @click="resetForm">Nhập lại</button>
        <button class="btn btn-submit" type="submit" :disabled="submitting || loadingProducts">
          {{ submitting ? "Đang tạo..." : "Tạo combo" }}
        </button>
      </footer>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { createComboDoiTac, getSanPhamComboDoiTac } from "../../services/comboDoiTacService.js";

const router = useRouter();
const products = ref([]);
const selectedProductIds = ref([]);
const productKeyword = ref("");
const loadingProducts = ref(false);
const submitting = ref(false);

const form = reactive({
  tenCombo: "",
  gia: 0,
  moTa: "",
  hinhAnh: "",
  trangThai: 1
});

const fallbackImage = "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(`
  <svg xmlns="http://www.w3.org/2000/svg" width="500" height="350">
    <rect width="100%" height="100%" fill="#eef2f7"/>
    <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle"
      fill="#6b7280" font-family="Arial" font-size="24">Chưa có hình ảnh</text>
  </svg>
`);

const filteredProducts = computed(() => {
  const keyword = productKeyword.value.trim().toLowerCase();
  if (!keyword) return products.value;
  return products.value.filter(product =>
      (product.tenSanPham || "").toLowerCase().includes(keyword)
      || String(product.maSanPham || "").includes(keyword)
  );
});

const selectedProducts = computed(() =>
    products.value.filter(product => selectedProductIds.value.includes(product.maSanPham))
);

const totalProductPrice = computed(() =>
    selectedProducts.value.reduce((total, product) => total + Number(product.giaTien || 0), 0)
);

const savingAmount = computed(() => Math.max(0, totalProductPrice.value - Number(form.gia || 0)));

onMounted(loadProducts);

async function loadProducts() {
  loadingProducts.value = true;
  try {
    const response = await getSanPhamComboDoiTac();
    products.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không tải được sản phẩm của đối tác"));
  } finally {
    loadingProducts.value = false;
  }
}

function isSelected(productId) {
  return selectedProductIds.value.includes(productId);
}

async function submitCombo() {
  if (form.tenCombo.trim().length < 3) {
    ElMessage.warning("Tên combo phải có ít nhất 3 ký tự");
    return;
  }
  if (Number(form.gia) < 0) {
    ElMessage.warning("Giá combo không được âm");
    return;
  }
  if (!selectedProductIds.value.length) {
    ElMessage.warning("Vui lòng chọn ít nhất một sản phẩm");
    return;
  }

  submitting.value = true;
  try {
    await createComboDoiTac({
      tenCombo: form.tenCombo.trim(),
      gia: Number(form.gia || 0),
      moTa: form.moTa.trim(),
      hinhAnh: form.hinhAnh.trim(),
      trangThai: Number(form.trangThai),
      maSanPhams: [...selectedProductIds.value]
    });
    ElMessage.success("Tạo combo thành công");
    await router.push("/doi-tac/quan-ly-combo");
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không thể tạo combo"));
  } finally {
    submitting.value = false;
  }
}

function resetForm() {
  form.tenCombo = "";
  form.gia = 0;
  form.moTa = "";
  form.hinhAnh = "";
  form.trangThai = 1;
  productKeyword.value = "";
  selectedProductIds.value = [];
}

function formatMoney(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(Number(value || 0));
}

function useFallbackImage(event) {
  event.target.src = fallbackImage;
}

function getErrorMessage(error, fallback) {
  return error?.response?.data?.message || error?.response?.data?.error || fallback;
}
</script>

<style scoped src="../../assets/styles/doitac/QLcombo/TaoCombo.css"></style>

<style scoped>
.manage-link {
  display: inline-flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 10px;
  background: #fff;
  color: #b42318;
  border: 1px solid #f0b4ad;
  text-decoration: none;
  font-weight: 700;
}

.form-group select {
  width: 100%;
  min-height: 44px;
  border: 1px solid #dfe3e8;
  border-radius: 9px;
  padding: 0 12px;
  background: #fff;
}

.btn:disabled {
  opacity: .6;
  cursor: not-allowed;
}
</style>
