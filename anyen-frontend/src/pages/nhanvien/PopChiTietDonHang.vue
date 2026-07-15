<script setup>
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  formatCurrency,
  formatDate,
  capNhatTrangThai,
  getSanPhamTaoDonHang,
  guiDoiTac,
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

  return {
    ...sp,
    MaSanPham: maSanPham,
    maSanPham,
    SoLuong: soLuong,
    soLuong,
    giaTien,
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
    },
    {
      immediate: true,
    }
);

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
  "Chờ đối tác xác nhận",
  "Đã xác nhận",
  "Đang xử lý",
  "Chờ thanh toán",
  "Hoàn thành",
];

const getStepIndex = (trangThai) => STEPS.indexOf(trangThai);

const lichSuArr = computed(() => {
  if (!order.value) return [];

  const currentIdx = getStepIndex(order.value.trangThai);
  const isDaHuy = order.value.trangThai === "Đã hủy";

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

const capNhatTrangThaiTiep = async () => {
  if (!order.value || !nextStatus.value) return;

  try {
    if (nextStatus.value === 'Chờ đối tác xác nhận') {
      await guiDoiTac(getOrderId());
    } else {
      await capNhatTrangThai(getOrderId(), nextStatus.value);
    }

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
      top="4vh"
      class="chi-tiet-dialog"
      :show-close="true"
      destroy-on-close
      :z-index="10050"
  >
    <template #header>
      <div class="dialog-title-new">
        Sửa đơn hàng #{{ order?.maCode }}
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
                v-if="nextStatus && (currentStatus === 'Mới tạo' || currentStatus === 'Chờ thanh toán')"
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

            <div v-if="nextStatus && (currentStatus === 'Mới tạo' || currentStatus === 'Chờ thanh toán')" class="status-note">
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
                        v-if="sp.HinhAnh"
                        :src="sp.HinhAnh"
                        alt="Ảnh sản phẩm"
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
                    <button @click="decreaseQty(sp)">-</button>
                    <span>{{ sp.SoLuong }}</span>
                    <button @click="increaseQty(sp)">+</button>
                  </div>
                </td>

                <td style="text-align: right; font-weight: 700">
                  {{ formatCurrency(sp.thanhTien) }}
                </td>

                <td style="text-align: center">
                  <button
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
            Trạng thái hiện tại:
            <strong>{{ currentStatus }}</strong>
            <br />
            Nhấn để chuyển sang trạng thái:
            <strong>{{ nextStatus }}</strong>
          </div>
        </div>
      </div>

      <div class="col-panel products-col">
        <div class="products-header">
          <h4 class="section-title">Danh sách sản phẩm</h4>

          <button
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
                      v-if="sp.HinhAnh"
                      :src="sp.HinhAnh"
                      alt="Ảnh sản phẩm"
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
                  <button @click="decreaseQty(sp)">-</button>
                  <span>{{ sp.SoLuong }}</span>
                  <button @click="increaseQty(sp)">+</button>
                </div>
              </td>

              <td style="text-align: right; font-weight: 700">
                {{ formatCurrency(sp.thanhTien) }}
              </td>

              <td style="text-align: center">
                <button
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

    <template #footer>
      <div class="dialog-footer-new">
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
  </el-dialog>
</template>

<style scoped src="../../assets/styles/nhanvien/QLDonHang/PopChiTietDonHang.css"></style>