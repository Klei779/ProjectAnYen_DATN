<template>
  <div class="account-page">
    <div class="page-header">
      <div>
        <h2>Thông tin tài khoản</h2>
        <p>Quản lý và cập nhật thông tin cá nhân của nhân viên</p>
      </div>

      <button class="btn-back" type="button" @click="closePage">
        <i class="fa-solid fa-arrow-left"></i>
        Quay lại
      </button>
    </div>

    <div v-if="loading" class="account-loading">
      <i class="fa-solid fa-spinner fa-spin"></i>
      Đang tải thông tin tài khoản...
    </div>

    <div v-else class="account-container">
      <aside class="profile-card">
        <div class="avatar-wrap">
          <div class="avatar-main">
            {{ initials }}
          </div>

          <button class="camera-btn" type="button" title="Ảnh đại diện">
            <i class="fa-solid fa-camera"></i>
          </button>
        </div>

        <h3>{{ account?.hoTen || "Nhân viên" }}</h3>

        <span
            class="status-badge"
            :class="{ inactive: account?.tenTrangThai !== 'Đang hoạt động' }"
        >
          {{ account?.tenTrangThai || "Unknow" }}
        </span>

        <div class="profile-summary">
          <div>
            <span>Mã nhân viên</span>
            <strong>{{ maNhanVienDisplay }}</strong>
          </div>

          <div>
            <span>Tên đăng nhập</span>
            <strong>{{ account?.tenDangNhap || "---" }}</strong>
          </div>

          <div>
            <span>Vai trò</span>
            <strong>{{ account?.tenVaiTro || "---" }}</strong>
          </div>
        </div>
      </aside>

      <main class="info-card">
        <div class="card-header">
          <div>
            <h3>Thông tin cá nhân</h3>
            <p>Chỉ cập nhật các thông tin có trong bảng nhân viên</p>
          </div>

          <div class="header-actions">
            <button
                v-if="!editMode"
                class="btn-edit"
                type="button"
                @click="startEdit"
            >
              <i class="fa-solid fa-pen"></i>
              Chỉnh sửa
            </button>

            <button
                class="btn-password"
                type="button"
                @click="showDoiMatKhau = !showDoiMatKhau"
            >
              <i class="fa-solid fa-key"></i>
              {{ showDoiMatKhau ? "Ẩn đổi mật khẩu" : "Đổi mật khẩu" }}
            </button>
          </div>
        </div>

        <form v-if="editMode" class="edit-form" @submit.prevent="submitUpdate">
          <div class="form-row">
            <label>Họ và tên</label>
            <input
                v-model.trim="form.hoTen"
                type="text"
                placeholder="Nhập họ và tên"
            />
          </div>

          <div class="form-row">
            <label>Số điện thoại</label>
            <input
                v-model.trim="form.soDienThoai"
                type="text"
                placeholder="Nhập số điện thoại"
            />
          </div>

          <div class="form-row">
            <label>Email</label>
            <input
                v-model.trim="form.email"
                type="email"
                placeholder="Nhập email"
            />
          </div>

          <div class="form-row">
            <label>Địa chỉ</label>
            <textarea
                v-model.trim="form.diaChi"
                rows="4"
                placeholder="Nhập địa chỉ"
            ></textarea>
          </div>

          <div class="action-row">
            <button class="btn-outline" type="button" @click="cancelEdit">
              Hủy
            </button>

            <button class="btn-save" type="submit" :disabled="saving">
              <i v-if="saving" class="fa-solid fa-spinner fa-spin"></i>
              <i v-else class="fa-solid fa-floppy-disk"></i>
              Lưu thay đổi
            </button>
          </div>
        </form>

        <div v-else class="info-list">
          <div class="info-row">
            <span>Họ và tên</span>
            <strong>{{ account?.hoTen || "Chưa cập nhật" }}</strong>
          </div>

          <div class="info-row">
            <span>Số điện thoại</span>
            <strong>{{ account?.soDienThoai || "Chưa cập nhật" }}</strong>
          </div>

          <div class="info-row">
            <span>Email</span>
            <strong>{{ account?.email || "Chưa cập nhật" }}</strong>
          </div>

          <div class="info-row">
            <span>Địa chỉ</span>
            <strong>{{ account?.diaChi || "Chưa cập nhật" }}</strong>
          </div>
        </div>
        <div v-if="showDoiMatKhau" class="password-section">
          <DoiMatKhau/>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import api from "../../api/api.js";
