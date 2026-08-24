<template>
  <div class="debt-payment-page">
    <button
        class="sidebar-toggle-btn"
        @click="sidebarOpen = !sidebarOpen"
        :title="sidebarOpen ? 'Thu gọn' : 'Mở tóm tắt'"
    >
      <i :class="sidebarOpen ? 'fa-solid fa-chevron-right' : 'fa-solid fa-chart-pie'"></i>
      <span v-if="!sidebarOpen" class="toggle-label">Tóm tắt</span>
    </button>

    <div class="main-content">
      <div class="stats-row">
        <div class="stat-card">
          <div class="icon-box red-light">
            <i class="fa-solid fa-file-invoice-dollar text-red"></i>
          </div>

          <div class="stat-info">
            <p class="stat-title">Tổng công nợ phải trả</p>

            <h3 class="stat-value text-red">
              {{ formatCurrency(summaryData.tongCongNoPhaiTra) }}
            </h3>

            <p class="stat-desc">
              {{ summaryData.soDoiTacConNo }} đối tác
            </p>
          </div>
        </div>

        <div class="stat-card">
          <div class="icon-box yellow-light">
            <i class="fa-regular fa-calendar text-yellow"></i>
          </div>

          <div class="stat-info">
            <p class="stat-title">
              Đến hạn trong 7 ngày
            </p>

            <h3 class="stat-value text-black">
              {{ formatCurrency(summaryData.denHanThanhToan) }}
            </h3>

            <p class="stat-desc">
              {{ summaryData.soDoiTacDenHan }} đối tác
            </p>
          </div>
        </div>

        <div class="stat-card">
          <div class="icon-box blue-light">
            <i class="fa-regular fa-clock text-blue"></i>
          </div>

          <div class="stat-info">
            <p class="stat-title">
              Quá hạn thanh toán
            </p>

            <h3 class="stat-value text-black">
              {{ formatCurrency(summaryData.quaHanThanhToan) }}
            </h3>

            <p class="stat-desc">
              {{ summaryData.soDoiTacQuaHan }} đối tác
            </p>
          </div>
        </div>

        <div class="stat-card">
          <div class="icon-box green-light">
            <i class="fa-regular fa-circle-check text-green"></i>
          </div>

          <div class="stat-info">
            <p class="stat-title">
              Đã thanh toán tháng này
            </p>

            <h3 class="stat-value text-black">
              {{ formatCurrency(summaryData.daThanhToanThangNay) }}
            </h3>

            <p class="stat-desc">
              {{ summaryData.soGiaoDichThangNay }} giao dịch
            </p>
          </div>
        </div>
      </div>

      <!-- ========================= -->
      <!-- FILTER -->
      <!-- ========================= -->

      <div class="filter-section">
        <div class="search-box">
          <el-input
              v-model="searchKeyword"
              placeholder="Tìm đối tác, mã công nợ, mã đơn hàng..."
              :prefix-icon="Search"
              clearable
          />
        </div>

        <div class="filter-dropdowns">
          <el-select
              v-model="statusFilter"
              style="width: 185px"
          >
            <el-option
                label="Trạng thái: Tất cả"
                value="all"
            />

            <el-option
                label="Chưa thanh toán"
                value="unpaid"
            />

            <el-option
                label="Thanh toán một phần"
                value="partial"
            />

            <el-option
                label="Đã thanh toán"
                value="paid"
            />

            <el-option
                label="Quá hạn"
                value="overdue"
            />
          </el-select>

          <el-select
              v-model="partnerFilter"
              style="width: 190px"
          >
            <el-option
                label="Đối tác: Tất cả"
                value="all"
            />

            <el-option
                v-for="partner in partnerOptions"
                :key="partner.maDoiTac"
                :label="partner.tenDoiTac"
                :value="String(partner.maDoiTac)"
            />
          </el-select>

          <el-select
              v-model="timeFilter"
              style="width: 180px"
          >
            <el-option
                label="Thời gian: Tất cả"
                value="all"
            />

            <el-option
                label="Tháng này"
                value="this_month"
            />
          </el-select>
        </div>

      </div>

      <!-- ========================= -->
      <!-- TABLE -->
      <!-- ========================= -->

      <div
          class="table-container"
          v-loading="loading"
      >
        <table class="debt-table">
          <thead>
          <tr>
            <th>Đối tác</th>
            <th>Mã công nợ</th>
            <th>Đơn hàng</th>
            <th>Ngày đến hạn</th>
            <th>Số tiền phải trả</th>
            <th>Công nợ còn lại</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
          </thead>

          <tbody>
          <tr
              v-for="item in paginatedDebts"
              :key="item.maCongNo"
          >
            <!-- ĐỐI TÁC -->
            <td>
              <div class="partner-cell">
                <div class="partner-logo">
                    <span>
                      {{ getPartnerInitials(item.tenDoiTac) }}
                    </span>
                </div>

                <div>
                  <div class="partner-name">
                    {{ item.tenDoiTac || "Không xác định" }}
                  </div>

                  <div class="partner-code">
                    Mã đối tác: DT{{ item.maDoiTac }}
                  </div>
                </div>
              </div>
            </td>

            <!-- MÃ CÔNG NỢ -->
            <td>
              <div class="invoice-no">
                CN{{ item.maCongNo }}
              </div>

              <div class="date-sub">
                {{ formatDate(item.createdAt) }}
              </div>
            </td>

            <!-- ĐƠN HÀNG -->
            <td>
              <div class="order-no">
                DH{{ item.maDonHang }}
              </div>

              <div class="date-sub">
                {{ formatDate(item.ngayTaoDon) }}
              </div>
            </td>

            <!-- HẠN THANH TOÁN -->
            <td>
              <div class="due-date">
                {{ formatDate(item.hanThanhToan) }}
              </div>

              <div
                  :class="[
                    'days-left',
                    getDueTextClass(item),
                  ]"
              >
                {{ getDueText(item) }}
              </div>
            </td>

            <!-- TỔNG TIỀN -->
            <td>
              <b>
                {{ formatCurrency(item.tongTien) }}
              </b>
            </td>

            <!-- CÒN LẠI -->
            <td>
              <b class="text-red">
                {{ formatCurrency(item.conLai) }}
              </b>
            </td>

            <!-- TRẠNG THÁI -->
            <td>
                <span
                    :class="[
                    'status-badge',
                    getBadgeClass(item),
                  ]"
                >
                  <span class="dot-status"></span>

                  {{ getEffectiveStatusText(item) }}
                </span>
            </td>

            <!-- THAO TÁC -->
            <td>
              <div class="action-buttons">
                <el-button
                    size="small"
                    class="btn-action-thanh-toan"
                    :disabled="Number(item.conLai) <= 0"
                    @click="openPaymentDialog(item)"
                >
                  Thanh toán
                </el-button>

                <el-button
                    size="small"
                    :icon="MoreFilled"
                    class="btn-more"
                    style="border: none"
                    @click="showDebtInfo(item)"
                />
              </div>
            </td>
          </tr>

          <tr
              v-if="
                !loading &&
                paginatedDebts.length === 0
              "
          >
            <td
                colspan="8"
                class="empty-state"
            >
              Không có công nợ phù hợp.
            </td>
          </tr>
          </tbody>
        </table>

        <!-- ========================= -->
        <!-- PAGINATION -->
        <!-- ========================= -->

        <div class="pagination-container">
          <span class="page-info">
            Hiển thị
            {{ pageStart }}
            đến
            {{ pageEnd }}
            trong tổng số
            {{ filteredDebts.length }}
            kết quả
          </span>

          <div class="pagination-controls">
            <el-pagination
                v-model:current-page="currentPage"
                background
                layout="prev, pager, next"
                :total="filteredDebts.length"
                :page-size="pageSize"
            />

            <el-select
                v-model="pageSize"
                style="
                width: 120px;
                margin-left: 10px;
              "
                @change="currentPage = 1"
            >
              <el-option
                  label="5 / trang"
                  :value="5"
              />

              <el-option
                  label="10 / trang"
                  :value="10"
              />

              <el-option
                  label="20 / trang"
                  :value="20"
              />
            </el-select>
          </div>
        </div>
      </div>

      <!-- ========================= -->
      <!-- QUY TRÌNH -->
      <!-- ========================= -->

      <div class="process-section">
        <h6 class="process-title">
          Quy trình thanh toán công nợ
        </h6>

        <div class="process-steps">
          <div class="step">
            <div class="step-icon-wrap">
              <span class="step-num bg-red text-white">
                1
              </span>

              <div class="step-icon">
                <i class="fa-solid fa-file-invoice"></i>
              </div>
            </div>

            <h6>Ghi nhận công nợ</h6>

            <p>
              Công nợ được tạo theo đơn hàng và phần sản phẩm
              thuộc từng đối tác.
            </p>
          </div>

          <div class="step-arrow">
            <i class="fa-solid fa-chevron-right"></i>
          </div>

          <div class="step">
            <div class="step-icon-wrap">
              <span class="step-num bg-red text-white">
                2
              </span>

              <div class="step-icon">
                <i class="fa-solid fa-money-bill-transfer"></i>
              </div>
            </div>

            <h6>Chọn số tiền</h6>

            <p>
              Admin có thể thanh toán một phần hoặc toàn bộ
              số công nợ còn lại.
            </p>
          </div>

          <div class="step-arrow">
            <i class="fa-solid fa-chevron-right"></i>
          </div>

          <div class="step">
            <div class="step-icon-wrap">
              <span class="step-num bg-red text-white">
                3
              </span>

              <div class="step-icon">
                <i class="fa-solid fa-qrcode"></i>
              </div>
            </div>

            <h6>Tạo QR</h6>

            <p>
              Hệ thống tạo mã giao dịch.
            </p>
          </div>

          <div class="step-arrow">
            <i class="fa-solid fa-chevron-right"></i>
          </div>

          <div class="step">
            <div class="step-icon-wrap">
              <span class="step-num bg-red text-white">
                4
              </span>

              <div class="step-icon">
                <i class="fa-solid fa-circle-check"></i>
              </div>
            </div>

            <h6>Xác nhận & cập nhật</h6>

            <p>
              Callback thành công mới cập nhật công nợ và
              lưu lịch sử thanh toán.
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- ====================================================== -->
    <!-- SIDEBAR -->
    <!-- ====================================================== -->

    <transition name="sidebar-slide">
      <div
          class="right-sidebar"
          v-show="sidebarOpen"
      >
        <div class="sidebar-header">
          <div class="sidebar-tabs">
            <button
                class="sidebar-tab"
                :class="{
                active: activeTab === 'summary',
              }"
                @click="activeTab = 'summary'"
            >
              <i class="fa-solid fa-chart-pie"></i>

              Tóm tắt
            </button>

            <button
                class="sidebar-tab"
                :class="{
                active: activeTab === 'history',
              }"
                @click="activeTab = 'history'"
            >
              <i class="fa-solid fa-clock-rotate-left"></i>

              Lịch sử
            </button>
          </div>

          <button
              class="sidebar-close-btn"
              @click="sidebarOpen = false"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <!-- ========================= -->
        <!-- TAB TÓM TẮT -->
        <!-- ========================= -->

        <div
            v-show="activeTab === 'summary'"
            class="sidebar-tab-content"
        >
          <div class="summary-chart-card">
            <div class="chart-container">
              <div
                  class="donut-chart"
                  :style="donutStyle"
              >
                <div class="donut-inner">
                  <span class="donut-val">
                    {{
                      formatMillions(
                          summaryData.tongCongNoPhaiTra
                      )
                    }}
                  </span>

                  <span class="donut-label">
                    Triệu đồng
                  </span>
                </div>
              </div>
            </div>

            <div class="chart-legend">
              <div class="legend-item">
                <div>
                  <span class="dot bg-blue"></span>

                  Chưa đến hạn
                </div>

                <span class="legend-val">
                  {{ formatCurrency(notDueAmount) }}
                </span>
              </div>

              <div class="legend-item">
                <div>
                  <span class="dot bg-yellow"></span>

                  Đến hạn 7 ngày
                </div>

                <span class="legend-val">
                  {{
                    formatCurrency(
                        summaryData.denHanThanhToan
                    )
                  }}
                </span>
              </div>

              <div class="legend-item">
                <div>
                  <span class="dot bg-red"></span>

                  Quá hạn
                </div>

                <span class="legend-val">
                  {{
                    formatCurrency(
                        summaryData.quaHanThanhToan
                    )
                  }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- ========================= -->
        <!-- TAB LỊCH SỬ -->
        <!-- ========================= -->

        <div
            v-show="activeTab === 'history'"
            class="sidebar-tab-content"
        >
          <div class="recent-history-card">
            <div
                v-if="historyData.length"
                class="history-list"
            >
              <div
                  class="history-item"
                  v-for="h in historyData"
                  :key="h.maLichSuCongNo"
              >
                <div class="h-icon bg-red-light">
                  <i class="fa-solid fa-bolt text-red"></i>
                </div>

                <div class="h-info">
                  <h6>
                    {{ h.tenDoiTac }}
                  </h6>

                  <small>
                    {{ h.phuongThucThanhToanText }}
                    -
                    {{ formatDateTime(h.ngayThanhToan) }}
                  </small>
                </div>

                <div class="h-amount text-green">
                  -{{ formatCurrency(h.soTienThanhToan) }}
                </div>
              </div>
            </div>

            <div
                v-else
                class="empty-history"
            >
              Chưa có lịch sử thanh toán.
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- ====================================================== -->
    <!-- DIALOG THANH TOÁN -->
    <!-- ====================================================== -->

    <el-dialog
        v-model="paymentDialogVisible"
        title="Thanh toán công nợ"
        width="520px"
        destroy-on-close
    >
      <div class="payment-dialog-body">
        <!-- ========================= -->
        <!-- CHỌN CÔNG NỢ -->
        <!-- ========================= -->

        <div class="payment-field">
          <label>
            Khoản công nợ
            <span class="required">*</span>
          </label>

          <el-select
              v-model="selectedDebtId"
              placeholder="Chọn công nợ cần thanh toán"
              filterable
              style="width: 100%"
          >
            <el-option
                v-for="item in payableDebts"
                :key="item.maCongNo"
                :label="
                `CN${item.maCongNo} - ${item.tenDoiTac} - còn ${formatCurrency(
                  item.conLai
                )}`
              "
                :value="item.maCongNo"
            />
          </el-select>
        </div>

        <!-- ========================= -->
        <!-- THÔNG TIN CÔNG NỢ -->
        <!-- ========================= -->

        <div
            v-if="selectedDebt"
            class="selected-debt-card"
        >
          <div>
            <span>Đối tác</span>

            <strong>
              {{ selectedDebt.tenDoiTac }}
            </strong>
          </div>

          <div>
            <span>Đơn hàng</span>

            <strong>
              DH{{ selectedDebt.maDonHang }}
            </strong>
          </div>

          <div>
            <span>Tổng công nợ</span>

            <strong>
              {{
                formatCurrency(
                    selectedDebt.tongTien
                )
              }}
            </strong>
          </div>

          <div>
            <span>Đã thanh toán</span>

            <strong>
              {{
                formatCurrency(
                    selectedDebt.daThanhToan
                )
              }}
            </strong>
          </div>

          <div class="remaining-row">
            <span>Còn phải trả</span>

            <strong class="text-red">
              {{
                formatCurrency(
                    selectedDebt.conLai
                )
              }}
            </strong>
          </div>
        </div>

        <!-- ========================= -->
        <!-- SỐ TIỀN -->
        <!-- ========================= -->

        <div class="payment-field">
          <div class="payment-label-row">
            <label>
              Số tiền thanh toán
              <span class="required">*</span>
            </label>

            <el-button
                v-if="selectedDebt"
                link
                type="danger"
                @click="payFullAmount"
            >
              Thanh toán toàn bộ
            </el-button>
          </div>

          <el-input-number
              v-model="paymentAmount"
              :min="1000"
              :max="
              selectedDebt
                ? Number(selectedDebt.conLai)
                : 1000
            "
              :step="100000"
              :precision="0"
              controls-position="right"
              style="width: 100%"
          />

          <div class="amount-preview">
            {{ formatCurrency(paymentAmount) }}
          </div>
        </div>

        <el-alert
            type="info"
            :closable="false"
            show-icon
            title="Payoo Mock phục vụ demo: tạo giao dịch trước, chỉ khi xác nhận callback thì công nợ mới thay đổi."
        />
      </div>

      <template #footer>
        <el-button
            @click="
            paymentDialogVisible = false
          "
        >
          Hủy
        </el-button>

        <el-button
            type="danger"
            :loading="creatingPayment"
            @click="createPayooPayment"
        >
          Tạo thanh toán Payoo
        </el-button>
      </template>
    </el-dialog>

    <!-- ====================================================== -->
    <!-- DIALOG PAYOO QR -->
    <!-- ====================================================== -->

    <el-dialog
        v-model="qrDialogVisible"
        title="Payoo Mock - Thanh toán công nợ"
        width="500px"
        :close-on-click-modal="false"
        :close-on-press-escape="
        payooStatus !== 'processing'
      "
        :show-close="
        payooStatus !== 'processing'
      "
        @closed="resetPayoo"
    >
      <div class="payoo-box">
        <!-- ========================= -->
        <!-- ĐANG CHỜ THANH TOÁN -->
        <!-- ========================= -->

        <template
            v-if="
            payooStatus !== 'success'
          "
        >
          <div class="payoo-logo">
            PAYOO MOCK
          </div>

          <p class="payoo-description">
            Nhấn vào QR hoặc nút xác nhận bên dưới để giả lập
            Payoo callback thành công.
          </p>

          <img
              v-if="qrImage"
              :src="qrImage"
              class="payoo-qr"
              alt="QR Payoo Mock"
              @click="confirmPayment"
          />

          <div class="payoo-amount">
            {{
              formatCurrency(
                  currentTransaction?.soTien
              )
            }}
          </div>

          <div class="payoo-code">
            Mã giao dịch:

            <strong>
              {{
                currentTransaction?.maGiaoDich
              }}
            </strong>
          </div>

          <div
              v-if="
              payooStatus === 'processing'
            "
              class="payoo-processing"
          >
            <i class="fa-solid fa-spinner fa-spin"></i>

            Payoo đang xử lý giao dịch...
          </div>
        </template>

        <!-- ========================= -->
        <!-- THÀNH CÔNG -->
        <!-- ========================= -->

        <template v-else>
          <div class="payoo-success-icon">
            <i class="fa-solid fa-circle-check"></i>
          </div>

          <h3>
            Thanh toán thành công
          </h3>

          <p>
            Công nợ đã được cập nhật và giao dịch đã được lưu
            vào lịch sử.
          </p>

          <div class="payoo-code">
            Mã giao dịch:

            <strong>
              {{
                currentTransaction?.maGiaoDich
              }}
            </strong>
          </div>
        </template>
      </div>

      <template #footer>
        <el-button
            v-if="
            payooStatus === 'success'
          "
            type="primary"
            @click="
            qrDialogVisible = false
          "
        >
          Đóng
        </el-button>

        <template v-else>
          <el-button
              :disabled="
              payooStatus === 'processing'
            "
              @click="
              qrDialogVisible = false
            "
          >
            Hủy
          </el-button>

          <el-button
              type="danger"
              :loading="
              payooStatus === 'processing'
            "
              @click="confirmPayment"
          >
            Xác nhận đã thanh toán
          </el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  computed,
  onMounted,
  ref,
  watch,
} from "vue";

