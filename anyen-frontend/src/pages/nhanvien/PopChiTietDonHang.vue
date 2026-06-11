<script setup>
import { computed } from "vue";
import {
  Printer, Close, User, ShoppingCart,
  Timer, Phone, Message, Location,
  Lock, Calendar, Van,
} from "@element-plus/icons-vue";
import { formatCurrency, formatDate } from "../../services/donHangService.js";
import "../../assets/styles/PopChiTietDonHang.css";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  donHang:    { type: Object,  default: null },
});

const emit = defineEmits(["update:modelValue", "huy-don", "dong"]);

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

const ghiChuLines = computed(() => {
  if (!order.value?.ghiChuKH) return [];
  return order.value.ghiChuKH.split(".").filter((l) => l.trim() !== "");
});

const avatarInitials = computed(() => {
  if (!order.value?.tenKhachHang) return "KH";
  return order.value.tenKhachHang
    .split(" ").slice(-2).map((w) => w[0]).join("").toUpperCase();
});

const trangThaiClass = computed(() => {
  const map = {
    "Đã xác nhận":   "badge-green",
    "Đang xử lý":    "badge-orange",
    "Chờ thanh toán":"badge-blue",
    "Đã hủy":        "badge-red",
    "Hoàn thành":    "badge-green",
  };
  return map[order.value?.trangThai] || "badge-gray";
});

const lichSuIconClass = (item) => {
  if (!item.done) return "ls-icon ls-gray";
  const map = {
    green: "ls-icon ls-green", orange: "ls-icon ls-orange",
    purple: "ls-icon ls-purple", blue: "ls-icon ls-blue",
    cyan: "ls-icon ls-cyan",
  };
  return map[item.color] || "ls-icon ls-gray";
};

const handleHuyDon = () => emit("huy-don", order.value?.MaDonHang);
const handleDong   = () => { visible.value = false; emit("dong"); };
</script>

