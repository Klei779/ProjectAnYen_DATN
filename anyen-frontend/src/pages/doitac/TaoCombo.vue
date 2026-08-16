<template>
  <section class="create-combo-page">
    <form
        class="combo-form"
        @submit.prevent="submitCombo"
    >
      <!-- Thông tin combo -->
      <div class="form-card">
        <div class="card-heading">
          <span class="step-number">1</span>

          <div>
            <h2>Thông tin combo</h2>

            <p>
              Giá combo phải lớn hơn 0 và không vượt tổng giá sản phẩm
              theo số lượng.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <label class="form-group full-width">
            <span>Tên combo <b>*</b></span>

            <input
                :value="form.tenCombo"
                :maxlength="50"
                placeholder="Ví dụ: Gói tang lễ cơ bản"
                @input="handleSingleLineInput(
      'tenCombo',
      $event,
      50
    )"
            />
          </label>

          <label class="form-group">
            <span class="field-label">
              <span>Giá combo <b>*</b></span>

              <button
                  type="button"
                  class="use-total-button"
                  :disabled="!totalProductPrice"
                  @click="form.gia = totalProductPrice"
              >
                Dùng tổng giá
              </button>
            </span>

            <input
                v-model.number="form.gia"
                type="number"
                min="1"
                step="1"
                placeholder="Nhập giá combo"
            />

            <small
                v-if="priceExceedsTotal"
                class="field-error"
            >
              Giá combo đang lớn hơn tổng giá sản phẩm
              {{ formatMoney(totalProductPrice) }}.
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

          <!-- Chọn nhiều ảnh -->
          <div class="form-group full-width">
            <span>Hình ảnh combo</span>

            <div class="image-upload-box">
              <input
                  ref="imageInput"
                  class="image-file-input"
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  multiple
                  @change="handleImageSelect"
              />

              <button
                  type="button"
                  class="select-image-button"
                  :disabled="selectedImages.length >= MAX_IMAGES"
                  @click="openImageSelector"
              >
                Chọn ảnh
              </button>

              <span class="selected-image-count">
                Đã chọn {{ selectedImages.length }}/{{ MAX_IMAGES }} ảnh
              </span>
            </div>

            <small class="image-note">
              Tối đa 20 ảnh. Mỗi ảnh không vượt quá 5 MB.
              Hỗ trợ JPG, PNG và WEBP.
            </small>

            <!-- Chỉ hiển thị tên file, không xem trước -->
            <div
                v-if="selectedImages.length > 0"
                class="selected-file-list"
            >
              <div
                  v-for="(item, index) in selectedImages"
                  :key="item.key"
                  class="selected-file-row"
              >
                <span class="file-order">
                  {{ index + 1 }}
                </span>

                <span
                    class="selected-file-path"
                    :title="item.file.name"
                >
                  {{ item.file.webkitRelativePath || item.file.name }}
                </span>

                <span class="selected-file-size">
                  {{ formatFileSize(item.file.size) }}
                </span>

                <button
                    type="button"
                    class="remove-file-button"
                    title="Xóa ảnh"
                    @click="removeSelectedImage(index)"
                >
                  −
                </button>
              </div>
            </div>
          </div>

          <label class="form-group full-width">
            <span>Mô tả combo</span>

            <textarea
                :value="form.moTa"
                rows="4"
                maxlength="220"
                class="fixed-four-lines"
                placeholder="Nhập mô tả ngắn cho combo"
                @input="handleFourLineInput(
      'moTa',
      $event,
      220
    )"
            ></textarea>
          </label>

          <div class="form-group full-width">
            <label for="ghiChu">
              Quyền lợi của combo
            </label>

            <textarea
                id="ghiChu"
                :value="form.ghiChu"
                rows="4"
                maxlength="220"
                class="fixed-four-lines"
                placeholder="Mỗi quyền lợi nhập trên một dòng"
                @input="handleFourLineInput(
      'ghiChu',
      $event,
      220
    )"
            ></textarea>

            <small>
              Ví dụ: Tư vấn hỗ trợ 24/7, xe đưa đón, nhân sự phục vụ...
            </small>
          </div>
        </div>
      </div>

      <!-- Chọn sản phẩm -->
      <div class="form-card product-card">
        <div class="card-heading">
          <span class="step-number product-step">2</span>

          <div>
            <h2>Chọn sản phẩm và số lượng</h2>

            <p>
              Mỗi sản phẩm được chọn một lần,
              số lượng không được vượt tồn kho.
            </p>
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
            {{ selectedProducts.length }} sản phẩm ·
            {{ totalSelectedQuantity }} đơn vị
          </span>
        </div>

        <div
            v-if="loadingProducts"
            class="empty-products"
        >
          Đang tải sản phẩm...
        </div>

        <div
            v-else
            class="product-list"
        >
          <article
              v-for="product in filteredProducts"
              :key="product.maSanPham"
              class="product-item"
              :class="{
                selected: isSelected(product.maSanPham),
                unavailable: Number(product.soLuong || 0) <= 0
              }"
          >
            <input
                type="checkbox"
                :checked="isSelected(product.maSanPham)"
                :disabled="Number(product.soLuong || 0) <= 0"
                @change="event => toggleProduct(
                  product,
                  event.target.checked
                )"
            />

            <img
                :src="product.hinhAnh || fallbackImage"
                :alt="product.tenSanPham"
                @error="useFallbackImage"
            />

            <div class="product-info">
              <strong>
                {{ product.tenSanPham }}
              </strong>

              <p>
                Mã SP: {{ product.maSanPham }} · Đã duyệt
              </p>

              <div class="product-meta">
                <span>
                  {{ formatMoney(product.giaTien) }}
                </span>

                <small
                    :class="{
                      out: Number(product.soLuong || 0) <= 0
                    }"
                >
                  Tồn kho {{ product.soLuong ?? 0 }}
                </small>
              </div>
            </div>

            <div
                v-if="isSelected(product.maSanPham)"
                class="quantity-editor"
                @click.stop
            >
              <span>Số lượng</span>

              <div class="quantity-control">
                <button
                    type="button"
                    :disabled="getQuantity(product) <= 1"
                    @click="changeQuantity(product, -1)"
                >
                  −
                </button>

                <input
                    v-model.number="
                      selectedQuantities[product.maSanPham]
                    "
                    type="number"
                    min="1"
                    :max="
                      Math.max(
                        1,
                        Number(product.soLuong || 0)
                      )
                    "
                    @change="normalizeQuantity(product)"
                />

                <button
                    type="button"
                    :disabled="
                      getQuantity(product)
                      >= Number(product.soLuong || 0)
                    "
                    @click="changeQuantity(product, 1)"
                >
                  +
                </button>
              </div>

              <strong>
                {{ formatMoney(lineTotal(product)) }}
              </strong>
            </div>
          </article>

          <div
              v-if="filteredProducts.length === 0"
              class="empty-products"
          >
            Không có sản phẩm đã duyệt phù hợp.
            Hãy tạo sản phẩm và chờ nhân viên duyệt trước.
          </div>
        </div>
      </div>

      <!-- Tổng kết -->
      <div class="summary-card">
        <div>
          <p>Loại sản phẩm</p>
          <strong>{{ selectedProducts.length }}</strong>
        </div>

        <div>
          <p>Tổng số lượng</p>
          <strong>{{ totalSelectedQuantity }}</strong>
        </div>

        <div>
          <p>Tổng giá sản phẩm</p>
          <strong>
            {{ formatMoney(totalProductPrice) }}
          </strong>
        </div>

        <div>
          <p>Giá combo</p>
          <strong class="combo-price">
            {{ formatMoney(form.gia || 0) }}
          </strong>
        </div>

        <div>
          <p>Khách hàng tiết kiệm</p>
          <strong class="saving-price">
            {{ formatMoney(savingAmount) }}
          </strong>
        </div>
      </div>

      <footer class="form-actions">
        <button
            class="btn btn-reset"
            type="button"
            :disabled="submitting"
            @click="resetForm"
        >
          Nhập lại
        </button>

        <button
            class="btn btn-submit"
            type="submit"
            :disabled="
              submitting
              || loadingProducts
              || priceExceedsTotal
            "
        >
          {{ submitting ? "Đang tạo..." : "Tạo combo" }}
        </button>
      </footer>
    </form>
    <!--    <Teleport to="body">-->
    <!--      <div-->
    <!--          v-if="showDetailPopup && activeDetailProduct"-->
    <!--          class="detail-popup-overlay"-->
    <!--          @click.self="closeDetailPopup"-->
    <!--      >-->
    <!--        <section class="detail-popup">-->
    <!--          <header class="detail-popup-header">-->
    <!--            <div>-->
    <!--              <span>NỘI DUNG CHI TIẾT GÓI</span>-->

    <!--              <h3>-->
    <!--                {{ activeDetailProduct.tenSanPham }}-->
    <!--              </h3>-->

    <!--              <p>-->
    <!--                Nhập mô tả và chọn hình ảnh hiển thị-->
    <!--                khi khách hàng xem chi tiết dịch vụ.-->
    <!--              </p>-->
    <!--            </div>-->

    <!--            <button-->
    <!--                type="button"-->
    <!--                class="detail-popup-close"-->
    <!--                @click="closeDetailPopup"-->
    <!--            >-->
    <!--              ×-->
    <!--            </button>-->
    <!--          </header>-->

    <!--          <div class="detail-popup-body">-->
    <!--            &lt;!&ndash; MÔ TẢ &ndash;&gt;-->
    <!--            <label class="popup-description">-->
    <!--              <span>Mô tả chi tiết</span>-->

    <!--              <textarea-->
    <!--                  v-model="-->
    <!--                detailDescriptions[-->
    <!--                  activeDetailProduct.maSanPham-->
    <!--                ]-->
    <!--              "-->
    <!--                  rows="7"-->
    <!--                  maxlength="5000"-->
    <!--                  placeholder="Ví dụ: Sản phẩm được chuẩn bị trang nghiêm, phù hợp với từng nghi thức..."-->
    <!--              ></textarea>-->
    <!--            </label>-->

    <!--            &lt;!&ndash; HÌNH ẢNH &ndash;&gt;-->
    <!--            <div class="popup-images">-->
    <!--              <div class="popup-image-toolbar">-->
    <!--                <div>-->
    <!--                  <strong>Hình ảnh chi tiết</strong>-->

    <!--                  <small>-->
    <!--                    Đã chọn-->
    <!--                    {{-->
    <!--                      detailImages[-->
    <!--                          activeDetailProduct.maSanPham-->
    <!--                          ]?.length || 0-->
    <!--                    }}-->
    <!--                    / {{ MAX_DETAIL_IMAGES }} ảnh-->
    <!--                  </small>-->
    <!--                </div>-->

    <!--                <label class="popup-select-image">-->
    <!--                  Chọn hình ảnh-->

    <!--                  <input-->
    <!--                      type="file"-->
    <!--                      accept="image/jpeg,image/png,image/webp"-->
    <!--                      multiple-->
    <!--                      hidden-->
    <!--                      @change="-->
    <!--                    event => handleDetailImageSelect(-->
    <!--                      activeDetailProduct,-->
    <!--                      event-->
    <!--                    )-->
    <!--                  "-->
    <!--                  />-->
    <!--                </label>-->
    <!--              </div>-->

    <!--              <div-->
    <!--                  v-if="-->
    <!--                detailImages[-->
    <!--                  activeDetailProduct.maSanPham-->
    <!--                ]?.length-->
    <!--              "-->
    <!--                  class="popup-image-grid"-->
    <!--              >-->
    <!--                <article-->
    <!--                    v-for="(image, index) in-->
    <!--                  detailImages[-->
    <!--                    activeDetailProduct.maSanPham-->
    <!--                  ]"-->
    <!--                    :key="image.key"-->
    <!--                    class="popup-image-card"-->
    <!--                >-->
    <!--                  <img-->
    <!--                      :src="image.preview || fallbackImage"-->
    <!--                      :alt="image.file.name"-->
    <!--                  />-->

    <!--                  <span :title="image.file.name">-->
    <!--                {{ image.file.name }}-->
    <!--              </span>-->

    <!--                  <button-->
    <!--                      type="button"-->
    <!--                      @click="-->
    <!--                    removeDetailImage(-->
    <!--                      activeDetailProduct.maSanPham,-->
    <!--                      index-->
    <!--                    )-->
    <!--                  "-->
    <!--                  >-->
    <!--                    ×-->
    <!--                  </button>-->
    <!--                </article>-->
    <!--              </div>-->

    <!--              <div-->
    <!--                  v-else-->
    <!--                  class="popup-image-empty"-->
    <!--              >-->
    <!--                Hình ảnh được chọn sẽ hiển thị tại đây.-->
    <!--              </div>-->
    <!--            </div>-->
    <!--          </div>-->

    <!--          <footer class="detail-popup-footer">-->
    <!--            <button-->
    <!--                type="button"-->
    <!--                @click="closeDetailPopup"-->
    <!--            >-->
    <!--              Hoàn tất-->
    <!--            </button>-->
    <!--          </footer>-->
    <!--        </section>-->
    <!--      </div>-->
    <!--    </Teleport>-->
  </section>