import {
  ElMessage,
  ElMessageBox,
} from "element-plus";

import {
  MoreFilled,
  Search,
} from "@element-plus/icons-vue";

import QRCode from "qrcode";

import {
  formatCurrency,
  formatDate,
  getCongNoList,
  getLichSuCongNo,
  getTongQuanCongNo,
  taoThanhToanCongNoPayoo,
} from "../../services/congNoService.js";

import {
  confirmPayooTransaction,
} from "../../services/payooMockService.js";

// ======================================================
// SIDEBAR
// ======================================================

const sidebarOpen = ref(true);

const activeTab = ref("summary");

// ======================================================
// LOADING
// ======================================================

const loading = ref(false);

// ======================================================
// DATA
// ======================================================

const allDebts = ref([]);

const historyData = ref([]);

const backendSummary = ref(null);

// ======================================================
// FILTER
// ======================================================

const searchKeyword = ref("");

const statusFilter = ref("all");

const partnerFilter = ref("all");

const timeFilter = ref("all");

// ======================================================
// PAGINATION
// ======================================================

const currentPage = ref(1);

const pageSize = ref(10);

// ======================================================
// THANH TOÁN
// ======================================================

const paymentDialogVisible = ref(false);

const selectedDebtId = ref(null);

const paymentAmount = ref(1000);

