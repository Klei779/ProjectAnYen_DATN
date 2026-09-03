<template>
  <div class="account-page">
    <!-- TIÊU ĐỀ -->
    <div class="page-header">
      <div>
        <h2>Thông tin tài khoản</h2>
        <p>Quản lý và cập nhật thông tin cá nhân của nhân viên</p>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="account-loading">
      <i class="fa-solid fa-spinner fa-spin"></i>
      Đang tải thông tin tài khoản...
    </div>

    <div v-else class="account-container">
      <!-- THÔNG TIN TÓM TẮT -->
      <aside class="profile-card">
        <div class="avatar-wrap">
          <div class="avatar-main">
            {{ initials }}
          </div>
        </div>

        <h3>{{ account?.hoTen || "Nhân viên" }}</h3>

        <span
            class="status-badge"
            :class="{ inactive: Number(account?.trangThai) === 0 }"
        >
          {{ account?.tenTrangThai || getDefaultStatus(account?.trangThai) }}
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

      <!-- THÔNG TIN CHI TIẾT -->
      <main class="info-card">
        <div class="card-header">
          <div>
            <h3>Thông tin cá nhân</h3>
            <p>Cập nhật thông tin liên hệ và địa chỉ của nhân viên</p>
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
                @click="showDoiMatKhau = true"
            >
              <i class="fa-solid fa-key"></i>
              Đổi mật khẩu
            </button>
          </div>
        </div>

        <!-- FORM CẬP NHẬT -->
        <form
            v-if="editMode"
            class="edit-form"
            @submit.prevent="submitUpdate"
        >
          <div class="form-section-title">
            <i class="fa-solid fa-user"></i>
            Thông tin cơ bản
          </div>

          <div class="form-row">
            <label>
              Họ và tên
              <span class="required">*</span>
            </label>

            <div class="input-area">
              <input
                  v-model="form.hoTen"
                  type="text"
                  maxlength="50"
                  placeholder="Nhập họ và tên"
              />

              <small class="helper-text">
                Tối đa 50 ký tự
              </small>
            </div>
          </div>

          <div class="form-row">
            <label>
              Số điện thoại
              <span class="required">*</span>
            </label>

            <div class="input-area">
              <input
                  v-model="form.soDienThoai"
                  type="text"
                  maxlength="10"
                  inputmode="numeric"
                  placeholder="Ví dụ: 0912345678"
                  @input="onlyNumberPhone"
              />

              <small class="helper-text">
                Số điện thoại Việt Nam gồm 10 chữ số
              </small>
            </div>
          </div>

          <div class="form-row">
            <label>
              Email
              <span class="required">*</span>
            </label>

            <div class="input-area">
              <input
                  v-model="form.email"
                  type="email"
                  maxlength="100"
                  placeholder="Ví dụ: example@anyen.vn"
              />
            </div>
          </div>

          <div class="form-section-title address-title">
            <i class="fa-solid fa-location-dot"></i>
            Thông tin địa chỉ
          </div>

          <!-- SỐ NHÀ, ĐƯỜNG -->
          <div class="form-row">
            <label>Số nhà, tên đường</label>

            <div class="input-area">
              <input
                  v-model="form.soNhaDuong"
                  type="text"
                  maxlength="255"
                  placeholder="Ví dụ: 123 Nguyễn Văn Linh"
              />

              <small class="helper-text">
                {{ form.soNhaDuong.length }}/255 ký tự
              </small>
            </div>
          </div>

          <!-- PHƯỜNG/XÃ -->
          <div class="form-row">
            <label>Phường/Xã</label>

            <div class="input-area">
              <input
                  v-model="form.phuongXa"
                  type="text"
                  maxlength="100"
                  placeholder="Ví dụ: Phường Tân Phong"
              />
            </div>
          </div>

          <!-- QUẬN/HUYỆN -->
          <div class="form-row">
            <label>Quận/Huyện</label>

            <div class="input-area">
              <input
                  v-model="form.quanHuyen"
                  type="text"
                  maxlength="100"
                  placeholder="Ví dụ: Quận 7"
              />
            </div>
          </div>

          <!-- TỈNH/THÀNH PHỐ -->
          <div class="form-row">
            <label>Tỉnh/Thành phố</label>

            <div class="input-area">
              <input
                  v-model="form.tinhThanh"
                  type="text"
                  maxlength="100"
                  placeholder="Ví dụ: Thành phố Hồ Chí Minh"
              />
            </div>
          </div>

          <div class="action-row">
            <button
                class="btn-outline"
                type="button"
                :disabled="saving"
                @click="cancelEdit"
            >
              <i class="fa-solid fa-xmark"></i>
              Hủy
            </button>

            <button
                class="btn-save"
                type="submit"
                :disabled="saving"
            >
              <i
                  class="fa-solid"
                  :class="
                  saving
                    ? 'fa-spinner fa-spin'
                    : 'fa-floppy-disk'
                "
              ></i>

              {{
                saving
                    ? "Đang lưu..."
                    : "Lưu thay đổi"
              }}
            </button>
          </div>
        </form>

        <!-- HIỂN THỊ THÔNG TIN -->
        <div v-else class="info-content">
          <div class="info-section">
            <div class="info-section-heading">
              <i class="fa-solid fa-user"></i>

              <div>
                <h4>Thông tin cơ bản</h4>
                <p>Thông tin cá nhân và liên hệ của nhân viên</p>
              </div>
            </div>

            <div class="info-list">
              <div class="info-row">
                <span>Họ và tên</span>

                <strong>
                  {{ account?.hoTen || "Chưa cập nhật" }}
                </strong>
              </div>

              <div class="info-row">
                <span>Số điện thoại</span>

                <strong>
                  {{ account?.soDienThoai || "Chưa cập nhật" }}
                </strong>
              </div>

              <div class="info-row">
                <span>Email</span>

                <strong>
                  {{ account?.email || "Chưa cập nhật" }}
                </strong>
              </div>
            </div>
          </div>

          <div class="info-section address-section">
            <div class="info-section-heading">
              <i class="fa-solid fa-location-dot"></i>

              <div>
                <h4>Thông tin địa chỉ</h4>
                <p>Địa chỉ cư trú hiện tại của nhân viên</p>
              </div>
            </div>

            <div class="info-list">
              <div class="info-row">
                <span>Số nhà, tên đường</span>

                <strong>
                  {{ account?.soNhaDuong || "Chưa cập nhật" }}
                </strong>
              </div>

              <div class="info-row">
                <span>Phường/Xã</span>

                <strong>
                  {{ account?.phuongXa || "Chưa cập nhật" }}
                </strong>
              </div>

              <div class="info-row">
                <span>Quận/Huyện</span>

                <strong>
                  {{ account?.quanHuyen || "Chưa cập nhật" }}
                </strong>
              </div>

              <div class="info-row">
                <span>Tỉnh/Thành phố</span>

                <strong>
                  {{ account?.tinhThanh || "Chưa cập nhật" }}
                </strong>
              </div>
            </div>
          </div>
        </div>

        <!-- ĐỔI MẬT KHẨU -->
        <DoiMatKhau
            v-model="showDoiMatKhau"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref,
} from "vue";

