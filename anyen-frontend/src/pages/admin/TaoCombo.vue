<template>
  <main class="combo-admin-editor">
    <header class="editor-header">
      <div>
        <p class="eyebrow">QUẢN TRỊ COMBO</p>
        <h1>{{ isEdit ? 'Chỉnh sửa combo' : 'Tạo combo mới' }}</h1>
        <p class="header-description">
          Chỉ sản phẩm được chọn riêng mới được đưa vào combo.
        </p>
      </div>

      <RouterLink class="back-button" to="/admin/quan-ly-combo">
        <i class="fa-solid fa-arrow-left"></i>
        Quay lại quản lý combo
      </RouterLink>
    </header>

    <div v-if="pageError" class="notice notice-error">
      <i class="fa-solid fa-circle-exclamation"></i>
      <span>{{ pageError }}</span>
      <button type="button" @click="pageError = ''">×</button>
    </div>

    <div v-if="loading" class="loading-card">
      <span class="spinner-border spinner-border-sm"></span>
      Đang tải dữ liệu combo và sản phẩm...
    </div>

    <form v-else class="editor-form" @submit.prevent="submitCombo">
      <section class="editor-card">
        <div class="card-title-row">
          <span class="step-badge">1</span>
          <div>
            <h2>Thông tin combo</h2>
            <p>Nhập tên, giá bán, mô tả và quyền lợi hiển thị trên website.</p>
          </div>
        </div>

        <div class="form-grid">
          <label class="field field-full">
            <span>Tên combo <b>*</b></span>
            <input
                v-model.trim="form.tenCombo"
                type="text"
                maxlength="255"
                placeholder="Ví dụ: Gói tang lễ trang nghiêm"
            />
          </label>

          <label class="field">
            <span class="field-heading">
              <span>Giá combo <b>*</b></span>
              <button
                  type="button"
                  class="text-button"
                  :disabled="totalProductPrice <= 0"
                  @click="form.gia = totalProductPrice"
              >
                Dùng tổng giá sản phẩm
              </button>
            </span>
            <input
                v-model.number="form.gia"
                type="number"
                min="1"
                step="1"
                placeholder="Nhập giá combo"
            />
            <small>
              Tổng giá tham khảo của sản phẩm đã chọn: {{ formatMoney(totalProductPrice) }}
            </small>
          </label>

          <label class="field">
            <span>Trạng thái</span>
            <select v-model.number="form.trangThai">
              <option :value="1">Đang hoạt động</option>
              <option :value="0">Đang ẩn</option>
              <option :value="2">Ngừng kinh doanh</option>
            </select>
          </label>

          <label class="field field-full">
            <span>Mô tả combo</span>
            <textarea
                v-model.trim="form.moTa"
                rows="4"
                maxlength="5000"
                placeholder="Mô tả ngắn về đối tượng phù hợp và nội dung chính của combo"
            ></textarea>
          </label>

          <label class="field field-full">
            <span>Quyền lợi / ghi chú</span>
            <textarea
                v-model="form.ghiChu"
                rows="5"
                maxlength="10000"
                placeholder="Mỗi quyền lợi nhập trên một dòng"
            ></textarea>
            <small>Mỗi dòng sẽ được website hiển thị thành một quyền lợi riêng.</small>
          </label>
        </div>
      </section>

      <section class="editor-card">
        <div class="card-title-row">
          <span class="step-badge image-step">2</span>
          <div>
            <h2>Hình ảnh combo</h2>
            <p>Tách riêng ảnh đại diện và ảnh minh họa quy trình.</p>
          </div>
        </div>

        <div class="image-sections">
          <article class="image-panel cover-panel">
            <div class="panel-heading">
              <div>
                <h3>Ảnh đại diện <b>*</b></h3>
                <p>Bắt buộc đúng 3 ảnh JPG, PNG hoặc WEBP; mỗi ảnh tối đa 5 MB.</p>
              </div>
              <span
                  class="count-pill"
                  :class="{ complete: displayedCoverImages.length === 3 }"
              >
                {{ displayedCoverImages.length }}/3
              </span>
            </div>

            <input
                ref="coverInput"
                class="hidden-input"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                @change="handleCoverSelect"
            />

            <div class="cover-grid">
              <div v-for="slot in 3" :key="slot" class="cover-slot">
                <template v-if="displayedCoverImages[slot - 1]">
                  <img
                      :src="displayedCoverImages[slot - 1]"
                      :alt="`Ảnh đại diện ${slot}`"
                      @error="useFallbackImage"
                  />
                  <span>Ảnh {{ slot }}</span>
                  <button
                      v-if="coverFiles.length"
                      type="button"
                      class="image-remove"
                      title="Bỏ ảnh"
                      @click="removeCoverFile(slot - 1)"
                  >
                    ×
                  </button>
                </template>
                <template v-else>
                  <i class="fa-regular fa-image"></i>
                  <span>Chưa chọn ảnh {{ slot }}</span>
                </template>
              </div>
            </div>

            <button
                type="button"
                class="upload-button"
                @click="openCoverPicker"
            >
              <i class="fa-solid fa-images"></i>
              {{ isEdit && !coverFiles.length ? 'Thay toàn bộ 3 ảnh' : 'Chọn đúng 3 ảnh' }}
            </button>

            <p v-if="isEdit && existingCoverImages.length !== 3 && !coverFiles.length" class="inline-warning">
              Combo cũ hiện có {{ existingCoverImages.length }} ảnh đại diện. Hãy chọn lại đúng 3 ảnh để lưu.
            </p>
          </article>

          <article class="image-panel process-panel">
            <div class="panel-heading">
              <div>
                <h3>Ảnh quy trình</h3>
                <p>Không bắt buộc, tối đa 20 ảnh; sắp xếp theo thứ tự đã chọn.</p>
              </div>
              <span class="count-pill neutral">
                {{ displayedProcessImages.length }}/20
              </span>
            </div>

            <input
                ref="processInput"
                class="hidden-input"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                @change="handleProcessSelect"
            />

            <div v-if="displayedProcessImages.length" class="process-grid">
              <div
                  v-for="(image, index) in displayedProcessImages"
                  :key="`${image}-${index}`"
                  class="process-image"
              >
                <img :src="image" :alt="`Ảnh quy trình ${index + 1}`" @error="useFallbackImage" />
                <span>{{ index + 1 }}</span>
                <button
                    v-if="processFiles.length"
                    type="button"
                    class="image-remove"
                    title="Bỏ ảnh"
                    @click="removeProcessFile(index)"
                >
                  ×
                </button>
              </div>
            </div>
            <div v-else class="empty-process">
              <i class="fa-solid fa-diagram-project"></i>
              <span>Chưa có ảnh quy trình</span>
            </div>

            <div class="image-actions">
              <button type="button" class="upload-button" @click="openProcessPicker">
                <i class="fa-solid fa-upload"></i>
                {{ isEdit && existingProcessImages.length && !processFiles.length
                  ? 'Thay toàn bộ ảnh quy trình'
                  : 'Chọn ảnh quy trình' }}
              </button>
              <button
                  v-if="displayedProcessImages.length"
                  type="button"
                  class="clear-button"
                  @click="clearProcessImages"
              >
                Xóa toàn bộ
              </button>
            </div>
          </article>
        </div>
      </section>

      <section class="editor-card product-section">
        <div class="card-title-row">
          <span class="step-badge product-step">3</span>
          <div>
            <h2>Chọn sản phẩm đưa vào combo</h2>
            <p>
              Loại sản phẩm chỉ dùng để chia nhóm. Admin vẫn phải chọn từng sản phẩm cụ thể.
            </p>
          </div>
        </div>

        <div class="product-toolbar">
          <label class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input
                v-model.trim="productKeyword"
                type="search"
                placeholder="Tìm theo tên, loại, vật liệu, màu sắc hoặc đối tác..."
            />
          </label>
          <div class="selection-summary">
            <strong>{{ selectedProducts.length }}</strong> sản phẩm đã chọn ·
            <strong>{{ totalSelectedQuantity }}</strong> đơn vị
          </div>
        </div>

        <div v-if="groupedProducts.length === 0" class="empty-products">
          Không có sản phẩm đang bán phù hợp với từ khóa.
        </div>

        <div v-else class="product-groups">
          <article
              v-for="group in groupedProducts"
              :key="group.name"
              class="product-group"
          >
            <button
                type="button"
                class="group-header"
                @click="toggleGroup(group.name)"
            >
              <span class="group-name">
                <i class="fa-solid fa-layer-group"></i>
                {{ group.name }}
              </span>
              <span class="group-meta">
                {{ selectedCountInGroup(group.items) }}/{{ group.items.length }} đã chọn
                <i
                    class="fa-solid fa-chevron-down"
                    :class="{ rotated: isGroupOpen(group.name) }"
                ></i>
              </span>
            </button>

            <div v-show="isGroupOpen(group.name)" class="group-products">
              <label
                  v-for="product in group.items"
                  :key="product.maSanPham"
                  class="product-row"
                  :class="{
                  selected: isSelected(product.maSanPham),
                  unavailable: !isProductAvailable(product)
                }"
              >
                <input
                    class="product-checkbox"
                    type="checkbox"
                    :checked="isSelected(product.maSanPham)"
                    :disabled="!isProductAvailable(product) && !isSelected(product.maSanPham)"
                    @change="toggleProduct(product, $event.target.checked)"
                />

                <img
                    class="product-image"
                    :src="normalizeImage(product.hinhAnh)"
                    :alt="product.tenSanPham"
                    @error="useFallbackImage"
                />

                <div class="product-main">
                  <div class="product-name-line">
                    <strong>{{ product.tenSanPham }}</strong>
                    <span>#{{ product.maSanPham }}</span>
                  </div>
                  <div class="product-tags">
                    <span><b>Vật liệu:</b> {{ product.vatLieu || 'Chưa cập nhật' }}</span>
                    <span><b>Màu:</b> {{ product.mauSac || 'Chưa cập nhật' }}</span>
                    <span><b>Đối tác:</b> {{ product.tenDoiTac || 'Không xác định' }}</span>
                  </div>
                  <div class="product-bottom">
                    <strong class="product-price">{{ formatMoney(product.giaTien) }}</strong>
                    <span :class="{ out: Number(product.soLuong || 0) <= 0 }">
                      Tồn kho: {{ product.soLuong ?? 0 }}
                    </span>
                    <span v-if="Number(product.trangThai) !== 1" class="out">
                      Không còn đang bán
                    </span>
                  </div>
                </div>

                <div v-if="isSelected(product.maSanPham)" class="quantity-box" @click.prevent.stop>
                  <span>Số lượng trong combo</span>
                  <div class="quantity-control">
                    <button
                        type="button"
                        :disabled="getQuantity(product) <= 1"
                        @click="changeQuantity(product, -1)"
                    >−</button>
                    <input
                        v-model.number="selectedQuantities[product.maSanPham]"
                        type="number"
                        min="1"
                        :max="Math.max(1, Number(product.soLuong || 0))"
                        @change="normalizeQuantity(product)"
                    />
                    <button
                        type="button"
                        :disabled="getQuantity(product) >= Number(product.soLuong || 0)"
                        @click="changeQuantity(product, 1)"
                    >+</button>
                  </div>
                  <strong>{{ formatMoney(lineTotal(product)) }}</strong>
                </div>
              </label>
            </div>
          </article>
        </div>
      </section>

      <section class="summary-card">
        <div class="summary-info">
          <p>Đã chọn <b>{{ selectedProducts.length }}</b> sản phẩm</p>
          <p>Tổng giá sản phẩm: <b>{{ formatMoney(totalProductPrice) }}</b></p>
          <p>Giá combo: <b>{{ formatMoney(form.gia) }}</b></p>
          <p>Ảnh đại diện: <b>{{ displayedCoverImages.length }}/3</b></p>
        </div>

        <div class="submit-actions">
          <RouterLink class="cancel-button" to="/admin/quan-ly-combo">
            Hủy
          </RouterLink>
          <button class="save-button" type="submit" :disabled="saving">
            <span v-if="saving" class="spinner-border spinner-border-sm"></span>
            <i v-else class="fa-solid fa-floppy-disk"></i>
            {{ saving ? 'Đang lưu...' : (isEdit ? 'Lưu thay đổi' : 'Tạo combo') }}
          </button>
        </div>
      </section>
    </form>
  </main>