</template>

<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref
} from "vue";

import {useRouter} from "vue-router";
import {ElMessage} from "element-plus";

import {
  createComboDoiTac,
  getSanPhamComboDoiTac
} from "../../services/comboDoiTacService.js";

const router = useRouter();

const products = ref([]);
const selectedProductIds = ref([]);
const selectedQuantities = reactive({});
const detailDescriptions = reactive({});
const detailImages = reactive({});
const showDetailPopup = ref(false);
const activeDetailProduct = ref(null);

const MAX_DETAIL_IMAGES = 20;

const productKeyword = ref("");
const loadingProducts = ref(false);
const submitting = ref(false);

const MAX_IMAGES = 20;
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

const imageInput = ref(null);
const selectedImages = ref([]);

const form = reactive({
  tenCombo: "",
  gia: "",
  moTa: "",
  ghiChu: "",
  trangThai: 1
});

const fallbackImage =
    "data:image/svg+xml;charset=UTF-8,"
    + encodeURIComponent(`
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="500"
        height="350"
      >
        <rect
          width="100%"
          height="100%"
          fill="#eef2f7"
        />

        <text
          x="50%"
          y="50%"
          dominant-baseline="middle"
          text-anchor="middle"
          fill="#6b7280"
          font-family="Arial"
          font-size="24"
        >
          Chưa có hình ảnh
        </text>
      </svg>
    `);

