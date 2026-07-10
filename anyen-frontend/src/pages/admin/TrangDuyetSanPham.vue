<template>
  <div class="container py-4">
    <!-- TIÊU ĐỀ -->
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

    <!-- BẢNG DANH SÁCH -->
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

          <tr v-for="sp in danhSachSanPham" :key="sp.id">
            <td class="ps-4 fw-semibold text-muted">
              #{{ sp.id }}
            </td>

            <td>
              <img
                  v-if="sp.image"
                  :src="getImageUrl(sp.image)"
                  alt="Ảnh sản phẩm"
                  class="product-img"
              />
              <span v-else class="text-muted small">Không có ảnh</span>
            </td>

            <td>
              <div class="fw-semibold text-dark">
                {{ sp.name || "Chưa có tên" }}
              </div>
              <div class="small text-muted">
                Vật liệu: {{ sp.vatLieu || "Chưa có" }}
              </div>
              <div class="small text-muted">
                Màu sắc: {{ sp.mauSac || "Chưa có" }}
              </div>
            </td>

            <td>
              <div class="fw-medium">
                {{ sp.tenDoiTac || "Không rõ đối tác" }}
              </div>
              <small v-if="sp.tenDoanhNghiep" class="text-muted">
                {{ sp.tenDoanhNghiep }}
              </small>
            </td>

            <td>
                <span class="badge bg-light text-dark border border-secondary-subtle px-2 py-1">
                  {{ sp.loai || "Chưa có" }}
                </span>
            </td>

            <td>
              {{ sp.soLuong ?? 0 }}
            </td>

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

    <!-- THÔNG BÁO LỖI -->
    <div v-if="errorMessage" class="alert alert-danger mt-3 mb-0">
      {{ errorMessage }}
    </div>
  </div>
  <!-- FORM TỪ CHỐI -->
  <div
      v-if="showRejectModal"
      class="reject-overlay"
      @click.self="closeRejectModal"
  >
    <div class="reject-modal">
      <div class="d-flex justify-content-between align-items-start mb-3">
        <div>
          <h5 class="fw-bold mb-1">Từ chối sản phẩm</h5>
          <p class="text-muted mb-0">
            Nhập lý do từ chối để gửi thông báo về đối tác.
          </p>
        </div>

        <button
            type="button"
            class="btn-close"
            @click="closeRejectModal"
            :disabled="isRejecting"
        ></button>
      </div>

      <div v-if="selectedProduct" class="reject-product-box mb-3">
        <img
            v-if="selectedProduct.image"
            :src="getImageUrl(selectedProduct.image)"
            alt="Ảnh sản phẩm"
        />

        <div>
          <div class="fw-semibold">
            {{ selectedProduct.name }}
          </div>
          <div class="small text-muted">
            Mã SP: #{{ selectedProduct.id }}
          </div>
          <div class="small text-muted">
            Đối tác: {{ selectedProduct.tenDoiTac || "Không rõ đối tác" }}
          </div>
        </div>
      </div>

      <label class="form-label fw-semibold">
        Lý do từ chối <span class="text-danger">*</span>
      </label>

      <textarea
          v-model="lyDoTuChoi"
          class="form-control"
          rows="5"
          placeholder="Ví dụ: Hình ảnh chưa rõ, thông tin sản phẩm chưa đầy đủ..."
          :disabled="isRejecting"
      ></textarea>

      <div class="small text-muted mt-1">
        Tối thiểu 3 ký tự.
      </div>

      <div class="d-flex justify-content-end gap-2 mt-4">
        <button
            type="button"
            class="btn btn-outline-secondary"
            @click="closeRejectModal"
            :disabled="isRejecting"
        >
          Hủy
        </button>

        <button
            type="button"
            class="btn btn-danger"
            @click="submitTuChoi"
            :disabled="isRejecting"
        >
        <span
            v-if="isRejecting"
            class="spinner-border spinner-border-sm me-1"
        ></span>
          Xác nhận từ chối
        </button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from "vue";
