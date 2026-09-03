<script setup>
import { ref, watch } from "vue";

const visible = defineModel({
  type: Boolean,
  default: false,
});

const props = defineProps({
  product: {
    type: Object,
    default: null,
  },
  loadingAction: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["approve", "reject", "closed"]);

const tabs = [
  { id: "description", label: "MÔ TẢ SẢN PHẨM" },
  { id: "specs", label: "THÔNG SỐ KỸ THUẬT" },
  { id: "partner", label: "THÔNG TIN ĐỐI TÁC" },
  { id: "storage", label: "HƯỚNG DẪN BẢO QUẢN" },
];

const activeTab = ref("description");

watch(visible, (isOpen) => {
  if (isOpen) {
    activeTab.value = "description";
  }
});

const formatCurrency = (value) => {
  if (value === null || value === undefined || value === "") return "Liên hệ";

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(value));
};

const formatDateTime = (value) => {
  if (!value) return "N/A";

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString("vi-VN");
};

const getImageUrl = (path) => {
  if (!path) return "";
  if (path.startsWith("http") || path.startsWith("blob:")) return path;
  if (path.startsWith("/")) return `http://localhost:8080${path}`;
  return `http://localhost:8080/uploads/${path}`;
};

const closePopup = () => {
  if (props.loadingAction) return;
  visible.value = false;
};

const beforeClose = (done) => {
  if (!props.loadingAction) done();
};

const approveProduct = () => {
  if (!props.product || props.loadingAction) return;
  emit("approve", props.product);
};

const rejectProduct = () => {
  if (!props.product || props.loadingAction) return;
  emit("reject", props.product);
};
</script>