const filteredProducts = computed(() => {
  const keyword =
      productKeyword.value.trim().toLowerCase();

  if (!keyword) {
    return products.value;
  }

  return products.value.filter(product =>
      (product.tenSanPham || "")
          .toLowerCase()
          .includes(keyword)
      || String(product.maSanPham || "")
          .includes(keyword)
  );
});

const selectedProducts = computed(() =>
    products.value
        .filter(product =>
            selectedProductIds.value.includes(
                product.maSanPham
            )
        )
        .map(product => ({
          ...product,
          soLuongTrongCombo: getQuantity(product)
        }))
);

const totalSelectedQuantity = computed(() =>
    selectedProducts.value.reduce(
        (total, product) =>
            total + product.soLuongTrongCombo,
        0
    )
);

const totalProductPrice = computed(() =>
    selectedProducts.value.reduce(
        (total, product) =>
            total
            + Number(product.giaTien || 0)
            * product.soLuongTrongCombo,
        0
    )
);

const priceExceedsTotal = computed(() =>
    totalProductPrice.value > 0
    && Number(form.gia || 0) > totalProductPrice.value
);

const savingAmount = computed(() =>
    Math.max(
        0,
        totalProductPrice.value - Number(form.gia || 0)
    )
);

onMounted(async () => {
  document.body.style.overflow = "";
  await loadProducts();
});

