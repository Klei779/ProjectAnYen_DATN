<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import api from "../api/api.js";

const loading = ref(false);
const showOldPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const form = ref({
  matKhauCu: "",
  matKhauMoi: "",
  xacNhanMatKhau: "",
});

const resetForm = () => {
  form.value = {
    matKhauCu: "",
    matKhauMoi: "",
    xacNhanMatKhau: "",
  };
};

const getErrorMessage = (error) => {
  return (
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.response?.data ||
      "Đổi mật khẩu thất bại"
  );
};

const handleDoiMatKhau = async () => {
  const matKhauCu = form.value.matKhauCu.trim();
  const matKhauMoi = form.value.matKhauMoi.trim();
  const xacNhanMatKhau = form.value.xacNhanMatKhau.trim();

  if (!matKhauCu) {
    ElMessage.warning("Vui lòng nhập mật khẩu cũ");
    return;
  }

  if (!matKhauMoi) {
    ElMessage.warning("Vui lòng nhập mật khẩu mới");
    return;
  }

  if (matKhauMoi.length < 6) {
    ElMessage.warning("Mật khẩu mới phải có ít nhất 6 ký tự");
    return;
  }

  if (!xacNhanMatKhau) {
    ElMessage.warning("Vui lòng xác nhận mật khẩu mới");
    return;
  }

  if (matKhauMoi !== xacNhanMatKhau) {
    ElMessage.error("Xác nhận mật khẩu không khớp");
    return;
  }

  if (matKhauCu === matKhauMoi) {
    ElMessage.warning("Mật khẩu mới không được trùng mật khẩu cũ");
    return;
  }

  try {
    loading.value = true;

    const response = await api.put("/api/tai-khoan/doi-mat-khau", {
      matKhauCu,
      matKhauMoi,
      xacNhanMatKhau,
    });

    ElMessage.success(response.data?.message || "Đổi mật khẩu thành công");
    resetForm();
  } catch (error) {
    console.error("Lỗi đổi mật khẩu:", error);
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="change-password-card">
    <div class="card-header">
      <div>
        <h3>Thay đổi mật khẩu</h3>
        <p>Cập nhật mật khẩu đăng nhập cho tài khoản hiện tại</p>
      </div>
    </div>

    <div class="form-body">
      <div class="form-group">
        <label>Mật khẩu cũ <span>*</span></label>

        <div class="password-input">
          <input
              v-model="form.matKhauCu"
              :type="showOldPassword ? 'text' : 'password'"
              placeholder="Nhập mật khẩu cũ"
              autocomplete="current-password"
          />

          <button type="button" @click="showOldPassword = !showOldPassword">
            {{ showOldPassword ? "Ẩn" : "Hiện" }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label>Mật khẩu mới <span>*</span></label>

        <div class="password-input">
          <input
              v-model="form.matKhauMoi"
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="Nhập mật khẩu mới"
              autocomplete="new-password"
          />

          <button type="button" @click="showNewPassword = !showNewPassword">
            {{ showNewPassword ? "Ẩn" : "Hiện" }}
          </button>
        </div>

        <small>Mật khẩu mới phải có ít nhất 6 ký tự</small>
      </div>

      <div class="form-group">
        <label>Xác nhận mật khẩu mới <span>*</span></label>

        <div class="password-input">
          <input
              v-model="form.xacNhanMatKhau"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="Nhập lại mật khẩu mới"
              autocomplete="new-password"
              @keyup.enter="handleDoiMatKhau"
          />

          <button type="button" @click="showConfirmPassword = !showConfirmPassword">
            {{ showConfirmPassword ? "Ẩn" : "Hiện" }}
          </button>
        </div>
      </div>

      <div class="form-actions">
        <button
            type="button"
            class="btn-reset"
            :disabled="loading"
            @click="resetForm"
        >
          Làm mới
        </button>

        <button
            type="button"
            class="btn-submit"
            :disabled="loading"
            @click="handleDoiMatKhau"
        >
          {{ loading ? "Đang đổi..." : "Đổi mật khẩu" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.change-password-card {
  width: 100%;
  max-width: 560px;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #edf0f2;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 24px;
  box-sizing: border-box;
}

.card-header {
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef1f4;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
}

.card-header p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 7px;
  font-size: 13px;
  font-weight: 700;
  color: #374151;
}

.form-group label span {
  color: #ef4444;
}

.form-group small {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}

.password-input {
  display: flex;
  align-items: center;
  height: 42px;
  border: 1px solid #d9dee3;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  transition: all 0.2s;
}

.password-input:focus-within {
  border-color: #17934a;
  box-shadow: 0 0 0 3px rgba(23, 147, 74, 0.1);
}

.password-input input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  padding: 0 12px;
  font-size: 14px;
  background: transparent;
  box-sizing: border-box;
}

.password-input button {
  height: 100%;
  border: none;
  border-left: 1px solid #edf0f2;
  background: #f9fafb;
  color: #17934a;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.password-input button:hover {
  background: #e8f8ef;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.btn-reset,
.btn-submit {
  height: 40px;
  border-radius: 8px;
  padding: 0 18px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset {
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #374151;
}

.btn-reset:hover {
  background: #f3f4f6;
}

.btn-submit {
  border: 1px solid #17934a;
  background: #17934a;
  color: #ffffff;
}

.btn-submit:hover {
  background: #137d3e;
}

.btn-reset:disabled,
.btn-submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .change-password-card {
    max-width: 100%;
    padding: 18px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-reset,
  .btn-submit {
    width: 100%;
  }
}
</style>