import { ElMessage } from "element-plus";

import api from "../../api/api.js";
import DoiMatKhau from "../../components/DoiMatKhau.vue";

const API_URL = "/api/nhan-vien/tai-khoan";

const loading = ref(false);
const saving = ref(false);
const editMode = ref(false);
const showDoiMatKhau = ref(false);

const account = ref(null);

/*
 * Không còn trường diaChi.
 * Thay bằng 4 trường địa chỉ mới.
 */
const form = reactive({
  hoTen: "",
  email: "",
  soDienThoai: "",
  soNhaDuong: "",
  phuongXa: "",
  quanHuyen: "",
  tinhThanh: "",
});

/* =========================
   COMPUTED
========================= */
const initials = computed(() => {
  const name =
      account.value?.hoTen ||
      account.value?.tenDangNhap ||
      "NV";

  const parts = String(name)
      .trim()
      .split(/\s+/)
      .filter(Boolean);

  if (parts.length === 0) {
    return "NV";
  }

  if (parts.length === 1) {
    return parts[0]
        .substring(0, 2)
        .toUpperCase();
  }

  return `${parts[0][0]}${parts[parts.length - 1][0]}`
      .toUpperCase();
});

const maNhanVienDisplay = computed(() => {
  const id = account.value?.maNhanVien;

  if (id === null || id === undefined || id === "") {
    return "---";
  }

  return `NV${String(id).padStart(2, "0")}`;
});

/*
 * Ghép địa chỉ đầy đủ chỉ để hiển thị.
 * Không gửi trường diaChi về backend.
 */


