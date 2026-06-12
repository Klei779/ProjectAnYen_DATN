<script setup>
import { computed, onMounted, ref } from "vue";
import api from "../../api/api.js";
import {Plus} from "@element-plus/icons-vue";
import PopTaoHopDong from "./PopTaoHopDong.vue";
import PopChiTietDonHang from "./PopChiTietDonHang.vue";
const keyword = ref("");
const trangThai = ref("Tất cả");
const showCreateContract  = ref(false);
const hopDongs = ref([]);
const selectedHopDong = ref(null);

const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const loading = ref(false);
const showDetailModal = ref(false);

const statuses = [
  "Tất cả",
  "Đã ký / Hiệu lực",
  "Chờ ký",
  "Đang thực hiện",
  "Hoàn thành",
  "Đã hủy",
];

const loadHopDongs = async () => {
  try {
    loading.value = true;

    const response = await api.get("/api/nhan-vien/hop-dong", {
      params: {
        keyword: keyword.value,
        trangThai: trangThai.value,
        page: page.value,
        pageSize: pageSize.value,
      },
    });

    console.log("DATA HỢP ĐỒNG:", response.data);

    hopDongs.value = response.data.items || [];
    total.value = response.data.total || 0;
  } catch (error) {
    console.error("Lỗi load hợp đồng:", error);
    console.log("STATUS:", error.response?.status);
    console.log("DATA ERROR:", error.response?.data);
    alert("Không thể tải danh sách hợp đồng");
  } finally {
    loading.value = false;
  }
};

const openDetail = async (id) => {
  try {
    const response = await api.get(`/api/nhan-vien/hop-dong/${id}`);
    selectedHopDong.value = response.data;
    showDetailModal.value = true;
  } catch (error) {
    console.error("Lỗi xem chi tiết hợp đồng:", error);
    alert("Không thể xem chi tiết hợp đồng");
  }
};

onMounted(() => {
  loadHopDongs();
});

const totalPages = computed(() => {
  return Math.max(Math.ceil(total.value / pageSize.value), 1);
});

const startItem = computed(() => {
  if (total.value === 0) return 0;
  return (page.value - 1) * pageSize.value + 1;
});

const endItem = computed(() => {
  return Math.min(page.value * pageSize.value, total.value);
});

const thongKe = computed(() => {
  const result = {
    "Tất cả": total.value,
    "Đã ký / Hiệu lực": 0,
    "Chờ ký": 0,
    "Đang thực hiện": 0,
    "Hoàn thành": 0,
    "Đã hủy": 0,
  };

  hopDongs.value.forEach((item) => {
    if (result[item.trangThai] !== undefined) {
      result[item.trangThai]++;
    }
  });

  return result;
});

const searchHopDong = () => {
  page.value = 1;
  loadHopDongs();
};

const changeStatus = (status) => {
  trangThai.value = status;
  page.value = 1;
  loadHopDongs();
};

const changePage = (newPage) => {
  if (newPage < 1 || newPage > totalPages.value) return;

  page.value = newPage;
  loadHopDongs();
};

const changePageSize = () => {
  page.value = 1;
  loadHopDongs();
};

const closeDetail = () => {
  showDetailModal.value = false;
  selectedHopDong.value = null;
};

const formatMoney = (value) => {
  if (value === null || value === undefined) return "0 đ";
  return Number(value).toLocaleString("vi-VN") + " đ";
};

const formatDate = (value) => {
  if (!value) return "---";
  const date = new Date(value);
  return date.toLocaleDateString("vi-VN");
};

const statusClass = (status) => {
  if (status === "Đã ký / Hiệu lực") return "green";
  if (status === "Chờ ký") return "orange";
  if (status === "Đang thực hiện") return "blue";
  if (status === "Hoàn thành") return "gray";
  if (status === "Đã hủy") return "red";
  return "gray";
};