const creatingPayment = ref(false);

// ======================================================
// PAYOO
// ======================================================

const qrDialogVisible = ref(false);

const qrImage = ref("");

const currentTransaction = ref(null);

const payooStatus = ref("waiting");

// ======================================================
// CÔNG NỢ CÓ THỂ THANH TOÁN
// ======================================================

const payableDebts = computed(() =>
    allDebts.value.filter(
        (item) =>
            Number(
                item.conLai || 0
            ) > 0
    )
);

// ======================================================
// CÔNG NỢ ĐANG CHỌN
// ======================================================

const selectedDebt = computed(
    () =>
        allDebts.value.find(
            (item) =>
                Number(item.maCongNo) ===
                Number(selectedDebtId.value)
        ) || null
);

// ======================================================
// DANH SÁCH ĐỐI TÁC FILTER
// ======================================================

const partnerOptions = computed(() => {
  const map = new Map();

  allDebts.value.forEach(
      (item) => {
        if (
            item.maDoiTac !== null &&
            item.maDoiTac !== undefined
        ) {
          map.set(
              String(item.maDoiTac),
              {
                maDoiTac:
                item.maDoiTac,

                tenDoiTac:
                    item.tenDoiTac ||
                    `Đối tác ${item.maDoiTac}`,
              }
          );
        }
      }
  );

  return Array.from(
      map.values()
  ).sort(
      (a, b) =>
          a.tenDoiTac.localeCompare(
              b.tenDoiTac,
              "vi"
          )
  );
});