</template>

<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref
} from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  createComboAdmin,
  getComboAdmin,
  getSanPhamComboAdmin,
  updateComboAdmin
} from "../../services/comboAdminService.js";

const route = useRoute();
const router = useRouter();

const isEdit = computed(() => Boolean(route.params.id));
const comboId = computed(() => Number(route.params.id));

const loading = ref(true);
const saving = ref(false);
const pageError = ref("");
const products = ref([]);
const productKeyword = ref("");
const openGroups = ref(new Set());

const form = reactive({
  tenCombo: "",
  gia: null,
  moTa: "",
  ghiChu: "",
  trangThai: 1
});

const selectedQuantities = reactive({});

const coverInput = ref(null);
const processInput = ref(null);
const coverFiles = ref([]);
const processFiles = ref([]);
const coverObjectUrls = ref([]);
const processObjectUrls = ref([]);
const existingCoverImages = ref([]);
const existingProcessImages = ref([]);
const replaceProcessImages = ref(false);

const MAX_FILE_SIZE = 5 * 1024 * 1024;
const ALLOWED_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

const fallbackImage =
    "data:image/svg+xml;charset=UTF-8," +
    encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="480" height="320">
      <rect width="100%" height="100%" fill="#f3f4f6"/>
      <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle"
        font-family="Arial" font-size="24" fill="#9ca3af">Chưa có ảnh</text>
    </svg>
  `);

const displayedCoverImages = computed(() =>
    coverFiles.value.length ? coverObjectUrls.value : existingCoverImages.value
);

const displayedProcessImages = computed(() =>
    processFiles.value.length ? processObjectUrls.value : existingProcessImages.value
);

const filteredProducts = computed(() => {
  const keyword = productKeyword.value.toLocaleLowerCase("vi");
  if (!keyword) return products.value;

  return products.value.filter((product) => [
    product.tenSanPham,
    product.loai,
    product.vatLieu,
    product.mauSac,
    product.tenDoiTac,
    product.maSanPham
  ].some((value) => String(value ?? "").toLocaleLowerCase("vi").includes(keyword)));
});

const groupedProducts = computed(() => {
  const map = new Map();
  filteredProducts.value.forEach((product) => {
    const groupName = String(product.loai || "Chưa phân loại").trim() || "Chưa phân loại";
    if (!map.has(groupName)) map.set(groupName, []);
    map.get(groupName).push(product);
  });

  return [...map.entries()]
      .sort(([a], [b]) => a.localeCompare(b, "vi"))
      .map(([name, items]) => ({ name, items }));
});

const selectedProducts = computed(() =>
    products.value.filter((product) => isSelected(product.maSanPham))
);

const totalSelectedQuantity = computed(() =>
    selectedProducts.value.reduce(
        (total, product) => total + getQuantity(product),
        0
    )
);

const totalProductPrice = computed(() =>
    selectedProducts.value.reduce(
        (total, product) => total + lineTotal(product),
        0
    )
);

const normalizeImage = (url) => {
  if (!url) return fallbackImage;
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  if (url.startsWith("//")) return `https:${url}`;
  if (url.startsWith("/")) return `http://localhost:8080${url}`;
  return `http://localhost:8080/${url}`;
};

