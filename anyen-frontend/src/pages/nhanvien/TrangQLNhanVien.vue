<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold text-dark mb-1">Quản lý nhân viên</h3>
        <div class="small text-muted">
          Chỉ Quản lý/Admin An Yên mới có quyền thêm, sửa và cho nhân viên nghỉ việc.
        </div>
      </div>

      <button
          v-if="isAdmin"
          class="btn btn-primary d-flex align-items-center gap-2 px-3 shadow-sm"
          @click="openCreateForm"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
             class="bi bi-person-plus" viewBox="0 0 16 16">
          <path
              d="M6 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6m2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0m4 8c0 1-1 1-1 1H1s-1 0-1-1 1-4 6-4 6 3 6 4m-1-.004c-.001-.246-.154-.986-.832-1.664C9.516 10.68 8.289 10 6 10s-3.516.68-4.168 1.332c-.678.678-.83 1.418-.832 1.664z"/>
          <path fill-rule="evenodd"
                d="M13.5 5a.5.5 0 0 1 .5.5V7h1.5a.5.5 0 0 1 0 1H14v1.5a.5.5 0 0 1-1 0V8h-1.5a.5.5 0 0 1 0-1H13V5.5a.5.5 0 0 1 .5-.5"/>
        </svg>
        Thêm nhân viên mới
      </button>
    </div>

    <div v-if="!isAdmin" class="alert alert-danger">
      Tài khoản hiện tại không có quyền thay đổi thông tin nhân viên.
    </div>

    <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-secondary">
          <tr>
            <th class="ps-4">Mã số</th>
            <th>Họ và tên</th>
            <th>Tài khoản</th>
            <th>Liên hệ</th>
            <th>Vai trò</th>
            <th>Trạng thái</th>
            <th class="text-end pe-4">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="nv in pagedList" :key="nv.maNhanVien">
            <td class="ps-4 fw-semibold text-muted">#{{ nv.maNhanVien }}</td>
            <td>
              <div class="fw-medium text-dark">{{ nv.hoTen }}</div>
              <div class="small text-muted">{{ nv.diaChi || "Chưa cập nhật địa chỉ" }}</div>
            </td>
            <td><code class="text-purple">{{ nv.tenDangNhap }}</code></td>
            <td>
              <div class="small text-dark">{{ nv.email }}</div>
              <div class="small text-muted">{{ nv.soDienThoai }}</div>
            </td>
            <td>
              <span class="badge bg-light text-dark border border-secondary-subtle px-2 py-2">
                {{ hienThiVaiTro(nv.vaiTro) }}
              </span>
            </td>
            <td>
              <span
                  :class="[
                    'badge px-2 py-2 rounded-pill',
                    Number(nv.trangThai) === 0
                      ? 'bg-danger-subtle text-danger'
                      : 'bg-success-subtle text-success'
                  ]"
              >
                {{ nv.tenTrangThai }}
              </span>
            </td>
            <td class="text-end pe-4">
              <div v-if="isAdmin" class="d-flex justify-content-end gap-2 flex-wrap">
                <button
                    class="btn btn-sm btn-outline-primary px-3 rounded-2"
                    @click="openEditForm(nv)"
                >
                  Sửa
                </button>

                <button
                    v-if="Number(nv.trangThai) === 1"
                    class="btn btn-sm btn-outline-danger px-3 rounded-2"
                    :disabled="loadingStates[nv.maNhanVien]"
                    @click="confirmNghiViec(nv)"
                >
                  <span
                      v-if="loadingStates[nv.maNhanVien]"
                      class="spinner-border spinner-border-sm me-1"
                  ></span>
                  Cho nghỉ việc
                </button>

                <span v-else class="text-muted small fst-italic align-self-center">
                  Đã nghỉ việc
                </span>
              </div>

              <span v-else class="text-muted small fst-italic">Không có quyền</span>
            </td>
          </tr>

          <tr v-if="danhSachNhanVien.length === 0">
            <td colspan="7" class="text-center py-4 text-muted">
              Không tìm thấy dữ liệu nhân viên trong hệ thống.
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
        <span class="pag-info">
          Hiển thị {{ displayFrom }} - {{ displayTo }}
          của {{ danhSachNhanVien.length }} nhân viên
        </span>

        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="danhSachNhanVien.length"
            layout="prev, pager, next"
        />
      </div>
    </div>

    <div
        v-if="showEmployeeForm"
        class="modal fade show d-block"
        style="background: rgba(0,0,0,0.55);"
        tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-3">
          <div class="modal-header border-bottom-0 pb-0">
            <div>
              <h5 class="modal-title fw-bold text-dark">
                {{ isEditing ? "Sửa thông tin nhân viên" : "Tạo tài khoản nhân viên" }}
              </h5>
              <div v-if="isEditing" class="small text-muted mt-1">
                Mã nhân viên: #{{ editingId }}
              </div>
            </div>
            <button type="button" class="btn-close" @click="closeEmployeeForm"></button>
          </div>

          <div class="modal-body py-3" style="max-height: 68vh; overflow-y: auto;">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Họ và tên</label>
                <input
                    v-model="form.hoTen"
                    type="text"
                    maxlength="50"
                    class="form-control"
                    :class="{ 'is-invalid': errors.hoTen }"
                    placeholder="VD: Nguyễn Văn A"
                />
                <div class="invalid-feedback">{{ errors.hoTen }}</div>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-bold text-secondary">Tên đăng nhập</label>
                <input
                    v-model="form.tenDangNhap"
                    type="text"
                    maxlength="50"
                    autocomplete="off"
                    class="form-control"
                    :class="{ 'is-invalid': errors.tenDangNhap }"
                />
                <div class="invalid-feedback">{{ errors.tenDangNhap }}</div>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-bold text-secondary">
                  {{ isEditing ? "Mật khẩu mới (không bắt buộc)" : "Mật khẩu" }}
                </label>
                <input
                    v-model="form.matKhau"
                    type="password"
                    maxlength="100"
                    autocomplete="new-password"
                    class="form-control"
                    :class="{ 'is-invalid': errors.matKhau }"
                    :placeholder="isEditing ? 'Để trống để giữ mật khẩu cũ' : ''"
                />
                <div class="invalid-feedback">{{ errors.matKhau }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Địa chỉ email</label>
                <input
                    v-model="form.email"
                    type="email"
                    maxlength="100"
                    class="form-control"
                    :class="{ 'is-invalid': errors.email }"
                    placeholder="example@anyen.vn"
                />
                <div class="invalid-feedback">{{ errors.email }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Số điện thoại</label>
                <input
                    v-model="form.soDienThoai"
                    type="text"
                    maxlength="10"
                    class="form-control"
                    :class="{ 'is-invalid': errors.soDienThoai }"
                    placeholder="VD: 0912345678"
                />
                <div class="invalid-feedback">{{ errors.soDienThoai }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Địa chỉ cư trú</label>
                <input
                    v-model="form.diaChi"
                    type="text"
                    maxlength="255"
                    class="form-control"
                    :class="{ 'is-invalid': errors.diaChi }"
                />
                <div class="invalid-feedback">{{ errors.diaChi }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Vai trò</label>
                <select
                    v-model="form.vaiTro"
                    class="form-select"
                    :class="{ 'is-invalid': errors.vaiTro }"
                >
                  <option value="">-- Lựa chọn vai trò hệ thống --</option>
                  <option value="1">Quản lý/Admin An Yên</option>
                  <option value="2">Nhân viên</option>
                  <option value="3">Hotline</option>
                </select>
                <div class="invalid-feedback">{{ errors.vaiTro }}</div>
              </div>
            </div>
          </div>

          <div class="modal-footer border-top-0 pt-0">
            <button type="button" class="btn btn-light px-3" @click="closeEmployeeForm">
              Đóng
            </button>
            <button
                type="button"
                class="btn btn-primary px-4 shadow-sm"
                :disabled="isSubmitting"
                @click="submitEmployeeForm"
            >
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEditing ? "Lưu thay đổi" : "Lưu thông tin" }}
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
  createNhanVien,
  getAllNhanVien,
  nghiViecNhanVien,
  updateNhanVien,
} from "../../services/QuanLyNhanVienService.js";

const showEmployeeForm = ref(false);
const isEditing = ref(false);
const editingId = ref(null);
const isSubmitting = ref(false);
const danhSachNhanVien = ref([]);
const loadingStates = reactive({});

const form = reactive({
  hoTen: "",
  tenDangNhap: "",
  matKhau: "",
  email: "",
  soDienThoai: "",
  diaChi: "",
  vaiTro: "",
});

const errors = reactive({});
const currentPage = ref(1);
const pageSize = ref(10);

function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem("user") || "null");
  } catch (error) {
    console.error("Không đọc được thông tin đăng nhập:", error);
    return null;
  }
}

