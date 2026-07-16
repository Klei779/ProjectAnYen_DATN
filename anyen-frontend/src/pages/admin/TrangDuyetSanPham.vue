<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import PopXemSanPham from "./PopXemSanPham.vue";
import {
  getSanPhamChoDuyet,
  duyetSanPham,
  tuChoiSanPham,
} from "../../services/duyetSanPhamService.js";

const danhSachSanPham = ref([]);
const loadingStates = reactive({});
const isLoading = ref(false);
const errorMessage = ref("");

const keyword = ref("");
const categoryFilter = ref("all");
const partnerFilter = ref("all");

const currentPage = ref(1);
const pageSize = ref(8);

const showRejectModal = ref(false);
const selectedProduct = ref(null);
const lyDoTuChoi = ref("");
const isRejecting = ref(false);

const showDetailModal = ref(false);
const detailProduct = ref(null);

const fetchDanhSachSanPhamChoDuyet = async () => {
  isLoading.value = true;
  errorMessage.value = "";

  try {
    const data = await getSanPhamChoDuyet(1, 100);

    if (data?.items) {
      danhSachSanPham.value = data.items;
    } else if (data?.content) {
      danhSachSanPham.value = data.content;
    } else {
      danhSachSanPham.value = Array.isArray(data) ? data : [];
    }
  } catch (error) {
    console.error("Lỗi khi tải danh sách sản phẩm chờ duyệt:", error);
    danhSachSanPham.value = [];
    errorMessage.value =
        error.response?.data?.message ||
        "Không tải được danh sách sản phẩm chờ duyệt.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchDanhSachSanPhamChoDuyet);

const categories = computed(() => {
  const values = danhSachSanPham.value
      .map((item) => item.loai)
      .filter(Boolean);

  return [...new Set(values)].sort((a, b) =>
      String(a).localeCompare(String(b), "vi")
  );
});

const partners = computed(() => {
  const values = danhSachSanPham.value
      .map((item) => item.tenDoiTac)
      .filter(Boolean);

  return [...new Set(values)].sort((a, b) =>
      String(a).localeCompare(String(b), "vi")
  );
});

const filteredProducts = computed(() => {
  const searchValue = keyword.value.trim().toLowerCase();

  return danhSachSanPham.value.filter((product) => {
    const searchable = [
      product.id,
      product.name,
      product.tenDoiTac,
      product.loai,
      product.vatLieu,
      product.tenTrangThai,
    ]
        .map((value) => String(value ?? "").toLowerCase())
        .join(" ");

    const matchKeyword =
        !searchValue || searchable.includes(searchValue);

    const matchCategory =
        categoryFilter.value === "all" ||
        product.loai === categoryFilter.value;

    const matchPartner =
        partnerFilter.value === "all" ||
        product.tenDoiTac === partnerFilter.value;

    return matchKeyword && matchCategory && matchPartner;
  });
});

const totalPages = computed(() =>
    Math.max(
        Math.ceil(filteredProducts.value.length / pageSize.value),
        1
    )
);

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;

  return filteredProducts.value.slice(
      start,
      start + pageSize.value
  );
});

const displayFrom = computed(() => {
  if (!filteredProducts.value.length) return 0;

  return (currentPage.value - 1) * pageSize.value + 1;
});

const displayTo = computed(() =>
    Math.min(
        currentPage.value * pageSize.value,
        filteredProducts.value.length
    )
);

const visiblePages = computed(() => {
  const total = totalPages.value;

  if (total <= 5) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }

  if (currentPage.value <= 3) {
    return [1, 2, 3, "...", total];
  }

  if (currentPage.value >= total - 2) {
    return [1, "...", total - 2, total - 1, total];
  }

  return [1, "...", currentPage.value, "...", total];
});

const totalQuantity = computed(() =>
    danhSachSanPham.value.reduce(
        (total, product) => total + Number(product.soLuong || 0),
        0
    )
);

const totalPartner = computed(() => partners.value.length);

watch([keyword, categoryFilter, partnerFilter], () => {
  currentPage.value = 1;
});

watch(totalPages, (newTotal) => {
  if (currentPage.value > newTotal) {
    currentPage.value = newTotal;
  }
});

