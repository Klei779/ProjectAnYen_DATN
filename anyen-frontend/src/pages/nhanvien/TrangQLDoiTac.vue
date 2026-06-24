<template>
  <div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
      <div>
        <h3 class="fw-bold text-dark mb-1">Quản Lý Đối Tác</h3>
        <div class="text-muted small">
          Hiển thị danh sách, sửa, xóa và đổi trạng thái đối tác.
        </div>
      </div>

      <button class="btn btn-primary shadow-sm" @click="openCreateModal">
        <i class="bi bi-plus-circle me-1"></i>
        Thêm Đối Tác Mới
      </button>
    </div>

    <div
        v-if="successMessage"
        class="alert alert-success alert-dismissible fade show shadow-sm"
        role="alert"
    >
      <strong>Thành công!</strong> {{ successMessage }}
      <button type="button" class="btn-close" @click="successMessage = ''"></button>
    </div>

    <div class="card border-0 shadow-sm rounded-3 mb-3">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-6">
            <label class="form-label small fw-bold text-secondary">Tìm kiếm</label>
            <input
                v-model="keyword"
                type="text"
                class="form-control"
                placeholder="Tìm theo tên, doanh nghiệp, email, số điện thoại..."
                @input="currentPage = 1"
            />
          </div>

          <div class="col-md-3">
            <label class="form-label small fw-bold text-secondary">Trạng thái</label>
            <select v-model="statusFilter" class="form-select" @change="currentPage = 1">
              <option value="Tất cả">Tất cả</option>
              <option value="Tạm ngưng">Tạm ngưng</option>
              <option value="Đang hợp tác">Đang hợp tác</option>
              <option value="Hết hợp tác">Hết hợp tác</option>
            </select>
          </div>

          <div class="col-md-3 d-flex gap-2">
            <button class="btn btn-outline-secondary w-100" @click="resetFilter">
              Xóa lọc
            </button>

            <button class="btn btn-outline-primary" @click="fetchDoiTac" :disabled="isLoading">
              <span v-if="isLoading" class="spinner-border spinner-border-sm"></span>
              <span v-else>↻</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary">
          <tr>
            <th class="ps-4">Mã</th>
            <th>Đối tác</th>
            <th>Liên hệ</th>
            <th>Thanh toán</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th class="text-end pe-4">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="isLoading">
            <td colspan="7" class="text-center py-5 text-muted">
              <span class="spinner-border spinner-border-sm me-2"></span>
              Đang tải danh sách đối tác...
            </td>
          </tr>

          <tr v-else-if="pagedList.length === 0">
            <td colspan="7" class="text-center py-5 text-muted">
              <div style="font-size: 2.4rem">🏢</div>
              Không tìm thấy dữ liệu đối tác.
            </td>
          </tr>

          <tr v-for="dt in pagedList" :key="dt.maDoiTac">
            <td class="ps-4 fw-semibold text-muted">
              #{{ dt.maDoiTac }}
            </td>

            <td>
              <div class="fw-bold text-dark">{{ dt.tenDoiTac }}</div>
              <div class="small text-muted">
                {{ dt.tenDoanhNghiep || "Chưa có tên doanh nghiệp" }}
              </div>
              <div class="small text-muted">
                MST: {{ dt.maSoThue || "Chưa có" }}
              </div>
            </td>

            <td>
              <div class="small text-dark">{{ dt.email || "Chưa có email" }}</div>
              <div class="small text-muted">{{ dt.soDienThoai || "Chưa có SĐT" }}</div>
              <div class="small text-muted text-truncate address-cell">
                {{ dt.diaChi || "Chưa có địa chỉ" }}
              </div>
            </td>

            <td>
              <div class="small text-dark">
                {{ dt.nganHang || "Chưa có ngân hàng" }}
              </div>
              <div class="small text-muted">
                STK: {{ dt.soTaiKhoan || "Chưa có" }}
              </div>
              <div class="small text-muted">
                TK: {{ dt.tenDangNhap || "Chưa có" }}
              </div>
            </td>

            <td>
              <span :class="['badge rounded-pill px-3 py-2', getStatusClass(dt.trangThai)]">
                {{ hienThiTrangThai(dt.trangThai) }}
              </span>
            </td>

            <td class="small text-muted">
              {{ formatDate(dt.createdAt) }}
            </td>

            <td class="text-end pe-4">
              <div class="btn-group">
                <button class="btn btn-sm btn-outline-primary" @click="openEditModal(dt)">
                  Sửa
                </button>

                <button
                    class="btn btn-sm btn-outline-success"
                    :disabled="isDangHopTac(dt.trangThai) || rowLoading[dt.maDoiTac]"
                    @click="confirmChangeStatus(dt, 'Đang hợp tác')"
                >
                  Hợp tác
                </button>

                <button
                    class="btn btn-sm btn-outline-warning"
                    :disabled="dt.trangThai === 'Hết hợp tác' || rowLoading[dt.maDoiTac]"
                    @click="confirmChangeStatus(dt, 'Hết hợp tác')"
                >
                  Hết hợp tác
                </button>

                <button
                    class="btn btn-sm btn-outline-danger"
                    :disabled="rowLoading[dt.maDoiTac]"
                    @click="confirmDelete(dt)"
                >
                  Xóa
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
        <span class="pag-info">
          Hiển thị {{ showingFrom }} - {{ showingTo }} của {{ filteredList.length }} đối tác
        </span>

        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredList.length"
            layout="prev, pager, next"
        />
      </div>
    </div>

    <!-- MODAL THÊM ĐỐI TÁC: GIỮ LẠI CHO NGƯỜI KHÁC LÀM -->
    <div v-if="showCreateModal" class="modal-backdrop fade show"></div>

    <div
        class="modal fade"
        :class="{ 'show d-block': showCreateModal }"
        tabindex="-1"
        role="dialog"
        aria-hidden="true"
        style="background-color: rgba(0,0,0,0.4);"
    >
      <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
          <div class="modal-header bg-light border-bottom-0 py-3 px-4">
            <h5 class="modal-title fw-bold text-dark">Thêm Đối Tác Mới</h5>
            <button type="button" class="btn-close" aria-label="Close" @click="closeCreateModal"></button>
          </div>

          <div class="modal-body p-4">
            <form @submit.prevent="submitCreateDoiTac">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">
                    Tên đối tác <span class="text-danger">*</span>
                  </label>
                  <input
                      v-model="form.tenDoiTac"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.tenDoiTac}"
                      placeholder="VD: Công ty An Phúc"
                  />
                  <div class="invalid-feedback">{{ errors.tenDoiTac }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Tên doanh nghiệp</label>
                  <input
                      v-model="form.tenDoanhNghiep"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.tenDoanhNghiep}"
                      placeholder="Tên pháp lý nếu có"
                  />
                  <div class="invalid-feedback">{{ errors.tenDoanhNghiep }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Mã số thuế</label>
                  <input
                      v-model="form.maSoThue"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.maSoThue}"
                      placeholder="Mã số thuế doanh nghiệp"
                  />
                  <div class="invalid-feedback">{{ errors.maSoThue }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">
                    Số điện thoại <span class="text-danger">*</span>
                  </label>
                  <input
                      v-model="form.soDienThoai"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.soDienThoai}"
                      placeholder="SĐT liên hệ"
                  />
                  <div class="invalid-feedback">{{ errors.soDienThoai }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">
                    Email <span class="text-danger">*</span>
                  </label>
                  <input
                      v-model="form.email"
                      type="email"
                      class="form-control"
                      :class="{'is-invalid': errors.email}"
                      placeholder="example@doitac.com"
                  />
                  <div class="invalid-feedback">{{ errors.email }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Địa chỉ</label>
                  <input
                      v-model="form.diaChi"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.diaChi}"
                      placeholder="Địa chỉ trụ sở"
                  />
                  <div class="invalid-feedback">{{ errors.diaChi }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Số tài khoản</label>
                  <input
                      v-model="form.soTaiKhoan"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.soTaiKhoan}"
                      placeholder="Số tài khoản thanh toán"
                  />
                  <div class="invalid-feedback">{{ errors.soTaiKhoan }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Ngân hàng</label>
                  <input
                      v-model="form.nganHang"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': errors.nganHang}"
                      placeholder="Tên ngân hàng"
                  />
                  <div class="invalid-feedback">{{ errors.nganHang }}</div>
                </div>
              </div>
            </form>
          </div>

          <div class="modal-footer bg-light border-top-0 py-3 px-4">
            <button type="button" class="btn btn-outline-secondary px-4 fw-bold" @click="closeCreateModal">
              Hủy
            </button>

            <button
                type="button"
                class="btn btn-primary px-4 fw-bold shadow-sm"
                :disabled="isSubmitting"
                @click="submitCreateDoiTac"
            >
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
              Gửi lời mời hợp tác
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL SỬA ĐỐI TÁC -->
    <div v-if="showEditModal" class="modal-backdrop fade show"></div>

    <div
        class="modal fade"
        :class="{ 'show d-block': showEditModal }"
        tabindex="-1"
        role="dialog"
        aria-hidden="true"
        style="background-color: rgba(0,0,0,0.4);"
    >
      <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
          <div class="modal-header bg-light border-bottom-0 py-3 px-4">
            <h5 class="modal-title fw-bold text-dark">Sửa thông tin đối tác</h5>
            <button type="button" class="btn-close" aria-label="Close" @click="closeEditModal"></button>
          </div>

          <div class="modal-body p-4">
            <form @submit.prevent="submitUpdateDoiTac">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">
                    Tên đối tác <span class="text-danger">*</span>
                  </label>
                  <input
                      v-model="editForm.tenDoiTac"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.tenDoiTac}"
                  />
                  <div class="invalid-feedback">{{ editErrors.tenDoiTac }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Tên doanh nghiệp</label>
                  <input
                      v-model="editForm.tenDoanhNghiep"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.tenDoanhNghiep}"
                  />
                  <div class="invalid-feedback">{{ editErrors.tenDoanhNghiep }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Mã số thuế</label>
                  <input
                      v-model="editForm.maSoThue"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.maSoThue}"
                  />
                  <div class="invalid-feedback">{{ editErrors.maSoThue }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">
                    Số điện thoại <span class="text-danger">*</span>
                  </label>
                  <input
                      v-model="editForm.soDienThoai"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.soDienThoai}"
                  />
                  <div class="invalid-feedback">{{ editErrors.soDienThoai }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">
                    Email <span class="text-danger">*</span>
                  </label>
                  <input
                      v-model="editForm.email"
                      type="email"
                      class="form-control"
                      :class="{'is-invalid': editErrors.email}"
                  />
                  <div class="invalid-feedback">{{ editErrors.email }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Địa chỉ</label>
                  <input
                      v-model="editForm.diaChi"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.diaChi}"
                  />
                  <div class="invalid-feedback">{{ editErrors.diaChi }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Số tài khoản</label>
                  <input
                      v-model="editForm.soTaiKhoan"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.soTaiKhoan}"
                  />
                  <div class="invalid-feedback">{{ editErrors.soTaiKhoan }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Ngân hàng</label>
                  <input
                      v-model="editForm.nganHang"
                      type="text"
                      class="form-control"
                      :class="{'is-invalid': editErrors.nganHang}"
                  />
                  <div class="invalid-feedback">{{ editErrors.nganHang }}</div>
                </div>
              </div>
            </form>
          </div>

          <div class="modal-footer bg-light border-top-0 py-3 px-4">
            <button type="button" class="btn btn-outline-secondary px-4 fw-bold" @click="closeEditModal">
              Hủy
            </button>

            <button
                type="button"
                class="btn btn-primary px-4 fw-bold shadow-sm"
                :disabled="isEditSubmitting"
                @click="submitUpdateDoiTac"
            >
              <span v-if="isEditSubmitting" class="spinner-border spinner-border-sm me-2"></span>
              Lưu thay đổi
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
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
const statusFilter = ref("Tất cả");
const currentPage = ref(1);
const pageSize = ref(10);

const form = reactive({
  tenDoiTac: "",
  tenDoanhNghiep: "",
  maSoThue: "",
  soTaiKhoan: "",
  nganHang: "",
  email: "",
  soDienThoai: "",
  diaChi: "",
});

const editForm = reactive({
  tenDoiTac: "",
  tenDoanhNghiep: "",
  maSoThue: "",
  soTaiKhoan: "",
  nganHang: "",
  email: "",
  soDienThoai: "",
  diaChi: "",
});

const errors = reactive({});
const editErrors = reactive({});

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase();

  return danhSachDoiTac.value.filter((dt) => {
    const matchStatus =
        statusFilter.value === "Tất cả" || dt.trangThai === statusFilter.value;

    const searchText = [
      dt.maDoiTac,
      dt.tenDoiTac,
      dt.tenDoanhNghiep,
      dt.maSoThue,
      dt.email,
      dt.soDienThoai,
      dt.diaChi,
      dt.nganHang,
      dt.soTaiKhoan,
      dt.tenDangNhap,
      dt.trangThai,
    ]
        .join(" ")
        .toLowerCase();

    return matchStatus && (!kw || searchText.includes(kw));
  });
});

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

const showingFrom = computed(() => {
  if (filteredList.value.length === 0) return 0;
  return (currentPage.value - 1) * pageSize.value + 1;
});

const showingTo = computed(() =>
    Math.min(currentPage.value * pageSize.value, filteredList.value.length)
);

onMounted(() => {
  fetchDoiTac();
});

async function fetchDoiTac() {
  isLoading.value = true;

  try {
    danhSachDoiTac.value = await getAllDoiTac();
    currentPage.value = 1;
  } catch (error) {
    alert(getErrorMessage(error, "Không thể tải danh sách đối tác."));
  } finally {
    isLoading.value = false;
  }
}

function resetFilter() {
  keyword.value = "";
  statusFilter.value = "Tất cả";
  currentPage.value = 1;
}

function clearErrors() {
  Object.keys(errors).forEach((key) => delete errors[key]);
}

function clearEditErrors() {
  Object.keys(editErrors).forEach((key) => delete editErrors[key]);
}

function resetForm() {
  Object.keys(form).forEach((key) => form[key] = "");
  clearErrors();
}

function resetEditForm() {
  Object.keys(editForm).forEach((key) => editForm[key] = "");
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
}

function openEditModal(doiTac) {
  resetEditForm();

  selectedId.value = doiTac.maDoiTac;

  editForm.tenDoiTac = doiTac.tenDoiTac || "";
  editForm.tenDoanhNghiep = doiTac.tenDoanhNghiep || "";
  editForm.maSoThue = doiTac.maSoThue || "";
  editForm.soTaiKhoan = doiTac.soTaiKhoan || "";
  editForm.nganHang = doiTac.nganHang || "";
  editForm.email = doiTac.email || "";
  editForm.soDienThoai = doiTac.soDienThoai || "";
  editForm.diaChi = doiTac.diaChi || "";

  successMessage.value = "";
  showEditModal.value = true;
}

function closeEditModal() {
  showEditModal.value = false;
  resetEditForm();
}

function validateCreateForm() {
  clearErrors();

  let isValid = true;

  if (!form.tenDoiTac.trim()) {
    errors.tenDoiTac = "Tên đối tác không được để trống";
    isValid = false;
  }

  if (!form.email.trim()) {
    errors.email = "Địa chỉ Email không được để trống";
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = "Định dạng Email không chính xác";
    isValid = false;
  }

  if (!form.soDienThoai.trim()) {
    errors.soDienThoai = "Số điện thoại không được trống";
    isValid = false;
  } else if (!/^0[35789][0-9]{8}$/.test(form.soDienThoai.trim())) {
    errors.soDienThoai = "Số điện thoại không đúng định dạng nhà mạng Việt Nam";
    isValid = false;
  }

  return isValid;
}

function validateEditForm() {
  clearEditErrors();

  let isValid = true;

  if (!editForm.tenDoiTac.trim()) {
    editErrors.tenDoiTac = "Tên đối tác không được để trống";
    isValid = false;
  }

  if (!editForm.email.trim()) {
    editErrors.email = "Email không được để trống";
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email.trim())) {
    editErrors.email = "Email không đúng định dạng";
    isValid = false;
  }

  if (!editForm.soDienThoai.trim()) {
    editErrors.soDienThoai = "Số điện thoại không được để trống";
    isValid = false;
  } else if (!/^0[35789][0-9]{8}$/.test(editForm.soDienThoai.trim())) {
    editErrors.soDienThoai =
        "Số điện thoại phải có 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09";
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
  if (!validateCreateForm()) return;

  isSubmitting.value = true;
  successMessage.value = "";

  try {
    await createDoiTac(buildPayload(form));

    successMessage.value =
        "Hệ thống đã ghi nhận thêm mới đối tác và gửi email thư mời hợp tác thành công!";

    closeCreateModal();
    resetForm();

    await fetchDoiTac();
  } catch (error) {
    handleCreateError(error);
  } finally {
    isSubmitting.value = false;
  }
}

async function submitUpdateDoiTac() {
  if (!validateEditForm()) return;

  isEditSubmitting.value = true;
  successMessage.value = "";

  try {
    await updateDoiTac(selectedId.value, buildPayload(editForm));

    successMessage.value = "Đã cập nhật thông tin đối tác.";

    closeEditModal();

    await fetchDoiTac();
  } catch (error) {
    handleEditError(error);
  } finally {
    isEditSubmitting.value = false;
  }
}

async function confirmChangeStatus(doiTac, trangThaiMoi) {
  const ok = confirm(
      `Bạn có chắc muốn đổi trạng thái đối tác "${doiTac.tenDoiTac}" thành "${trangThaiMoi}" không?`
  );

  if (!ok) return;

  rowLoading[doiTac.maDoiTac] = true;
  successMessage.value = "";

  try {
    const updated = await updateTrangThaiDoiTac(doiTac.maDoiTac, trangThaiMoi);

    replaceLocalItem(updated);

    successMessage.value = `Đã đổi trạng thái đối tác thành "${trangThaiMoi}".`;
  } catch (error) {
    alert(getErrorMessage(error, "Không thể đổi trạng thái đối tác."));
  } finally {
    rowLoading[doiTac.maDoiTac] = false;
  }
}

async function confirmDelete(doiTac) {
  const ok = confirm(`Bạn có chắc muốn xóa đối tác "${doiTac.tenDoiTac}" không?`);

  if (!ok) return;

  rowLoading[doiTac.maDoiTac] = true;
  successMessage.value = "";

  try {
    await deleteDoiTac(doiTac.maDoiTac);

    danhSachDoiTac.value = danhSachDoiTac.value.filter(
        (item) => item.maDoiTac !== doiTac.maDoiTac
    );

    successMessage.value = "Đã xóa đối tác khỏi danh sách.";
  } catch (error) {
    alert(getErrorMessage(error, "Không thể xóa đối tác."));
  } finally {
    rowLoading[doiTac.maDoiTac] = false;
  }
}

function replaceLocalItem(updated) {
  const index = danhSachDoiTac.value.findIndex(
      (item) => item.maDoiTac === updated.maDoiTac
  );

  if (index !== -1) {
    danhSachDoiTac.value[index] = updated;
  }
}

function handleCreateError(error) {
  const data = error.response?.data;

  if (data && typeof data === "object") {
    if (data.message) {
      alert(data.message);
    } else {
      Object.keys(data).forEach((key) => errors[key] = data[key]);

      if (Object.keys(errors).length === 0) {
        alert("Hệ thống xử lý gặp sự cố, vui lòng thử lại.");
      }
    }
  } else if (typeof data === "string" && data.trim() !== "") {
    alert(data);
  } else {
    alert("Lỗi kết nối máy chủ không ổn định. Vui lòng kiểm tra lại.");
  }
}

function handleEditError(error) {
  const data = error.response?.data;

  if (data && typeof data === "object") {
    if (data.message) {
      alert(data.message);
    } else {
      Object.keys(data).forEach((key) => editErrors[key] = data[key]);

      if (Object.keys(editErrors).length === 0) {
        alert("Không thể cập nhật đối tác.");
      }
    }
  } else if (typeof data === "string" && data.trim() !== "") {
    alert(data);
  } else {
    alert("Không thể cập nhật đối tác.");
  }
}

function getErrorMessage(error, fallback) {
  const data = error.response?.data;

  if (typeof data === "string" && data.trim()) return data;
  if (data?.message) return data.message;
  if (data?.error) return data.error;
  if (error.message) return error.message;

  return fallback;
}

function isDangHopTac(trangThai) {
  return trangThai === "Đang hợp tác" || trangThai === "Đã hợp tác";
}

function getStatusClass(trangThai) {
  if (trangThai === "Đang hợp tác" || trangThai === "Đã hợp tác") {
    return "bg-success-subtle text-success border border-success-subtle";
  }

  if (trangThai === "Hết hợp tác") {
    return "bg-danger-subtle text-danger border border-danger-subtle";
  }

  if (trangThai === "Đã xác nhận") {
    return "bg-info-subtle text-info border border-info-subtle";
  }

  if (trangThai === "Chờ xác nhận") {
    return "bg-warning-subtle text-warning border border-warning-subtle";
  }

  return "bg-secondary-subtle text-secondary border border-secondary-subtle";
}

function hienThiTrangThai(trangThai) {
  return trangThai || "Chưa có";
}

function formatDate(value) {
  if (!value) return "Chưa có";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleDateString("vi-VN");
}
</script>

<style scoped>
.form-control,
.form-select {
  border-color: #e2e8f0;
  padding: 0.6rem 0.75rem;
}

.form-control:focus,
.form-select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 0.25rem rgba(37, 99, 235, 0.1);
}

.modal-backdrop {
  opacity: 0.5;
}

.address-cell {
  max-width: 220px;
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border-top: 1px solid #edf2f7;
  background: #fff;
}

.pag-info {
  font-size: 0.9rem;
  color: #64748b;
}

.btn-group .btn {
  white-space: nowrap;
}

@media (max-width: 768px) {
  .pagination-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .btn-group {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 4px;
  }
}
</style>