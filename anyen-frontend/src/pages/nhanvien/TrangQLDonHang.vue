<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

import PopTaoDonHang from "./PopTaoDonHang.vue";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
import PopTaoHoaDon from "./PopTaoHoaDon.vue";

import {
  getDonHangs,
  taoDonHang,
  capNhatDonHang,
  capNhatTrangThai,
  huyDonHang as huyDonHangAPI,
  kiemTraDonHangCoHopDong,
  formatDate,
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
  Plus,
  Tickets,
  View,
} from "@element-plus/icons-vue";

// ── Trạng thái popup ─────────────────────────────
const showCreateOrder = ref(false);
const showChiTiet = ref(false);
const selectedDonHang = ref(null);

const showPaymentDialog = ref(false);
const showCashConfirmDialog = ref(false);
const selectedOrderForPayment = ref(null);

const showTaoHoaDon = ref(false);
const selectedDonHangHoaDon = ref(null);
const hoaDonMode = ref("create");

// ── Bộ lọc ─────────────────────────────
const keyword = ref("");
const trangThaiFilter = ref("Tất cả");
const ptThanhToanFilter = ref("Tất cả");
const dateRange = ref([]);

const currentPage = ref(1);
const pageSize = ref(3);

// ── Dữ liệu ─────────────────────────────
const donHangs = ref([]);
const loading = ref(false);

const loadDonHangs = async () => {
  try {
    loading.value = true;

    const data = await getDonHangs();

    donHangs.value = Array.isArray(data)
        ? data
        : data?.items || [];
  } catch (error) {
    console.error("Lỗi khi tải đơn hàng:", error);
    ElMessage.error("Không thể tải danh sách đơn hàng");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadDonHangs();
});

// ── Helper mã đơn hàng ─────────────────────────────
const getMaDonHang = (dh) => {
  return dh?.maDonHang || dh?.MaDonHang || dh?.id;
};

// ── Hủy đơn hàng ─────────────────────────────
const showCancelDialog = ref(false);
const selectedCancelOrder = ref(null);
const cancelReason = ref("");

const isCancelReasonValid = computed(() => {
  return cancelReason.value.trim().length > 3;
});

const canCancelOrder = (order) => {
  const status = order?.trangThai || order?.TrangThai || "";
  return status !== "Đã hủy" && status !== "Hoàn thành";
};

const openCancelDialog = (order) => {
  selectedCancelOrder.value = order;
  cancelReason.value = "";
  showCancelDialog.value = true;
};

const closeCancelDialog = () => {
  showCancelDialog.value = false;
  selectedCancelOrder.value = null;
  cancelReason.value = "";
};

const confirmCancelOrder = async () => {
  if (!selectedCancelOrder.value) {
    ElMessage.warning("Chưa chọn đơn hàng cần hủy");
    return;
  }

  if (!isCancelReasonValid.value) {
    ElMessage.warning("Lý do hủy phải trên 3 ký tự");
    return;
  }

  try {
    const maDonHang = getMaDonHang(selectedCancelOrder.value);

    await huyDonHangAPI(maDonHang, cancelReason.value.trim());

    ElMessage.success("Hủy đơn hàng thành công");

    closeCancelDialog();
    showChiTiet.value = false;

    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi hủy đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Hủy đơn hàng thất bại"
    );
  }
};