const resetFilters = () => {
  keyword.value = "";
  categoryFilter.value = "all";
  partnerFilter.value = "all";
  currentPage.value = 1;
};

const changePage = (page) => {
  if (page === "...") return;
  if (page < 1 || page > totalPages.value) return;

  currentPage.value = page;
};

const openDetailModal = (product) => {
  detailProduct.value = product;
  showDetailModal.value = true;
};

const handleDetailClosed = () => {
  detailProduct.value = null;
};

const handleApproveFromDetail = async (product) => {
  const approved = await confirmDuyet(product);

  if (approved) {
    showDetailModal.value = false;
  }
};

const handleRejectFromDetail = (product) => {
  showDetailModal.value = false;
  confirmTuChoi(product);
};

const confirmDuyet = async (product) => {
  const accepted = confirm(
      `Bạn có chắc chắn muốn duyệt sản phẩm "${product.name}" không?`
  );

  if (!accepted) return false;

  loadingStates[product.id] = "duyet";

  try {
    await duyetSanPham(product.id);
    alert("Duyệt sản phẩm thành công!");

    danhSachSanPham.value = danhSachSanPham.value.filter(
        (item) => item.id !== product.id
    );

    return true;
  } catch (error) {
    console.error("Lỗi khi duyệt sản phẩm:", error);
    alert(
        error.response?.data?.message ||
        "Đã xảy ra lỗi khi duyệt sản phẩm."
    );

    return false;
  } finally {
    delete loadingStates[product.id];
  }
};

const confirmTuChoi = (product) => {
  selectedProduct.value = product;
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
  const product = selectedProduct.value;

  if (!product) return;

  const reason = lyDoTuChoi.value.trim();

  if (reason.length < 3) {
    alert("Lý do từ chối phải từ 3 ký tự trở lên.");
    return;
  }

  const accepted = confirm(
      `Bạn có chắc chắn muốn từ chối sản phẩm "${product.name}" không?`
  );

  if (!accepted) return;

  isRejecting.value = true;
  loadingStates[product.id] = "tuChoi";

  try {
    await tuChoiSanPham(product.id, reason);
    alert("Đã từ chối sản phẩm thành công.");

    danhSachSanPham.value = danhSachSanPham.value.filter(
        (item) => item.id !== product.id
    );

    showRejectModal.value = false;
    selectedProduct.value = null;
    lyDoTuChoi.value = "";
  } catch (error) {
    console.error("Lỗi khi từ chối sản phẩm:", error);
    alert(
        error.response?.data?.message ||
        "Đã xảy ra lỗi khi từ chối sản phẩm."
    );
  } finally {
    isRejecting.value = false;
    delete loadingStates[product.id];
  }
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || value === "") {
    return "Liên hệ";
  }

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(value));
};

