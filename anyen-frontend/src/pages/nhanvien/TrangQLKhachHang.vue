<script setup>
import { ref, computed } from "vue";

const activeTab = ref("list");
const keyword = ref("");
const statusFilter = ref("Tất cả");
const selectedCustomer = ref(null);

const customers = ref([
  {
    id: 1,
    name: "Nguyễn Văn A",
    type: "Cá nhân",
    phone: "0987 654 321",
    email: "nguyenvana@gmail.com",
    address: "123 Nguyễn Trãi, Q.1, TP.HCM",
    status: "Đang làm việc",
    stage: "Quản lý dịch vụ",
    startDate: "15/05/2024",
    updatedAt: "20/05/2024 10:30",
    source: "Giới thiệu",
    staff: "NV Hotline 03",
    avatar: "NA",
  },
  {
    id: 2,
    name: "Trần Thị B",
    type: "Cá nhân",
    phone: "0932 111 222",
    email: "tranthib@gmail.com",
    address: "45 Lê Lợi, Q.3, TP.HCM",
    status: "Hoàn thành",
    stage: "Hoàn thành",
    startDate: "01/05/2024",
    updatedAt: "18/05/2024 16:45",
    source: "Website",
    staff: "NV Hotline 03",
    avatar: "TB",
  },
  {
    id: 3,
    name: "Lê Văn C",
    type: "Cá nhân",
    phone: "0912 345 678",
    email: "levanc@gmail.com",
    address: "88 Nguyễn Văn Cừ, Q.5, TP.HCM",
    status: "Đang làm việc",
    stage: "Chốt hợp đồng",
    startDate: "18/05/2024",
    updatedAt: "20/05/2024 09:15",
    source: "Facebook",
    staff: "NV Hotline 03",
    avatar: "LVC",
  },
  {
    id: 4,
    name: "Hoàng Minh Tú",
    type: "Doanh nghiệp",
    phone: "0909 876 543",
    email: "tukc@minhtugroup.vn",
    address: "25 Hai Bà Trưng, Q.1, TP.HCM",
    status: "Đang làm việc",
    stage: "Chốt sản phẩm",
    startDate: "16/05/2024",
    updatedAt: "20/05/2024 08:20",
    source: "Hotline",
    staff: "NV Hotline 03",
    avatar: "HMT",
  },
  {
    id: 5,
    name: "Phạm Thị Dung",
    type: "Cá nhân",
    phone: "0977 333 444",
    email: "dungpham@gmail.com",
    address: "12 Võ Văn Tần, Q.3, TP.HCM",
    status: "Tư vấn mới",
    stage: "Hỗ trợ khách hàng",
    startDate: "20/05/2024",
    updatedAt: "20/05/2024 07:50",
    source: "Zalo",
    staff: "NV Hotline 03",
    avatar: "PTD",
  },
  {
    id: 6,
    name: "Nguyễn Thành Khoa",
    type: "Doanh nghiệp",
    phone: "0939 222 333",
    email: "khoa.nguyen@ntk.vn",
    address: "30 Pasteur, Q.1, TP.HCM",
    status: "Đang làm việc",
    stage: "Quản lý dịch vụ",
    startDate: "10/05/2024",
    updatedAt: "19/05/2024 17:10",
    source: "Giới thiệu",
    staff: "NV Hotline 03",
    avatar: "NTK",
  },
  {
    id: 7,
    name: "Võ Hoài Linh",
    type: "Cá nhân",
    phone: "0918 555 666",
    email: "linhvh@gmail.com",
    address: "77 Cách Mạng Tháng 8, Q.10, TP.HCM",
    status: "Tạm dừng",
    stage: "Chốt hợp đồng",
    startDate: "05/05/2024",
    updatedAt: "17/05/2024 15:00",
    source: "Website",
    staff: "NV Hotline 03",
    avatar: "VHL",
  },
  {
    id: 8,
    name: "Dương Quốc Huy",
    type: "Cá nhân",
    phone: "0908 123 456",
    email: "huydq@gmail.com",
    address: "56 Trường Chinh, Tân Bình",
    status: "Hoàn thành",
    stage: "Hoàn thành",
    startDate: "12/04/2024",
    updatedAt: "15/05/2024 11:30",
    source: "Hotline",
    staff: "NV Hotline 03",
    avatar: "DQR",
  },
]);