async function loadProducts() {
  loadingProducts.value = true;

  try {
    const response =
        await getSanPhamComboDoiTac();

    products.value =
        Array.isArray(response.data)
            ? response.data
            : [];
  } catch (error) {
    console.error(
        "Lỗi tải sản phẩm:",
        error
    );

    ElMessage.error(
        getErrorMessage(
            error,
            "Không tải được sản phẩm của đối tác"
        )
    );
  } finally {
    loadingProducts.value = false;
  }
}

function openImageSelector() {
  if (selectedImages.value.length >= MAX_IMAGES) {
    ElMessage.warning(
        "Chỉ được chọn tối đa 20 ảnh"
    );
    return;
  }

  imageInput.value?.click();
}

function handleImageSelect(event) {
  const pickedFiles =
      Array.from(event.target.files || []);

  if (!pickedFiles.length) {
    return;
  }

  const remainingSlots =
      MAX_IMAGES - selectedImages.value.length;

  if (remainingSlots <= 0) {
    ElMessage.warning(
        "Chỉ được chọn tối đa 20 ảnh"
    );

    event.target.value = "";
    return;
  }

  if (pickedFiles.length > remainingSlots) {
    ElMessage.warning(
        `Bạn chỉ có thể chọn thêm ${remainingSlots} ảnh`
    );
  }

  const allowedTypes = [
    "image/jpeg",
    "image/png",
    "image/webp"
  ];

  const acceptedFiles = pickedFiles
      .slice(0, remainingSlots)
      .filter(file => {
        if (!allowedTypes.includes(file.type)) {
          ElMessage.warning(
              `${file.name}: chỉ hỗ trợ JPG, PNG hoặc WEBP`
          );

          return false;
        }

        if (file.size > MAX_IMAGE_SIZE) {
          ElMessage.warning(
              `${file.name}: dung lượng vượt quá 5 MB`
          );

          return false;
        }

        const duplicated =
            selectedImages.value.some(item =>
                item.file.name === file.name
                && item.file.size === file.size
                && item.file.lastModified
                === file.lastModified
            );

        if (duplicated) {
          ElMessage.warning(
              `${file.name}: ảnh này đã được chọn`
          );

          return false;
        }

        return true;
      });

  const newImages = acceptedFiles.map(file => ({
    file,
    key:
        `${file.name}-${file.size}-${file.lastModified}`
  }));

  selectedImages.value.push(...newImages);

  // Cho phép chọn lại một file sau khi đã xóa
  event.target.value = "";
}