const formatDateTime = (value) => {
  if (!value) return "---";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const getImageUrl = (path) => {
  if (!path) return "";

  if (path.startsWith("http")) return path;
  if (path.startsWith("/")) return `http://localhost:8080${path}`;

  return `http://localhost:8080/uploads/${path}`;
};
</script>

<template>
  <div class="product-approval-page">
    <section class="page-content">
      <div class="page-heading">
        <div>
          <p class="eyebrow">KIỂM DUYỆT SẢN PHẨM</p>
          <h2>Duyệt sản phẩm đối tác</h2>
          <p class="heading-description">
            Kiểm tra thông tin sản phẩm do đối tác gửi trước khi hiển thị trên hệ thống.
          </p>
        </div>

        <div class="heading-statistics">
          <div class="heading-stat">
            <strong>{{ danhSachSanPham.length }}</strong>
            <span>Chờ duyệt</span>
          </div>

          <div class="heading-stat">
            <strong>{{ totalPartner }}</strong>
            <span>Đối tác gửi</span>
          </div>

          <div class="heading-stat">
            <strong>{{ totalQuantity }}</strong>
            <span>Tổng số lượng</span>
          </div>
        </div>
      </div>

      <div
          v-if="errorMessage"
          class="error-alert"
      >
        <i class="fa-solid fa-circle-exclamation"></i>

        <div>
          <strong>Không thể tải dữ liệu</strong>
          <p>{{ errorMessage }}</p>
        </div>

        <button
            type="button"
            @click="fetchDanhSachSanPhamChoDuyet"
        >
          Thử lại
        </button>
      </div>

      <div class="card">
        <div class="filter-row">
          <div class="search-box">
            <span class="search-label">Tìm</span>
            <input
                v-model="keyword"
                type="text"
                placeholder="Tìm tên sản phẩm, mã, đối tác, loại..."
            />
          </div>

          <select
              v-model="categoryFilter"
              aria-label="Lọc loại sản phẩm"
          >
            <option value="all">Tất cả loại sản phẩm</option>

            <option
                v-for="category in categories"
                :key="category"
                :value="category"
            >
              {{ category }}
            </option>
          </select>

          <select
              v-model="partnerFilter"
              aria-label="Lọc đối tác"
          >
            <option value="all">Tất cả đối tác</option>

            <option
                v-for="partner in partners"
                :key="partner"
                :value="partner"
            >
              {{ partner }}
            </option>
          </select>

          <button
              class="reset-btn"
              type="button"
              :disabled="
              !keyword &&
              categoryFilter === 'all' &&
              partnerFilter === 'all'
            "
              @click="resetFilters"
          >
            <i class="fa-solid fa-filter-circle-xmark"></i>
            Xóa lọc
          </button>

          <button
              class="reload-btn"
              type="button"
              :disabled="isLoading"
              @click="fetchDanhSachSanPhamChoDuyet"
          >
            <i
                class="fa-solid"
                :class="
                isLoading
                  ? 'fa-spinner fa-spin'
                  : 'fa-rotate-right'
              "
            ></i>
            Tải lại
          </button>
        </div>

        <div
            v-if="isLoading"
            class="table-state"
        >
          <i class="fa-solid fa-spinner fa-spin"></i>
          Đang tải danh sách sản phẩm...
        </div>

        <div
            v-else
            class="table-wrapper"
        >
          <table class="product-table">
            <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Đối tác</th>
              <th>Loại</th>
              <th class="text-center">Số lượng</th>
              <th>Giá bán</th>
              <th>Trạng thái</th>
              <th>Ngày gửi</th>
              <th class="text-center">Hành động</th>
            </tr>
            </thead>

            <tbody>
            <tr
                v-for="product in pagedProducts"
                :key="product.id"
            >
              <td data-label="Sản phẩm">
                <div class="product-cell">
                  <button
                      class="product-image-button"
                      type="button"
                      title="Xem chi tiết sản phẩm"
                      @click="openDetailModal(product)"
                  >
                    <img
                        v-if="product.image"
                        :src="getImageUrl(product.image)"
                        :alt="product.name || 'Ảnh sản phẩm'"
                    />

                    <span v-else>
                        <i class="fa-regular fa-image"></i>
                      </span>
                  </button>

                  <div class="product-main-info">
                    <button
                        class="product-name"
                        type="button"
                        @click="openDetailModal(product)"
                    >
                      {{ product.name || "Chưa có tên" }}
                    </button>

                    <p>
                      #SP{{ String(product.id).padStart(4, "0") }}
                      <span>•</span>
                      {{ product.vatLieu || "Chưa có vật liệu" }}
                    </p>
                  </div>
                </div>
              </td>

              <td data-label="Đối tác">
                <div class="partner-cell">
                    <span class="partner-icon">
                      <i class="fa-solid fa-building"></i>
                    </span>

                  <div>
                    <strong>
                      {{ product.tenDoiTac || "Không rõ đối tác" }}
                    </strong>
                    <small>Nhà cung cấp</small>
                  </div>
                </div>
              </td>

              <td data-label="Loại">
                  <span class="category-badge">
                    {{ product.loai || "Chưa phân loại" }}
                  </span>
              </td>

              <td
                  data-label="Số lượng"
                  class="text-center"
              >
                  <span class="quantity-value">
                    {{ product.soLuong ?? 0 }}
                  </span>
              </td>

              <td data-label="Giá bán">
                <strong class="price-value">
                  {{ formatCurrency(product.price) }}
                </strong>
              </td>

              <td data-label="Trạng thái">
                  <span class="status-badge">
                    <span class="status-dot"></span>
                    {{ product.tenTrangThai || "Chờ duyệt" }}
                  </span>
              </td>

              <td data-label="Ngày gửi">
                  <span class="date-value">
                    {{ formatDateTime(product.ngayTao) }}
                  </span>
              </td>

              <td
                  data-label="Hành động"
                  class="text-center"
              >
                <div class="action-group">
                  <button
                      class="view-btn"
                      type="button"
                      title="Xem chi tiết"
                      @click="openDetailModal(product)"
                  >
                    <i class="fa-solid fa-eye"></i>
                  </button>

                  <button
                      class="approve-btn"
                      type="button"
                      :disabled="loadingStates[product.id]"
                      @click="confirmDuyet(product)"
                  >
                    <i
                        class="fa-solid"
                        :class="
                          loadingStates[product.id] === 'duyet'
                            ? 'fa-spinner fa-spin'
                            : 'fa-check'
                        "
                    ></i>
                    Duyệt
                  </button>

                  <button
                      class="reject-btn"
                      type="button"
                      :disabled="loadingStates[product.id]"
                      @click="confirmTuChoi(product)"
                  >
                    <i
                        class="fa-solid"
                        :class="
                          loadingStates[product.id] === 'tuChoi'
                            ? 'fa-spinner fa-spin'
                            : 'fa-xmark'
                        "
                    ></i>
                    Từ chối
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="!pagedProducts.length">
              <td colspan="8">
                <div class="table-state empty">
                  <i class="fa-regular fa-folder-open"></i>

                  <strong>
                    {{
                      danhSachSanPham.length
                          ? "Không có sản phẩm phù hợp"
                          : "Không có sản phẩm đang chờ duyệt"
                    }}
                  </strong>

                  <p v-if="danhSachSanPham.length">
                    Hãy thử thay đổi từ khóa hoặc bộ lọc.
                  </p>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination-row">
          <p>
            Hiển thị {{ displayFrom }} - {{ displayTo }} của
            {{ filteredProducts.length }} sản phẩm
          </p>

          <div class="pagination">
            <button
                type="button"
                :disabled="currentPage === 1"
                @click="changePage(currentPage - 1)"
            >
              &lt;
            </button>

            <button
                v-for="(page, index) in visiblePages"
                :key="`${page}-${index}`"
                type="button"
                :class="{
                active: page === currentPage,
                dots: page === '...'
              }"
                :disabled="page === '...'"
                @click="changePage(page)"
            >
              {{ page }}
            </button>

            <button
                type="button"
                :disabled="currentPage === totalPages"
                @click="changePage(currentPage + 1)"
            >
              &gt;
            </button>
          </div>
        </div>
      </div>

      <div class="legend">
        <div>
          <h5>Quy trình duyệt sản phẩm</h5>

          <div class="process-list">
            <span>
              <span class="process-number">1</span>
              Kiểm tra hình ảnh
            </span>

            <span>
              <span class="process-number">2</span>
              Đối chiếu thông tin
            </span>

            <span>
              <span class="process-number">3</span>
              Duyệt hoặc từ chối
            </span>
          </div>
        </div>

        <div>
          <h5>Lưu ý kiểm duyệt</h5>
          <p>
            Chỉ duyệt khi tên, hình ảnh, giá bán, số lượng và thông tin đối tác
            đầy đủ. Khi từ chối cần ghi lý do rõ ràng để đối tác có thể chỉnh sửa.
          </p>
        </div>
      </div>
    </section>

    <PopXemSanPham
        v-model="showDetailModal"
        :product="detailProduct"
        :loading-action="
        detailProduct
          ? loadingStates[detailProduct.id] || ''
          : ''
      "
        @approve="handleApproveFromDetail"
        @reject="handleRejectFromDetail"
        @closed="handleDetailClosed"
    />

    <div
        v-if="showRejectModal"
        class="modal-overlay"
        @click.self="closeRejectModal"
    >
      <div class="reject-modal">
        <div class="modal-header">
          <div>
            <p class="modal-eyebrow">TỪ CHỐI SẢN PHẨM</p>
            <h3>Nhập lý do từ chối</h3>
            <span>
              Thông tin này sẽ được gửi lại cho đối tác.
            </span>
          </div>

          <button
              class="modal-close"
              type="button"
              :disabled="isRejecting"
              aria-label="Đóng"
              @click="closeRejectModal"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="modal-body">
          <div
              v-if="selectedProduct"
              class="reject-product-card"
          >
            <div class="reject-image">
              <img
                  v-if="selectedProduct.image"
                  :src="getImageUrl(selectedProduct.image)"
                  :alt="selectedProduct.name"
              />

              <i
                  v-else
                  class="fa-regular fa-image"
              ></i>
            </div>

            <div>
              <strong>{{ selectedProduct.name }}</strong>
              <p>
                #SP{{ String(selectedProduct.id).padStart(4, "0") }}
              </p>
              <span>
                {{ selectedProduct.tenDoiTac || "Không rõ đối tác" }}
              </span>
            </div>
          </div>

          <div class="form-group">
            <label>
              Lý do từ chối
              <span>*</span>
            </label>

            <textarea
                v-model="lyDoTuChoi"
                rows="5"
                maxlength="500"
                :disabled="isRejecting"
                placeholder="Ví dụ: Hình ảnh chưa rõ, giá bán chưa hợp lệ hoặc thông tin sản phẩm chưa đầy đủ..."
            ></textarea>

            <div class="field-footer">
              <small>
                Tối thiểu 3 ký tự
              </small>

              <small>
                {{ lyDoTuChoi.length }}/500
              </small>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button
              class="cancel-btn"
              type="button"
              :disabled="isRejecting"
              @click="closeRejectModal"
          >
            Hủy
          </button>

          <button
              class="confirm-reject-btn"
              type="button"
              :disabled="isRejecting || lyDoTuChoi.trim().length < 3"
              @click="submitTuChoi"
          >
            <i
                class="fa-solid"
                :class="
                isRejecting
                  ? 'fa-spinner fa-spin'
                  : 'fa-ban'
              "
            ></i>

            {{
              isRejecting
                  ? "Đang xử lý..."
                  : "Xác nhận từ chối"
            }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.product-approval-page {
  min-height: 100%;
  background: #f5f7fb;
  color: #172033;
}

.page-content {
  width: 100%;
  padding: 28px;
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 28px;
  margin-bottom: 22px;
}

.eyebrow,
.modal-eyebrow {
  margin: 0 0 7px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1.4px;
}

.page-heading h2 {
  margin: 0;
  color: #172033;
  font-size: 29px;
  font-weight: 800;
  letter-spacing: -0.6px;
}

.heading-description {
  margin: 8px 0 0;
  color: #728097;
  font-size: 14px;
}

.heading-statistics {
  display: flex;
  gap: 10px;
}

.heading-stat {
  min-width: 118px;
  padding: 13px 16px;
  border: 1px solid #e7ebf2;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(24, 39, 75, 0.05);
}

.heading-stat strong,
.heading-stat span {
  display: block;
}

.heading-stat strong {
  color: #172033;
  font-size: 20px;
  line-height: 1;
}

.heading-stat span {
  margin-top: 7px;
  color: #7d889b;
  font-size: 12px;
}

.error-alert {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-bottom: 18px;
  padding: 15px 18px;
  border: 1px solid #fecaca;
  border-radius: 12px;
  background: #fff1f2;
  color: #be123c;
}

.error-alert > i {
  font-size: 19px;
}

.error-alert div {
  flex: 1;
}

.error-alert strong {
  display: block;
  margin-bottom: 3px;
}

.error-alert p {
  margin: 0;
  color: #9f1239;
  font-size: 13px;
}

.error-alert button {
  min-height: 35px;
  padding: 0 14px;
  border: 1px solid #f4a8b4;
  border-radius: 8px;
  background: #ffffff;
  color: #be123c;
  font-weight: 700;
  cursor: pointer;
}

.card {
  overflow: hidden;
  border: 1px solid #e7ebf2;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 12px 34px rgba(24, 39, 75, 0.06);
}

.filter-row {
  display: grid;
  grid-template-columns:
    minmax(280px, 1fr)
    200px
    190px
    auto
    auto;
  gap: 12px;
  align-items: center;
  padding: 18px;
  border-bottom: 1px solid #edf0f5;
}

.search-box {
  display: flex;
  align-items: center;
  min-width: 0;
  height: 42px;
  overflow: hidden;
  border: 1px solid #dce2eb;
  border-radius: 9px;
  background: #ffffff;
  transition: 0.2s ease;
}

.search-box:focus-within {
  border-color: #7da4ff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.09);
}

.search-label {
  display: grid;
  place-items: center;
  align-self: stretch;
  min-width: 55px;
  border-right: 1px solid #e7ebf2;
  background: #f8fafc;
  color: #536176;
  font-size: 13px;
  font-weight: 700;
}

.search-box input {
  width: 100%;
  min-width: 0;
  height: 100%;
  padding: 0 13px;
  border: 0;
  outline: none;
  color: #263247;
  font-size: 13px;
}

.filter-row select {
  width: 100%;
  height: 42px;
  padding: 0 36px 0 12px;
  border: 1px solid #dce2eb;
  border-radius: 9px;
  outline: none;
  background: #ffffff;
  color: #445168;
  font-size: 13px;
}

.filter-row select:focus {
  border-color: #7da4ff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.09);
}

.reset-btn,
.reload-btn,
.view-btn,
.approve-btn,
.reject-btn,
.cancel-btn,
.confirm-reject-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 9px;
  font-weight: 700;
  cursor: pointer;
  transition:
      transform 0.16s ease,
      box-shadow 0.16s ease,
      background 0.16s ease,
      border-color 0.16s ease;
}

