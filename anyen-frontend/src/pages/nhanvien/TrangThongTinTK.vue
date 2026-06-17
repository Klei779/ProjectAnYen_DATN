<template>
  <div class="account-page">
    <div v-if="loading" class="account-loading">
      <i class="fa-solid fa-spinner fa-spin"></i>
      Đang tải thông tin tài khoản...
    </div>

    <section v-else class="account-popup">
      <button class="close-x" type="button" @click="closePage">
        <i class="fa-solid fa-xmark"></i>
      </button>

      <h3>Thông tin tài khoản</h3>

      <div class="account-content">
        <aside class="profile-left">
          <div class="avatar-wrap">
            <div class="avatar-main">
              {{ initials }}
            </div>

            <button class="camera-btn" type="button" title="Ảnh đại diện">
              <i class="fa-solid fa-camera"></i>
            </button>
          </div>

          <h2>{{ account?.hoTen || "Nhân viên" }}</h2>

          <span
              class="status-badge"
              :class="{ inactive: account?.trangThai !== 'Đang hoạt động' }"
          >
            {{ account?.trangThai || "Đang hoạt động" }}
          </span>
        </aside>

        <main class="profile-right">
          <div class="readonly-grid">
            <label>Mã nhân viên</label>
            <div class="readonly-box">{{ maNhanVienDisplay }}</div>

            <label>Tên đăng nhập</label>
            <div class="readonly-box">{{ account?.tenDangNhap || "---" }}</div>

            <label>Vai trò</label>
            <div class="readonly-box">{{ account?.vaiTro || "---" }}</div>
          </div>

          <div class="section-title">
            <h4>Thông tin cá nhân</h4>

            <button
                v-if="!editMode"
                class="btn-edit"
                type="button"
                @click="startEdit"
            >
              <i class="fa-solid fa-pen"></i>
              Chỉnh sửa
            </button>
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
                  rows="3"
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

          <div class="bottom-actions">
            <button class="btn-close" type="button" @click="closePage">
              <i class="fa-solid fa-xmark"></i>
              Đóng
            </button>
          </div>
        </main>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import api from "../../api/api.js";

const router = useRouter();

const API_URL = "/api/nhan-vien/tai-khoan";

const loading = ref(false);
const saving = ref(false);
const editMode = ref(false);

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
  padding: 32px;
  background: #f8fafc;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  font-family: "Inter", Arial, sans-serif;
}

.account-loading {
  margin-top: 80px;
  padding: 18px 24px;
  background: #fff;
  border-radius: 14px;
  color: #0f172a;
  box-shadow: 0 12px 35px rgba(15, 23, 42, 0.08);
  display: flex;
  align-items: center;
  gap: 10px;
}

.account-popup {
  width: 860px;
  max-width: 100%;
  position: relative;
  background: #fff;
  border-radius: 20px;
  padding: 34px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.12);
}

.close-x {
  position: absolute;
  top: 24px;
  right: 26px;
  border: 0;
  background: transparent;
  color: #10264f;
  font-size: 21px;
  cursor: pointer;
}

.account-popup h3 {
  margin: 0 0 28px;
  color: #111827;
  font-size: 22px;
  font-weight: 800;
}

.account-content {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 34px;
}

.profile-left {
  padding-top: 28px;
  text-align: center;
}

.avatar-wrap {
  width: 140px;
  height: 140px;
  margin: 0 auto 20px;
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
  color: #111;
  font-size: 42px;
  font-weight: 800;
}

.camera-btn {
  position: absolute;
  right: 10px;
  bottom: 12px;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #fff;
  color: #0f172a;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.18);
  cursor: default;
}

.profile-left h2 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 21px;
  font-weight: 800;
}

.status-badge {
  display: inline-flex;
  padding: 7px 14px;
  border-radius: 7px;
  background: #dcfce7;
  color: #16803a;
  font-size: 13px;
  font-weight: 800;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #dc2626;
}

.profile-right {
  min-width: 0;
}

.readonly-grid {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 14px 18px;
  align-items: center;
  margin-bottom: 28px;
}

.readonly-grid label {
  color: #17335c;
  font-size: 14px;
  font-weight: 700;
}

.readonly-box {
  min-height: 40px;
  padding: 10px 14px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 14px;
  font-weight: 700;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-title h4 {
  margin: 0;
  color: #111827;
  font-size: 17px;
  font-weight: 800;
}

.btn-edit {
  height: 34px;
  padding: 0 14px;
  border: 1px solid #ff5b6b;
  border-radius: 7px;
  background: #fff;
  color: #ff3045;
  font-weight: 800;
  font-size: 13px;
  cursor: pointer;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-row {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 18px;
}

.info-row span {
  color: #17335c;
  font-size: 14px;
  font-weight: 600;
}

.info-row strong {
  color: #0f274f;
  font-size: 14px;
  font-weight: 800;
  word-break: break-word;
}

.edit-form {
  padding: 18px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eef2f7;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.form-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 14px;
  align-items: center;
  margin-bottom: 14px;
}

.form-row label {
  color: #17335c;
  font-size: 14px;
  font-weight: 700;
}

.form-row input,
.form-row textarea {
  width: 100%;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  padding: 10px 12px;
  color: #0f172a;
  outline: none;
  font-size: 14px;
}

.form-row textarea {
  resize: vertical;
}

.form-row input:focus,
.form-row textarea:focus {
  border-color: #ff5b6b;
  box-shadow: 0 0 0 3px rgba(255, 91, 107, 0.12);
}

.bottom-actions,
.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
  flex-wrap: wrap;
}

.action-row {
  margin-top: 20px;
}

.btn-outline,
.btn-save,
.btn-close {
  height: 42px;
  padding: 0 20px;
  border-radius: 7px;
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

.btn-close {
  border: 1px solid #ef0000;
  background: #ef0000;
  color: #fff;
}

.btn-save:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .account-page {
    padding: 18px;
  }

  .account-popup {
    padding: 24px 18px;
  }

  .account-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .profile-left {
    padding-top: 0;
  }

  .readonly-grid,
  .info-row,
  .form-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .bottom-actions,
  .action-row {
    justify-content: stretch;
  }

  .btn-outline,
  .btn-save,
  .btn-close {
    width: 100%;
    justify-content: center;
  }
}
</style>