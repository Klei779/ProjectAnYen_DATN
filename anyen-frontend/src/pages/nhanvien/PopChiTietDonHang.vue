<script setup>
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  formatCurrency,
  formatDate,
  capNhatTrangThai,
  getSanPhamTaoDonHang,
} from "../../services/donHangService.js";

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  donHang: {
    type: Object,
    default: null,
  },
  readonly: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits([
  "update:modelValue",
  "huy-don",
  "dong",
  "luu",
  "cap-nhat",
]);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const order = ref(null);

const showPaymentDialog = ref(false);
const showCashConfirmDialog = ref(false);
const showAddProductDialog = ref(false);

const activeTab = ref("info");

const loadingSanPham = ref(false);
const keywordSanPham = ref("");
const sanPhamOptions = ref([]);

const cloneData = (data) => {
  if (!data) return null;
  return JSON.parse(JSON.stringify(data));
};

const normalizeSanPham = (sp) => {
  const maSanPham = sp.MaSanPham || sp.maSanPham;
  const soLuong = Number(sp.SoLuong || sp.soLuong || 1);
  const giaTien = Number(sp.giaTien || sp.GiaTien || 0);
  const hinhAnh = sp.HinhAnh || sp.hinhAnh || sp.HinhAnhUrl || sp.hinhAnhUrl || "";

  return {
    ...sp,
    MaSanPham: maSanPham,
    maSanPham,
    SoLuong: soLuong,
    soLuong,
    giaTien,
    HinhAnh: hinhAnh,
    hinhAnh,
    thanhTien: soLuong * giaTien,
  };
};

const normalizeSanPhamOption = (sp) => {
  const maSanPham = sp.MaSanPham || sp.maSanPham;
  const giaTien = Number(sp.giaTien || sp.GiaTien || 0);
  const soLuongTon = Number(
      sp.soLuongTon ||
      sp.tonKho ||
      sp.SoLuong ||
      sp.soLuong ||
      0
  );

  return {
    ...sp,
    MaSanPham: maSanPham,
    maSanPham,
    tenSanPham: sp.tenSanPham || sp.TenSanPham || "",
    phanLoai: sp.phanLoai || sp.loai || sp.Loai || "",
    HinhAnh: sp.HinhAnh || sp.hinhAnh || sp.hinhanh || "",
    giaTien,
    SoLuong: soLuongTon,
    soLuongTon,
  };
};

const normalizeOrder = (data) => {
  if (!data) return null;

  const cloned = cloneData(data);

  cloned.MaDonHang = cloned.MaDonHang || cloned.maDonHang;
  cloned.maDonHang = cloned.maDonHang || cloned.MaDonHang;
  cloned.MaKhachHang = cloned.MaKhachHang || cloned.maKhachHang;
  cloned.maKhachHang = cloned.maKhachHang || cloned.MaKhachHang;

  cloned.maCode =
      cloned.maCode ||
      `DH${String(cloned.MaDonHang || "").padStart(4, "0")}`;

  cloned.NgayTaoDon = cloned.NgayTaoDon || cloned.ngayTaoDon;

  cloned.tenKhachHang = cloned.tenKhachHang || "";
  cloned.soDienThoaiKH = cloned.soDienThoaiKH || cloned.soDienThoai || "";
  cloned.emailKH = cloned.emailKH || cloned.email || "";
  cloned.diaChiKH = cloned.diaChiKH || cloned.diaChi || "";
  cloned.cccd = cloned.cccd || cloned.CCCD || "";

  cloned.GhiChu = cloned.GhiChu || cloned.ghiChuNoiBo || cloned.ghiChu || "";

  cloned.phuongThucThanhToan =
      cloned.phuongThucThanhToan || "Chưa chọn";

  cloned.trangThaiThanhToan =
      cloned.trangThaiThanhToan || "Chưa thanh toán";

  cloned.phiVanChuyen = Number(cloned.phiVanChuyen || 0);
  cloned.giamGia = Number(cloned.giamGia || 0);

  cloned.sanPhams = Array.isArray(cloned.sanPhams)
      ? cloned.sanPhams.map(normalizeSanPham)
      : [];

  return cloned;
};

watch(
    () => props.donHang,
    (newVal) => {
      order.value = normalizeOrder(newVal);
      activeTab.value = "info";
      
      // Debug: Log sản phẩm để kiểm tra hình ảnh
      if (order.value?.sanPhams) {
        console.log('Sản phẩm trong đơn hàng:', order.value.sanPhams.map(sp => ({
          ten: sp.tenSanPham,
          HinhAnh: sp.HinhAnh,
          hinhAnh: sp.hinhAnh,
          HinhAnhUrl: sp.HinhAnhUrl,
          hinhAnhUrl: sp.hinhAnhUrl
        })));
      }
    },
    {
      immediate: true,
    }
);

