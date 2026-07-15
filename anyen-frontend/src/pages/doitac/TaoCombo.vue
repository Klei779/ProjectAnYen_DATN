<template>
  <section class="create-combo-page">
    <header class="page-header">
      <div>
        <p class="page-label">ĐỐI TÁC</p>
        <h1>Tạo combo mới</h1>
        <p class="page-description">
          Combo chỉ sử dụng sản phẩm đã duyệt của tài khoản hiện tại và có kiểm tra số lượng tồn kho.
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
            <p>Giá combo phải lớn hơn 0 và không vượt tổng giá sản phẩm theo số lượng.</p>
          </div>
        </div>

        <div class="form-grid">
          <label class="form-group full-width">
            <span>Tên combo <b>*</b></span>
            <input v-model.trim="form.tenCombo" maxlength="255" placeholder="Ví dụ: Gói tang lễ cơ bản" />
          </label>

          <label class="form-group">
            <span class="field-label">
              <span>Giá combo <b>*</b></span>
              <button type="button" class="use-total-button" :disabled="!totalProductPrice" @click="form.gia = totalProductPrice">
                Dùng tổng giá
              </button>
            </span>
            <input v-model.number="form.gia" type="number" min="1" step="1000" placeholder="Nhập giá combo" />
            <small v-if="priceExceedsTotal" class="field-error">
              Giá combo đang lớn hơn tổng giá sản phẩm {{ formatMoney(totalProductPrice) }}.
            </small>
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
            <h2>Chọn sản phẩm và số lượng</h2>
            <p>Mỗi sản phẩm được chọn một lần, số lượng không được vượt tồn kho.</p>
          </div>
        </div>

        <div class="product-toolbar">
          <label class="product-search">
            <span>⌕</span>
            <input v-model.trim="productKeyword" type="search" placeholder="Tìm sản phẩm..." />
          </label>
          <span class="selected-count">
            {{ selectedProducts.length }} sản phẩm · {{ totalSelectedQuantity }} đơn vị
          </span>
        </div>

        <div v-if="loadingProducts" class="empty-products">Đang tải sản phẩm...</div>
        <div v-else class="product-list">
          <article
              v-for="product in filteredProducts"
              :key="product.maSanPham"
              class="product-item"
              :class="{ selected: isSelected(product.maSanPham), unavailable: Number(product.soLuong || 0) <= 0 }"
          >
            <input
                type="checkbox"
                :checked="isSelected(product.maSanPham)"
                :disabled="Number(product.soLuong || 0) <= 0"
                @change="event => toggleProduct(product, event.target.checked)"
            />
            <img :src="product.hinhAnh || fallbackImage" :alt="product.tenSanPham" @error="useFallbackImage" />
            <div class="product-info">
              <strong>{{ product.tenSanPham }}</strong>
              <p>Mã SP: {{ product.maSanPham }} · Đã duyệt</p>
              <div class="product-meta">
                <span>{{ formatMoney(product.giaTien) }}</span>
                <small :class="{ out: Number(product.soLuong || 0) <= 0 }">
                  Tồn kho {{ product.soLuong ?? 0 }}
                </small>
              </div>
            </div>

            <div v-if="isSelected(product.maSanPham)" class="quantity-editor" @click.stop>
              <span>Số lượng</span>
              <div class="quantity-control">
                <button type="button" @click="changeQuantity(product, -1)">−</button>
                <input
                    v-model.number="selectedQuantities[product.maSanPham]"
                    type="number"
                    min="1"
                    :max="Math.max(1, Number(product.soLuong || 0))"
                    @change="normalizeQuantity(product)"
                />
                <button type="button" @click="changeQuantity(product, 1)">+</button>
              </div>
              <strong>{{ formatMoney(lineTotal(product)) }}</strong>
            </div>
          </article>

          <div v-if="filteredProducts.length === 0" class="empty-products">
            Không có sản phẩm đã duyệt phù hợp. Hãy tạo sản phẩm và chờ nhân viên duyệt trước.
          </div>
        </div>
      </div>

      <div class="summary-card">
        <div><p>Loại sản phẩm</p><strong>{{ selectedProducts.length }}</strong></div>
        <div><p>Tổng số lượng</p><strong>{{ totalSelectedQuantity }}</strong></div>
        <div><p>Tổng giá sản phẩm</p><strong>{{ formatMoney(totalProductPrice) }}</strong></div>
        <div><p>Giá combo</p><strong class="combo-price">{{ formatMoney(form.gia || 0) }}</strong></div>
        <div><p>Khách hàng tiết kiệm</p><strong class="saving-price">{{ formatMoney(savingAmount) }}</strong></div>
      </div>

      <footer class="form-actions">
        <button class="btn btn-reset" type="button" :disabled="submitting" @click="resetForm">Nhập lại</button>
        <button class="btn btn-submit" type="submit" :disabled="submitting || loadingProducts || priceExceedsTotal">
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
const selectedQuantities = reactive({});
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
    products.value
        .filter(product => selectedProductIds.value.includes(product.maSanPham))
        .map(product => ({
          ...product,
          soLuongTrongCombo: getQuantity(product)
        }))
);