// ======================================================
// FILTER DATA
// ======================================================

const filteredDebts = computed(() => {
  const keyword =
      searchKeyword.value
          .trim()
          .toLowerCase();

  return allDebts.value.filter(
      (item) => {
        // =========================
        // SEARCH
        // =========================

        if (keyword) {
          const haystack = [
            item.tenDoiTac,
            item.maCongNo,
            item.maDonHang,
            item.maDoiTac,
            item.ghiChu,
          ]
              .join(" ")
              .toLowerCase();

          if (
              !haystack.includes(
                  keyword
              )
          ) {
            return false;
          }
        }

        // =========================
        // ĐỐI TÁC
        // =========================

        if (
            partnerFilter.value !==
            "all" &&
            String(item.maDoiTac) !==
            partnerFilter.value
        ) {
          return false;
        }

        // =========================
        // TRẠNG THÁI
        // =========================

        const effectiveStatus =
            getEffectiveStatusKey(
                item
            );

        if (
            statusFilter.value !==
            "all" &&
            effectiveStatus !==
            statusFilter.value
        ) {
          return false;
        }

        // =========================
        // THỜI GIAN
        // =========================

        if (
            timeFilter.value ===
            "this_month" &&
            !isCurrentMonth(
                item.createdAt ||
                item.ngayTaoDon
            )
        ) {
          return false;
        }

        return true;
      }
  );
});

