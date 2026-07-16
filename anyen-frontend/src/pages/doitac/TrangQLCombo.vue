<template>
  <div class="combo-page">
    <div class="summary-row">
      <div class="summary-card"><span>Tổng combo</span><strong>{{ combos.length }}</strong></div>
      <div class="summary-card"><span>Đang hoạt động</span><strong>{{ activeCount }}</strong></div>
      <div class="summary-card"><span>Sản phẩm có thể chọn</span><strong>{{ products.length }}</strong></div>
    </div>

    <section class="table-card">
      <div class="table-toolbar">
        <el-input
            v-model="keyword"
            clearable
            placeholder="Tìm theo tên combo hoặc sản phẩm"
            class="search-input"
        >
          <template #prefix><i class="fa-solid fa-magnifying-glass"></i></template>
        </el-input>
        <el-select v-model="statusFilter" class="status-filter">
          <el-option label="Tất cả trạng thái" value="all" />
          <el-option label="Đang hoạt động" :value="1" />
          <el-option label="Đang ẩn" :value="0" />
          <el-option label="Ngừng kinh doanh" :value="2" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="filteredCombos" stripe empty-text="Chưa có combo nào">
        <el-table-column label="Combo" min-width="250">
          <template #default="{ row }">
            <div class="combo-cell">
              <img v-if="row.hinhAnh" :src="row.hinhAnh" :alt="row.tenCombo" />
              <div v-else class="combo-placeholder"><i class="fa-solid fa-layer-group"></i></div>
              <div>
                <strong>{{ row.tenCombo }}</strong>
                <small>{{ row.moTa || "Chưa có mô tả" }}</small>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Sản phẩm và số lượng" min-width="310">
          <template #default="{ row }">
            <div class="product-tags">
              <el-tag
                  v-for="product in row.sanPhams"
                  :key="product.maSanPham"
                  type="info"
                  effect="plain"
              >
                {{ product.tenSanPham }} × {{ product.soLuongTrongCombo || 1 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Giá" width="175" align="right">
          <template #default="{ row }">
            <div class="price-cell">
              <strong>{{ formatCurrency(row.gia) }}</strong>
              <small>Tổng SP {{ formatCurrency(row.tongGiaSanPham) }}</small>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Trạng thái" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.trangThai)">{{ row.tenTrangThai }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Thao tác" width="190" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">Sửa</el-button>
            <el-dropdown trigger="click" @command="value => changeStatus(row, value)">
              <el-button size="small" type="warning">
                Trạng thái <i class="fa-solid fa-angle-down ms-1"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="1" :disabled="row.trangThai === 1">Đang hoạt động</el-dropdown-item>
                  <el-dropdown-item :command="0" :disabled="row.trangThai === 0">Ẩn combo</el-dropdown-item>
                  <el-dropdown-item :command="2" :disabled="row.trangThai === 2">Ngừng kinh doanh</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog
        v-model="dialogVisible"
        title="Cập nhật combo"
        width="900px"
        destroy-on-close
        @closed="resetForm"
    >
      <el-form label-position="top">
        <div class="dialog-grid">
          <el-form-item label="Tên combo *" class="full-width">
            <el-input v-model="form.tenCombo" maxlength="255" show-word-limit />
          </el-form-item>

          <el-form-item label="Giá combo *">
            <el-input-number
                v-model="form.gia"
                :min="1"
                :step="100000"
                controls-position="right"
                class="number-input"
            />
          </el-form-item>

          <el-form-item label="Trạng thái">
            <el-select v-model="form.trangThai" class="full-control">
              <el-option label="Đang hoạt động" :value="1" />
              <el-option label="Đang ẩn" :value="0" />
              <el-option label="Ngừng kinh doanh" :value="2" />
            </el-select>
          </el-form-item>

          <el-form-item label="Ảnh combo (URL)" class="full-width">
            <el-input v-model="form.hinhAnh" maxlength="500" placeholder="https://..." />
          </el-form-item>

          <el-form-item label="Mô tả" class="full-width">
            <el-input v-model="form.moTa" type="textarea" :rows="3" maxlength="5000" show-word-limit />
          </el-form-item>

          <el-form-item class="full-width product-picker">
            <template #label>
              <div class="picker-label">
                <span>Sản phẩm trong combo *</span>
                <small>Chọn sản phẩm và nhập số lượng không vượt tồn kho.</small>
              </div>
            </template>

            <el-input v-model="productKeyword" clearable placeholder="Tìm sản phẩm" class="product-search" />

            <div v-if="filteredProducts.length" class="product-list">
              <article
                  v-for="product in filteredProducts"
                  :key="product.maSanPham"
                  class="product-option"
                  :class="{ selected: isProductSelected(product.maSanPham) }"
              >
                <el-checkbox
                    :model-value="isProductSelected(product.maSanPham)"
                    :disabled="Number(product.soLuong || 0) <= 0 && !isProductSelected(product.maSanPham)"
                    @change="checked => toggleProduct(product, checked)"
                />
                <img v-if="product.hinhAnh" :src="product.hinhAnh" :alt="product.tenSanPham" />
                <div v-else class="product-image-placeholder"><i class="fa-solid fa-box"></i></div>
                <div class="product-copy">
                  <strong>{{ product.tenSanPham }}</strong>
                  <small>{{ formatCurrency(product.giaTien) }} · Tồn kho {{ product.soLuong ?? 0 }}</small>
                </div>
                <div v-if="isProductSelected(product.maSanPham)" class="product-quantity">
                  <span>Số lượng</span>
                  <el-input-number
                      :model-value="getSelectedQuantity(product.maSanPham)"
                      :min="1"
                      :max="Math.max(1, Number(product.soLuong || 0))"
                      size="small"
                      @update:model-value="value => updateQuantity(product, value)"
                  />
                  <strong>{{ formatCurrency(productLineTotal(product)) }}</strong>
                </div>
              </article>
            </div>
            <el-empty v-else description="Không có sản phẩm đang bán để tạo combo" :image-size="80" />
          </el-form-item>

          <div class="edit-summary full-width">
            <div><span>Sản phẩm</span><strong>{{ selectedFormProducts.length }}</strong></div>
            <div><span>Tổng số lượng</span><strong>{{ editTotalQuantity }}</strong></div>
            <div><span>Tổng giá sản phẩm</span><strong>{{ formatCurrency(editTotalProductPrice) }}</strong></div>
            <div><span>Giá combo</span><strong :class="{ invalid: editPriceExceedsTotal }">{{ formatCurrency(form.gia) }}</strong></div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Hủy</el-button>
        <el-button type="danger" :loading="saving" :disabled="editPriceExceedsTotal" @click="saveCombo">
          Lưu thay đổi
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getCombosDoiTac,
  getSanPhamComboDoiTac,
  updateComboDoiTac,
  updateTrangThaiComboDoiTac
} from "../../services/comboDoiTacService.js";

const combos = ref([]);
const products = ref([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingCombo = ref(null);
const keyword = ref("");
const productKeyword = ref("");
const statusFilter = ref("all");

const form = reactive({
  tenCombo: "",
  gia: 0,
  moTa: "",
  hinhAnh: "",
  trangThai: 1,
  sanPhams: []
});

const activeCount = computed(() => combos.value.filter(item => item.trangThai === 1).length);

const filteredCombos = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  return combos.value.filter(item => {
    const matchStatus = statusFilter.value === "all" || item.trangThai === statusFilter.value;
    const productText = (item.sanPhams || []).map(product => product.tenSanPham).join(" ").toLowerCase();
    return matchStatus && (!query || (item.tenCombo || "").toLowerCase().includes(query) || productText.includes(query));
  });
});

const filteredProducts = computed(() => {
  const query = productKeyword.value.trim().toLowerCase();
  if (!query) return products.value;
  return products.value.filter(product =>
      (product.tenSanPham || "").toLowerCase().includes(query)
      || String(product.maSanPham || "").includes(query)
  );
});

const selectedFormProducts = computed(() =>
    form.sanPhams.map(item => {
      const product = products.value.find(candidate => candidate.maSanPham === item.maSanPham);
      return product ? { ...product, soLuongTrongCombo: Number(item.soLuong || 1) } : null;
    }).filter(Boolean)
);

const editTotalQuantity = computed(() =>
    form.sanPhams.reduce((total, item) => total + Number(item.soLuong || 0), 0)
);

const editTotalProductPrice = computed(() =>
    selectedFormProducts.value.reduce(
        (total, product) => total + Number(product.giaTien || 0) * product.soLuongTrongCombo,
        0
    )
);

const editPriceExceedsTotal = computed(() =>
    editTotalProductPrice.value > 0 && Number(form.gia || 0) > editTotalProductPrice.value
);

onMounted(async () => {
  await Promise.all([loadCombos(), loadProducts()]);
});

async function loadCombos() {
  loading.value = true;
  try {
    const res = await getCombosDoiTac();
    combos.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không tải được danh sách combo"));
  } finally {
    loading.value = false;
  }
}

async function loadProducts() {
  try {
    const res = await getSanPhamComboDoiTac();
    products.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không tải được sản phẩm của đối tác"));
  }
}

function openEditDialog(combo) {
  editingCombo.value = combo;
  form.tenCombo = combo.tenCombo || "";
  form.gia = Number(combo.gia || 0);
  form.moTa = combo.moTa || "";
  form.hinhAnh = combo.hinhAnh || "";
  form.trangThai = combo.trangThai ?? 1;
  form.sanPhams = (combo.sanPhams || []).map(product => ({
    maSanPham: product.maSanPham,
    soLuong: Math.max(1, Number(product.soLuongTrongCombo || 1))
  }));
  productKeyword.value = "";
  dialogVisible.value = true;
}

function resetForm() {
  form.tenCombo = "";
  form.gia = 0;
  form.moTa = "";
  form.hinhAnh = "";
  form.trangThai = 1;
  form.sanPhams = [];
  productKeyword.value = "";
  editingCombo.value = null;
}

function isProductSelected(productId) {
  return form.sanPhams.some(item => item.maSanPham === productId);
}

function getSelectedQuantity(productId) {
  return Number(form.sanPhams.find(item => item.maSanPham === productId)?.soLuong || 1);
}

function toggleProduct(product, checked) {
  if (checked && !isProductSelected(product.maSanPham)) {
    form.sanPhams.push({ maSanPham: product.maSanPham, soLuong: 1 });
  } else if (!checked) {
    form.sanPhams = form.sanPhams.filter(item => item.maSanPham !== product.maSanPham);
  }
}

function updateQuantity(product, value) {
  const item = form.sanPhams.find(candidate => candidate.maSanPham === product.maSanPham);
  if (!item) return;
  const stock = Math.max(1, Number(product.soLuong || 0));
  item.soLuong = Math.min(stock, Math.max(1, Math.trunc(Number(value || 1))));
}

function productLineTotal(product) {
  return Number(product.giaTien || 0) * getSelectedQuantity(product.maSanPham);
}

async function saveCombo() {
  if (!form.tenCombo.trim()) {
    ElMessage.warning("Vui lòng nhập tên combo");
    return;
  }
  if (Number(form.gia || 0) <= 0) {
    ElMessage.warning("Giá combo phải lớn hơn 0");
    return;
  }
  if (!form.sanPhams.length) {
    ElMessage.warning("Vui lòng chọn ít nhất một sản phẩm");
    return;
  }

  const invalidItem = form.sanPhams.find(item => {
    const product = products.value.find(candidate => candidate.maSanPham === item.maSanPham);
    return !product || Number(item.soLuong || 0) <= 0 || Number(item.soLuong) > Number(product.soLuong || 0);
  });
  if (invalidItem) {
    ElMessage.warning("Có sản phẩm có số lượng không hợp lệ hoặc vượt tồn kho");
    return;
  }
  if (editPriceExceedsTotal.value) {
    ElMessage.warning("Giá combo không được lớn hơn tổng giá sản phẩm theo số lượng");
    return;
  }
  if (!editingCombo.value) {
    ElMessage.warning("Vui lòng chọn combo cần sửa");
    return;
  }

  const payload = {
    tenCombo: form.tenCombo.trim(),
    gia: Number(form.gia),
    moTa: form.moTa.trim(),
    hinhAnh: form.hinhAnh.trim(),
    trangThai: form.trangThai,
    sanPhams: form.sanPhams.map(item => ({
      maSanPham: item.maSanPham,
      soLuong: Number(item.soLuong)
    }))
  };

  saving.value = true;
  try {
    await updateComboDoiTac(editingCombo.value.comboId, payload);
    ElMessage.success("Cập nhật combo thành công");
    dialogVisible.value = false;
    await loadCombos();
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không thể lưu combo"));
  } finally {
    saving.value = false;
  }
}

async function changeStatus(combo, status) {
  try {
    await updateTrangThaiComboDoiTac(combo.comboId, status);
    ElMessage.success("Đã cập nhật trạng thái combo");
    await loadCombos();
  } catch (error) {
    ElMessage.error(getErrorMessage(error, "Không thể cập nhật trạng thái"));
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(Number(value || 0));
}

function statusType(status) {
  if (status === 1) return "success";
  if (status === 2) return "warning";
  return "info";
}

function getErrorMessage(error, fallback) {
  return error?.response?.data?.message || error?.response?.data?.error || fallback;
}
</script>

<style scoped>
.combo-page { min-height: 100vh; padding: 28px; background: #f5f7fb; color: #1f2937; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 18px; margin-bottom: 22px; }
.eyebrow { margin: 0 0 5px !important; color: #be123c !important; font-size: 11px; font-weight: 800; letter-spacing: .1em; }
.page-header h2 { margin: 0; font-weight: 780; }
.page-header p { margin: 7px 0 0; color: #6b7280; }
.summary-row { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 20px; }
.summary-card, .table-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 15px; box-shadow: 0 8px 24px rgba(15, 23, 42, .05); }
.summary-card { padding: 18px 20px; display: flex; align-items: center; justify-content: space-between; }
.summary-card span { color: #64748b; }
.summary-card strong { color: #9f1239; font-size: 25px; }
.table-card { padding: 20px; }
.table-toolbar { display: flex; gap: 12px; margin-bottom: 18px; }
.search-input { max-width: 420px; }
.status-filter { width: 190px; }
.combo-cell { display: flex; gap: 12px; align-items: center; }
.combo-cell img, .combo-placeholder { width: 58px; height: 58px; border-radius: 10px; object-fit: cover; flex: 0 0 auto; }
.combo-placeholder { display: grid; place-items: center; color: #9f1239; background: #fff1f2; }
.combo-cell small { display: -webkit-box; margin-top: 5px; color: #6b7280; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.product-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.price-cell { display: grid; gap: 4px; }
.price-cell strong { color: #b42318; }
.price-cell small { color: #64748b; font-size: 11px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.full-width { grid-column: 1 / -1; }
.full-control, .number-input { width: 100%; }
.picker-label { display: flex; justify-content: space-between; align-items: baseline; width: 100%; gap: 12px; }
.picker-label small { color: #6b7280; font-weight: 400; }
.product-search { margin-bottom: 12px; }
.product-list { width: 100%; max-height: 390px; overflow: auto; display: grid; gap: 10px; padding: 4px; }
.product-option { min-width: 0; display: grid; grid-template-columns: auto 48px minmax(0, 1fr) minmax(190px, auto); align-items: center; gap: 10px; padding: 10px; border: 1px solid #e5e7eb; border-radius: 11px; transition: .18s ease; }
.product-option.selected { border-color: #e11d48; background: #fff1f2; }
.product-option img, .product-image-placeholder { width: 46px; height: 46px; border-radius: 8px; object-fit: cover; }
.product-image-placeholder { display: grid; place-items: center; background: #f1f5f9; color: #64748b; }
.product-copy { min-width: 0; }
.product-copy strong, .product-copy small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-copy small { margin-top: 4px; color: #64748b; }
.product-quantity { display: grid; grid-template-columns: auto auto; gap: 5px 10px; align-items: center; justify-content: end; }
.product-quantity span { color: #64748b; font-size: 11px; }
.product-quantity strong { grid-column: 1 / -1; justify-self: end; color: #9f1239; font-size: 12px; }
.edit-summary { display: grid; grid-template-columns: repeat(4, 1fr); overflow: hidden; margin-top: 6px; border: 1px solid #e5e7eb; border-radius: 12px; background: #f8fafc; }
.edit-summary div { padding: 12px 14px; border-right: 1px solid #e5e7eb; }
.edit-summary div:last-child { border-right: 0; }
.edit-summary span, .edit-summary strong { display: block; }
.edit-summary span { color: #64748b; font-size: 11px; }
.edit-summary strong { margin-top: 4px; color: #1f2937; }
.edit-summary strong.invalid { color: #dc2626; }
@media (max-width: 900px) { .summary-row, .dialog-grid { grid-template-columns: 1fr; } .full-width { grid-column: auto; } .product-option { grid-template-columns: auto 48px minmax(0, 1fr); } .product-quantity { grid-column: 2 / -1; justify-content: start; padding-top: 8px; border-top: 1px solid #fecdd3; } .edit-summary { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .combo-page { padding: 16px; } .page-header, .table-toolbar { flex-direction: column; align-items: stretch; } .search-input, .status-filter { max-width: none; width: 100%; } }
</style>