const totalSelectedQuantity = computed(() =>
    selectedProducts.value.reduce((total, product) => total + product.soLuongTrongCombo, 0)
);

const totalProductPrice = computed(() =>
    selectedProducts.value.reduce(
        (total, product) => total + Number(product.giaTien || 0) * product.soLuongTrongCombo,
        0
    )
);

const priceExceedsTotal = computed(() =>
    totalProductPrice.value > 0 && Number(form.gia || 0) > totalProductPrice.value
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

function toggleProduct(product, checked) {
  const productId = product.maSanPham;
  if (checked && !isSelected(productId)) {
    selectedProductIds.value.push(productId);
    selectedQuantities[productId] = 1;
  } else if (!checked) {
    selectedProductIds.value = selectedProductIds.value.filter(id => id !== productId);
    delete selectedQuantities[productId];
  }
}

function getQuantity(product) {
  const stock = Math.max(0, Number(product.soLuong || 0));
  const current = Number(selectedQuantities[product.maSanPham] || 1);
  return Math.min(Math.max(1, Math.trunc(current)), Math.max(1, stock));
}

function normalizeQuantity(product) {
  selectedQuantities[product.maSanPham] = getQuantity(product);
}

function changeQuantity(product, delta) {
  selectedQuantities[product.maSanPham] = getQuantity(product) + delta;
  normalizeQuantity(product);
}

function lineTotal(product) {
  return Number(product.giaTien || 0) * getQuantity(product);
}

async function submitCombo() {
  if (form.tenCombo.trim().length < 3) {
    ElMessage.warning("Tên combo phải có ít nhất 3 ký tự");
    return;
  }
  if (Number(form.gia || 0) <= 0) {
    ElMessage.warning("Giá combo phải lớn hơn 0");
    return;
  }
  if (!selectedProducts.value.length) {
    ElMessage.warning("Vui lòng chọn ít nhất một sản phẩm");
    return;
  }

  const invalidProduct = selectedProducts.value.find(product =>
      product.soLuongTrongCombo <= 0
      || product.soLuongTrongCombo > Number(product.soLuong || 0)
  );
  if (invalidProduct) {
    ElMessage.warning(`Số lượng ${invalidProduct.tenSanPham} không hợp lệ hoặc vượt tồn kho`);
    return;
  }
  if (priceExceedsTotal.value) {
    ElMessage.warning("Giá combo không được lớn hơn tổng giá sản phẩm theo số lượng");
    return;
  }

  submitting.value = true;
  try {
    await createComboDoiTac({
      tenCombo: form.tenCombo.trim(),
      gia: Number(form.gia),
      moTa: form.moTa.trim(),
      hinhAnh: form.hinhAnh.trim(),
      trangThai: Number(form.trangThai),
      sanPhams: selectedProducts.value.map(product => ({
        maSanPham: product.maSanPham,
        soLuong: product.soLuongTrongCombo
      }))
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
  Object.keys(selectedQuantities).forEach(key => delete selectedQuantities[key]);
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

.btn:disabled,
.use-total-button:disabled {
  opacity: .55;
  cursor: not-allowed;
}
</style>