const useFallbackImage = (event) => {
  event.target.onerror = null;
  event.target.src = fallbackImage;
};

const formatMoney = (value) =>
    `${Number(value || 0).toLocaleString("vi-VN")} đ`;

const isSelected = (productId) =>
    Object.prototype.hasOwnProperty.call(selectedQuantities, productId);

const isProductAvailable = (product) =>
    Number(product.trangThai) === 1 && Number(product.soLuong || 0) > 0;

const getQuantity = (product) =>
    Math.max(1, Number(selectedQuantities[product.maSanPham] || 1));

const lineTotal = (product) =>
    Number(product.giaTien || 0) * getQuantity(product);

const toggleProduct = (product, checked) => {
  if (checked) {
    if (!isProductAvailable(product)) return;
    selectedQuantities[product.maSanPham] = 1;
  } else {
    delete selectedQuantities[product.maSanPham];
  }
};

const normalizeQuantity = (product) => {
  const max = Math.max(1, Number(product.soLuong || 0));
  const value = Number(selectedQuantities[product.maSanPham] || 1);
  selectedQuantities[product.maSanPham] = Math.min(max, Math.max(1, value));
};

const changeQuantity = (product, delta) => {
  selectedQuantities[product.maSanPham] = getQuantity(product) + delta;
  normalizeQuantity(product);
};

