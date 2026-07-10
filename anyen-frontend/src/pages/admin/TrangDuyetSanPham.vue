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
              #{{ sp. }}
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
                {{ sp.tenSanPham || "Chưa có tên" }}
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
                {{ formatCurrency(sp.giaTien) }}
              </code>
            </td>

            <td>
                <span class="badge bg-warning text-dark px-2 py-1 rounded-pill">
                  {{ sp.trangThai || "Chờ xác nhận" }}
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
                    :disabled="loadingStates[sp.maSanPham]"
                >
                    <span
                        v-if="loadingStates[sp.maSanPham] === 'duyet'"
                        class="spinner-border spinner-border-sm me-1"
                    ></span>
                  <i v-else class="fa-solid fa-check me-1"></i>
                  Duyệt
                </button>

                <button
                    class="btn btn-sm btn-danger px-3 rounded-2 shadow-sm"
                    @click="confirmTuChoi(sp)"
                    :disabled="loadingStates[sp.maSanPham]"
                >
                    <span
                        v-if="loadingStates[sp.maSanPham] === 'tuChoi'"
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
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import api from "../../api/api.js";

const danhSachSanPham = ref([]);
const loadingStates = reactive({});
const isLoading = ref(false);
const errorMessage = ref("");

const fetchDanhSachSanPhamChoDuyet = async () => {
  isLoading.value = true;
  errorMessage.value = "";

  try {
    const res = await api.get("/api/san-pham/cho-duyet");

    // Backend trả về List<DuyetSanPhamResponse>
    danhSachSanPham.value = Array.isArray(res.data) ? res.data : res.data.items || [];
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

const confirmDuyet = async (sp) => {
  const ok = confirm(
      `Bạn có chắc chắn muốn duyệt sản phẩm "${sp.tenSanPham}" không?`
  );

  if (!ok) return;

  loadingStates[sp.maSanPham] = "duyet";
  errorMessage.value = "";

  try {
    await api.put(`/api/nhan-vien/duyet-san-pham/${sp.maSanPham}/duyet`);

    alert("Duyệt sản phẩm thành công! Sản phẩm đã chuyển sang Đang bán.");

    danhSachSanPham.value = danhSachSanPham.value.filter(
        (item) => item.maSanPham !== sp.maSanPham
    );
  } catch (error) {
    console.error("Lỗi khi duyệt sản phẩm:", error);
    alert(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Đã xảy ra lỗi khi duyệt sản phẩm."
    );
  } finally {
    delete loadingStates[sp.maSanPham];
  }
};

const confirmTuChoi = async (sp) => {
  const lyDoTuChoi = prompt(
      `Nhập lý do từ chối sản phẩm "${sp.tenSanPham}":`
  );

  if (lyDoTuChoi === null) return;

  if (!lyDoTuChoi.trim() || lyDoTuChoi.trim().length < 3) {
    alert("Lý do từ chối phải từ 3 ký tự trở lên.");
    return;
  }

  const ok = confirm(
      `Bạn có chắc chắn muốn từ chối và xóa sản phẩm "${sp.tenSanPham}" khỏi database không?`
  );

  if (!ok) return;

  loadingStates[sp.maSanPham] = "tuChoi";
  errorMessage.value = "";

  try {
    await api.put(`/api/nhan-vien/duyet-san-pham/${sp.maSanPham}/tu-choi`, {
      lyDoTuChoi: lyDoTuChoi.trim(),
    });

    alert("Đã từ chối sản phẩm và xóa khỏi database.");

    danhSachSanPham.value = danhSachSanPham.value.filter(
        (item) => item.maSanPham !== sp.maSanPham
    );
  } catch (error) {
    console.error("Lỗi khi từ chối sản phẩm:", error);
    alert(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Đã xảy ra lỗi khi từ chối sản phẩm."
    );
  } finally {
    delete loadingStates[sp.maSanPham];
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