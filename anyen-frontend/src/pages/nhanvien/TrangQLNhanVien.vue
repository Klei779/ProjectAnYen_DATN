<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";

import {
  createNhanVien,
  getAllNhanVien,
  nghiViecNhanVien,
  updateNhanVien,
    khoaTaiKhoanNhanVien,
  moKhoaTaiKhoanNhanVien,
} from "../../services/QuanLyNhanVienService.js";

/* =========================
   BỘ LỌC
========================= */
const keyword = ref("");
const statusFilter = ref("all");
const roleFilter = ref("all");

/* =========================
   TRẠNG THÁI GIAO DIỆN
========================= */
const showEmployeeForm = ref(false);
const isEditing = ref(false);
const editingId = ref(null);
const isSubmitting = ref(false);
const loading = ref(false);

/* =========================
   DỮ LIỆU NHÂN VIÊN
========================= */
const danhSachNhanVien = ref([]);
const loadingStates = reactive({});

/* =========================
   PHÂN TRANG
========================= */
const currentPage = ref(1);
const pageSize = ref(10);

/* =========================
   FORM NHÂN VIÊN
========================= */
const form = reactive({
  hoTen: "",
  tenDangNhap: "",
  matKhau: "",
  email: "",
  soDienThoai: "",

  // Các trường địa chỉ mới
  soNhaDuong: "",
  phuongXa: "",
  quanHuyen: "",
  tinhThanh: "",

  vaiTro: "",
});

const errors = reactive({});

/* =========================
   QUYỀN NGƯỜI DÙNG
========================= */
function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem("user") || "null");
  } catch (error) {
    console.error("Không đọc được thông tin đăng nhập:", error);
    return null;
  }
}

const currentUser = ref(getCurrentUser());

const isAdmin = computed(() => {
  const role =
      currentUser.value?.vaiTroChiTiet ||
      currentUser.value?.role;

  return (
      role === "ADMIN" ||
      Number(currentUser.value?.vaiTro) === 1
  );
});

/* =========================
   HÀM HIỂN THỊ
========================= */
function getAvatar(name) {
  if (!name) {
    return "NV";
  }

  const words = String(name)
      .trim()
      .split(/\s+/)
      .filter(Boolean);

  if (words.length === 1) {
    return words[0]
        .substring(0, 2)
        .toUpperCase();
  }

  return `${words[0][0]}${words[words.length - 1][0]}`
      .toUpperCase();
}

function getEmployeeStatus(employee) {
  if (Number(employee?.trangThai) === 0) {
    return employee?.tenTrangThai || "Đã nghỉ việc";
  }

  return employee?.tenTrangThai || "Đang làm việc";
}

function statusClass(employee) {
  return Number(employee?.trangThai) === 0
      ? "red"
      : "green";
}

function roleClass(employee) {
  const role = Number(employee?.vaiTro);

  if (role === 1) {
    return "purple";
  }

  if (role === 3) {
    return "orange";
  }

  return "blue";
}

function roleName(employee) {
  if (employee?.tenVaiTro) {
    return employee.tenVaiTro;
  }

  const role = Number(employee?.vaiTro);

  if (role === 1) {
    return "Quản lý/Admin";
  }

  if (role === 3) {
    return "Hotline";
  }

  return "Nhân viên";
}

/**
 * Ghép 4 trường địa chỉ thành một chuỗi để hiển thị.
 */
function formatAddress(employee) {
  const addressParts = [
    employee?.soNhaDuong,
    employee?.phuongXa,
    employee?.quanHuyen,
    employee?.tinhThanh,
  ]
      .map((value) => String(value ?? "").trim())
      .filter(Boolean);

  return addressParts.length
      ? addressParts.join(", ")
      : "Chưa cập nhật địa chỉ";
}

/* =========================
   LỌC VÀ TÌM KIẾM
========================= */
const filteredEmployees = computed(() => {
  const searchValue = keyword.value
      .trim()
      .toLowerCase();

  return danhSachNhanVien.value.filter((employee) => {
    const searchableValues = [
      employee.maNhanVien,
      employee.hoTen,
      employee.tenDangNhap,
      employee.email,
      employee.soDienThoai,

      // Tìm kiếm theo địa chỉ mới
      employee.soNhaDuong,
      employee.phuongXa,
      employee.quanHuyen,
      employee.tinhThanh,

      employee.tenVaiTro,
      employee.tenTrangThai,
    ]
        .map((value) =>
            String(value ?? "").toLowerCase()
        )
        .join(" ");

    const matchKeyword =
        !searchValue ||
        searchableValues.includes(searchValue);

    const matchStatus =
        statusFilter.value === "all" ||
        (
            statusFilter.value === "active" &&
            Number(employee.trangThai) === 1
        ) ||
        (
            statusFilter.value === "inactive" &&
            Number(employee.trangThai) === 0
        );

    const matchRole =
        roleFilter.value === "all" ||
        Number(employee.vaiTro) ===
        Number(roleFilter.value);

    return (
        matchKeyword &&
        matchStatus &&
        matchRole
    );
  });
});

/* =========================
   PHÂN TRANG
========================= */
const totalPages = computed(() => {
  return Math.max(
      Math.ceil(
          filteredEmployees.value.length /
          pageSize.value
      ),
      1
  );
});

const pagedList = computed(() => {
  const start =
      (currentPage.value - 1) *
      pageSize.value;

  return filteredEmployees.value.slice(
      start,
      start + pageSize.value
  );
});

const displayFrom = computed(() => {
  if (!filteredEmployees.value.length) {
    return 0;
  }

  return (
      (currentPage.value - 1) *
      pageSize.value +
      1
  );
});

const displayTo = computed(() => {
  return Math.min(
      currentPage.value * pageSize.value,
      filteredEmployees.value.length
  );
});

const visiblePages = computed(() => {
  const total = totalPages.value;

  if (total <= 5) {
    return Array.from(
        { length: total },
        (_, index) => index + 1
    );
  }

  if (currentPage.value <= 3) {
    return [1, 2, 3, "...", total];
  }

  if (currentPage.value >= total - 2) {
    return [
      1,
      "...",
      total - 2,
      total - 1,
      total,
    ];
  }

  return [
    1,
    "...",
    currentPage.value,
    "...",
    total,
  ];
});