const currentUser = ref(getCurrentUser());

// VaiTro = 1 được AuthService chuyển thành vaiTroChiTiet = ADMIN.
const isAdmin = computed(() => {
  const role = currentUser.value?.vaiTroChiTiet || currentUser.value?.role;
  return role === "ADMIN" || Number(currentUser.value?.vaiTro) === 1;
});

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSachNhanVien.value.slice(start, start +  pageSize.value);
});

const displayFrom = computed(() => {
  if (danhSachNhanVien.value.length === 0) return 0;
  return (currentPage.value - 1) * pageSize.value +  1;
});

const displayTo = computed(() =>
    Math.min(currentPage.value * pageSize.value, danhSachNhanVien.value.length)
);

async function fetchDanhSachNhanVien() {
  try {
    danhSachNhanVien.value = await getAllNhanVien();

    const maxPage = Math.max(
        1,
        Math.ceil(danhSachNhanVien.value.length / pageSize.value)
    );
    if (currentPage.value > maxPage) currentPage.value = maxPage;
  } catch (error) {
    console.error("Lỗi khi tải danh sách nhân viên:", error);
    alert(error.response?.data?.message || "Không thể tải danh sách nhân viên từ hệ thống.");
  }
}
//role
function hienThiVaiTro(vaiTro) {
  const roles = {
    1: "Quản lý/Admin",
    2: "Nhân viên",
    3: "Hotline",
  };
  return roles[Number(vaiTro)] || "Không xác định";
}