const getPercent = (status) => {
  const all = total.value || 0;
  const count = thongKe.value[status] || 0;

  if (all === 0) return "0%";

  return ((count / all) * 100).toFixed(2) + "%";
};
</script>
<template>
  <div class="contract-page">
    <div class="top-bar">
      <div class="search-box">
        <i class="fa-solid fa-magnifying-glass"></i>

        <input
            v-model="keyword"
            type="text"
            placeholder="Tìm kiếm hợp đồng, khách hàng..."
            @keyup.enter="searchHopDong"
        />
      </div>

      <button class="filter-btn" @click="searchHopDong">
        <i class="fa-solid fa-filter"></i>
        Bộ lọc
      </button>

      <el-button type="primary" class="create-btn" @click="showCreateContract = true">
        <el-icon><Plus /></el-icon>
        Tạo hợp đồng
      </el-button>
    </div>

    <div class="status-tabs">
      <button
          v-for="item in statuses"
          :key="item"
          :class="{ active: trangThai === item }"
          @click="changeStatus(item)"
      >
        {{ item }}
      </button>
    </div>

    <div class="table-card">
      <table>
        <thead>
        <tr>
          <th>Mã hợp đồng</th>
          <th>Khách hàng</th>
          <th>Đơn hàng</th>
          <th>Ngày tạo</th>
          <th>Giá trị hợp đồng</th>
          <th>Trạng thái</th>
          <th class="text-right">Thao tác</th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="loading">
          <td colspan="7" class="empty-row">
            Đang tải dữ liệu...
          </td>
        </tr>

        <tr
            v-for="item in hopDongs"
            v-else
            :key="item.maHopDong"
        >
          <td>
            <strong>{{ item.maHopDongText }}</strong>
          </td>

          <td>
            <strong>{{ item.tenKhachHang || "---" }}</strong>
            <p>SĐT: {{ item.soDienThoai || "---" }}</p>
          </td>

          <td>
            <strong>{{ item.maDonHangText || "---" }}</strong>
          </td>

          <td>
            <p>{{ formatDate(item.ngayTaoDon) }}</p>
            <small>Ngày ký: {{ formatDate(item.ngayKyHD) }}</small>
          </td>

          <td>
            <strong>{{ formatMoney(item.giaTriHopDong) }}</strong>
          </td>

          <td>
              <span class="badge" :class="statusClass(item.trangThai)">
                {{ item.trangThai || "---" }}
              </span>
          </td>

          <td class="action-cell">
            <button
                class="detail-btn"
                @click="openDetail(item.maHopDong)"
            >
              Xem chi tiết
            </button>

            <button class="more-btn">
              <i class="fa-solid fa-ellipsis-vertical"></i>
            </button>
          </td>
        </tr>

        <tr v-if="!loading && hopDongs.length === 0">
          <td colspan="7" class="empty-row">
            Không có hợp đồng nào.
          </td>
        </tr>
        </tbody>
      </table>

      <div class="pagination-row">
        <p>
          Hiển thị {{ startItem }} - {{ endItem }} của {{ total }} hợp đồng
        </p>

        <div class="pagination">
          <button @click="changePage(page - 1)">
            <i class="fa-solid fa-chevron-left"></i>
          </button>

          <button
              v-for="p in totalPages"
              :key="p"
              :class="{ active: page === p }"
              @click="changePage(p)"
          >
            {{ p }}
          </button>

          <button @click="changePage(page + 1)">
            <i class="fa-solid fa-chevron-right"></i>
          </button>

          <select v-model="pageSize" @change="changePageSize">
            <option :value="10">10 / trang</option>
            <option :value="20">20 / trang</option>
            <option :value="50">50 / trang</option>
          </select>
        </div>
      </div>
    </div>

    <div class="summary-grid">
      <div class="summary-card">
        <p>Tổng hợp đồng</p>
        <h3>{{ total }}</h3>
        <small>Tất cả</small>
      </div>

      <div class="summary-card green">
        <p>Đã ký / Hiệu lực</p>
        <h3>{{ thongKe["Đã ký / Hiệu lực"] }}</h3>
        <small>{{ getPercent("Đã ký / Hiệu lực") }}</small>
      </div>

      <div class="summary-card orange">
        <p>Chờ ký</p>
        <h3>{{ thongKe["Chờ ký"] }}</h3>
        <small>{{ getPercent("Chờ ký") }}</small>
      </div>

      <div class="summary-card blue">
        <p>Đang thực hiện</p>
        <h3>{{ thongKe["Đang thực hiện"] }}</h3>
        <small>{{ getPercent("Đang thực hiện") }}</small>
      </div>

      <div class="summary-card green">
        <p>Hoàn thành</p>
        <h3>{{ thongKe["Hoàn thành"] }}</h3>
        <small>{{ getPercent("Hoàn thành") }}</small>
      </div>

      <div class="summary-card red">
        <p>Đã hủy</p>
        <h3>{{ thongKe["Đã hủy"] }}</h3>
        <small>{{ getPercent("Đã hủy") }}</small>
      </div>
    </div>

    <div
        v-if="showDetailModal"
        class="modal-overlay"
        @click.self="closeDetail"
    >
      <div class="modal">
        <div class="modal-header">
          <h3>Chi tiết hợp đồng</h3>

          <button @click="closeDetail">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div v-if="selectedHopDong" class="detail-grid">
          <p>
            Mã hợp đồng
            <b>{{ selectedHopDong.maHopDongText }}</b>
          </p>

          <p>
            Khách hàng
            <b>{{ selectedHopDong.tenKhachHang || "---" }}</b>
          </p>

          <p>
            Số điện thoại
            <b>{{ selectedHopDong.soDienThoai || "---" }}</b>
          </p>

          <p>
            Mã đơn hàng
            <b>{{ selectedHopDong.maDonHangText || "---" }}</b>
          </p>

          <p>
            Ngày tạo đơn
            <b>{{ formatDate(selectedHopDong.ngayTaoDon) }}</b>
          </p>

          <p>
            Ngày ký hợp đồng
            <b>{{ formatDate(selectedHopDong.ngayKyHD) }}</b>
          </p>

          <p>
            Ngày viết
            <b>{{ formatDate(selectedHopDong.ngayViet) }}</b>
          </p>

          <p>
            Giá trị hợp đồng
            <b>{{ formatMoney(selectedHopDong.giaTriHopDong) }}</b>
          </p>

          <p>
            Trạng thái
            <b>{{ selectedHopDong.trangThai || "---" }}</b>
          </p>
        </div>
      </div>
    </div>
    <PopTaoHopDong v-model="showCreateContract" />

    <!-- ── Popup chi tiết đơn hàng ── -->
    <PopChiTietDonHang
        v-model="showChiTiet"
        :don-hang="selectedDonHang"
        @huy-don="huyDon"
    />
  </div>