// ======================================================
// PHÂN TRANG
// ======================================================

const paginatedDebts = computed(
    () => {
      const start =
          (currentPage.value - 1) *
          pageSize.value;

      return filteredDebts.value.slice(
          start,
          start + pageSize.value
      );
    }
);

const pageStart = computed(() => {
  if (
      filteredDebts.value.length ===
      0
  ) {
    return 0;
  }

  return (
      (currentPage.value - 1) *
      pageSize.value +
      1
  );
});

const pageEnd = computed(() =>
    Math.min(
        currentPage.value *
        pageSize.value,

        filteredDebts.value.length
    )
);

// ======================================================
// SUMMARY
// ======================================================

const summaryData = computed(
    () =>
        backendSummary.value ||
        buildLocalSummary()
);

// ======================================================
// CHƯA ĐẾN HẠN
// ======================================================

const notDueAmount = computed(() =>
    Math.max(
        0,

        Number(
            summaryData.value
                .tongCongNoPhaiTra || 0
        ) -
        Number(
            summaryData.value
                .denHanThanhToan || 0
        ) -
        Number(
            summaryData.value
                .quaHanThanhToan || 0
        )
    )
);

// ======================================================
// DONUT CHART
// ======================================================

const donutStyle = computed(() => {
  const total = Number(
      summaryData.value
          .tongCongNoPhaiTra || 0
  );

  if (total <= 0) {
    return {
      background: "#e5e7eb",
    };
  }

  const notDuePercent =
      (notDueAmount.value /
          total) *
      100;

  const duePercent =
      (Number(
              summaryData.value
                  .denHanThanhToan || 0
          ) /
          total) *
      100;

  const dueEnd =
      notDuePercent +
      duePercent;

  return {
    background:
        `conic-gradient(` +
        `#2196f3 0% ${notDuePercent}%, ` +
        `#ffb300 ${notDuePercent}% ${dueEnd}%, ` +
        `#f44336 ${dueEnd}% 100%)`,
  };
});

// ======================================================
// WATCH FILTER
// ======================================================

watch(
    [
      searchKeyword,
      statusFilter,
      partnerFilter,
      timeFilter,
    ],

    () => {
      currentPage.value = 1;
    }
);

// ======================================================
// KHI ĐỔI CÔNG NỢ
// ======================================================

watch(
    selectedDebtId,
    () => {
      if (
          selectedDebt.value
      ) {
        paymentAmount.value =
            Math.max(
                1000,

                Number(
                    selectedDebt.value
                        .conLai || 0
                )
            );
      }
    }
);

// ======================================================
// MOUNT
// ======================================================

onMounted(() => {
  loadPageData();
});