/* =========================
   THỐNG KÊ
========================= */
const activeEmployees = computed(() => {
  return danhSachNhanVien.value.filter(
      (employee) =>
          Number(employee.trangThai) === 1
  ).length;
});

const inactiveEmployees = computed(() => {
  return danhSachNhanVien.value.filter(
      (employee) =>
          Number(employee.trangThai) === 0
  ).length;
});

/* =========================
   WATCH
========================= */
watch(
    [keyword, statusFilter, roleFilter],
    () => {
      currentPage.value = 1;
    }
);

watch(totalPages, (newTotal) => {
  if (currentPage.value > newTotal) {
    currentPage.value = newTotal;
  }
});

/* =========================
   API DANH SÁCH NHÂN VIÊN
========================= */
async function fetchDanhSachNhanVien() {
  try {
    loading.value = true;

    const response =
        await getAllNhanVien();

    if (Array.isArray(response)) {
      danhSachNhanVien.value = response;
    } else if (
        Array.isArray(response?.content)
    ) {
      danhSachNhanVien.value =
          response.content;
    } else if (
        Array.isArray(response?.data)
    ) {
      danhSachNhanVien.value =
          response.data;
    } else {
      danhSachNhanVien.value = [];
    }
  } catch (error) {
    console.error(
        "Lỗi khi tải danh sách nhân viên:",
        error
    );

    danhSachNhanVien.value = [];

    alert(
        error.response?.data?.message ||
        "Không thể tải danh sách nhân viên từ hệ thống."
    );
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (isAdmin.value) {
    fetchDanhSachNhanVien();
  }
});

/* =========================
   RESET FORM
========================= */
function clearErrors() {
  Object.keys(errors).forEach((key) => {
    delete errors[key];
  });
}

function resetForm() {
  Object.assign(form, {
    hoTen: "",
    tenDangNhap: "",
    matKhau: "",
    email: "",
    soDienThoai: "",

    soNhaDuong: "",
    phuongXa: "",
    quanHuyen: "",
    tinhThanh: "",

    vaiTro: "",
  });

  clearErrors();
}

/* =========================
   MỞ/ĐÓNG FORM
========================= */
function openCreateForm() {
  if (!isAdmin.value) {
    return;
  }

  resetForm();

  isEditing.value = false;
  editingId.value = null;
  showEmployeeForm.value = true;
}

function openEditForm(employee) {
  if (!isAdmin.value) {
    return;
  }

  clearErrors();

  isEditing.value = true;
  editingId.value =
      employee.maNhanVien;

  Object.assign(form, {
    hoTen: employee.hoTen || "",
    tenDangNhap:
        employee.tenDangNhap || "",
    matKhau: "",
    email: employee.email || "",
    soDienThoai:
        employee.soDienThoai || "",

    // Lấy dữ liệu địa chỉ mới khi sửa
    soNhaDuong:
        employee.soNhaDuong || "",
    phuongXa:
        employee.phuongXa || "",
    quanHuyen:
        employee.quanHuyen || "",
    tinhThanh:
        employee.tinhThanh || "",

    vaiTro: String(
        employee.vaiTro ?? ""
    ),
  });

  showEmployeeForm.value = true;
}

function closeEmployeeForm() {
  if (isSubmitting.value) {
    return;
  }

  showEmployeeForm.value = false;
  isEditing.value = false;
  editingId.value = null;

  resetForm();
}

/* =========================
   VALIDATE FORM
========================= */
function validateForm() {
  clearErrors();

  let isValid = true;

  const hoTen =
      form.hoTen.trim();

  const tenDangNhap =
      form.tenDangNhap.trim();

  const matKhau =
      form.matKhau.trim();

  const email =
      form.email.trim();

  const soDienThoai =
      form.soDienThoai.trim();

  const soNhaDuong =
      form.soNhaDuong.trim();

  const phuongXa =
      form.phuongXa.trim();

  const quanHuyen =
      form.quanHuyen.trim();

  const tinhThanh =
      form.tinhThanh.trim();

  if (!hoTen) {
    errors.hoTen =
        "Họ tên không được để trống";

    isValid = false;
  } else if (hoTen.length > 50) {
    errors.hoTen =
        "Họ tên tối đa 50 ký tự";

    isValid = false;
  }

  if (!tenDangNhap) {
    errors.tenDangNhap =
        "Tên đăng nhập không được để trống";

    isValid = false;
  } else if (
      tenDangNhap.length < 4 ||
      tenDangNhap.length > 50
  ) {
    errors.tenDangNhap =
        "Tên đăng nhập phải từ 4 đến 50 ký tự";

    isValid = false;
  }

  if (
      !isEditing.value &&
      !matKhau
  ) {
    errors.matKhau =
        "Mật khẩu không được để trống";

    isValid = false;
  } else if (
      matKhau &&
      matKhau.length < 6
  ) {
    errors.matKhau =
        "Mật khẩu phải có ít nhất 6 ký tự";

    isValid = false;
  }

  if (!email) {
    errors.email =
        "Email không được để trống";

    isValid = false;
  } else if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
          email
      )
  ) {
    errors.email =
        "Email không đúng định dạng";

    isValid = false;
  }

  if (!soDienThoai) {
    errors.soDienThoai =
        "Số điện thoại không được để trống";

    isValid = false;
  } else if (
      !/^0[35789][0-9]{8}$/.test(
          soDienThoai
      )
  ) {
    errors.soDienThoai =
        "Số điện thoại không đúng định dạng Việt Nam";

    isValid = false;
  }

  /*
   * Địa chỉ không bắt buộc.
   * Chỉ kiểm tra độ dài khi người dùng có nhập.
   */
  if (soNhaDuong.length > 255) {
    errors.soNhaDuong =
        "Số nhà, tên đường tối đa 255 ký tự";

    isValid = false;
  }

  if (phuongXa.length > 100) {
    errors.phuongXa =
        "Phường/Xã tối đa 100 ký tự";

    isValid = false;
  }

  if (quanHuyen.length > 100) {
    errors.quanHuyen =
        "Quận/Huyện tối đa 100 ký tự";

    isValid = false;
  }

  if (tinhThanh.length > 100) {
    errors.tinhThanh =
        "Tỉnh/Thành phố tối đa 100 ký tự";

    isValid = false;
  }

  if (
      !["1", "2", "3"].includes(
          String(form.vaiTro)
      )
  ) {
    errors.vaiTro =
        "Vui lòng chọn vai trò hợp lệ";

    isValid = false;
  }

  return isValid;
}