/* =========================
   KHỞI TẠO
========================= */
onMounted(() => {
  loadAccount();
});

/* =========================
   HÀM HỖ TRỢ
========================= */
function getDefaultStatus(trangThai) {
  return Number(trangThai) === 0
      ? "Đã nghỉ việc"
      : "Đang hoạt động";
}

function normalizeText(value) {
  return String(value ?? "").trim();
}

function onlyNumberPhone(event) {
  const value = event.target.value
      .replace(/\D/g, "")
      .slice(0, 10);

  form.soDienThoai = value;
}

function fillForm(data) {
  form.hoTen = data?.hoTen || "";
  form.email = data?.email || "";
  form.soDienThoai = data?.soDienThoai || "";

  form.soNhaDuong = data?.soNhaDuong || "";
  form.phuongXa = data?.phuongXa || "";
  form.quanHuyen = data?.quanHuyen || "";
  form.tinhThanh = data?.tinhThanh || "";
}

/* =========================
   TẢI THÔNG TIN TÀI KHOẢN
========================= */
async function loadAccount() {
  loading.value = true;

  try {
    const response = await api.get(`${API_URL}/me`);

    account.value = response.data;

    fillForm(response.data);
    syncLocalUser(response.data);
  } catch (error) {
    console.error(
        "Lỗi tải thông tin tài khoản:",
        error
    );

    ElMessage.error(
        getErrorMessage(
            error,
            "Không tải được thông tin tài khoản"
        )
    );
  } finally {
    loading.value = false;
  }
}

/* =========================
   MỞ/ĐÓNG CHỈNH SỬA
========================= */
function startEdit() {
  fillForm(account.value);
  editMode.value = true;
}

function cancelEdit() {
  fillForm(account.value);
  editMode.value = false;
}

/* =========================
   VALIDATE
========================= */
function validateForm() {
  const hoTen = normalizeText(form.hoTen);
  const email = normalizeText(form.email);
  const soDienThoai = normalizeText(form.soDienThoai);

  const soNhaDuong = normalizeText(form.soNhaDuong);
  const phuongXa = normalizeText(form.phuongXa);
  const quanHuyen = normalizeText(form.quanHuyen);
  const tinhThanh = normalizeText(form.tinhThanh);

  if (!hoTen) {
    ElMessage.warning("Vui lòng nhập họ và tên");
    return false;
  }

  if (hoTen.length > 50) {
    ElMessage.warning("Họ và tên không được vượt quá 50 ký tự");
    return false;
  }

  if (!email) {
    ElMessage.warning("Vui lòng nhập email");
    return false;
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning("Email không đúng định dạng");
    return false;
  }

  if (!soDienThoai) {
    ElMessage.warning("Vui lòng nhập số điện thoại");
    return false;
  }

  if (!/^0[35789][0-9]{8}$/.test(soDienThoai)) {
    ElMessage.warning(
        "Số điện thoại không đúng định dạng Việt Nam"
    );

    return false;
  }

  if (soNhaDuong.length > 255) {
    ElMessage.warning(
        "Số nhà, tên đường không được vượt quá 255 ký tự"
    );

    return false;
  }

  if (phuongXa.length > 100) {
    ElMessage.warning(
        "Phường/Xã không được vượt quá 100 ký tự"
    );

    return false;
  }

  if (quanHuyen.length > 100) {
    ElMessage.warning(
        "Quận/Huyện không được vượt quá 100 ký tự"
    );

    return false;
  }

  if (tinhThanh.length > 100) {
    ElMessage.warning(
        "Tỉnh/Thành phố không được vượt quá 100 ký tự"
    );

    return false;
  }

  return true;
}

/* =========================
   CẬP NHẬT TÀI KHOẢN
========================= */
async function submitUpdate() {
  if (!validateForm()) {
    return;
  }

  saving.value = true;

  try {
    /*
     * Payload không còn diaChi.
     * Gửi đúng 4 trường địa chỉ backend đang sử dụng.
     */
    const payload = {
      hoTen: normalizeText(form.hoTen),
      email: normalizeText(form.email),
      soDienThoai: normalizeText(form.soDienThoai),

      soNhaDuong: normalizeText(form.soNhaDuong),
      phuongXa: normalizeText(form.phuongXa),
      quanHuyen: normalizeText(form.quanHuyen),
      tinhThanh: normalizeText(form.tinhThanh),
    };

    const response = await api.put(
        `${API_URL}/me`,
        payload
    );

    account.value = response.data;

    fillForm(response.data);
    syncLocalUser(response.data);

    editMode.value = false;

    ElMessage.success(
        "Cập nhật thông tin tài khoản thành công"
    );
  } catch (error) {
    console.error(
        "Lỗi cập nhật tài khoản:",
        error
    );

    ElMessage.error(
        getErrorMessage(
            error,
            "Cập nhật thông tin thất bại"
        )
    );
  } finally {
    saving.value = false;
  }
}