const handleImageError = (event) => {
  event.target.style.display = 'none';
  const placeholder = event.target.nextElementSibling;
  if (placeholder && placeholder.classList.contains('img-placeholder')) {
    placeholder.style.display = 'block';
  }
};

const tamTinh = computed(() => {
  if (!order.value?.sanPhams) return 0;

  return order.value.sanPhams.reduce((sum, sp) => {
    const soLuong = Number(sp.SoLuong || 0);
    const giaTien = Number(sp.giaTien || 0);
    return sum + soLuong * giaTien;
  }, 0);
});

const tongCong = computed(() => {
  if (!order.value) return 0;

  return (
      tamTinh.value -
      Number(order.value.giamGia || 0) +
      Number(order.value.phiVanChuyen || 0)
  );
});

const STEPS = [
  "Mới tạo",
  "Xác nhận",
  "Đã nhận",
  "Xử lý",
  "Đã giao",
  "Thanh toán",
  "Hoàn thành",
];

const normalizeTrangThai = (trangThai) => {
  if (!trangThai) return "Mới tạo";
  
  // Nếu là số, chuyển sang text
  if (typeof trangThai === 'number') {
    const statusMap = {
      1: "Mới tạo",
      2: "Xác nhận",
      3: "Đã nhận",
      4: "Xử lý",
      5: "Đã giao",
      6: "Thanh toán",
      7: "Hoàn thành",
      0: "Đã hủy"
    };
    return statusMap[trangThai] || "Mới tạo";
  }
  
  // Nếu là text, chuẩn hóa
  const status = String(trangThai).trim().toLowerCase();
  
  if (status.includes("mới") || status.includes("tạo")) return "Mới tạo";
  if (status.includes("xác nhận")) return "Xác nhận";
  if (status.includes("đã nhận")) return "Đã nhận";
  if (status.includes("xử lý") || status.includes("đang xử lý")) return "Xử lý";
  if (status.includes("đã giao")) return "Đã giao";
  if (status.includes("thanh toán")) return "Thanh toán";
  if (status.includes("hoàn thành")) return "Hoàn thành";
  if (status.includes("hủy")) return "Đã hủy";
  
  return trangThai;
};

const getStepIndex = (trangThai) => {
  const normalized = normalizeTrangThai(trangThai);
  console.log('Trạng thái gốc:', trangThai, '-> Normalized:', normalized);
  return STEPS.indexOf(normalized);
};

const lichSuArr = computed(() => {
  if (!order.value) return [];

  const normalizedStatus = normalizeTrangThai(order.value.trangThai);
  const currentIdx = getStepIndex(normalizedStatus);
  const isDaHuy = normalizedStatus === "Đã hủy";

  return STEPS.map((step, idx) => {
    const backendStep = order.value.lichSu?.find(
        (ls) => ls.trangThai === step
    );

    const isDone = !isDaHuy && currentIdx >= 0 && idx <= currentIdx;
    const isActive = !isDaHuy && currentIdx >= 0 && idx === currentIdx;

    return {
      title: step,
      time: backendStep?.thoiGian || null,
      desc: backendStep?.moTa || (isDone ? "Đã hoàn thành" : "Chưa cập nhật"),
      isDone,
      isActive,
    };
  });
});

const currentStatus = computed(() => order.value?.trangThai || "");

const nextStatus = computed(() => {
  if (!order.value) return null;

  if (currentStatus.value === "Đã hủy") return null;
  if (currentStatus.value === "Hoàn thành") return null;

  const currentIdx = getStepIndex(currentStatus.value);

  if (currentIdx >= 0 && currentIdx < STEPS.length - 1) {
    return STEPS[currentIdx + 1];
  }

  return null;
});

const updateThanhTien = (sp) => {
  sp.SoLuong = Number(sp.SoLuong || 1);
  sp.soLuong = sp.SoLuong;
  sp.giaTien = Number(sp.giaTien || 0);
  sp.thanhTien = sp.SoLuong * sp.giaTien;
};

const increaseQty = (sp) => {
  sp.SoLuong = Number(sp.SoLuong || 1) + 1;
  updateThanhTien(sp);
};