// ======================================================
// LOAD TOÀN BỘ DỮ LIỆU
// ======================================================

async function loadPageData() {
  loading.value = true;

  try {
    await loadAllDebts();

    await Promise.all([
      loadSummary(),
      loadHistory(),
    ]);
  } catch (error) {
    console.error(
        "Lỗi tải công nợ:",
        error
    );

    ElMessage.error(
        getErrorMessage(
            error,
            "Không tải được dữ liệu công nợ"
        )
    );
  } finally {
    loading.value = false;
  }
}

// ======================================================
// LOAD TẤT CẢ CÔNG NỢ
// ======================================================

async function loadAllDebts() {
  const firstPage =
      await getCongNoList({
        page: 0,
        size: 100,
      });

  const items = [
    ...firstPage.items,
  ];

  const totalPages =
      Math.ceil(
          Number(
              firstPage.total || 0
          ) / 100
      );

  if (
      totalPages > 1
  ) {
    const requests = [];

    for (
        let page = 1;
        page < totalPages;
        page += 1
    ) {
      requests.push(
          getCongNoList({
            page,
            size: 100,
          })
      );
    }

    const pages =
        await Promise.all(
            requests
        );

    pages.forEach(
        (result) =>
            items.push(
                ...result.items
            )
    );
  }

  allDebts.value = items;

  const maxPage =
      Math.max(
          1,

          Math.ceil(
              filteredDebts.value
                  .length /
              pageSize.value
          )
      );

  if (
      currentPage.value >
      maxPage
  ) {
    currentPage.value =
        maxPage;
  }
}

// ======================================================
// LOAD SUMMARY
// ======================================================

async function loadSummary() {
  try {
    backendSummary.value =
        await getTongQuanCongNo();
  } catch (error) {
    console.warn(
        "Không tải được tổng quan, dùng dữ liệu cục bộ:",
        error
    );

    backendSummary.value =
        null;
  }
}

// ======================================================
// LOAD LỊCH SỬ
// ======================================================

async function loadHistory() {
  try {
    const result =
        await getLichSuCongNo({
          page: 0,
          size: 10,
        });

    historyData.value =
        result.items;
  } catch (error) {
    console.warn(
        "Không tải được lịch sử công nợ:",
        error
    );

    historyData.value = [];
  }
}

// ======================================================
// SUMMARY LOCAL
// ======================================================

function buildLocalSummary() {
  const today =
      startOfDay(
          new Date()
      );

  const dueLimit =
      new Date(today);

  dueLimit.setDate(
      dueLimit.getDate() + 7
  );

  let total = 0;

  let due = 0;

  let overdue = 0;

  const partners =
      new Set();

  const duePartners =
      new Set();

  const overduePartners =
      new Set();

  allDebts.value.forEach(
      (item) => {
        const remain =
            Number(
                item.conLai || 0
            );

        if (
            remain <= 0
        ) {
          return;
        }

        total += remain;

        partners.add(
            item.maDoiTac
        );

        const dueDate =
            parseDate(
                item.hanThanhToan
            );

        if (!dueDate) {
          return;
        }

        if (
            dueDate < today
        ) {
          overdue += remain;

          overduePartners.add(
              item.maDoiTac
          );
        } else if (
            dueDate <=
            dueLimit
        ) {
          due += remain;

          duePartners.add(
              item.maDoiTac
          );
        }
      }
  );

  const paidThisMonth =
      historyData.value
          .filter(
              (item) =>
                  isCurrentMonth(
                      item.ngayThanhToan
                  )
          )
          .reduce(
              (sum, item) =>
                  sum +
                  Number(
                      item.soTienThanhToan ||
                      0
                  ),

              0
          );

  return {
    tongCongNoPhaiTra:
    total,

    soDoiTacConNo:
    partners.size,

    denHanThanhToan:
    due,

    soDoiTacDenHan:
    duePartners.size,

    quaHanThanhToan:
    overdue,

    soDoiTacQuaHan:
    overduePartners.size,

    daThanhToanThangNay:
    paidThisMonth,

    soGiaoDichThangNay:
    historyData.value.filter(
        (item) =>
            isCurrentMonth(
                item.ngayThanhToan
            )
    ).length,
  };
}

// ======================================================
// MỞ DIALOG THANH TOÁN
// ======================================================

function openPaymentDialog(
    item = null
) {
  const debt =
      item ||
      payableDebts.value[0];

  if (!debt) {
    ElMessage.warning(
        "Hiện không có công nợ cần thanh toán"
    );

    return;
  }

  selectedDebtId.value =
      debt.maCongNo;

  paymentAmount.value =
      Number(
          debt.conLai || 0
      );

  paymentDialogVisible.value =
      true;
}

// ======================================================
// THANH TOÁN TOÀN BỘ
// ======================================================

function payFullAmount() {
  if (
      !selectedDebt.value
  ) {
    return;
  }

  paymentAmount.value =
      Number(
          selectedDebt.value
              .conLai || 0
      );
}

// ======================================================
// TẠO THANH TOÁN PAYOO
// ======================================================