.reset-btn,
.reload-btn {
  min-height: 42px;
  padding: 0 15px;
  white-space: nowrap;
}

.reset-btn {
  border: 1px solid #dce2eb;
  background: #ffffff;
  color: #627086;
}

.reset-btn:hover:not(:disabled) {
  background: #f7f9fc;
}

.reload-btn {
  border: 0;
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.2);
}

.reload-btn:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
  transform: none !important;
}

.table-wrapper {
  overflow-x: auto;
}

.product-table {
  width: 100%;
  min-width: 1220px;
  border-collapse: collapse;
}

.product-table th {
  padding: 14px 16px;
  border-bottom: 1px solid #e8edf4;
  background: #f8fafc;
  color: #66758d;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.25px;
  text-align: left;
  text-transform: uppercase;
  white-space: nowrap;
}

.product-table td {
  padding: 15px 16px;
  border-bottom: 1px solid #eef1f5;
  color: #38465c;
  font-size: 13px;
  vertical-align: middle;
}

.product-table tbody tr {
  transition: background 0.17s ease;
}

.product-table tbody tr:hover {
  background: #fbfcff;
}

.product-table tbody tr:last-child td {
  border-bottom: 0;
}

.text-center {
  text-align: center !important;
}

.product-cell {
  display: flex;
  align-items: center;
  min-width: 265px;
  gap: 12px;
}