<template>
  <el-dialog
    v-model="visible"
    width="92%"
    top="3vh"
    class="don-hang-dialog"
    :show-close="false"
    destroy-on-close
  >
    <!-- HEADER -->
    <template #header>
      <div class="dh-header">
        <div class="dh-header-left">
          <h2 class="dh-title">
            Chi tiết đơn hàng
            <span class="dh-code">#{{ order?.maCode }}</span>
          </h2>
          <span v-if="order?.trangThai" class="dh-badge" :class="trangThaiClass">
            {{ order.trangThai }}
          </span>
        </div>

        <div class="dh-header-right">
          <div class="dh-meta">
            <span class="dh-meta-item">
              <el-icon><Calendar /></el-icon>
              Tạo đơn: {{ formatDate(order?.NgayTaoDon) }}
            </span>
            <span class="dh-meta-item">
              <el-icon><User /></el-icon>
              Nhân viên phụ trách: <strong>{{ order?.tenNhanVien }}</strong>
            </span>
          </div>
          <div class="dh-header-actions">
            <el-button size="small" plain>
              <el-icon><Printer /></el-icon> In đơn hàng
            </el-button>
            <el-button size="small" plain circle @click="handleDong">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- BODY -->
    <div class="dh-body" v-if="order">

      <!-- Cột trái: Khách hàng -->
      <div class="col-left">
        <div class="section-card">
          <div class="section-header">
            <el-icon class="sec-icon"><User /></el-icon>
            <span>Thông tin khách hàng</span>
          </div>

          <div class="kh-identity">
            <div class="kh-avatar">{{ avatarInitials }}</div>
            <div class="kh-name-block">
              <span class="kh-name">{{ order.tenKhachHang }}</span>
              <span v-if="order.loaiKH === 'VIP'" class="kh-vip">⭐ VIP</span>
            </div>
          </div>

          <div class="kh-info-list">
            <div class="kh-info-item">
              <el-icon><Phone /></el-icon>
              <span>{{ order.soDienThoaiKH }}</span>
            </div>
            <div class="kh-info-item">
              <el-icon><Message /></el-icon>
              <span>{{ order.emailKH }}</span>
            </div>
            <div class="kh-info-item">
              <el-icon><Location /></el-icon>
              <span>{{ order.diaChiKH }}</span>
            </div>
          </div>

          <div class="kh-stats">
            <div class="kh-stat-item">
              <span class="stat-label">Tổng số đơn</span>
              <span class="stat-value">{{ order.tongDonKH }} đơn</span>
            </div>
            <div class="kh-stat-item">
              <span class="stat-label">Tổng chi tiêu</span>
              <span class="stat-value red">{{ formatCurrency(order.tongChiTieuKH) }}</span>
            </div>
          </div>
        </div>

        <div class="section-card" v-if="ghiChuLines.length">
          <div class="section-header">
            <el-icon class="sec-icon"><Timer /></el-icon>
            <span>Ghi chú khách hàng</span>
          </div>
          <ul class="note-list">
            <template v-for="(line, i) in ghiChuLines" :key="i">
              <li>{{ line.trim() }}</li>
            </template>
          </ul>
        </div>

        <div class="section-card" v-if="order.ghiChuNoiBo">
          <div class="section-header">
            <el-icon class="sec-icon"><Lock /></el-icon>
            <span>Ghi chú nội bộ</span>
          </div>
          <p class="note-text">{{ order.ghiChuNoiBo }}</p>
        </div>
      </div>

      <!-- Cột giữa: Chi tiết đơn -->
      <div class="col-middle">
        <div class="section-card">
          <div class="section-header">
            <el-icon class="sec-icon"><ShoppingCart /></el-icon>
            <span>Chi tiết đơn hàng</span>
          </div>

          <div class="order-methods">
            <div class="method-item">
              <el-icon class="method-icon"><Lock /></el-icon>
              <div>
                <p class="method-label">Phương thức thanh toán</p>
                <p class="method-value">{{ order.phuongThucThanhToan }}</p>
              </div>
            </div>
            <div class="method-item">
              <el-icon class="method-icon"><Van /></el-icon>
              <div>
                <p class="method-label">Phương thức giao hàng</p>
                <p class="method-value">{{ order.phuongThucGiaoHang }}</p>
              </div>
            </div>
          </div>

          <table class="sp-table">
            <thead>
              <tr>
                <th class="col-sp">Sản phẩm</th>
                <th class="col-gia">Đơn giá</th>
                <th class="col-sl">SL</th>
                <th class="col-tt">Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sp in order.sanPhams" :key="sp.MaSanPham">
                <td>
                  <div class="sp-cell">
                    <img v-if="sp.HinhAnh" :src="sp.HinhAnh" :alt="sp.tenSanPham" class="sp-img" />
                    <div v-else class="sp-img-placeholder">
                      <el-icon><ShoppingCart /></el-icon>
                    </div>
                    <div class="sp-info">
                      <span class="sp-name">{{ sp.tenSanPham }}</span>
                      <span class="sp-sku">SKU: {{ sp.maSKU }}</span>
                      <span class="sp-loai">Phân loại: {{ sp.phanLoai }}</span>
                    </div>
                  </div>
                </td>
                <td class="text-right">{{ formatCurrency(sp.giaTien) }}</td>
                <td class="text-center">{{ sp.SoLuong }}</td>
                <td class="text-right">{{ formatCurrency(sp.thanhTien) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="section-card">
          <div class="order-summary">
            <div class="summary-row">
              <span>Tạm tính</span>
              <span>{{ formatCurrency(tamTinh) }}</span>
            </div>
            <div class="summary-row giam" v-if="order.giamGia">
              <span>Giảm giá</span>
              <span>- {{ formatCurrency(order.giamGia) }}</span>
            </div>
            <div class="summary-row" v-if="order.phiVanChuyen">
              <span>Phí vận chuyển</span>
              <span>{{ formatCurrency(order.phiVanChuyen) }}</span>
            </div>
            <div class="summary-row total-row">
              <span>Tổng cộng</span>
              <span class="total-value">{{ formatCurrency(tongCong) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Cột phải: Lịch sử -->
      <div class="col-right">
        <div class="section-card">
          <div class="section-header">
            <el-icon class="sec-icon"><Timer /></el-icon>
            <span>Lịch sử giao đơn</span>
          </div>

          <div class="timeline">
            <div v-for="(item, idx) in order.lichSu" :key="idx" class="tl-item">
              <div class="tl-line-wrap">
                <div :class="lichSuIconClass(item)">
                  <span v-if="item.done">✓</span>
                  <span v-else class="dot-inner"></span>
                </div>
                <div v-if="idx < order.lichSu.length - 1" class="tl-connector"></div>
              </div>
              <div class="tl-content">
                <div class="tl-title-row">
                  <span class="tl-title" :class="item.done ? 'tl-done' : 'tl-pending'">
                    {{ item.trangThai }}
                  </span>
                  <span v-if="item.thoiGian" class="tl-time">{{ item.thoiGian }}</span>
                  <span v-else class="tl-pending-label">Chưa thực hiện</span>
                </div>
                <p class="tl-desc">{{ item.moTa }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- FOOTER -->
    <template #footer>
      <div class="dh-footer">
        <el-button class="btn-huy" @click="handleHuyDon">
          <el-icon style="margin-right: 6px;"><Close /></el-icon>
          Hủy đơn
        </el-button>
        <el-button class="btn-dong" @click="handleDong">Đóng</el-button>
      </div>
    </template>
  </el-dialog>
</template>
