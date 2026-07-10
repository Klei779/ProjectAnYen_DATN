<template>
  <div class="debt-payment-page">
    <!-- Nút toggle sidebar -->
    <button class="sidebar-toggle-btn" @click="sidebarOpen = !sidebarOpen" :title="sidebarOpen ? 'Thu gọn' : 'Mở tóm tắt'">
      <i :class="sidebarOpen ? 'fa-solid fa-chevron-right' : 'fa-solid fa-chart-pie'"></i>
      <span v-if="!sidebarOpen" class="toggle-label">Tóm tắt</span>
    </button>

    <div class="main-content">
      <!-- Stats Cards -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="icon-box red-light">
            <i class="fa-solid fa-file-invoice-dollar text-red"></i>
          </div>
          <div class="stat-info">
            <p class="stat-title">Tổng công nợ phải trả</p>
            <h3 class="stat-value text-red">850,450,000 đ</h3>
            <p class="stat-desc">12 đối tác</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon-box yellow-light">
            <i class="fa-regular fa-calendar text-yellow"></i>
          </div>
          <div class="stat-info">
            <p class="stat-title">Đến hạn thanh toán</p>
            <h3 class="stat-value text-black">245,450,000 đ</h3>
            <p class="stat-desc">5 đối tác</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon-box blue-light">
            <i class="fa-regular fa-clock text-blue"></i>
          </div>
          <div class="stat-info">
            <p class="stat-title">Quá hạn thanh toán</p>
            <h3 class="stat-value text-black">105,000,000 đ</h3>
            <p class="stat-desc">2 đối tác</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon-box green-light">
            <i class="fa-regular fa-circle-check text-green"></i>
          </div>
          <div class="stat-info">
            <p class="stat-title">Đã thanh toán tháng này</p>
            <h3 class="stat-value text-black">620,000,000 đ</h3>
            <p class="stat-desc">8 giao dịch</p>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="filter-section">
        <div class="search-box">
          <el-input placeholder="Tìm kiếm đối tác, hóa đơn, đơn hàng..." prefix-icon="Search"></el-input>
        </div>
        <div class="filter-dropdowns">
          <el-select placeholder="Trạng thái: Tất cả" style="width: 160px">
            <el-option label="Tất cả" value="all"></el-option>
          </el-select>
          <el-select placeholder="Đối tác: Tất cả" style="width: 160px">
            <el-option label="Tất cả" value="all"></el-option>
          </el-select>
          <el-select placeholder="Thời gian: Tháng này" style="width: 180px">
            <el-option label="Tháng này" value="this_month"></el-option>
          </el-select>
        </div>
        <el-button type="danger" class="btn-thanh-toan">
          <i class="fa-solid fa-plus mr-1"></i> Thanh toán công nợ
        </el-button>
      </div>

      <!-- Table -->
      <div class="table-container">
        <table class="debt-table">
          <thead>
            <tr>
              <th>Đối tác</th>
              <th>Số hóa đơn</th>
              <th>Đơn hàng</th>
              <th>Ngày đến hạn</th>
              <th>Số tiền phải trả</th>
              <th>Công nợ còn lại</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in tableData" :key="item.id">
              <td>
                <div class="partner-cell">
                  <div class="partner-logo">
                    <img src="https://via.placeholder.com/40" alt="logo" v-if="!item.logoText" />
                    <span v-else>{{item.logoText}}</span>
                  </div>
                  <div>
                    <div class="partner-name">{{item.partnerName}}</div>
                    <div class="partner-code">Mã đối tác: {{item.partnerCode}}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="invoice-no">{{item.invoiceNo}}</div>
                <div class="date-sub">{{item.invoiceDate}}</div>
              </td>
              <td>
                <div class="order-no">{{item.orderNo}}</div>
                <div class="date-sub">{{item.orderDate}}</div>
              </td>
              <td>
                <div class="due-date">{{item.dueDate}}</div>
                <div :class="['days-left', item.statusClass]">{{item.daysLeftText}}</div>
              </td>
              <td><b>{{item.amount}}</b></td>
              <td><b class="text-red">{{item.remain}}</b></td>
              <td>
                <span :class="['status-badge', item.badgeClass]">
                  <span class="dot-status"></span> {{item.statusText}}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <el-button size="small" class="btn-action-thanh-toan">Thanh toán</el-button>
                  <el-button size="small" icon="MoreFilled" class="btn-more" style="border: none"></el-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div class="pagination-container">
          <span class="page-info">Hiển thị 1 đến 5 trong tổng số 12 kết quả</span>
          <div class="pagination-controls">
            <el-pagination background layout="prev, pager, next" :total="12" :page-size="5"></el-pagination>
            <el-select placeholder="10 / trang" style="width: 120px; margin-left: 10px;">
              <el-option label="10 / trang" value="10"></el-option>
            </el-select>
          </div>
        </div>
      </div>

      <!-- Process Section -->
      <div class="process-section">
        <h6 class="process-title">Quy trình thanh toán công nợ</h6>
        <div class="process-steps">
          <div class="step">
            <div class="step-icon-wrap">
               <span class="step-num bg-red text-white">1</span>
               <div class="step-icon"><i class="fa-solid fa-file-invoice"></i></div>
            </div>
            <h6>Tạo công nợ</h6>
            <p>Hệ thống tự động tạo công nợ khi đối tác giao sản phẩm/dịch vụ</p>
          </div>
          <div class="step-arrow"><i class="fa-solid fa-chevron-right"></i></div>
          <div class="step">
            <div class="step-icon-wrap">
               <span class="step-num bg-red text-white">2</span>
               <div class="step-icon"><i class="fa-solid fa-briefcase"></i></div>
            </div>
            <h6>Thu tiền từ khách hàng</h6>
            <p>Nhân viên thu tiền mặt hoặc chuyển khoản từ khách hàng</p>
          </div>
          <div class="step-arrow"><i class="fa-solid fa-chevron-right"></i></div>
          <div class="step">
            <div class="step-icon-wrap">
               <span class="step-num bg-red text-white">3</span>
               <div class="step-icon"><i class="fa-solid fa-building-columns"></i></div>
            </div>
            <h6>Tiền về tài khoản công ty</h6>
            <p>Tất cả tiền đều vào tài khoản tổng công ty</p>
          </div>
          <div class="step-arrow"><i class="fa-solid fa-chevron-right"></i></div>
          <div class="step">
            <div class="step-icon-wrap">
               <span class="step-num bg-red text-white">4</span>
               <div class="step-icon"><i class="fa-solid fa-handshake"></i></div>
            </div>
            <h6>Thanh toán cho đối tác</h6>
            <p>Công ty thanh toán một lượt theo kỳ hạn</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Sidebar Overlay -->
    <transition name="sidebar-slide">
      <div class="right-sidebar" v-show="sidebarOpen">
        <!-- Header sidebar -->
        <div class="sidebar-header">
          <!-- Tabs -->
          <div class="sidebar-tabs">
            <button
              class="sidebar-tab"
              :class="{ active: activeTab === 'summary' }"
              @click="activeTab = 'summary'"
            >
              <i class="fa-solid fa-chart-pie"></i> Tóm tắt
            </button>
            <button
              class="sidebar-tab"
              :class="{ active: activeTab === 'history' }"
              @click="activeTab = 'history'"
            >
              <i class="fa-solid fa-clock-rotate-left"></i> Lịch sử
            </button>
          </div>
          <button class="sidebar-close-btn" @click="sidebarOpen = false">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <!-- Tab: Tóm tắt -->
        <div v-show="activeTab === 'summary'" class="sidebar-tab-content">
          <div class="summary-chart-card">
            <div class="chart-container">
              <div class="donut-chart">
                <div class="donut-inner">
                  <span class="donut-val">850.45</span>
                  <span class="donut-label">Triệu đồng</span>
                </div>
              </div>
            </div>
            <div class="chart-legend">
              <div class="legend-item">
                <div><span class="dot bg-blue"></span> Chưa đến hạn</div>
                <span class="legend-val">500.00 tr (58.8%)</span>
              </div>
              <div class="legend-item">
                <div><span class="dot bg-yellow"></span> Đến hạn</div>
                <span class="legend-val">245.45 tr (28.8%)</span>
              </div>
              <div class="legend-item">
                <div><span class="dot bg-red"></span> Quá hạn</div>
                <span class="legend-val">105.00 tr (12.4%)</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab: Lịch sử -->
        <div v-show="activeTab === 'history'" class="sidebar-tab-content">
          <div class="recent-history-card">
            <div class="history-list">
              <div class="history-item" v-for="(h, index) in historyData" :key="index">
                <div class="h-icon bg-red-light"><i class="fa-solid fa-bolt text-red"></i></div>
                <div class="h-info">
                  <h6>{{h.name}}</h6>
                  <small>{{h.type}} - {{h.date}}</small>
                </div>
                <div class="h-amount text-green">{{h.amount}}</div>
              </div>
            </div>
            <el-button class="w-100 btn-view-all" plain>Xem tất cả</el-button>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Search, MoreFilled } from '@element-plus/icons-vue';

