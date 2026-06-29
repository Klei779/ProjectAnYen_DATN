<template>
  <div class="container py-4">
    <!-- TIÊU ĐỀ & NÚT THÊM MỚI -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold text-dark mb-0">Quản Lý Giao Diện Nhân Viên</h3>
      <button class="btn btn-primary d-flex align-items-center gap-2 px-3 shadow-sm" @click="openCreateForm">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person-plus"
             viewBox="0 0 16 16">
          <path
              d="M6 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6m2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0m4 8c0 1-1 1-1 1H1s-1 0-1-1 1-4 6-4 6 3 6 4m-1-.004c-.001-.246-.154-.986-.832-1.664C9.516 10.68 8.289 10 6 10s-3.516.68-4.168 1.332c-.678.678-.83 1.418-.832 1.664z"/>
          <path fill-rule="evenodd"
                d="M13.5 5a.5.5 0 0 1 .5.5V7h1.5a.5.5 0 0 1 0 1H14v1.5a.5.5 0 0 1-1 0V8h-1.5a.5.5 0 0 1 0-1H13V5.5a.5.5 0 0 1 .5-.5"/>
        </svg>
        Thêm nhân viên mới
      </button>
    </div>

    <!-- BẢNG DANH SÁCH CHUẨN BOOTSTRAP 5 -->
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
            <td class="fw-medium text-dark">{{ nv.hoTen }}</td>
            <td><code class="text-purple">{{ nv.tenDangNhap }}</code></td>
            <td>
              <div class="small text-dark">{{ nv.email }}</div>
              <div class="small text-muted">{{ nv.soDienThoai }}</div>
            </td>
            <td>
              <span class="badge bg-light text-dark border border-secondary-subtle px-2 py-1.5">{{nhanvienhienthi(nv.vaiTro)}}</span>
            </td>
            <td>
                <span
                    :class="['badge px-2.5 py-1.5 rounded-pill', nv.trangThai == '1' ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger']">
                  {{
                    nv.trangThai == '1'  ? 'Đang làm việc' : 'Đã nghỉ việc'
                  }}
                </span>
            </td>
            <td class="text-end pe-4">
              <button
                  v-if="nv.trangThai == '1' "
                  class="btn btn-sm btn-outline-danger px-3 rounded-2"
                  @click="confirmNghiViec(nv)"
                  :disabled="loadingStates[nv.maNhanVien]"
              >
                <span v-if="loadingStates[nv.maNhanVien]" class="spinner-border spinner-border-sm me-1"></span>
                Cho nghỉ việc
              </button>
              <span v-else class="text-muted small fst-italic">Không khả dụng</span>
            </td>
          </tr>
          <tr v-if="danhSachNhanVien.length === 0">
            <td colspan="7" class="text-center py-4 text-muted">Không tìm thấy dữ liệu nhân viên nào hệ thống.</td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
      <span class="pag-info">
        Hiển thị
        {{ Math.min((currentPage - 1) * pageSize + 1, pagedList.length) }}
        -
        {{ Math.min(currentPage * pageSize, pagedList.length) }}
        của {{ pagedList.length }} nhân viên
      </span>

        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="danhSachNhanVien.length"
            layout="prev, pager, next"
        />
      </div>
    </div>

    <!-- MODAL POPUP (SỬ DỤNG LỚP NỀN BOOTSTRAP) -->
    <div v-if="showCreateForm" class="modal fade show d-block" style="background: rgba(0,0,0,0.55);" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-3">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold text-dark">Tạo Tài Khoản Nhân Viên</h5>
            <button type="button" class="btn-close" @click="closeCreateForm"></button>
          </div>

          <div class="modal-body py-3" style="max-height: 65vh; overflow-y: auto;">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Họ và tên</label>
                <input v-model="form.hoTen" type="text" class="form-control" :class="{'is-invalid': errors.hoTen}"
                       placeholder="VD: Nguyễn Văn A"/>
                <div class="invalid-feedback">{{ errors.hoTen }}</div>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-bold text-secondary">Tên đăng nhập</label>
                <input v-model="form.tenDangNhap" type="text" class="form-control"
                       :class="{'is-invalid': errors.tenDangNhap}"/>
                <div class="invalid-feedback">{{ errors.tenDangNhap }}</div>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-bold text-secondary">Mật khẩu</label>
                <input v-model="form.matKhau" type="password" class="form-control"
                       :class="{'is-invalid': errors.matKhau}"/>
                <div class="invalid-feedback">{{ errors.matKhau }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Địa chỉ Email</label>
                <input v-model="form.email" type="email" class="form-control" :class="{'is-invalid': errors.email}"
                       placeholder="example@anyen.vn"/>
                <div class="invalid-feedback">{{ errors.email }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Số điện thoại di động</label>
                <input v-model="form.soDienThoai" type="text" class="form-control"
                       :class="{'is-invalid': errors.soDienThoai}"/>
                <div class="invalid-feedback">{{ errors.soDienThoai }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Địa chỉ cư trú</label>
                <input v-model="form.diaChi" type="text" class="form-control" :class="{'is-invalid': errors.diaChi}"/>
                <div class="invalid-feedback">{{ errors.diaChi }}</div>
              </div>

              <div class="col-12">
                <label class="form-label small fw-bold text-secondary">Phân quyền Vai trò</label>
                <select v-model="form.vaiTro" class="form-select" :class="{'is-invalid': errors.vaiTro}">
                  <option value="">-- Lựa chọn vai trò hệ thống --</option>
                  <option value="NHAN_VIEN">Nhân viên văn phòng</option>
                  <option value="QUAN_LY">Quản trị viên bộ phận</option>
                  <option value="HOTLINE">Tổng đài viên</option>
                </select>
                <div class="invalid-feedback">{{ errors.vaiTro }}</div>
              </div>
            </div>
          </div>

          <div class="modal-footer border-top-0 pt-0">
            <button type="button" class="btn btn-light px-3" @click="closeCreateForm">Đóng</button>
            <button type="button" class="btn btn-primary px-4 shadow-sm" @click="submitCreateNhanVien"
                    :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
              Lưu thông tin
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import {reactive, ref, onMounted, computed} from "vue";
// 1. CHỈNH SỬA: Import thêm hàm lấy danh sách từ Service JS
import {createNhanVien, nghiViecNhanVien, getAllNhanVien} from "../../services/QuanLyNhanVienService.js";

const showCreateForm = ref(false);
const isSubmitting = ref(false);
const danhSachNhanVien = ref([]);
const loadingStates = reactive({});

const form = reactive({hoTen: "", tenDangNhap: "", matKhau: "", email: "", soDienThoai: "", diaChi: "", vaiTro: ""});
const errors = reactive({});

const currentPage = ref(1);
const pageSize = ref(10);
//1.1 phân trang cho nhân viên
const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSachNhanVien.value.slice(start, start + pageSize.value);
});

// 2. CHỈNH SỬA: Viết hàm gọi API để lấy danh sách nhân viên từ Database
async function fetchDanhSachNhanVien() {
  try {
    const data = await getAllNhanVien();
    danhSachNhanVien.value = data; // Gán dữ liệu trả về từ API vào mảng hiển thị
  } catch (error) {
    console.error("Lỗi khi tải danh sách nhân viên:", error);
    alert("Không thể tải danh sách nhân viên từ hệ thống.");
  }
}

const nhanvienhienthi = (vaiTro) => {
  const role= {
    1:"Quản lý",
    2:"Nhân viên",
    3:"Hotline",
  };
  return role[Number(vaiTro)] ||"không xác định";
};
// 3. CHỈNH SỬA: Gọi hàm fetch khi component vừa được render
onMounted(() => {
  fetchDanhSachNhanVien();
});

function resetForm() {
  Object.keys(form).forEach(key => form[key] = "");
  clearErrors();
}

function clearErrors() {
  Object.keys(errors).forEach(key => delete errors[key]);
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
    errors.tenDangNhap = "Yêu cầu tối thiểu 4 ký tự";
    isValid = false;
  }

  if (!form.matKhau.trim()) {
    errors.matKhau = "Mật khẩu bảo mật không được trống";
    isValid = false;
  } else if (form.matKhau.trim().length < 6) {
    errors.matKhau = "Mật khẩu tối thiểu phải đạt 6 ký tự";
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

  if (form.diaChi && form.diaChi.length > 255) {
    errors.diaChi = "Độ dài địa chỉ không vượt quá 255 ký tự";
    isValid = false;
  }
  if (!form.vaiTro) {
    errors.vaiTro = "Vui lòng chỉ định quyền hạn";
    isValid = false;
  }

  return isValid;
}

async function submitCreateNhanVien() {
  if (!validateForm()) return;
  isSubmitting.value = true;

  try {
    const payload = {...form};
    Object.keys(payload).forEach(key => {
      if (typeof payload[key] === 'string') payload[key] = payload[key].trim();
    });

    await createNhanVien(payload);
    alert("Hệ thống đã ghi nhận thêm mới nhân viên thành công!");

    // 4. CHỈNH SỬA: Thay vì push local, gọi lại hàm fetch để đồng bộ danh sách mới nhất từ DB
    await fetchDanhSachNhanVien();
    closeCreateForm();
  } catch (error) {
    const data = error.response?.data;
    if (data && typeof data === "object") {
      Object.keys(data).forEach(key => errors[key] = data[key]);
      if (data.message) alert(data.message);
    } else {
      alert("Lỗi kết nối máy chủ không ổn định.");
    }
  } finally {
    isSubmitting.value = false;
  }
}

async function confirmNghiViec(nhanVien) {
  if (!confirm(`Hành động nghiêm trọng: Bạn xác nhận muốn thực hiện cho nhân viên [${nhanVien.hoTen}] thôi việc?`)) return;

  const id = nhanVien.maNhanVien;
  loadingStates[id] = true;

  try {
    await nghiViecNhanVien(id);
    alert("Đã cập nhật trạng thái nghỉ việc thành công.");

    // 5. CHỈNH SỬA: Cập nhật trực tiếp trạng thái trên UI local để tối ưu trải nghiệm người dùng
    await fetchDanhSachNhanVien();
  } catch (error) {
    alert(error.response?.data?.message || "Hệ thống gặp sự cố khi xử lý dữ liệu.");
  } finally {
    loadingStates[id] = false;
  }
}
</script>

<style scoped>
/*
  Sử dụng toàn bộ Utility Class của Bootstrap 5 nên phần CSS Scoped gần như trống rỗng,
  giúp tối ưu hóa dung lượng file Build Frontend.
*/
.text-purple {
  color: #6f42c1;
}

.form-select, .form-control {
  border-color: #e2e8f0;
}

.form-select:focus, .form-control:focus {
  border-color: #94a3b8;
  box-shadow: none;
}
</style>