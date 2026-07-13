<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-dark mb-1">Duyệt Sản Phẩm Đối Tác</h3>
        <p class="text-muted mb-0">
          Danh sách sản phẩm mới do đối tác gửi chờ nhân viên xác nhận.
        </p>
      </div>

      <button
          class="btn btn-outline-secondary d-flex align-items-center gap-2 px-3 shadow-sm"
          @click="fetchDanhSachSanPhamChoDuyet"
          :disabled="isLoading"
      >
        <span v-if="isLoading" class="spinner-border spinner-border-sm"></span>
        <i v-else class="fa-solid fa-rotate-right"></i>
        Làm mới
      </button>
    </div>

    <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary">
          <tr>
            <th class="ps-4">Mã SP</th>
            <th>Hình ảnh</th>
            <th>Tên sản phẩm</th>
            <th>Đối tác</th>
            <th>Loại</th>
            <th>Số lượng</th>
            <th>Giá bán</th>
            <th>Trạng thái</th>
            <th>Ngày gửi</th>
            <th class="text-end pe-4">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="isLoading">
            <td colspan="10" class="text-center py-4 text-muted">
              Đang tải dữ liệu...
            </td>
          </tr>

          <tr v-else-if="danhSachSanPham.length === 0">
            <td colspan="10" class="text-center py-4 text-muted">
              Không có sản phẩm nào đang chờ duyệt.
            </td>
          </tr>

          <tr v-for="sp in danhSachSanPham" :key="sp.id" v-else>
            <td class="ps-4 fw-semibold text-muted">#{{ sp.id }}</td>
            <td>
              <img
                  v-if="sp.image"
                  :src="getImageUrl(sp.image)"
                  alt="Ảnh sản phẩm"
                  class="product-img cursor-pointer"
                  @click="openDetailModal(sp)"
              />
              <span v-else class="text-muted small">Không có ảnh</span>
            </td>

            <td>
              <div class="fw-semibold text-dark text-primary-hover cursor-pointer" @click="openDetailModal(sp)">
                {{ sp.name || "Chưa có tên" }}
              </div>
              <div class="small text-muted">Vật liệu: {{ sp.vatLieu || "Chưa có" }}</div>
            </td>

            <td><div class="fw-medium">{{ sp.tenDoiTac || "Không rõ đối tác" }}</div></td>
            <td>
              <span class="badge bg-light text-dark border border-secondary-subtle px-2 py-1">
                {{ sp.loai || "Chưa có" }}
              </span>
            </td>
            <td>{{ sp.soLuong ?? 0 }}</td>
            <td><code class="text-danger fw-bold">{{ formatCurrency(sp.price) }}</code></td>
            <td><span class="badge bg-warning text-dark px-2 py-1 rounded-pill">{{ sp.tenTrangThai }}</span></td>
            <td class="text-muted small">{{ formatDateTime(sp.ngayTao) }}</td>

            <td class="text-end pe-4">
              <div class="d-flex justify-content-end gap-2">
                <button class="btn btn-sm btn-outline-primary px-2 rounded-2" title="Xem chi tiết" @click="openDetailModal(sp)">
                  <i class="fa-solid fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-success px-3 rounded-2 shadow-sm" @click="confirmDuyet(sp)" :disabled="loadingStates[sp.id]">
                  <span v-if="loadingStates[sp.id] === 'duyet'" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="fa-solid fa-check me-1"></i> Duyệt
                </button>
                <button class="btn btn-sm btn-danger px-3 rounded-2 shadow-sm" @click="confirmTuChoi(sp)" :disabled="loadingStates[sp.id]">
                  <span v-if="loadingStates[sp.id] === 'tuChoi'" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="fa-solid fa-xmark me-1"></i> Từ chối
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger mt-3 mb-0">{{ errorMessage }}</div>
  </div>

  <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
    <div class="modal-box modal-xl-custom animate-fade-in text-start">

      <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
        <div class="d-flex align-items-center gap-2">
          <span class="badge bg-dark px-2 py-1 tracking-wider text-uppercase font-sans">CHẾ ĐỘ XEM TRƯỚC (PREVIEW)</span>
          <span class="text-muted small">ID: #{{ detailProduct?.id }}</span>
        </div>
        <button type="button" class="btn-close" @click="closeDetailModal"></button>
      </div>

      <div class="product-detail-container" v-if="detailProduct">
        <section class="product-detail-section p-0 mb-4">
          <div class="product-detail-layout">

            <div class="product-gallery">
              <div class="main-image">
                <img :src="getImageUrl(detailProduct.image)" :alt="detailProduct.name" />
              </div>
            </div>

            <div class="product-info-panel">
              <h1 class="product-title">{{ detailProduct.name || 'Chưa có tên' }}</h1>

              <div class="product-code">
                <span>Mã sản phẩm: </span>
                <strong>{{ detailProduct.code || ('SP' + detailProduct.id) }}</strong>
              </div>

              <div class="product-price-section">
                <div class="current-price">{{ formatCurrency(detailProduct.price) }}</div>
                <div v-if="detailProduct.oldPrice" class="old-price">{{ formatCurrency(detailProduct.oldPrice) }}</div>
                <div v-if="detailProduct.oldPrice && detailProduct.price" class="discount-badge">
                  Mới
                </div>
              </div>

              <div class="product-meta">
                <div class="meta-row">
                  <span class="meta-label">Loại sản phẩm:</span>
                  <span class="meta-value">{{ detailProduct.loai || 'N/A' }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">Chất liệu:</span>
                  <span class="meta-value">{{ detailProduct.vatLieu || 'N/A' }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">Tôn giáo:</span>
                  <span class="meta-value">{{ detailProduct.tonGiao || 'Không phân biệt' }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">Màu sắc:</span>
                  <span class="meta-value">{{ detailProduct.mauSac || 'N/A' }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">Số lượng kho:</span>
                  <span class="meta-value">{{ detailProduct.soLuong ?? 0 }} sản phẩm</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">Đối tác gửi duyệt:</span>
                  <span class="meta-value text-primary fw-bold">{{ detailProduct.tenDoiTac || 'N/A' }}</span>
                </div>
              </div>
            </div>

          </div>
        </section>

        <section class="product-tabs-section p-0">
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
                  <p>{{ detailProduct.moTa || 'Chưa có mô tả chi tiết cho sản phẩm này.' }}</p>
                </div>
              </div>

              <div v-if="activeTab === 'specs'" class="tab-panel">
                <div class="specs-table">
                  <div class="spec-row">
                    <span class="spec-label">Mã sản phẩm</span>
                    <span class="spec-value">#{{ detailProduct.id }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Loại sản phẩm</span>
                    <span class="spec-value">{{ detailProduct.loai || 'N/A' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Chất liệu</span>
                    <span class="spec-value">{{ detailProduct.vatLieu || 'N/A' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Màu sắc</span>
                    <span class="spec-value">{{ detailProduct.mauSac || 'N/A' }}</span>
                  </div>
                </div>
              </div>

              <div v-if="activeTab === 'partner'" class="tab-panel">
                <div class="partner-info">
                  <div class="partner-row">
                    <span class="partner-label">Nhà cung cấp / Đối tác:</span>
                    <span class="partner-value fw-semibold text-dark">{{ detailProduct.tenDoiTac || 'N/A' }}</span>
                  </div>
                  <div class="partner-row">
                    <span class="partner-label">Trạng thái phê duyệt:</span>
                    <span class="partner-value badge bg-warning text-dark">{{ detailProduct.tenTrangThai }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <div class="d-flex justify-content-end gap-2 border-top pt-3 mt-4">
        <button type="button" class="btn btn-outline-secondary px-4 fw-medium" @click="closeDetailModal">Đóng</button>
        <button type="button" class="btn btn-danger px-4 fw-medium" @click="handleActionFromDetail('tuChoi')">
          <i class="fa-solid fa-xmark me-1"></i> Từ chối duyệt
        </button>
        <button type="button" class="btn btn-success px-5 fw-bold shadow-sm" @click="handleActionFromDetail('duyet')">
          <i class="fa-solid fa-check me-1"></i> Duyệt sản phẩm
        </button>
      </div>
    </div>
  </div>

  <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
    <div class="modal-box">
      <div class="d-flex justify-content-between align-items-start mb-3">
        <div>
          <h5 class="fw-bold mb-1">Từ chối sản phẩm</h5>
          <p class="text-muted mb-0">Nhập lý do từ chối để gửi thông báo về đối tác.</p>
        </div>
        <button type="button" class="btn-close" @click="closeRejectModal" :disabled="isRejecting"></button>
      </div>

      <div v-if="selectedProduct" class="reject-product-box mb-3">
        <img v-if="selectedProduct.image" :src="getImageUrl(selectedProduct.image)" alt="Ảnh sản phẩm" />
        <div>
          <div class="fw-semibold text-dark">{{ selectedProduct.name }}</div>
          <div class="small text-muted">Mã SP: #{{ selectedProduct.id }}</div>
          <div class="small text-muted">Đối tác: {{ selectedProduct.tenDoiTac || "Không rõ đối tác" }}</div>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold text-dark">Lý do từ chối <span class="text-danger">*</span></label>
        <textarea
            v-model="lyDoTuChoi"
            class="form-control"
            rows="5"
            placeholder="Ví dụ: Hình ảnh chưa rõ, thông tin sản phẩm chưa đầy đủ..."
            :disabled="isRejecting"
        ></textarea>
        <div class="small text-muted mt-1">Tối thiểu 3 ký tự.</div>
      </div>

      <div class="d-flex justify-content-end gap-2 mt-4">
        <button type="button" class="btn btn-outline-secondary" @click="closeRejectModal" :disabled="isRejecting">Hủy</button>
        <button type="button" class="btn btn-danger" @click="submitTuChoi" :disabled="isRejecting">
          <span v-if="isRejecting" class="spinner-border spinner-border-sm me-1"></span>
          Xác nhận từ chối
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { getSanPhamChoDuyet, duyetSanPham, tuChoiSanPham } from "../../services/duyetSanPhamService.js";

const danhSachSanPham = ref([]);
const loadingStates = reactive({});
const isLoading = ref(false);
const errorMessage = ref("");

// State điều khiển modal từ chối
const showRejectModal = ref(false);
const selectedProduct = ref(null);
const lyDoTuChoi = ref("");
const isRejecting = ref(false);

// State điều khiển modal Xem chi tiết cấu trúc Client
const showDetailModal = ref(false);
const detailProduct = ref(null);

// Cấu hình Tabs giống hệt trang client của bạn
const tabs = [
  { id: 'description', label: 'MÔ TẢ SẢN PHẨM' },
  { id: 'specs', label: 'THÔNG SỐ KỸ THUẬT' },
  { id: 'partner', label: 'THÔNG TIN ĐỐI TÁC' }
];
const activeTab = ref('description');

const fetchDanhSachSanPhamChoDuyet = async () => {
  isLoading.value = true;
  errorMessage.value = "";
  try {
    const data = await getSanPhamChoDuyet(1, 16);
    if (data && data.items) {
      danhSachSanPham.value = data.items;
    } else if (data && data.content) {
      danhSachSanPham.value = data.content;
    } else {
      danhSachSanPham.value = Array.isArray(data) ? data : [];
    }
  } catch (error) {
    console.error("Lỗi khi tải danh sách sản phẩm chờ duyệt:", error);
    errorMessage.value = "Không tải được danh sách sản phẩm chờ duyệt.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchDanhSachSanPhamChoDuyet();
});

const openDetailModal = (sp) => {
  detailProduct.value = sp;
  activeTab.value = 'description'; // Reset về tab đầu tiên khi mở sản phẩm mới
  showDetailModal.value = true;
};

const closeDetailModal = () => {
  showDetailModal.value = false;
  detailProduct.value = null;
};

const handleActionFromDetail = (actionType) => {
  const sp = detailProduct.value;
  closeDetailModal();
  if (actionType === 'duyet') {
    confirmDuyet(sp);
  } else if (actionType === 'tuChoi') {
    confirmTuChoi(sp);
  }
};

const confirmDuyet = async (sp) => {
  const ok = confirm(`Bạn có chắc chắn muốn duyệt sản phẩm "${sp.name}" không?`);
  if (!ok) return;

  loadingStates[sp.id] = "duyet";
  try {
    await duyetSanPham(sp.id);
    alert("Duyệt sản phẩm thành công!");
    danhSachSanPham.value = danhSachSanPham.value.filter(item => item.id !== sp.id);
  } catch (error) {
    console.error("Lỗi khi duyệt sản phẩm:", error);
    alert("Đã xảy ra lỗi khi duyệt sản phẩm.");
  } finally {
    delete loadingStates[sp.id];
  }
};

const confirmTuChoi = (sp) => {
  selectedProduct.value = sp;
  lyDoTuChoi.value = "";
  showRejectModal.value = true;
};

const closeRejectModal = () => {
  if (isRejecting.value) return;
  showRejectModal.value = false;
  selectedProduct.value = null;
  lyDoTuChoi.value = "";
};

const submitTuChoi = async () => {
  const sp = selectedProduct.value;
  if (!sp) return;

  const textLyDo = lyDoTuChoi.value.trim();
  if (!textLyDo || textLyDo.length < 3) {
    alert("Lý do từ chối phải từ 3 ký tự trở lên.");
    return;
  }

  const ok = confirm(`Bạn có chắc chắn muốn từ chối sản phẩm "${sp.name}" không?`);
  if (!ok) return;

  isRejecting.value = true;
  loadingStates[sp.id] = "tuChoi";
  try {
    await tuChoiSanPham(sp.id, textLyDo);
    alert("Đã từ chối sản phẩm thành công.");
    danhSachSanPham.value = danhSachSanPham.value.filter(item => item.id !== sp.id);
    showRejectModal.value = false;
    selectedProduct.value = null;
    lyDoTuChoi.value = "";
  } catch (error) {
    console.error(error);
    alert("Đã xảy ra lỗi khi từ chối.");
  } finally {
    isRejecting.value = false;
    delete loadingStates[sp.id];
  }
};

const formatCurrency = (value) => {
  if (!value) return "Liên hệ";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(Number(value));
};

const formatDateTime = (value) => {
  if (!value) return "";
  return new Date(value).toLocaleString("vi-VN");
};

const getImageUrl = (path) => {
  if (!path) return "";
  if (path.startsWith("http")) return path;
  if (path.startsWith("/")) return `http://localhost:8080${path}`;
  return `http://localhost:8080/uploads/${path}`;
};
</script>

<style scoped>
/* Biến mã màu đồng bộ từ trang chi tiết An Yên của khách hàng */
.modal-xl-custom {
  --primary-gold: #d4a017;
  --primary-gold-dark: #b8860b;
  --text-dark: #1a1a1a;
  --text-gray: #666666;
  --text-light: #999999;
  --bg-light: #f9f9f9;
  --border-color: #e8e8e8;
  --error-red: #ff4444;

  max-width: 1100px; /* Độ rộng cực đại để hiển thị vừa vặn layout grid */
  width: 100%;
}

.font-sans {
  font-family: 'Noto Sans', sans-serif;
}

.product-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.cursor-pointer { cursor: pointer; }

.text-primary-hover:hover {
  color: #0d6efd !important;
  text-decoration: underline;
}

.table th, .table td {
  font-size: 14px;
  vertical-align: middle;
}

/* MODAL OVERLAY CHUNG */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}

.modal-box {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

/* ==========================================================
   CSS MÔ PHỎNG LAYOUT SẢN PHẨM CHI TIẾT TỪ CLIENT CỦA BẠN
   ========================================================== */
.product-detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: start;
}

.main-image {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-light);
  aspect-ratio: 4/3;
  max-height: 350px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info-panel {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-dark);
  margin: 0;
  line-height: 1.3;
  font-family: 'Faustina', serif;
}

.product-code {
  font-size: 13px;
  color: var(--text-gray);
}

.product-price-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-top: 2px solid var(--border-color);
  border-bottom: 2px solid var(--border-color);
}

.current-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-gold);
}

.old-price {
  font-size: 14px;
  color: var(--text-light);
  text-decoration: line-through;
}

.discount-badge {
  padding: 3px 8px;
  background: var(--error-red);
  color: white;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.product-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 20px;
  background: var(--bg-light);
  padding: 14px;
  border-radius: 10px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding-bottom: 5px;
  border-bottom: 1px solid var(--border-color);
}

.meta-row:last-child {
  border-bottom: none;
}

.meta-label { color: var(--text-gray); }
.meta-value { color: var(--text-dark); font-weight: 600; }

/* TABS SYSTEM STYLE */
.tabs-container {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid var(--border-color);
  background: #fafafa;
}

.tab-btn {
  flex: 1;
  padding: 12px 15px;
  background: none;
  border: none;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-gray);
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s ease;
  text-transform: uppercase;
}

.tab-btn:hover {
  color: var(--primary-gold);
}

.tab-btn.active {
  color: var(--primary-gold);
  border-bottom-color: var(--primary-gold);
  background: white;
}

.tabs-content {
  padding: 20px;
  min-height: 120px;
  font-size: 14px;
}

.spec-row, .partner-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}
.spec-row:last-child, .partner-row:last-child { border-bottom: none; }
.spec-label, .partner-label { color: var(--text-gray); font-weight: 600; }

.reject-product-box {
  display: flex;
  gap: 12px;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  border: 1px dashed #dee2e6;
}
.reject-product-box img { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; }

.tracking-wider { letter-spacing: 0.5px; }
.animate-fade-in { animation: modalFadeIn 0.2s ease-out; }

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.97); }
  to { opacity: 1; transform: scale(1); }
}

@media (max-width: 768px) {
  .product-detail-layout { grid-template-columns: 1fr; gap: 20px; }
  .product-meta { grid-template-columns: 1fr; }
}
</style>