function removeSelectedImage(index) {
  if (
      index < 0
      || index >= selectedImages.value.length
  ) {
    return;
  }

  selectedImages.value.splice(index, 1);
}

function handleDetailImageSelect(
    product,
    event
) {
  const productId = product.maSanPham;

  const pickedFiles = Array.from(
      event.target.files || []
  );

  if (!pickedFiles.length) {
    return;
  }

  if (!detailImages[productId]) {
    detailImages[productId] = [];
  }

  const currentImages =
      detailImages[productId];

  const remainingSlots =
      MAX_DETAIL_IMAGES - currentImages.length;

  if (remainingSlots <= 0) {
    ElMessage.warning(
        `Mỗi mục chi tiết chỉ được tối đa ${MAX_DETAIL_IMAGES} ảnh`
    );

    event.target.value = "";
    return;
  }

  const allowedTypes = [
    "image/jpeg",
    "image/png",
    "image/webp"
  ];

  const acceptedFiles = pickedFiles
      .slice(0, remainingSlots)
      .filter(file => {
        if (!allowedTypes.includes(file.type)) {
          ElMessage.warning(
              `${file.name}: chỉ hỗ trợ JPG, PNG hoặc WEBP`
          );

          return false;
        }

        if (file.size > MAX_IMAGE_SIZE) {
          ElMessage.warning(
              `${file.name}: dung lượng vượt quá 5 MB`
          );

          return false;
        }

        const duplicated = currentImages.some(
            item =>
                item.file.name === file.name
                && item.file.size === file.size
                && item.file.lastModified
                === file.lastModified
        );

        if (duplicated) {
          ElMessage.warning(
              `${file.name}: ảnh này đã được chọn`
          );

          return false;
        }

        return true;
      });

  const newImages = acceptedFiles.map(
      file => ({
        file,
        preview: URL.createObjectURL(file),

        key:
            `detail-${productId}-`
            + `${file.name}-${file.size}-`
            + `${file.lastModified}`
      })
  );

  currentImages.push(...newImages);

  event.target.value = "";
}

