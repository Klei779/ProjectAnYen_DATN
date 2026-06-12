<script setup>
import { ref, computed, onMounted } from "vue";
import api from "../../api/api.js";

const keyword = ref("");
const statusFilter = ref("Tất cả");

const customers = ref([]);

const API_URL = "/api/nhan-vien/khach-hang";

const loadCustomers = async () => {
  try {
    const res = await api.get(API_URL);
    customers.value = res.data;
  } catch (error) {
    console.error("Lỗi load khách hàng:", error);
  }
};

onMounted(loadCustomers);

const getAvatar = (name) => {
  if (!name) return "KH";

  const arr = name.trim().split(/\s+/);

  if (arr.length === 1) {
    return arr[0].substring(0, 2).toUpperCase();
  }

  return (arr[0][0] + arr[arr.length - 1][0]).toUpperCase();
};

const filteredCustomers = computed(() => {
  return customers.value.filter((item) => {
    const name = item.tenKhachHang || "";
    const phone = item.soDienThoai || "";
    const email = item.email || "";

    const matchKeyword =
        name.toLowerCase().includes(keyword.value.toLowerCase()) ||
        phone.includes(keyword.value) ||
        email.toLowerCase().includes(keyword.value.toLowerCase());

    if (statusFilter.value === "Tất cả") return matchKeyword;

    return matchKeyword && getCustomerStatus(item) === statusFilter.value;
  });
});

const getCustomerStatus = () => {
  return "Tư vấn mới";
};

const getCustomerStage = () => {
  return "Hỗ trợ khách hàng";
};

const statusClass = (status) => {
  if (status === "Đang làm việc" || status === "Hoàn thành") return "green";
  if (status === "Tạm dừng") return "orange";
  return "blue";
};

const stageClass = (stage) => {
  if (stage === "Hỗ trợ khách hàng") return "blue";
  if (stage === "Chốt sản phẩm") return "green";
  if (stage === "Chốt hợp đồng") return "orange";
  if (stage === "Quản lý dịch vụ") return "purple";
  return "blue";
};
</script>
<template>
  <div class="customer-management">
    <section class="page-content">

        <div class="card">
          <div class="filter-row">
            <div class="search-box">
              <i class="fa-solid fa-magnifying-glass"></i>
              <input
                  v-model="keyword"
                  placeholder="Tìm kiếm khách hàng, số điện thoại, email..."
              />
            </div>

            <select v-model="statusFilter">
              <option>Tất cả</option>
              <option>Tư vấn mới</option>
              <option>Đang làm việc</option>
              <option>Tạm dừng</option>
              <option>Hoàn thành</option>
            </select>

            <button class="filter-btn">
              <i class="fa-solid fa-filter"></i>
              Bộ lọc
            </button>

            <button class="add-btn">
              <i class="fa-solid fa-plus"></i>
              Thêm khách hàng
            </button>
          </div>

          <table class="customer-table">
            <thead>
            <tr>
              <th>Khách hàng</th>
              <th>Số điện thoại</th>
              <th>Email</th>
              <th>Trạng thái hiện tại</th>
              <th>Giai đoạn hiện tại</th>
              <th>Địa chỉ</th>
              <th>CCCD</th>
              <th>Thao tác</th>
            </tr>
            </thead>

            <tbody>
            <tr
                v-for="customer in filteredCustomers"
                :key="customer.maKhachHang"
            >
              <td>
                <div class="customer-cell">
                  <div class="avatar">
                    {{ getAvatar(customer.tenKhachHang) }}
                  </div>

                  <div>
                    <strong>{{ customer.tenKhachHang }}</strong>
                    <p>Khách hàng</p>
                  </div>
                </div>
              </td>

              <td>{{ customer.soDienThoai }}</td>
              <td>{{ customer.email }}</td>

              <td>
                  <span
                      class="badge"
                      :class="statusClass(getCustomerStatus(customer))"
                  >
                    {{ getCustomerStatus(customer) }}
                  </span>
              </td>

              <td>
                  <span
                      class="badge"
                      :class="stageClass(getCustomerStage(customer))"
                  >
                    {{ getCustomerStage(customer) }}
                  </span>
              </td>

              <td>{{ customer.diaChi }}</td>
              <td>{{ customer.cccd }}</td>

              <td>
                <button class="history-btn" @click="openHistory(customer)">
                  Xem lịch sử
                </button>

                <button class="more-btn">
                  <i class="fa-solid fa-ellipsis-vertical"></i>
                </button>
              </td>
            </tr>
            </tbody>
          </table>

          <div class="pagination-row">
            <p>
              Hiển thị 1 - {{ filteredCustomers.length }} của
              {{ filteredCustomers.length }} khách hàng
            </p>

            <div class="pagination">
              <button>
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <button class="active">1</button>
              <button>
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="legend">
          <div>
            <h5>Chú thích trạng thái & giai đoạn</h5>

            <div class="status-list">
              <span><span class="dot blue"></span> Tư vấn mới</span>
              <span><span class="dot green"></span> Đang làm việc</span>
              <span><span class="dot orange"></span> Tạm dừng</span>
              <span><span class="dot green"></span> Hoàn thành</span>
            </div>
          </div>

          <div>
            <h5>Giai đoạn làm việc</h5>
            <p>
              1. Hỗ trợ khách hàng &nbsp;&nbsp;
              2. Chốt sản phẩm &nbsp;&nbsp;
              3. Chốt hợp đồng &nbsp;&nbsp;
              4. Quản lý dịch vụ &nbsp;&nbsp;
              ✓ Hoàn thành
            </p>
          </div>
        </div>


    </section>
  </div>