import DoiMatKhau from "../../components/DoiMatKhau.vue";

const router = useRouter();

const API_URL = "/api/nhan-vien/tai-khoan";

const loading = ref(false);
const saving = ref(false);
const editMode = ref(false);
const showDoiMatKhau = ref(false);

const account = ref(null);

const form = reactive({
  hoTen: "",
  email: "",
  soDienThoai: "",
  diaChi: ""
});

const initials = computed(() => {
  const name = account.value?.hoTen || account.value?.tenDangNhap || "NV";
  const parts = name.trim().split(/\s+/);

  if (parts.length === 1) {
    return parts[0].substring(0, 2).toUpperCase();
  }

  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
});

const maNhanVienDisplay = computed(() => {
  const id = account.value?.maNhanVien;

  if (!id) {
    return "---";
  }

  return `NV${String(id).padStart(2, "0")}`;
});

onMounted(() => {
  loadAccount();
});

async function loadAccount() {
  loading.value = true;

  try {
    const res = await api.get(`${API_URL}/me`);
    account.value = res.data;
    fillForm(res.data);
    syncLocalUser(res.data);
  } catch (error) {
    console.error("Lỗi tải thông tin tài khoản:", error);
    ElMessage.error("Không tải được thông tin tài khoản");
  } finally {
    loading.value = false;
  }
}

function fillForm(data) {
  form.hoTen = data?.hoTen || "";
  form.email = data?.email || "";
  form.soDienThoai = data?.soDienThoai || "";
  form.diaChi = data?.diaChi || "";
}

function startEdit() {
  fillForm(account.value);
  editMode.value = true;
}

function cancelEdit() {
  fillForm(account.value);
  editMode.value = false;
}

async function submitUpdate() {
  if (!form.hoTen.trim()) {
    ElMessage.warning("Vui lòng nhập họ và tên");
    return;
  }

  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    ElMessage.warning("Email không đúng định dạng");
    return;
  }

  if (form.soDienThoai && !/^[0-9]{9,20}$/.test(form.soDienThoai)) {
    ElMessage.warning("Số điện thoại chỉ gồm 9 - 20 chữ số");
    return;
  }

  saving.value = true;

  try {
    const res = await api.put(`${API_URL}/me`, {
      hoTen: form.hoTen,
      email: form.email,
      soDienThoai: form.soDienThoai,
      diaChi: form.diaChi
    });

    account.value = res.data;
    fillForm(res.data);
    syncLocalUser(res.data);

    editMode.value = false;
    ElMessage.success("Cập nhật thông tin tài khoản thành công");
  } catch (error) {
    console.error("Lỗi cập nhật tài khoản:", error);
    ElMessage.error(getErrorMessage(error, "Cập nhật thông tin thất bại"));
  } finally {
    saving.value = false;
  }
}

function syncLocalUser(data) {
  const oldUser = JSON.parse(localStorage.getItem("user") || "{}");

  const newUser = {
    ...oldUser,
    id: data.maNhanVien,
    hoTen: data.hoTen,
    tenDangNhap: data.tenDangNhap,
    email: data.email,
    soDienThoai: data.soDienThoai,
    diaChi: data.diaChi,
    vaiTro: data.vaiTro,
    trangThai: data.trangThai
  };

  localStorage.setItem("user", JSON.stringify(newUser));
  window.dispatchEvent(new Event("session-updated"));
}

function closePage() {
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const role = user.vaiTroChiTiet || user.role || user.vaiTro;

  if (role === "ADMIN") {
    router.push("/admin/tong-quan");
  } else if (role === "HOTLINE") {
    router.push("/hotline/quan-ly-cong-viec");
  } else {
    router.push("/nhan-vien/tong-quan");
  }
}

function getErrorMessage(error, fallback) {
  return error?.response?.data?.message ||
      error?.response?.data?.error ||
      fallback;
}
</script>

<style scoped>
.account-page {
  min-height: calc(100vh - 80px);
  padding: 28px;
  background: #f5f7fb;
  font-family: "Inter", Arial, sans-serif;
}