const decreaseQty = (sp) => {
  const currentQty = Number(sp.SoLuong || 1);

  if (currentQty <= 1) return;

  sp.SoLuong = currentQty - 1;
  updateThanhTien(sp);
};

const removeSp = (sp) => {
  if (!order.value?.sanPhams) return;

  order.value.sanPhams = order.value.sanPhams.filter(
      (item) => Number(item.MaSanPham) !== Number(sp.MaSanPham)
  );
};

const loadSanPhamOptions = async () => {
  try {
    loadingSanPham.value = true;

    const data = await getSanPhamTaoDonHang();

    sanPhamOptions.value = Array.isArray(data)
        ? data.map(normalizeSanPhamOption)
        : [];
  } catch (error) {
    console.error("Lỗi khi tải sản phẩm:", error);
    ElMessage.error("Không thể tải danh sách sản phẩm");
  } finally {
    loadingSanPham.value = false;
  }
};

const openAddProductDialog = async () => {
  keywordSanPham.value = "";
  showAddProductDialog.value = true;

  if (sanPhamOptions.value.length === 0) {
    await loadSanPhamOptions();
  }
};

const filteredSanPhamOptions = computed(() => {
  const kw = keywordSanPham.value.trim().toLowerCase();

  return sanPhamOptions.value.filter((sp) => {
    const existed = order.value?.sanPhams?.some(
        (item) => Number(item.MaSanPham) === Number(sp.MaSanPham)
    );

    if (existed) return false;

    if (!kw) return true;

    return (
        sp.tenSanPham.toLowerCase().includes(kw) ||
        sp.phanLoai.toLowerCase().includes(kw)
    );
  });
});

const addSanPhamToOrder = (sp) => {
  if (!order.value) return;

  if (!order.value.sanPhams) {
    order.value.sanPhams = [];
  }

  const existed = order.value.sanPhams.find(
      (item) => Number(item.MaSanPham) === Number(sp.MaSanPham)
  );

  if (existed) {
    ElMessage.warning("Sản phẩm đã có trong đơn hàng");
    return;
  }

  if (Number(sp.soLuongTon || 0) <= 0) {
    ElMessage.warning("Sản phẩm đã hết hàng");
    return;
  }

  order.value.sanPhams.push({
    MaSanPham: sp.MaSanPham,
    maSanPham: sp.MaSanPham,
    tenSanPham: sp.tenSanPham,
    phanLoai: sp.phanLoai,
    HinhAnh: sp.HinhAnh,
    giaTien: Number(sp.giaTien || 0),
    SoLuong: 1,
    soLuong: 1,
    thanhTien: Number(sp.giaTien || 0),
  });

  ElMessage.success("Đã thêm sản phẩm vào đơn hàng");
  showAddProductDialog.value = false;
};

const handleHuy = () => {
  emit("dong");
  emit("update:modelValue", false);
};

const validateBeforeSave = () => {
  if (!order.value) {
    ElMessage.error("Không tìm thấy dữ liệu đơn hàng");
    return false;
  }

  if (!order.value.tenKhachHang?.trim()) {
    ElMessage.error("Tên khách hàng không được để trống");
    activeTab.value = "info";
    return false;
  }

  if (!order.value.soDienThoaiKH?.trim()) {
    ElMessage.error("Số điện thoại không được để trống");
    activeTab.value = "info";
    return false;
  }

  if (!order.value.sanPhams || order.value.sanPhams.length === 0) {
    ElMessage.error("Đơn hàng phải có ít nhất 1 sản phẩm");
    activeTab.value = "products";
    return false;
  }

  const invalidItem = order.value.sanPhams.find(
      (sp) => !sp.MaSanPham || Number(sp.SoLuong || 0) <= 0
  );

  if (invalidItem) {
    ElMessage.error("Số lượng sản phẩm phải lớn hơn 0");
    activeTab.value = "products";
    return false;
  }

  return true;
};

const handleLuu = () => {
  if (!validateBeforeSave()) return;

  const payload = cloneData(order.value);

  payload.tongTien = tongCong.value;
  payload.sanPhams = payload.sanPhams.map((sp) => ({
    ...sp,
    SoLuong: Number(sp.SoLuong || 1),
    soLuong: Number(sp.SoLuong || 1),
    thanhTien: Number(sp.SoLuong || 1) * Number(sp.giaTien || 0),
  }));

  emit("luu", payload);
};

const getOrderId = () => {
  return order.value?.maDonHang || order.value?.MaDonHang;
};

