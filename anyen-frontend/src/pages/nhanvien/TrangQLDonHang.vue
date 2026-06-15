<script setup>
import {ref, computed, onMounted} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import PopTaoDonHang from "./PopTaoDonHang.vue";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import PopTaoHoaDon from "./PopTaoHoaDon.vue";

import {
  getDonHangs,
  taoDonHang,
  capNhatTrangThai,
  huyDonHang as huyDonHangAPI,
  formatCurrency,
  formatDate
} from "../../services/donHangService.js";

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
  Plus,
  Tickets,
  View
} from "@element-plus/icons-vue";

// ── Trạng thái ──────────────────────────────────────────
const showCreateOrder = ref(false);
const showChiTiet = ref(false);
const selectedDonHang = ref(null);
const showPaymentDialog = ref(false);
const showCashConfirmDialog = ref(false);
const selectedOrderForPayment = ref(null);
const showTaoHoaDon = ref(false);
const selectedDonHangHoaDon = ref(null);

const keyword = ref("");
const trangThaiFilter = ref("Tất cả");
const ptThanhToanFilter = ref("Tất cả");
const dateRange = ref([]);

const currentPage = ref(1);
const pageSize = ref(3);

// ── Dữ liệu ─────────────────────────────
const donHangs = ref([]);

const loadDonHangs = async () => {
  try {
    const data = await getDonHangs();
    donHangs.value = data.items || data || [];
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
  }
};

onMounted(loadDonHangs);

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

// ── Hành động ───────────────────────────────────────────
const xemChiTiet = (dh) => {
  selectedDonHang.value = dh;
  showChiTiet.value = true;
};

const huyDon = async (maDonHang) => {
  try {
    await huyDonHangAPI(maDonHang);
    ElMessage.success("Đã hủy đơn hàng thành công");
    await loadDonHangs();
    showChiTiet.value = false;
  } catch (error) {
    console.error("Lỗi khi hủy đơn hàng:", error);
    ElMessage.error(error.response?.data?.message || "Hủy đơn hàng thất bại");
  }
};

const apDungBoLoc = () => {
  currentPage.value = 1;
  ElMessage.success("Đã áp dụng bộ lọc");
};

const daCoHoaDon = (dh) => {
  return Boolean(dh.maHoaDon || dh.MaHoaDon || dh.daCoHoaDon);
};

const canTaoHoaDon = (dh) => {
  return (
      ["Chờ thanh toán", "Hoàn thành"].includes(dh.trangThai) &&
      !daCoHoaDon(dh)
  );
};

const taoHoaDon = (dh) => {
  selectedDonHangHoaDon.value = dh;
  showTaoHoaDon.value = true;
};

const handleTaoHoaDonSubmit = async (payload) => {
  try {
    await taoHoaDonAPI(payload);

    ElMessage.success("Tạo hóa đơn thành công");
    showTaoHoaDon.value = false;
    selectedDonHangHoaDon.value = null;

    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi tạo hóa đơn:", error);
    ElMessage.error(error.response?.data?.message || "Tạo hóa đơn thất bại");
  }
};

const xemHoaDon = (dh) => {
  ElMessage.info(`Xem hóa đơn của đơn hàng #${dh.maCode}`);
};

// ── Stepper Logic ───────────────────────────────────────
const STEPS = ["Mới tạo", "Đã xác nhận", "Đang xử lý", "Chờ thanh toán", "Hoàn thành"];

const getStepIndex = (trangThai) => {
  return STEPS.indexOf(trangThai);
};

const isStepCompleted = (dh, stepName) => {
  const currentIdx = getStepIndex(dh.trangThai);
  const targetIdx = getStepIndex(stepName);
  if (dh.trangThai === "Đã hủy") return false;
  return targetIdx < currentIdx;
};

const isStepActive = (dh, stepName) => {
  return dh.trangThai === stepName;
};

const isLineCompleted = (dh, targetStep) => {
  return isStepCompleted(dh, targetStep) || isStepActive(dh, targetStep);
};

// ── Badge class ─────────────────────────────────────────
const trangThaiBadgeClass = (tt) => {
  if (tt === "Đã xác nhận") return "badge-blue";
  if (tt === "Đang xử lý") return "badge-orange";
  if (tt === "Mới tạo") return "badge-yellow";
  if (tt === "Chờ thanh toán") return "badge-purple";
  if (tt === "Hoàn thành") return "badge-green";
  if (tt === "Đã hủy") return "badge-red";
  return "badge-gray";
};

