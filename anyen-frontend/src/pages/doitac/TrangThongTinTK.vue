<template>
  <div class="account-page">
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
        </div>

        <h3>
          {{ account?.tenDoiTac || "Đối tác" }}
        </h3>

        <span
            class="status-badge"
            :class="{
              inactive: !trangThaiDangHoatDong
            }"
        >
          {{ trangThaiDisplay }}
        </span>

        <div class="profile-summary">
          <div>
            <span>Mã đối tác</span>
            <strong>{{ maDoiTacDisplay }}</strong>
          </div>

          <div>
            <span>Tên đăng nhập</span>

            <strong>
              {{ account?.tenDangNhap || "---" }}
            </strong>
          </div>

          <div>
            <span>Vai trò</span>
            <strong>Đối tác</strong>
          </div>
        </div>
      </aside>

      <main class="info-card">
        <div class="card-header">
          <div>
            <h3>Thông tin cá nhân</h3>

            <p>
              Chỉ cập nhật các thông tin có trong
              bảng đối tác
            </p>
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

        <form
            v-if="editMode"
            class="edit-form"
            @submit.prevent="submitUpdate"
        >
          <div class="form-row">
            <label>Tên đối tác</label>

            <input
                v-model.trim="form.tenDoiTac"
                type="text"
                placeholder="Nhập tên đối tác"
            />
          </div>

          <div class="form-row">
            <label>Tên doanh nghiệp</label>

            <input
                v-model.trim="form.tenDoanhNghiep"
                type="text"
                placeholder="Nhập tên doanh nghiệp"
            />
          </div>

          <div class="form-row">
            <label>Mã số thuế</label>

            <input
                v-model.trim="form.maSoThue"
                type="text"
                placeholder="Nhập mã số thuế"
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
            <button
                class="btn-outline"
                type="button"
                @click="cancelEdit"
            >
              Hủy
            </button>

            <button
                class="btn-save"
                type="submit"
                :disabled="saving"
            >
              <i
                  v-if="saving"
                  class="fa-solid fa-spinner fa-spin"
              ></i>

              <i
                  v-else
                  class="fa-solid fa-floppy-disk"
              ></i>

              Lưu thay đổi
            </button>
          </div>
        </form>

        <div v-else class="info-list">
          <div class="info-row">
            <span>Tên đối tác</span>

            <strong>
              {{
                account?.tenDoiTac ||
                "Chưa cập nhật"
              }}
            </strong>
          </div>

          <div class="info-row">
            <span>Tên doanh nghiệp</span>

            <strong>
              {{
                account?.tenDoanhNghiep ||
                "Chưa cập nhật"
              }}
            </strong>
          </div>

          <div class="info-row">
            <span>Mã số thuế</span>

            <strong>
              {{
                account?.maSoThue ||
                "Chưa cập nhật"
              }}
            </strong>
          </div>

          <div class="info-row">
            <span>Số điện thoại</span>

            <strong>
              {{
                account?.soDienThoai ||
                "Chưa cập nhật"
              }}
            </strong>
          </div>

          <div class="info-row">
            <span>Email</span>

            <strong>
              {{
                account?.email ||
                "Chưa cập nhật"
              }}
            </strong>
          </div>

          <div class="info-row">
            <span>Địa chỉ</span>

            <strong>
              {{
                account?.diaChi ||
                "Chưa cập nhật"
              }}
            </strong>

          </div>
        </div>
        <button
            class="btn-edit"
            type="button"
            @click="getLocation"
        >
          <i class="fa-solid fa-pen"></i>
          Update vị trí
        </button>

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
  ref
} from "vue";

import {ElMessage} from "element-plus";
import api from "../../api/api.js";
import DoiMatKhau from "../../components/DoiMatKhau.vue";

const API_URL = "/api/doi-tac/tai-khoan";

const loading = ref(false);
const saving = ref(false);
const editMode = ref(false);
const showDoiMatKhau = ref(false);

const account = ref(null);

const form = reactive({
  tenDoiTac: "",
  tenDoanhNghiep: "",
  maSoThue: "",
  email: "",
  soDienThoai: "",
  diaChi: ""
});