</template>

<style scoped>
.customer-management {
  min-height: 100vh;
  background: #fafafa;
  color: #0f172a;
  font-family: Arial, sans-serif;
}

.page-content {
  padding: 26px 32px;
}

.tabs button {
  background: none;
  border: none;
  padding: 14px 0;
  cursor: pointer;
  font-size: 14px;
}

.tabs button.active {
  color: #d00018;
  font-weight: 700;
  border-bottom: 2px solid #d00018;
}

.card,
.legend,
.card {
  padding: 18px;
}

.filter-row {
  display: grid;
  grid-template-columns: 1fr 230px 92px 140px;
  gap: 14px;
  margin-bottom: 20px;
}

.search-box,
.filter-row select,
.filter-btn,
.add-btn {
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 7px;
  background: white;
  padding: 0 10px;
  font-size: 13px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-box input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 13px;
}

.add-btn {
  background: #e60019;
  color: white;
  font-weight: 600;
  border-color: #e60019;
  cursor: pointer;
}

.filter-btn {
  cursor: pointer;
}

.customer-table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table th,
.customer-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #f1f1f1;
  text-align: left;
  font-size: 13px;
}

.customer-table th {
  color: #475569;
  font-weight: 600;
}

.customer-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar,

.avatar {
  width: 38px;
  height: 38px;
  font-size: 13px;
}

.customer-cell p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 12px;
}

.badge {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 6px;
  font-size: 11px;
}

.badge.green {
  background: #eaf9ef;
  color: #17934a;
}

.badge.blue {
  background: #eaf3ff;
  color: #1d70d6;
}

.badge.orange {
  background: #fff6df;
  color: #d49000;
}


.more-btn {
  border: none;
  background: white;
  margin-left: 6px;
  cursor: pointer;
  font-size: 12px;
}

.pagination-row,
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pagination-row {
  margin-top: 16px;
  font-size: 13px;
}

.pagination {
  gap: 8px;
}

.pagination button {
  height: 32px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
}

.pagination button.active {
  background: #fff1f2;
  color: #d00018;
  border-color: #f3c3c9;
}

.legend {
  display: grid;
  margin-top: 18px;
  padding: 16px;
  font-size: 13px;
  grid-template-columns: 1.3fr 2fr;
  gap: 20px;
}

.legend h5 {
  margin: 0 0 12px;
}

.status-list {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: nowrap;
}

.status-list span {
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
}

.dot {
  width: 8px;
  height: 8px;
  display: inline-block;
  border-radius: 50%;
}

.dot.blue {
  background: #1d70d6;
}

.dot.green {
  background: #17934a;
}

.dot.orange {
  background: #d49000;
}


.customer-summary {
  padding: 22px;
  display: grid;
  grid-template-columns: 72px 1fr 1.2fr 1fr;
  gap: 22px;
  align-items: center;
}

.customer-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
}

.quick-icons {
  margin-top: 14px;
  display: flex;
  gap: 18px;
  color: #334155;
}

.summary-info {
  border-left: 1px solid #eee;
  padding-left: 20px;
}

.summary-info p,
.info-card p {
  color: #64748b;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13px;
}

.summary-info b,
.info-card b {
  color: #0f172a;
  font-weight: 500;
}

.timeline-box,
.progress-box,
.detail-history,
.info-card {
  padding: 22px;
}

.timeline-box h5,
.progress-box h5,
.detail-history h5,
.panel-title h5 {
  margin: 0;
  font-size: 15px;
}

.work-step-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
  margin-top: 22px;
}

.work-step {
  text-align: center;
}

.step-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 12px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 22px;
}

.step-icon.blue,
.progress-card.blue {
  background: #eef6ff;
  color: #1d70d6;
}

.step-icon.green,
.progress-card.green {
  background: #ecfbf1;
  color: #17934a;
}

.step-icon.orange,
.progress-card.orange {
  background: #fff7e8;
  color: #d49000;
}

.step-icon.purple,
.progress-card.purple {
  background: #f8efff;
  color: #8b3fd1;
}

.work-step h4 {
  margin: 0 0 6px;
  font-size: 14px;
}

.work-step p {
  color: #64748b;
  margin: 0;
  font-size: 13px;
}

.service-note p {
  display: block;
  color: #334155;
  margin: 8px 0;
  font-size: 13px;
}


@media (max-width: 1200px) {
  .history-layout {
    grid-template-columns: 1fr;
  }

  .filter-row,
  .customer-summary,
  .progress-grid,
  .work-step-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 800px) {
  .filter-row,
  .customer-summary,
  .progress-grid,
  .work-step-row,
  .legend {
    grid-template-columns: 1fr;
  }

  .customer-table {
    display: block;
    overflow-x: auto;
  }

  .status-list {
    flex-wrap: wrap;
  }
}
</style>