// Hàm này dùng cho popup chi tiết nếu PopChiTietDonHang emit @huy-don
const huyDon = (orderOrId) => {
  if (typeof orderOrId === "object") {
    openCancelDialog(orderOrId);
    return;
  }

  const order = selectedDonHang.value || {
    maDonHang: orderOrId,
  };

  openCancelDialog(order);
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

// ── Chi tiết / sửa đơn hàng ─────────────────────────────
const hienThongBaoDonDaCoHopDong = async (dh) => {
  await ElMessageBox.alert(
      `Đơn hàng ${dh?.maCode ? "#" + dh.maCode : "này"} đã ký hợp đồng, không thể chỉnh sửa.\n\nNếu có sai sót thông tin vui lòng hủy đơn hàng và hủy hợp đồng.`,
      "Không thể chỉnh sửa đơn hàng",
      {
        confirmButtonText: "Đã hiểu",
        type: "warning",
      }
  );
};

const xemChiTiet = async (dh) => {
  try {
    const maDonHang = getMaDonHang(dh);

    if (!maDonHang) {
      ElMessage.error("Không tìm thấy mã đơn hàng");
      return;
    }

    if (dh.daCoHopDong === true) {
      await hienThongBaoDonDaCoHopDong(dh);
      return;
    }

    const daCoHopDong = await kiemTraDonHangCoHopDong(maDonHang);

    if (daCoHopDong) {
      dh.daCoHopDong = true;
      await hienThongBaoDonDaCoHopDong(dh);
      return;
    }

    selectedDonHang.value = JSON.parse(JSON.stringify(dh));
    showChiTiet.value = true;
  } catch (error) {
    console.error("Lỗi khi kiểm tra hợp đồng của đơn hàng:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Không kiểm tra được hợp đồng của đơn hàng"
    );
  }
};

const buildUpdateOrderPayload = (order) => {
  const sanPhams = order?.sanPhams || [];

  return {
    maKhachHang: order?.MaKhachHang || order?.maKhachHang || null,
    tenKhachHang: order?.tenKhachHang || "",
    soDienThoai: order?.soDienThoaiKH || order?.soDienThoai || "",
    cccd: order?.cccd || order?.CCCD || "",
    email: order?.emailKH || order?.email || "",
    diaChi: order?.diaChiKH || order?.diaChi || "",
    ghiChu: order?.GhiChu || order?.ghiChuNoiBo || order?.ghiChu || "",
    phuongThucThanhToan: order?.phuongThucThanhToan || "Chưa chọn",
    trangThaiThanhToan: order?.trangThaiThanhToan || "Chưa thanh toán",
    items: sanPhams.map((sp) => ({
      maSanPham: sp.MaSanPham || sp.maSanPham,
      soLuong: Number(sp.SoLuong || sp.soLuong || 1),
    })),
  };
};

const handleUpdateOrder = async (order) => {
  try {
    const maDonHang = getMaDonHang(order);

    if (!maDonHang) {
      ElMessage.error("Không tìm thấy mã đơn hàng");
      return;
    }

    const payload = buildUpdateOrderPayload(order);

    if (!payload.items || payload.items.length === 0) {
      ElMessage.error("Đơn hàng phải có ít nhất 1 sản phẩm");
      return;
    }

    await capNhatDonHang(maDonHang, payload);

    ElMessage.success("Cập nhật đơn hàng thành công");

    showChiTiet.value = false;
    selectedDonHang.value = null;

    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi cập nhật đơn hàng:", error);

    const message =
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật đơn hàng thất bại";

    if (error.response?.status === 409) {
      await ElMessageBox.alert(message, "Không thể chỉnh sửa đơn hàng", {
        confirmButtonText: "Đã hiểu",
        type: "warning",
      });

      showChiTiet.value = false;
      selectedDonHang.value = null;

      await loadDonHangs();
      return;
    }

    ElMessage.error(message);
  }
};

// ── Hóa đơn ─────────────────────────────
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
  hoaDonMode.value = "create";
  selectedDonHangHoaDon.value = dh;
  showTaoHoaDon.value = true;
};

const xemHoaDon = (dh) => {
  if (!dh) {
    ElMessage.warning("Không tìm thấy dữ liệu đơn hàng");
    return;
  }

  hoaDonMode.value = "view";
  selectedDonHangHoaDon.value = {
    ...dh,
    trangThaiHoaDon: dh.trangThaiHoaDon || dh.trangThaiHoaDon || "Đã in",
  };
  showTaoHoaDon.value = true;
};

// ── Stepper trạng thái ─────────────────────────────
const STEPS = [
  "Mới tạo",
  "Chờ đối tác xác nhận",
  "Đã xác nhận",
  "Đang xử lý",
  "Chờ thanh toán",
  "Hoàn thành",
];