async function createPayooPayment() {
  const debt =
      selectedDebt.value;

  const amount =
      Number(
          paymentAmount.value || 0
      );

  // =========================
  // CHECK CÔNG NỢ
  // =========================

  if (!debt) {
    ElMessage.warning(
        "Vui lòng chọn công nợ cần thanh toán"
    );

    return;
  }

  // =========================
  // CHECK TIỀN
  // =========================

  if (
      !Number.isFinite(
          amount
      ) ||
      amount < 1000
  ) {
    ElMessage.warning(
        "Số tiền thanh toán tối thiểu là 1.000đ"
    );

    return;
  }

  // =========================
  // KHÔNG VƯỢT CÔNG NỢ
  // =========================

  if (
      amount >
      Number(
          debt.conLai || 0
      )
  ) {
    ElMessage.warning(
        "Số tiền thanh toán không được vượt công nợ còn lại"
    );

    return;
  }

  // =========================
  // CHECK SỐ DƯ CÒN LẠI
  // =========================

  const remainingAfterPayment =
      Number(
          debt.conLai || 0
      ) - amount;

  if (
      remainingAfterPayment >
      0 &&
      remainingAfterPayment <
      1000
  ) {
    ElMessage.warning(
        "Vui lòng thanh toán toàn bộ hoặc để công nợ còn lại tối thiểu 1.000đ"
    );

    return;
  }

  creatingPayment.value =
      true;

  try {
    // =========================
    // CALL BACKEND
    // =========================

    const transaction =
        await taoThanhToanCongNoPayoo(
            debt.maCongNo,
            amount
        );

    currentTransaction.value =
        transaction;

    payooStatus.value =
        "waiting";

    // =========================
    // NỘI DUNG QR
    // =========================

    const qrContent = [
      "PAYOO MOCK",

      `MA_GIAO_DICH=${transaction.maGiaoDich}`,

      `LOAI=${transaction.loaiGiaoDich}`,

      `MA_CONG_NO=${debt.maCongNo}`,

      `MA_DOI_TAC=${debt.maDoiTac}`,

      `SO_TIEN=${transaction.soTien}`,
    ].join("|");

    // =========================
    // SINH QR
    // =========================

    qrImage.value =
        await QRCode.toDataURL(
            qrContent,
            {
              width: 280,
              margin: 2,
            }
        );

    // Đóng dialog nhập tiền
    paymentDialogVisible.value =
        false;

    // Mở Payoo QR
    qrDialogVisible.value =
        true;
  } catch (error) {
    console.error(
        "Không tạo được giao dịch công nợ:",
        error
    );

    ElMessage.error(
        getErrorMessage(
            error,
            "Không tạo được giao dịch Payoo"
        )
    );
  } finally {
    creatingPayment.value =
        false;
  }
}

// ======================================================
// XÁC NHẬN PAYOO
// ======================================================

async function confirmPayment() {
  if (
      !currentTransaction
          .value
          ?.maGiaoDich ||
      payooStatus.value !==
      "waiting"
  ) {
    return;
  }

  payooStatus.value =
      "processing";

  try {
    // =========================
    // PAYOO CALLBACK
    // =========================

    const result =
        await confirmPayooTransaction(
            currentTransaction
                .value
                .maGiaoDich
        );

    currentTransaction.value =
        result;

    payooStatus.value =
        "success";

    // =========================
    // LOAD LẠI DATA
    // =========================

    await Promise.all([
      loadAllDebts(),
      loadSummary(),
      loadHistory(),
    ]);

    ElMessage.success(
        "Thanh toán công nợ thành công"
    );
  } catch (error) {
    console.error(
        "Callback Payoo công nợ lỗi:",
        error
    );

    payooStatus.value =
        "waiting";

    ElMessage.error(
        getErrorMessage(
            error,
            "Thanh toán công nợ thất bại"
        )
    );
  }
}

// ======================================================
// RESET PAYOO
// ======================================================

function resetPayoo() {
  currentTransaction.value =
      null;

  qrImage.value = "";

  payooStatus.value =
      "waiting";
}

// ======================================================
// XEM CHI TIẾT
// ======================================================

function showDebtInfo(
    item
) {
  ElMessageBox.alert(
      [
        `Đối tác: ${item.tenDoiTac}`,

        `Đơn hàng: DH${item.maDonHang}`,

        `Tổng công nợ: ${formatCurrency(
            item.tongTien
        )}`,

        `Đã thanh toán: ${formatCurrency(
            item.daThanhToan
        )}`,

        `Còn lại: ${formatCurrency(
            item.conLai
        )}`,

        `Hạn thanh toán: ${formatDate(
            item.hanThanhToan
        )}`,

        `Ghi chú: ${
            item.ghiChu ||
            "Không có"
        }`,
      ].join("\n"),

      `Chi tiết công nợ CN${item.maCongNo}`,

      {
        confirmButtonText:
            "Đóng",

        customClass:
            "debt-info-message-box",
      }
  );
}

// ======================================================
// XÁC ĐỊNH TRẠNG THÁI THỰC TẾ
// ======================================================

function getEffectiveStatusKey(
    item
) {
  // Có tiền còn nợ + quá hạn
  if (
      Number(
          item.conLai || 0
      ) > 0 &&
      isOverdue(item)
  ) {
    return "overdue";
  }

  // Đã thanh toán hết
  if (
      Number(
          item.conLai || 0
      ) <= 0 ||
      Number(
          item.trangThai
      ) === 2
  ) {
    return "paid";
  }

  // Thanh toán một phần
  if (
      Number(
          item.daThanhToan || 0
      ) > 0 ||
      Number(
          item.trangThai
      ) === 1
  ) {
    return "partial";
  }

  return "unpaid";
}

