<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";

import PopChiTietDonHang from "../nhanvien/PopChiTietDonHang.vue";

import {
  formatDate,
  formatCurrency,
  getTrangThaiDonHangText,
} from "../../services/donHangService.js";
import api from "../../api/api.js";

import {
  Search,
  Filter,
  User,
  Avatar,
  Wallet,
  Calendar,
  View,
  Check,
} from "@element-plus/icons-vue";

// ── Trạng thái popup ─────────────────────────────
const showChiTiet = ref(false);
const selectedDonHang = ref(null);

// ── Bộ lọc ─────────────────────────────
const keyword = ref("");
const trangThaiFilter = ref("Tất cả");
const ptThanhToanFilter = ref("Tất cả");
const dateRange = ref([]);

const currentPage = ref(1);
const pageSize = ref(8);

// ── Dữ liệu ─────────────────────────────
const donHangs = ref([]);
const loading = ref(false);

const loadDonHangs = async () => {
  try {
    loading.value = true;

    const response = await api.get("/api/don-hang");
    const data = response.data;

    donHangs.value = Array.isArray(data)
        ? data
        : data?.items || [];

    // Convert trangThai from number to text
    donHangs.value = donHangs.value.map(dh => ({
      ...dh,
      trangThai: getTrangThaiDonHangText(dh.trangThai)
    }));
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
    ElMessage.error("Không thể tải danh sách đơn hàng");
  } finally {
    loading.value = false;
  }
};

const getOrderCode = (order) => {
  return String(
      order?.maCode ??
      order?.maDonHang ??
      order?.MaDonHang ??
      ""
  ).trim();
};

const getOrderAmount = (order) => {
  const rawAmount =
      order?.tongTien ??
      order?.TongTien ??
      order?.thanhTien ??
      order?.ThanhTien ??
      order?.totalAmount ??
      order?.total ??
      0;

  if (typeof rawAmount === "number") {
    return Number.isFinite(rawAmount)
        ? Math.max(0, Math.round(rawAmount))
        : 0;
  }

  let value = String(rawAmount)
      .replace(/[₫đ\s]/gi, "")
      .trim();

  if (/^\d{1,3}(\.\d{3})+$/.test(value)) {
    value = value.replace(/\./g, "");
  } else {
    value = value.replace(/,/g, "");
  }

  const amount = Number(value);

  return Number.isFinite(amount)
      ? Math.max(0, Math.round(amount))
      : 0;
};

onMounted(() => {
  loadDonHangs();
});

// ── Helper mã đơn hàng ─────────────────────────────
const getMaDonHang = (dh) => {
  return dh?.maDonHang || dh?.MaDonHang || dh?.id;
};

// ── Lọc + tìm kiếm ─────────────────────────────
const filteredList = computed(() => {
  return donHangs.value.filter((dh) => {
    const kw = keyword.value.trim().toLowerCase();

    const matchKw =
        !kw ||
        dh.maCode?.toLowerCase().includes(kw) ||
        dh.tenKhachHang?.toLowerCase().includes(kw) ||
        dh.tenNhanVien?.toLowerCase().includes(kw);

    const matchTT =
        trangThaiFilter.value === "Tất cả" ||
        dh.trangThai === trangThaiFilter.value;

    const matchPT =
        ptThanhToanFilter.value === "Tất cả" ||
        dh.phuongThucThanhToan === ptThanhToanFilter.value;

    let matchDate = true;

    if (dateRange.value && dateRange.value.length === 2) {
      const orderDate = new Date(dh.ngayTaoDon || dh.NgayTaoDon);
      const startDate = new Date(dateRange.value[0]);
      const endDate = new Date(dateRange.value[1]);

      if (orderDate < startDate || orderDate > endDate) {
        matchDate = false;
      }
    }

    return matchKw && matchTT && matchPT && matchDate;
  });
});

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

const apDungBoLoc = () => {
  currentPage.value = 1;
  ElMessage.success("Đã áp dụng bộ lọc");
};

// ── Chi tiết đơn hàng (read-only) ─────────────────────────────
const xemChiTiet = (dh) => {
  selectedDonHang.value = JSON.parse(JSON.stringify(dh));
  showChiTiet.value = true;
};