const handleSaveDraft = (payload) => {
  console.log("Tạm lưu đơn hàng:", payload);
  alert("Đã tạm lưu đơn hàng");
};

const handleCreateOrder = async (payload) => {
  try {
    await taoDonHang(payload);

    ElMessage.success("Tạo đơn hàng và gửi thông báo đối tác thành công");

    showCreateOrder.value = false;
    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi tạo đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Tạo đơn hàng thất bại"
    );
  }
};
const nextStatus = (dh) => {
  const currentIdx = getStepIndex(dh.trangThai);
  if (currentIdx >= 0 && currentIdx < STEPS.length - 1 && dh.trangThai !== "Đã hủy") {
    return STEPS[currentIdx + 1];
  }
  return null;
}

const doUpdateStatus = async (dh, next) => {
  try {
    await capNhatTrangThai(dh.maDonHang || dh.MaDonHang, next);
    ElMessage.success(`Đã cập nhật trạng thái: ${next}`);
    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
    ElMessage.error(error.response?.data?.message || "Cập nhật trạng thái thất bại");
  }
};

const updateNextStatus = async (dh) => {
  const next = nextStatus(dh);
  if (next) {
    if (dh.trangThai === "Chờ thanh toán") {
      if (dh.phuongThucThanhToan === "Chuyển khoản") {
        selectedOrderForPayment.value = dh;
        showPaymentDialog.value = true;
        return;
      } else if (dh.phuongThucThanhToan === "Tiền mặt") {
        selectedOrderForPayment.value = dh;
        showCashConfirmDialog.value = true;
        return;
      }
    }
    await doUpdateStatus(dh, next);
  }
};

const confirmPayment = async () => {
  if (selectedOrderForPayment.value) {
    await doUpdateStatus(selectedOrderForPayment.value, "Hoàn thành");
    showPaymentDialog.value = false;
    selectedOrderForPayment.value = null;
  }
};

const confirmCashPayment = async () => {
  if (selectedOrderForPayment.value) {
    await doUpdateStatus(selectedOrderForPayment.value, "Hoàn thành");
    showCashConfirmDialog.value = false;
    selectedOrderForPayment.value = null;
  }
};
</script>