/* =========================
   TẠO PAYLOAD
========================= */
function buildPayload() {
  const payload = {
    hoTen:
        form.hoTen.trim(),

    tenDangNhap:
        form.tenDangNhap.trim(),

    email:
        form.email.trim(),

    soDienThoai:
        form.soDienThoai.trim(),

    // Gửi đúng 4 trường địa chỉ sang backend
    soNhaDuong:
        form.soNhaDuong.trim(),

    phuongXa:
        form.phuongXa.trim(),

    quanHuyen:
        form.quanHuyen.trim(),

    tinhThanh:
        form.tinhThanh.trim(),

    vaiTro:
        Number(form.vaiTro),
  };

  const newPassword =
      form.matKhau.trim();

  if (
      !isEditing.value ||
      newPassword
  ) {
    payload.matKhau =
        newPassword;
  }

  return payload;
}

/* =========================
   XỬ LÝ LỖI BACKEND
========================= */
function applyServerErrors(error) {
  const data =
      error.response?.data;

  if (
      data &&
      typeof data === "object"
  ) {
    Object.entries(data).forEach(
        ([key, value]) => {
          if (key !== "message") {
            errors[key] =
                Array.isArray(value)
                    ? value.join(", ")
                    : String(value);
          }
        }
    );

    if (data.message) {
      alert(data.message);
    }

    return;
  }

  alert(
      "Không thể kết nối tới máy chủ."
  );
}

/* =========================
   THÊM/CẬP NHẬT NHÂN VIÊN
========================= */
async function submitEmployeeForm() {
  if (
      !isAdmin.value ||
      !validateForm()
  ) {
    return;
  }

  isSubmitting.value = true;

  try {
    const payload =
        buildPayload();

    if (isEditing.value) {
      await updateNhanVien(
          editingId.value,
          payload
      );

      alert(
          "Cập nhật thông tin nhân viên thành công."
      );
    } else {
      await createNhanVien(payload);

      alert(
          "Thêm nhân viên thành công."
      );
    }

    await fetchDanhSachNhanVien();

    showEmployeeForm.value = false;
    isEditing.value = false;
    editingId.value = null;

    resetForm();
  } catch (error) {
    console.error(
        "Lỗi lưu nhân viên:",
        error
    );

    applyServerErrors(error);
  } finally {
    isSubmitting.value = false;
  }
}

/* =========================
   CHO NHÂN VIÊN NGHỈ VIỆC
========================= */
async function confirmNghiViec(employee) {
  if (!isAdmin.value) {
    return;
  }

  const accepted = confirm(
      `Bạn xác nhận cho nhân viên [${employee.hoTen}] nghỉ việc?`
  );

  if (!accepted) {
    return;
  }

  const id =
      employee.maNhanVien;

  loadingStates[id] = true;

  try {
    await nghiViecNhanVien(id);

    alert(
        "Đã cập nhật trạng thái nghỉ việc thành công."
    );

    await fetchDanhSachNhanVien();
  } catch (error) {
    console.error(
        "Lỗi cập nhật nghỉ việc:",
        error
    );

    alert(
        error.response?.data?.message ||
        "Không thể cập nhật trạng thái nhân viên."
    );
  } finally {
    loadingStates[id] = false;
  }
}
async function confirmKhoaTaiKhoan(employee) {
  if (!isAdmin.value) {
    return;
  }

  const accepted = confirm(
      `Bạn xác nhận khóa tài khoản nhân viên [${employee.hoTen}]?`
  );

  if (!accepted) {
    return;
  }

  const id =
      employee.maNhanVien;

  loadingStates[id] = true;

  try {
    await khoaTaiKhoanNhanVien(id);

    alert(
        "Đã cập nhật trạng thái khóa thành công."
    );

    await fetchDanhSachNhanVien();
  } catch (error) {
    console.error(
        "Lỗi cập nhật khóa tài khoản:",
        error
    );

    alert(
        error.response?.data?.message ||
        "Không thể cập nhật trạng thái nhân viên."
    );
  } finally {
    loadingStates[id] = false;
  }
}
async function confirmMoKhoaTaiKhoan(employee) {
  if (!isAdmin.value) {
    return;
  }

  const accepted = confirm(
      `Bạn xác nhận mở khóa tài khoản nhân viên [${employee.hoTen}]?`
  );

  if (!accepted) {
    return;
  }

  const id =
      employee.maNhanVien;

  loadingStates[id] = true;

  try {
    await moKhoaTaiKhoanNhanVien(id);

    alert(
        "Đã cập nhật trạng thái mở khóa thành công."
    );

    await fetchDanhSachNhanVien();
  } catch (error) {
    console.error(
        "Lỗi cập nhật khóa tài khoản:",
        error
    );

    alert(
        error.response?.data?.message ||
        "Không thể cập nhật trạng thái nhân viên."
    );
  } finally {
    loadingStates[id] = false;
  }
}

/* =========================
   CHUYỂN TRANG
========================= */
function changePage(page) {
  if (page === "...") {
    return;
  }

  if (
      page < 1 ||
      page > totalPages.value
  ) {
    return;
  }

  currentPage.value = page;
}
</script>

