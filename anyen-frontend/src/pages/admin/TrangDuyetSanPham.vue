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
            <td class="ps-4 fw-semibold text-muted">
              #{{ sp.id }}
            </td>

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
              <div class="small text-muted">
                Vật liệu: {{ sp.vatLieu || "Chưa có" }}
              </div>
            </td>

            <td>
              <div class="fw-medium">
                {{ sp.tenDoiTac || "Không rõ đối tác" }}
              </div>
            </td>

            <td>
                <span class="badge bg-light text-dark border border-secondary-subtle px-2 py-1">
                  {{ sp.loai || "Chưa có" }}
                </span>
            </td>

            <td>{{ sp.soLuong ?? 0 }}</td>

            <td>
              <code class="text-danger fw-bold">
                {{ formatCurrency(sp.price) }}
              </code>
            </td>

            <td>
                <span class="badge bg-warning text-dark px-2 py-1 rounded-pill">
                  {{ sp.tenTrangThai }}
                </span>
            </td>

            <td class="text-muted small">
              {{ formatDateTime(sp.ngayTao) }}
            </td>

            <td class="text-end pe-4">
              <div class="d-flex justify-content-end gap-2">
                <button
                    class="btn btn-sm btn-outline-primary px-2 rounded-2"
                    title="Xem chi tiết"
                    @click="openDetailModal(sp)"
                >
                  <i class="fa-solid fa-eye"></i>
                </button>

                <button
                    class="btn btn-sm btn-success px-3 rounded-2 shadow-sm"
                    @click="confirmDuyet(sp)"
                    :disabled="loadingStates[sp.id]"
                >
                    <span
                        v-if="loadingStates[sp.id] === 'duyet'"
                        class="spinner-border spinner-border-sm me-1"
                    ></span>
                  <i v-else class="fa-solid fa-check me-1"></i>
                  Duyệt
                </button>

                <button
                    class="btn btn-sm btn-danger px-3 rounded-2 shadow-sm"
                    @click="confirmTuChoi(sp)"
                    :disabled="loadingStates[sp.id]"
                >
                    <span
                        v-if="loadingStates[sp.id] === 'tuChoi'"
                        class="spinner-border spinner-border-sm me-1"
                    ></span>
                  <i v-else class="fa-solid fa-xmark me-1"></i>
                  Từ chối
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger mt-3 mb-0">
      {{ errorMessage }}
    </div>
  </div>

  <div
      v-if="showDetailModal"
      class="modal-overlay"
      @click.self="closeDetailModal"
  >
    <div class="modal-box modal-lg-custom">
      <div class="d-flex justify-content-between align-items-start border-bottom pb-3 mb-3">
        <div>
          <h5 class="fw-bold text-dark mb-1">Chi Tiết Sản Phẩm Chờ Duyệt</h5>
          <p class="text-muted small mb-0">Mã sản phẩm: #{{ detailProduct?.id }}</p>
        </div>
        <button type="button" class="btn-close" @click="closeDetailModal"></button>
      </div>

      <div class="row g-4" v-if="detailProduct">
        <div class="col-md-5 text-center bg-light rounded-3 p-3 d-flex align-items-center justify-content-center" style="min-height: 250px;">
          <img
              v-if="detailProduct.image"
              :src="getImageUrl(detailProduct.image)"
              alt="Ảnh sản phẩm lớn"
              class="img-fluid rounded shadow-sm detail-view-img"
          />
          <div v-else class="text-muted">
            <i class="fa-regular fa-image fa-3x mb-2"></i>
            <div>Sản phẩm không có ảnh đại diện</div>
          </div>
        </div>

        <div class="col-md-7">
          <h4 class="fw-bold text-dark mb-2">{{ detailProduct.name || "Chưa có tên" }}</h4>
          <div class="mb-3">
            <span class="badge bg-warning text-dark me-2">{{ detailProduct.tenTrangThai }}</span>
            <span class="badge bg-secondary">{{ detailProduct.loai || "Chưa phân loại" }}</span>
          </div>

          <div class="p-3 bg-light rounded-3 mb-3">
            <div class="row g-2">
              <div class="col-6">
                <small class="text-muted d-block">Giá bán hiện tại:</small>
                <span class="text-danger fw-bold fs-5">{{ formatCurrency(detailProduct.price) }}</span>
              </div>
              <div class="col-6" v-if="detailProduct.oldPrice">
                <small class="text-muted d-block">Giá gốc / Khuyến mãi:</small>
                <del class="text-muted small">{{ formatCurrency(detailProduct.oldPrice) }}</del>
              </div>
            </div>
          </div>

          <table class="table table-sm table-borderless detail-table mb-0">
            <tbody>
            <tr>
              <td class="text-muted py-1" style="width: 120px;">Đối tác gửi:</td>
              <td class="fw-medium text-dark py-1">{{ detailProduct.tenDoiTac || "Không rõ đối tác" }}</td>
            </tr>
            <tr>
              <td class="text-muted py-1">Vật liệu:</td>
              <td class="text-dark py-1">{{ detailProduct.vatLieu || "Chưa cập nhật" }}</td>
            </tr>
            <tr>
              <td class="text-muted py-1">Màu sắc:</td>
              <td class="text-dark py-1">{{ detailProduct.mauSac || "Chưa cập nhật" }}</td>
            </tr>
            <tr>
              <td class="text-muted py-1">Tôn giáo:</td>
              <td class="text-dark py-1">{{ detailProduct.tonGiao || "Không thuộc tôn giáo" }}</td>
            </tr>
            <tr>
              <td class="text-muted py-1">Số lượng kho:</td>
              <td class="text-dark py-1 fw-bold">{{ detailProduct.soLuong ?? 0 }} cái</td>
            </tr>
            <tr>
              <td class="text-muted py-1">Ngày gửi duyêt:</td>
              <td class="text-muted py-1">{{ formatDateTime(detailProduct.ngayTao) }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2 border-top pt-3 mt-4">
        <button type="button" class="btn btn-secondary px-4" @click="closeDetailModal">Đóng</button>
        <button type="button" class="btn btn-danger px-3" @click="handleActionFromDetail('tuChoi')">
          <i class="fa-solid fa-xmark me-1"></i> Từ chối
        </button>
        <button type="button" class="btn btn-success px-4" @click="handleActionFromDetail('duyet')">
          <i class="fa-solid fa-check me-1"></i> Duyệt ngay
        </button>
      </div>
    </div>
  </div>

  <div
      v-if="showRejectModal"
      class="modal-overlay"
      @click.self="closeRejectModal"
  >
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
        <label class="form-label fw-semibold text-dark">
          Lý do từ chối <span class="text-danger">*</span>
        </label>
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

// State quản lý Modal từ chối
const showRejectModal = ref(false);
const selectedProduct = ref(null);
const lyDoTuChoi = ref("");
const isRejecting = ref(false);

// State quản lý Modal Xem chi tiết sản phẩm
const showDetailModal = ref(false);
const detailProduct = ref(null);

// 1. HÀM LẤY DANH SÁCH
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

// 2. HÀM MỞ/ĐÓNG POPUP CHI TIẾT
const openDetailModal = (sp) => {
  detailProduct.value = sp;
  showDetailModal.value = true;
};
const closeDetailModal = () => {
  showDetailModal.value = false;
  detailProduct.value = null;
};

// Hàm phụ hỗ trợ bấm Duyệt/Từ chối trực tiếp ngay khi đang xem Popup Chi tiết
const handleActionFromDetail = (actionType) => {
  const sp = detailProduct.value;
  closeDetailModal(); // Đóng popup chi tiết lại
  if (actionType === 'duyet') {
    confirmDuyet(sp);
  } else if (actionType === 'tuChoi') {
    confirmTuChoi(sp);
  }
};

// 3. HÀM DUYỆT SẢN PHẨM
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

// 4. HÀM ĐIỀU KHIỂN MODAL TỪ CHỐI
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

// 5. SUBMIT TỪ CHỐI
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
    closeRejectModal();
  } catch (error) {
    console.error(error);
    alert("Đã xảy ra lỗi khi từ chối.");
  } finally {
    isRejecting.value = false;
    delete loadingStates[sp.id];
  }
};

const formatCurrency = (value) => {
  if (!value) return "0 ₫";
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
.product-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.cursor-pointer {
  cursor: pointer;
}

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

/* KHUNG HỘP THOẠI MODAL */
.modal-box {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  animation: modalFadeIn 0.2s ease-out;
}

/* Custom kích thước rộng hơn cho màn hình chi tiết sản phẩm */
.modal-lg-custom {
  max-width: 750px;
}

.detail-view-img {
  max-height: 280px;
  object-fit: contain;
}

.detail-table td {
  font-size: 14px;
}

.reject-product-box {
  display: flex;
  gap: 12px;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  border: 1px dashed #dee2e6;
}

.reject-product-box img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

@media (max-width: 768px) {
  .table { min-width: 1050px; }
  .modal-lg-custom { max-width: 100%; }
}
</style>