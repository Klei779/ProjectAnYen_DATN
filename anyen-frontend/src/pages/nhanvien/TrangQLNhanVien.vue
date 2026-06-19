<template>
  <div class="nhan-vien-page">
    <button class="btn-add" @click="openCreateForm">
      Thêm nhân viên
    </button>

    <div v-if="showCreateForm" class="modal-overlay">
      <div class="modal-box">
        <div class="modal-header">
          <h3>Thêm nhân viên</h3>
          <button class="btn-close" @click="closeCreateForm">×</button>
        </div>

        <div class="form-group">
          <label>Họ tên</label>
          <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" />
          <p v-if="errors.hoTen" class="error">{{ errors.hoTen }}</p>
        </div>

        <div class="form-group">
          <label>Tên đăng nhập</label>
          <input v-model="form.tenDangNhap" type="text" placeholder="Nhập tên đăng nhập" />
          <p v-if="errors.tenDangNhap" class="error">{{ errors.tenDangNhap }}</p>
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <input v-model="form.matKhau" type="password" placeholder="Nhập mật khẩu" />
          <p v-if="errors.matKhau" class="error">{{ errors.matKhau }}</p>
        </div>

        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="text" placeholder="Nhập email" />
          <p v-if="errors.email" class="error">{{ errors.email }}</p>
        </div>

        <div class="form-group">
          <label>Số điện thoại</label>
          <input v-model="form.soDienThoai" type="text" placeholder="Nhập số điện thoại" />
          <p v-if="errors.soDienThoai" class="error">{{ errors.soDienThoai }}</p>
        </div>

        <div class="form-group">
          <label>Địa chỉ</label>
          <input v-model="form.diaChi" type="text" placeholder="Nhập địa chỉ" />
          <p v-if="errors.diaChi" class="error">{{ errors.diaChi }}</p>
        </div>

        <div class="form-group">
          <label>Vai trò</label>
          <select v-model="form.vaiTro">
            <option value="">-- Chọn vai trò --</option>
            <option value="NHAN_VIEN">Nhân viên</option>
            <option value="QUAN_LY">Quản lý</option>
            <option value="HOTLINE">Hotline</option>
          </select>
          <p v-if="errors.vaiTro" class="error">{{ errors.vaiTro }}</p>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeCreateForm">Hủy</button>
          <button class="btn-save" @click="submitCreateNhanVien">
            Lưu nhân viên
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { createNhanVien } from "../../services/QuanLyNhanVienService.js";

const showCreateForm = ref(false);

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

function resetForm() {
  form.hoTen = "";
  form.tenDangNhap = "";
  form.matKhau = "";
  form.email = "";
  form.soDienThoai = "";
  form.diaChi = "";
  form.vaiTro = "";

  clearErrors();
}

function clearErrors() {
  Object.keys(errors).forEach((key) => {
    delete errors[key];
  });
}

function openCreateForm() {
  resetForm();
  showCreateForm.value = true;
}

function closeCreateForm() {
  showCreateForm.value = false;
  resetForm();
}

function validateForm() {
  clearErrors();

  let isValid = true;

  if (!form.hoTen.trim()) {
    errors.hoTen = "Họ tên không được để trống";
    isValid = false;
  }

  if (!form.tenDangNhap.trim()) {
    errors.tenDangNhap = "Tên đăng nhập không được để trống";
    isValid = false;
  } else if (form.tenDangNhap.trim().length < 4) {
    errors.tenDangNhap = "Tên đăng nhập phải từ 4 ký tự trở lên";
    isValid = false;
  }

  if (!form.matKhau.trim()) {
    errors.matKhau = "Mật khẩu không được để trống";
    isValid = false;
  } else if (form.matKhau.trim().length < 6) {
    errors.matKhau = "Mật khẩu phải có ít nhất 6 ký tự";
    isValid = false;
  }

  if (!form.email.trim()) {
    errors.email = "Email không được để trống";
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = "Email không đúng định dạng";
    isValid = false;
  }

  if (!form.soDienThoai.trim()) {
    errors.soDienThoai = "Số điện thoại không được để trống";
    isValid = false;
  } else if (!/^0[35789][0-9]{8}$/.test(form.soDienThoai.trim())) {
    errors.soDienThoai =
        "Số điện thoại phải có 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09";
    isValid = false;
  }

  if (form.diaChi && form.diaChi.length > 255) {
    errors.diaChi = "Địa chỉ tối đa 255 ký tự";
    isValid = false;
  }

  if (!form.vaiTro) {
    errors.vaiTro = "Vui lòng chọn vai trò";
    isValid = false;
  }

  return isValid;
}

async function submitCreateNhanVien() {
  if (!validateForm()) {
    return;
  }

  try {
    const payload = {
      hoTen: form.hoTen.trim(),
      tenDangNhap: form.tenDangNhap.trim(),
      matKhau: form.matKhau.trim(),
      email: form.email.trim(),
      soDienThoai: form.soDienThoai.trim(),
      diaChi: form.diaChi?.trim() || "",
      vaiTro: form.vaiTro,
    };

    await createNhanVien(payload);

    alert("Thêm nhân viên thành công");
    closeCreateForm();

    // Nếu trang có hàm load danh sách nhân viên thì gọi lại ở đây
    // await loadNhanViens();

  } catch (error) {
    const data = error.response?.data;

    if (data) {
      // Trường hợp backend trả lỗi validate dạng:
      // { "email": "Email không đúng định dạng", "soDienThoai": "Số điện thoại không hợp lệ" }
      if (typeof data === "object") {
        Object.keys(data).forEach((key) => {
          errors[key] = data[key];
        });
      }

      // Trường hợp backend trả lỗi dạng:
      // { "message": "Email đã tồn tại" }
      if (data.message) {
        alert(data.message);
      }
    } else {
      alert("Không thể kết nối đến máy chủ");
    }
  }
}
</script>

<style scoped>
.nhan-vien-page {
  padding: 24px;
}

.btn-add {
  background: #198754;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 16px;
}

.modal-box {
  width: 520px;
  max-width: 100%;
  background: white;
  border-radius: 10px;
  padding: 20px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.modal-header h3 {
  margin: 0;
}

.btn-close {
  border: none;
  background: transparent;
  font-size: 28px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 6px;
}

.form-group input,
.form-group select {
  width: 100%;
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 0 10px;
  box-sizing: border-box;
}

.error {
  color: #dc3545;
  font-size: 13px;
  margin: 5px 0 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.btn-cancel {
  border: 1px solid #ccc;
  background: white;
  padding: 9px 14px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-save {
  border: none;
  background: #0d6efd;
  color: white;
  padding: 9px 14px;
  border-radius: 6px;
  cursor: pointer;
}
</style>