onMounted(() => {
  if (isAdmin.value) {
    fetchDanhSachNhanVien();
  }
});

function clearErrors() {
  Object.keys(errors).forEach((key) => delete errors[key]);
}

function resetForm() {
  Object.assign(form, {
    hoTen: "",
    tenDangNhap: "",
    matKhau: "",
    email: "",
    soDienThoai: "",
    diaChi: "",
    vaiTro: "",
  });
  clearErrors();
}

function openCreateForm() {
  if (!isAdmin.value) return;

  resetForm();
  isEditing.value = false;
  editingId.value = null;
  showEmployeeForm.value = true;
}

function openEditForm(nhanVien) {
  if (!isAdmin.value) return;

  clearErrors();
  isEditing.value = true;
  editingId.value = nhanVien.maNhanVien;

  Object.assign(form, {
    hoTen: nhanVien.hoTen || "",
    tenDangNhap: nhanVien.tenDangNhap || "",
    matKhau: "",
    email: nhanVien.email || "",
    soDienThoai: nhanVien.soDienThoai || "",
    diaChi: nhanVien.diaChi || "",
    vaiTro: String(nhanVien.vaiTro ?? ""),
  });

  showEmployeeForm.value = true;
}

function closeEmployeeForm() {
  showEmployeeForm.value = false;
  isEditing.value = false;
  editingId.value = null;
  resetForm();
}

