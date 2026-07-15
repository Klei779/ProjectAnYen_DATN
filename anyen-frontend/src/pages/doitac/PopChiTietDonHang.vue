<script setup>
import { computed } from "vue";
import {
  Close,
  User,
  Phone,
  Message,
  Location,
  Download,
  List,
  Check,
  Van,
  Box,
  CircleCheck,
  Setting,
  Postcard
} from "@element-plus/icons-vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  donHang: { type: Object, default: () => ({}) }
});

const emit = defineEmits(["update:modelValue"]);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val)
});

const handleClose = () => {
  visible.value = false;
};

// Map step name to icon
const getStepIcon = (stepName) => {
  if (stepName === 'Đặt hàng') return List;
  if (stepName === 'Đã xác nhận') return Box;
  if (stepName === 'Đang xử lý') return Setting;
  if (stepName === 'Đang giao') return Van;
  if (stepName === 'Hoàn thành') return CircleCheck;
  return Check;
};

const formatMoney = (val) => {
  if (val == null) return "0";
  return Number(val).toLocaleString('vi-VN');
};

const getActionButtonText = (step) => {
  if (step.trangThai === 'done') return 'Đã hoàn tất';
  if (step.trangThai === 'active') {
    if (step.buoc === 'Đang giao') return 'Xác nhận đã giao';
    if (step.buoc === 'Đang xử lý') return 'Xác nhận xử lý xong';
    return `Xác nhận ${step?.buoc?.toLowerCase() || ''}`;
  }
  return 'Chưa thể thực hiện';
};

const getProgressWidth = (steps) => {
  if (!steps || steps.length <= 1) return '0%';
  const totalSegments = steps.length - 1;
  let activeIndex = steps.findIndex(s => s.trangThai === 'active');
  if (activeIndex === -1) {
    if (steps[steps.length - 1].trangThai === 'done') return '100%';
    return '0%';
  }
  return `${(activeIndex / totalSegments) * 100}%`;
};

</script>