/* =========================
   ĐỒNG BỘ LOCALSTORAGE
========================= */
function syncLocalUser(data) {
  let oldUser = {};

  try {
    oldUser = JSON.parse(
        localStorage.getItem("user") || "{}"
    );
  } catch (error) {
    console.error(
        "Không đọc được user trong localStorage:",
        error
    );
  }

  const newUser = {
    ...oldUser,

    id: data?.maNhanVien,
    maNhanVien: data?.maNhanVien,
    hoTen: data?.hoTen,
    tenDangNhap: data?.tenDangNhap,
    email: data?.email,
    soDienThoai: data?.soDienThoai,

    // Các trường địa chỉ mới
    soNhaDuong: data?.soNhaDuong,
    phuongXa: data?.phuongXa,
    quanHuyen: data?.quanHuyen,
    tinhThanh: data?.tinhThanh,

    vaiTro: data?.vaiTro,
    tenVaiTro: data?.tenVaiTro,
    trangThai: data?.trangThai,
    tenTrangThai: data?.tenTrangThai,
  };

  /*
   * Xóa địa chỉ cũ khỏi object trước khi lưu.
   */
  delete newUser.diaChi;

  localStorage.setItem(
      "user",
      JSON.stringify(newUser)
  );

  window.dispatchEvent(
      new Event("session-updated")
  );
}