function removeDetailImage(productId, imageIndex) {
  const images = detailImages[productId];

  if (
      !Array.isArray(images)
      || imageIndex < 0
      || imageIndex >= images.length
  ) {
    return;
  }

  const removed = images[imageIndex];

  if (removed?.preview) {
    URL.revokeObjectURL(removed.preview);
  }

  images.splice(imageIndex, 1);
}

function openDetailPopup(product) {
  const productId = product?.maSanPham;

  if (!productId) {
    return;
  }

  if (detailDescriptions[productId] === undefined) {
    detailDescriptions[productId] = "";
  }

  if (!Array.isArray(detailImages[productId])) {
    detailImages[productId] = [];
  }

  activeDetailProduct.value = product;
  showDetailPopup.value = true;

  document.body.style.overflow = "hidden";
}

function closeDetailPopup() {
  showDetailPopup.value = false;
  activeDetailProduct.value = null;

  document.body.style.overflow = "";
}

function isSelected(productId) {
  return selectedProductIds.value.includes(productId);
}

function toggleProduct(product, checked) {
  const productId = product.maSanPham;

  if (checked && !isSelected(productId)) {
    selectedProductIds.value.push(productId);
    selectedQuantities[productId] = 1;

    // Có thể giữ dữ liệu này để code submit không lỗi
    detailDescriptions[productId] = "";
    detailImages[productId] = [];

    // Không mở popup nữa
    return;
  }

  if (!checked) {
    selectedProductIds.value =
        selectedProductIds.value.filter(
            id => id !== productId
        );

    const images = detailImages[productId] || [];

    images.forEach(item => {
      if (item.preview) {
        URL.revokeObjectURL(item.preview);
      }
    });

    delete selectedQuantities[productId];
    delete detailDescriptions[productId];
    delete detailImages[productId];
  }
}

function getQuantity(product) {
  const stock = Math.max(
      0,
      Number(product.soLuong || 0)
  );

  const current = Number(
      selectedQuantities[product.maSanPham] || 1
  );

  return Math.min(
      Math.max(1, Math.trunc(current)),
      Math.max(1, stock)
  );
}

function normalizeQuantity(product) {
  selectedQuantities[product.maSanPham] =
      getQuantity(product);
}

function changeQuantity(product, delta) {
  selectedQuantities[product.maSanPham] =
      getQuantity(product) + delta;

  normalizeQuantity(product);
}

function lineTotal(product) {
  return Number(product.giaTien || 0)
      * getQuantity(product);
}