const selectedCountInGroup = (items) =>
    items.filter((item) => isSelected(item.maSanPham)).length;

const isGroupOpen = (name) => openGroups.value.has(name);

const toggleGroup = (name) => {
  const next = new Set(openGroups.value);
  next.has(name) ? next.delete(name) : next.add(name);
  openGroups.value = next;
};

const validateFiles = (files, maxCount, label) => {
  if (files.length > maxCount) {
    throw new Error(`${label} chỉ được chọn tối đa ${maxCount} ảnh.`);
  }
  files.forEach((file) => {
    if (!ALLOWED_TYPES.has(file.type)) {
      throw new Error(`${file.name} không đúng định dạng JPG, PNG hoặc WEBP.`);
    }
    if (file.size > MAX_FILE_SIZE) {
      throw new Error(`${file.name} vượt quá 5 MB.`);
    }
  });
};

const revokeUrls = (urls) => {
  urls.forEach((url) => URL.revokeObjectURL(url));
};

const setCoverFiles = (files) => {
  validateFiles(files, 3, "Ảnh đại diện");
  revokeUrls(coverObjectUrls.value);
  coverFiles.value = files;
  coverObjectUrls.value = files.map((file) => URL.createObjectURL(file));
};

const setProcessFiles = (files) => {
  validateFiles(files, 20, "Ảnh quy trình");
  revokeUrls(processObjectUrls.value);
  processFiles.value = files;
  processObjectUrls.value = files.map((file) => URL.createObjectURL(file));
  replaceProcessImages.value = true;
};