<template>
  <div class="employee-management">
    <section class="page-content">
      <!-- TIÊU ĐỀ -->
      <div class="page-heading">
        <div>
          <p class="eyebrow">
            QUẢN TRỊ NHÂN SỰ
          </p>

          <h2>Quản lý nhân viên</h2>

          <p class="heading-description">
            Theo dõi tài khoản, vai trò, địa chỉ và trạng thái làm việc của nhân viên An Yên.
          </p>
        </div>

        <div class="heading-statistics">
          <div class="heading-stat">
            <strong>
              {{ danhSachNhanVien.length }}
            </strong>

            <span>
              Tổng nhân viên
            </span>
          </div>

          <div class="heading-stat">
            <strong>
              {{ activeEmployees }}
            </strong>

            <span>
              Đang làm việc
            </span>
          </div>

          <div class="heading-stat">
            <strong>
              {{ inactiveEmployees }}
            </strong>

            <span>
              Đã nghỉ việc
            </span>
          </div>
        </div>
      </div>

      <!-- CẢNH BÁO QUYỀN -->
      <div
          v-if="!isAdmin"
          class="permission-warning"
      >
        <i
            class="fa-solid fa-triangle-exclamation"
        ></i>

        <div>
          <strong>
            Bạn không có quyền quản lý nhân viên
          </strong>

          <p>
            Chỉ tài khoản Quản lý/Admin An Yên mới có thể thêm, sửa hoặc cho nhân viên nghỉ việc.
          </p>
        </div>
      </div>

      <!-- DANH SÁCH -->
      <div class="card">
        <!-- BỘ LỌC -->
        <div class="filter-row">
          <div class="search-box">
            <span class="search-label">
              Tìm
            </span>

            <input
                v-model="keyword"
                type="text"
                placeholder="Tìm tên, tài khoản, email, số điện thoại, địa chỉ..."
            />
          </div>

          <select
              v-model="statusFilter"
              aria-label="Lọc trạng thái"
          >
            <option value="all">
              Tất cả trạng thái
            </option>

            <option value="active">
              Đang làm việc
            </option>

            <option value="inactive">
              Đã nghỉ việc
            </option>
          </select>

          <select
              v-model="roleFilter"
              aria-label="Lọc vai trò"
          >
            <option value="all">
              Tất cả vai trò
            </option>

            <option value="1">
              Quản lý/Admin
            </option>

            <option value="2">
              Nhân viên
            </option>

            <option value="3">
              Hotline
            </option>
          </select>

          <button
              class="filter-btn"
              type="button"
              :disabled="loading || !isAdmin"
              @click="fetchDanhSachNhanVien"
          >
            <i
                class="fa-solid"
                :class="
                loading
                  ? 'fa-spinner fa-spin'
                  : 'fa-rotate-right'
              "
            ></i>

            Tải lại
          </button>

          <button
              v-if="isAdmin"
              class="add-btn"
              type="button"
              @click="openCreateForm"
          >
            <i
                class="fa-solid fa-user-plus"
            ></i>

            Thêm nhân viên
          </button>
        </div>

        <!-- LOADING -->
        <div
            v-if="loading"
            class="table-state"
        >
          <i
              class="fa-solid fa-spinner fa-spin"
          ></i>

          Đang tải danh sách nhân viên...
        </div>

        <!-- BẢNG -->
        <div
            v-else
            class="table-wrapper"
        >
          <table class="employee-table">
            <thead>
            <tr>
              <th>Nhân viên</th>
              <th>Tài khoản</th>
              <th>Liên hệ</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Địa chỉ</th>

              <th class="text-center">
                Cập nhật nhân viên
              </th>

              <th class="text-center">
                Hành động
              </th>
              <th class="text-center">
              Khóa tài khoàn
              </th>
            </tr>
            </thead>

            <tbody>
            <tr
                v-for="employee in pagedList"
                :key="employee.maNhanVien"
            >
              <!-- NHÂN VIÊN -->
              <td data-label="Nhân viên">
                <div class="employee-cell">
                  <div class="avatar">
                    {{
                      getAvatar(
                          employee.hoTen
                      )
                    }}
                  </div>

                  <div class="employee-main-info">
                    <strong>
                      {{
                        employee.hoTen ||
                        "Chưa cập nhật"
                      }}
                    </strong>

                    <p>
                      #NV{{
                        String(
                            employee.maNhanVien
                        ).padStart(4, "0")
                      }}
                    </p>
                  </div>
                </div>
              </td>

              <!-- TÀI KHOẢN -->
              <td data-label="Tài khoản">
                <strong class="account-name">
                  {{
                    employee.tenDangNhap ||
                    "---"
                  }}
                </strong>
              </td>

              <!-- LIÊN HỆ -->
              <td data-label="Liên hệ">
                <div class="contact-info">
                    <span>
                      {{
                        employee.email ||
                        "---"
                      }}
                    </span>

                  <small>
                    {{
                      employee.soDienThoai ||
                      "---"
                    }}
                  </small>
                </div>
              </td>

              <!-- VAI TRÒ -->
              <td data-label="Vai trò">
                  <span
                      class="badge"
                      :class="
                      roleClass(employee)
                    "
                  >
                    {{
                      roleName(employee)
                    }}
                  </span>
              </td>

              <!-- TRẠNG THÁI -->
              <td data-label="Trạng thái">
                  <span
                      class="badge status-badge"
                      :class="
                      statusClass(employee)
                    "
                  >
                    <span
                        class="status-dot"
                        :class="
                        statusClass(employee)
                      "
                    ></span>

                    {{
                      getEmployeeStatus(
                          employee
                      )
                    }}
                  </span>
              </td>

              <!-- ĐỊA CHỈ -->
              <td data-label="Địa chỉ">
                  <span
                      class="address-text"
                      :title="
                      formatAddress(employee)
                    "
                  >
                    {{
                      formatAddress(employee)
                    }}
                  </span>
              </td>

              <!-- SỬA -->
              <td
                  data-label="Cập nhật nhân viên"
                  class="text-center"
              >
                <button
                    v-if="isAdmin &&
                      Number(employee.trangThai) === 1 && Number(employee.vaiTro) !== 1 "
                    class="edit-btn"
                    type="button"
                    @click="
                      openEditForm(employee)
                    "
                >
                  <i
                      class="fa-solid fa-pen-to-square"
                  ></i>

                  Sửa
                </button>

                <span
                    v-else-if="Number(employee.trangThai) === 0"
                    class="muted-action"
                >
Nhân viên đã nghỉ việc
                  </span>

                <span
                    v-else
                    class="muted-action"
                >