// ── Stepper trạng thái ─────────────────────────────
const STEPS = [
  "Mới tạo",
  "Xác nhận",
  "Đã nhận",
  "Xử lý",
  "Đã giao",
  "Thanh toán",
  "Hoàn thành",
];

const normalizeStepStatus = (value) => {
  const status = String(value ?? "").trim().toLowerCase();

  if (status === "mới tạo") {
    return "Mới tạo";
  }

  if (
      status === "xác nhận" ||
      status === "đã xác nhận" ||
      status === "chờ đối tác xác nhận"
  ) {
    return "Xác nhận";
  }

  if (status === "đã nhận") {
    return "Đã nhận";
  }

  if (
      status === "xử lý" ||
      status === "đang xử lý"
  ) {
    return "Xử lý";
  }

  if (status === "đã giao") {
    return "Đã giao";
  }

  if (
      status === "thanh toán" ||
      status === "chờ thanh toán" ||
      status === "đã thanh toán"
  ) {
    return "Thanh toán";
  }

  if (status === "hoàn thành") {
    return "Hoàn thành";
  }

  return String(value ?? "").trim();
};

const getStepIndex = (trangThai) => {
  return STEPS.indexOf(
      normalizeStepStatus(trangThai)
  );
};

const isStepCompleted = (dh, stepName) => {
  if (
      dh.trangThai === "Đã hủy" ||
      dh.trangThai === "Từ chối" ||
      dh.trangThai === "Gặp sự cố"
  ) {
    return false;
  }

  const currentIdx = getStepIndex(dh.trangThai);
  const targetIdx = getStepIndex(stepName);

  return (
      currentIdx >= 0 &&
      targetIdx >= 0 &&
      targetIdx < currentIdx
  );
};

const isStepActive = (dh, stepName) => {
  return (
      normalizeStepStatus(dh.trangThai) ===
      normalizeStepStatus(stepName)
  );
};

const isLineCompleted = (dh, targetStep) => {
  return (
      isStepCompleted(dh, targetStep) ||
      isStepActive(dh, targetStep)
  );
};

// ── Badge class ─────────────────────────────
const trangThaiBadgeClass = (tt) => {
  const status = normalizeStepStatus(tt);

  if (status === "Mới tạo") return "badge-yellow";
  if (status === "Xác nhận") return "badge-pink";
  if (status === "Đã nhận") return "badge-blue";
  if (status === "Xử lý") return "badge-orange";
  if (status === "Đã giao") return "badge-teal";
  if (status === "Thanh toán") return "badge-indigo";
  if (status === "Hoàn thành") return "badge-green";

  if (
      tt === "Đã hủy" ||
      tt === "Từ chối" ||
      tt === "Gặp sự cố"
  ) {
    return "badge-red";
  }

  return "badge-gray";
};
</script>