function validateForm() {
  clearErrors();
  let isValid = true;

  const hoTen = form.hoTen.trim();
  const tenDangNhap = form.tenDangNhap.trim();
  const matKhau = form.matKhau.trim();
  const email = form.email.trim();
  const soDienThoai = form.soDienThoai.trim();
  const diaChi = form.diaChi.trim();

  if (!hoTen) {
    errors.hoTen = "Họ tên không được để trống";
    isValid = false;
  } else if (hoTen.length > 50) {
    errors.hoTen = "Họ tên tối đa 50 ký tự";
    isValid = false;
  }

  if (!tenDangNhap) {
    errors.tenDangNhap = "Tên đăng nhập không được để trống";
    isValid = false;
  } else if (tenDangNhap.length < 4 || tenDangNhap.length > 50) {
    errors.tenDangNhap = "Tên đăng nhập phải từ 4 đến 50 ký tự";
    isValid = false;
  }

  if (!isEditing.value && !matKhau) {
    errors.matKhau = "Mật khẩu không được để trống";
    isValid = false;
  } else if (matKhau && matKhau.length < 6) {
    errors.matKhau = "Mật khẩu phải có ít nhất 6 ký tự";
    isValid = false;
  }

  if (!email) {
    errors.email = "Email không được để trống";
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.email = "Email không đúng định dạng";
    isValid = false;
  }

  if (!soDienThoai) {
    errors.soDienThoai = "Số điện thoại không được để trống";
    isValid = false;
  } else if (!/^0[35789][0-9]{8}$/.test(soDienThoai)) {
    errors.soDienThoai = "Số điện thoại không đúng định dạng Việt Nam";
    isValid = false;
  }

  if (diaChi.length > 255) {
    errors.diaChi = "Địa chỉ tối đa 255 ký tự";
    isValid = false;
  }

  if (!["1", "2", "3"].includes(String(form.vaiTro))) {
    errors.vaiTro = "Vui lòng chọn vai trò hợp lệ";
    isValid = false;
  }

  return isValid;
}

function buildPayload() {
  const payload = {
    hoTen: form.hoTen.trim(),
    tenDangNhap: form.tenDangNhap.trim(),
    email: form.email.trim(),
    soDienThoai: form.soDienThoai.trim(),
    diaChi: form.diaChi.trim(),
    vaiTro: Number(form.vaiTro),
  };

  const matKhauMoi = form.matKhau.trim();
  if (!isEditing.value || matKhauMoi) {
    payload.matKhau = matKhauMoi;
  }

  return payload;
}

function applyServerErrors(error) {
  const data = error.response?.data;

  if (data && typeof data === "object") {
    Object.entries(data).forEach(([key, value]) => {
      if (key !== "message") errors[key] = value;
    });

    if (data.message) alert(data.message);
    return;
  }

  alert("Không thể kết nối tới máy chủ.");
}

async function submitEmployeeForm() {
  if (!isAdmin.value || !validateForm()) return;

  isSubmitting.value = true;
  try {
    const payload = buildPayload();

    if (isEditing.value) {
      await updateNhanVien(editingId.value, payload);
      alert("Cập nhật thông tin nhân viên thành công.");
    } else {
      await createNhanVien(payload);
      alert("Thêm nhân viên thành công.");
    }

    await fetchDanhSachNhanVien();
    closeEmployeeForm();
  } catch (error) {
    applyServerErrors(error);
  } finally {
    isSubmitting.value = false;
  }
}

async function confirmNghiViec(nhanVien) {
  if (!isAdmin.value) return;

  const accepted = confirm(
      `Bạn xác nhận cho nhân viên [${nhanVien.hoTen}] nghỉ việc?`
  );
  if (!accepted) return;

  const id = nhanVien.maNhanVien;
  loadingStates[id] = true;

  try {
    await nghiViecNhanVien(id);
    alert("Đã cập nhật trạng thái nghỉ việc thành công.");
    await fetchDanhSachNhanVien();
  } catch (error) {
    alert(error.response?.data?.message || "Không thể cập nhật trạng thái nhân viên.");
  } finally {
    loadingStates[id] = false;
  }
}
</script>

<style scoped>
.text-purple {
  color: #6f42c1;
}

.form-select,
.form-control {
  border-color: #e2e8f0;
}

.form-select:focus,
.form-control:focus {
  border-color: #94a3b8;
  box-shadow: none;
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  border-top: 1px solid #eef2f7;
  background: #fff;
}

.pag-info {
  color: #64748b;
  font-size: 14px;
}

@media (max-width: 768px) {
  .pagination-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>