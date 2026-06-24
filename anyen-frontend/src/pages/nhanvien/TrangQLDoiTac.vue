<template>
  <div class="container py-4">
    <!-- TIÊU ĐỀ -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold text-dark mb-0">Quản Lý Đối Tác</h3>
      <button class="btn btn-primary shadow-sm" @click="openModal">
        <i class="bi bi-plus-circle me-1"></i> Thêm Đối Tác Mới
      </button>
    </div>

    <!-- THÔNG BÁO THÀNH CÔNG -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
      <strong>Thành công!</strong> {{ successMessage }}
      <button type="button" class="btn-close" @click="successMessage = ''"></button>
    </div>

    <!-- BẢNG DANH SÁCH (TRỐNG TẠM THỜI DO CHƯA YÊU CẦU LÀM DANH SÁCH) -->
    <div class="card border-0 shadow-sm rounded-3">
      <div class="card-body p-5 text-center text-muted">
        <div style="font-size: 3rem; margin-bottom: 1rem;">🏢</div>
        <h5 class="fw-bold text-secondary">Chưa có dữ liệu danh sách</h5>
        <p class="mb-0">Dữ liệu quản lý đối tác sẽ được hiển thị ở đây.</p>
      </div>
    </div>

    <!-- MODAL THÊM ĐỐI TÁC -->
    <div v-if="showModal" class="modal-backdrop fade show"></div>
    <div class="modal fade" :class="{ 'show d-block': showModal }" tabindex="-1" role="dialog" aria-hidden="true" style="background-color: rgba(0,0,0,0.4);">
      <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
          <div class="modal-header bg-light border-bottom-0 py-3 px-4">
            <h5 class="modal-title fw-bold text-dark">Thêm Đối Tác Mới</h5>
            <button type="button" class="btn-close" aria-label="Close" @click="closeModal"></button>
          </div>
          
          <div class="modal-body p-4">
            <form @submit.prevent="submitCreateDoiTac">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Tên đối tác <span class="text-danger">*</span></label>
                  <input v-model="form.tenDoiTac" type="text" class="form-control" :class="{'is-invalid': errors.tenDoiTac}" placeholder="VD: Công ty An Phúc" />
                  <div class="invalid-feedback">{{ errors.tenDoiTac }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Tên doanh nghiệp</label>
                  <input v-model="form.tenDoanhNghiep" type="text" class="form-control" :class="{'is-invalid': errors.tenDoanhNghiep}" placeholder="Tên pháp lý (nếu có)" />
                  <div class="invalid-feedback">{{ errors.tenDoanhNghiep }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Mã số thuế</label>
                  <input v-model="form.maSoThue" type="text" class="form-control" :class="{'is-invalid': errors.maSoThue}" placeholder="Mã số thuế doanh nghiệp" />
                  <div class="invalid-feedback">{{ errors.maSoThue }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Số điện thoại <span class="text-danger">*</span></label>
                  <input v-model="form.soDienThoai" type="text" class="form-control" :class="{'is-invalid': errors.soDienThoai}" placeholder="SĐT liên hệ" />
                  <div class="invalid-feedback">{{ errors.soDienThoai }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Email <span class="text-danger">*</span></label>
                  <input v-model="form.email" type="email" class="form-control" :class="{'is-invalid': errors.email}" placeholder="example@doitac.com" />
                  <div class="invalid-feedback">{{ errors.email }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Địa chỉ</label>
                  <input v-model="form.diaChi" type="text" class="form-control" :class="{'is-invalid': errors.diaChi}" placeholder="Địa chỉ trụ sở" />
                  <div class="invalid-feedback">{{ errors.diaChi }}</div>
                </div>
                
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Số tài khoản</label>
                  <input v-model="form.soTaiKhoan" type="text" class="form-control" :class="{'is-invalid': errors.soTaiKhoan}" placeholder="Số tài khoản thanh toán" />
                  <div class="invalid-feedback">{{ errors.soTaiKhoan }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold text-secondary">Ngân hàng</label>
                  <input v-model="form.nganHang" type="text" class="form-control" :class="{'is-invalid': errors.nganHang}" placeholder="Tên ngân hàng" />
                  <div class="invalid-feedback">{{ errors.nganHang }}</div>
                </div>
              </div>
            </form>
          </div>
          
          <div class="modal-footer bg-light border-top-0 py-3 px-4">
            <button type="button" class="btn btn-outline-secondary px-4 fw-bold" @click="closeModal">Hủy</button>
            <button type="button" class="btn btn-primary px-4 fw-bold shadow-sm" :disabled="isSubmitting" @click="submitCreateDoiTac">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
              <span v-else>✅</span> Gửi lời mời hợp tác
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { createDoiTac } from "../../services/QuanLyDoiTacService.js";

const showModal = ref(false);
const isSubmitting = ref(false);
const successMessage = ref("");

const form = reactive({
  tenDoiTac: "",
  tenDoanhNghiep: "",
  maSoThue: "",
  soTaiKhoan: "",
  nganHang: "",
  email: "",
  soDienThoai: "",
  diaChi: ""
});

const errors = reactive({});

function clearErrors() {
  Object.keys(errors).forEach(key => delete errors[key]);
}

function resetForm() {
  Object.keys(form).forEach(key => form[key] = "");
  clearErrors();
}

function openModal() {
  resetForm();
  successMessage.value = "";
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

function validateForm() {
  clearErrors();
  let isValid = true;

  if (!form.tenDoiTac.trim()) { errors.tenDoiTac = "Tên đối tác không được để trống"; isValid = false; }
  
  if (!form.email.trim()) { errors.email = "Địa chỉ Email không được để trống"; isValid = false; }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) { errors.email = "Định dạng Email không chính xác"; isValid = false; }

  if (!form.soDienThoai.trim()) { errors.soDienThoai = "Số điện thoại không được trống"; isValid = false; }
  else if (!/^0[35789][0-9]{8}$/.test(form.soDienThoai.trim())) { errors.soDienThoai = "Số điện thoại không đúng định dạng nhà mạng Việt Nam"; isValid = false; }

  return isValid;
}

async function submitCreateDoiTac() {
  if (!validateForm()) return;
  isSubmitting.value = true;
  successMessage.value = "";

  try {
    const payload = { ...form };
    Object.keys(payload).forEach(key => { if(typeof payload[key] === 'string') payload[key] = payload[key].trim(); });

    await createDoiTac(payload);
    
    successMessage.value = "Hệ thống đã ghi nhận thêm mới đối tác và gửi email thư mời hợp tác thành công!";
    closeModal();
    
  } catch (error) {
    const data = error.response?.data;
    if (data && typeof data === "object") {
      // Nếu là object chứa thông báo lỗi Validation của Spring
      if (data.message) {
        alert(data.message);
      } else {
        // Gắn lỗi validation vào form
        Object.keys(data).forEach(key => errors[key] = data[key]);
        if (Object.keys(errors).length === 0) {
           alert("Hệ thống xử lý gặp sự cố, vui lòng thử lại.");
        }
      }
    } else if (typeof data === 'string' && data.trim() !== '') {
      alert(data);
    } else {
      alert("Lỗi kết nối máy chủ không ổn định. Vui lòng kiểm tra lại.");
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.form-control { border-color: #e2e8f0; padding: 0.6rem 0.75rem; }
.form-control:focus { border-color: #2563eb; box-shadow: 0 0 0 0.25rem rgba(37, 99, 235, 0.1); }
.modal-backdrop { opacity: 0.5; }
</style>