.product-image-button {
  display: grid;
  place-items: center;
  flex: 0 0 58px;
  width: 58px;
  height: 58px;
  overflow: hidden;
  padding: 0;
  border: 1px solid #e1e7ef;
  border-radius: 10px;
  background: #f7f9fc;
  color: #a1abba;
  cursor: pointer;
}

.product-image-button img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image-button span {
  font-size: 20px;
}

.product-main-info {
  min-width: 0;
}

.product-name {
  display: block;
  max-width: 245px;
  overflow: hidden;
  padding: 0;
  border: 0;
  background: transparent;
  color: #202c40;
  font-size: 13px;
  font-weight: 800;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.product-name:hover {
  color: #2563eb;
}

.product-main-info p {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 5px 0 0;
  color: #8a95a8;
  font-size: 11px;
}

.partner-cell {
  display: flex;
  align-items: center;
  min-width: 185px;
  gap: 9px;
}

.partner-icon {
  display: grid;
  place-items: center;
  flex: 0 0 31px;
  width: 31px;
  height: 31px;
  border-radius: 8px;
  background: #eef2ff;
  color: #4f46e5;
}

.partner-cell strong,
.partner-cell small {
  display: block;
}

.partner-cell strong {
  max-width: 175px;
  overflow: hidden;
  color: #354257;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.partner-cell small {
  margin-top: 3px;
  color: #8f99aa;
  font-size: 10px;
}

.category-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 28px;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
  white-space: nowrap;
}