</template>

<style scoped>
.contract-page {
  min-height: 100vh;
  padding: 24px;
  background: #fafafa;
  color: #0f172a;
  font-family: Arial, sans-serif;
}

.top-bar {
  display: grid;
  grid-template-columns: 1fr 90px 150px;
  gap: 12px;
  margin-bottom: 16px;
}

.search-box {
  height: 38px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  gap: 10px;
}

.search-box i {
  color: #64748b;
  font-size: 13px;
}

.search-box input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 13px;
}

.filter-btn,
.create-btn {
  height: 38px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}

.filter-btn {
  background: white;
  border: 1px solid #e5e7eb;
}

.create-btn {
  border: 1px solid #ef233c;
  background: #ef233c;
  color: white;
  font-weight: 600;
}

.status-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.status-tabs button {
  border: 1px solid #e5e7eb;
  background: white;
  padding: 7px 12px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.status-tabs button.active {
  background: #fff1f2;
  color: #e11d48;
  border-color: #fecdd3;
  font-weight: 600;
}

.table-card {
  background: white;
  border: 1px solid #f1f1f1;
  border-radius: 12px;
  padding: 14px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid #f1f5f9;
  text-align: left;
  font-size: 13px;
}

th {
  color: #334155;
  font-weight: 700;
}

td p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

td small {
  color: #64748b;
  font-size: 12px;
}

.text-right {
  text-align: right;
}

.action-cell {
  text-align: right;
  white-space: nowrap;
}

.detail-btn {
  height: 28px;
  padding: 0 10px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
}

.detail-btn:hover {
  border-color: #ef233c;
  color: #ef233c;
}

.more-btn {
  border: none;
  background: white;
  margin-left: 8px;
  cursor: pointer;
  color: #64748b;
}

.badge {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.badge.green {
  background: #eaf9ef;
  color: #17934a;
}

.badge.orange {
  background: #fff6df;
  color: #d49000;
}

.badge.blue {
  background: #eaf3ff;
  color: #1d70d6;
}

.badge.gray {
  background: #f1f5f9;
  color: #475569;
}

.badge.red {
  background: #fff1f2;
  color: #e11d48;
}

.empty-row {
  text-align: center;
  color: #64748b;
  padding: 28px;
}

.pagination-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  font-size: 13px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination button,
.pagination select {
  height: 32px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  cursor: pointer;
}

.pagination button.active {
  background: #fff1f2;
  color: #e11d48;
  border-color: #fecdd3;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-top: 18px;
}

.summary-card {
  background: white;
  border: 1px solid #f1f1f1;
  border-radius: 12px;
  padding: 18px;
  text-align: center;
}

.summary-card p {
  margin: 0 0 10px;
  font-size: 12px;
  color: #64748b;
}

.summary-card h3 {
  margin: 0 0 8px;
  font-size: 24px;
}

.summary-card small {
  color: #64748b;
}

.summary-card.green h3,
.summary-card.green small {
  color: #17934a;
}

.summary-card.orange h3,
.summary-card.orange small {
  color: #d49000;
}

.summary-card.blue h3,
.summary-card.blue small {
  color: #1d70d6;
}

.summary-card.red h3,
.summary-card.red small {
  color: #e11d48;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: grid;
  place-items: center;
  z-index: 999;
}

.modal {
  width: 560px;
  max-width: calc(100vw - 32px);
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.modal-header button {
  border: none;
  background: white;
  cursor: pointer;
  font-size: 18px;
}

.detail-grid p {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.detail-grid b {
  color: #0f172a;
  text-align: right;
}

@media (max-width: 1100px) {
  .top-bar {
    grid-template-columns: 1fr;
  }

  .table-card {
    overflow-x: auto;
  }

  table {
    min-width: 900px;
  }

  .summary-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 700px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .pagination-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>