/**
 * Tạo chữ viết tắt dùng cho ảnh đại diện.
 *
 * Ví dụ:
 * Nguyễn Văn A -> NA
 */
const initials = computed(() => {
  const name =
      account.value?.tenDoiTac ||
      account.value?.tenDangNhap ||
      "DT";

  const parts = name
      .trim()
      .split(/\s+/);

  if (parts.length === 1) {
    return parts[0]
        .substring(0, 2)
        .toUpperCase();
  }

  return (
      `${parts[0][0]}${parts[parts.length - 1][0]}`
  ).toUpperCase();
});

/**
 * Hiển thị mã đối tác.
 *
 * Ví dụ:
 * 1 -> DT01
 * 12 -> DT12
 */
const maDoiTacDisplay = computed(() => {
  const id = account.value?.maDoiTac;

  if (
      id === null ||
      id === undefined ||
      id === ""
  ) {
    return "---";
  }

  return `DT${String(id).padStart(2, "0")}`;
});

/**
 * Kiểm tra trạng thái hiện tại có đang hoạt động không.
 *
 * Backend hiện trả:
 * 1 = đang hoạt động
 * 0 = ngừng hoạt động
 */
const trangThaiDangHoatDong = computed(() => {
  const trangThai =
      account.value?.trangThai;

  if (
      trangThai === null ||
      trangThai === undefined ||
      trangThai === ""
  ) {
    return false;
  }

  if (Number(trangThai) === 1) {
    return true;
  }

  const text = String(trangThai)
      .trim()
      .toLowerCase();

  return (
      text === "đang hoạt động" ||
      text === "dang hoat dong" ||
      text === "đang hợp tác" ||
      text === "dang hop tac"
  );
});

/**
 * Chuyển trạng thái dạng số sang chữ
 * để không còn hiển thị số 1 trên giao diện.
 */
const trangThaiDisplay = computed(() => {
  const trangThai =
      account.value?.trangThai;

  if (
      trangThai === null ||
      trangThai === undefined ||
      trangThai === ""
  ) {
    return "Chưa cập nhật";
  }

  const numericStatus =
      Number(trangThai);

  if (!Number.isNaN(numericStatus)) {
    if (numericStatus === 1) {
      return "Đang hoạt động";
    }

    if (numericStatus === 0) {
      return "Ngừng hoạt động";
    }
  }

  return String(trangThai);
});

onMounted(() => {
  loadAccount();
});

/**
 * Lấy thông tin tài khoản đối tác đang đăng nhập.
 */