const getStepIndex = (trangThai) => {
  return STEPS.indexOf(trangThai);
};

const isStepCompleted = (dh, stepName) => {
  if (dh.trangThai === "Đã hủy") return false;

  const currentIdx = getStepIndex(dh.trangThai);
  const targetIdx = getStepIndex(stepName);

  return currentIdx >= 0 && targetIdx < currentIdx;
};

const isStepActive = (dh, stepName) => {
  return dh.trangThai === stepName;
};

const isLineCompleted = (dh, targetStep) => {
  return isStepCompleted(dh, targetStep) || isStepActive(dh, targetStep);
};

const nextStatus = (dh) => {
  const currentIdx = getStepIndex(dh.trangThai);

  if (
      currentIdx >= 0 &&
      currentIdx < STEPS.length - 1 &&
      dh.trangThai !== "Đã hủy"
  ) {
    return STEPS[currentIdx + 1];
  }

  return null;
};

// ── Badge class ─────────────────────────────
const trangThaiBadgeClass = (tt) => {
  if (tt === "Mới tạo") return "badge-yellow";
  if (tt === "Chờ đối tác xác nhận") return "badge-pink";
  if (tt === "Đã xác nhận") return "badge-blue";
  if (tt === "Đang xử lý") return "badge-orange";
  if (tt === "Chờ thanh toán") return "badge-purple";
  if (tt === "Hoàn thành") return "badge-green";
  if (tt === "Đã hủy") return "badge-red";
  return "badge-gray";
};

// ── Tạo đơn hàng ─────────────────────────────
const handleSaveDraft = (payload) => {
  console.log("Tạm lưu đơn hàng:", payload);
  ElMessage.success("Đã tạm lưu đơn hàng");
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

// ── Cập nhật trạng thái ─────────────────────────────
const doUpdateStatus = async (dh, next) => {
  try {
    await capNhatTrangThai(getMaDonHang(dh), next);

    ElMessage.success(`Đã cập nhật trạng thái: ${next}`);

    await loadDonHangs();
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);

    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật trạng thái thất bại"
    );
  }
};

const updateNextStatus = async (dh) => {
  const next = nextStatus(dh);

  if (!next) return;

  if (dh.trangThai === "Chờ thanh toán") {
    if (dh.phuongThucThanhToan === "Chuyển khoản") {
      selectedOrderForPayment.value = dh;
      showPaymentDialog.value = true;
      return;
    }

    if (dh.phuongThucThanhToan === "Tiền mặt") {
      selectedOrderForPayment.value = dh;
      showCashConfirmDialog.value = true;
      return;
    }
  }

  await doUpdateStatus(dh, next);
};

const confirmPayment = async () => {
  if (!selectedOrderForPayment.value) return;

  const order = selectedOrderForPayment.value;

  await doUpdateStatus(order, "Hoàn thành");

  showPaymentDialog.value = false;
  selectedOrderForPayment.value = null;

  xemHoaDon({
    ...order,
    trangThai: "Hoàn thành",
    phuongThucThanhToan: "Chuyển khoản",
  });
};

const confirmCashPayment = async () => {
  if (!selectedOrderForPayment.value) return;

  const order = selectedOrderForPayment.value;

  await doUpdateStatus(order, "Hoàn thành");

  showCashConfirmDialog.value = false;
  selectedOrderForPayment.value = null;

  xemHoaDon({
    ...order,
    trangThai: "Hoàn thành",
    phuongThucThanhToan: "Tiền mặt",
  });
};
</script>