const currentCustomer = computed(() => selectedCustomer.value || customers.value[0]);

const filteredCustomers = computed(() => {
  return customers.value.filter((item) => {
    const matchKeyword =
        item.name.toLowerCase().includes(keyword.value.toLowerCase()) ||
        item.phone.includes(keyword.value) ||
        item.email.toLowerCase().includes(keyword.value.toLowerCase());

    const matchStatus =
        statusFilter.value === "Tất cả" || item.status === statusFilter.value;

    return matchKeyword && matchStatus;
  });
});

const openHistory = (customer) => {
  selectedCustomer.value = customer;
  activeTab.value = "history";
};

const workSteps = [
  {
    title: "Hỗ trợ khách hàng",
    desc: "Bắt đầu tư vấn",
    color: "blue",
    icon: "fa-solid fa-user-headset",
  },
  {
    title: "Chốt sản phẩm",
    desc: "Tạo đơn sản phẩm",
    color: "green",
    icon: "fa-solid fa-box-open",
  },
  {
    title: "Chốt hợp đồng",
    desc: "Hợp đồng xác nhận",
    color: "orange",
    icon: "fa-regular fa-calendar-days",
  },
  {
    title: "Quản lý dịch vụ",
    desc: "Quản lý đến khi hoàn tất",
    color: "purple",
    icon: "fa-regular fa-heart",
  },
];

const notes = [
  {
    text: "Khách quan tâm gói Hỏa táng Cao cấp, yêu cầu trang trí hoa sen.",
    time: "15/05/2024 09:20",
  },
  {
    text: "Đã gửi báo giá và tư vấn chi tiết qua Zalo.",
    time: "16/05/2024 10:10",
  },
];

const statusClass = (status) => {
  if (status === "Đang làm việc") return "green";
  if (status === "Hoàn thành") return "green";
  if (status === "Tạm dừng") return "orange";
  return "blue";
};

const stageClass = (stage) => {
  if (stage === "Hỗ trợ khách hàng") return "blue";
  if (stage === "Chốt sản phẩm") return "green";
  if (stage === "Chốt hợp đồng") return "orange";
  if (stage === "Quản lý dịch vụ") return "purple";
  return "green";
};
</script>