Không có quyền
                  </span>
              </td>

              <!-- NGHỈ VIỆC -->
              <td
                  data-label="Hành động"
                  class="text-center"
              >
                <button
                    v-if="
                      isAdmin &&
                      Number(employee.trangThai) === 1 &&
                      Number(employee.vaiTro) !== 1 ||   Number(employee.trangThai) === 2
                    "
                    class="danger-btn"
                    type="button"
                    :disabled="
                      loadingStates[
                        employee.maNhanVien
                      ]
                    "
                    @click="
                      confirmNghiViec(
                        employee
                      )
                    "
                >
                  <i
                      class="fa-solid"
                      :class="
                        loadingStates[
                          employee.maNhanVien
                        ]
                          ? 'fa-spinner fa-spin'
                          : 'fa-user-slash'
                      "
                  ></i>

                  Cho nghỉ việc
                </button>

                <span
                    v-else-if="
                      Number(employee.vaiTro) === 1
                    "
                    class="muted-action"
                >
                    Không có quyền
                  </span>

                <span
                    v-else-if="
                      Number(employee.trangThai) === 0
                    "
                    class="muted-action"
                >
                    Đã nghỉ việc
                  </span>

                <span
                    v-else
                    class="muted-action"
                >
                    Không có quyền
                  </span>
              </td>
              <td
                                data-label="Hành động"
                                class="text-center"
                            >
                            <button
                               v-if="
                                 isAdmin &&
                                 Number(employee.trangThai) === 1 &&
                                 Number(employee.vaiTro) !== 1
                               "
                               class="warning-btn"
                               type="button"
                               :disabled="loadingStates[employee.maNhanVien]"
                               @click="confirmKhoaTaiKhoan(employee)"
                             >
                               <i
                                 class="fa-solid"
                                 :class="
                                   loadingStates[employee.maNhanVien]
                                     ? 'fa-spinner fa-spin'
                                     : 'fa-user-slash'
                                 "
                               ></i>

                               Khóa tài khoản
                             </button>

                             <!-- ==================== MỞ KHÓA ==================== -->
                             <button
                               v-else-if="
                                 isAdmin &&
                                 Number(employee.trangThai) === 2 &&
                                 Number(employee.vaiTro) !== 1 &&
                                 Number(employee.vaiTro) !== 0
                               "
                               class="success-btn"
                               type="button"
                               :disabled="loadingStates[employee.maNhanVien]"
                               @click="confirmMoKhoaTaiKhoan(employee)"
                             >
                               <i
                                 class="fa-solid"
                                 :class="
                                   loadingStates[employee.maNhanVien]
                                     ? 'fa-spinner fa-spin'
                                     : 'fa-user-check'
                                 "
                               ></i>

                               Mở khóa
                             </button>

                             <span
                               v-else-if="Number(employee.vaiTro) === 1"
                               class="muted-action"
                             >
                               Không có quyền
                             </span>

                             <span
                               v-else
                               class="muted-action"
                             >
                               Không có quyền
                             </span>
                            </td>
            </tr>

            <!-- KHÔNG CÓ DỮ LIỆU -->
            <tr v-if="!pagedList.length">
              <td colspan="8">
                <div class="table-state empty">
                  <i
                      class="fa-regular fa-folder-open"
                  ></i>

                  Không có nhân viên phù hợp
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- PHÂN TRANG -->
        <div class="pagination-row">
          <p>
            Hiển thị {{ displayFrom }} -
            {{ displayTo }} của
            {{ filteredEmployees.length }}
            nhân viên
          </p>

          <div class="pagination">
            <button
                type="button"
                :disabled="
                currentPage === 1
              "
                @click="
                changePage(
                  currentPage - 1
                )
              "
            >
              &lt;
            </button>

            <button
                v-for="(
                page,
                index
              ) in visiblePages"
                :key="`${page}-${index}`"
                type="button"
                :class="{
                active:
                  page === currentPage,
                dots:
                  page === '...',
              }"
                :disabled="page === '...'"
                @click="changePage(page)"
            >
              {{ page }}
            </button>

            <button
                type="button"
                :disabled="
                currentPage ===
                totalPages
              "
                @click="
                changePage(
                  currentPage + 1
                )
              "
            >
              &gt;
            </button>
          </div>
        </div>
      </div>

      <!-- CHÚ THÍCH -->
      <div class="legend">
        <div>
          <h5>Chú thích trạng thái</h5>

          <div class="status-list">
            <span>
              <span
                  class="dot green"
              ></span>

              Đang làm việc
            </span>

            <span>
              <span
                  class="dot red"
              ></span>

              Đã nghỉ việc
            </span>
          </div>
        </div>

        <div>
          <h5>
            Phân quyền nhân viên
          </h5>

          <p>
            Quản lý/Admin có toàn quyền quản trị. Nhân viên xử lý nghiệp vụ nội bộ. Hotline phụ trách tư vấn và chăm sóc khách hàng.
          </p>
        </div>
      </div>
    </section>

    <!-- MODAL THÊM/SỬA NHÂN VIÊN -->
    <div
        v-if="showEmployeeForm"
        class="modal-overlay"
        @click.self="closeEmployeeForm"
    >
      <div class="employee-modal">
        <!-- HEADER MODAL -->
        <div class="modal-header">
          <div>
            <p class="modal-eyebrow">
              {{
                isEditing
                    ? "CẬP NHẬT NHÂN VIÊN"
                    : "THÊM NHÂN VIÊN MỚI"
              }}
            </p>

            <h3>
              {{
                isEditing
                    ? "Sửa thông tin nhân viên"
                    : "Tạo tài khoản nhân viên"
              }}
            </h3>

            <span v-if="isEditing">
              Mã nhân viên:
              #NV{{
                String(
                    editingId
                ).padStart(4, "0")
              }}
            </span>
          </div>

          <button
              class="modal-close"
              type="button"
              :disabled="isSubmitting"
              aria-label="Đóng"
              @click="closeEmployeeForm"
          >
            <i
                class="fa-solid fa-xmark"
            ></i>
          </button>
        </div>

        <!-- BODY MODAL -->
        <div class="modal-body">
          <div class="form-grid">
            <!-- HỌ TÊN -->
            <div class="form-group full-width">
              <label>
                Họ và tên
                <span>*</span>
              </label>

              <input
                  v-model="form.hoTen"
                  type="text"
                  maxlength="50"
                  :class="{
                  invalid:
                    errors.hoTen,
                }"
                  placeholder="Ví dụ: Nguyễn Văn A"
              />

              <small
                  v-if="errors.hoTen"
                  class="error-text"
              >
                {{ errors.hoTen }}
              </small>
            </div>

            <!-- TÊN ĐĂNG NHẬP -->
            <div class="form-group">
              <label>
                Tên đăng nhập
                <span>*</span>
              </label>

              <input
                  v-model="
                  form.tenDangNhap
                "
                  type="text"
                  maxlength="50"
                  autocomplete="off"
                  :class="{
                  invalid:
                    errors.tenDangNhap,
                }"
                  placeholder="Nhập tên đăng nhập"
              />

              <small
                  v-if="
                  errors.tenDangNhap
                "
                  class="error-text"
              >
                {{
                  errors.tenDangNhap
                }}
              </small>
            </div>

            <!-- MẬT KHẨU -->
            <div
                v-if="!isEditing"
                class="form-group"
            >
              <label>
                Mật khẩu
                <span>*</span>
              </label>

              <input
                  v-model="form.matKhau"
                  type="password"
                  maxlength="100"
                  autocomplete="new-password"
                  :class="{
                  invalid:
                    errors.matKhau,
                }"
                  placeholder="Tối thiểu 6 ký tự"
              />

              <small
                  v-if="errors.matKhau"
                  class="error-text"
              >
                {{ errors.matKhau }}
              </small>
            </div>

            <!-- EMAIL -->
            <div class="form-group">
              <label>
                Email
                <span>*</span>
              </label>

              <input
                  v-model="form.email"
                  type="email"
                  maxlength="100"
                  :class="{
                  invalid:
                    errors.email,
                }"
                  placeholder="example@anyen.vn"
              />

              <small
                  v-if="errors.email"
                  class="error-text"
              >
                {{ errors.email }}
              </small>
            </div>

            <!-- SỐ ĐIỆN THOẠI -->
            <div class="form-group">
              <label>
                Số điện thoại
                <span>*</span>
              </label>

              <input
                  v-model="
                  form.soDienThoai
                "
                  type="text"
                  maxlength="10"
                  :class="{
                  invalid:
                    errors.soDienThoai,
                }"
                  placeholder="Ví dụ: 0912345678"
              />

              <small
                  v-if="
                  errors.soDienThoai
                "
                  class="error-text"
              >
                {{
                  errors.soDienThoai
                }}
              </small>
            </div>

            <!-- SỐ NHÀ, ĐƯỜNG -->
            <div class="form-group full-width">
              <label>
                Số nhà, tên đường
              </label>

              <input
                  v-model="
                  form.soNhaDuong
                "
                  type="text"
                  maxlength="255"
                  :class="{
                  invalid:
                    errors.soNhaDuong,
                }"
                  placeholder="Ví dụ: 123 Nguyễn Văn Linh"
              />

              <div class="field-footer">
                <small
                    v-if="
                    errors.soNhaDuong
                  "
                    class="error-text"
                >
                  {{
                    errors.soNhaDuong
                  }}
                </small>

                <small
                    v-else
                    class="helper-text"
                >
                  {{
                    form.soNhaDuong
                        .length
                  }}/255 ký tự
                </small>
              </div>
            </div>

            <!-- PHƯỜNG XÃ -->
            <div class="form-group">
              <label>
                Phường/Xã
              </label>

              <input
                  v-model="form.phuongXa"
                  type="text"
                  maxlength="100"
                  :class="{
                  invalid:
                    errors.phuongXa,
                }"
                  placeholder="Ví dụ: Phường Tân Phong"
              />

              <small
                  v-if="errors.phuongXa"
                  class="error-text"
              >
                {{ errors.phuongXa }}
              </small>
            </div>

            <!-- QUẬN HUYỆN -->
            <div class="form-group">
              <label>
                Quận/Huyện
              </label>

              <input
                  v-model="
                  form.quanHuyen
                "
                  type="text"
                  maxlength="100"
                  :class="{
                  invalid:
                    errors.quanHuyen,
                }"
                  placeholder="Ví dụ: Quận 7"
              />

              <small
                  v-if="
                  errors.quanHuyen
                "
                  class="error-text"
              >
                {{
                  errors.quanHuyen
                }}
              </small>
            </div>

            <!-- TỈNH THÀNH -->
            <div class="form-group full-width">
              <label>
                Tỉnh/Thành phố
              </label>

              <input
                  v-model="
                  form.tinhThanh
                "
                  type="text"
                  maxlength="100"
                  :class="{
                  invalid:
                    errors.tinhThanh,
                }"
                  placeholder="Ví dụ: Thành phố Hồ Chí Minh"
              />

              <small
                  v-if="
                  errors.tinhThanh
                "
                  class="error-text"
              >
                {{
                  errors.tinhThanh
                }}
              </small>
            </div>

            <!-- VAI TRÒ -->
            <div class="form-group full-width">
              <label>
                Vai trò
                <span>*</span>
              </label>

              <select
                  v-model="form.vaiTro"
                  :class="{
                  invalid:
                    errors.vaiTro,
                }"
              >
                <option value="">
                  -- Lựa chọn vai trò hệ thống --
                </option>

                <option value="1">
                  Quản lý/Admin An Yên
                </option>

                <option value="2">
                  Nhân viên
                </option>

                <option value="3">
                  Hotline
                </option>
              </select>

              <small
                  v-if="errors.vaiTro"
                  class="error-text"
              >
                {{ errors.vaiTro }}
              </small>
            </div>
          </div>
        </div>

        <!-- FOOTER MODAL -->
        <div class="modal-footer">
          <button
              class="cancel-btn"
              type="button"
              :disabled="isSubmitting"
              @click="closeEmployeeForm"
          >
            Đóng
          </button>

          <button
              class="save-btn"
              type="button"
              :disabled="isSubmitting"
              @click="
              submitEmployeeForm
            "
          >
            <i
                class="fa-solid"
                :class="
                isSubmitting
                  ? 'fa-spinner fa-spin'
                  : isEditing
                    ? 'fa-floppy-disk'
                    : 'fa-user-plus'
              "
            ></i>

            {{
              isSubmitting
                  ? "Đang lưu..."
                  : isEditing
                      ? "Lưu thay đổi"
                      : "Lưu thông tin"
            }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.employee-management {
  min-height: 100%;
  background: #f5f7fb;
  color: #172033;
}

.page-content {
  width: 100%;
  padding: 28px;
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 28px;
  margin-bottom: 22px;
}

.eyebrow,
.modal-eyebrow {
  margin: 0 0 7px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1.4px;
}

.page-heading h2 {
  margin: 0;
  color: #172033;
  font-size: 29px;
  font-weight: 800;
  letter-spacing: -0.6px;
}

.heading-description {
  margin: 8px 0 0;
  color: #728097;
  font-size: 14px;
}

.heading-statistics {
  display: flex;
  gap: 10px;
}

.heading-stat {
  min-width: 118px;
  padding: 13px 16px;
  border: 1px solid #e7ebf2;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(24, 39, 75, 0.05);
}

.heading-stat strong,
.heading-stat span {
  display: block;
}

.heading-stat strong {
  color: #172033;
  font-size: 20px;
  line-height: 1;
}

.heading-stat span {
  margin-top: 7px;
  color: #7d889b;
  font-size: 12px;
}

.permission-warning {
  display: flex;
  align-items: flex-start;
  gap: 13px;
  margin-bottom: 18px;
  padding: 15px 18px;
  border: 1px solid #fecaca;
  border-radius: 12px;
  background: #fff1f2;
  color: #be123c;
}

.permission-warning i {
  margin-top: 3px;
  font-size: 18px;
}

.permission-warning strong {
  display: block;
  margin-bottom: 3px;
}

.permission-warning p {
  margin: 0;
  color: #9f1239;
  font-size: 13px;
}

.card {
  overflow: hidden;
  border: 1px solid #e7ebf2;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 12px 34px rgba(24, 39, 75, 0.06);
}

.filter-row {
  display: grid;
  grid-template-columns:
    minmax(280px, 1fr)
    190px
    180px
    auto
    auto;
  gap: 12px;
  align-items: center;
  padding: 18px;
  border-bottom: 1px solid #edf0f5;
}

.search-box {
  display: flex;
  align-items: center;
  min-width: 0;
  height: 42px;
  overflow: hidden;
  border: 1px solid #dce2eb;
  border-radius: 9px;
  background: #ffffff;
  transition: 0.2s ease;
}

.search-box:focus-within {
  border-color: #7da4ff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.09);
}

.search-label {
  display: grid;
  place-items: center;
  align-self: stretch;
  min-width: 55px;
  border-right: 1px solid #e7ebf2;
  background: #f8fafc;
  color: #536176;
  font-size: 13px;
  font-weight: 700;
}

.search-box input {
  width: 100%;
  min-width: 0;
  height: 100%;
  padding: 0 13px;
  border: 0;
  outline: none;
  color: #263247;
  font-size: 13px;
}

.filter-row select {
  width: 100%;
  height: 42px;
  padding: 0 36px 0 12px;
  border: 1px solid #dce2eb;
  border-radius: 9px;
  outline: none;
  background: #ffffff;
  color: #445168;
  font-size: 13px;
}

.filter-row select:focus {
  border-color: #7da4ff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.09);
}

.filter-btn,
.add-btn,
.edit-btn,
.danger-btn,
.cancel-btn,
.save-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 0;
  border-radius: 9px;
  font-weight: 700;
  cursor: pointer;
  transition:
      transform 0.16s ease,
      box-shadow 0.16s ease,
      background 0.16s ease;
}

