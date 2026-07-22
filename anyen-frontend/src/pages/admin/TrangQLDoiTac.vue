<template>
  <div class="partner-page">
    <!-- HEADER -->
    <div class="page-header">
      <div>
        <h3 class="page-title">Quản Lý Đối Tác</h3>

        <p class="page-subtitle">
          Theo dõi thông tin, trạng thái hợp tác và quyền xóa của từng đối tác.
        </p>
      </div>

      <button
          type="button"
          class="btn btn-add-partner"
          @click="openCreateModal"
      >
        <i class="bi bi-plus-circle"></i>
        Thêm đối tác mới
      </button>
    </div>

    <!-- THÔNG BÁO -->
    <div
        v-if="successMessage"
        class="partner-alert"
        role="alert"
    >
      <div class="partner-alert-content">
        <i class="bi bi-check-circle-fill"></i>

        <div>
          <strong>Thành công!</strong>
          <span>{{ successMessage }}</span>
        </div>
      </div>

      <button
          type="button"
          class="partner-alert-close"
          aria-label="Đóng"
          @click="successMessage = ''"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!-- BỘ LỌC -->
    <div class="filter-card">
      <div class="filter-card-body">
        <div class="row g-3 align-items-end">
          <div class="col-lg-6 col-md-12">
            <label class="filter-label">
              Tìm kiếm đối tác
            </label>

            <div class="search-wrapper">
              <i class="bi bi-search search-icon"></i>

              <input
                  v-model="keyword"
                  type="text"
                  class="form-control search-input"
                  placeholder="Tên đối tác, doanh nghiệp, email, số điện thoại..."
                  @input="currentPage = 1"
              />
            </div>
          </div>

          <div class="col-lg-3 col-md-6">
            <label class="filter-label">
              Trạng thái
            </label>

            <select
                v-model="statusFilter"
                class="form-select"
                @change="currentPage = 1"
            >
              <option value="all">Tất cả trạng thái</option>
              <option value="1">Đang hợp tác</option>
              <option value="0">Ngưng hợp tác</option>
              <option value="2">Chờ xác nhận</option>
            </select>
          </div>

          <div class="col-lg-3 col-md-6">
            <div class="d-flex gap-2">
              <button
                  type="button"
                  class="btn btn-reset-filter flex-grow-1"
                  @click="resetFilter"
              >
                <i class="bi bi-funnel"></i>
                Xóa lọc
              </button>

              <button
                  type="button"
                  class="btn btn-refresh"
                  :disabled="isLoading"
                  title="Tải lại danh sách"
                  @click="fetchDoiTac"
              >
                <span
                    v-if="isLoading"
                    class="spinner-border spinner-border-sm"
                ></span>

                <i
                    v-else
                    class="bi bi-arrow-clockwise"
                ></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- BẢNG -->
    <div class="partner-table-card">
      <div class="table-responsive">
        <table class="table partner-table align-middle">
          <thead>
          <tr>
            <th class="ps-4">Mã</th>
            <th>Đối tác</th>

            <!-- ĐÃ SỬA CĂN CỘT LIÊN HỆ -->
            <th class="contact-header">
              Liên hệ
            </th>

            <th>Tài khoản</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th class="text-end pe-4">
              Hành động
            </th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="isLoading">
            <td
                colspan="7"
                class="loading-row text-center"
            >
              <span class="spinner-border spinner-border-sm me-2"></span>
              Đang tải danh sách đối tác...
            </td>
          </tr>

          <tr v-else-if="pagedList.length === 0">
            <td
                colspan="7"
                class="empty-state"
            >
              <div class="empty-icon">
                <i class="bi bi-buildings"></i>
              </div>

              <div class="fw-semibold">
                Không tìm thấy dữ liệu đối tác
              </div>

              <div class="small mt-1">
                Hãy thử thay đổi từ khóa hoặc bộ lọc trạng thái.
              </div>
            </td>
          </tr>

          <template v-else>
            <tr
                v-for="dt in pagedList"
                :key="dt.maDoiTac"
            >
              <td class="ps-4">
                  <span class="partner-code">
                    #{{ dt.maDoiTac }}
                  </span>
              </td>

              <td>
                <div class="partner-info">
                  <div class="partner-avatar">
                    {{ getInitials(dt.tenDoiTac) }}
                  </div>

                  <div class="partner-detail">
                    <div class="partner-name">
                      {{ dt.tenDoiTac || "Chưa cập nhật" }}
                    </div>

                    <div
                        class="partner-company"
                        :title="dt.tenDoanhNghiep"
                    >
                      {{
                        dt.tenDoanhNghiep ||
                        "Chưa có tên doanh nghiệp"
                      }}
                    </div>

                    <div class="partner-tax">
                      MST: {{ dt.maSoThue || "Chưa có" }}
                    </div>
                  </div>
                </div>
              </td>

              <!-- ĐÃ THÊM CLASS CONTACT-CELL -->
              <td class="contact-cell">
                <div class="contact-line">
                  <i class="bi bi-envelope"></i>

                  <span>
                      {{ dt.email || "Chưa có email" }}
                    </span>
                </div>

                <div class="contact-line">
                  <i class="bi bi-telephone"></i>

                  <span>
                      {{ dt.soDienThoai || "Chưa có SĐT" }}
                    </span>
                </div>

                <div
                    class="contact-line address-cell"
                    :title="dt.diaChi"
                >
                  <i class="bi bi-geo-alt"></i>

                  <span>
                      {{ dt.diaChi || "Chưa có địa chỉ" }}
                    </span>
                </div>
              </td>

              <td>
                  <span class="account-name">
                    {{ dt.tenDangNhap || "Chưa có" }}
                  </span>
              </td>

              <td>
                  <span
                      class="status-badge"
                      :class="getStatusClass(dt.trangThai)"
                  >
                    <span class="status-dot"></span>

                    {{ hienThiTrangThai(dt.trangThai) }}
                  </span>
              </td>

              <td>
                <div class="date-cell">
                  <i class="bi bi-calendar3"></i>

                  {{ formatDate(dt.createdAt) }}
                </div>
              </td>

              <td class="text-end pe-4">
                <div class="action-buttons">
                  <button
                      type="button"
                      class="btn action-btn btn-edit"
                      :disabled="rowLoading[dt.maDoiTac]"
                      @click="openEditModal(dt)"
                  >
                    <i class="bi bi-pencil-square"></i>
                    Sửa
                  </button>

                  <!-- ĐANG HỢP TÁC -->
                  <button
                      v-if="Number(dt.trangThai) === 1"
                      type="button"
                      class="btn action-btn btn-stop"
                      :disabled="rowLoading[dt.maDoiTac]"
                      @click="confirmChangeStatus(dt, 0)"
                  >
                      <span
                          v-if="rowLoading[dt.maDoiTac]"
                          class="spinner-border spinner-border-sm"
                      ></span>

                    <i
                        v-else
                        class="bi bi-pause-circle"
                    ></i>

                    Ngưng hợp tác
                  </button>

                  <!-- NGƯNG HỢP TÁC -->
                  <button
                      v-else-if="Number(dt.trangThai) === 0"
                      type="button"
                      class="btn action-btn btn-cooperate"
                      :disabled="rowLoading[dt.maDoiTac]"
                      @click="confirmChangeStatus(dt, 1)"
                  >
                      <span
                          v-if="rowLoading[dt.maDoiTac]"
                          class="spinner-border spinner-border-sm"
                      ></span>

                    <i
                        v-else
                        class="bi bi-play-circle"
                    ></i>

                    Hợp tác
                  </button>

                  <!-- ĐƯỢC PHÉP XÓA -->
                  <button
                      v-if="coTheXoaDoiTac(dt)"
                      type="button"
                      class="btn action-btn btn-delete"
                      :disabled="rowLoading[dt.maDoiTac]"
                      @click="confirmDelete(dt)"
                  >
                    <i class="bi bi-trash3"></i>
                    Xóa
                  </button>

                  <!-- KHÔNG ĐƯỢC XÓA -->
                  <button
                      v-else
                      type="button"
                      class="btn action-btn btn-cannot-delete"
                      disabled
                      title="Không thể xóa vì sản phẩm của đối tác đã phát sinh đơn hàng"
                  >
                    <i class="bi bi-lock"></i>
                    Không thể xóa
                  </button>
                </div>
              </td>
            </tr>
          </template>
          </tbody>
        </table>
      </div>

      <!-- PHÂN TRANG -->
      <div class="pagination-bar">
        <span class="pag-info">
          Hiển thị
          <strong>{{ showingFrom }}</strong>
          -
          <strong>{{ showingTo }}</strong>
          trong tổng số
          <strong>{{ filteredList.length }}</strong>
          đối tác
        </span>

        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredList.length"
            layout="prev, pager, next"
        />
      </div>
    </div>

    <!-- MODAL THÊM -->
    <Teleport to="body">
      <div
          v-if="showCreateModal"
          class="modal-backdrop fade show partner-modal-backdrop"
      ></div>

      <div
          v-if="showCreateModal"
          class="modal fade show d-block partner-modal"
          tabindex="-1"
          role="dialog"
          aria-modal="true"
      >
        <div
            class="modal-dialog modal-lg modal-dialog-centered"
            role="document"
        >
          <div class="modal-content">
            <div class="modal-header">
              <div>
                <h5 class="modal-title">
                  Thêm đối tác mới
                </h5>

                <div class="modal-subtitle">
                  Gửi lời mời hợp tác qua email.
                </div>
              </div>

              <button
                  type="button"
                  class="btn-close"
                  aria-label="Đóng"
                  @click="closeCreateModal"
              ></button>
            </div>

            <div class="modal-body">
              <form @submit.prevent="submitCreateDoiTac">
                <div class="row g-3">
                  <div class="col-12">
                    <label class="form-label">
                      Email đối tác
                      <span class="text-danger">*</span>
                    </label>

                    <input
                        v-model="form.email"
                        type="email"
                        class="form-control"
                        :class="{ 'is-invalid': errors.email }"
                        placeholder="example@doitac.com"
                        autocomplete="email"
                    />

                    <div class="invalid-feedback">
                      {{ errors.email }}
                    </div>

                    <div class="form-text mt-3">
                      <i class="bi bi-info-circle me-1"></i>

                      Đối tác sẽ nhận được email chứa đường dẫn xác nhận và
                      tự điền thông tin doanh nghiệp, mã số thuế, số điện
                      thoại và địa chỉ.
                    </div>
                  </div>
                </div>
              </form>
            </div>

            <div class="modal-footer">
              <button
                  type="button"
                  class="btn btn-light modal-cancel-btn"
                  @click="closeCreateModal"
              >
                Hủy
              </button>

              <button
                  type="button"
                  class="btn btn-primary modal-submit-btn"
                  :disabled="isSubmitting"
                  @click="submitCreateDoiTac"
              >
                <span
                    v-if="isSubmitting"
                    class="spinner-border spinner-border-sm me-2"
                ></span>

                <i
                    v-else
                    class="bi bi-send me-1"
                ></i>

                Gửi lời mời hợp tác
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- MODAL SỬA -->
    <Teleport to="body">
      <div
          v-if="showEditModal"
          class="modal-backdrop fade show partner-modal-backdrop"
      ></div>

      <div
          v-if="showEditModal"
          class="modal fade show d-block partner-modal"
          tabindex="-1"
          role="dialog"
          aria-modal="true"
      >
        <div
            class="modal-dialog modal-lg modal-dialog-centered"
            role="document"
        >
          <div class="modal-content">
            <div class="modal-header">
              <div>
                <h5 class="modal-title">
                  Sửa thông tin đối tác
                </h5>

                <div class="modal-subtitle">
                  Cập nhật thông tin liên hệ và doanh nghiệp.
                </div>
              </div>

              <button
                  type="button"
                  class="btn-close"
                  aria-label="Đóng"
                  @click="closeEditModal"
              ></button>
            </div>

            <div class="modal-body">
              <form @submit.prevent="submitUpdateDoiTac">
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">
                      Tên đối tác
                      <span class="text-danger">*</span>
                    </label>

                    <input
                        v-model="editForm.tenDoiTac"
                        type="text"
                        class="form-control"
                        :class="{ 'is-invalid': editErrors.tenDoiTac }"
                    />

                    <div class="invalid-feedback">
                      {{ editErrors.tenDoiTac }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">
                      Tên doanh nghiệp
                    </label>

                    <input
                        v-model="editForm.tenDoanhNghiep"
                        type="text"
                        class="form-control"
                        :class="{ 'is-invalid': editErrors.tenDoanhNghiep }"
                    />

                    <div class="invalid-feedback">
                      {{ editErrors.tenDoanhNghiep }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">
                      Mã số thuế
                    </label>

                    <input
                        v-model="editForm.maSoThue"
                        type="text"
                        class="form-control"
                        :class="{ 'is-invalid': editErrors.maSoThue }"
                    />

                    <div class="invalid-feedback">
                      {{ editErrors.maSoThue }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">
                      Số điện thoại
                      <span class="text-danger">*</span>
                    </label>

                    <input
                        v-model="editForm.soDienThoai"
                        type="text"
                        class="form-control"
                        maxlength="10"
                        :class="{ 'is-invalid': editErrors.soDienThoai }"
                    />

                    <div class="invalid-feedback">
                      {{ editErrors.soDienThoai }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">
                      Email
                      <span class="text-danger">*</span>
                    </label>

                    <input
                        v-model="editForm.email"
                        type="email"
                        class="form-control"
                        :class="{ 'is-invalid': editErrors.email }"
                    />

                    <div class="invalid-feedback">
                      {{ editErrors.email }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">
                      Địa chỉ
                    </label>

                    <input
                        v-model="editForm.diaChi"
                        type="text"
                        class="form-control"
                        :class="{ 'is-invalid': editErrors.diaChi }"
                    />

                    <div class="invalid-feedback">
                      {{ editErrors.diaChi }}
                    </div>
                  </div>
                </div>
              </form>
            </div>

            <div class="modal-footer">
              <button
                  type="button"
                  class="btn btn-light modal-cancel-btn"
                  @click="closeEditModal"
              >
                Hủy
              </button>

              <button
                  type="button"
                  class="btn btn-primary modal-submit-btn"
                  :disabled="isEditSubmitting"
                  @click="submitUpdateDoiTac"
              >
                <span
                    v-if="isEditSubmitting"
                    class="spinner-border spinner-border-sm me-2"
                ></span>

                <i
                    v-else
                    class="bi bi-check2-circle me-1"
                ></i>

                Lưu thay đổi
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import {
  computed,
  onMounted,
  onUnmounted,
  reactive,
  ref,
  watch,
} from "vue";

import {
  createDoiTac,
  deleteDoiTac,
  getAllDoiTac,
  updateDoiTac,
  updateTrangThaiDoiTac,
} from "../../services/QuanLyDoiTacService.js";

const showCreateModal = ref(false);
const showEditModal = ref(false);

const isSubmitting = ref(false);
const isEditSubmitting = ref(false);
const isLoading = ref(false);

const successMessage = ref("");
const danhSachDoiTac = ref([]);
const selectedId = ref(null);

const rowLoading = reactive({});

const keyword = ref("");
const statusFilter = ref("all");
const currentPage = ref(1);
const pageSize = ref(10);

const form = reactive({
  email: "",
});

const editForm = reactive({
  tenDoiTac: "",
  tenDoanhNghiep: "",
  maSoThue: "",
  email: "",
  soDienThoai: "",
  diaChi: "",
});

const errors = reactive({});
const editErrors = reactive({});

const filteredList = computed(() => {
  const kw = keyword.value
      .trim()
      .toLocaleLowerCase("vi");

  return danhSachDoiTac.value.filter((dt) => {
    const matchStatus =
        statusFilter.value === "all" ||
        String(Number(dt.trangThai)) === statusFilter.value;

    const searchText = [
      dt.maDoiTac,
      dt.tenDoiTac,
      dt.tenDoanhNghiep,
      dt.maSoThue,
      dt.email,
      dt.soDienThoai,
      dt.diaChi,
      dt.tenDangNhap,
      hienThiTrangThai(dt.trangThai),
    ]
        .filter(
            (value) =>
                value !== null &&
                value !== undefined
        )
        .join(" ")
        .toLocaleLowerCase("vi");

    return (
        matchStatus &&
        (!kw || searchText.includes(kw))
    );
  });
});

const totalPages = computed(() =>
    Math.max(
        1,
        Math.ceil(
            filteredList.value.length /
            pageSize.value
        )
    )
);

const pagedList = computed(() => {
  const start =
      (currentPage.value - 1) *
      pageSize.value;

  return filteredList.value.slice(
      start,
      start + pageSize.value
  );
});

const showingFrom = computed(() => {
  if (filteredList.value.length === 0) {
    return 0;
  }

  return (
      (currentPage.value - 1) *
      pageSize.value +
      1
  );
});

const showingTo = computed(() =>
    Math.min(
        currentPage.value * pageSize.value,
        filteredList.value.length
    )
);

watch(totalPages, (value) => {
  if (currentPage.value > value) {
    currentPage.value = value;
  }
});

watch(
    [showCreateModal, showEditModal],
    ([createVisible, editVisible]) => {
      document.body.style.overflow =
          createVisible || editVisible
              ? "hidden"
              : "";
    }
);

onMounted(() => {
  fetchDoiTac();
});

onUnmounted(() => {
  document.body.style.overflow = "";
});

async function fetchDoiTac() {
  isLoading.value = true;

  try {
    const data = await getAllDoiTac();

    danhSachDoiTac.value =
        Array.isArray(data)
            ? data.filter(
                (item) =>
                    Number(item.trangThai) !== 3
            )
            : [];

    currentPage.value = 1;
  } catch (error) {
    alert(
        getErrorMessage(
            error,
            "Không thể tải danh sách đối tác."
        )
    );
  } finally {
    isLoading.value = false;
  }
}

function resetFilter() {
  keyword.value = "";
  statusFilter.value = "all";
  currentPage.value = 1;
}

function clearErrors() {
  Object.keys(errors).forEach((key) => {
    delete errors[key];
  });
}

function clearEditErrors() {
  Object.keys(editErrors).forEach((key) => {
    delete editErrors[key];
  });
}

function resetForm() {
  form.email = "";
  clearErrors();
}

function resetEditForm() {
  editForm.tenDoiTac = "";
  editForm.tenDoanhNghiep = "";
  editForm.maSoThue = "";
  editForm.email = "";
  editForm.soDienThoai = "";
  editForm.diaChi = "";

  selectedId.value = null;

  clearEditErrors();
}

function openCreateModal() {
  resetForm();
  successMessage.value = "";
  showCreateModal.value = true;
}

function closeCreateModal() {
  showCreateModal.value = false;
  resetForm();
}

function openEditModal(doiTac) {
  resetEditForm();

  selectedId.value = doiTac.maDoiTac;

  editForm.tenDoiTac =
      doiTac.tenDoiTac || "";

  editForm.tenDoanhNghiep =
      doiTac.tenDoanhNghiep || "";

  editForm.maSoThue =
      doiTac.maSoThue || "";

  editForm.email =
      doiTac.email || "";

  editForm.soDienThoai =
      doiTac.soDienThoai || "";

  editForm.diaChi =
      doiTac.diaChi || "";

  successMessage.value = "";
  showEditModal.value = true;
}

function closeEditModal() {
  showEditModal.value = false;
  resetEditForm();
}

function validateCreateForm() {
  clearErrors();

  const email = form.email.trim();

  if (!email) {
    errors.email =
        "Địa chỉ email không được để trống.";

    return false;
  }

  if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
          email
      )
  ) {
    errors.email =
        "Địa chỉ email không đúng định dạng.";

    return false;
  }

  return true;
}

function validateEditForm() {
  clearEditErrors();

  let isValid = true;

  if (!editForm.tenDoiTac.trim()) {
    editErrors.tenDoiTac =
        "Tên đối tác không được để trống.";

    isValid = false;
  }

  if (!editForm.email.trim()) {
    editErrors.email =
        "Email không được để trống.";

    isValid = false;
  } else if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
          editForm.email.trim()
      )
  ) {
    editErrors.email =
        "Email không đúng định dạng.";

    isValid = false;
  }

  if (!editForm.soDienThoai.trim()) {
    editErrors.soDienThoai =
        "Số điện thoại không được để trống.";

    isValid = false;
  } else if (
      !/^0[35789][0-9]{8}$/.test(
          editForm.soDienThoai.trim()
      )
  ) {
    editErrors.soDienThoai =
        "Số điện thoại phải có 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09.";

    isValid = false;
  }

  return isValid;
}

function buildPayload(source) {
  const payload = { ...source };

  Object.keys(payload).forEach((key) => {
    if (typeof payload[key] === "string") {
      payload[key] = payload[key].trim();
    }
  });

  return payload;
}

async function submitCreateDoiTac() {
  if (!validateCreateForm()) {
    return;
  }

  isSubmitting.value = true;
  successMessage.value = "";

  try {
    await createDoiTac(
        buildPayload(form)
    );

    closeCreateModal();

    await fetchDoiTac();

    successMessage.value =
        "Đã gửi lời mời hợp tác đến email đối tác.";
  } catch (error) {
    handleCreateError(error);
  } finally {
    isSubmitting.value = false;
  }
}

async function submitUpdateDoiTac() {
  if (!validateEditForm()) {
    return;
  }

  isEditSubmitting.value = true;
  successMessage.value = "";

  try {
    await updateDoiTac(
        selectedId.value,
        buildPayload(editForm)
    );

    closeEditModal();

    await fetchDoiTac();

    successMessage.value =
        "Đã cập nhật thông tin đối tác.";
  } catch (error) {
    handleEditError(error);
  } finally {
    isEditSubmitting.value = false;
  }
}

async function confirmChangeStatus(
    doiTac,
    trangThaiMoi
) {
  const tenTrangThaiMoi =
      hienThiTrangThai(trangThaiMoi);

  const ok = window.confirm(
      `Bạn có chắc muốn chuyển đối tác "${doiTac.tenDoiTac}" sang trạng thái "${tenTrangThaiMoi}" không?`
  );

  if (!ok) {
    return;
  }

  rowLoading[doiTac.maDoiTac] = true;
  successMessage.value = "";

  try {
    const updated =
        await updateTrangThaiDoiTac(
            doiTac.maDoiTac,
            trangThaiMoi
        );

    replaceLocalItem(updated);

    successMessage.value =
        `Đã chuyển đối tác sang trạng thái "${tenTrangThaiMoi}".`;
  } catch (error) {
    alert(
        getErrorMessage(
            error,
            "Không thể đổi trạng thái đối tác."
        )
    );
  } finally {
    rowLoading[doiTac.maDoiTac] = false;
  }
}

async function confirmDelete(doiTac) {
  if (!coTheXoaDoiTac(doiTac)) {
    alert(
        "Không thể xóa vì sản phẩm của đối tác đã phát sinh đơn hàng."
    );

    return;
  }

  const ok = window.confirm(
      `Bạn có chắc muốn xóa đối tác "${doiTac.tenDoiTac}" không?`
  );

  if (!ok) {
    return;
  }

  rowLoading[doiTac.maDoiTac] = true;
  successMessage.value = "";

  try {
    await deleteDoiTac(
        doiTac.maDoiTac
    );

    danhSachDoiTac.value =
        danhSachDoiTac.value.filter(
            (item) =>
                item.maDoiTac !==
                doiTac.maDoiTac
        );

    successMessage.value =
        "Đã xóa đối tác khỏi danh sách.";
  } catch (error) {
    alert(
        getErrorMessage(
            error,
            "Không thể xóa đối tác."
        )
    );
  } finally {
    rowLoading[doiTac.maDoiTac] = false;
  }
}

function replaceLocalItem(updated) {
  if (!updated) {
    fetchDoiTac();
    return;
  }

  const index =
      danhSachDoiTac.value.findIndex(
          (item) =>
              item.maDoiTac ===
              updated.maDoiTac
      );

  if (index !== -1) {
    danhSachDoiTac.value[index] = {
      ...danhSachDoiTac.value[index],
      ...updated,
    };
  }
}

function coTheXoaDoiTac(doiTac) {
  const value = doiTac?.coTheXoa;

  return (
      value === true ||
      value === 1 ||
      value === "1" ||
      value === "true"
  );
}

function handleCreateError(error) {
  const data = error.response?.data;

  if (data && typeof data === "object") {
    if (data.message) {
      alert(data.message);
      return;
    }

    Object.keys(data).forEach((key) => {
      errors[key] = data[key];
    });

    if (Object.keys(errors).length === 0) {
      alert(
          "Hệ thống xử lý gặp sự cố, vui lòng thử lại."
      );
    }

    return;
  }

  if (
      typeof data === "string" &&
      data.trim() !== ""
  ) {
    alert(data);
    return;
  }

  alert(
      "Lỗi kết nối máy chủ không ổn định. Vui lòng kiểm tra lại."
  );
}

function handleEditError(error) {
  const data = error.response?.data;

  if (data && typeof data === "object") {
    if (data.message) {
      alert(data.message);
      return;
    }

    Object.keys(data).forEach((key) => {
      editErrors[key] = data[key];
    });

    if (
        Object.keys(editErrors).length === 0
    ) {
      alert(
          "Không thể cập nhật đối tác."
      );
    }

    return;
  }

  if (
      typeof data === "string" &&
      data.trim() !== ""
  ) {
    alert(data);
    return;
  }

  alert(
      "Không thể cập nhật đối tác."
  );
}

function getErrorMessage(
    error,
    fallback
) {
  const data = error.response?.data;

  if (
      typeof data === "string" &&
      data.trim()
  ) {
    return data;
  }

  if (data?.message) {
    return data.message;
  }

  if (data?.error) {
    return data.error;
  }

  if (error.message) {
    return error.message;
  }

  return fallback;
}

function getStatusClass(trangThai) {
  switch (Number(trangThai)) {
    case 1:
      return "status-active";

    case 0:
      return "status-inactive";

    case 2:
      return "status-pending";

    case 3:
      return "status-deleted";

    default:
      return "status-unknown";
  }
}

function hienThiTrangThai(trangThai) {
  switch (Number(trangThai)) {
    case 1:
      return "Đang hợp tác";

    case 0:
      return "Ngưng hợp tác";

    case 2:
      return "Chờ xác nhận";

    case 3:
      return "Đã xóa";

    default:
      return "Chưa xác định";
  }
}

function formatDate(value) {
  if (!value) {
    return "Chưa có";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(
      "vi-VN",
      {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }
  ).format(date);
}

function getInitials(name) {
  if (!name) {
    return "DT";
  }

  const words = name
      .trim()
      .split(/\s+/)
      .filter(Boolean);

  if (words.length === 1) {
    return words[0]
        .slice(0, 2)
        .toUpperCase();
  }

  return (
      words[0][0] +
      words[words.length - 1][0]
  ).toUpperCase();
}
</script>

<style scoped>
.partner-page {
  min-height: 100%;
  padding: 24px;
  background:
      radial-gradient(
          circle at top right,
          rgba(37, 99, 235, 0.06),
          transparent 30%
      ),
      #f6f8fb;
}

/* HEADER */

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  color: #172033;
  font-size: 1.75rem;
  font-weight: 750;
  letter-spacing: -0.02em;
}

.page-subtitle {
  margin: 6px 0 0;
  color: #748094;
  font-size: 0.92rem;
}

.btn-add-partner {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  padding: 9px 16px;
  border: 1px solid #2563eb;
  border-radius: 9px;
  background: #2563eb;
  color: #fff;
  font-size: 0.92rem;
  font-weight: 650;
  box-shadow: 0 7px 18px rgba(37, 99, 235, 0.18);
  transition:
      transform 0.18s ease,
      box-shadow 0.18s ease,
      background 0.18s ease;
}

.btn-add-partner:hover {
  border-color: #1d4ed8;
  background: #1d4ed8;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.23);
}

/* ALERT */

.partner-alert {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 13px 16px;
  border: 1px solid #b7e4c7;
  border-radius: 10px;
  background: #ecfdf3;
  color: #166534;
  box-shadow: 0 5px 15px rgba(22, 101, 52, 0.06);
}

.partner-alert-content {
  display: flex;
  align-items: center;
  gap: 9px;
}

.partner-alert-content > div {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.partner-alert-close {
  padding: 3px;
  border: 0;
  background: transparent;
  color: #166534;
  font-size: 18px;
}

/* FILTER */

.filter-card {
  margin-bottom: 18px;
  border: 1px solid #e8edf5;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 6px 20px rgba(24, 39, 75, 0.05);
}

.filter-card-body {
  padding: 18px;
}

.filter-label {
  display: block;
  margin-bottom: 7px;
  color: #596579;
  font-size: 0.78rem;
  font-weight: 700;
}

.search-wrapper {
  position: relative;
}

.search-icon {
  position: absolute;
  top: 50%;
  left: 13px;
  z-index: 2;
  color: #98a2b3;
  transform: translateY(-50%);
}

.search-input {
  padding-left: 39px !important;
}

.form-control,
.form-select {
  min-height: 42px;
  border: 1px solid #dfe5ee;
  border-radius: 9px;
  background: #fff;
  color: #253047;
  font-size: 0.9rem;
}

.form-control::placeholder {
  color: #a0a8b7;
}

.form-control:focus,
.form-select:focus {
  border-color: #6f9df8;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.btn-reset-filter,
.btn-refresh {
  min-height: 42px;
  border-radius: 9px;
  font-weight: 600;
}

.btn-reset-filter {
  border-color: #d9e0ea;
  background: #fff;
  color: #5e6a7e;
}

.btn-reset-filter:hover {
  border-color: #cbd4e1;
  background: #f8fafc;
  color: #344054;
}

.btn-refresh {
  min-width: 44px;
  border-color: #cbdafa;
  background: #eff6ff;
  color: #2563eb;
}

.btn-refresh:hover:not(:disabled) {
  border-color: #2563eb;
  background: #2563eb;
  color: #fff;
}

/* TABLE */

.partner-table-card {
  overflow: hidden;
  border: 1px solid #e7ecf3;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 8px 26px rgba(24, 39, 75, 0.06);
}

.partner-table {
  width: 100%;
  min-width: 1180px;
  margin: 0;
  border-collapse: separate;
  border-spacing: 0;
}

.partner-table thead th {
  padding: 14px 13px;
  border-bottom: 1px solid #e9edf3;
  background: #f8fafc;
  color: #667085;
  font-size: 0.74rem;
  font-weight: 750;
  letter-spacing: 0.045em;
  text-transform: uppercase;
  white-space: nowrap;
}

.partner-table tbody td {
  padding: 15px 13px;
  border-bottom: 1px solid #eff2f6;
  color: #354052;
  vertical-align: middle;
}

.partner-table tbody tr {
  transition: background-color 0.16s ease;
}

.partner-table tbody tr:hover {
  background: #fbfcfe;
}

.partner-table tbody tr:last-child td {
  border-bottom: 0;
}

/*
  FIX CỘT LIÊN HỆ:
  Nội dung có icon rộng 15px và khoảng cách 7px.
  Tiêu đề được đẩy sang phải để thẳng hàng với phần chữ.
*/
.partner-table thead th.contact-header {
  min-width: 225px;
  padding-left: 35px;
}

.partner-table tbody td.contact-cell {
  min-width: 225px;
}

.partner-code {
  color: #7a8699;
  font-size: 0.86rem;
  font-weight: 700;
}

/* PARTNER */

.partner-info {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 220px;
}

.partner-detail {
  min-width: 0;
}

.partner-avatar {
  display: inline-flex;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: 1px solid #d7e3ff;
  border-radius: 11px;
  background: linear-gradient(145deg, #eff6ff, #dbeafe);
  color: #1d4ed8;
  font-size: 0.95rem;
  font-weight: 800;
  text-transform: uppercase;
}

.partner-name {
  margin-bottom: 3px;
  color: #202b3c;
  font-size: 0.93rem;
  font-weight: 700;
}

.partner-company,
.partner-tax {
  max-width: 230px;
  overflow: hidden;
  color: #7b8798;
  font-size: 0.78rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* CONTACT */

.contact-line {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
  margin-bottom: 4px;
  color: #596579;
  font-size: 0.82rem;
  line-height: 1.4;
}

.contact-line:last-child {
  margin-bottom: 0;
}

.contact-line i {
  display: inline-flex;
  flex: 0 0 15px;
  align-items: center;
  justify-content: center;
  width: 15px;
  min-width: 15px;
  color: #98a2b3;
  text-align: center;
}

.contact-line span {
  min-width: 0;
}

.address-cell {
  max-width: 230px;
}

.address-cell span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ACCOUNT */

.account-name {
  display: inline-block;
  padding: 4px 8px;
  border: 1px solid #e1dcf8;
  border-radius: 6px;
  background: #f7f5ff;
  color: #6941c6;
  font-family: monospace;
  font-size: 0.78rem;
}

.date-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #748094;
  font-size: 0.82rem;
  white-space: nowrap;
}

/* STATUS */

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 0.76rem;
  font-weight: 700;
  white-space: nowrap;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-active {
  border-color: #b7e4c7;
  background: #ecfdf3;
  color: #177245;
}

.status-active .status-dot {
  background: #22c55e;
}

.status-inactive {
  border-color: #f4c7c7;
  background: #fff1f2;
  color: #b42318;
}

.status-inactive .status-dot {
  background: #ef4444;
}

.status-pending {
  border-color: #f5d997;
  background: #fffaeb;
  color: #b54708;
}

.status-pending .status-dot {
  background: #f59e0b;
}

.status-deleted,
.status-unknown {
  border-color: #d9dee7;
  background: #f2f4f7;
  color: #667085;
}

.status-deleted .status-dot,
.status-unknown .status-dot {
  background: #98a2b3;
}

/* ACTION */

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
  min-width: 285px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  min-height: 32px;
  padding: 5px 9px;
  border-radius: 7px;
  font-size: 0.76rem;
  font-weight: 650;
  white-space: nowrap;
  transition:
      transform 0.15s ease,
      box-shadow 0.15s ease,
      background 0.15s ease;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-edit {
  border: 1px solid #c7d7fe;
  background: #eff6ff;
  color: #1d4ed8;
}

.btn-edit:hover:not(:disabled) {
  border-color: #2563eb;
  background: #2563eb;
  color: #fff;
}

.btn-cooperate {
  border: 1px solid #a7dfbd;
  background: #ecfdf3;
  color: #16794a;
}

.btn-cooperate:hover:not(:disabled) {
  border-color: #16a34a;
  background: #16a34a;
  color: #fff;
}

.btn-stop {
  border: 1px solid #f2d19f;
  background: #fff8e6;
  color: #b35c00;
}

.btn-stop:hover:not(:disabled) {
  border-color: #d97706;
  background: #d97706;
  color: #fff;
}

.btn-delete {
  border: 1px solid #f2b9bd;
  background: #fff1f2;
  color: #c8323e;
}

.btn-delete:hover:not(:disabled) {
  border-color: #dc3545;
  background: #dc3545;
  color: #fff;
}

.btn-cannot-delete {
  border: 1px solid #d8dee8;
  background: #f2f4f7;
  color: #8791a2;
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.64;
}

/* EMPTY */

.empty-state {
  padding: 58px 20px !important;
  color: #7c8799 !important;
  text-align: center;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  margin: 0 auto 11px;
  border-radius: 14px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 1.55rem;
}

.loading-row {
  padding: 50px 20px !important;
  color: #748094 !important;
}

/* PAGINATION */

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 15px 18px;
  border-top: 1px solid #e9edf3;
  background: #ffffff;
}

.pag-info {
  color: #748094;
  font-size: 0.84rem;
}

/* MODAL */

.partner-modal {
  z-index: 1080;
  background: rgba(15, 23, 42, 0.52);
  backdrop-filter: blur(3px);
}

.partner-modal-backdrop {
  z-index: 1070;
  opacity: 0.48;
}

.partner-modal .modal-dialog {
  width: calc(100% - 28px);
  max-width: 760px;
  margin: 24px auto;
}

.partner-modal .modal-content {
  overflow: hidden;
  border: 0;
  border-radius: 15px;
  background: #ffffff;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.24);
}

.partner-modal .modal-header {
  padding: 18px 22px;
  border-bottom: 1px solid #edf0f5;
  background: linear-gradient(
      135deg,
      #ffffff,
      #f8fafc
  );
}

.partner-modal .modal-title {
  margin: 0;
  color: #1f2937;
  font-size: 1.05rem;
  font-weight: 700;
}

.partner-modal .modal-subtitle {
  margin-top: 4px;
  color: #8490a2;
  font-size: 0.8rem;
}

.partner-modal .modal-body {
  max-height: 68vh;
  overflow-y: auto;
  padding: 22px;
}

.partner-modal .modal-footer {
  padding: 15px 22px;
  border-top: 1px solid #edf0f5;
  background: #fafbfc;
}

.partner-modal .form-label {
  margin-bottom: 6px;
  color: #596579;
  font-size: 0.79rem;
  font-weight: 700;
}

.partner-modal .form-control {
  min-height: 42px;
  border: 1px solid #dfe5ee;
  border-radius: 9px;
}

.partner-modal .form-control:focus {
  border-color: #6f9df8;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.partner-modal .form-text {
  padding: 10px 12px;
  border: 1px solid #dce8ff;
  border-radius: 8px;
  background: #f5f9ff;
  color: #58708f !important;
}

.partner-modal .modal-cancel-btn,
.partner-modal .modal-submit-btn {
  min-height: 40px;
  padding-right: 18px;
  padding-left: 18px;
  border-radius: 8px;
  font-weight: 600;
}

.partner-modal .modal-cancel-btn {
  border: 1px solid #e0e5ec;
  background: #ffffff;
  color: #596579;
}

.partner-modal .modal-submit-btn {
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.17);
}

.partner-modal .modal-submit-btn:hover {
  border-color: #1d4ed8;
  background: #1d4ed8;
  color: #ffffff;
}

/* RESPONSIVE */

@media (max-width: 991px) {
  .partner-page {
    padding: 18px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .btn-add-partner {
    width: 100%;
  }

  .action-buttons {
    min-width: 240px;
  }
}

@media (max-width: 767px) {
  .partner-page {
    padding: 14px;
  }

  .page-title {
    font-size: 1.42rem;
  }

  .filter-card-body {
    padding: 15px;
  }

  .pagination-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .partner-modal .modal-dialog {
    width: calc(100% - 18px);
    margin: 9px auto;
  }

  .partner-modal .modal-body {
    max-height: 72vh;
    padding: 17px;
  }

  .partner-modal .modal-header,
  .partner-modal .modal-footer {
    padding-right: 17px;
    padding-left: 17px;
  }

  .partner-modal .modal-footer {
    gap: 8px;
  }

  .partner-modal .modal-footer .btn {
    flex: 1;
  }
}

@media (max-width: 575px) {
  .partner-info {
    min-width: 190px;
  }

  .partner-table thead th.contact-header,
  .partner-table tbody td.contact-cell {
    min-width: 210px;
  }

  .action-buttons {
    min-width: 210px;
  }

  .action-btn {
    padding-right: 8px;
    padding-left: 8px;
    font-size: 0.72rem;
  }
}
</style>