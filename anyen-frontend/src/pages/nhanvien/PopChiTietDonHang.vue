<script setup>
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { formatCurrency, formatDate, capNhatTrangThai } from "../../services/donHangService.js";
import "../../assets/styles/nhanvien/QLDonHang/PopChiTietDonHang.css";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  donHang:    { type: Object,  default: null },
});

const emit = defineEmits(["update:modelValue", "huy-don", "dong", "luu", "cap-nhat"]);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const order = computed(() => props.donHang);

const tamTinh = computed(() => {
  if (!order.value?.sanPhams) return 0;
  return order.value.sanPhams.reduce((s, sp) => s + sp.thanhTien, 0);
});

const tongCong = computed(() => {
  if (!order.value) return 0;
  return tamTinh.value - (order.value.giamGia || 0) + (order.value.phiVanChuyen || 0);
});

const STEPS = ["Mới tạo", "Đã xác nhận", "Đang xử lý", "Chờ thanh toán", "Hoàn thành"];
const getStepIndex = (trangThai) => STEPS.indexOf(trangThai);

const lichSuArr = computed(() => {
  if (!order.value) return [];
  const currentIdx = getStepIndex(order.value.trangThai);
  const isDaHuy = order.value.trangThai === "Đã hủy";

  return STEPS.map((step, idx) => {
    let isDone = false;
    let isActive = false;
    if (isDaHuy) {
      isDone = false;
    } else {
      isDone = idx <= currentIdx;
      isActive = idx === currentIdx;
    }

    // Try to find matching step in backend lichSu
    const backendStep = order.value.lichSu?.find(ls => ls.trangThai === step);

    return {
      title: step,
      time: backendStep?.thoiGian || null,
      desc: backendStep?.moTa || (isDone ? "Đã hoàn thành" : "Chưa cập nhật"),
      isDone,
      isActive
    };
  });
});

const currentStatus = computed(() => order.value?.trangThai);
const nextStatus = computed(() => {
  const currentIdx = getStepIndex(currentStatus.value);
  if (currentIdx >= 0 && currentIdx < STEPS.length - 1 && currentStatus.value !== "Đã hủy") {
    return STEPS[currentIdx + 1];
  }
  return null;
});

const handleHuy = () => emit("dong");
const handleLuu = () => { emit("luu", order.value); emit("dong"); };

const increaseQty = (sp) => { sp.SoLuong++; sp.thanhTien = sp.SoLuong * sp.giaTien; };
const decreaseQty = (sp) => {
  if(sp.SoLuong > 1) {
    sp.SoLuong--;
    sp.thanhTien = sp.SoLuong * sp.giaTien;
  }
};
const removeSp = (sp) => {
  order.value.sanPhams = order.value.sanPhams.filter(item => item.MaSanPham !== sp.MaSanPham);
};

const showPaymentDialog = ref(false);
const showCashConfirmDialog = ref(false);
const activeTab = ref("info");

const confirmPayment = async () => {
  if (order.value) {
    try {
      await capNhatTrangThai(order.value.maDonHang || order.value.MaDonHang, "Hoàn thành");
      ElMessage.success(`Đã cập nhật trạng thái: Hoàn thành`);
      order.value.trangThai = "Hoàn thành";
      emit("cap-nhat");
      showPaymentDialog.value = false;
    } catch (error) {
      console.error("Lỗi khi cập nhật trạng thái:", error);
      ElMessage.error(error.response?.data?.message || "Cập nhật trạng thái thất bại");
    }
  }
};

const confirmCashPayment = async () => {
  if (order.value) {
    try {
      await capNhatTrangThai(order.value.maDonHang || order.value.MaDonHang, "Hoàn thành");
      ElMessage.success(`Đã cập nhật trạng thái: Hoàn thành`);
      order.value.trangThai = "Hoàn thành";
      emit("cap-nhat");
      showCashConfirmDialog.value = false;
    } catch (error) {
      console.error("Lỗi khi cập nhật trạng thái:", error);
      ElMessage.error(error.response?.data?.message || "Cập nhật trạng thái thất bại");
    }
  }
};