.category-badge {
  background: #eef2ff;
  color: #4f46e5;
}

.status-badge {
  background: #fff5db;
  color: #a86400;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f59e0b;
}

.quantity-value {
  display: inline-grid;
  place-items: center;
  min-width: 38px;
  height: 30px;
  padding: 0 8px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #455268;
  font-weight: 800;
}

.price-value {
  color: #dc3545;
  white-space: nowrap;
}

.date-value {
  display: block;
  min-width: 125px;
  color: #78859a;
  font-size: 12px;
  line-height: 1.45;
}

.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 245px;
}

.view-btn,
.approve-btn,
.reject-btn {
  min-height: 34px;
  border: 1px solid;
  font-size: 12px;
}

.view-btn {
  width: 35px;
  padding: 0;
  border-color: #bcd0ff;
  background: #f5f8ff;
  color: #2563eb;
}

.view-btn:hover {
  border-color: #8aafff;
  background: #eaf1ff;
}

.approve-btn,
.reject-btn {
  padding: 0 12px;
}

.approve-btn {
  border-color: #a9ddbf;
  background: #f0fbf5;
  color: #16844b;
}

.approve-btn:hover:not(:disabled) {
  border-color: #73c796;
  background: #def6e8;
}

.reject-btn {
  border-color: #f3bdc7;
  background: #fff7f8;
  color: #c82945;
}