/* =========================
   XỬ LÝ THÔNG BÁO LỖI
========================= */
function getErrorMessage(error, fallback) {
  const responseData = error?.response?.data;

  if (typeof responseData === "string") {
    return responseData;
  }

  if (
      responseData &&
      typeof responseData === "object"
  ) {
    /*
     * Trường hợp backend trả lỗi validate theo từng field.
     */
    const fieldErrors = Object.entries(responseData)
        .filter(([key]) => !["message", "error"].includes(key))
        .map(([, value]) => {
          if (Array.isArray(value)) {
            return value.join(", ");
          }

          return String(value);
        })
        .filter(Boolean);

    if (fieldErrors.length) {
      return fieldErrors.join(". ");
    }
  }

  return (
      responseData?.message ||
      responseData?.error ||
      fallback
  );
}
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.account-page {
  min-height: calc(100vh - 80px);
  padding: 28px;
  background: #f5f7fb;
  font-family: "Inter", Arial, sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1100px;
  margin: 0 auto 22px;
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

.account-loading {
  display: flex;
  align-items: center;
  max-width: 1100px;
  margin: 40px auto 0;
  padding: 18px 24px;
  gap: 10px;
  border-radius: 14px;
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.account-container {
  display: grid;
  grid-template-columns: 320px 1fr;
  align-items: start;
  max-width: 1100px;
  margin: 0 auto;
  gap: 24px;
}

.profile-card,
.info-card {
  border: 1px solid #eef2f7;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.07);
}

.profile-card {
  padding: 30px 24px;
  text-align: center;
}

.avatar-wrap {
  position: relative;
  width: 140px;
  height: 140px;
  margin: 0 auto 18px;
}

.avatar-main {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: #fdeff7;
  color: #111827;
  font-size: 42px;
  font-weight: 800;
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
  display: flex;
  flex-direction: column;
  margin-top: 26px;
  padding-top: 18px;
  gap: 14px;
  border-top: 1px solid #eef2f7;
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
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 22px;
  padding-bottom: 20px;
  gap: 16px;
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

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.btn-edit,
.btn-password {
  display: inline-flex;
  align-items: center;
  height: 38px;
  padding: 0 15px;
  gap: 8px;
  border-radius: 9px;
  background: #ffffff;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.btn-edit {
  border: 1px solid #ff5b6b;
  color: #ff3045;
}

.btn-edit:hover {
  background: #fff1f2;
}

.btn-password {
  border: 1px solid #17934a;
  color: #17934a;
}

.btn-password:hover {
  background: #e8f8ef;
}

/* =========================
   THÔNG TIN HIỂN THỊ
========================= */
.info-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section {
  overflow: hidden;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #ffffff;
}

.info-section-heading {
  display: flex;
  align-items: center;
  padding: 16px 18px;
  gap: 12px;
  border-bottom: 1px solid #eef2f7;
  background: #f8fafc;
}

.info-section-heading > i {
  display: grid;
  place-items: center;
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #eaf2ff;
  color: #2563eb;
}

.address-section .info-section-heading > i {
  background: #fff1f2;
  color: #ff3045;
}

.info-section-heading h4 {
  margin: 0;
  color: #172033;
  font-size: 15px;
  font-weight: 800;
}

.info-section-heading p {
  margin: 4px 0 0;
  color: #7b879a;
  font-size: 12px;
}

.info-list {
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 12px;
}

.info-row {
  display: grid;
  grid-template-columns: 180px 1fr;
  align-items: center;
  min-height: 46px;
  padding: 12px 14px;
  gap: 18px;
  border-radius: 11px;
  background: #f8fafc;
}

.info-row span,
.full-address span {
  color: #17335c;
  font-size: 14px;
  font-weight: 700;
}

.info-row strong,
.full-address strong {
  color: #0f274f;
  font-size: 14px;
  font-weight: 800;
  word-break: break-word;
}

.full-address {
  display: grid;
  grid-template-columns: 180px 1fr;
  align-items: start;
  min-height: 52px;
  padding: 14px;
  gap: 18px;
  border: 1px dashed #bfdbfe;
  border-radius: 11px;
  background: #eff6ff;
}

.full-address strong {
  line-height: 1.6;
}

/* =========================
   FORM
========================= */
.edit-form {
  padding: 20px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #f8fafc;
}

.form-section-title {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
  padding-bottom: 12px;
  gap: 9px;
  border-bottom: 1px solid #e2e8f0;
  color: #17335c;
  font-size: 14px;
  font-weight: 800;
}

.form-section-title i {
  color: #2563eb;
}

.form-section-title.address-title {
  margin-top: 28px;
}

.form-section-title.address-title i {
  color: #ff3045;
}

.form-row {
  display: grid;
  grid-template-columns: 170px 1fr;
  align-items: start;
  margin-bottom: 16px;
  gap: 16px;
}

.form-row > label {
  padding-top: 12px;
  color: #17335c;
  font-size: 14px;
  font-weight: 800;
}

.required {
  color: #dc2626;
}

.input-area {
  min-width: 0;
}

.form-row input {
  width: 100%;
  height: 44px;
  padding: 0 13px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  outline: none;
  background: #ffffff;
  color: #0f172a;
  font-size: 14px;
  transition:
      border-color 0.16s ease,
      box-shadow 0.16s ease;
}

.form-row input:focus {
  border-color: #ff5b6b;
  box-shadow: 0 0 0 3px rgba(255, 91, 107, 0.12);
}

.helper-text {
  display: block;
  margin-top: 6px;
  color: #8994a6;
  font-size: 11px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  margin-top: 26px;
  padding-top: 18px;
  gap: 12px;
  border-top: 1px solid #e2e8f0;
}

.btn-outline,
.btn-save {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  padding: 0 20px;
  gap: 8px;
  border-radius: 9px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.btn-outline {
  border: 1px solid #ff5b6b;
  background: #ffffff;
  color: #ff3045;
}

.btn-outline:hover:not(:disabled) {
  background: #fff1f2;
}

.btn-save {
  border: 1px solid #ff3045;
  background: #ff3045;
  color: #ffffff;
}

.btn-save:hover:not(:disabled) {
  background: #e9243a;
}

.btn-save:disabled,
.btn-outline:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

/* =========================
   MẬT KHẨU
========================= */
.password-section {
  opacity: 0.7;
}

/* =========================
   M  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eef2f7;
}

.password-section :deep(.change-password-card) {
  max-width: 100%;
  border: 1px solid #eef2f7;
  box-shadow: none;
}

/* =========================
   RESPONSIVE
========================= */
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

  .info-card,
  .profile-card {
    padding: 22px 18px;
  }

  .card-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
  }

  .btn-edit,
  .btn-password {
    width: 100%;
    justify-content: center;
  }

  .info-row,
  .full-address,
  .form-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .form-row > label {
    padding-top: 0;
  }

  .action-row {
    justify-content: stretch;
  }

  .btn-outline,
  .btn-save {
    width: 100%;
  }
}
</style>