<script setup>
import { ref, computed, onMounted } from "vue";
import PopTaoDonHang from "./PopTaoDonHang.vue";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import { getDonHangs, formatCurrency, formatDate } from "../../services/donHangService.js";
import {
  Search,
  Filter,
  Check,
  User,
  Avatar,
  Wallet,
  Calendar,
  EditPen,
  Delete,
  Right,
  Plus
} from "@element-plus/icons-vue";

// ── Trạng thái ──────────────────────────────────────────
const showCreateOrder = ref(false);
const showChiTiet         = ref(false);
const selectedDonHang     = ref(null);

const keyword      = ref("");
const trangThaiFilter = ref("Tất cả");
const ptThanhToanFilter = ref("Tất cả");
const dateRange = ref([]);

const currentPage  = ref(1);
const pageSize     = ref(3);

// ── Dữ liệu ─────────────────────────────
const donHangs = ref([]);

onMounted(async () => {
  try {
    const data = await getDonHangs();
    donHangs.value = data.items || data || [];
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
  }
});

// ── Lọc + tìm kiếm ─────────────────────────────────────
const filteredList = computed(() => {
  return donHangs.value.filter((dh) => {
    const kw = keyword.value.toLowerCase();
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
        const orderDate = new Date(dh.NgayTaoDon);
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

// ── Hành động ───────────────────────────────────────────
const xemChiTiet = (dh) => {
  selectedDonHang.value = dh;
  showChiTiet.value = true;
};

const huyDon = (maDonHang) => {
  const idx = donHangs.value.findIndex((d) => d.MaDonHang === maDonHang);
  if (idx !== -1) {
    donHangs.value[idx].trangThai = "Đã hủy";
    if (donHangs.value[idx].lichSu) {
       donHangs.value[idx].lichSu.forEach((ls) => { ls.done = false; });
    }
  }
  showChiTiet.value = false;
};

// ── Stepper Logic ───────────────────────────────────────
const STEPS = ["Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Thanh toán"];

const getStepIndex = (trangThai) => {
    return STEPS.indexOf(trangThai);
};

const isStepCompleted = (dh, stepName) => {
    const currentIdx = getStepIndex(dh.trangThai);
    const targetIdx = getStepIndex(stepName);
    if (dh.trangThai === "Đã hủy") return false;
    return targetIdx < currentIdx || (targetIdx === currentIdx && stepName === "Thanh toán" && dh.trangThai === "Thanh toán");
};

const isStepActive = (dh, stepName) => {
    return dh.trangThai === stepName && stepName !== "Thanh toán";
};

const isLineCompleted = (dh, targetStep) => {
    return isStepCompleted(dh, targetStep) || isStepActive(dh, targetStep);
};

// ── Badge class ─────────────────────────────────────────
const trangThaiBadgeClass = (tt) => {
  if (tt === "Đã xác nhận")   return "badge-blue";
  if (tt === "Đang xử lý")    return "badge-orange";
  if (tt === "Chờ xác nhận")  return "badge-yellow";
  if (tt === "Thanh toán")    return "badge-green";
  if (tt === "Đã hủy")        return "badge-red";
  if (tt === "Hoàn thành")    return "badge-purple";
  return "badge-gray";
};

const handleSaveDraft = (payload) => {
  console.log("Tạm lưu đơn hàng:", payload);
  alert("Đã tạm lưu đơn hàng");
};

const handleCreateOrder = async (payload) => {
  try {
    alert("Tạo đơn hàng thành công");
    showCreateOrder.value = false;
    const data = await getDonHangs();
    donHangs.value = data.items || data || [];
  } catch (error) {
    console.error("Lỗi khi tạo đơn hàng:", error);
    alert("Tạo đơn hàng thất bại");
  }
};

const nextStatus = (dh) => {
    const currentIdx = getStepIndex(dh.trangThai);
    if (currentIdx >= 0 && currentIdx < STEPS.length - 1) {
        return STEPS[currentIdx + 1];
    }
    return null;
}

const updateNextStatus = (dh) => {
    const next = nextStatus(dh);
    if (next) {
        dh.trangThai = next;
    }
}
</script>

<template>
  <div class="don-hang-page">

    <!-- ── Bộ lọc ── -->
    <div class="filter-bar">
      <div class="search-wrap">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="Tìm kiếm mã đơn, khách hàng..."
        />
      </div>

      <div class="filter-item">
          <span class="filter-label">Trạng thái:</span>
          <select v-model="trangThaiFilter" class="select-filter">
            <option>Tất cả</option>
            <option>Chờ xác nhận</option>
            <option>Đã xác nhận</option>
            <option>Hoàn thành</option>
            <option>Thanh toán</option>
            <option>Đã hủy</option>
          </select>
      </div>

      <div class="filter-item">
          <span class="filter-label">Phương thức thanh toán:</span>
          <select v-model="ptThanhToanFilter" class="select-filter">
            <option>Tất cả</option>
            <option>Chuyển khoản</option>
            <option>Thanh toán khi nhận hàng</option>
            <option>Ví điện tử</option>
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

      <button class="btn-create-order" @click="showCreateOrder = true">
        <el-icon><Filter /></el-icon>
        Bộ lọc
      </button>
      
      <!-- User requested to replace filter with create button -->
      <button class="btn-create-order btn-tao" @click="showCreateOrder = true">
        <el-icon><Plus /></el-icon>
        Tạo đơn hàng
      </button>
    </div>

    <!-- ── Grid đơn hàng ── -->
    <div class="order-grid">
      <div v-if="pagedList.length === 0" class="empty-state">
         Không có đơn hàng nào.
      </div>
      <div v-for="dh in pagedList" :key="dh.MaDonHang" class="order-card">
          <!-- Header -->
          <div class="card-header">
              <h3 class="order-code">#{{ dh.maCode }}</h3>
              <span class="badge" :class="trangThaiBadgeClass(dh.trangThai)">{{ dh.trangThai }}</span>
          </div>

          <!-- Stepper ngang -->
          <div class="card-stepper">
              <div class="stepper-track">
                  <!-- Chờ xác nhận -->
                  <div class="step-item" :class="{ completed: isStepCompleted(dh, 'Chờ xác nhận'), active: isStepActive(dh, 'Chờ xác nhận') }">
                      <div class="step-circle"><el-icon v-if="isStepCompleted(dh, 'Chờ xác nhận')"><Check/></el-icon><div v-else class="inner-dot"></div></div>
                      <div class="step-label">Chờ xác nhận</div>
                  </div>
                  <div class="step-line" :class="{ completed: isLineCompleted(dh, 'Đã xác nhận') }"></div>
                  
                  <!-- Đã xác nhận -->
                  <div class="step-item" :class="{ completed: isStepCompleted(dh, 'Đã xác nhận'), active: isStepActive(dh, 'Đã xác nhận') }">
                      <div class="step-circle"><el-icon v-if="isStepCompleted(dh, 'Đã xác nhận')"><Check/></el-icon><div v-else class="inner-dot"></div></div>
                      <div class="step-label">Đã xác nhận</div>
                  </div>
                  <div class="step-line" :class="{ completed: isLineCompleted(dh, 'Hoàn thành') }"></div>

                  <!-- Hoàn thành -->
                  <div class="step-item" :class="{ completed: isStepCompleted(dh, 'Hoàn thành'), active: isStepActive(dh, 'Hoàn thành') }">
                      <div class="step-circle"><el-icon v-if="isStepCompleted(dh, 'Hoàn thành')"><Check/></el-icon><div v-else class="inner-dot"></div></div>
                      <div class="step-label">Hoàn thành</div>
                  </div>
                  <div class="step-line" :class="{ completed: isLineCompleted(dh, 'Thanh toán') }"></div>

                  <!-- Thanh toán -->
                  <div class="step-item" :class="{ completed: isStepCompleted(dh, 'Thanh toán'), active: isStepActive(dh, 'Thanh toán') }">
                      <div class="step-circle"><el-icon v-if="isStepCompleted(dh, 'Thanh toán')"><Check/></el-icon><div v-else class="inner-dot"></div></div>
                      <div class="step-label">Thanh toán</div>
                  </div>
              </div>
          </div>

          <!-- Thông tin -->
          <div class="card-info">
              <div class="info-row">
                  <el-icon><User/></el-icon> <span>Khách hàng:</span>
                  <strong>{{ dh.tenKhachHang }}</strong>
              </div>
              <div class="info-row">
                  <el-icon><Avatar/></el-icon> <span>Nhân viên phụ trách:</span>
                  <strong>{{ dh.tenNhanVien || 'Không có' }}</strong>
              </div>
              <div class="info-row">
                  <el-icon><Wallet/></el-icon> <span>Phương thức thanh toán:</span>
                  <strong>{{ dh.phuongThucThanhToan || 'Không có' }}</strong>
              </div>
              <div class="info-row">
                  <el-icon><Calendar/></el-icon> <span>Ngày tạo:</span>
                  <strong>{{ formatDate(dh.NgayTaoDon) }}</strong>
              </div>
          </div>

          <!-- Actions -->
          <div class="card-actions">
              <button class="btn-outline-green" @click="xemChiTiet(dh)">
                  <el-icon><EditPen/></el-icon> Sửa
              </button>
              <button class="btn-outline-red" @click="huyDon(dh.MaDonHang)">
                  <el-icon><Delete/></el-icon> Hủy
              </button>
              
              <button v-if="nextStatus(dh)" class="btn-filled-green" @click="updateNextStatus(dh)">
                  Cập nhật trạng thái tiếp theo <el-icon><Right/></el-icon>
              </button>
              <button v-else class="btn-disabled">
                  Không còn trạng thái tiếp theo
              </button>
          </div>
      </div>
    </div>

    <!-- ── Phân trang ── -->
    <div class="pagination-bar">
      <span class="pag-info">
        Hiển thị {{ Math.min((currentPage - 1) * pageSize + 1, filteredList.length) }}
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

    <!-- ── Popup tạo đơn hàng ── -->
    <PopTaoDonHang
        v-if="showCreateOrder"
        @close="showCreateOrder = false"
        @submit="handleCreateOrder"
        @save-draft="handleSaveDraft"
    />

    <!-- ── Popup chi tiết đơn hàng ── -->
    <PopChiTietDonHang
      v-model="showChiTiet"
      :don-hang="selectedDonHang"
      @huy-don="huyDon"
    />
  </div>
</template>

<style scoped>
.don-hang-page {
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Inter', Arial, sans-serif;
}

/* ── Filter bar ── */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  background: transparent;
}
.search-wrap {
  flex: 1;
  min-width: 250px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 0 12px;
  height: 38px;
}
.search-icon {
  color: #aaa;
  font-size: 15px;
}
.search-input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 13px;
  background: transparent;
}

.filter-item {
    display: flex;
    align-items: center;
    background: #fff;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    height: 38px;
    padding: 0 10px;
    gap: 8px;
}
.filter-label {
    font-size: 13px;
    color: #666;
}
.select-filter {
  border: none;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
  outline: none;
  color: #333;
  font-weight: 500;
}
.date-picker-item :deep(.el-date-editor) {
    border: none;
    box-shadow: none;
}

.btn-create-order {
  height: 38px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 0 14px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #17934a;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-tao {
    border: 1px solid #17934a;
    background: #e8f8ef;
}
.btn-tao:hover {
    background: #17934a;
    color: #fff;
}

/* ── Order Grid ── */
.order-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 24px;
}

.empty-state {
    grid-column: 1 / -1;
    text-align: center;
    padding: 40px;
    color: #888;
    background: #fff;
    border-radius: 12px;
}

.order-card {
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.02);
    display: flex;
    flex-direction: column;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.order-code {
    margin: 0;
    font-size: 18px;
    font-weight: 800;
    color: #1a1a2e;
}

.badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}
.badge-yellow { background: #fff8e1; color: #f57f17; }
.badge-blue { background: #e8f0fe; color: #1967d2; }
.badge-purple { background: #f3e8fd; color: #7b1fa2; }
.badge-green { background: #e8f8ef; color: #17934a; }
.badge-orange { background: #fff3e0; color: #e65100; }
.badge-red { background: #fce8e6; color: #c5221f; }
.badge-gray { background: #f1f3f4; color: #5f6368; }

/* ── Stepper ── */
.card-stepper {
    margin-bottom: 24px;
}
.stepper-track {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
}

.step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    z-index: 2;
    width: 60px;
}
.step-circle {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 12px;
}
.inner-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ccc;
}
.step-item.active .step-circle {
    background: #f57f17;
    border: 2px solid #fff8e1;
    box-shadow: 0 0 0 3px #f57f17;
}
.step-item.active .inner-dot {
    background: #fff;
}
.step-item.completed .step-circle {
    background: #17934a;
}
.step-label {
    font-size: 11px;
    color: #666;
    text-align: center;
    white-space: nowrap;
}

.step-line {
    flex: 1;
    height: 2px;
    background: #f0f0f0;
    margin: 0 4px;
    position: relative;
    top: -10px;
}
.step-line.completed {
    background: #17934a;
}

/* ── Info ── */
.card-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;
    flex: 1;
}
.info-row {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #555;
}
.info-row .el-icon {
    margin-right: 8px;
    color: #888;
    font-size: 16px;
}
.info-row span {
    width: 150px;
}
.info-row strong {
    color: #222;
    font-weight: 600;
    flex: 1;
}

/* ── Actions ── */
.card-actions {
    display: grid;
    grid-template-columns: auto auto 1fr;
    gap: 8px;
}
.btn-outline-green, .btn-outline-red, .btn-filled-green, .btn-disabled {
    border-radius: 6px;
    padding: 8px 12px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    transition: all 0.2s;
}
.btn-outline-green {
    border: 1px solid #17934a;
    color: #17934a;
    background: #fff;
}
.btn-outline-green:hover { background: #e8f8ef; }

.btn-outline-red {
    border: 1px solid #dc3545;
    color: #dc3545;
    background: #fff;
}
.btn-outline-red:hover { background: #fce8e6; }

.btn-filled-green {
    border: 1px solid #17934a;
    color: #fff;
    background: #17934a;
}
.btn-filled-green:hover { background: #137d3e; }

.btn-disabled {
    border: 1px solid #e0e0e0;
    color: #aaa;
    background: #f5f5f5;
    cursor: not-allowed;
    grid-column: span 1;
}

/* ── Pagination ── */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
}
.pag-info {
  font-size: 13px;
  color: #666;
}
</style>