const capNhatTrangThaiTiep = async () => {
  if (currentStatus.value === "Chờ thanh toán") {
    if (order.value.phuongThucThanhToan === "Chuyển khoản") {
      showPaymentDialog.value = true;
      return;
    } else if (order.value.phuongThucThanhToan === "Tiền mặt") {
      showCashConfirmDialog.value = true;
      return;
    }
  }

  if (nextStatus.value && order.value) {
    try {
      await capNhatTrangThai(order.value.maDonHang || order.value.MaDonHang, nextStatus.value);
      ElMessage.success(`Đã cập nhật trạng thái: ${nextStatus.value}`);
      order.value.trangThai = nextStatus.value;
      emit("cap-nhat");
    } catch (error) {
      console.error("Lỗi khi cập nhật trạng thái:", error);
      ElMessage.error(error.response?.data?.message || "Cập nhật trạng thái thất bại");
    }
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
    <!-- HEADER -->
    <template #header>
      <div class="dialog-title-new">
        Sửa đơn hàng #{{ order?.maCode }}
      </div>
    </template>

    <!-- BODY -->
    <el-tabs
        v-model="activeTab"
        class="order-tabs mobile-layout"
        v-if="order"
    >
      <el-tab-pane label="Thông tin" name="info">
        <!-- CỘT 1: THÔNG TIN CHUNG -->
        <div class="col-panel info-col">
          <!-- Thông tin đơn hàng -->
          <div class="info-section">
            <h4 class="section-title">Thông tin đơn hàng</h4>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-calendar"></i> Mã đơn hàng:</div>
              <div class="info-val bold">#{{ order.maCode }}</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-calendar-days"></i> Ngày tạo:</div>
              <div class="info-val">{{ formatDate(order.NgayTaoDon) }}</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-wallet"></i> Phương thức thanh toán:</div>
              <div class="info-val">{{ order.phuongThucThanhToan || 'Chưa cập nhật' }}</div>
            </div>
          </div>

          <div class="divider"></div>

          <!-- Thông tin khách hàng -->
          <div class="info-section">
            <h4 class="section-title">Thông tin khách hàng</h4>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-user"></i> Họ tên:</div>
              <div class="info-val">{{ order.tenKhachHang }}</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-phone"></i> SĐT:</div>
              <div class="info-val">{{ order.soDienThoaiKH || 'Chưa cập nhật' }}</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-envelope"></i> Email:</div>
              <div class="info-val">{{ order.emailKH || 'Chưa cập nhật' }}</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-location-dot"></i> Địa chỉ:</div>
              <div class="info-val address-val">{{ order.diaChiKH || 'Chưa cập nhật' }}</div>
            </div>
          </div>

          <div class="divider"></div>

          <!-- Nhân viên phụ trách -->
          <div class="info-section">
            <h4 class="section-title">Nhân viên phụ trách</h4>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-user"></i> Họ tên:</div>
              <div class="info-val">{{ order.tenNhanVien || 'Chưa cập nhật' }}</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-phone"></i> SĐT:</div>
              <div class="info-val">Chưa cập nhật</div>
            </div>
            <div class="info-row">
              <div class="info-label"><i class="fa-solid fa-envelope"></i> Email:</div>
              <div class="info-val">Chưa cập nhật</div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Tiến trình" name="progress">
        <!-- CỘT 2: TIẾN TRÌNH -->
        <div class="col-panel progress-col">
          <h4 class="section-title">Tiến trình đơn hàng</h4>

          <div class="vertical-stepper">
            <div v-for="(step, idx) in lichSuArr" :key="idx" class="v-step" :class="{ done: step.isDone, active: step.isActive }">
              <div class="v-step-indicator">
                <div class="v-step-circle">
                  <span v-if="step.isDone && !step.isActive">✓</span>
                  <span v-else-if="step.isActive"><i class="fa-solid fa-clock"></i></span>
                  <span v-else>{{ idx + 1 }}</span>
                </div>
                <div class="v-step-line" v-if="idx < lichSuArr.length - 1"></div>
              </div>
              <div class="v-step-content">
                <div class="v-step-title">{{ step.title }}</div>
                <div class="v-step-time" v-if="step.time">{{ step.time }}</div>
                <div class="v-step-desc">{{ step.desc }}</div>
              </div>
            </div>
          </div>

          <div class="progress-actions">
            <button v-if="nextStatus" class="btn-capnhat-tt" @click="capNhatTrangThaiTiep">
              <i class="fa-solid fa-arrow-right"></i> {{ currentStatus === 'Chờ thanh toán' ? 'Thanh toán' : 'Cập nhật trạng thái tiếp theo' }}
            </button>
            <div v-if="nextStatus" class="status-note">
              Trạng thái hiện tại: <strong>{{ currentStatus }}</strong><br/>
              Nhấn để chuyển sang trạng thái: <strong>{{ nextStatus }}</strong>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Sản phẩm" name="products">
        <!-- CỘT 3: SẢN PHẨM -->
        <div class="col-panel products-col">
          <div class="products-header">
            <h4 class="section-title">Danh sách sản phẩm</h4>
            <button class="btn-outline-green"><i class="fa-solid fa-plus"></i> Thêm sản phẩm</button>
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
                <td colspan="5" style="text-align: center; color: #888;">Chưa có sản phẩm</td>
              </tr>
              <tr v-for="sp in order.sanPhams" :key="sp.MaSanPham">
                <td>
                  <div class="sp-cell-new">
                    <img v-if="sp.HinhAnh" :src="sp.HinhAnh" />
                    <div v-else class="img-placeholder"></div>
                    <div class="sp-info-new">
                      <div class="sp-name-new">{{ sp.tenSanPham }}</div>
                      <div class="sp-sub-new">{{ sp.phanLoai || sp.SoLuong }}</div>
                    </div>
                  </div>
                </td>
                <td style="text-align: right; font-weight: 600;">{{ formatCurrency(sp.giaTien) }}</td>
                <td>
                  <div class="qty-control">
                    <button @click="decreaseQty(sp)">-</button>
                    <span>{{ sp.SoLuong }}</span>
                    <button @click="increaseQty(sp)">+</button>
                  </div>
                </td>
                <td style="text-align: right; font-weight: 700;">{{ formatCurrency(sp.thanhTien) }}</td>
                <td style="text-align: center;">
                  <button class="btn-trash" @click="removeSp(sp)"><i class="fa-solid fa-trash"></i></button>
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
            <div class="sum-row red-text" v-if="order.giamGia">
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

    <div class="popup-3col desktop-layout" v-if="order">
      <div class="col-panel info-col">
        <!-- Thông tin đơn hàng -->
        <div class="info-section">
          <h4 class="section-title">Thông tin đơn hàng</h4>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-calendar"></i> Mã đơn hàng:</div>
            <div class="info-val bold">#{{ order.maCode }}</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-calendar-days"></i> Ngày tạo:</div>
            <div class="info-val">{{ formatDate(order.NgayTaoDon) }}</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-wallet"></i> Phương thức thanh toán:</div>
            <div class="info-val">{{ order.phuongThucThanhToan || 'Chưa cập nhật' }}</div>
          </div>
        </div>

        <div class="divider"></div>

        <!-- Thông tin khách hàng -->
        <div class="info-section">
          <h4 class="section-title">Thông tin khách hàng</h4>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-user"></i> Họ tên:</div>
            <div class="info-val">{{ order.tenKhachHang }}</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-phone"></i> SĐT:</div>
            <div class="info-val">{{ order.soDienThoaiKH || 'Chưa cập nhật' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-envelope"></i> Email:</div>
            <div class="info-val">{{ order.emailKH || 'Chưa cập nhật' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-location-dot"></i> Địa chỉ:</div>
            <div class="info-val address-val">{{ order.diaChiKH || 'Chưa cập nhật' }}</div>
          </div>
        </div>

        <div class="divider"></div>

        <!-- Nhân viên phụ trách -->
        <div class="info-section">
          <h4 class="section-title">Nhân viên phụ trách</h4>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-user"></i> Họ tên:</div>
            <div class="info-val">{{ order.tenNhanVien || 'Chưa cập nhật' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-phone"></i> SĐT:</div>
            <div class="info-val">Chưa cập nhật</div>
          </div>
          <div class="info-row">
            <div class="info-label"><i class="fa-solid fa-envelope"></i> Email:</div>
            <div class="info-val">Chưa cập nhật</div>
          </div>
        </div>
      </div>

      <div class="col-panel progress-col">
        <h4 class="section-title">Tiến trình đơn hàng</h4>

        <div class="vertical-stepper">
          <div v-for="(step, idx) in lichSuArr" :key="idx" class="v-step" :class="{ done: step.isDone, active: step.isActive }">
            <div class="v-step-indicator">
              <div class="v-step-circle">
                <span v-if="step.isDone && !step.isActive">✓</span>
                <span v-else-if="step.isActive"><i class="fa-solid fa-clock"></i></span>
                <span v-else>{{ idx + 1 }}</span>
              </div>
              <div class="v-step-line" v-if="idx < lichSuArr.length - 1"></div>
            </div>
            <div class="v-step-content">
              <div class="v-step-title">{{ step.title }}</div>
              <div class="v-step-time" v-if="step.time">{{ step.time }}</div>
              <div class="v-step-desc">{{ step.desc }}</div>
            </div>
          </div>
        </div>

        <div class="progress-actions">
          <button v-if="nextStatus" class="btn-capnhat-tt" @click="capNhatTrangThaiTiep">
            <i class="fa-solid fa-arrow-right"></i> {{ currentStatus === 'Chờ thanh toán' ? 'Thanh toán' : 'Cập nhật trạng thái tiếp theo' }}
          </button>
          <div v-if="nextStatus" class="status-note">
            Trạng thái hiện tại: <strong>{{ currentStatus }}</strong><br/>
            Nhấn để chuyển sang trạng thái: <strong>{{ nextStatus }}</strong>
          </div>
        </div>
      </div>

      <div class="col-panel products-col">
        <div class="products-header">
          <h4 class="section-title">Danh sách sản phẩm</h4>
          <button class="btn-outline-green"><i class="fa-solid fa-plus"></i> Thêm sản phẩm</button>
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
              <td colspan="5" style="text-align: center; color: #888;">Chưa có sản phẩm</td>
            </tr>
            <tr v-for="sp in order.sanPhams" :key="sp.MaSanPham">
              <td>
                <div class="sp-cell-new">
                  <img v-if="sp.HinhAnh" :src="sp.HinhAnh" />
                  <div v-else class="img-placeholder"></div>
                  <div class="sp-info-new">
                    <div class="sp-name-new">{{ sp.tenSanPham }}</div>
                    <div class="sp-sub-new">{{ sp.phanLoai || sp.SoLuong }}</div>
                  </div>
                </div>
              </td>
              <td style="text-align: right; font-weight: 600;">{{ formatCurrency(sp.giaTien) }}</td>
              <td>
                <div class="qty-control">
                  <button @click="decreaseQty(sp)">-</button>
                  <span>{{ sp.SoLuong }}</span>
                  <button @click="increaseQty(sp)">+</button>
                </div>
              </td>
              <td style="text-align: right; font-weight: 700;">{{ formatCurrency(sp.thanhTien) }}</td>
              <td style="text-align: center;">
                <button class="btn-trash" @click="removeSp(sp)"><i class="fa-solid fa-trash"></i></button>
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
          <div class="sum-row red-text" v-if="order.giamGia">
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
    <!-- FOOTER -->
    <template #footer>
      <div class="dialog-footer-new">
        <button class="btn-cancel-new" @click="handleHuy">Hủy</button>
        <button class="btn-save-new" @click="handleLuu">Lưu thay đổi</button>
      </div>
    </template>

    <!-- Popup thanh toán QR -->
    <el-dialog
        v-model="showPaymentDialog"
        title="Thanh toán chuyển khoản"
        width="400px"
        center
        :append-to-body="true"
        :z-index="10060"
    >
      <div style="text-align: center;">
        <p>Vui lòng quét mã QR bên dưới để thanh toán cho đơn hàng <strong>#{{ order?.maCode }}</strong></p>
        <p style="font-size: 18px; font-weight: bold; color: #e74c3c; margin: 10px 0;">Số tiền: {{ formatCurrency(tongCong) }}</p>
        <img
            v-if="order"
            :src="`https://img.vietqr.io/image/MB-140213032008-compact.png?amount=${tongCong}&addInfo=${order.maCode}`"
            alt="QR Code Thanh Toán"
            style="max-width: 100%; border-radius: 8px; margin: 10px 0;"
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

    <!-- Popup xác nhận thanh toán tiền mặt -->
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

  </el-dialog>
</template>
<style scoped src="../../assets/styles/nhanvien/QLDonHang/PopChiTietDonHang.css"></style>