// Import các hàm từ service
import { getSanPhamChoDuyet, duyetSanPham, tuChoiSanPham } from "../../services/duyetSanPhamService.js";

const danhSachSanPham = ref([]);
const loadingStates = reactive({});
const isLoading = ref(false);
const errorMessage = ref("");

// 1. SỬA HÀM LẤY DANH SÁCH
const fetchDanhSachSanPhamChoDuyet = async () => {
  isLoading.value = true;
  errorMessage.value = "";

  try {
    // Gọi thẳng hàm từ service
    const data = await getSanPhamChoDuyet(1, 16);

    // Kiểm tra cấu trúc data trả về từ Backend (PageResponse)
    // Thông thường Spring Boot PageResponse sẽ bọc danh sách trong thuộc tính .content hoặc .items
    if (data) {
      danhSachSanPham.value = data.content || data.items || (Array.isArray(data) ? data : []);
    }
  } catch (error) {
    console.error("Lỗi khi tải danh sách sản phẩm chờ duyệt:", error);
    errorMessage.value =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không tải được danh sách sản phẩm chờ duyệt.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchDanhSachSanPhamChoDuyet();
});

// 2. SỬA HÀM DUYỆT
const confirmDuyet = async (sp) => {
  const ok = confirm(`Bạn có chắc chắn muốn duyệt sản phẩm "${sp.name}" không?`);
  if (!ok) return;

  loadingStates[sp.id] = "duyet";
  errorMessage.value = "";

  try {
    // Gọi hàm từ service, không dùng api.put(...) bọc ngoài nữa
    await duyetSanPham(sp.id);

    alert("Duyệt sản phẩm thành công! Sản phẩm đã chuyển sang Đang bán.");

    danhSachSanPham.value = danhSachSanPham.value.filter(
        (item) => item.id !== sp.id
    );
  } catch (error) {
    console.error("Lỗi khi duyệt sản phẩm:", error);
    alert(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Đã xảy ra lỗi khi duyệt sản phẩm."
    );
  } finally {
    delete loadingStates[sp.id];
  }
};

// 3. SỬA HÀM TỪ CHỐI
const confirmTuChoi = async (sp) => {
  const lyDo = prompt(`Nhập lý do từ chối sản phẩm "${sp.name}":`);
  if (lyDo === null) return;

  if (!lyDo.trim() || lyDo.trim().length < 3) {
    alert("Lý do từ chối phải từ 3 ký tự trở lên.");
    return;
  }

  const ok = confirm(`Bạn có chắc chắn muốn từ chối sản phẩm "${sp.name}" không?`);
  if (!ok) return;

  loadingStates[sp.id] = "tuChoi";
  errorMessage.value = "";

  try {
    // Sử dụng đúng hàm service đã viết
    await tuChoiSanPham(sp.id, lyDo);

    alert("Đã từ chối sản phẩm.");

    danhSachSanPham.value = danhSachSanPham.value.filter(
        (item) => item.id !== sp.id
    );
  } catch (error) {
    console.error("Lỗi khi từ chối sản phẩm:", error);
    alert(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Đã xảy ra lỗi khi từ chối sản phẩm."
    );
  } finally {
    delete loadingStates[sp.id];
  }
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || value === "") return "0 ₫";

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(value));
};

const formatDateTime = (value) => {
  if (!value) return "";

  return new Date(value).toLocaleString("vi-VN");
};

const getImageUrl = (path) => {
  if (!path) return "";

  if (path.startsWith("http")) {
    return path;
  }

  if (path.startsWith("/")) {
    return `http://localhost:8080${path}`;
  }

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

.table th {
  white-space: nowrap;
  font-size: 14px;
}

.table td {
  vertical-align: middle;
  font-size: 14px;
}

.btn {
  white-space: nowrap;
}

@media (max-width: 768px) {
  .container {
    padding-left: 12px;
    padding-right: 12px;
  }

  .d-flex.justify-content-between {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 12px;
  }

  .table {
    min-width: 1050px;
  }
}
</style>