async function loadAccount() {
  loading.value = true;

  try {
    const response = await api.get(
        `${API_URL}/me`
    );

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

/**
 * Đổ dữ liệu tài khoản vào form chỉnh sửa.
 */
function fillForm(data) {
  form.tenDoiTac =
      data?.tenDoiTac || "";

  form.tenDoanhNghiep =
      data?.tenDoanhNghiep || "";

  form.maSoThue =
      data?.maSoThue || "";

  form.email =
      data?.email || "";

  form.soDienThoai =
      data?.soDienThoai || "";

  form.diaChi =
      data?.diaChi || "";
}

/**
 * Bắt đầu chỉnh sửa thông tin.
 */
function startEdit() {
  fillForm(account.value);
  editMode.value = true;
}

/**
 * Hủy chỉnh sửa và khôi phục dữ liệu cũ.
 */
function cancelEdit() {
  fillForm(account.value);
  editMode.value = false;
}

/**
 * Gửi yêu cầu cập nhật thông tin tài khoản.
 */
async function submitUpdate() {
  if (!form.tenDoiTac.trim()) {
    ElMessage.warning(
        "Vui lòng nhập họ và tên"
    );

    return;
  }

  if (
      form.email &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
          form.email
      )
  ) {
    ElMessage.warning(
        "Email không đúng định dạng"
    );

    return;
  }

  if (
      form.soDienThoai &&
      !/^[0-9]{9,20}$/.test(
          form.soDienThoai
      )
  ) {
    ElMessage.warning(
        "Số điện thoại chỉ gồm 9 - 20 chữ số"
    );

    return;
  }

  saving.value = true;

  try {
    const response = await api.put(
        `${API_URL}/me`,
        {
          tenDoiTac:
          form.tenDoiTac,

          tenDoanhNghiep:
          form.tenDoanhNghiep,

          maSoThue:
          form.maSoThue,

          email:
          form.email,

          soDienThoai:
          form.soDienThoai,

          diaChi:
          form.diaChi
        }
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

/**
 * Đồng bộ lại thông tin người dùng trong localStorage.
 *
 * API tài khoản đối tác không trả vaiTroChiTiet,
 * nên không được gán vaiTro bằng undefined.
 */
function syncLocalUser(data) {
  let oldUser = {};

  try {
    oldUser = JSON.parse(
        localStorage.getItem("user") ||
        "{}"
    );
  } catch (error) {
    console.error(
        "Không thể đọc dữ liệu người dùng trong localStorage:",
        error
    );
  }

  const newUser = {
    ...oldUser,

    id:
        data?.maDoiTac ??
        oldUser.id,

    maDoiTac:
        data?.maDoiTac ??
        oldUser.maDoiTac,

    // Header layout đang lấy hoTen,
    // nên cập nhật lại khi đổi tên đối tác.
    hoTen:
        data?.tenDoiTac ||
        oldUser.hoTen,

    tenDoiTac:
        data?.tenDoiTac ||
        oldUser.tenDoiTac,

    tenDoanhNghiep:
        data?.tenDoanhNghiep ??
        oldUser.tenDoanhNghiep,

    maSoThue:
        data?.maSoThue ??
        oldUser.maSoThue,

    tenDangNhap:
        data?.tenDangNhap ||
        oldUser.tenDangNhap,

    email:
        data?.email ??
        oldUser.email,

    soDienThoai:
        data?.soDienThoai ??
        oldUser.soDienThoai,

    diaChi:
        data?.diaChi ??
        oldUser.diaChi,

    trangThai:
        data?.trangThai ??
        oldUser.trangThai,

    /*
     * API tài khoản đối tác không trả quyền.
     * Giữ quyền hiện tại để router không
     * mất quyền truy cập trang đối tác.
     */
    loaiTaiKhoan:
        oldUser.loaiTaiKhoan ||
        "DOI_TAC",

    vaiTroChiTiet:
        oldUser.vaiTroChiTiet ||
        "DOITAC"
  };

  localStorage.setItem(
      "user",
      JSON.stringify(newUser)
  );

  window.dispatchEvent(
      new Event("session-updated")
  );
}

/**
 * Lấy thông báo lỗi từ backend.
 */
function getErrorMessage(
    error,
    fallback
) {
  return (
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      fallback
  );
}

async function getLocation() {
  if (!navigator.geolocation) {
    ElMessage.error("Trình duyệt không hỗ trợ Geolocation");
    return;
  }

  navigator.geolocation.getCurrentPosition(
      async (position) => {
        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;

        try {
          const payload = {
            latitude,
            longitude,
          };
          const response = await api.post(
              `${API_URL}/location`,
              payload
          );

          console.log("Cập nhật vị trí thành công:", response.data);

          ElMessage.success(
              "Cập nhật vị trí thành công"
          );
        } catch (error) {
          console.error(
              "Lỗi cập nhật vị trí:",
              error
          );

          ElMessage.error(
              getErrorMessage(
                  error,
                  "Cập nhật vị trí thất bại"
              )
          );
        }
      },
      (error) => {
        console.error(
            "Không lấy được vị trí:",
            error
        );

        switch (error.code) {
          case error.PERMISSION_DENIED:
            ElMessage.warning(
                "Bạn đã từ chối quyền truy cập vị trí"
            );
            break;

          case error.POSITION_UNAVAILABLE:
            ElMessage.error(
                "Không xác định được vị trí hiện tại"
            );
            break;

          case error.TIMEOUT:
            ElMessage.error(
                "Lấy vị trí quá thời gian cho phép"
            );
            break;

          default:
            ElMessage.error(
                "Không lấy được vị trí hiện tại"
            );
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
  );
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