<template>
  <div class="customer-management">

      <section class="page-content">
        <div class="tabs">
          <button :class="{ active: activeTab === 'list' }" @click="activeTab = 'list'">
            Danh sách khách hàng
          </button>
          <button :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">
            Lịch sử làm việc
          </button>
        </div>

        <template v-if="activeTab === 'list'">
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
                <th>Ngày bắt đầu</th>
                <th>Ngày cập nhật cuối</th>
                <th>Thao tác</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="customer in filteredCustomers" :key="customer.id">
                <td>
                  <div class="customer-cell">
                    <div class="avatar">{{ customer.avatar }}</div>
                    <div>
                      <strong>{{ customer.name }}</strong>
                      <p>{{ customer.type }}</p>
                    </div>
                  </div>
                </td>
                <td>{{ customer.phone }}</td>
                <td>{{ customer.email }}</td>
                <td>
                    <span class="badge" :class="statusClass(customer.status)">
                      {{ customer.status }}
                    </span>
                </td>
                <td>
                    <span class="badge" :class="stageClass(customer.stage)">
                      {{ customer.stage }}
                    </span>
                </td>
                <td>{{ customer.startDate }}</td>
                <td>{{ customer.updatedAt }}</td>
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
              <p>Hiển thị 1 - {{ filteredCustomers.length }} của 21 khách hàng</p>

              <div class="pagination">
                <button><i class="fa-solid fa-chevron-left"></i></button>
                <button class="active">1</button>
                <button>2</button>
                <button>3</button>
                <button><i class="fa-solid fa-chevron-right"></i></button>
                <select>
                  <option>10 / trang</option>
                  <option>20 / trang</option>
                </select>
              </div>
            </div>
          </div>

          <div class="legend">
            <div>
              <h5>Chú thích trạng thái & giai đoạn</h5>
              <p>
                <span class="dot blue"></span> Tư vấn mới
                <span class="dot green"></span> Đang làm việc
                <span class="dot orange"></span> Tạm dừng
                <span class="dot green"></span> Hoàn thành
              </p>
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
        </template>

        <template v-if="activeTab === 'history'">
          <div class="history-layout">
            <section class="history-main">
              <div class="customer-summary">
                <div class="big-avatar">{{ currentCustomer.avatar }}</div>

                <div>
                  <h3 class="customer-name">{{ currentCustomer.name }}</h3>
                  <span class="badge green">{{ currentCustomer.status }}</span>

                  <div class="quick-icons">
                    <i class="fa-solid fa-phone"></i>
                    <i class="fa-regular fa-envelope"></i>
                    <i class="fa-regular fa-comment"></i>
                  </div>
                </div>

                <div class="summary-info">
                  <p>Số điện thoại <b>{{ currentCustomer.phone }}</b></p>
                  <p>Địa chỉ <b>{{ currentCustomer.address }}</b></p>
                </div>

                <div class="summary-info">
                  <p>Nguồn khách hàng <b>{{ currentCustomer.source }}</b></p>
                  <p>Ngày tạo hồ sơ <b>{{ currentCustomer.startDate }}</b></p>
                </div>
              </div>

              <div class="timeline-box">
                <h5>Lịch sử làm việc với khách hàng</h5>

                <div class="work-step-row">
                  <div
                      class="work-step"
                      v-for="(step, index) in workSteps"
                      :key="step.title"
                  >
                    <div class="step-icon" :class="step.color">
                      <i :class="step.icon"></i>
                    </div>
                    <h4>{{ index + 1 }}. {{ step.title }}</h4>
                    <p>{{ step.desc }}</p>
                  </div>
                </div>
              </div>

              <div class="progress-box">
                <h5>Tổng quan tiến trình</h5>

                <div class="progress-grid">
                  <div class="progress-card blue">
                    <h4>Hỗ trợ khách hàng</h4>
                    <p>15/05/2024 09:15</p>
                    <b>Đã hoàn thành</b>
                  </div>
                  <div class="progress-card green">
                    <h4>Chốt sản phẩm</h4>
                    <p>16/05/2024 14:30</p>
                    <b>Đã hoàn thành</b>
                  </div>
                  <div class="progress-card orange">
                    <h4>Chốt hợp đồng</h4>
                    <p>18/05/2024 10:45</p>
                    <b>Đã hoàn thành</b>
                  </div>
                  <div class="progress-card purple">
                    <h4>Quản lý dịch vụ</h4>
                    <p>Đang thực hiện</p>
                    <b>2 ghi chú</b>
                  </div>
                </div>
              </div>

              <div class="detail-history">
                <h5>Chi tiết lịch sử làm việc</h5>

                <div class="history-item blue">
                  <div class="circle"></div>
                  <div>
                    <h4>Hỗ trợ khách hàng</h4>
                    <p>Nhận đơn và bắt đầu tư vấn, hỗ trợ khách hàng</p>
                  </div>
                  <span>15/05/2024 09:15</span>
                </div>

                <div class="history-item green">
                  <div class="circle"></div>
                  <div>
                    <h4>Chốt sản phẩm</h4>
                    <p>Tạo đơn sản phẩm: DSP-2024-0516-0021</p>
                    <p>Sản phẩm: Gói Hỏa táng Cao cấp - Gói Hoa sen</p>
                  </div>
                  <span>16/05/2024 14:30</span>
                </div>

                <div class="history-item orange">
                  <div class="circle"></div>
                  <div>
                    <h4>Chốt hợp đồng</h4>
                    <p>Tạo hợp đồng: HD-2024-0518-0007</p>
                    <p>Hợp đồng xác nhận thành công</p>
                  </div>
                  <span>18/05/2024 10:45</span>
                </div>

                <div class="history-item purple">
                  <div class="circle"></div>
                  <div>
                    <h4>Quản lý dịch vụ</h4>
                    <p>Đang quản lý dịch vụ cho gia đình khách hàng</p>
                  </div>
                  <span>19/05/2024 - Hiện tại</span>
                </div>
              </div>
            </section>

            <aside class="right-panel">
              <div class="info-card">
                <div class="panel-title">
                  <h5>Thông tin khách hàng</h5>
                  <button><i class="fa-solid fa-pen"></i> Sửa</button>
                </div>

                <p>Họ và tên <b>{{ currentCustomer.name }}</b></p>
                <p>Số điện thoại <b>{{ currentCustomer.phone }}</b></p>
                <p>Email <b>{{ currentCustomer.email }}</b></p>
                <p>Địa chỉ <b>{{ currentCustomer.address }}</b></p>
                <p>Nguồn khách hàng <b>{{ currentCustomer.source }}</b></p>
                <p>Ngày tạo <b>{{ currentCustomer.startDate }}</b></p>
                <p>Nhân viên phụ trách <b>{{ currentCustomer.staff }}</b></p>
              </div>

              <div class="info-card">
                <div class="panel-title">
                  <h5>Ghi chú</h5>
                  <button><i class="fa-solid fa-plus"></i> Thêm ghi chú</button>
                </div>

                <div class="note-item" v-for="note in notes" :key="note.time">
                  <p>{{ note.text }}</p>
                  <small>NV Hotline 03 - {{ note.time }}</small>
                  <i class="fa-solid fa-ellipsis-vertical"></i>
                </div>
              </div>

              <div class="info-card">
                <div class="panel-title">
                  <h5>Ghi chú quản lý dịch vụ</h5>
                  <button><i class="fa-solid fa-plus"></i> Thêm</button>
                </div>

                <div class="service-note blue-line">
                  <b>20/05/2024 09:00</b>
                  <p>Gia đình thống nhất thời gian tổ chức lễ. Đã lên hệ nhà tang lễ.</p>
                  <small>NV Hotline 03</small>
                </div>

                <div class="service-note purple-line">
                  <b>21/05/2024 14:30</b>
                  <p>Hoàn tất lễ hỏa táng. Gia đình hài lòng về dịch vụ.</p>
                  <small>NV Hotline 03</small>
                </div>

                <button class="view-all">Xem tất cả ghi chú</button>
              </div>
            </aside>
          </div>
        </template>
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

