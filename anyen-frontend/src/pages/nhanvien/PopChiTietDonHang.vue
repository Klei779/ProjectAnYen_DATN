<script setup>
import { computed } from "vue";
import {
  Calendar, User, Wallet, Phone, Message, Location,
  Plus, Delete, Right
} from "@element-plus/icons-vue";
import { formatCurrency, formatDate } from "../../services/donHangService.js";
import "../../assets/styles/PopChiTietDonHang.css";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  donHang:    { type: Object,  default: null },
});

const emit = defineEmits(["update:modelValue", "huy-don", "dong", "luu"]);

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

const STEPS = ["Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Thanh toán"];
const getStepIndex = (trangThai) => STEPS.indexOf(trangThai);

const lichSuArr = computed(() => {
    if (!order.value) return [];
    // If backend returns lichSu, we format it or we mock the 4 steps based on current status
    const currentIdx = getStepIndex(order.value.trangThai);
    
    return STEPS.map((step, idx) => {
        let isDone = false;
        let isActive = false;
        if (order.value.trangThai === "Đã hủy") {
            isDone = false;
        } else {
            isDone = idx <= currentIdx;
            isActive = idx === currentIdx && step !== "Thanh toán";
        }

        // Try to find matching step in backend lichSu
        const backendStep = order.value.lichSu?.find(ls => ls.trangThai === step);

        return {
            title: step,
            time: backendStep?.thoiGian || null,
            desc: backendStep?.moTa || (isDone ? "" : "Chưa cập nhật"),
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

const capNhatTrangThaiTiep = () => {
    if (nextStatus.value && order.value) {
        order.value.trangThai = nextStatus.value;
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
    <div class="popup-3col" v-if="order">

      <!-- CỘT 1: THÔNG TIN CHUNG -->
      <div class="col-panel info-col">
          <!-- Thông tin đơn hàng -->
          <div class="info-section">
              <h4 class="section-title">Thông tin đơn hàng</h4>
              <div class="info-row">
                  <div class="info-label"><el-icon><Calendar/></el-icon> Mã đơn hàng:</div>
                  <div class="info-val bold">#{{ order.maCode }}</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Calendar/></el-icon> Ngày tạo:</div>
                  <div class="info-val">{{ formatDate(order.NgayTaoDon) }}</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Wallet/></el-icon> Phương thức thanh toán:</div>
                  <div class="info-val">{{ order.phuongThucThanhToan || 'Chưa cập nhật' }}</div>
              </div>
          </div>
          
          <div class="divider"></div>

          <!-- Thông tin khách hàng -->
          <div class="info-section">
              <h4 class="section-title">Thông tin khách hàng</h4>
              <div class="info-row">
                  <div class="info-label"><el-icon><User/></el-icon> Họ tên:</div>
                  <div class="info-val">{{ order.tenKhachHang }}</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Phone/></el-icon> SĐT:</div>
                  <div class="info-val">{{ order.soDienThoaiKH || 'Chưa cập nhật' }}</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Message/></el-icon> Email:</div>
                  <div class="info-val">{{ order.emailKH || 'Chưa cập nhật' }}</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Location/></el-icon> Địa chỉ:</div>
                  <div class="info-val address-val">{{ order.diaChiKH || 'Chưa cập nhật' }}</div>
              </div>
          </div>

          <div class="divider"></div>

          <!-- Nhân viên phụ trách -->
          <div class="info-section">
              <h4 class="section-title">Nhân viên phụ trách</h4>
              <div class="info-row">
                  <div class="info-label"><el-icon><User/></el-icon> Họ tên:</div>
                  <div class="info-val">{{ order.tenNhanVien || 'Chưa cập nhật' }}</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Phone/></el-icon> SĐT:</div>
                  <div class="info-val">Chưa cập nhật</div>
              </div>
              <div class="info-row">
                  <div class="info-label"><el-icon><Message/></el-icon> Email:</div>
                  <div class="info-val">Chưa cập nhật</div>
              </div>
          </div>
      </div>

      <!-- CỘT 2: TIẾN TRÌNH -->
      <div class="col-panel progress-col">
          <h4 class="section-title">Tiến trình đơn hàng</h4>
          
          <div class="vertical-stepper">
              <div v-for="(step, idx) in lichSuArr" :key="idx" class="v-step" :class="{ done: step.isDone, active: step.isActive }">
                  <div class="v-step-indicator">
                      <div class="v-step-circle">
                         <span v-if="step.isDone && !step.isActive">✓</span>
                         <span v-else-if="step.isActive"><el-icon><Timer/></el-icon></span>
                         <span v-else>{{ idx + 1 }}</span>
                      </div>
                      <div class="v-step-line" v-if="idx < lichSuArr.length - 1"></div>
                  </div>
                  <div class="v-step-content">
                      <div class="v-step-title">{{ step.title }}</div>
                      <div class="v-step-time" v-if="step.time">{{ step.time }}</div>
                      <div class="v-step-time" v-else-if="step.isDone">Đã cập nhật</div>
                      <div class="v-step-time" v-else>Chưa cập nhật</div>
                      <div class="v-step-desc">{{ step.desc }}</div>
                  </div>
              </div>
          </div>

          <div class="progress-actions">
              <button v-if="nextStatus" class="btn-capnhat-tt" @click="capNhatTrangThaiTiep">
                  <el-icon><Right/></el-icon> Cập nhật trạng thái tiếp theo
              </button>
              <div v-if="nextStatus" class="status-note">
                  Trạng thái hiện tại: <strong>{{ currentStatus }}</strong><br/>
                  Nhấn để chuyển sang trạng thái: <strong>{{ nextStatus }}</strong>
              </div>
          </div>
      </div>

      <!-- CỘT 3: SẢN PHẨM -->
      <div class="col-panel products-col">
          <div class="products-header">
              <h4 class="section-title">Danh sách sản phẩm</h4>
              <button class="btn-outline-green"><el-icon><Plus/></el-icon> Thêm sản phẩm</button>
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
                              <button class="btn-trash" @click="removeSp(sp)"><el-icon><Delete/></el-icon></button>
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
  </el-dialog>
</template>