const openCoverPicker = () => {
  if (coverInput.value) {
    coverInput.value.value = "";
    coverInput.value.click();
  }
};

const openProcessPicker = () => {
  if (processInput.value) {
    processInput.value.value = "";
    processInput.value.click();
  }
};

const handleCoverSelect = (event) => {
  pageError.value = "";
  try {
    setCoverFiles(Array.from(event.target.files || []));
  } catch (error) {
    event.target.value = "";
    pageError.value = error.message;
  }
};

const handleProcessSelect = (event) => {
  pageError.value = "";
  try {
    setProcessFiles(Array.from(event.target.files || []));
  } catch (error) {
    event.target.value = "";
    pageError.value = error.message;
  }
};

const removeCoverFile = (index) => {
  const next = coverFiles.value.filter((_, fileIndex) => fileIndex !== index);
  setCoverFiles(next);
};

const removeProcessFile = (index) => {
  const next = processFiles.value.filter((_, fileIndex) => fileIndex !== index);
  setProcessFiles(next);
};

const clearProcessImages = () => {
  revokeUrls(processObjectUrls.value);
  processFiles.value = [];
  processObjectUrls.value = [];
  existingProcessImages.value = [];
  replaceProcessImages.value = true;
};

const mergeComboProducts = (comboProducts) => {
  const currentIds = new Set(products.value.map((product) => product.maSanPham));
  const missing = (comboProducts || []).filter(
      (product) => !currentIds.has(product.maSanPham)
  );
  if (missing.length) products.value = [...products.value, ...missing];
};