.tabs {
  display: flex;
  gap: 36px;
  border-bottom: 1px solid #eee;
  margin-bottom: 24px;
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
.customer-summary,
.timeline-box,
.progress-box,
.detail-history,
.info-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 12px;
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
.big-avatar {
  border-radius: 50%;
  background: #fff1f2;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.avatar {
  width: 38px;
  height: 38px;
  font-size: 13px;
}

.big-avatar {
  width: 62px;
  height: 62px;
  font-size: 22px;
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

.badge.purple {
  background: #f5eaff;
  color: #8b3fd1;
}

.history-btn {
  height: 28px;
  padding: 0 9px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
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

.pagination button,
.pagination select {
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

.dot {
  width: 8px;
  height: 8px;
  display: inline-block;
  border-radius: 50%;
  margin-left: 14px;
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

.history-layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
}

.history-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.progress-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-top: 16px;
}

.progress-card {
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.progress-card h4,
.progress-card p {
  margin: 0 0 10px;
  font-size: 13px;
}

.history-item {
  display: grid;
  grid-template-columns: 20px 1fr auto;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f1f1;
}

.history-item .circle {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  margin-top: 4px;
}

.history-item.blue .circle {
  background: #1d70d6;
}

.history-item.green .circle {
  background: #17934a;
}

.history-item.orange .circle {
  background: #d49000;
}

.history-item.purple .circle {
  background: #8b3fd1;
}

.history-item h4 {
  margin: 0 0 6px;
  font-size: 14px;
}

.history-item p {
  margin: 4px 0;
  color: #64748b;
  font-size: 13px;
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title button {
  border: 1px solid #f0b6bd;
  background: white;
  color: #d00018;
  border-radius: 6px;
  padding: 7px 9px;
  font-size: 12px;
}

.note-item {
  border-bottom: 1px solid #eee;
  padding: 14px 20px 14px 0;
  position: relative;
}

.note-item p {
  margin: 0 0 8px;
  color: #334155;
  display: block;
  font-size: 13px;
}

.note-item small,
.service-note small {
  color: #64748b;
  font-size: 12px;
}

.note-item i {
  position: absolute;
  right: 0;
  top: 16px;
}

.service-note {
  border-left: 2px solid #ddd;
  padding: 0 0 18px 18px;
  margin-top: 16px;
}

.blue-line {
  border-color: #1d70d6;
}

.purple-line {
  border-color: #8b3fd1;
}

.service-note p {
  display: block;
  color: #334155;
  margin: 8px 0;
  font-size: 13px;
}

.view-all {
  width: 100%;
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 7px;
  background: white;
  margin-top: 14px;
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
  .card{
    padding: 18px;
  }
  .legend p {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: nowrap;
  }
}
</style>