const sidebarOpen = ref(true);
const activeTab = ref('summary');

const tableData = ref([
  {
    id: 1,
    partnerName: 'Hoa Viên Bình An',
    partnerCode: 'DT001',
    logoText: 'HVBA',
    invoiceNo: 'HD20250701',
    invoiceDate: '01/07/2025',
    orderNo: 'DH20250701',
    orderDate: '30/06/2025',
    dueDate: '15/07/2025',
    daysLeftText: 'Còn 5 ngày',
    statusClass: 'text-yellow',
    amount: '120,000,000 đ',
    remain: '120,000,000 đ',
    statusText: 'Đến hạn',
    badgeClass: 'badge-yellow'
  },
  {
    id: 2,
    partnerName: 'Trại Hòm Phúc Lộc',
    partnerCode: 'DT002',
    logoText: 'TH',
    invoiceNo: 'HD20250628',
    invoiceDate: '28/06/2025',
    orderNo: 'DH20250628',
    orderDate: '27/06/2025',
    dueDate: '10/07/2025',
    daysLeftText: 'Quá 5 ngày',
    statusClass: 'text-red',
    amount: '80,000,000 đ',
    remain: '80,000,000 đ',
    statusText: 'Quá hạn',
    badgeClass: 'badge-red'
  },
  {
    id: 3,
    partnerName: 'Cơ Sở Mai Táng An Lạc',
    partnerCode: 'DT003',
    logoText: 'CS',
    invoiceNo: 'HD20250625',
    invoiceDate: '25/06/2025',
    orderNo: 'DH20250625',
    orderDate: '24/06/2025',
    dueDate: '09/07/2025',
    daysLeftText: 'Quá 6 ngày',
    statusClass: 'text-red',
    amount: '45,000,000 đ',
    remain: '25,000,000 đ',
    statusText: 'Quá hạn',
    badgeClass: 'badge-red'
  },
  {
    id: 4,
    partnerName: 'Dịch Vụ Tang Lễ Tâm Đức',
    partnerCode: 'DT004',
    logoText: 'DV',
    invoiceNo: 'HD20250703',
    invoiceDate: '03/07/2025',
    orderNo: 'DH20250703',
    orderDate: '02/07/2025',
    dueDate: '18/07/2025',
    daysLeftText: 'Còn 8 ngày',
    statusClass: 'text-gray',
    amount: '200,000,000 đ',
    remain: '200,000,000 đ',
    statusText: 'Chưa đến hạn',
    badgeClass: 'badge-blue'
  },
  {
    id: 5,
    partnerName: 'Trại Hòm Thiện Nhân',
    partnerCode: 'DT005',
    logoText: 'TH',
    invoiceNo: 'HD20250704',
    invoiceDate: '04/07/2025',
    orderNo: 'DH20250704',
    orderDate: '03/07/2025',
    dueDate: '19/07/2025',
    daysLeftText: 'Còn 9 ngày',
    statusClass: 'text-gray',
    amount: '150,000,000 đ',
    remain: '150,000,000 đ',
    statusText: 'Chưa đến hạn',
    badgeClass: 'badge-blue'
  },
]);

const historyData = ref([
  {
    name: 'Trại Hòm Phúc Lộc',
    type: 'Chuyển khoản',
    date: '05/07/2025',
    amount: '-80,000,000 đ'
  },
  {
    name: 'Hoa Viên Bình An',
    type: 'Tiền mặt',
    date: '03/07/2025',
    amount: '-120,000,000 đ'
  },
  {
    name: 'Cơ Sở An Lạc',
    type: 'Chuyển khoản',
    date: '01/07/2025',
    amount: '-25,000,000 đ'
  },
  {
    name: 'Dịch Vụ Tâm Đức',
    type: 'Chuyển khoản',
    date: '30/06/2025',
    amount: '-200,000,000 đ'
  },
  {
    name: 'Trại Hòm Thiện Nhân',
    type: 'Tiền mặt',
    date: '28/06/2025',
    amount: '-150,000,000 đ'
  }
]);

</script>
<style scoped src="../../assets/styles/admin/TrangThanhToanCongNo.css"></style>