<template>
  <el-dialog
    v-model="visible"
    width="960px"
    class="partner-order-dialog"
    :show-close="false"
    destroy-on-close
    align-center
  >
    <!-- HEADER -->
    <template #header>
      <div class="partner-dh-header">
        <h2 class="partner-dh-title">
          Chi tiết đơn hàng #{{ donHang?.maCode || donHang?.maDonHang }}
        </h2>
        <el-button class="btn-close-icon" text @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </template>

    <!-- BODY -->
    <div class="partner-dh-body" v-if="donHang">

      <!-- Khối thông tin khách hàng & cơ bản -->
      <div class="info-top-block">
        <div class="customer-col">
          <div class="partner-section-title">
            <el-icon><User /></el-icon>
            Thông tin khách hàng
          </div>
          <div class="customer-name-row">
            <span class="c-name">{{ donHang?.tenKhachHang }}</span>
          </div>
          <div class="c-contact-list">
            <div class="c-contact-item" v-if="donHang?.cccd">
              <el-icon><Postcard /></el-icon> CCCD: {{ donHang?.cccd }}
            </div>
            <div class="c-contact-item">
              <el-icon><Phone /></el-icon> {{ donHang?.soDienThoai }}
            </div>
            <div class="c-contact-item">
              <el-icon><Message /></el-icon> {{ donHang?.email }}
            </div>
            <div class="c-contact-item">
              <el-icon><Location /></el-icon> <span>{{ donHang?.diaChi }}</span>
            </div>
          </div>
        </div>

        <div class="basic-info-col">
          <div class="b-row">
            <span class="b-label">Mã đơn hàng</span>
            <span class="b-value fw-600">#{{ donHang?.maCode || donHang?.maDonHang }}</span>
          </div>
          <div class="b-row">
            <span class="b-label">Ngày tạo đơn</span>
            <span class="b-value">{{ donHang?.ngayDat }}</span>
          </div>
          <div class="b-row">
            <span class="b-label">Trạng thái</span>
            <span class="b-value">
              <span class="pay-badge">{{ donHang?.trangThai }}</span>
            </span>
          </div>
          <div class="b-row">
            <span class="b-label">Nhân viên PT</span>
            <span class="b-value">{{ donHang?.nhanVien || 'Chưa phân công' }}</span>
          </div>
          <div class="b-row">
            <span class="b-label">Ghi chú</span>
            <span class="b-value">{{ donHang?.ghiChu || 'Không có ghi chú' }}</span>
          </div>
        </div>
      </div>

      <!-- Khối trạng thái đơn hàng (Timeline) -->
      <div class="partner-timeline-block" v-if="donHang?.trangThaiLichSu">
        <h3 class="partner-block-title">Tiến trình xử lý</h3>
        <div class="steps-container">
          <div 
            v-for="(step, index) in donHang?.trangThaiLichSu" 
            :key="index"
            class="step-item"
            :class="step?.trangThai"
          >
            <!-- Line connector -->
            <div v-if="index < donHang?.trangThaiLichSu?.length - 1" class="step-line"></div>
            
            <div class="step-icon-wrap">
              <el-icon class="step-icon"><component :is="getStepIcon(step?.buoc)" /></el-icon>
            </div>
            <div class="step-name">{{ step?.buoc }}</div>
            <div class="step-time">{{ step?.thoiGian || '—' }}</div>
            
            <button 
              class="step-btn" 
              :class="{
                'btn-done': step?.trangThai === 'done',
                'btn-active': step?.trangThai === 'active',
                'btn-disabled': step?.trangThai === 'pending'
              }"
              :disabled="step?.trangThai === 'pending'"
            >
              {{ getActionButtonText(step) }}
            </button>
          </div>
        </div>
      </div>

      <!-- Khối chi tiết dưới: Thông tin đơn hàng & Sản phẩm -->
      <div class="partner-bottom-blocks">
        
        <div class="partner-left-col">
          <div class="partner-section-card">
            <h3 class="partner-block-title">Thông tin tổng hợp</h3>
            <div class="partner-info-list" style="display: block;">
              <div class="i-row">
                <span class="i-label">Mã đơn hàng</span>
                <span class="i-value fw-600">#{{ donHang?.maCode || donHang?.maDonHang }}</span>
              </div>
              <div class="i-row">
                <span class="i-label">Ngày tạo đơn</span>
                <span class="i-value">{{ donHang?.ngayDat }}</span>
              </div>
              <div class="i-row">
                <span class="i-label">Trạng thái</span>
                <span class="i-value">
                  <span class="pay-badge">{{ donHang?.trangThai }}</span>
                </span>
              </div>
              <div class="i-row">
                <span class="i-label">Nhân viên phụ trách</span>
                <span class="i-value">{{ donHang?.nhanVien || 'Chưa phân công' }}</span>
              </div>
              <div class="i-row">
                <span class="i-label">Địa chỉ phục vụ</span>
                <span class="i-value">{{ donHang?.diaChi }}</span>
              </div>
              <div class="i-row">
                <span class="i-label">Ghi chú</span>
                <span class="i-value">{{ donHang?.ghiChu || 'Không có ghi chú' }}</span>
              </div>
            </div>
          </div>

          <div class="partner-section-card mt-12" v-if="donHang?.tepDinhKem">
            <h3 class="partner-block-title">Tệp đính kèm</h3>
            <div class="attachment-item">
              <div class="att-icon">PDF</div>
              <div class="att-info">
                <div class="att-name">{{ donHang?.tepDinhKem?.ten }}</div>
                <div class="att-size">{{ donHang?.tepDinhKem?.dungLuong }}</div>
              </div>
              <el-button size="small" class="btn-download" text>
                <el-icon size="16"><Download /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div class="partner-right-col">
          <div class="partner-section-card">
            <h3 class="partner-block-title">Sản phẩm / Dịch vụ</h3>
            <div class="partner-table-wrap" style="display: block;">
              <table class="sp-table">
                <thead>
                  <tr>
                    <th width="40">STT</th>
                    <th>Sản phẩm / Dịch vụ</th>
                    <th class="text-center" width="80">Số lượng</th>
                    <th class="text-right" width="110">Đơn giá</th>
                    <th class="text-right" width="110">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(sp, idx) in (donHang?.sanPhams || [])" :key="idx">
                    <td>{{ sp?.stt || idx + 1 }}</td>
                    <td class="sp-name-cell">{{ sp?.ten || sp?.tenSanPham }}</td>
                    <td class="text-center">{{ sp?.soLuong || sp?.SoLuong || 1 }}</td>
                    <td class="text-right">{{ formatMoney(sp?.donGia || sp?.giaTien) }}đ</td>
                    <td class="text-right fw-600">{{ formatMoney(sp?.thanhTien) }}đ</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="order-summary-box">
              <div class="sum-row total-row">
                <span class="s-label fw-bold">Tổng tiền</span>
                <span class="s-val total-val">{{ formatMoney(donHang?.tongCong) }}đ</span>
              </div>
            </div>
          </div>
        </div>

      </div>

    </div>

    <!-- FOOTER -->
    <template #footer>
      <div class="partner-dh-footer">
        <el-button class="btn-close-bottom" @click="handleClose">Đóng</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style>
/* Override element plus dialog padding/margins to make it compact and fit screen */
.partner-order-dialog {
  display: flex !important;
  flex-direction: column !important;
  max-height: 90vh !important;
  margin-top: 5vh !important;
  margin-bottom: 5vh !important;
  border-radius: 12px;
  overflow: hidden;
}
.partner-order-dialog .el-dialog__header {
  padding: 16px 24px !important;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}
.partner-order-dialog .el-dialog__body {
  padding: 20px 24px !important;
  background: #fdfdfd;
  overflow-y: auto; /* Adds internal scroll if necessary */
  flex: 1;
}
.partner-order-dialog .el-dialog__footer {
  padding: 12px 24px !important;
  border-top: 1px solid #f0f0f0;
}
</style>