<template>
  <el-dialog
      v-model="visible"
      width="95%"
      top="2vh"
      class="custom-product-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="!loadingAction"
      :before-close="beforeClose"
      :z-index="10000"
      append-to-body
      destroy-on-close
      @closed="emit('closed')"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-title-group">
          <div class="dialog-icon">
            <i class="fa-solid fa-box-open"></i>
          </div>

          <div>
            <div class="dialog-title">Xem chi tiết sản phẩm</div>
            <div class="dialog-subtitle">
              Bản xem trước sản phẩm do đối tác gửi duyệt
              <span v-if="product?.id"> · ID #{{ product.id }}</span>
            </div>
          </div>
        </div>

        <button
            type="button"
            class="dialog-close"
            aria-label="Đóng"
            :disabled="!!loadingAction"
            @click="closePopup"
        >
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>
    </template>

    <div v-if="product" class="product-dialog-scroll">
      <section class="product-detail-section">
        <div class="product-detail-layout">
          <div class="product-gallery">
            <div v-if="product.image" class="main-image">
              <img :src="getImageUrl(product.image)" :alt="product.name || 'Ảnh sản phẩm'" />
            </div>

            <div v-else class="main-image image-empty">
              <i class="fa-regular fa-image"></i>
              <span>Sản phẩm chưa có hình ảnh</span>
            </div>
          </div>

          <div class="product-info-panel">
            <div>
              <div class="preview-label">CHẾ ĐỘ XEM TRƯỚC</div>
              <h1 class="product-title">{{ product.name || "Chưa có tên sản phẩm" }}</h1>
            </div>

            <div class="product-code">
              <span>Mã sản phẩm: </span>
              <strong>{{ product.code || `SP${product.id || "00000"}` }}</strong>
            </div>

            <div class="product-price-section">
              <div class="current-price">{{ formatCurrency(product.price) }}</div>
              <div v-if="product.oldPrice" class="old-price">
                {{ formatCurrency(product.oldPrice) }}
              </div>
              <div class="approval-badge">
                {{ product.tenTrangThai || product.trangThai || "Chờ duyệt" }}
              </div>
            </div>

            <div class="product-meta">
              <div class="meta-row">
                <span class="meta-label">Loại sản phẩm:</span>
                <span class="meta-value">{{ product.loai || "N/A" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Chất liệu:</span>
                <span class="meta-value">{{ product.vatLieu || "N/A" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Quy cách:</span>
                <span class="meta-value">{{ product.quyCach || "N/A" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Tôn giáo:</span>
                <span class="meta-value">{{ product.tonGiao || "Không phân biệt" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Màu sắc:</span>
                <span class="meta-value">{{ product.mauSac || "N/A" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Kích thước:</span>
                <span class="meta-value">{{ product.kichThuoc || "N/A" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Trọng lượng:</span>
                <span class="meta-value">{{ product.trongLuong || "N/A" }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">Số lượng:</span>
                <span class="meta-value">{{ product.soLuong ?? 0 }} sản phẩm</span>
              </div>
              <div class="meta-row meta-row-full">
                <span class="meta-label">Đối tác gửi duyệt:</span>
                <span class="meta-value partner-name">
                  {{ product.tenDoiTac || product.nhaCungCap || "N/A" }}
                </span>
              </div>
              <div class="meta-row meta-row-full">
                <span class="meta-label">Ngày gửi duyệt:</span>
                <span class="meta-value">{{ formatDateTime(product.ngayTao) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="product-tabs-section">
        <div class="tabs-container">
          <div class="tabs-header">
            <button
                v-for="tab in tabs"
                :key="tab.id"
                type="button"
                class="tab-btn"
                :class="{ active: activeTab === tab.id }"
                @click="activeTab = tab.id"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="tabs-content">
            <div v-if="activeTab === 'description'" class="tab-panel product-description">
              <p>{{ product.moTa || "Chưa có mô tả sản phẩm." }}</p>
            </div>

            <div v-else-if="activeTab === 'specs'" class="tab-panel specs-table">
              <div class="spec-row">
                <span class="spec-label">Mã sản phẩm</span>
                <span class="spec-value">{{ product.code || `SP${product.id || "00000"}` }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Loại sản phẩm</span>
                <span class="spec-value">{{ product.loai || "N/A" }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Chất liệu</span>
                <span class="spec-value">{{ product.vatLieu || "N/A" }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Quy cách</span>
                <span class="spec-value">{{ product.quyCach || "N/A" }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Màu sắc</span>
                <span class="spec-value">{{ product.mauSac || "N/A" }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Kích thước</span>
                <span class="spec-value">{{ product.kichThuoc || "N/A" }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Trọng lượng</span>
                <span class="spec-value">{{ product.trongLuong || "N/A" }}</span>
              </div>
              <div class="spec-row">
                <span class="spec-label">Xuất xứ</span>
                <span class="spec-value">{{ product.xuatXu || "Việt Nam" }}</span>
              </div>
            </div>

            <div v-else-if="activeTab === 'partner'" class="tab-panel partner-info">
              <div class="partner-row">
                <span class="partner-label">Nhà cung cấp / Đối tác</span>
                <span class="partner-value">
                  {{ product.tenDoiTac || product.nhaCungCap || "N/A" }}
                </span>
              </div>
              <div class="partner-row">
                <span class="partner-label">Nhà sản xuất</span>
                <span class="partner-value">{{ product.nhaSanXuat || "N/A" }}</span>
              </div>
              <div class="partner-row">
                <span class="partner-label">Trạng thái phê duyệt</span>
                <span class="partner-value status-pill">
                  {{ product.tenTrangThai || product.trangThai || "Chờ duyệt" }}
                </span>
              </div>
            </div>

            <div v-else class="tab-panel storage-info">
              <p>
                {{
                  product.huongDanBaoQuan ||
                  "Sản phẩm nên được bảo quản ở nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp và độ ẩm cao."
                }}
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div v-else class="empty-product">
      <i class="fa-solid fa-circle-exclamation"></i>
      <span>Không có dữ liệu sản phẩm để hiển thị.</span>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <button
            type="button"
            class="btn btn-outline-secondary px-4"
            :disabled="!!loadingAction"
            @click="closePopup"
        >
          Đóng
        </button>

        <button
            type="button"
            class="btn btn-danger px-4"
            :disabled="!!loadingAction"
            @click="rejectProduct"
        >
          <span
              v-if="loadingAction === 'tuChoi'"
              class="spinner-border spinner-border-sm me-1"
          ></span>
          <i v-else class="fa-solid fa-xmark me-1"></i>
          Từ chối duyệt
        </button>

        <button
            type="button"
            class="btn btn-success px-5 fw-bold"
            :disabled="!!loadingAction"
            @click="approveProduct"
        >
          <span
              v-if="loadingAction === 'duyet'"
              class="spinner-border spinner-border-sm me-1"
          ></span>
          <i v-else class="fa-solid fa-check me-1"></i>
          Duyệt sản phẩm
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
:global(.custom-product-dialog) {
  --primary-gold: #d4a017;
  --primary-gold-dark: #b8860b;
  --text-dark: #1a1a1a;
  --text-gray: #666666;
  --text-light: #999999;
  --bg-light: #f9f9f9;
  --border-color: #e8e8e8;
  --success-green: #4caf50;
  --error-red: #dc3545;

  max-width: 1200px;
  height: 96vh;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  overflow: hidden;
}

:global(.custom-product-dialog .el-dialog__header) {
  flex: 0 0 auto;
  margin-right: 0;
  padding: 16px 22px;
  border-bottom: 1px solid var(--border-color);
  background: #ffffff;
}

:global(.custom-product-dialog .el-dialog__body) {
  flex: 1 1 auto;
  min-height: 0;
  padding: 0;
  overflow: hidden;
  background: #f5f6f8;
}

:global(.custom-product-dialog .el-dialog__footer) {
  flex: 0 0 auto;
  padding: 14px 22px;
  border-top: 1px solid var(--border-color);
  background: #ffffff;
}

.dialog-header,
.dialog-title-group,
.dialog-footer {
  display: flex;
  align-items: center;
}

.dialog-header {
  justify-content: space-between;
  gap: 16px;
}

.dialog-title-group {
  gap: 12px;
  min-width: 0;
}

.dialog-icon {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(135deg, var(--primary-gold), var(--primary-gold-dark));
  box-shadow: 0 5px 14px rgba(212, 160, 23, 0.28);
}

.dialog-title {
  color: var(--text-dark);
  font-size: 18px;
  font-weight: 700;
}

.dialog-subtitle {
  margin-top: 2px;
  color: var(--text-gray);
  font-size: 12px;
}

.dialog-close {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #606266;
  font-size: 20px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.dialog-close:hover:not(:disabled) {
  color: var(--error-red);
  background: #fff1f2;
}

.dialog-close:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.product-dialog-scroll {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  overscroll-behavior: contain;
  padding: 28px 32px 36px;
  scrollbar-gutter: stable;
}

.product-detail-section,
.product-tabs-section {
  max-width: 1120px;
  margin: 0 auto;
}

.product-detail-section {
  padding: 0 0 30px;
}

.product-detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 50px;
  align-items: start;
}

.product-gallery {
  position: sticky;
  top: 0;
}

.main-image {
  width: 100%;
  max-height: 420px;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: 12px;
  background: var(--bg-light);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-light);
  border: 1px dashed #dcdfe6;
}

.image-empty i {
  font-size: 52px;
}

.product-info-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.preview-label {
  display: inline-flex;
  margin-bottom: 8px;
  padding: 5px 10px;
  border-radius: 999px;
  background: #1f2937;
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.7px;
}

.product-title {
  margin: 0;
  color: var(--text-dark);
  font-family: "Faustina", serif;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.3;
}

.product-code {
  color: var(--text-gray);
  font-family: "Noto Sans", sans-serif;
  font-size: 13px;
}

.product-code strong {
  color: var(--text-dark);
  font-weight: 600;
}

.product-price-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-top: 2px solid var(--border-color);
  border-bottom: 2px solid var(--border-color);
}

.current-price {
  color: var(--primary-gold);
  font-family: "Noto Sans", sans-serif;
  font-size: 28px;
  font-weight: 700;
}

.old-price {
  color: var(--text-light);
  font-size: 15px;
  font-weight: 500;
  text-decoration: line-through;
}

.approval-badge,
.status-pill {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  background: #fff3cd;
  color: #7a5a00;
  font-size: 12px;
  font-weight: 700;
}

.product-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  padding: 16px;
  border-radius: 12px;
  background: var(--bg-light);
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-color);
  font-family: "Noto Sans", sans-serif;
  font-size: 13px;
}

.meta-row-full {
  grid-column: 1 / -1;
}

.meta-label {
  color: var(--text-gray);
  font-weight: 500;
}

.meta-value {
  color: var(--text-dark);
  font-weight: 600;
  text-align: right;
  overflow-wrap: anywhere;
}

.partner-name {
  color: #0d6efd;
}

.tabs-container {
  overflow: hidden;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.tabs-header {
  display: flex;
  border-bottom: 2px solid var(--border-color);
  background: #ffffff;
}

.tab-btn {
  flex: 1;
  padding: 17px 18px;
  border: 0;
  border-bottom: 3px solid transparent;
  background: #ffffff;
  color: var(--text-gray);
  font-family: "Noto Sans", sans-serif;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.35px;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.tab-btn:hover {
  color: var(--primary-gold);
  background: var(--bg-light);
}

.tab-btn.active {
  color: var(--primary-gold);
  border-bottom-color: var(--primary-gold);
}

.tabs-content {
  min-height: 220px;
  padding: 32px 38px;
}

.tab-panel {
  color: var(--text-dark);
  line-height: 1.8;
}

.product-description p,
.storage-info p {
  margin: 0;
  white-space: pre-line;
}

.spec-row,
.partner-row {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 15px 0;
  border-bottom: 1px solid var(--border-color);
  font-family: "Noto Sans", sans-serif;
}

.spec-row:last-child,
.partner-row:last-child {
  border-bottom: 0;
}

.spec-label,
.partner-label {
  color: var(--text-gray);
  font-size: 14px;
  font-weight: 600;
}

.spec-value,
.partner-value {
  color: var(--text-dark);
  font-size: 14px;
  font-weight: 500;
  text-align: right;
}

.dialog-footer {
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.empty-product {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-gray);
  font-size: 15px;
}

.empty-product i {
  color: var(--text-light);
  font-size: 44px;
}

@media (max-width: 900px) {
  .product-dialog-scroll {
    padding: 24px;
  }

  .product-detail-layout {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .product-gallery {
    position: static;
  }

  .main-image {
    max-height: 360px;
  }
}

@media (max-width: 640px) {
  :global(.custom-product-dialog) {
    width: calc(100% - 16px) !important;
    height: 96vh;
  }

  :global(.custom-product-dialog .el-dialog__header),
  :global(.custom-product-dialog .el-dialog__footer) {
    padding: 12px 14px;
  }

  .dialog-subtitle {
    display: none;
  }

  .product-dialog-scroll {
    padding: 18px 14px 24px;
  }

  .product-title {
    font-size: 21px;
  }

  .current-price {
    font-size: 23px;
  }

  .product-meta {
    grid-template-columns: 1fr;
  }

  .meta-row-full {
    grid-column: auto;
  }

  .tabs-header {
    flex-wrap: wrap;
  }

  .tab-btn {
    flex: 1 1 50%;
    padding: 13px 8px;
    font-size: 11px;
  }

  .tabs-content {
    min-height: 180px;
    padding: 20px 16px;
  }

  .spec-row,
  .partner-row {
    gap: 12px;
  }

  .dialog-footer > .btn {
    flex: 1 1 auto;
  }
}
</style>