.reject-btn:hover:not(:disabled) {
  border-color: #e894a4;
  background: #ffebef;
}

.table-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 240px;
  padding: 35px 20px;
  color: #748198;
  font-size: 14px;
}

.table-state.empty {
  min-height: 210px;
  flex-direction: column;
}

.table-state.empty i {
  color: #aab3c2;
  font-size: 31px;
}

.table-state.empty strong {
  color: #59677c;
}

.table-state.empty p {
  margin: 0;
  color: #929cad;
  font-size: 12px;
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 15px 19px;
  border-top: 1px solid #edf0f5;
  background: #ffffff;
}

.pagination-row p {
  margin: 0;
  color: #778399;
  font-size: 13px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pagination button {
  display: grid;
  place-items: center;
  min-width: 32px;
  height: 32px;
  padding: 0 9px;
  border: 1px solid #dfe4ec;
  border-radius: 7px;
  background: #ffffff;
  color: #5c697e;
  font-size: 12px;
  cursor: pointer;
}

.pagination button:hover:not(:disabled):not(.active) {
  border-color: #9db9fa;
  background: #f4f7ff;
  color: #2563eb;
}

.pagination button.active {
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.pagination button.dots {
  border-color: transparent;
  background: transparent;
}

.legend {
  display: grid;
  grid-template-columns:
    minmax(340px, 1fr)
    minmax(360px, 1fr);
  gap: 18px;
  margin-top: 18px;
  padding: 20px 22px;
  border: 1px solid #e7ebf2;
  border-radius: 13px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(24, 39, 75, 0.04);
}

.legend h5 {
  margin: 0 0 12px;
  color: #344158;
  font-size: 13px;
}

.legend p {
  margin: 0;
  color: #7d899d;
  font-size: 12px;
  line-height: 1.7;
}

.process-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
}

.process-list > span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #657288;
  font-size: 12px;
}

.process-number {
  display: grid;
  place-items: center;
  width: 21px;
  height: 21px;
  border-radius: 50%;
  background: #eaf1ff;
  color: #2563eb;
  font-size: 10px;
  font-weight: 800;
}

.modal-overlay {
  position: fixed;
  z-index: 12000;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22px;
  background: rgba(20, 29, 45, 0.58);
  backdrop-filter: blur(3px);
}