function escapeHtml(value) {
  return String(value || "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
}

function convertGhiChuToHtml(value) {
  const items = String(value || "")
      .split(/\r?\n/)
      .map(item => item.trim())
      .filter(Boolean);

  if (items.length === 0) {
    return null;
  }

  return `<ul>${items
      .map(item => `<li>${escapeHtml(item)}</li>`)
      .join("")}</ul>`;
}

/*
 * Input 1 dòng.
 *
 * - Tối đa maxLength ký tự.
 * - Nếu chữ đã vượt chiều rộng thật của ô input
 *   thì trả lại giá trị trước đó.
 * - Không cho input chạy ngang.
 */
function handleSingleLineInput(
    field,
    event,
    maxLength
) {
  const element = event.target;

  const previousValue =
      String(form[field] || "");

  const nextValue =
      String(element.value || "")
          .slice(0, maxLength);

  /*
   * Giá trị mới đang nằm trực tiếp trong input,
   * nên có thể kiểm tra scrollWidth ngay.
   */
  if (
      element.scrollWidth >
      element.clientWidth + 2
  ) {
    element.value = previousValue;
    return;
  }

  form[field] = nextValue;
}


/*
 * Textarea cố định khoảng 4 dòng.
 *
 * Nếu text:
 * - tự wrap thành dòng thứ 5
 * - hoặc người dùng Enter sang dòng thứ 5
 *
 * => không nhận ký tự đó.
 */
function handleFourLineInput(
    field,
    event,
    maxLength
) {
  const element = event.target;

  const previousValue =
      String(form[field] || "");

  const nextValue =
      String(element.value || "")
          .slice(0, maxLength);

  /*
   * clientHeight = chiều cao cố định của textarea.
   * scrollHeight > clientHeight
   * nghĩa là nội dung đã muốn tạo thêm dòng.
   */
  if (
      element.scrollHeight >
      element.clientHeight + 2
  ) {
    element.value = previousValue;
    return;
  }

  form[field] = nextValue;
}

async function submitCombo() {
  if (form.tenCombo.trim().length < 3) {
    ElMessage.warning(
        "Tên combo phải có ít nhất 3 ký tự"
    );
    return;
  }

  if (form.tenCombo.trim().length > 50) {
    ElMessage.warning(
        "Tên combo không được vượt quá 50 ký tự"
    );
    return;
  }

  if (form.moTa.length > 220) {
    ElMessage.warning(
        "Mô tả combo không được vượt quá 220 ký tự"
    );
    return;
  }

  if (form.ghiChu.length > 220) {
    ElMessage.warning(
        "Quyền lợi combo không được vượt quá 220 ký tự"
    );
    return;
  }

  if (Number(form.gia || 0) <= 0) {
    ElMessage.warning(
        "Giá combo phải lớn hơn 0"
    );
    return;
  }

  if (!selectedProducts.value.length) {
    ElMessage.warning(
        "Vui lòng chọn ít nhất một sản phẩm"
    );
    return;
  }

  if (selectedImages.value.length > MAX_IMAGES) {
    ElMessage.warning(
        "Combo chỉ được chọn tối đa 20 ảnh"
    );
    return;
  }

  const invalidProduct =
      selectedProducts.value.find(product =>
          product.soLuongTrongCombo <= 0
          || product.soLuongTrongCombo
          > Number(product.soLuong || 0)
      );

  if (invalidProduct) {
    ElMessage.warning(
        `Số lượng ${invalidProduct.tenSanPham}`
        + " không hợp lệ hoặc vượt tồn kho"
    );
    return;
  }

  if (priceExceedsTotal.value) {
    ElMessage.warning(
        "Giá combo không được lớn hơn "
        + "tổng giá sản phẩm theo số lượng"
    );
    return;
  }

  submitting.value = true;

  try {
    const comboData = {
      tenCombo: form.tenCombo.trim(),
      gia: Number(form.gia),
      moTa: form.moTa.trim(),

      ghiChu: convertGhiChuToHtml(
          form.ghiChu
      ),

      trangThai: Number(form.trangThai),

      sanPhams: selectedProducts.value.map(
          product => ({
            maSanPham: product.maSanPham,
            soLuong: product.soLuongTrongCombo,

            /*
             * Lưu vào combochitiet.NoiDung.
             */
            noiDung:
                detailDescriptions[
                    product.maSanPham
                    ]?.trim() || product.tenSanPham
          })
      )
    };

    const multipartData = new FormData();

    multipartData.append(
        "data",
        new Blob(
            [JSON.stringify(comboData)],
            {
              type: "application/json"
            }
        )
    );

    // Tất cả file dùng chung tên part "files"
    selectedImages.value.forEach(item => {
      multipartData.append(
          "files",
          item.file
      );
    });

    /*
 * Ảnh chi tiết được nhóm theo mã sản phẩm.
 *
 * Ví dụ:
 * detailFiles_5
 * detailFiles_8
 */
    selectedProducts.value.forEach(product => {
      const productId = product.maSanPham;

      const images =
          detailImages[productId] || [];

      images.forEach(item => {
        multipartData.append(
            `detailFiles_${productId}`,
            item.file
        );
      });
    });

    await createComboDoiTac(multipartData);

    ElMessage.success(
        "Tạo combo thành công"
    );

    await router.push(
        "/doi-tac/quan-ly-combo"
    );
  } catch (error) {
    ElMessage.error(
        getErrorMessage(
            error,
            "Không thể tạo combo"
        )
    );
  } finally {
    submitting.value = false;
  }
}

function resetForm() {
  form.tenCombo = "";
  form.gia = 0;
  form.moTa = "";
  form.ghiChu = "";
  form.trangThai = 1;

  productKeyword.value = "";
  selectedProductIds.value = [];
  selectedImages.value = [];

  Object.keys(selectedQuantities).forEach(
      key => delete selectedQuantities[key]
  );

  if (imageInput.value) {
    imageInput.value.value = "";
  }
}

function formatMoney(value) {
  return new Intl.NumberFormat(
      "vi-VN",
      {
        style: "currency",
        currency: "VND",
        maximumFractionDigits: 0
      }
  ).format(Number(value || 0));
}

function formatFileSize(size) {
  const bytes = Number(size || 0);

  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(
      bytes / (1024 * 1024)
  ).toFixed(1)} MB`;
}

function useFallbackImage(event) {
  event.target.src = fallbackImage;
}

function getErrorMessage(error, fallback) {
  const responseData = error?.response?.data;

  if (typeof responseData === "string") {
    return responseData;
  }

  return responseData?.message
      || responseData?.error
      || fallback;
}
</script>

<style
    scoped
    src="../../assets/styles/doitac/QLcombo/TaoCombo.css"
></style>

<style scoped>
.manage-link {
  display: inline-flex;
  align-items: center;
  padding: 10px 16px;
  border: 1px solid #f0b4ad;
  border-radius: 10px;
  background: #fff;
  color: #b42318;
  font-weight: 700;
  text-decoration: none;
}

.form-group select {
  width: 100%;
  min-height: 44px;
  padding: 0 12px;
  border: 1px solid #dfe3e8;
  border-radius: 9px;
  background: #fff;
}

.btn:disabled,
.use-total-button:disabled,
.select-image-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* Upload ảnh */

.image-upload-box {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.image-file-input {
  display: none;
}

.select-image-button {
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid #f0b4ad;
  border-radius: 9px;
  background: #fff;
  color: #b42318;
  font-weight: 700;
  cursor: pointer;
}

.select-image-button:not(:disabled):hover {
  border-color: #e58c82;
  background: #fff5f4;
}

.selected-image-count {
  color: #4b5563;
  font-size: 14px;
  font-weight: 600;
}

.image-note {
  display: block;
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
}

/* Danh sách tên file */

.selected-file-list {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.selected-file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 7px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 9px;
  background: #f9fafb;
}

.file-order {
  flex: 0 0 auto;
  width: 25px;
  height: 25px;
  display: inline-grid;
  place-items: center;
  border-radius: 50%;
  background: #e5e7eb;
  color: #374151;
  font-size: 12px;
  font-weight: 700;
}

.selected-file-path {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #374151;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-file-size {
  flex: 0 0 auto;
  color: #6b7280;
  font-size: 12px;
  white-space: nowrap;
}

.remove-file-button {
  flex: 0 0 auto;
  width: 28px;
  height: 28px;
  display: inline-grid;
  place-items: center;
  padding: 0;
  border: 1px solid #fecaca;
  border-radius: 50%;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
}

.remove-file-button:hover {
  border-color: #fca5a5;
  background: #fee2e2;
}

@media (max-width: 640px) {
  .selected-file-row {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .selected-file-path {
    flex-basis: calc(100% - 75px);
  }

  .selected-file-size {
    margin-left: 35px;
  }
}
</style>