const confirmPayment = async () => {
  if (!order.value) return;

  try {
    await capNhatTrangThai(getOrderId(), "Hoàn thành");

    ElMessage.success("Đã cập nhật trạng thái: Hoàn thành");

    order.value.trangThai = "Hoàn thành";
    showPaymentDialog.value = false;

    emit("cap-nhat");
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật trạng thái thất bại"
    );
  }
};

const confirmCashPayment = async () => {
  if (!order.value) return;

  try {
    await capNhatTrangThai(getOrderId(), "Hoàn thành");

    ElMessage.success("Đã cập nhật trạng thái: Hoàn thành");

    order.value.trangThai = "Hoàn thành";
    showCashConfirmDialog.value = false;

    emit("cap-nhat");
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật trạng thái thất bại"
    );
  }
};

const capNhatTrangThaiTiep = async () => {
  if (!order.value || !nextStatus.value) return;

  if (currentStatus.value === "Chờ thanh toán") {
    if (order.value.phuongThucThanhToan === "Chuyển khoản") {
      showPaymentDialog.value = true;
      return;
    }

    if (order.value.phuongThucThanhToan === "Tiền mặt") {
      showCashConfirmDialog.value = true;
      return;
    }
  }

  try {
    await capNhatTrangThai(getOrderId(), nextStatus.value);

    ElMessage.success(`Đã cập nhật trạng thái: ${nextStatus.value}`);

    order.value.trangThai = nextStatus.value;

    emit("cap-nhat");
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
    ElMessage.error(
        error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật trạng thái thất bại"
    );
  }
};
</script>