<style scoped>
/* Typography & Layout base */
.fw-600 { font-weight: 600; }
.fw-bold { font-weight: 700; }
.text-center { text-align: center; }
.text-right { text-align: right; }
.mt-12 { margin-top: 16px; }

/* HEADER */
.partner-dh-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.partner-dh-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}
.btn-close-icon {
  font-size: 18px;
  padding: 2px;
  color: #888;
}

/* BODY Layout */
.partner-dh-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: 'Inter', Arial, sans-serif;
  color: #333;
}

/* Info Top Block */
.info-top-block {
  display: flex;
  gap: 40px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px 20px;
}
.customer-col {
  flex: 1;
}
.basic-info-col {
  flex: 1;
}

.partner-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 12px;
  color: #1a1a2e;
}
.customer-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.c-name {
  font-weight: 600;
  font-size: 14px;
  color: #1a1a2e;
}
.c-badge {
  background: #e6f7ef;
  color: #0fb365;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.c-contact-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.c-contact-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: #444;
  line-height: 1.4;
}
.c-contact-item .el-icon {
  color: #888;
  margin-top: 2px;
}

.b-row, .i-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 12px;
  line-height: 1.4;
}
.b-label, .i-label {
  width: 160px;
  color: #666;
  display: inline-block;
}
.b-value, .i-value {
  flex: 1;
  color: #222;
  display: inline-block;
}
.pay-badge {
  background: #e6f7ef;
  color: #0fb365;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}

/* Timeline */
.partner-timeline-block {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px 20px;
}
.partner-block-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #1a1a2e;
}
.steps-container {
  display: flex;
  position: relative;
  padding: 0 10px;
}
.step-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  z-index: 1;
}
.step-line {
  position: absolute;
  top: 16px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #eee;
  z-index: -1;
}
.step-item.done .step-line {
  background: #0fb365;
}
.step-item.active .step-line {
  background: #eee;
}

.step-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  color: #999;
  font-size: 14px;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #ddd;
}
.step-item.done .step-icon-wrap {
  background: #e6f7ef;
  color: #0fb365;
  box-shadow: 0 0 0 1px #0fb365;
}
.step-item.active .step-icon-wrap {
  background: #e6f7ef;
  color: #0fb365;
  box-shadow: 0 0 0 1px #0fb365;
}

.step-name {
  font-size: 12px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  text-align: center;
}
.step-time {
  font-size: 11px;
  color: #888;
  margin-bottom: 8px;
  text-align: center;
  height: 14px;
}

.step-btn {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  padding: 4px 0;
  font-size: 11px;
  cursor: pointer;
  width: 120px;
  color: #666;
  text-align: center;
}
.step-btn.btn-done {
  background: #f8f8f8;
  color: #999;
  cursor: default;
  border-color: #eee;
}
.step-btn.btn-active {
  border-color: #f15249;
  color: #f15249;
  font-weight: 600;
}
.step-btn.btn-active:hover {
  background: #fff1f0;
}
.step-btn.btn-disabled {
  background: #fff;
  color: #ccc;
  border-color: #eee;
  cursor: not-allowed;
}

/* Bottom Blocks */
.partner-bottom-blocks {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.partner-left-col {
  width: 380px;
  display: flex;
  flex-direction: column;
}
.partner-right-col {
  flex: 1;
}

.partner-section-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px 20px;
  width: 100%;
  box-sizing: border-box;
}

/* Attachment */
.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #eee;
  border-radius: 6px;
}
.att-icon {
  background: #fce4e4;
  color: #d32f2f;
  font-size: 11px;
  font-weight: 700;
  padding: 6px 8px;
  border-radius: 4px;
}
.att-info {
  flex: 1;
}
.att-name {
  font-size: 12px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}
.att-size {
  font-size: 11px;
  color: #888;
}
.btn-download {
  color: #555;
}

/* Table products */
.partner-table-wrap {
  width: 100%;
  margin-bottom: 16px;
  overflow-x: auto;
}
.sp-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.sp-table th {
  padding: 0 4px 12px 4px;
  border-bottom: 1px solid #eee;
  color: #1a1a2e;
  font-weight: 600;
  text-align: left;
}
.sp-table td {
  padding: 12px 4px;
  color: #333;
}
.sp-name-cell {
  font-weight: 500;
}

/* Summary Box */
.order-summary-box {
  width: 280px;
  margin-left: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.sum-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #333;
}
.total-row {
  border-top: 1px solid #eee;
  padding-top: 8px;
  margin-top: 4px;
}
.total-val {
  font-size: 14px;
  font-weight: 700;
  color: #d32f2f;
}

/* Footer */
.partner-dh-footer {
  display: flex;
  justify-content: flex-end;
}
.btn-close-bottom {
  padding: 6px 20px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