<template>
  <div class="don-hang-page">
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
          <option>Chờ đối tác xác nhận</option>
          <option>Đã xác nhận</option>
          <option>Đang xử lý</option>
          <option>Chờ thanh toán</option>
          <option>Hoàn thành</option>
          <option>Đã hủy</option>
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
          class="btn-create-order btn-tao"
          @click="showCreateOrder = true"
      >
        <el-icon>
          <Plus />
        </el-icon>
        Tạo đơn hàng
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

        <!-- Actions -->
        <div class="card-actions">
          <button
              class="btn-outline-green"
              @click="xemChiTiet(dh)"
          >
            <el-icon>
              <EditPen />
            </el-icon>
            Sửa
          </button>

          <button
              v-if="canCancelOrder(dh)"
              class="btn-outline-red"
              @click="openCancelDialog(dh)"
          >
            <el-icon>
              <Delete />
            </el-icon>
            Hủy
          </button>

          <button
              class="btn-invoice-created"
              @click.stop="xemHoaDon(dh)"
          >
            <el-icon>
              <View />
            </el-icon>
            Xem hóa đơn
          </button>

          <button
              v-if="nextStatus(dh)"
              class="btn-filled-green"
              @click="updateNextStatus(dh)"
          >
            {{ dh.trangThai === "Chờ thanh toán" ? "Thanh toán" : "Cập nhật" }}
          </button>

          <button
              v-else
              class="btn-disabled"
          >
            Hoàn tất
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

    <!-- ── Popup tạo đơn hàng ── -->
    <PopTaoDonHang
        v-if="showCreateOrder"
        @close="showCreateOrder = false"
        @submit="handleCreateOrder"
        @save-draft="handleSaveDraft"
    />

    <!-- ── Popup tạo / xem hóa đơn ── -->
    <PopTaoHoaDon
        v-model="showTaoHoaDon"
        :don-hang="selectedDonHangHoaDon"
        :mode="hoaDonMode"
        @created="loadDonHangs"
    />

    <!-- ── Popup chi tiết / sửa đơn hàng ── -->
    <PopChiTietDonHang
        v-model="showChiTiet"
        :don-hang="selectedDonHang"
        @huy-don="huyDon"
        @dong="showChiTiet = false"
        @luu="handleUpdateOrder"
        @cap-nhat="loadDonHangs"
    />

    <!-- ── Popup hủy đơn hàng ── -->
    <el-dialog
        v-model="showCancelDialog"
        title="Lý do hủy đơn hàng"
        width="460px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :z-index="10070"
    >
      <div class="cancel-dialog-content">
        <p class="cancel-warning">
          Bạn cần nhập lý do hủy đơn hàng. Lý do phải trên 3 ký tự.
        </p>

        <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="Nhập lý do hủy đơn hàng..."
        />

        <p
            v-if="
            cancelReason.trim().length > 0 &&
            cancelReason.trim().length <= 3
          "
            class="cancel-error"
        >
          Lý do hủy phải trên 3 ký tự.
        </p>
      </div>

      <template #footer>
        <el-button @click="closeCancelDialog">
          Đóng
        </el-button>

        <el-button
            type="danger"
            :disabled="!isCancelReasonValid"
            @click="confirmCancelOrder"
        >
          Xác nhận hủy
        </el-button>
      </template>
    </el-dialog>

    <!-- ── Popup thanh toán QR ── -->
    <el-dialog
        v-model="showPaymentDialog"
        title="Thanh toán chuyển khoản"
        width="400px"
        center
        :append-to-body="true"
        :z-index="10060"
    >
      <div style="text-align: center">
        <p>
          Vui lòng quét mã QR bên dưới để thanh toán cho đơn hàng
          <strong>#{{ selectedOrderForPayment?.maCode }}</strong>
        </p>

        <img
            v-if="selectedOrderForPayment"
            :src="`https://img.vietqr.io/image/MB-140213032008-compact.png?addInfo=${selectedOrderForPayment.maCode}`"
            alt="QR Code Thanh Toán"
            style="max-width: 100%; border-radius: 8px; margin: 20px 0"
        />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPaymentDialog = false">
            Hủy
          </el-button>

          <el-button
              type="primary"
              @click="confirmPayment"
          >
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
      <div style="text-align: center; padding: 20px 0">
        <p style="font-size: 16px">
          Bạn có chắc chắn khách đã thanh toán đủ?
        </p>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCashConfirmDialog = false">
            Hủy
          </el-button>

          <el-button
              type="primary"
              @click="confirmCashPayment"
          >
            Xác nhận
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/TrangQLDonHang.css"></style>