// ======================================================
// TEXT TRẠNG THÁI
// ======================================================

function getEffectiveStatusText(
    item
) {
  const key =
      getEffectiveStatusKey(
          item
      );

  const labels = {
    unpaid:
        "Chưa thanh toán",

    partial:
        "Thanh toán một phần",

    paid:
        "Đã thanh toán",

    overdue:
        "Quá hạn",
  };

  return (
      labels[key] ||
      item.trangThaiText ||
      "Không xác định"
  );
}

// ======================================================
// CLASS BADGE
// ======================================================

function getBadgeClass(
    item
) {
  const key =
      getEffectiveStatusKey(
          item
      );

  return (
      {
        unpaid:
            "badge-blue",

        partial:
            "badge-yellow",

        paid:
            "badge-green",

        overdue:
            "badge-red",
      }[key] ||
      "badge-gray"
  );
}

// ======================================================
// TEXT HẠN THANH TOÁN
// ======================================================

function getDueText(
    item
) {
  if (
      !item.hanThanhToan
  ) {
    return "Chưa đặt hạn";
  }

  if (
      Number(
          item.conLai || 0
      ) <= 0
  ) {
    return "Đã thanh toán";
  }

  const due =
      parseDate(
          item.hanThanhToan
      );

  if (!due) {
    return "Không xác định";
  }

  const today =
      startOfDay(
          new Date()
      );

  const diff =
      Math.ceil(
          (
              due.getTime() -
              today.getTime()
          ) /
          86400000
      );

  if (
      diff < 0
  ) {
    return `Quá ${Math.abs(
        diff
    )} ngày`;
  }

  if (
      diff === 0
  ) {
    return "Đến hạn hôm nay";
  }

  return `Còn ${diff} ngày`;
}

// ======================================================
// MÀU HẠN THANH TOÁN
// ======================================================

function getDueTextClass(
    item
) {
  if (
      Number(
          item.conLai || 0
      ) <= 0
  ) {
    return "text-green";
  }

  if (
      isOverdue(item)
  ) {
    return "text-red";
  }

  const due =
      parseDate(
          item.hanThanhToan
      );

  if (!due) {
    return "text-gray";
  }

  const diff =
      Math.ceil(
          (
              due.getTime() -
              startOfDay(
                  new Date()
              ).getTime()
          ) /
          86400000
      );

  return diff <= 7
      ? "text-yellow"
      : "text-gray";
}

// ======================================================
// KIỂM TRA QUÁ HẠN
// ======================================================

function isOverdue(
    item
) {
  const due =
      parseDate(
          item.hanThanhToan
      );

  if (!due) {
    return false;
  }

  return (
      due <
      startOfDay(
          new Date()
      )
  );
}

// ======================================================
// KIỂM TRA THÁNG HIỆN TẠI
// ======================================================

function isCurrentMonth(
    value
) {
  const date =
      parseDate(value);

  if (!date) {
    return false;
  }

  const now =
      new Date();

  return (
      date.getFullYear() ===
      now.getFullYear() &&
      date.getMonth() ===
      now.getMonth()
  );
}

// ======================================================
// PARSE DATE
// ======================================================

function parseDate(
    value
) {
  if (!value) {
    return null;
  }

  const date =
      new Date(value);

  if (
      Number.isNaN(
          date.getTime()
      )
  ) {
    return null;
  }

  return startOfDay(
      date
  );
}

// ======================================================
// START OF DAY
// ======================================================

function startOfDay(
    date
) {
  const result =
      new Date(date);

  result.setHours(
      0,
      0,
      0,
      0
  );

  return result;
}

// ======================================================
// FORMAT DATETIME
// ======================================================

function formatDateTime(
    value
) {
  if (!value) {
    return "—";
  }

  const date =
      new Date(value);

  if (
      Number.isNaN(
          date.getTime()
      )
  ) {
    return value;
  }

  return date.toLocaleString(
      "vi-VN",
      {
        day: "2-digit",

        month:
            "2-digit",

        year:
            "numeric",

        hour:
            "2-digit",

        minute:
            "2-digit",
      }
  );
}

// ======================================================
// FORMAT TRIỆU
// ======================================================

function formatMillions(
    value
) {
  const millions =
      Number(
          value || 0
      ) /
      1_000_000;

  return new Intl.NumberFormat(
      "vi-VN",
      {
        maximumFractionDigits:
            2,
      }
  ).format(
      millions
  );
}

// ======================================================
// CHỮ VIẾT TẮT ĐỐI TÁC
// ======================================================

function getPartnerInitials(
    name
) {
  const words =
      String(
          name || "DT"
      )
          .trim()
          .split(/\s+/)
          .filter(Boolean);

  if (
      !words.length
  ) {
    return "DT";
  }

  return words
      .slice(
          0,
          2
      )
      .map(
          (word) =>
              word[0]?.toUpperCase()
      )
      .join("");
}

// ======================================================
// LẤY MESSAGE LỖI
// ======================================================

function getErrorMessage(
    error,
    fallback
) {
  return (
      error?.response?.data
          ?.message ||
      error?.response?.data
          ?.detail ||
      error?.response?.data
          ?.error ||
      error?.message ||
      fallback
  );
}
</script>

<style
    scoped
    src="../../assets/styles/admin/TrangThanhToanCongNo.css"
></style>