<template>
  <div class="don-hang-page">

    <!-- ── Bộ lọc ── -->
    <div class="filter-bar">
      <div class="search-wrap">
        <el-icon class="search-icon">
          <Search/>
        </el-icon>
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
          <option>Mới tạo</option>
          <option>Đã xác nhận</option>
          <option>Đang xử lý</option>
          <option>Chờ thanh toán</option>
          <option>Hoàn thành</option>
          <option>Đã hủy</option>
        </select>
      </div>

      <div class="filter-item">
        <span class="filter-label">Phương thức thanh toán:</span>
        <select v-model="ptThanhToanFilter" class="select-filter">
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

      <button class="btn-create-order" @click="apDungBoLoc">
        <el-icon><Filter /></el-icon>
        Bộ lọc
      </button>

      <!-- User requested to replace filter with create button -->
      <button class="btn-create-order btn-tao" @click="showCreateOrder = true">
        <el-icon>
          <Plus/>
        </el-icon>
        Tạo đơn hàng
      </button>
    </div>

    <!-- ── Grid đơn hàng ── -->
    <div class="order-grid">
      <div v-if="pagedList.length === 0" class="empty-state">
        Không có đơn hàng nào.
      </div>
      <div v-for="dh in pagedList" :key="dh.maDonHang || dh.MaDonHang" class="order-card">
        <!-- Header -->
        <div class="card-header">
          <h3 class="order-code">#{{ dh.maCode }}</h3>
          <span class="badge" :class="trangThaiBadgeClass(dh.trangThai)">{{ dh.trangThai }}</span>
        </div>

        <!-- Stepper ngang -->
        <div class="card-stepper">
          <div class="stepper-track">
            <template v-for="(step, idx) in STEPS" :key="step">
              <div class="step-item" :class="{ completed: isStepCompleted(dh, step), active: isStepActive(dh, step) }">
                <div class="step-circle">
                  <el-icon v-if="isStepCompleted(dh, step)">
                    <Check/>
                  </el-icon>
                  <div v-else class="inner-dot"></div>
                </div>
                <div class="step-label">{{ step }}</div>
              </div>
              <div v-if="idx < STEPS.length - 1" class="step-line"
                   :class="{ completed: isLineCompleted(dh, STEPS[idx + 1]) }"></div>
            </template>
          </div>
        </div>

        <!-- Thông tin -->
        <div class="card-info">
          <div class="info-row">
            <el-icon>
              <User/>
            </el-icon>
            <span>Khách hàng:</span>
            <strong>{{ dh.tenKhachHang }}</strong>
          </div>
          <div class="info-row">
            <el-icon>
              <Avatar/>
            </el-icon>
            <span>Nhân viên phụ trách:</span>
            <strong>{{ dh.tenNhanVien || 'Không có' }}</strong>
          </div>
          <div class="info-row">
            <el-icon>
              <Wallet/>
            </el-icon>
            <span>Phương thức thanh toán:</span>
            <strong>{{ dh.phuongThucThanhToan || 'Không có' }}</strong>
          </div>
          <div class="info-row">
            <el-icon>
              <Calendar/>
            </el-icon>
            <span>Ngày tạo:</span>
            <strong>{{ formatDate(dh.ngayTaoDon || dh.NgayTaoDon) }}</strong>
          </div>
        </div>

        <!-- Actions -->
        <div class="card-actions">
          <button class="btn-outline-green" @click="xemChiTiet(dh)">
            <el-icon><EditPen /></el-icon>
            Sửa
          </button>

          <button class="btn-outline-red" @click="huyDon(dh.maDonHang || dh.MaDonHang)">
            <el-icon><Delete /></el-icon>
            Hủy
          </button>

          <button
              v-if="canTaoHoaDon(dh)"
              class="btn-invoice"
              @click="taoHoaDon(dh)"
          >
            <el-icon><Tickets /></el-icon>
            Tạo hóa đơn
          </button>

          <button
              v-else-if="daCoHoaDon(dh)"
              class="btn-invoice-created"
              @click="xemHoaDon(dh)"
          >
            <el-icon><View /></el-icon>
            Xem hóa đơn
          </button>

          <button
              v-if="nextStatus(dh)"
              class="btn-filled-green"
              @click="updateNextStatus(dh)"
          >
            {{ dh.trangThai === 'Chờ thanh toán' ? 'Thanh toán' : 'Cập nhật trạng thái tiếp theo' }}
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

    <PopTaoHoaDon
        v-model="showTaoHoaDon"
        :don-hang="selectedDonHangHoaDon"
        @submit="handleTaoHoaDonSubmit"
    />

    <!-- ── Popup chi tiết đơn hàng ── -->
    <PopChiTietDonHang
        v-model="showChiTiet"
        :don-hang="selectedDonHang"
        @huy-don="huyDon"
        @cap-nhat="loadDonHangs"
    />

    <!-- ── Popup thanh toán QR ── -->
    <el-dialog
        v-model="showPaymentDialog"
        title="Thanh toán chuyển khoản"
        width="400px"
        center
        :append-to-body="true"
        :z-index="10060"
    >
      <div style="text-align: center;">
        <p>Vui lòng quét mã QR bên dưới để thanh toán cho đơn hàng <strong>#{{
            selectedOrderForPayment?.maCode
          }}</strong></p>
        <img
            v-if="selectedOrderForPayment"
            :src="`https://img.vietqr.io/image/MB-140213032008-compact.png?addInfo=${selectedOrderForPayment.maCode}`"
            alt="QR Code Thanh Toán"
            style="max-width: 100%; border-radius: 8px; margin: 20px 0;"
        />
      </div>
      <template #footer>
            <span class="dialog-footer">
                <el-button @click="showPaymentDialog = false">Hủy</el-button>
                <el-button type="primary" @click="confirmPayment">
                    Xác nhận đã thanh toán
                </el-button>
            </span>
      </template>
    </el-dialog>

    <!-- ── Popup xác nhận thanh toán tiền mặt ── -->
    <el-dialog
        v-model="showCashConfirmDialog"
        title="Xác nhận thanh toán"
        width="400px"
        center
        :append-to-body="true"
        :z-index="10060"
    >
      <div style="text-align: center; padding: 20px 0;">
        <p style="font-size: 16px;">Bạn có chắc chắn khách đã thanh toán đủ?</p>
      </div>
      <template #footer>
            <span class="dialog-footer">
                <el-button @click="showCashConfirmDialog = false">Hủy</el-button>
                <el-button type="primary" @click="confirmCashPayment">
                    Xác nhận
                </el-button>
            </span>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped src="../../assets/styles/nhanvien/QLDonHang/TrangQLDonHang.css"></style>