<template>
  <el-dialog
      v-model="visible"
      width="90%"
      top="20px"
      class="chi-tiet-dialog"
      :show-close="true"
      destroy-on-close
      :z-index="10050"
  >
    <template #header>
      <div class="dialog-title-new">
        {{ readonly ? 'Xem chi tiết đơn hàng' : 'Sửa đơn hàng' }} #{{ order?.maCode }}
      </div>
    </template>

    <el-tabs
        v-if="order"
        v-model="activeTab"
        class="order-tabs mobile-layout"
    >
      <el-tab-pane label="Thông tin" name="info">
        <div class="col-panel info-col">
          <div class="info-section">
            <h4 class="section-title">Thông tin đơn hàng</h4>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-barcode"></i>
                Mã đơn hàng:
              </div>
              <div class="info-val bold">#{{ order.maCode }}</div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-calendar-days"></i>
                Ngày tạo:
              </div>
              <div class="info-val">
                {{ formatDate(order.NgayTaoDon) }}
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-wallet"></i>
                Phương thức thanh toán:
              </div>
              <div class="info-val">
                <el-select
                    v-model="order.phuongThucThanhToan"
                    placeholder="Chọn phương thức"
                    style="width: 100%"
                    :disabled="readonly"
                >
                  <el-option label="Chưa chọn" value="Chưa chọn" />
                  <el-option label="Tiền mặt" value="Tiền mặt" />
                  <el-option label="Chuyển khoản" value="Chuyển khoản" />
                </el-select>
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-money-check"></i>
                Trạng thái thanh toán:
              </div>
              <div class="info-val">
                <el-select
                    v-model="order.trangThaiThanhToan"
                    placeholder="Chọn trạng thái"
                    style="width: 100%"
                    :disabled="readonly"
                >
                  <el-option label="Chưa thanh toán" value="Chưa thanh toán" />
                  <el-option label="Đã đặt cọc" value="Đã đặt cọc" />
                  <el-option label="Đã thanh toán" value="Đã thanh toán" />
                </el-select>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="info-section">
            <h4 class="section-title">Thông tin khách hàng</h4>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-user"></i>
                Họ tên:
              </div>
              <div class="info-val">
                <el-input
                    v-model="order.tenKhachHang"
                    maxlength="30"
                    show-word-limit
                    placeholder="Nhập họ tên khách hàng"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-phone"></i>
                SĐT:
              </div>
              <div class="info-val">
                <el-input
                    v-model="order.soDienThoaiKH"
                    maxlength="10"
                    placeholder="Nhập số điện thoại"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-envelope"></i>
                Email:
              </div>
              <div class="info-val">
                <el-input
                    v-model="order.emailKH"
                    placeholder="Nhập email"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-location-dot"></i>
                Địa chỉ:
              </div>
              <div class="info-val address-val">
                <el-input
                    v-model="order.diaChiKH"
                    maxlength="40"
                    show-word-limit
                    placeholder="Nhập địa chỉ"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-note-sticky"></i>
                Ghi chú:
              </div>
              <div class="info-val address-val">
                <el-input
                    v-model="order.GhiChu"
                    type="textarea"
                    :rows="3"
                    placeholder="Nhập ghi chú đơn hàng"
                />
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="info-section">
            <h4 class="section-title">Nhân viên phụ trách</h4>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-user-tie"></i>
                Họ tên:
              </div>
              <div class="info-val">
                {{ order.tenNhanVien || "Chưa cập nhật" }}
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Tiến trình" name="progress">
        <div class="col-panel progress-col">
          <h4 class="section-title">Tiến trình đơn hàng</h4>

          <div class="vertical-stepper">
            <div
                v-for="(step, idx) in lichSuArr"
                :key="idx"
                class="v-step"
                :class="{ done: step.isDone, active: step.isActive }"
            >
              <div class="v-step-indicator">
                <div class="v-step-circle">
                  <span v-if="step.isDone && !step.isActive">✓</span>

                  <span v-else-if="step.isActive">
                    <i class="fa-solid fa-clock"></i>
                  </span>

                  <span v-else>{{ idx + 1 }}</span>
                </div>

                <div
                    v-if="idx < lichSuArr.length - 1"
                    class="v-step-line"
                ></div>
              </div>

              <div class="v-step-content">
                <div class="v-step-title">
                  {{ step.title }}
                </div>

                <div v-if="step.time" class="v-step-time">
                  {{ step.time }}
                </div>

                <div class="v-step-desc">
                  {{ step.desc }}
                </div>
              </div>
            </div>
          </div>

          <div class="progress-actions">
            <button
                v-if="nextStatus && !readonly"
                class="btn-capnhat-tt"
                @click="capNhatTrangThaiTiep"
            >
              <i class="fa-solid fa-arrow-right"></i>
              {{
                currentStatus === "Chờ thanh toán"
                    ? "Thanh toán"
                    : "Cập nhật trạng thái tiếp theo"
              }}
            </button>

            <div v-if="nextStatus && !readonly" class="status-note">
              Trạng thái hiện tại:
              <strong>{{ currentStatus }}</strong>
              <br />
              Nhấn để chuyển sang trạng thái:
              <strong>{{ nextStatus }}</strong>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Sản phẩm" name="products">
        <div class="col-panel products-col">
          <div class="products-header">
            <h4 class="section-title">Danh sách sản phẩm</h4>

            <button
                v-if="!readonly"
                class="btn-outline-green"
                @click="openAddProductDialog"
            >
              <i class="fa-solid fa-plus"></i>
              Thêm sản phẩm
            </button>
          </div>

          <div class="products-table-wrapper">
            <table class="products-table">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th style="text-align: right">Đơn giá</th>
                <th style="text-align: center">Số lượng</th>
                <th style="text-align: right">Thành tiền</th>
                <th style="text-align: center">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <tr v-if="!order.sanPhams || order.sanPhams.length === 0">
                <td colspan="5" style="text-align: center; color: #888">
                  Chưa có sản phẩm
                </td>
              </tr>

              <tr
                  v-for="sp in order.sanPhams"
                  :key="sp.MaSanPham"
              >
                <td>
                  <div class="sp-cell-new">
                    <img
                        v-if="sp.HinhAnh || sp.hinhAnh || sp.HinhAnhUrl"
                        :src="sp.HinhAnh || sp.hinhAnh || sp.HinhAnhUrl"
                        alt="Ảnh sản phẩm"
                        @error="handleImageError"
                    />

                    <div v-else class="img-placeholder"></div>

                    <div class="sp-info-new">
                      <div class="sp-name-new">
                        {{ sp.tenSanPham }}
                      </div>

                      <div class="sp-sub-new">
                        {{ sp.phanLoai || "Sản phẩm" }}
                      </div>
                    </div>
                  </div>
                </td>

                <td style="text-align: right; font-weight: 600">
                  {{ formatCurrency(sp.giaTien) }}
                </td>

                <td>
                  <div class="qty-control">
                    <button @click="decreaseQty(sp)" :disabled="readonly">-</button>
                    <span>{{ sp.SoLuong }}</span>
                    <button @click="increaseQty(sp)" :disabled="readonly">+</button>
                  </div>
                </td>

                <td style="text-align: right; font-weight: 700">
                  {{ formatCurrency(sp.thanhTien) }}
                </td>

                <td style="text-align: center">
                  <button
                      v-if="!readonly"
                      class="btn-trash"
                      @click="removeSp(sp)"
                  >
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div class="summary-section">
            <div class="sum-row">
              <span>Tạm tính:</span>
              <span>{{ formatCurrency(tamTinh) }}</span>
            </div>

            <div class="sum-row">
              <span>Phí vận chuyển:</span>
              <span>{{ formatCurrency(order.phiVanChuyen || 0) }}</span>
            </div>

            <div
                v-if="order.giamGia"
                class="sum-row red-text"
            >
              <span>Giảm giá:</span>
              <span>- {{ formatCurrency(order.giamGia) }}</span>
            </div>

            <div class="sum-divider"></div>

            <div class="sum-row total">
              <span>Tổng cộng:</span>
              <span>{{ formatCurrency(tongCong) }}</span>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div
        v-if="order"
        class="popup-3col desktop-layout"
    >
      <!-- Thanh tiến trình ngang phía trên -->
      <div class="progress-section-top">
        <h4 class="section-title">Tiến trình đơn hàng</h4>
        <div class="horizontal-stepper">
          <div
              v-for="(step, idx) in lichSuArr"
              :key="idx"
              class="h-step"
              :class="{ done: step.isDone, active: step.isActive }"
          >
            <div class="h-step-indicator">
              <div class="h-step-circle">
                <span v-if="step.isDone && !step.isActive">✓</span>
                <span v-else-if="step.isActive">
                  <i class="fa-solid fa-clock"></i>
                </span>
                <span v-else>{{ idx + 1 }}</span>
              </div>
              <div
                  v-if="idx < lichSuArr.length - 1"
                  class="h-step-line"
              ></div>
            </div>
            <div class="h-step-content">
              <div class="h-step-title">{{ step.title }}</div>
              <div v-if="step.time" class="h-step-time">{{ step.time }}</div>
            </div>
          </div>
        </div>
        <div class="progress-actions" v-if="!readonly">
          <button
              v-if="nextStatus"
              class="btn-capnhat-tt"
              @click="capNhatTrangThaiTiep"
          >
            <i class="fa-solid fa-arrow-right"></i>
            {{
              currentStatus === "Chờ thanh toán"
                  ? "Thanh toán"
                  : "Cập nhật trạng thái tiếp theo"
            }}
          </button>
          <div v-if="nextStatus" class="status-note">
            Trạng thái hiện tại: <strong>{{ currentStatus }}</strong>
            <br />
            Nhấn để chuyển sang trạng thái: <strong>{{ nextStatus }}</strong>
          </div>
        </div>
      </div>

      <!-- Hàng dưới: thông tin + sản phẩm -->
      <div class="bottom-section">
        <div class="col-panel info-col">
          <div class="info-section">
            <h4 class="section-title">Thông tin đơn hàng</h4>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-barcode"></i>
                Mã đơn hàng:
              </div>
              <div class="info-val bold">#{{ order.maCode }}</div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-calendar-days"></i>
                Ngày tạo:
              </div>
              <div class="info-val">
                {{ formatDate(order.NgayTaoDon) }}
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-wallet"></i>
                Phương thức thanh toán:
              </div>
              <div class="info-val">
                <el-select
                    v-model="order.phuongThucThanhToan"
                    placeholder="Chọn phương thức"
                    style="width: 100%"
                    :disabled="readonly"
                >
                  <el-option label="Chưa chọn" value="Chưa chọn" />
                  <el-option label="Tiền mặt" value="Tiền mặt" />
                  <el-option label="Chuyển khoản" value="Chuyển khoản" />
                </el-select>
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-money-check"></i>
                Trạng thái thanh toán:
              </div>
              <div class="info-val">
                <el-select
                    v-model="order.trangThaiThanhToan"
                    placeholder="Chọn trạng thái"
                    style="width: 100%"
                    :disabled="readonly"
                >
                  <el-option label="Chưa thanh toán" value="Chưa thanh toán" />
                  <el-option label="Đã đặt cọc" value="Đã đặt cọc" />
                  <el-option label="Đã thanh toán" value="Đã thanh toán" />
                </el-select>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="info-section">
            <h4 class="section-title">Thông tin khách hàng</h4>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-user"></i>
                Họ tên:
              </div>
              <div class="info-val">
                <el-input
                    v-model="order.tenKhachHang"
                    maxlength="30"
                    show-word-limit
                    placeholder="Nhập họ tên khách hàng"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-phone"></i>
                SĐT:
              </div>
              <div class="info-val">
                <el-input
                    v-model="order.soDienThoaiKH"
                    maxlength="10"
                    placeholder="Nhập số điện thoại"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-envelope"></i>
                Email:
              </div>
              <div class="info-val">
                <el-input
                    v-model="order.emailKH"
                    placeholder="Nhập email"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-location-dot"></i>
                Địa chỉ:
              </div>
              <div class="info-val address-val">
                <el-input
                    v-model="order.diaChiKH"
                    maxlength="40"
                    show-word-limit
                    placeholder="Nhập địa chỉ"
                    :disabled="readonly"
                />
              </div>
            </div>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-note-sticky"></i>
                Ghi chú:
              </div>
              <div class="info-val address-val">
                <el-input
                    v-model="order.GhiChu"
                    type="textarea"
                    :rows="3"
                    placeholder="Nhập ghi chú đơn hàng"
                    :disabled="readonly"
                />
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="info-section">
            <h4 class="section-title">Nhân viên phụ trách</h4>

            <div class="info-row">
              <div class="info-label">
                <i class="fa-solid fa-user-tie"></i>
                Họ tên:
              </div>
              <div class="info-val">
                {{ order.tenNhanVien || "Chưa cập nhật" }}
              </div>
            </div>
          </div>
        </div>

        <div class="col-panel products-col">
          <div class="products-header">
            <h4 class="section-title">Danh sách sản phẩm</h4>

            <button
                v-if="!readonly"
                class="btn-outline-green"
                @click="openAddProductDialog"
            >
              <i class="fa-solid fa-plus"></i>
              Thêm sản phẩm
            </button>
          </div>

          <div class="products-table-wrapper">
            <table class="products-table">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th style="text-align: right">Đơn giá</th>
                <th style="text-align: center">Số lượng</th>
                <th style="text-align: right">Thành tiền</th>
                <th style="text-align: center">Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <tr v-if="!order.sanPhams || order.sanPhams.length === 0">
                <td colspan="5" style="text-align: center; color: #888">
                  Chưa có sản phẩm
                </td>
              </tr>

              <tr
                  v-for="sp in order.sanPhams"
                  :key="sp.MaSanPham"
              >
                <td>
                  <div class="sp-cell-new">
                    <img
                        v-if="sp.HinhAnh || sp.hinhAnh || sp.HinhAnhUrl"
                        :src="sp.HinhAnh || sp.hinhAnh || sp.HinhAnhUrl"
                        alt="Ảnh sản phẩm"
                        @error="handleImageError"
                    />

                    <div v-else class="img-placeholder"></div>

                    <div class="sp-info-new">
                      <div class="sp-name-new">
                        {{ sp.tenSanPham }}
                      </div>

                      <div class="sp-sub-new">
                        {{ sp.phanLoai || "Sản phẩm" }}
                      </div>
                    </div>
                  </div>
                </td>

                <td style="text-align: right; font-weight: 600">
                  {{ formatCurrency(sp.giaTien) }}
                </td>

                <td>
                  <div class="qty-control">
                    <button @click="decreaseQty(sp)" :disabled="readonly">-</button>
                    <span>{{ sp.SoLuong }}</span>
                    <button @click="increaseQty(sp)" :disabled="readonly">+</button>
                  </div>
                </td>

                <td style="text-align: right; font-weight: 700">
                  {{ formatCurrency(sp.thanhTien) }}
                </td>

                <td style="text-align: center">
                  <button
                      v-if="!readonly"
                      class="btn-trash"
                      @click="removeSp(sp)"
                  >
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div class="summary-section">
            <div class="sum-row">
              <span>Tạm tính:</span>
              <span>{{ formatCurrency(tamTinh) }}</span>
            </div>

            <div class="sum-row">
              <span>Phí vận chuyển:</span>
              <span>{{ formatCurrency(order.phiVanChuyen || 0) }}</span>
            </div>

            <div
                v-if="order.giamGia"
                class="sum-row red-text"
            >
              <span>Giảm giá:</span>
              <span>- {{ formatCurrency(order.giamGia) }}</span>
            </div>

            <div class="sum-divider"></div>

            <div class="sum-row total">
              <span>Tổng cộng:</span>
              <span>{{ formatCurrency(tongCong) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div v-if="!readonly" class="dialog-footer-new">
        <button
            class="btn-cancel-new"
            @click="handleHuy"
        >
          Hủy
        </button>

        <button
            class="btn-save-new"
            @click="handleLuu"
        >
          Lưu thay đổi
        </button>
      </div>
      <div v-else class="dialog-footer-new">
        <button
            class="btn-cancel-new"
            @click="visible = false"
        >
          Đóng
        </button>
      </div>
    </template>

    <el-dialog
        v-model="showAddProductDialog"
        title="Thêm sản phẩm vào đơn hàng"
        width="760px"
        :append-to-body="true"
        :z-index="10080"
    >
      <div class="add-product-popup">
        <el-input
            v-model="keywordSanPham"
            placeholder="Tìm kiếm sản phẩm..."
            clearable
            style="margin-bottom: 16px"
        />

        <div v-if="loadingSanPham" class="empty-product">
          Đang tải sản phẩm...
        </div>

        <div
            v-else-if="filteredSanPhamOptions.length === 0"
            class="empty-product"
        >
          Không có sản phẩm phù hợp
        </div>

        <div v-else class="product-option-list">
          <div
              v-for="sp in filteredSanPhamOptions"
              :key="sp.MaSanPham"
              class="product-option-item"
          >
            <div class="product-option-left">
              <img
                  v-if="sp.HinhAnh"
                  :src="sp.HinhAnh"
                  alt="Ảnh sản phẩm"
                  class="product-option-img"
              />

              <div
                  v-else
                  class="product-option-img product-option-img-empty"
              ></div>

              <div>
                <div class="product-option-name">
                  {{ sp.tenSanPham }}
                </div>

                <div class="product-option-meta">
                  {{ sp.phanLoai || "Sản phẩm" }}
                  · Tồn kho: {{ sp.soLuongTon }}
                </div>
              </div>
            </div>

            <div class="product-option-right">
              <div class="product-option-price">
                {{ formatCurrency(sp.giaTien) }}
              </div>

              <el-button
                  type="success"
                  size="small"
                  :disabled="Number(sp.soLuongTon || 0) <= 0"
                  @click="addSanPhamToOrder(sp)"
              >
                Thêm
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

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
          <strong>#{{ order?.maCode }}</strong>
        </p>

        <p
            style="
            font-size: 18px;
            font-weight: bold;
            color: #e74c3c;
            margin: 10px 0;
          "
        >
          Số tiền: {{ formatCurrency(tongCong) }}
        </p>

        <img
            v-if="order"
            :src="`https://img.vietqr.io/image/MB-140213032008-compact.png?amount=${tongCong}&addInfo=${order.maCode}`"
            alt="QR Code Thanh Toán"
            style="max-width: 100%; border-radius: 8px; margin: 10px 0"
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
  </el-dialog>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/PopChiTietDonHang.css"></style>

<style scoped>
/* Layout mới cho desktop */
.popup-3col.desktop-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.progress-section-top {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.horizontal-stepper {
  display: flex;
  align-items: center;
  gap: 0;
  margin-top: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.h-step {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 120px;
}

.h-step-indicator {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.h-step-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.h-step.done .h-step-circle {
  background: #10b981;
  color: #fff;
}

.h-step.active .h-step-circle {
  background: #3b82f6;
  color: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.h-step-line {
  width: 40px;
  height: 2px;
  background: #e5e7eb;
  margin: 0 8px;
  flex-shrink: 0;
}

.h-step.done .h-step-line {
  background: #10b981;
}

.h-step.active .h-step-line {
  background: #3b82f6;
}

.h-step-content {
  margin-left: 12px;
  flex: 1;
}

.h-step-title {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.h-step.done .h-step-title {
  color: #10b981;
}

.h-step.active .h-step-title {
  color: #3b82f6;
  font-weight: 600;
}

.h-step-time {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
}

.bottom-section {
  display: flex;
  gap: 20px;
}

.bottom-section .info-col {
  flex: 1;
  min-width: 280px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.bottom-section .info-col::-webkit-scrollbar {
  width: 6px;
}

.bottom-section .info-col::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.bottom-section .info-col::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.bottom-section .info-col::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.bottom-section .products-col {
  flex: 2;
  min-width: 400px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.bottom-section .products-col::-webkit-scrollbar {
  width: 6px;
}

.bottom-section .products-col::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.bottom-section .products-col::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.bottom-section .products-col::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.progress-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 1024px) {
  .bottom-section {
    flex-direction: column;
  }
  
  .bottom-section .info-col,
  .bottom-section .products-col {
    min-width: 100%;
  }
  
  .horizontal-stepper {
    font-size: 12px;
  }
  
  .h-step {
    min-width: 100px;
  }
  
  .h-step-circle {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .h-step-line {
    width: 30px;
  }
  
  .h-step-title {
    font-size: 12px;
  }
}
</style>