.filter-btn,
.add-btn {
  min-height: 42px;
  padding: 0 16px;
  white-space: nowrap;
}

.filter-btn {
  border: 1px solid #dce2eb;
  background: #ffffff;
  color: #526177;
}

.filter-btn:hover:not(:disabled) {
  background: #f7f9fc;
}

.add-btn {
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.2);
}

.add-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
  transform: none !important;
}

.table-wrapper {
  overflow-x: auto;
}

.employee-table {
  width: 100%;
  min-width: 1180px;
  border-collapse: collapse;
}

.employee-table th {
  padding: 14px 16px;
  border-bottom: 1px solid #e8edf4;
  background: #f8fafc;
  color: #66758d;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.25px;
  text-align: left;
  text-transform: uppercase;
  white-space: nowrap;
}

.employee-table td {
  padding: 15px 16px;
  border-bottom: 1px solid #eef1f5;
  color: #38465c;
  font-size: 13px;
  vertical-align: middle;
}

.employee-table tbody tr {
  transition: background 0.17s ease;
}

.employee-table tbody tr:hover {
  background: #fbfcff;
}

.employee-table tbody tr:last-child td {
  border-bottom: 0;
}

.text-center {
  text-align: center !important;
}

.employee-cell {
  display: flex;
  align-items: center;
  min-width: 210px;
  gap: 11px;
}