<template>
  <div class="don-hang-page admin-don-hang-page">
    <!-- ── Bộ lọc ── -->
    <div class="filter-bar">
      <div class="search-wrap">
        <el-icon class="search-icon">
          <Search />
        </el-icon>

        <input
            v-model="keyword"
            class="search-input"
            placeholder="Tìm kiếm mã đơn, khách hàng..."
        />
      </div>

      <div class="filter-item">
        <span class="filter-label">Trạng thái:</span>

        <select
            v-model="trangThaiFilter"
            class="select-filter"
        >
          <option>Tất cả</option>
          <option>Mới tạo</option>
          <option>Xác nhận</option>
          <option>Đã nhận</option>
          <option>Xử lý</option>
          <option>Thanh toán</option>
          <option>Hoàn thành</option>
          <option>Đã hủy</option>
          <option>Từ chối</option>
        </select>
      </div>

      <div class="filter-item">
        <span class="filter-label">Phương thức thanh toán:</span>

        <select
            v-model="ptThanhToanFilter"
            class="select-filter"
        >
          <option>Tất cả</option>
          <option>Chuyển khoản</option>
          <option>Tiền mặt</option>
        </select>
      </div>

      <div class="filter-item date-picker-item">
        <span class="filter-label">Ngày tạo:</span>

        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Bắt đầu"
            end-placeholder="Kết thúc"
            format="DD/MM/YYYY"
            size="small"
            style="width: 200px"
        />
      </div>

      <button
          class="btn-create-order"
          @click="apDungBoLoc"
      >
        <el-icon>
          <Filter />
        </el-icon>
        Bộ lọc
      </button>

      <button
          class="btn-create-order btn-reload"
          @click="loadDonHangs"
      >
        <el-icon>
          <View />
        </el-icon>
        Làm mới
      </button>
    </div>

    <!-- ── Grid đơn hàng ── -->
    <div class="order-grid">
      <div
          v-if="loading"
          class="empty-state"
      >
        Đang tải đơn hàng...
      </div>

      <div
          v-else-if="pagedList.length === 0"
          class="empty-state"
      >
        Không có đơn hàng nào.
      </div>

      <div
          v-for="dh in pagedList"
          :key="dh.maDonHang || dh.MaDonHang"
          class="order-card"
      >
        <!-- Header -->
        <div class="card-header">
          <h3 class="order-code">#{{ dh.maCode }}</h3>

          <span
              class="badge"
              :class="trangThaiBadgeClass(dh.trangThai)"
          >
            {{ dh.trangThai }}
          </span>
        </div>

        <!-- Stepper ngang -->
        <div class="card-stepper">
          <div class="stepper-track">
            <template
                v-for="(step, idx) in STEPS"
                :key="step"
            >
              <div
                  class="step-item"
                  :class="{
                  completed: isStepCompleted(dh, step),
                  active: isStepActive(dh, step),
                }"
              >
                <div class="step-circle">
                  <el-icon v-if="isStepCompleted(dh, step)">
                    <Check />
                  </el-icon>

                  <div
                      v-else
                      class="inner-dot"
                  ></div>
                </div>

                <div class="step-label">
                  {{ step }}
                </div>
              </div>

              <div
                  v-if="idx < STEPS.length - 1"
                  class="step-line"
                  :class="{ completed: isLineCompleted(dh, STEPS[idx + 1]) }"
              ></div>
            </template>
          </div>
        </div>

        <!-- Thông tin -->
        <div class="card-info">
          <div class="info-row">
            <el-icon>
              <User />
            </el-icon>

            <span>Khách hàng:</span>
            <strong>{{ dh.tenKhachHang }}</strong>
          </div>

          <div class="info-row">
            <el-icon>
              <Avatar />
            </el-icon>

            <span>Nhân viên phụ trách:</span>
            <strong>{{ dh.tenNhanVien || "Không có" }}</strong>
          </div>

          <div class="info-row">
            <el-icon>
              <Wallet />
            </el-icon>

            <span>Phương thức thanh toán:</span>
            <strong>{{ dh.phuongThucThanhToan || "Không có" }}</strong>
          </div>

          <div class="info-row">
            <el-icon>
              <Calendar />
            </el-icon>

            <span>Ngày tạo:</span>
            <strong>
              {{ formatDate(dh.ngayTaoDon || dh.NgayTaoDon) }}
            </strong>
          </div>
        </div>

        <!-- Actions - Admin chỉ xem chi tiết -->
        <div class="card-actions">
          <button
              class="btn-outline-green"
              @click="xemChiTiet(dh)"
          >
            <el-icon>
              <View />
            </el-icon>
            Xem chi tiết
          </button>
        </div>
      </div>
    </div>

    <!-- ── Phân trang ── -->
    <div class="pagination-bar">
      <span class="pag-info">
        Hiển thị
        {{ Math.min((currentPage - 1) * pageSize + 1, filteredList.length) }}
        -
        {{ Math.min(currentPage * pageSize, filteredList.length) }}
        của {{ filteredList.length }} đơn hàng
      </span>

      <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next"
      />
    </div>

    <!-- ── Popup chi tiết đơn hàng (read-only) ── -->
    <PopChiTietDonHang
        v-model="showChiTiet"
        :don-hang="selectedDonHang"
        :readonly="true"
        @dong="showChiTiet = false"
    />
  </div>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/TrangQLDonHang.css"></style>

<style scoped>
.admin-don-hang-page .btn-reload {
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
}

.admin-don-hang-page .btn-reload:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.admin-don-hang-page .order-grid {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  padding-right: 8px;
}

.admin-don-hang-page .order-grid::-webkit-scrollbar {
  width: 8px;
}

.admin-don-hang-page .order-grid::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.admin-don-hang-page .order-grid::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.admin-don-hang-page .order-grid::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