const fillEditData = (combo) => {
  form.tenCombo = combo.tenCombo || "";
  form.gia = Number(combo.gia || 0);
  form.moTa = combo.moTa || "";
  form.ghiChu = combo.ghiChu || "";
  form.trangThai = Number(combo.trangThai ?? 1);
  existingCoverImages.value = (combo.hinhAnhDaiDiens || [])
      .map(normalizeImage)
      .filter(Boolean);
  existingProcessImages.value = (combo.hinhAnhQuyTrinhs || [])
      .map(normalizeImage)
      .filter(Boolean);

  mergeComboProducts(combo.sanPhams || []);
  (combo.sanPhams || []).forEach((product) => {
    selectedQuantities[product.maSanPham] = Math.max(
        1,
        Number(product.soLuongTrongCombo || 1)
    );
  });
};

const openAllProductGroups = () => {
  openGroups.value = new Set(
      products.value.map((product) => product.loai || "Chưa phân loại")
  );
};

const loadData = async () => {
  loading.value = true;
  pageError.value = "";
  try {
    const productResponse = await getSanPhamComboAdmin();
    products.value = Array.isArray(productResponse.data)
        ? productResponse.data
        : [];

    if (isEdit.value) {
      const comboResponse = await getComboAdmin(comboId.value);
      fillEditData(comboResponse.data);
    }

    openAllProductGroups();
  } catch (error) {
    pageError.value = extractError(error, "Không thể tải dữ liệu tạo combo.");
  } finally {
    loading.value = false;
  }
};

const validateForm = () => {
  if (!form.tenCombo.trim()) return "Vui lòng nhập tên combo.";
  if (Number(form.gia || 0) <= 0) return "Giá combo phải lớn hơn 0.";
  if (!selectedProducts.value.length) {
    return "Phải chọn ít nhất một sản phẩm cụ thể vào combo.";
  }
  const invalidQuantity = selectedProducts.value.find((product) => {
    const quantity = getQuantity(product);
    return quantity <= 0 || quantity > Number(product.soLuong || 0);
  });
  if (invalidQuantity) {
    return `Số lượng ${invalidQuantity.tenSanPham} không hợp lệ hoặc vượt tồn kho.`;
  }

  if (coverFiles.value.length) {
    if (coverFiles.value.length !== 3) return "Ảnh đại diện bắt buộc phải đúng 3 ảnh.";
  } else if (!isEdit.value || existingCoverImages.value.length !== 3) {
    return "Ảnh đại diện bắt buộc phải đúng 3 ảnh.";
  }
  return "";
};

const extractError = (error, fallback) =>
    error?.response?.data?.detail ||
    error?.response?.data?.message ||
    error?.message ||
    fallback;

const submitCombo = async () => {
  pageError.value = validateForm();
  if (pageError.value) {
    window.scrollTo({ top: 0, behavior: "smooth" });
    return;
  }

  saving.value = true;
  try {
    const payload = {
      tenCombo: form.tenCombo.trim(),
      gia: Number(form.gia),
      moTa: form.moTa?.trim() || null,
      ghiChu: form.ghiChu?.trim() || null,
      trangThai: Number(form.trangThai),
      thayAnhDaiDien: isEdit.value && coverFiles.value.length === 3,
      thayAnhQuyTrinh: isEdit.value && replaceProcessImages.value,
      sanPhams: selectedProducts.value.map((product) => ({
        maSanPham: Number(product.maSanPham),
        soLuong: getQuantity(product),
        noiDung: null
      }))
    };

    if (isEdit.value) {
      await updateComboAdmin(
          comboId.value,
          payload,
          coverFiles.value,
          processFiles.value
      );
    } else {
      await createComboAdmin(
          payload,
          coverFiles.value,
          processFiles.value
      );
    }

    await router.push({
      path: "/admin/quan-ly-combo",
      query: {
        saved: isEdit.value ? "updated" : "created"
      }
    });
  } catch (error) {
    pageError.value = extractError(error, "Không thể lưu combo.");
    window.scrollTo({ top: 0, behavior: "smooth" });
  } finally {
    saving.value = false;
  }
};

onMounted(loadData);

onBeforeUnmount(() => {
  revokeUrls(coverObjectUrls.value);
  revokeUrls(processObjectUrls.value);
});
</script>

<style scoped src="../../assets/styles/admin/QLCombo/ComboAdmin.css"></style>