.avatar {
  display: grid;
  place-items: center;
  flex: 0 0 39px;
  width: 39px;
  height: 39px;
  border: 1px solid #bfdbfe;
  border-radius: 50%;
  background: #eaf2ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
}

.employee-main-info strong {
  display: block;
  max-width: 190px;
  overflow: hidden;
  color: #202c40;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-main-info p {
  margin: 4px 0 0;
  color: #8a95a8;
  font-size: 11px;
}

.account-name {
  color: #2f3c51;
  font-size: 13px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  min-width: 170px;
  gap: 4px;
}

.contact-info span {
  color: #354257;
}

.contact-info small {
  color: #8792a5;
}

.address-text {
  display: -webkit-box;
  min-width: 180px;
  max-width: 250px;
  overflow: hidden;
  color: #6f7c91;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 28px;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
  white-space: nowrap;
}

.badge.blue {
  background: #e8f1ff;
  color: #2563eb;
}

.badge.green {
  background: #e8f8ef;
  color: #16844b;
}

.badge.orange {
  background: #fff3dd;
  color: #b86500;
}

.badge.purple {
  background: #f1eafe;
  color: #7c3aed;
}

.badge.red {
  background: #ffe9ec;
  color: #c92a43;
}

.status-dot,
.dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-dot.green,
.dot.green {
  background: #22a861;
}

.status-dot.red,
.dot.red {
  background: #dc3545;
}

.dot.blue {
  background: #2563eb;
}

.dot.orange {
  background: #f59e0b;
}

.edit-btn,
.danger-btn {
  min-height: 34px;
  padding: 0 13px;
  font-size: 12px;
}

.edit-btn {
  border: 1px solid #bcd0ff;
  background: #f5f8ff;
  color: #2563eb;
}

.edit-btn:hover {
  border-color: #8aafff;
  background: #eaf1ff;
}

.danger-btn {
  border: 1px solid #f3bdc7;
  background: #fff7f8;
  color: #c82945;
}