.reject-modal {
  display: flex;
  flex-direction: column;
  width: min(570px, 100%);
  max-height: calc(100vh - 44px);
  overflow: hidden;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 25px 80px rgba(13, 25, 50, 0.28);
  animation: modal-in 0.18s ease-out;
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px 18px;
  border-bottom: 1px solid #edf0f5;
}

.modal-header h3 {
  margin: 0;
  color: #1f2a3d;
  font-size: 21px;
  font-weight: 800;
}

.modal-header span {
  display: block;
  margin-top: 6px;
  color: #8792a4;
  font-size: 12px;
}

.modal-close {
  display: grid;
  place-items: center;
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  border: 1px solid #e2e7ee;
  border-radius: 9px;
  background: #ffffff;
  color: #778398;
  cursor: pointer;
}

.modal-close:hover:not(:disabled) {
  background: #f6f8fb;
  color: #263247;
}

.modal-body {
  overflow-y: auto;
  padding: 22px 24px;
}

.reject-product-card {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-bottom: 19px;
  padding: 13px;
  border: 1px solid #e4e9f0;
  border-radius: 11px;
  background: #f8fafc;
}

.reject-image {
  display: grid;
  place-items: center;
  flex: 0 0 58px;
  width: 58px;
  height: 58px;
  overflow: hidden;
  border: 1px solid #dde4ec;
  border-radius: 9px;
  background: #ffffff;
  color: #a1abba;
}

.reject-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reject-product-card strong,
.reject-product-card p,
.reject-product-card span {
  display: block;
}

.reject-product-card strong {
  color: #263247;
  font-size: 13px;
}

.reject-product-card p {
  margin: 4px 0;
  color: #8792a4;
  font-size: 11px;
}

.reject-product-card span {
  color: #627086;
  font-size: 12px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #4a576d;
  font-size: 12px;
  font-weight: 800;
}

.form-group label span {
  color: #dc3545;
}

.form-group textarea {
  width: 100%;
  min-height: 125px;
  padding: 12px;
  border: 1px solid #dce2eb;
  border-radius: 9px;
  outline: none;
  background: #ffffff;
  color: #273449;
  font: inherit;
  font-size: 13px;
  line-height: 1.55;
  resize: vertical;
}

.form-group textarea:focus {
  border-color: #7da4ff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.09);
}

.field-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
}

.field-footer small {
  color: #8c97a9;
  font-size: 11px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #edf0f5;
  background: #fbfcfe;
}

.cancel-btn,
.confirm-reject-btn {
  min-height: 40px;
  padding: 0 18px;
  border: 1px solid;
  font-size: 13px;
}

.cancel-btn {
  border-color: #dce2eb;
  background: #ffffff;
  color: #5f6c81;
}

.cancel-btn:hover:not(:disabled) {
  background: #f5f7fa;
}

.confirm-reject-btn {
  border-color: #c92a43;
  background: #c92a43;
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(201, 42, 67, 0.18);
}

.confirm-reject-btn:hover:not(:disabled) {
  border-color: #b91c36;
  background: #b91c36;
}

@media (max-width: 1250px) {
  .filter-row {
    grid-template-columns:
      minmax(280px, 1fr)
      190px
      180px;
  }

  .reset-btn,
  .reload-btn {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .page-content {
    padding: 20px;
  }

  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .heading-statistics {
    width: 100%;
  }

  .heading-stat {
    flex: 1;
  }

  .filter-row {
    grid-template-columns: 1fr 1fr;
  }

  .search-box {
    grid-column: 1 / -1;
  }

  .legend {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .page-content {
    padding: 14px;
  }

  .page-heading h2 {
    font-size: 24px;
  }

  .heading-statistics {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
  }

  .heading-stat {
    min-width: 0;
    padding: 11px;
  }

  .heading-stat strong {
    font-size: 17px;
  }

  .heading-stat span {
    font-size: 10px;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .search-box {
    grid-column: auto;
  }

  .pagination-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .modal-overlay {
    align-items: flex-end;
    padding: 0;
  }

  .reject-modal {
    width: 100%;
    max-height: 94vh;
    border-radius: 16px 16px 0 0;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding-right: 18px;
    padding-left: 18px;
  }
}
</style>