.page-header {
  max-width: 1100px;
  margin: 0 auto 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.page-header h2 {
  margin: 0;
  color: #111827;
  font-size: 26px;
  font-weight: 800;
}

.page-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.btn-back {
  height: 40px;
  padding: 0 16px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  background: #fff;
  color: #0f274f;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
}

.account-loading {
  max-width: 1100px;
  margin: 40px auto 0;
  padding: 18px 24px;
  background: #fff;
  border-radius: 14px;
  color: #0f172a;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
  display: flex;
  align-items: center;
  gap: 10px;
}

.account-container {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  align-items: start;
}

.profile-card,
.info-card {
  background: #fff;
  border-radius: 18px;
  border: 1px solid #eef2f7;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.07);
}

.profile-card {
  padding: 30px 24px;
  text-align: center;
}

.avatar-wrap {
  width: 140px;
  height: 140px;
  margin: 0 auto 18px;
  position: relative;
}

.avatar-main {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: #fdeff7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #111827;
  font-size: 42px;
  font-weight: 800;
}

.camera-btn {
  position: absolute;
  right: 8px;
  bottom: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #fff;
  color: #0f172a;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.18);
  cursor: default;
}

.profile-card h3 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 21px;
  font-weight: 800;
}

.status-badge {
  display: inline-flex;
  padding: 7px 14px;
  border-radius: 8px;
  background: #dcfce7;
  color: #16803a;
  font-size: 13px;
  font-weight: 800;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #dc2626;
}

.profile-summary {
  margin-top: 26px;
  border-top: 1px solid #eef2f7;
  padding-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}

.profile-summary div {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.profile-summary span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.profile-summary strong {
  color: #0f274f;
  font-size: 15px;
  font-weight: 800;
  word-break: break-word;
}

.info-card {
  padding: 28px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 22px;
  border-bottom: 1px solid #eef2f7;
}

.card-header h3 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 800;
}

.card-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.btn-edit {
  height: 38px;
  padding: 0 15px;
  border: 1px solid #ff5b6b;
  border-radius: 9px;
  background: #fff;
  color: #ff3045;
  font-weight: 800;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-password {
  height: 38px;
  padding: 0 15px;
  border: 1px solid #17934a;
  border-radius: 9px;
  background: #fff;
  color: #17934a;
  font-weight: 800;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-password:hover {
  background: #e8f8ef;
}

.password-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eef2f7;
}

.password-section :deep(.change-password-card) {
  max-width: 100%;
  box-shadow: none;
  border: 1px solid #eef2f7;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: grid;
  grid-template-columns: 170px 1fr;
  gap: 18px;
  min-height: 44px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
}

.info-row span {
  color: #17335c;
  font-size: 14px;
  font-weight: 700;
}

.info-row strong {
  color: #0f274f;
  font-size: 14px;
  font-weight: 800;
  word-break: break-word;
}

.edit-form {
  padding: 20px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.form-row {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.form-row label {
  color: #17335c;
  font-size: 14px;
  font-weight: 800;
}

.form-row input,
.form-row textarea {
  width: 100%;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  padding: 11px 13px;
  color: #0f172a;
  outline: none;
  font-size: 14px;
  background: #fff;
  box-sizing: border-box;
}

.form-row textarea {
  resize: vertical;
}

.form-row input:focus,
.form-row textarea:focus {
  border-color: #ff5b6b;
  box-shadow: 0 0 0 3px rgba(255, 91, 107, 0.12);
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 22px;
  flex-wrap: wrap;
}

.btn-outline,
.btn-save {
  height: 42px;
  padding: 0 20px;
  border-radius: 9px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-outline {
  border: 1px solid #ff5b6b;
  background: #fff;
  color: #ff3045;
}

.btn-save {
  border: 1px solid #ff3045;
  background: #ff3045;
  color: #fff;
}

.btn-save:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .account-container {
    grid-template-columns: 1fr;
  }

  .profile-summary {
    text-align: center;
  }
}

@media (max-width: 768px) {
  .account-page {
    padding: 18px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-back {
    width: 100%;
    justify-content: center;
  }

  .info-card,
  .profile-card {
    padding: 22px 18px;
  }

  .card-header {
    flex-direction: column;
  }

  .btn-edit {
    width: 100%;
    justify-content: center;
  }

  .info-row,
  .form-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .action-row {
    justify-content: stretch;
  }

  .btn-outline,
  .btn-save {
    width: 100%;
    justify-content: center;
  }

  .header-actions {
    width: 100%;
  }

  .btn-password {
    width: 100%;
    justify-content: center;
  }
}
</style>