.danger-btn:hover:not(:disabled) {
  border-color: #e894a4;
  background: #ffebef;
}

.muted-action {
  color: #9aa4b5;
  font-size: 12px;
  font-style: italic;
}

.table-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 230px;
  padding: 35px 20px;
  color: #748198;
  font-size: 14px;
}

.table-state.empty {
  min-height: 180px;
  flex-direction: column;
}

.table-state.empty i {
  color: #aab3c2;
  font-size: 30px;
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 15px 19px;
  border-top: 1px solid #edf0f5;
  background: #ffffff;
}

.pagination-row p {
  margin: 0;
  color: #778399;
  font-size: 13px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pagination button {
  display: grid;
  place-items: center;
  min-width: 32px;
  height: 32px;
  padding: 0 9px;
  border: 1px solid #dfe4ec;
  border-radius: 7px;
  background: #ffffff;
  color: #5c697e;
  font-size: 12px;
  cursor: pointer;
}

.pagination button:hover:not(:disabled):not(.active) {
  border-color: #9db9fa;
  background: #f4f7ff;
  color: #2563eb;
}

.pagination button.active {
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.pagination button.dots {
  border-color: transparent;
  background: transparent;
}

.pagination button:disabled {
  opacity: 0.45;
}

.legend {
  display: grid;
  grid-template-columns:
    minmax(260px, 0.85fr)
    minmax(360px, 1.15fr);
  gap: 18px;
  margin-top: 18px;
  padding: 20px 22px;
  border: 1px solid #e7ebf2;
  border-radius: 13px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(24, 39, 75, 0.04);
}

.legend h5 {
  margin: 0 0 12px;
  color: #344158;
  font-size: 13px;
}

.legend p {
  margin: 0;
  color: #7d899d;
  font-size: 12px;
  line-height: 1.7;
}

.status-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
}

.status-list > span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #657288;
  font-size: 12px;
}

.modal-overlay {
  position: fixed;
  z-index: 10000;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22px;
  background: rgba(20, 29, 45, 0.58);
  backdrop-filter: blur(3px);
}

.employee-modal {
  display: flex;
  flex-direction: column;
  width: min(720px, 100%);
  max-height: calc(100vh - 44px);
  overflow: hidden;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 25px 80px rgba(13, 25, 50, 0.28);
  animation: modal-in 0.18s ease-out;
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px 18px;
  border-bottom: 1px solid #edf0f5;
}

.modal-header h3 {
  margin: 0;
  color: #1f2a3d;
  font-size: 21px;
  font-weight: 800;
}

.modal-header span {
  display: block;
  margin-top: 6px;
  color: #8792a4;
  font-size: 12px;
}

.modal-close {
  display: grid;
  place-items: center;
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  border: 1px solid #e2e7ee;
  border-radius: 9px;
  background: #ffffff;
  color: #778398;
  cursor: pointer;
}

.modal-close:hover:not(:disabled) {
  background: #f6f8fb;
  color: #263247;
}

.modal-body {
  overflow-y: auto;
  padding: 22px 24px;
}

.form-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.form-group {
  min-width: 0;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #4a576d;
  font-size: 12px;
  font-weight: 800;
}

.form-group label span {
  color: #dc3545;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  border: 1px solid #dce2eb;
  border-radius: 9px;
  outline: none;
  background: #ffffff;
  color: #273449;
  font: inherit;
  font-size: 13px;
  transition:
      border-color 0.16s ease,
      box-shadow 0.16s ease;
}

.form-group input,
.form-group select {
  height: 42px;
  padding: 0 12px;
}

.form-group textarea {
  min-height: 86px;
  padding: 11px 12px;
  resize: vertical;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #7da4ff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.09);
}

.form-group input.invalid,
.form-group select.invalid,
.form-group textarea.invalid {
  border-color: #e8798a;
  background: #fffafb;
}

.error-text,
.helper-text {
  display: block;
  margin-top: 6px;
  font-size: 11px;
}

.error-text {
  color: #d12c49;
}

.helper-text {
  color: #8c97a9;
}

.field-footer {
  display: flex;
  justify-content: flex-end;
}

.field-footer .error-text {
  margin-right: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #edf0f5;
  background: #fbfcfe;
}

.cancel-btn,
.save-btn {
  min-height: 40px;
  padding: 0 18px;
  font-size: 13px;
}

.cancel-btn {
  border: 1px solid #dce2eb;
  background: #ffffff;
  color: #5f6c81;
}

.cancel-btn:hover:not(:disabled) {
  background: #f5f7fa;
}

.save-btn {
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.19);
}

.save-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

@media (max-width: 1200px) {
  .filter-row {
    grid-template-columns:
      minmax(260px, 1fr)
      170px
      160px;
  }

  .filter-btn,
  .add-btn {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .page-content {
    padding: 20px;
  }

  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .heading-statistics {
    width: 100%;
  }

  .heading-stat {
    flex: 1;
  }

  .filter-row {
    grid-template-columns: 1fr 1fr;
  }

  .search-box {
    grid-column: 1 / -1;
  }

  .legend {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .page-content {
    padding: 14px;
  }

  .page-heading h2 {
    font-size: 24px;
  }

  .heading-statistics {
    display: grid;
    grid-template-columns:
      repeat(3, 1fr);
  }

  .heading-stat {
    min-width: 0;
    padding: 11px;
  }

  .heading-stat strong {
    font-size: 17px;
  }

  .heading-stat span {
    font-size: 10px;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .search-box {
    grid-column: auto;
  }

  .pagination-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-group.full-width {
    grid-column: auto;
  }

  .modal-overlay {
    align-items: flex-end;
    padding: 0;
  }

  .employee-modal {
    width: 100%;
    max-height: 94vh;
    border-radius: 16px 16px 0 0;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding-right: 18px;
    padding-left: 18px;
  }
}
.warning-btn {
  background-color: #fff7d6;
  color: #b77900;
  border: 1.5px solid #d99a00;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}

.warning-btn:hover {
  background-color: #ffefb0;
  border-color: #b77900;
}

.warning-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success-btn {
  background-color: #e8f7ee;
  color: #218838;
  border: 1.5px solid #28a745;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}

.success-btn:hover {
  background-color: #d4f0df;
  border-color: #218838;
}

.success-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.muted-action {
  color: #999;
  font-size: 14px;
}
</style>